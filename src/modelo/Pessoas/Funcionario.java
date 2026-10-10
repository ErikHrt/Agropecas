
public class Funcionario extends Pessoa {
    
    private final double salarioBase;
    private double taxaComissao; // Ex: 0.05 para 5%
    private double totalVendasNoMes; // Para calcular a comissão

    public Funcionario(String nome, String cpf, String telefone, Endereco endereco, 
                       String matricula, double salarioBase, double taxaComissao) {
        // O "super" chama o construtor da classe pai (Pessoa)
        super(nome, cpf, telefone, endereco);
        this.salarioBase = salarioBase;
        this.taxaComissao = taxaComissao;
    }

    // Getters e setters para totalVendasNoMes
    public double getTotalVendasNoMes() {
        return totalVendasNoMes;
    }

    public double getSalarioBase() {
        return salarioBase;
    }
    
    public double getTaxaComissao() {
        return taxaComissao;
    }

    public void setTaxaComissao(double taxaComissao) {
        this.taxaComissao = taxaComissao;
    }

    public void setTotalVendasNoMes(double totalVendasNoMes) {
        this.totalVendasNoMes = totalVendasNoMes;
    }

    // Método específico de funcionário
    public double calcularSalarioComComissao(double totalVendidoNoMes) {
        return salarioBase + (totalVendidoNoMes * taxaComissao);
    }

    @Override
    public String getPerfil() {
        return super.getPerfil() +
               "\nSalário Base: R$" + salarioBase +
               "\nComissão: " + (taxaComissao * 100) + "%";
    }
}