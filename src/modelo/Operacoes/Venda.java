package modelo.Operacoes;

import java.time.LocalDate;
import java.util.ArrayList;

import modelo.Pessoas.Cliente;
import modelo.Pessoas.Funcionario;
import modelo.Produtos.Produto;

public class Venda implements Operacao {

    private int idVenda;
    private Cliente cliente;
    private Funcionario funcionario;
    private double totalVenda;
    private LocalDate dataVenda;
    private String metodoPagamento;

    private ArrayList<Produto> produtosVendidos;

    public Venda(int idVenda, Cliente cliente, Funcionario funcionario, String metodoPagamento) {
        this.idVenda = idVenda;
        this.metodoPagamento = metodoPagamento;
        this.cliente = cliente;
        this.funcionario = funcionario;
        this.dataVenda = LocalDate.now();
        this.produtosVendidos = new ArrayList<>();
    }
    
    public void adicionarProduto(Produto produto) {
        produtosVendidos.add(produto);
    }

    public double calcularTotalVenda() {
        double total = 0.0;
        for (Produto produto : produtosVendidos) {
            total += produto.getPreco();
        }
        this.totalVenda = total;
        return total;
    }


    @Override
    public void cadastrar() {
        // Implementar lógica de cadastro da venda
    }
    @Override
    public void executar() {
        // Implementar lógica de execução da venda
    }
    @Override
    public void atualizar() {
        // Implementar lógica de atualização da venda
    }
    @Override
    public void excluir() {
        // Implementar lógica de exclusão da venda
    }
    @Override
    public void consultar() {
        // Implementar lógica de consulta da venda
    }

    
}