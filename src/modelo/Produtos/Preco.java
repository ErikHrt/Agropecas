package modelo.Produtos;

import java.io.Serializable;

public class Preco implements Serializable {

    private static final long serialVersionUID = 1L;

    private final double valorUnitario;
    private double margemLucro;

    public Preco(double valorUnitario, double margemLucro) {
        this.valorUnitario = valorUnitario;
        this.margemLucro = margemLucro;
    }
    
    // Getters e Setters

    public double getValorUnitario() { return valorUnitario; }
    public double getMargemLucro() { return margemLucro; }
    public double getValordeVenda(){ return (valorUnitario * (1 + margemLucro)); }

    public void setMargemLucro(double margemLucro) { this.margemLucro = margemLucro; }
}
