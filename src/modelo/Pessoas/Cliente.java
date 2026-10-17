package modelo.Pessoas;

import modelo.Endereco;

// Subclasse
public class Cliente extends Pessoa {

    private double saldoDevedor;
    
    public Cliente(String nome, String cpf, String contato, Endereco endereco,
                   double saldoDevedor) {
        super(nome, cpf, contato, endereco);
        this.saldoDevedor = saldoDevedor;
    }

    // Getters e Setters
    public double getSaldoDevedor() { return saldoDevedor; }

    public void setSaldoDevedor(double saldoDevedor) { this.saldoDevedor = saldoDevedor; }

    // Overrides
    @Override 
    public String getPerfil() {
        return super.getPerfil() +
               "\nSaldo Devedor: R$" + saldoDevedor;
    }

}