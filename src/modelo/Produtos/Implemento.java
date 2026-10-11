package modelo.Produtos;

public class Implemento extends Produto {
    
    public Implemento(int id, String nome, String descricao, Preco preco, EspecificacaoFisica especificacaoFisica) {
        super(id, nome, descricao, preco, especificacaoFisica);
    }

    @Override
    public String getTipo() {
        return "IMPLEMENTO";
    }
}