package modelo.Pessoas;

import java.util.ArrayList;
import modelo.Endereco;

public class Cliente extends Pessoa {

    private final ArrayList<Integer> idsCompras;
    private double saldoDevedor;
    
    public Cliente(String nome, String cpf, String telefone, Endereco endereco) {
        super(nome, cpf, telefone, endereco);
        this.idsCompras = new ArrayList<>();
    }

    public double getSaldoDevedor() {
        return saldoDevedor;
    }

    public void setSaldoDevedor(double saldoDevedor) {
        this.saldoDevedor = saldoDevedor;
    }

    public void adicionarCompra(int idVenda) {
        idsCompras.add(idVenda);
    }

    public ArrayList<Integer> getIdsCompras() {
        return idsCompras;
    }

    @Override 
    public String getPerfil() {
        return super.getPerfil() +
               "\nSaldo Devedor: R$" + saldoDevedor +
               "\nCompras Realizadas (IDs): " + idsCompras.toString() + "\n";
    }

}