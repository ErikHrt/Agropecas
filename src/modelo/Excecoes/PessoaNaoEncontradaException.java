package modelo.Excecoes;

public class PessoaNaoEncontradaException extends Exception {
    public PessoaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
