package modelo.Pessoas;

import modelo.Endereco;

public class Funcionario extends Pessoa {
    
    private final double salarioBase;
    private String Cargo;
    private double taxaComissao;
    private double totalVendasNoMes;

    public Funcionario(String nome, String cpf, String telefone, Endereco endereco, 
                       String cargo, double salarioBase, double taxaComissao) {

        super(nome, cpf, telefone, endereco);
        this.salarioBase = salarioBase;
        this.taxaComissao = taxaComissao;
        this.Cargo = cargo;
    }

    // Getters e setters para totalVendasNoMes
    public double getTotalVendasNoMes() {
        return totalVendasNoMes;
    }

    public double getSalarioBase() {
        return salarioBase;
    }

    public String getCargo() {
        return Cargo;
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

    public void setCargo(String cargo) {
        Cargo = cargo;
    }
    
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