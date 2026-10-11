package modelo.Operacoes;

import modelo.Endereco;
import modelo.Pessoas.*;

public class Frete implements Operacao {

    private final int idFrete;
    private double valorFrete;
    private Funcionario funcionario;
    private java.time.LocalDate dataEntrega;
    private Venda vendaAssociada;
    private Endereco enderecoEntrega;
    private enum StatusFrete {
        PENDENTE,
        EM_TRANSITO,
        ENTREGUE
    }

    public Frete(int idFrete, double valorFrete, java.time.LocalDate dataEntrega, 
    Venda vendaAssociada, Endereco enderecoEntrega) {
        this.idFrete = idFrete;
        this.valorFrete = valorFrete;
        this.dataEntrega = dataEntrega;
        this.vendaAssociada = vendaAssociada;
        this.enderecoEntrega = enderecoEntrega;
    }

    @Override
    public void cadastrar() {
        // Implementar lógica de cadastro do frete
    }
    @Override
    public void executar() {
        // Implementar lógica de execução do frete
    }
    @Override
    public void atualizar() {
        // Implementar lógica de atualização do frete
    }
    @Override
    public void excluir() {
        // Implementar lógica de exclusão do frete
    }
    @Override
    public void consultar() {
        // Implementar lógica de consulta do frete
    }


}