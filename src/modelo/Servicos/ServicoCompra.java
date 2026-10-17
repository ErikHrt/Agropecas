package modelo.Servicos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import modelo.Excecoes.CodigoDuplicadoException;
import modelo.Excecoes.ItemNaoEncontradoException;
import modelo.Operacoes.Compra;
import modelo.Operacoes.ItemOps;
import modelo.Produtos.Produto;
import modelo.Repositorio.ArquivoCompra;
import modelo.Repositorio.ArquivoProduto;


// Execução da COMPRA que envolve vários arquivos ao mesmo tempo.
public class ServicoCompra {

    private final ArquivoProduto  produtos;
    private final ArquivoCompra compras;

    public ServicoCompra(ArquivoProduto produtos, ArquivoCompra compras) {
        this.produtos = produtos;
        this.compras = compras;
    }

    /*
        Devolve os itens cujo produto ainda não está cadastrado, com no máximo
        um item por produto (se o mesmo código novo aparecer em várias linhas
        da compra, só a primeira entra).
    */

    public List<ItemOps> filtrarNaoCadastrados(List<ItemOps> itens) throws IOException {
        // Lê o arquivo uma única vez, em vez de uma busca (e uma exceção) por item.
        Set<Integer> cadastrados = new HashSet<>();
        
        for (Produto p : produtos.listarTodos()) {
            cadastrados.add(p.getId());
        }

        Set<Integer> jaIncluidos = new HashSet<>();
        List<ItemOps> naoCadastrados = new ArrayList<>();

        for (ItemOps item : itens) {

            int id = item.getIdProduto();
            
            if (!cadastrados.contains(id) && jaIncluidos.add(id)) {
                naoCadastrados.add(item);
            }
        }
        return naoCadastrados;
    }

    /*
        Cadastra os produtos novos. Como o ItemOps não tem nome, tipo, medidas etc.,
        quem chama fornece o "criador", que monta o Produto a partir do item 
    */

    public void cadastrarNaoCadastrados(List<ItemOps> naoCadastrados, Function<ItemOps, Produto> criador)
            throws IOException, CodigoDuplicadoException {
        
            for (ItemOps item : naoCadastrados) {

                Produto novo = criador.apply(item);
            
                produtos.salvar(novo);
            }
    }

    // Soma a quantidade comprada ao estoque de cada produto (todos já devem estar cadastrados)
    public void darEntradaNoEstoque(List<ItemOps> itens) throws IOException, ItemNaoEncontradoException {
        
        for (ItemOps item : itens) {
            
            Produto p = produtos.buscarPorChave(item.getIdProduto());
            p.setQuantidadeEstoque(p.getQuantidadeEstoque() + item.getQtd());
            produtos.atualizar(p);
        }
    }

    /**
     * Fluxo completo: cadastra o que for novo, dá entrada no estoque de todos
     * os itens e registra a compra com id sequencial.
     */

    public void finalizarCompra(Compra compra, Function<ItemOps, Produto> criador)
            throws IOException, ItemNaoEncontradoException, CodigoDuplicadoException {

        List<ItemOps> itens = compra.getItensComprados();
        
        if (itens.isEmpty()) {
            throw new IllegalStateException("A compra não tem itens");
        }

        cadastrarNaoCadastrados(filtrarNaoCadastrados(itens), criador);
        darEntradaNoEstoque(itens);

        compra.setId(compras.proximoId());
        compras.salvar(compra);
    }
}