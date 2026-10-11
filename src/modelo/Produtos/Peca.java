package modelo.Produtos;


public class Peca extends Produto {
    
    public Peca(int id, String nome, String descricao, Preco preco, EspecificacaoFisica especificacaoFisica) {
        super(id, nome, descricao, preco, especificacaoFisica);
    }

    @Override
    public String getTipo() {
        return "PECA";
    }
    
}