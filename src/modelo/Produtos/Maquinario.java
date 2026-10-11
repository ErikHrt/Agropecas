package modelo.Produtos;

public class Maquinario extends Produto {
    
    public Maquinario(int id, String nome, String descricao, Preco preco, EspecificacaoFisica especificacaoFisica) {
        super(id, nome, descricao, preco, especificacaoFisica);
    }

    @Override
    public String getTipo() {
        return "MAQUINARIO";
    }
}