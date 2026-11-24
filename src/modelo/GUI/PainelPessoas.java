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
import modelo.Operacoes.Compra;
import modelo.Operacoes.Frete;
import modelo.Operacoes.Venda;
import modelo.Pessoas.Cliente;
import modelo.Pessoas.Funcionario;
import modelo.Pessoas.Pessoa;

/** Painel único para Clientes (funcionarios = false) e Funcionários (funcionarios = true). */
final class PainelPessoas extends JPanel implements Atualizavel {

    private final Contexto ctx;
    private final boolean funcionarios;
    private final String rotulo;   // "cliente" ou "funcionário"
    private final DefaultTableModel modelo;
    private final JTable tabela;
    private final JTextField busca = new JTextField(24);
    private List<Pessoa> atuais = new ArrayList<>();

    PainelPessoas(Contexto ctx, boolean funcionarios) {
        super(new BorderLayout(8, 8));
        this.ctx = ctx;
        this.funcionarios = funcionarios;
        this.rotulo = funcionarios ? "funcionário" : "cliente";
        setBorder(new EmptyBorder(10, 10, 10, 10));

        modelo = funcionarios
                ? Ui.modelo("CPF", "Nome", "Contato", "Endereço", "Cargo", "Salário base", "Comissão", "Vendas no mês")
                : Ui.modelo("CPF", "Nome", "Contato", "Endereço", "Saldo devedor");
        tabela = Ui.tabela(modelo);

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
            List<Pessoa> lidas = new ArrayList<>();
            modelo.setRowCount(0);
            if (funcionarios) {
                for (Funcionario f : ctx.funcionarios.listarTodos()) {
                    lidas.add(f);
                    modelo.addRow(new Object[] {
                            f.getCpf(), f.getNome(), f.getContato(), f.getEndereco().getEnderecoCompleto(),
                            f.getCargo(), Ui.moeda(f.getSalarioBase()),
                            Ui.num(f.getTaxaComissao() * 100) + "%", Ui.moeda(f.getTotalVendasNoMes())
                    });
                }
            } else {
                for (Cliente c : ctx.clientes.listarTodos()) {
                    lidas.add(c);
                    modelo.addRow(new Object[] {
                            c.getCpf(), c.getNome(), c.getContato(), c.getEndereco().getEnderecoCompleto(),
                            Ui.moeda(c.getSaldoDevedor())
                    });
                }
            }
            atuais = lidas;
        } catch (IOException e) {
            Ui.erro(this, e);
        }
    }

    private Pessoa selecionada() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            Ui.aviso(this, "Selecione um " + rotulo + " na tabela.");
            return null;
        }
        return atuais.get(tabela.convertRowIndexToModel(linha));
    }

    private void novo() {
        Pessoa nova = FormPessoa.abrir(this, funcionarios, "Novo " + rotulo, null);
        if (nova == null) {
            return;
        }
        try {
            if (funcionarios) {
                ctx.funcionarios.salvar((Funcionario) nova);
            } else {
                ctx.clientes.salvar((Cliente) nova);
            }
            atualizar();
        } catch (CodigoDuplicadoException | IOException e) {
            Ui.erro(this, e);
        }
    }

    private void editar() {
        Pessoa atual = selecionada();
        if (atual == null) {
            return;
        }
        Pessoa editada = FormPessoa.abrir(this, funcionarios, "Editar " + rotulo, atual);
        if (editada == null) {
            return;
        }
        try {
            if (funcionarios) {
                ctx.funcionarios.atualizar((Funcionario) editada);
            } else {
                ctx.clientes.atualizar((Cliente) editada);
            }
            atualizar();
        } catch (ItemNaoEncontradoException | IOException e) {
            Ui.erro(this, e);
        }
    }

    private void remover() {
        Pessoa p = selecionada();
        if (p == null || !Ui.confirmar(this, "Remover " + rotulo + " \"" + p.getNome() + "\"?")) {
            return;
        }
        try {
            // Vendas, compras e fretes gravam só o CPF; remover quem aparece nelas faria o arquivo
            // de operações deixar de carregar. Por isso a remoção é bloqueada nesse caso.
            if (temOperacoes(p)) {
                Ui.aviso(this, "Não é possível remover: há vendas, compras ou fretes registrados para "
                        + p.getNome() + ".");
                return;
            }
            if (funcionarios) {
                ctx.funcionarios.remover(p.getCpf());
            } else {
                ctx.clientes.remover(p.getCpf());
            }
            atualizar();
        } catch (ItemNaoEncontradoException | IOException e) {
            Ui.erro(this, e);
        }
    }

    private boolean temOperacoes(Pessoa p) throws IOException {
        for (Venda v : ctx.vendas.listarTodos()) {
            String cpf = funcionarios ? v.getFuncionario().getCpf() : v.getCliente().getCpf();
            if (cpf.equals(p.getCpf())) {
                return true;
            }
        }
        for (Frete fr : ctx.fretes.listarTodos()) {
            String cpf = funcionarios ? fr.getFuncionario().getCpf() : fr.getCliente().getCpf();
            if (cpf.equals(p.getCpf())) {
                return true;
            }
        }
        if (funcionarios) {
            for (Compra c : ctx.compras.listarTodos()) {
                if (c.getFuncionario().getCpf().equals(p.getCpf())) {
                    return true;
                }
            }
        }
        return false;
    }
}
