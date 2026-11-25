package modelo.GUI;

import java.awt.Component;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import modelo.Endereco;
import modelo.Excecoes.ValorInvalidoException;
import modelo.Pessoas.Cliente;
import modelo.Pessoas.Funcionario;
import modelo.Pessoas.Pessoa;

/** Formulário de Cliente ou Funcionário (o CPF é a chave e não pode ser editado depois de salvo). */
final class FormPessoa extends JPanel 
{

    private final boolean funcionario;

    private final JTextField cpf = new JTextField(20);
    private final JTextField nome = new JTextField(20);
    private final JTextField contato = new JTextField(20);
    private final JTextField rua = new JTextField(20);
    private final JTextField numero = new JTextField(20);
    private final JTextField cidade = new JTextField(20);
    private final JTextField estado = new JTextField(20);

    // Cliente
    private final JTextField saldoDevedor = new JTextField(20);
    // Funcionário
    private final JTextField cargo = new JTextField(20);
    private final JTextField salarioBase = new JTextField(20);
    private final JTextField comissao = new JTextField(20);
    private final JTextField totalMes = new JTextField(20);

    private FormPessoa(boolean funcionario, Pessoa p) {
        super(new java.awt.GridBagLayout());
        this.funcionario = funcionario;

        int y = 0;
        Ui.linha(this, y++, "CPF:", cpf);
        Ui.linha(this, y++, "Nome:", nome);
        Ui.linha(this, y++, "Contato:", contato);
        Ui.linha(this, y++, "Rua:", rua);
        Ui.linha(this, y++, "Número:", numero);
        Ui.linha(this, y++, "Cidade:", cidade);
        Ui.linha(this, y++, "Estado:", estado);
        if (funcionario) {
            Ui.linha(this, y++, "Cargo:", cargo);
            Ui.linha(this, y++, "Salário base (R$):", salarioBase);
            Ui.linha(this, y++, "Comissão (%):", comissao);
            Ui.linha(this, y++, "Vendas no mês (R$):", totalMes);
        } else {
            Ui.linha(this, y++, "Saldo devedor (R$):", saldoDevedor);
        }

        saldoDevedor.setText("0");
        totalMes.setText("0");

        if (p != null) {
            cpf.setText(p.getCpf());
            cpf.setEditable(false);
            nome.setText(p.getNome());
            contato.setText(p.getContato());
            Endereco e = p.getEndereco();
            rua.setText(e.getRua());
            numero.setText(e.getNumero());
            cidade.setText(e.getCidade());
            estado.setText(e.getEstado());

            if (p instanceof Funcionario) {
                Funcionario f = (Funcionario) p;
                cargo.setText(f.getCargo());
                salarioBase.setText(String.valueOf(f.getSalarioBase()));
                comissao.setText(Ui.pct(f.getTaxaComissao()));
                totalMes.setText(String.valueOf(f.getTotalVendasNoMes()));
            } else if (p instanceof Cliente) {
                saldoDevedor.setText(String.valueOf(((Cliente) p).getSaldoDevedor()));
            }
        }
    }

    private Pessoa ler() throws ValorInvalidoException 
    {
        String c = Ui.texto(cpf.getText(), "CPF");
        String n = Ui.texto(nome.getText(), "Nome");
        String ct = Ui.texto(contato.getText(), "Contato");
        Endereco end = new Endereco(
                Ui.texto(rua.getText(), "Rua"),
                Ui.texto(numero.getText(), "Número"),
                Ui.texto(cidade.getText(), "Cidade"),
                Ui.texto(estado.getText(), "Estado"));

        if (funcionario) 
            {
                String cg = Ui.texto(cargo.getText(), "Cargo");
                double sal = Ui.numero(salarioBase.getText(), "Salário base");
                double com = Ui.numero(comissao.getText(), "Comissão") / 100.0;
                double tot = Ui.numero(totalMes.getText(), "Vendas no mês");
                if (sal < 0 || com < 0 || tot < 0) {
                    throw new ValorInvalidoException("Salário, comissão e vendas não podem ser negativos.");
            }
                Funcionario f = new Funcionario(n, c, ct, end, cg, sal, com);
                f.setTotalVendasNoMes(tot);
                return f;
        }

        double saldo = Ui.numero(saldoDevedor.getText(), "Saldo devedor");
        if (saldo < 0) {
            throw new ValorInvalidoException("O saldo devedor não pode ser negativo.");
        }
        return new Cliente(n, c, ct, end, saldo);
    }

    /** Abre o formulário; devolve um Cliente/Funcionário preenchido ou null se cancelado. */
    static Pessoa abrir(Component pai, boolean funcionario, String titulo, Pessoa existente) 
    {
        FormPessoa form = new FormPessoa(funcionario, existente);
        while (true) {
            int r = JOptionPane.showConfirmDialog(pai, form, titulo,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) {
                return null;
            }
            try {
                return form.ler();
            } catch (ValorInvalidoException e) {
                Ui.erro(pai, e);
            }
        }
    }
}
