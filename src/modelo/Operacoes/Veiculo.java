package modelo.Operacoes;

import java.io.Serializable;

/** Veículo usado no frete: descrição e capacidades máximas de peso e volume. */
public class Veiculo implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final double TOLERANCIA = 1e-9;

    private final String descricao;
    private final double capacidadeKg;
    private final double capacidadeM3;

    public Veiculo(String descricao, double capacidadeKg, double capacidadeM3) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descrição do veículo não informada");
        }
        if (capacidadeKg <= 0 || capacidadeM3 <= 0) {
            throw new IllegalArgumentException("A capacidade do veículo (kg e m³) deve ser positiva");
        }
        this.descricao = descricao;
        this.capacidadeKg = capacidadeKg;
        this.capacidadeM3 = capacidadeM3;
    }

    // Getters
    public String getDescricao() { return descricao; }
    public double getCapacidadeKg() { return capacidadeKg; }
    public double getCapacidadeM3() { return capacidadeM3; }

    /** A carga cabe se não passa nem do peso nem do volume máximos. */
    public boolean comporta(double pesoKg, double volumeM3) {
        return pesoKg <= capacidadeKg + TOLERANCIA && volumeM3 <= capacidadeM3 + TOLERANCIA;
    }

    @Override
    public String toString() {
        return descricao + " (até " + capacidadeKg + " kg e " + capacidadeM3 + " m³)";
    }
}
