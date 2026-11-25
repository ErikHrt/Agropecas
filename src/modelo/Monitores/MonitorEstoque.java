package modelo.Monitores;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import modelo.Produtos.Produto;
import modelo.Repositorio.ArquivoProduto;

/**
 * THREAD COM Runnable: de tempos em tempos relê os produtos cadastrados e avisa quem estiver
 * ouvindo quais estão com estoque baixo e se algo mudou desde a última leitura.
 *
 * Como só implementa Runnable, quem usa decide como executar:
 *     Thread t = new Thread(monitor, "Monitor-Estoque");  t.setDaemon(true);  t.start();
 */
public class MonitorEstoque implements Runnable {

    /** Chamado DENTRO da thread do monitor; quem usa Swing precisa de invokeLater. */
    public interface Observador 
    {
        void aoVerificar(List<Produto> estoqueBaixo, int totalDeProdutos, boolean houveMudanca);

        default void aoFalhar(IOException erro) { }
    }

    private final ArquivoProduto produtos;
    private final long intervaloMs;
    private final Observador observador;

    private volatile int limite;                    // estoque <= limite é considerado baixo
    private volatile boolean executando = true;

    private String ultimaAssinatura;                // só usada pela thread do monitor

    public MonitorEstoque(ArquivoProduto produtos, long intervaloMs, int limite, Observador observador) 
    {
        this.produtos = produtos;
        this.intervaloMs = intervaloMs;
        this.limite = limite;
        this.observador = observador;
    }

    public void setLimite(int limite) { this.limite = limite; }

    public int getLimite() { return limite; }

    public void parar() { executando = false; }

    @Override
    public void run() 
    {
        while (executando && !Thread.currentThread().isInterrupted()) 
        {

            verificar();

            try {
                Thread.sleep(intervaloMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();   // devolve o sinal; o while termina
            }
        }
    }

    private void verificar() 
    {
        try {
            List<Produto> todos = produtos.listarTodos();
            List<Produto> baixo = new ArrayList<>();
            StringBuilder assinatura = new StringBuilder();
            int limiteAtual = limite;

            for (Produto p : todos) 
            {
                if (p.getQuantidadeEstoque() <= limiteAtual) 
                {
                    baixo.add(p);
                }
                // "Assinatura" dos dados: se mudar entre duas leituras, houve alteração nos arquivos
                assinatura.append(p.getId()).append(':').append(p.getNome()).append(':')
                        .append(p.getQuantidadeEstoque()).append(':')
                        .append(p.getPreco().getValordeVenda()).append(';');
            }

            String atual = assinatura.toString();
            boolean mudou = ultimaAssinatura != null && !atual.equals(ultimaAssinatura);
            ultimaAssinatura = atual;

            observador.aoVerificar(baixo, todos.size(), mudou);

        } catch (IOException e) {
            observador.aoFalhar(e);
        }
    }
}
