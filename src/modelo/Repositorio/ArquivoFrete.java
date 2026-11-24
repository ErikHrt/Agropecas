package modelo.Repositorio;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import modelo.Endereco;
import modelo.Excecoes.ItemNaoEncontradoException;
import modelo.Operacoes.Frete;
import modelo.Operacoes.ItemFrete;
import modelo.Operacoes.Veiculo;
import modelo.Pessoas.Cliente;
import modelo.Pessoas.Funcionario;

/**
 * Formato da linha (os itens vão na própria linha, separados por "|"):
 * id;cpfFuncionario;cpfCliente;data;rua;numero;cidade;estado;veiculo;capacidadeKg;capacidadeM3;
 * distanciaKm;valorPorKm;valorPorKgTaxavel;idProduto:qtd:pesoUnitarioKg:volumeUnitarioM3|...
 * O valor não é gravado: é calculado a partir dos itens e das tarifas.
 */
public class ArquivoFrete extends ArquivoTexto<Frete, Integer> {

    private static final int NUMERO_CAMPOS = 15;
    private static final int NUMERO_CAMPOS_ITEM = 4;
    private static final String SEPARADOR_ITENS = "|";
    private static final String SEPARADOR_CAMPOS_ITEM = ":";

    private final ArquivoCliente clientes;
    private final ArquivoFuncionario funcionarios;

    public ArquivoFrete(String caminho, ArquivoCliente clientes, ArquivoFuncionario funcionarios) {
        super(caminho);
        this.clientes = clientes;
        this.funcionarios = funcionarios;
    }

    // Overrides
    @Override
    protected Integer getChave(Frete frete) {
        return frete.getId();
    }

    @Override
    protected String nomeEntidade() {
        return "Frete";
    }

    @Override
    protected ItemNaoEncontradoException naoEncontrado(Integer id) {
        return new ItemNaoEncontradoException("Frete não encontrado com o ID: " + id);
    }

    @Override
    protected String formatar(Frete frete) {

        List<String> itens = new ArrayList<>();
        for (ItemFrete item : frete.getItens()) {
            itens.add(item.getIdProduto() + SEPARADOR_CAMPOS_ITEM
                    + item.getQtd() + SEPARADOR_CAMPOS_ITEM
                    + item.getPesoUnitarioKg() + SEPARADOR_CAMPOS_ITEM
                    + item.getVolumeUnitarioM3());
        }

        Endereco destino = frete.getDestino();
        Veiculo veiculo = frete.getVeiculo();

        return String.join(SEPARADOR,
                String.valueOf(frete.getId()),
                frete.getFuncionario().getCpf(),
                frete.getCliente().getCpf(),
                frete.getData().toString(),
                destino.getRua(),
                destino.getNumero(),
                destino.getCidade(),
                destino.getEstado(),
                veiculo.getDescricao(),
                String.valueOf(veiculo.getCapacidadeKg()),
                String.valueOf(veiculo.getCapacidadeM3()),
                String.valueOf(frete.getDistanciaKm()),
                String.valueOf(frete.getValorPorKm()),
                String.valueOf(frete.getValorPorKgTaxavel()),
                String.join(SEPARADOR_ITENS, itens));
    }

    @Override
    protected Frete converter(String linha, int numeroLinha) throws IOException {

        String[] d = linha.split(SEPARADOR, -1);

        if (d.length != NUMERO_CAMPOS) {
            throw linhaInvalida(numeroLinha, "esperados " + NUMERO_CAMPOS + " campos", null);
        }

        try {

            int id = Integer.parseInt(d[0]);
            Funcionario funcionario = funcionarios.buscarPorChave(d[1]);
            Cliente cliente = clientes.buscarPorChave(d[2]);
            LocalDate data = LocalDate.parse(d[3]);
            Endereco destino = new Endereco(d[4], d[5], d[6], d[7]);
            Veiculo veiculo = new Veiculo(d[8], Double.parseDouble(d[9]), Double.parseDouble(d[10]));
            double distanciaKm = Double.parseDouble(d[11]);
            double valorPorKm = Double.parseDouble(d[12]);
            double valorPorKg = Double.parseDouble(d[13]);

            List<ItemFrete> itens = new ArrayList<>();

            if (!d[14].isEmpty()) {

                for (String texto : d[14].split("\\" + SEPARADOR_ITENS)) {

                    String[] campos = texto.split(SEPARADOR_CAMPOS_ITEM, -1);

                    if (campos.length != NUMERO_CAMPOS_ITEM) {
                        throw linhaInvalida(numeroLinha, "item malformado '" + texto + "'", null);
                    }

                    itens.add(new ItemFrete(Integer.parseInt(campos[0]),
                            Integer.parseInt(campos[1]),
                            Double.parseDouble(campos[2]),
                            Double.parseDouble(campos[3])));
                }
            }

            return new Frete(id, funcionario, data, cliente, destino, veiculo,
                    distanciaKm, valorPorKm, valorPorKg, itens);

        } catch (NumberFormatException e) {
            throw linhaInvalida(numeroLinha, "número inválido", e);
        } catch (DateTimeParseException e) {
            throw linhaInvalida(numeroLinha, "data inválida", e);
        } catch (IllegalArgumentException e) {
            throw linhaInvalida(numeroLinha, "dados do frete inválidos: " + e.getMessage(), e);
        } catch (ItemNaoEncontradoException e) {
            throw linhaInvalida(numeroLinha, "cliente ou funcionário não encontrado", e);
        }
    }

    // Próximo id sequencial (maior id existente + 1)
    public int proximoId() throws IOException {
        int maior = 0;
        for (Frete frete : listarTodos()) {
            if (frete.getId() > maior) {
                maior = frete.getId();
            }
        }
        return maior + 1;
    }

    // Consultas específicas de Frete

    public List<Frete> listarPorCliente(String cpfCliente) throws IOException {
        List<Frete> resultado = new ArrayList<>();
        for (Frete frete : listarTodos()) {
            if (frete.getCliente().getCpf().equals(cpfCliente)) {
                resultado.add(frete);
            }
        }
        return resultado;
    }
}
