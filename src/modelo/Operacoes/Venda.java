package modelo.Operacoes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.Excecoes.EstoqueInsuficienteException;
import modelo.Pessoas.Cliente;
import modelo.Pessoas.Funcionario;
import modelo.Produtos.Produto;

public class Venda extends Operacao {

    private final Cliente cliente;
    private String metodoPagamento;
    private final List<ItemOps> carrinho;

    /** Venda nova (id definido ao salvar). */
    public Venda(Cliente cliente, Funcionario funcionario) {
        super(funcionario);
        this.cliente = cliente;
        this.carrinho = new ArrayList<>();
    }

    /** Venda carregada do arquivo: não revalida estoque, pois já foi vendida. */
    public Venda(int id, Cliente cliente, Funcionario funcionario, LocalDate data,
                 String metodoPagamento, List<ItemOps> itens) {
        super(id, funcionario, data);
        this.cliente = cliente;
        this.metodoPagamento = metodoPagamento;
        this.carrinho = new ArrayList<>(itens);
    }

    public Cliente getCliente() {
        return cliente;
    }

    public String getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(String metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    // Retorna uma visão protegida contra alterações externas
    public List<ItemOps> getCarrinho() {
        return Collections.unmodifiableList(carrinho);
    }

    public void adicionarAoCarrinho(Produto p, int qtd) throws EstoqueInsuficienteException {
        if (qtd <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva");
        }

        // Verifica se o Produto já se encontra no carrinho
        ItemOps existente = buscarItem(p.getId());
        int jaNoCarrinho = (existente == null) ? 0 : existente.getQtd();
        int disponivel = p.getQuantidadeEstoque() - jaNoCarrinho;

        if (qtd > disponivel) {
            throw new EstoqueInsuficienteException("Quantidade requerida (" + qtd + ") do produto "
                    + p.getNome() + " excede a disponível (" + disponivel + ")");
        }

        if (existente != null) {
            existente.setQtd(jaNoCarrinho + qtd);   // mesmo produto: soma no item existente
        } else {
            carrinho.add(new ItemOps(p.getId(), qtd, p.getPreco().getValordeVenda()));
        }
    }

    public void removerDoCarrinho(int idProduto) {
        carrinho.removeIf(item -> item.getIdProduto() == idProduto);
    }

    public double calcularTotal() {
        double total = 0;
        for (ItemOps item : carrinho) {
            total += item.getQtd() * item.getValorUn();
        }
        return total;
    }

    private ItemOps buscarItem(int idProduto) {
        for (ItemOps item : carrinho) {
            if (item.getIdProduto() == idProduto) {
                return item;
            }
        }
        return null;
    }

    @Override
    public double getValor() {
        return calcularTotal();
    }

    @Override
    public String toString(){

        String itens = new String();

        for (ItemOps item : carrinho) {
            itens = itens + (item.toString()) + "\n";
            
        }

        return "\nVenda \n" + super.toString() +
                "Cliente : " + cliente.getNome() + " | " + cliente.getCpf() + "\n" +
                "Método de Pagamento : " + metodoPagamento + "\n" +
                "Itens Vendidos: \n" + itens; 
        
    }

    
}