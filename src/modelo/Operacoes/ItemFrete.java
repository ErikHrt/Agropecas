package modelo.Operacoes;

/**
 * Item transportado. Guarda o peso e o volume UNITÁRIOS do produto no momento do frete,
 * para o frete continuar com os mesmos números mesmo que o cadastro do produto mude depois.
 */
public class ItemFrete {

    private final int idProduto;
    private int qtd;
    private final double pesoUnitarioKg;
    private final double volumeUnitarioM3;

    public ItemFrete(int idProduto, int qtd, double pesoUnitarioKg, double volumeUnitarioM3) {
        this.idProduto = idProduto;
        this.qtd = qtd;
        this.pesoUnitarioKg = pesoUnitarioKg;
        this.volumeUnitarioM3 = volumeUnitarioM3;
    }

    // Getters e Setters
    public int getIdProduto() { return idProduto; }
    public int getQtd() { return qtd; }
    public double getPesoUnitarioKg() { return pesoUnitarioKg; }
    public double getVolumeUnitarioM3() { return volumeUnitarioM3; }

    public void setQtd(int qtd) { this.qtd = qtd; }

    public double getPesoTotalKg() { return qtd * pesoUnitarioKg; }
    public double getVolumeTotalM3() { return qtd * volumeUnitarioM3; }

    @Override
    public String toString() {
        return "Id Produto : " + idProduto + " | " +
                "Quantidade: " + qtd + " | " +
                "Peso total: " + String.format("%.3f", getPesoTotalKg()) + " kg | " +
                "Volume total: " + String.format("%.3f", getVolumeTotalM3()) + " m³";
    }
}
