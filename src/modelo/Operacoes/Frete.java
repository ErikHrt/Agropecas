public class Frete implements Operacao {

    private int idFrete;
    private String transportadora;
    private double valorFrete;
    private java.time.LocalDate dataEntrega;
    private Venda vendaAssociada;
    private Endereco enderecoEntrega;

    public Frete(int idFrete, String transportadora, double valorFrete, java.time.LocalDate dataEntrega, 
    Venda vendaAssociada, Endereco enderecoEntrega) {
        this.idFrete = idFrete;
        this.transportadora = transportadora;
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