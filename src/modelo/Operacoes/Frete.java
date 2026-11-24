package modelo.Operacoes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.Endereco;
import modelo.Excecoes.CapacidadeTransporteExcedidaException;
import modelo.Excecoes.ValorInvalidoException;
import modelo.Pessoas.Cliente;
import modelo.Pessoas.Funcionario;
import modelo.Produtos.EspecificacaoFisica;
import modelo.Produtos.Produto;

// Subclasse
public class Frete extends Operacao {

    /** Fator de cubagem do transporte rodoviário (kg por m³): peso cubado = volume * fator. */
    public static final double FATOR_CUBAGEM = 300.0;

    private final Cliente cliente;
    private final Endereco destino;
    private final Veiculo veiculo;
    private final double distanciaKm;
    private final double valorPorKm;
    private final double valorPorKgTaxavel;
    private final List<ItemFrete> itens;

    /** Frete novo (id definido ao salvar). */
    public Frete(Funcionario funcionario, Cliente cliente, Endereco destino, Veiculo veiculo,
                 double distanciaKm, double valorPorKm, double valorPorKgTaxavel) {
        super(funcionario);
        this.cliente = cliente;
        this.destino = destino;
        this.veiculo = veiculo;
        this.distanciaKm = distanciaKm;
        this.valorPorKm = valorPorKm;
        this.valorPorKgTaxavel = valorPorKgTaxavel;
        this.itens = new ArrayList<>();
        validar();
    }

    /** Frete carregado do arquivo. */
    public Frete(int id, Funcionario funcionario, LocalDate data, Cliente cliente, Endereco destino,
                 Veiculo veiculo, double distanciaKm, double valorPorKm, double valorPorKgTaxavel,
                 List<ItemFrete> itens) {
        super(id, funcionario, data);
        this.cliente = cliente;
        this.destino = destino;
        this.veiculo = veiculo;
        this.distanciaKm = distanciaKm;
        this.valorPorKm = valorPorKm;
        this.valorPorKgTaxavel = valorPorKgTaxavel;
        this.itens = new ArrayList<>(itens);
        validar();
    }

    private void validar() {
        if (cliente == null || destino == null || veiculo == null) {
            throw new IllegalArgumentException("Cliente, destino e veículo são obrigatórios");
        }
        if (distanciaKm < 0 || valorPorKm < 0 || valorPorKgTaxavel < 0) {
            throw new IllegalArgumentException("Distância e tarifas não podem ser negativas");
        }
    }

    // Getters e Setters
    public Cliente getCliente() { return cliente; }
    public Endereco getDestino() { return destino; }
    public Veiculo getVeiculo() { return veiculo; }
    public double getDistanciaKm() { return distanciaKm; }
    public double getValorPorKm() { return valorPorKm; }
    public double getValorPorKgTaxavel() { return valorPorKgTaxavel; }

    // Retorna uma visão protegida contra alterações externas
    public List<ItemFrete> getItens() { return Collections.unmodifiableList(itens); }

    // ------ Cálculos da carga e do valor -----------

    public double getPesoRealKg() {
        double total = 0;
        for (ItemFrete item : itens) {
            total += item.getPesoTotalKg();
        }
        return total;
    }

    public double getVolumeM3() {
        double total = 0;
        for (ItemFrete item : itens) {
            total += item.getVolumeTotalM3();
        }
        return total;
    }

    public double getPesoCubadoKg() {
        return getVolumeM3() * FATOR_CUBAGEM;
    }

    /** Peso usado na cobrança: o maior entre o peso real e o cubado. */
    public double getPesoTaxavelKg() {
        return Math.max(getPesoRealKg(), getPesoCubadoKg());
    }

    // Overrides

    /** Valor = peso taxável * tarifa por kg  +  distância * tarifa por km. */
    @Override
    public double getValor() {
        return getPesoTaxavelKg() * valorPorKgTaxavel + distanciaKm * valorPorKm;
    }

    @Override
    public String toString() {
        String texto = new String();

        for (ItemFrete item : itens) {
            texto = texto + item.toString() + "\n";
        }

        return "\nFrete \n" + super.toString() +
                "Cliente : " + cliente.getNome() + " | " + cliente.getCpf() + "\n" +
                "Destino : " + destino.getEnderecoCompleto() + "\n" +
                "Veículo : " + veiculo + "\n" +
                "Distância : " + distanciaKm + " km\n" +
                "Peso real : " + String.format("%.3f", getPesoRealKg()) + " kg | Volume : "
                        + String.format("%.3f", getVolumeM3()) + " m³\n" +
                "Peso cubado : " + String.format("%.3f", getPesoCubadoKg()) + " kg | Peso taxável : "
                        + String.format("%.3f", getPesoTaxavelKg()) + " kg\n" +
                "Itens Transportados: \n" + texto;
    }

    // ------ Métodos específicos para a operação FRETE -----------

    /** Adiciona o produto à carga, recusando se o veículo não comportar o peso ou o volume. */
    public void adicionarItem(Produto p, int qtd)
            throws ValorInvalidoException, CapacidadeTransporteExcedidaException {

        if (qtd <= 0) {
            throw new ValorInvalidoException("Quantidade deve ser positiva");
        }

        EspecificacaoFisica espec = p.getEspecificacaoFisica();
        double volumeUnitario = espec.calcularVolumeMetrosCubicos();

        double novoPeso = getPesoRealKg() + qtd * espec.getPesoKg();
        double novoVolume = getVolumeM3() + qtd * volumeUnitario;

        if (!veiculo.comporta(novoPeso, novoVolume)) {
            throw new CapacidadeTransporteExcedidaException("Capacidade do veículo excedida ao adicionar "
                    + qtd + " x " + p.getNome() + ": a carga ficaria com " + novoPeso + " kg e "
                    + novoVolume + " m³ (limite: " + veiculo.getCapacidadeKg() + " kg e "
                    + veiculo.getCapacidadeM3() + " m³)");
        }

        ItemFrete existente = buscarItem(p.getId());
        if (existente != null) {
            existente.setQtd(existente.getQtd() + qtd);   // mesmo produto: soma no item existente
        } else {
            itens.add(new ItemFrete(p.getId(), qtd, espec.getPesoKg(), volumeUnitario));
        }
    }

    public void removerItem(int idProduto) {
        itens.removeIf(item -> item.getIdProduto() == idProduto);
    }

    private ItemFrete buscarItem(int idProduto) {
        for (ItemFrete item : itens) {
            if (item.getIdProduto() == idProduto) {
                return item;
            }
        }
        return null;
    }
}
