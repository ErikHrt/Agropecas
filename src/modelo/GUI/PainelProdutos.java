package modelo.GUI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import modelo.Excecoes.CodigoDuplicadoException;
import modelo.Excecoes.ItemNaoEncontradoException;
import modelo.Produtos.Produto;

final class PainelProdutos extends JPanel implements Atualizavel {

    private final Contexto ctx;
    private final DefaultTableModel modelo = Ui.modelo("ID", "Tipo", "Nome", "Descrição", "Custo",
            "Margem", "Preço de venda", "Estoque", "Peso (kg)", "Volume (m³)");
    private final JTable tabela = Ui.tabela(modelo);
    private final JTextField busca = new JTextField(24);
    private List<Produto> atuais = new ArrayList<>();

    PainelProdutos(Contexto ctx) 
    {
        super(new BorderLayout(8, 8));
        this.ctx = ctx;
        setBorder(new EmptyBorder(10, 10, 10, 10));

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
        tabela.setRowSorter(sorter);
        Ui.filtrar(busca, sorter);

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Buscar:"));
        topo.add(busca);

        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(Ui.botoes(
                Ui.botao("Novo", e -> novo()),
                Ui.botao("Editar", e -> editar()),
                Ui.botao("Remover", e -> remover()),
                Ui.botao("Atualizar", e -> atualizar())), BorderLayout.SOUTH);

        atualizar();
    }

    @Override
    public void atualizar() {
        try {
            // Guarda o produto selecionado: a tabela também é recarregada sozinha pelo monitor de estoque
            Integer idSelecionado = null;
            int linhaSelecionada = tabela.getSelectedRow();
            if (linhaSelecionada >= 0) {
                idSelecionado = atuais.get(tabela.convertRowIndexToModel(linhaSelecionada)).getId();
            }

            List<Produto> lidos = ctx.produtos.listarTodos();
            modelo.setRowCount(0);
            for (Produto p : lidos) {
                modelo.addRow(new Object[] {
                        p.getId(),
                        Ui.nomeTipo(p.getTipo()),
                        p.getNome(),
                        p.getDescricao(),
                        Ui.moeda(p.getPreco().getValorUnitario()),
                        Ui.num(p.getPreco().getMargemLucro() * 100) + "%",
                        Ui.moeda(p.getPreco().getValordeVenda()),
                        p.getQuantidadeEstoque(),
                        Ui.num(p.getEspecificacaoFisica().getPesoKg()),
                        Ui.num(p.getEspecificacaoFisica().calcularVolumeMetrosCubicos())
                });
            }
            atuais = lidos;

            if (idSelecionado != null) {
                for (int i = 0; i < atuais.size(); i++) {
                    if (atuais.get(i).getId() == idSelecionado) {
                        int linhaNaTela = tabela.convertRowIndexToView(i);
                        tabela.setRowSelectionInterval(linhaNaTela, linhaNaTela);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            Ui.erro(this, e);
        }
    }

    private Produto selecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            Ui.aviso(this, "Selecione um produto na tabela.");
            return null;
        }
        return atuais.get(tabela.convertRowIndexToModel(linha));
    }

    private int proximoId() throws IOException {
        int maior = 0;
        for (Produto p : ctx.produtos.listarTodos()) {
            maior = Math.max(maior, p.getId());
        }
        return maior + 1;
    }

    private void novo() {
        try {
            Produto p = FormProduto.abrir(this, "Novo produto", null, proximoId(), true, null, true);
            if (p == null) {
                return;
            }
            ctx.produtos.salvar(p);
            atualizar();
        } catch (CodigoDuplicadoException | IOException e) {
            Ui.erro(this, e);
        }
    }

    private void editar() {
        Produto atual = selecionado();
        if (atual == null) {
            return;
        }
        Produto editado = FormProduto.abrir(this, "Editar produto", atual, atual.getId(), false, null, true);
        if (editado == null) {
            return;
        }
        try {
            ctx.produtos.atualizar(editado);
            atualizar();
        } catch (ItemNaoEncontradoException | IOException e) {
            Ui.erro(this, e);
        }
    }

    private void remover() {
        Produto p = selecionado();
        if (p == null || !Ui.confirmar(this, "Remover o produto \"" + p.getNome() + "\" (ID " + p.getId() + ")?")) {
            return;
        }
        try {
            ctx.produtos.remover(p.getId());
            atualizar();
        } catch (ItemNaoEncontradoException | IOException e) {
            Ui.erro(this, e);
        }
    }
}
