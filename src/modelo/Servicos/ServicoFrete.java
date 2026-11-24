package modelo.Servicos;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import modelo.Excecoes.CodigoDuplicadoException;
import modelo.Excecoes.ItemNaoEncontradoException;
import modelo.Operacoes.Frete;
import modelo.Operacoes.ItemFrete;
import modelo.Produtos.Produto;
import modelo.Repositorio.ArquivoCliente;
import modelo.Repositorio.ArquivoFrete;
import modelo.Repositorio.ArquivoFuncionario;
import modelo.Repositorio.ArquivoProduto;

// Execução do FRETE que envolve vários arquivos ao mesmo tempo.
public class ServicoFrete {

    private final ArquivoProduto produtos;
    private final ArquivoCliente clientes;
    private final ArquivoFuncionario funcionarios;
    private final ArquivoFrete fretes;

    public ServicoFrete(ArquivoProduto produtos, ArquivoCliente clientes,
                        ArquivoFuncionario funcionarios, ArquivoFrete fretes) {
        this.produtos = produtos;
        this.clientes = clientes;
        this.funcionarios = funcionarios;
        this.fretes = fretes;
    }

    /**
     * Fluxo completo: confere com os arquivos que cliente, funcionário e todos os produtos
     * do frete ainda existem e registra o frete com id sequencial.
     * O frete não mexe no estoque (a saída do estoque acontece na venda).
     */
    public void finalizarFrete(Frete frete) throws IOException, ItemNaoEncontradoException,
            CodigoDuplicadoException {

        if (frete.getItens().isEmpty()) {
            throw new IllegalStateException("O frete não tem itens");
        }

        // Lê os dados atuais uma única vez, em vez de uma busca por item.
        clientes.buscarPorChave(frete.getCliente().getCpf());
        funcionarios.buscarPorChave(frete.getFuncionario().getCpf());

        Set<Integer> cadastrados = new HashSet<>();
        for (Produto p : produtos.listarTodos()) {
            cadastrados.add(p.getId());
        }
        for (ItemFrete item : frete.getItens()) {
            if (!cadastrados.contains(item.getIdProduto())) {
                throw new ItemNaoEncontradoException("Produto não encontrado: " + item.getIdProduto());
            }
        }

        frete.setId(fretes.proximoId());
        fretes.salvar(frete);
    }
}
