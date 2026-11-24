package modelo.GUI;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import modelo.Operacoes.Compra;
import modelo.Operacoes.Frete;
import modelo.Operacoes.Venda;

/** Consulta de vendas e compras já registradas, com os itens da operação selecionada. */
final class PainelHistorico extends JPanel implements Atualizavel {

    private final Contexto ctx;

    private final DefaultTableModel mVendas = Ui.modelo("ID", "Data", "Cliente", "Funcionário", "Pagamento", "Total");
    private final DefaultTableModel mCompras = Ui.modelo("ID", "Data", "Funcionário", "Total");
    private final DefaultTableModel mFretes = Ui.modelo("ID", "Data", "Cliente", "Funcionário", "Destino", "Veículo", "Distância (km)", "Valor");
    private final JTable tVendas = Ui.tabela(mVendas);
    private final JTable tCompras = Ui.tabela(mCompras);
    private final JTable tFretes = Ui.tabela(mFretes);
    private final JTextArea detalhe = new JTextArea();
    private final JTabbedPane abas = new JTabbedPane();

    private List<Venda> vendas = new ArrayList<>();
    private List<Compra> compras = new ArrayList<>();
    private List<Frete> fretes = new ArrayList<>();

    PainelHistorico(Contexto ctx) {
        super(new BorderLayout(8, 8));
        this.ctx = ctx;
        setBorder(new EmptyBorder(10, 10, 10, 10));

        detalhe.setEditable(false);
        detalhe.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        abas.addTab("Vendas", new JScrollPane(tVendas));
        abas.addTab("Compras", new JScrollPane(tCompras));
        abas.addTab("Fretes", new JScrollPane(tFretes));
        abas.addChangeListener(e -> detalhe.setText(""));

        tVendas.getSelectionModel().addListSelectionListener(e -> {
            int linha = tVendas.getSelectedRow();
            if (!e.getValueIsAdjusting() && linha >= 0) {
                Venda v = vendas.get(tVendas.convertRowIndexToModel(linha));
                detalhe.setText(v.toString() + "Total: " + Ui.moeda(v.getValor()));
                detalhe.setCaretPosition(0);
            }
        });
        tCompras.getSelectionModel().addListSelectionListener(e -> {
            int linha = tCompras.getSelectedRow();
            if (!e.getValueIsAdjusting() && linha >= 0) {
                Compra c = compras.get(tCompras.convertRowIndexToModel(linha));
                detalhe.setText(c.toString() + "Total: " + Ui.moeda(c.getValor()));
                detalhe.setCaretPosition(0);
            }
        });

        tFretes.getSelectionModel().addListSelectionListener(e -> {
            int linha = tFretes.getSelectedRow();
            if (!e.getValueIsAdjusting() && linha >= 0) {
                Frete fr = fretes.get(tFretes.convertRowIndexToModel(linha));
                detalhe.setText(fr.toString() + "Valor do frete: " + Ui.moeda(fr.getValor()));
                detalhe.setCaretPosition(0);
            }
        });

        JSplitPane divisao = new JSplitPane(JSplitPane.VERTICAL_SPLIT, abas, new JScrollPane(detalhe));
        divisao.setResizeWeight(0.6);

        add(divisao, BorderLayout.CENTER);
        add(Ui.botoes(Ui.botao("Atualizar", e -> atualizar())), BorderLayout.SOUTH);

        atualizar();
    }

    @Override
    public void atualizar() {
        detalhe.setText("");
        try {
            List<Venda> v = ctx.vendas.listarTodos();
            mVendas.setRowCount(0);
            for (Venda venda : v) {
                mVendas.addRow(new Object[] {
                        venda.getId(), venda.getData().toString(), venda.getCliente().getNome(),
                        venda.getFuncionario().getNome(), venda.getMetodoPagamento(), Ui.moeda(venda.getValor())
                });
            }
            vendas = v;

            List<Compra> c = ctx.compras.listarTodos();
            mCompras.setRowCount(0);
            for (Compra compra : c) {
                mCompras.addRow(new Object[] {
                        compra.getId(), compra.getData().toString(),
                        compra.getFuncionario().getNome(), Ui.moeda(compra.getValor())
                });
            }
            compras = c;

            List<Frete> fr = ctx.fretes.listarTodos();
            mFretes.setRowCount(0);
            for (Frete frete : fr) {
                mFretes.addRow(new Object[] {
                        frete.getId(), frete.getData().toString(), frete.getCliente().getNome(),
                        frete.getFuncionario().getNome(), frete.getDestino().getEnderecoCompleto(),
                        frete.getVeiculo().getDescricao(), Ui.num(frete.getDistanciaKm()), Ui.moeda(frete.getValor())
                });
            }
            fretes = fr;
        } catch (IOException e) {
            Ui.erro(this, e);
        }
    }
}
