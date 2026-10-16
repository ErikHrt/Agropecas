package modelo.Repositorio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import modelo.Endereco;
import modelo.Excecoes.ItemNaoEncontradoException;
import modelo.Excecoes.PessoaNaoEncontradaException;
import modelo.Pessoas.Cliente;


public class ArquivoCliente extends ArquivoTexto<Cliente, String> {

    private static final int NUMERO_CAMPOS = 8;

    public ArquivoCliente(String caminho) {
        super(caminho);
    }

    @Override
    protected String getChave(Cliente cliente) {
        return cliente.getCpf();
    }

    @Override
    protected String nomeEntidade() {
        return "Cliente";
    }

    @Override
    protected String formatar(Cliente cliente) {
        Endereco e = cliente.getEndereco();
        return String.join(SEPARADOR,
                cliente.getCpf(),
                cliente.getNome(),
                cliente.getContato(),
                e.getRua(),
                e.getNumero(),
                e.getCidade(),
                e.getEstado(),
                String.valueOf(cliente.getSaldoDevedor()));
    }

    @Override
    protected Cliente converter(String linha, int numeroLinha) throws IOException {
        String[] d = linha.split(SEPARADOR, -1);
        if (d.length != NUMERO_CAMPOS) {
            throw linhaInvalida(numeroLinha, "esperados " + NUMERO_CAMPOS + " campos", null);
        }

        try {
            Endereco endereco = new Endereco(d[3], d[4], d[5], d[6]);
            double saldoDevedor = Double.parseDouble(d[7]);
            return new Cliente(d[1], d[0], d[2], endereco, saldoDevedor);
        } catch (NumberFormatException e) {
            throw linhaInvalida(numeroLinha, "saldo devedor inválido", e);
        }
    }
    
    // ---- Consultas específicas de cliente ----

    public Cliente buscarPorNome(String nome) throws IOException, PessoaNaoEncontradaException {
        for (Cliente cliente : listarTodos()) {
            if (cliente.getNome().equalsIgnoreCase(nome)) {
                return cliente;
            }
        }
        throw new PessoaNaoEncontradaException("Cliente não encontrado com o nome: " + nome);
    }

    public void atualizarSaldoDevedor(String cpf, double novoSaldo)
            throws IOException, ItemNaoEncontradoException {
        Cliente cliente = buscarPorChave(cpf);
        cliente.setSaldoDevedor(novoSaldo);
        atualizar(cliente);
    }

    public List<Cliente> listarPorSaldoDevedor(double limite) throws IOException {
        List<Cliente> resultado = new ArrayList<>();
        for (Cliente cliente : listarTodos()) {
            if (cliente.getSaldoDevedor() > limite) {
                resultado.add(cliente);
            }
        }
        return resultado;
    }
}