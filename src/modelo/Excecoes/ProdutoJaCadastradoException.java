package modelo.Excecoes;

public class ProdutoJaCadastradoException extends Exception {
    public ProdutoJaCadastradoException(String mensagem) {
        super(mensagem);
    }
}