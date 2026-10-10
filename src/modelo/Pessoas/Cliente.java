package modelo.Pessoas;
import java.util.ArrayList;

public class Cliente extends Pessoa {

    private final ArrayList<Double> idsCompras;
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
        idsCompras.add((double) idVenda);
    }

    public ArrayList<Double> getIdsCompras() {
        return idsCompras;
    }

    @Override
    public String getPerfil() {
        return super.getPerfil() +
               "\nSaldo Devedor: R$" + saldoDevedor +
               "\nCompras Realizadas (IDs): " + idsCompras.toString();
    }

}