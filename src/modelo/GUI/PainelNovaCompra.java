package modelo.GUI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import modelo.Excecoes.CodigoDuplicadoException;
import modelo.Excecoes.ItemNaoEncontradoException;
import modelo.Excecoes.ValorInvalidoException;
import modelo.Operacoes.Compra;
import modelo.Operacoes.ItemOps;
import modelo.Pessoas.Funcionario;
import modelo.Repositorio.ArquivoTexto;
import modelo.Produtos.Produto;

final class PainelNovaCompra extends JPanel implements Atualizavel {

    private final Contexto ctx;

    private final JComboBox<Funcionario> funcionario = new JComboBox<>();
    private final JComboBox<Produto> cadastrados = new JComboBox<>();
    private final JTextField idProduto = new JTextField(6);
    private final JSpinner quantidade = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));
    private final JTextField valorUn = new JTextField(8);
    private final JTextField local = new JTextField(14);
    private final JLabel situacao = new JLabel(" ");

    private final DefaultTableModel modelo = Ui.modelo("ID", "Produto", "Quantidade", "Valor unitário", "Local", "Subtotal");
    private final JTable tabela = Ui.tabela(modelo);
    private final JLabel total = new JLabel();

    private final Map<Integer, Produto> porId = new HashMap<>();
    private Compra compra;            // null enquanto não há compra em andamento
    private boolean carregando;       // evita que recarregar o combo preencha o campo de ID

    PainelNovaCompra(Contexto ctx) {
        super(new BorderLayout(8, 8));
        this.ctx = ctx;
        setBorder(new EmptyBorder(10, 10, 10, 10));

        Ui.rotular(funcionario, f -> f.getNome() + " — " + f.getCpf());
        Ui.rotular(cadastrados, p -> p.getId() + " - " + p.getNome());
        total.setFont(total.getFont().deriveFont(Font.BOLD, 16f));

        // Escolher um produto já cadastrado preenche o ID; produto novo é só digitar um ID inexistente
        cadastrados.addActionListener(e -> {
            Produto p = (Produto) cadastrados.getSelectedItem();
            if (!carregando && p != null) {
                idProduto.setText(String.valueOf(p.getId()));
            }
        });
        Ui.aoDigitar(idProduto, this::atualizarSituacao);

        JPanel linhaResp = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        linhaResp.add(new JLabel("Funcionário responsável:"));
        linhaResp.add(funcionario);

        JPanel linhaItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        linhaItem.add(new JLabel("Produto cadastrado:"));
        linhaItem.add(cadastrados);
        linhaItem.add(new JLabel("ID:"));
        linhaItem.add(idProduto);
        linhaItem.add(new JLabel("Qtd:"));
        linhaItem.add(quantidade);
        linhaItem.add(new JLabel("Valor unitário (R$):"));
        linhaItem.add(valorUn);
        linhaItem.add(new JLabel("Local:"));
        linhaItem.add(local);
        linhaItem.add(Ui.botao("Adicionar item", e -> adicionar()));

        JPanel linhaSituacao = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        linhaSituacao.add(situacao);

        JPanel norte = new JPanel(new java.awt.GridLayout(0, 1));
        norte.add(linhaResp);
        norte.add(linhaItem);
        norte.add(linhaSituacao);

        JPanel sul = new JPanel(new BorderLayout());
        sul.add(Ui.botoes(
                Ui.botao("Remover produto", e -> removerItem()),
                Ui.botao("Cancelar compra", e -> cancelar()),
                Ui.botao("Finalizar compra", e -> finalizar())), BorderLayout.WEST);
        sul.add(total, BorderLayout.EAST);

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(sul, BorderLayout.SOUTH);

        atualizar();
    }

    @Override
    public void atualizar() {
        try {
            carregando = true;
            if (compra == null) {
                funcionario.setModel(new DefaultComboBoxModel<>(new Vector<>(ctx.funcionarios.listarTodos())));
            }
            List<Produto> produtos = ctx.produtos.listarTodos();
            porId.clear();
            for (Produto p : produtos) {
                porId.put(p.getId(), p);
            }
            cadastrados.setModel(new DefaultComboBoxModel<>(new Vector<>(produtos)));
            cadastrados.setSelectedIndex(-1);
            carregando = false;
            atualizarSituacao();
            redesenhar();
        } catch (IOException e) {
            carregando = false;
            Ui.erro(this, e);
        }
    }

    private void atualizarSituacao() {
        try {
            int id = Integer.parseInt(idProduto.getText().trim());
            Produto p = porId.get(id);
            situacao.setText(p != null
                    ? "Produto cadastrado: " + p.getNome() + " (estoque atual " + p.getQuantidadeEstoque() + ")"
                    : "Produto novo: o cadastro será pedido ao finalizar a compra.");
        } catch (NumberFormatException e) {
            situacao.setText(" ");
        }
    }

    private void redesenhar() {
        modelo.setRowCount(0);
        if (compra != null) {
            for (ItemOps i : compra.getItensComprados()) {
                Produto p = porId.get(i.getIdProduto());
                modelo.addRow(new Object[] {
                        i.getIdProduto(),
                        p == null ? "(produto novo)" : p.getNome(),
                        i.getQtd(),
                        Ui.moeda(i.getValorUn()),
                        i.getLocalDaCompra(),
                        Ui.moeda(i.getQtd() * i.getValorUn())
                });
            }
        }
        total.setText("Total: " + Ui.moeda(compra == null ? 0 : compra.getValor()));
        funcionario.setEnabled(compra == null);
    }

    private void adicionar() {
        Funcionario f = (Funcionario) funcionario.getSelectedItem();
        if (f == null) {
            Ui.aviso(this, "Cadastre e selecione o funcionário responsável pela compra.");
            return;
        }
        try {
            int id = Ui.inteiro(idProduto.getText(), "ID do produto");
            if (id <= 0) {
                throw new ValorInvalidoException("O ID do produto deve ser um inteiro positivo.");
            }
            double valor = Ui.numero(valorUn.getText(), "Valor unitário");
            if (valor < 0) {
                throw new ValorInvalidoException("O valor unitário não pode ser negativo.");
            }
            String onde = Ui.textoLocal(local.getText());

            if (compra == null) {
                compra = new Compra(f);
            }
            compra.adicionarItemNosComprados(new ItemOps(id, (Integer) quantidade.getValue(), valor, onde));

            idProduto.setText("");
            valorUn.setText("");
            quantidade.setValue(1);
            cadastrados.setSelectedIndex(-1);
            redesenhar();
        } catch (ValorInvalidoException e) {
            Ui.erro(this, e);
        }
    }

    private void removerItem() {
        int linha = tabela.getSelectedRow();
        if (compra == null || linha < 0) {
            Ui.aviso(this, "Selecione um item da compra.");
            return;
        }
        int id = (Integer) modelo.getValueAt(tabela.convertRowIndexToModel(linha), 0);
        compra.removerDaListaDeCompras(id);   // remove o produto de todos os locais
        if (compra.getItensComprados().isEmpty()) {
            compra = null;
        }
        redesenhar();
    }

    private void cancelar() {
        if (compra != null && Ui.confirmar(this, "Descartar a compra em andamento?")) {
            compra = null;
            redesenhar();
        }
    }

    private void finalizar() {
        if (compra == null || compra.getItensComprados().isEmpty()) {
            Ui.aviso(this, "A compra não tem itens.");
            return;
        }
        try {
            // Pede o cadastro de TODOS os produtos novos antes de gravar qualquer coisa:
            // se o usuário cancelar um deles, nada é alterado nos arquivos.
            List<ItemOps> itens = new ArrayList<>(compra.getItensComprados());
            Map<Integer, Produto> novos = new HashMap<>();
            for (ItemOps item : ctx.servicoCompra.filtrarNaoCadastrados(itens)) {
                Produto p = FormProduto.abrir(this,
                        "Cadastrar produto novo (ID " + item.getIdProduto() + ")",
                        null, item.getIdProduto(), false, item.getValorUn(), false);
                if (p == null) {
                    Ui.aviso(this, "Compra não finalizada: o cadastro do produto " + item.getIdProduto()
                            + " foi cancelado. Nada foi gravado.");
                    return;
                }
                novos.put(item.getIdProduto(), p);
            }

            synchronized (ArquivoTexto.TRAVA_GLOBAL) {   // operação em vários arquivos: foto do backup nunca fica pela metade
                ctx.servicoCompra.finalizarCompra(compra, item -> novos.get(item.getIdProduto()));
            }

            Ui.info(this, "Compra #" + compra.getId() + " registrada.\nTotal: " + Ui.moeda(compra.getValor()));
            compra = null;
            atualizar();
        } catch (IOException | ItemNaoEncontradoException | CodigoDuplicadoException e) {
            Ui.erro(this, e);
        } catch (RuntimeException e) {
            Ui.erro(this, e);
        }
    }
}
