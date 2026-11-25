package modelo.Backup;

import java.io.IOException;
import java.nio.file.Path;

/**
 * THREAD ESTENDENDO Thread: faz o backup automático em segundo plano.
 *
 * Fica dormindo com wait(tempo): acorda sozinha ao fim do intervalo, ou antes se alguém chamar
 * fazerBackupAgora() (notifyAll). É uma thread daemon, então não impede o programa de fechar.
 */
public final class ThreadBackup extends Thread {

    /** Quem quiser ser avisado (a interface gráfica, por exemplo). Chamado DENTRO desta thread. */
    public interface Observador 
    {
        void aoConcluir(Path arquivo);
        void aoFalhar(Exception erro);
    }

    private final ServicoBackup servico;
    private final long intervaloMs;
    private final Observador observador;

    private volatile boolean ativa = true;          // false = a thread deve terminar
    private volatile boolean automatico = true;     // false = só faz backup quando pedido
    private boolean pedidoImediato = false;         // protegido por synchronized(this)

    public ThreadBackup(ServicoBackup servico, long intervaloMs, Observador observador) 
    {
        super("Thread-Backup");
        this.servico = servico;
        this.intervaloMs = intervaloMs;
        this.observador = observador;
        setDaemon(true);
    }

    // ---- Chamados por OUTRAS threads (ex.: a da interface) ----

    public void setAutomatico(boolean automatico) { this.automatico = automatico; }

    public boolean isAutomatico() { return automatico; }

    /** Acorda a thread agora, sem esperar o fim do intervalo. */
    public synchronized void fazerBackupAgora() 
    {
        pedidoImediato = true;
        notifyAll();
    }

    /** Pede para a thread terminar e a acorda caso esteja dormindo. */
    public synchronized void parar() 
    {
        ativa = false;
        notifyAll();
    }

    // ---- Código que roda na thread nova ----

    @Override
    public void run() 
    {
        while (ativa) 
        {
            boolean pedido;
            try 
            {
                pedido = esperarProximoCiclo();
            } catch (InterruptedException e) {
                break;   // interrupt(): encerra
            }

            if (!ativa) 
            {
                break;
            }
            if (pedido || automatico) 
            {
                executarBackup();
            }
        }
    }

    // Dorme até o fim do intervalo OU até um pedido/parada. Devolve true se foi um pedido manual.
    private synchronized boolean esperarProximoCiclo() throws InterruptedException 
    {
        long fim = System.currentTimeMillis() + intervaloMs;

        while (ativa && !pedidoImediato) 
        {
            long restante = fim - System.currentTimeMillis();
            if (restante <= 0) 
            {
                break;
            }
            wait(restante);
        }

        boolean foiPedido = pedidoImediato;
        pedidoImediato = false;
        return foiPedido;
    }

    private void executarBackup() 
    {
        try {
            Path arquivo = servico.criarBackup(ServicoBackup.PREFIXO_AUTOMATICO);
            if (observador != null) {
                observador.aoConcluir(arquivo);
            }
        } catch (IOException e) {
            if (observador != null) {
                observador.aoFalhar(e);
            }
        }
    }
}
