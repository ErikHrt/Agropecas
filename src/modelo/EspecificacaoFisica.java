public class EspecificacaoFisica {
    private final double pesoKg;
    private final double alturaMetros;
    private final double larguraMetros;
    private final double profundidadeMetros;

    public EspecificacaoFisica(double pesoKg, double alturaMetros, double larguraMetros, double profundidadeMetros) {
        this.pesoKg = pesoKg;
        this.alturaMetros = alturaMetros;
        this.larguraMetros = larguraMetros;
        this.profundidadeMetros = profundidadeMetros;
    }

    public double getPesoKg() { return pesoKg; }
    
    public double calcularVolumeMetrosCubicos() {
        return alturaMetros * larguraMetros * profundidadeMetros;
    }

    public double calcularPesoCubado(double fatorCubagem) {
        return calcularVolumeMetrosCubicos() * fatorCubagem;
    }
    
}