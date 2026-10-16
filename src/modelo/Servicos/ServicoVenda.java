package modelo.Servicos;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import modelo.Excecoes.CodigoDuplicadoException;
import modelo.Excecoes.EstoqueInsuficienteException;
import modelo.Excecoes.ItemNaoEncontradoException;
import modelo.Operacoes.ItemOps;
import modelo.Operacoes.Venda;
import modelo.Pessoas.Funcionario;
import modelo.Produtos.Produto;
import modelo.Repositorio.ArquivoVenda;
import modelo.Repositorio.Repositorio;

/** Regras de negócio que envolvem vários repositórios ao mesmo tempo. */
public class ServicoVenda {

    private final Repositorio<Produto, Integer> produtos;
    private final Repositorio<Funcionario, String> funcionarios;
    private final ArquivoVenda vendas;

    public ServicoVenda(Repositorio<Produto, Integer> produtos,
                        Repositorio<Funcionario, String> funcionarios,
                        ArquivoVenda vendas) {
        this.produtos = produtos;
        this.funcionarios = funcionarios;
        this.vendas = vendas;
    }

    public void finalizarVenda(Venda venda) throws IOException, ItemNaoEncontradoException,
            EstoqueInsuficienteException, CodigoDuplicadoException {

        if (venda.getCarrinho().isEmpty()) {
            throw new IllegalStateException("A venda não tem itens");
        }
        String metodo = venda.getMetodoPagamento();
        if (metodo == null || metodo.isBlank()) {
            throw new IllegalStateException("Método de pagamento não informado");
        }

        // 1. Valida o estoque de TODOS os itens, com os dados atuais do arquivo,
        //    antes de alterar qualquer coisa.
        Map<Integer, Produto> envolvidos = new HashMap<>();
        
        for (ItemOps item : venda.getCarrinho()) {
            Produto p = produtos.buscarPorChave(item.getIdProduto());
            if (p.getQuantidadeEstoque() < item.getQtd()) {
                throw new EstoqueInsuficienteException("Estoque insuficiente de " + p.getNome()
                        + ": disponível " + p.getQuantidadeEstoque() + ", pedido " + item.getQtd());
            }
            envolvidos.put(p.getId(), p);
        }

        // 2. Baixa o estoque.
        for (ItemOps item : venda.getCarrinho()) {
            Produto p = envolvidos.get(item.getIdProduto());
            p.setQuantidadeEstoque(p.getQuantidadeEstoque() - item.getQtd());
            produtos.atualizar(p);
        }

        // 3. Registra a venda com id sequencial.
        venda.setId(vendas.proximoId());
        vendas.salvar(venda);

        // 4. Soma ao total do mês do funcionário (relido do arquivo para não usar dado velho).
        Funcionario funcionario = funcionarios.buscarPorChave(venda.getFuncionario().getCpf());
        funcionario.setTotalVendasNoMes(funcionario.getTotalVendasNoMes() + venda.getValor());
        funcionarios.atualizar(funcionario);
    }
}