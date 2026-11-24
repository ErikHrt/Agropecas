package modelo.GUI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
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
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import modelo.Excecoes.CodigoDuplicadoException;
import modelo.Excecoes.EstoqueInsuficienteException;
import modelo.Excecoes.ItemNaoEncontradoException;
import modelo.Excecoes.ValorInvalidoException;
import modelo.Operacoes.ItemOps;
import modelo.Operacoes.Venda;
import modelo.Pessoas.Cliente;
import modelo.Pessoas.Funcionario;
import modelo.Produtos.Produto;

final class PainelNovaVenda extends JPanel implements Atualizavel {

    private final Contexto ctx;

    private final JComboBox<Cliente> cliente = new JComboBox<>();
    private final JComboBox<Funcionario> funcionario = new JComboBox<>();
    private final JComboBox<String> pagamento = new JComboBox<>(
            new String[] {"PIX", "Dinheiro", "Cartão de débito", "Cartão de crédito", "Boleto"});
    private final JComboBox<Produto> produto = new JComboBox<>();
    private final JSpinner quantidade = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));

    private final DefaultTableModel modelo = Ui.modelo("ID", "Produto", "Quantidade", "Valor unitário", "Subtotal");
    private final JTable tabela = Ui.tabela(modelo);
    private final JLabel total = new JLabel();

    private final Map<Integer, Produto> porId = new HashMap<>();
    private Venda venda;   // null enquanto não há carrinho em andamento

    PainelNovaVenda(Contexto ctx) {
        super(new BorderLayout(8, 8));
        this.ctx = ctx;
        setBorder(new EmptyBorder(10, 10, 10, 10));

        Ui.rotular(cliente, c -> c.getNome() + " — " + c.getCpf());
        Ui.rotular(funcionario, f -> f.getNome() + " — " + f.getCpf());
        Ui.rotular(produto, p -> p.getId() + " - " + p.getNome()
                + "  (estoque " + p.getQuantidadeEstoque() + ", " + Ui.moeda(p.getPreco().getValordeVenda()) + ")");
        total.setFont(total.getFont().deriveFont(Font.BOLD, 16f));

        JPanel linhaPessoas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        linhaPessoas.add(new JLabel("Cliente:"));
        linhaPessoas.add(cliente);
        linhaPessoas.add(new JLabel("Funcionário:"));
        linhaPessoas.add(funcionario);
        linhaPessoas.add(new JLabel("Pagamento:"));
        linhaPessoas.add(pagamento);

        JPanel linhaProduto = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        linhaProduto.add(new JLabel("Produto:"));
        linhaProduto.add(produto);
        linhaProduto.add(new JLabel("Qtd:"));
        linhaProduto.add(quantidade);
        linhaProduto.add(Ui.botao("Adicionar ao carrinho", e -> adicionar()));

        JPanel norte = new JPanel(new java.awt.GridLayout(0, 1));
        norte.add(linhaPessoas);
        norte.add(linhaProduto);

        JPanel sul = new JPanel(new BorderLayout());
        sul.add(Ui.botoes(
                Ui.botao("Remover item", e -> removerItem()),
                Ui.botao("Cancelar venda", e -> cancelar()),
                Ui.botao("Finalizar venda", e -> finalizar())), BorderLayout.WEST);
        sul.add(total, BorderLayout.EAST);

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(sul, BorderLayout.SOUTH);

        atualizar();
    }

    @Override
    public void atualizar() {
        try {
            // Cliente e funcionário ficam travados enquanto há venda em andamento
            if (venda == null) {
                recarregar(cliente, ctx.clientes.listarTodos());
                recarregar(funcionario, ctx.funcionarios.listarTodos());
            }
            List<Produto> produtos = ctx.produtos.listarTodos();
            porId.clear();
            for (Produto p : produtos) {
                porId.put(p.getId(), p);
            }
            recarregar(produto, produtos);
            redesenhar();
        } catch (IOException e) {
            Ui.erro(this, e);
        }
    }

    private static <T> void recarregar(JComboBox<T> combo, List<T> itens) {
        combo.setModel(new DefaultComboBoxModel<>(new Vector<>(itens)));
    }

    private void redesenhar() {
        modelo.setRowCount(0);
        if (venda != null) {
            for (ItemOps i : venda.getCarrinho()) {
                Produto p = porId.get(i.getIdProduto());
                modelo.addRow(new Object[] {
                        i.getIdProduto(),
                        p == null ? "?" : p.getNome(),
                        i.getQtd(),
                        Ui.moeda(i.getValorUn()),
                        Ui.moeda(i.getQtd() * i.getValorUn())
                });
            }
        }
        total.setText("Total: " + Ui.moeda(venda == null ? 0 : venda.getValor()));
        cliente.setEnabled(venda == null);
        funcionario.setEnabled(venda == null);
    }

    private void adicionar() {
        Cliente c = (Cliente) cliente.getSelectedItem();
        Funcionario f = (Funcionario) funcionario.getSelectedItem();
        Produto p = (Produto) produto.getSelectedItem();
        if (c == null || f == null || p == null) {
            Ui.aviso(this, "Cadastre e selecione um cliente, um funcionário e um produto.");
            return;
        }

        boolean nova = (venda == null);
        if (nova) {
            venda = new Venda(c, f);
        }
        try {
            venda.adicionarAoCarrinho(p, (Integer) quantidade.getValue());
            quantidade.setValue(1);
        } catch (ValorInvalidoException | EstoqueInsuficienteException e) {
            if (nova) {
                venda = null;
            }
            Ui.erro(this, e);
        }
        redesenhar();
    }

    private void removerItem() {
        int linha = tabela.getSelectedRow();
        if (venda == null || linha < 0) {
            Ui.aviso(this, "Selecione um item do carrinho.");
            return;
        }
        int id = (Integer) modelo.getValueAt(tabela.convertRowIndexToModel(linha), 0);
        venda.removerDoCarrinho(id);
        if (venda.getCarrinho().isEmpty()) {
            venda = null;   // carrinho vazio: libera cliente e funcionário
        }
        redesenhar();
    }

    private void cancelar() {
        if (venda != null && Ui.confirmar(this, "Descartar a venda em andamento?")) {
            venda = null;
            redesenhar();
        }
    }

    private void finalizar() {
        if (venda == null || venda.getCarrinho().isEmpty()) {
            Ui.aviso(this, "O carrinho está vazio.");
            return;
        }
        venda.setMetodoPagamento((String) pagamento.getSelectedItem());
        try {
            ctx.servicoVenda.finalizarVenda(venda);
            Ui.info(this, "Venda #" + venda.getId() + " registrada.\nTotal: " + Ui.moeda(venda.getValor()));
            venda = null;
            atualizar();
        } catch (IOException | ItemNaoEncontradoException | CodigoDuplicadoException
                 | EstoqueInsuficienteException e) {
            Ui.erro(this, e);
        } catch (RuntimeException e) {
            // IllegalStateException (venda sem itens ou sem método de pagamento)
            Ui.erro(this, e);
        }
    }
}
