public class Preco {

    private final double valorUnitario;
    private double margemLucro;

    public Preco(double valorUnitario, double margemLucro) {
        this.valorUnitario = valorUnitario;
        this.margemLucro = margemLucro;
    }

    public void setMargemLucro(double margemLucro) { this.margemLucro = margemLucro; }

    public double getValorUnitario() { return valorUnitario; }

    public double getValordeVenda(){ return valorUnitario * (1 + margemLucro); }
}
