public class Peca extends Produto {
    
    public Peca(int id, String nome, String descricao, Preco preco, EspecificacaoFisica especificacaoFisica) {
        super(id, nome, descricao, preco, especificacaoFisica);
    }

    @Override
    public double calcularFrete() {
        // Implementar lógica de cálculo de frete para peças
        return 0.0; // Retorna 0.0 como placeholder
    }
}