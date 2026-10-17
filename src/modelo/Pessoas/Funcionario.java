package modelo.Pessoas;

import modelo.Endereco;

public class Funcionario extends Pessoa {
    
    private final double salarioBase;
    private String cargo;
    private double taxaComissao;
    private double totalVendasNoMes;

    public Funcionario(String nome, String cpf, String contato, Endereco endereco, 
                       String cargo, double salarioBase, double taxaComissao) {

        super(nome, cpf, contato, endereco);
        this.salarioBase = salarioBase;
        this.taxaComissao = taxaComissao;
        this.cargo = cargo;
    }

    // Getters e setters
    public double getTotalVendasNoMes() { return totalVendasNoMes; }
    public double getSalarioBase() { return salarioBase; }
    public String getCargo() { return cargo; }
    public double getTaxaComissao() { return taxaComissao; }

    public void setTaxaComissao(double taxaComissao) { this.taxaComissao = taxaComissao; }
    public void setTotalVendasNoMes(double totalVendasNoMes) { this.totalVendasNoMes = totalVendasNoMes; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    
    // Overrides
    @Override
    public String getPerfil() {
        return super.getPerfil() +
               "\nSalário Base: R$" + salarioBase +
               "\nComissão: " + (taxaComissao * 100) + "%";
    }

    // ------ Métodos Específicos para Pessoa Funcionario --------

    public double calcularSalarioComComissao(double totalVendidoNoMes) {
        return salarioBase + (totalVendidoNoMes * taxaComissao);
    }

}