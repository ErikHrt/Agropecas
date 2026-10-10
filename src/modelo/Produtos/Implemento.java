public class Implemento extends Produto {
    
    public Implemento(int id, String nome, String descricao, Preco preco, EspecificacaoFisica especificacaoFisica) {
        super(id, nome, descricao, preco, especificacaoFisica);
    }

    @Override
    public double calcularFrete() {
        // Implementar lógica de cálculo de frete para implementos
        return 0.0; // Retorna 0.0 como placeholder
    }
}