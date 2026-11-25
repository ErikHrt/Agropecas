package modelo.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import modelo.Backup.ThreadBackup;
import modelo.Monitores.MonitorEstoque;
import modelo.Produtos.Produto;

final class JanelaPrincipal extends JFrame {

    private static final long INTERVALO_BACKUP_MS = 5L * 60 * 1000;   // backup automático a cada 5 min
    private static final long INTERVALO_MONITOR_MS = 5_000;           // monitor relê os produtos a cada 5 s
    private static final int LIMITE_ESTOQUE_BAIXO = 5;                // estoque <= 5 é considerado baixo
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Contexto ctx = new Contexto();
    private final JTabbedPane abas = new JTabbedPane();
    private final Atualizavel painelProdutos;

    private final JLabel indicadorEstoque = new JLabel("Estoque: verificando...");
    private final JLabel indicadorBackup = new JLabel("Backup: aguardando");

    // Thread 1: backup automático (classe que ESTENDE Thread)
    private final ThreadBackup threadBackup;
    // Thread 2: monitor de estoque (classe que IMPLEMENTA Runnable, executada dentro de um Thread)
    private final MonitorEstoque monitorEstoque;
    private final Thread threadMonitor;

    JanelaPrincipal() {

        super("Agropecas — Maquinário, Peças e Implementos");

        PainelProdutos produtos = new PainelProdutos(ctx);
        painelProdutos = produtos;

        abas.addTab("Produtos", produtos);
        abas.addTab("Clientes", new PainelPessoas(ctx, false));
        abas.addTab("Funcionários", new PainelPessoas(ctx, true));
        abas.addTab("Nova venda", new PainelNovaVenda(ctx));
        abas.addTab("Nova compra", new PainelNovaCompra(ctx));
        abas.addTab("Novo frete", new PainelNovoFrete(ctx));
        abas.addTab("Histórico", new PainelHistorico(ctx));

        // Ao entrar numa aba, recarrega os dados (outra aba pode ter alterado os arquivos)
        abas.addChangeListener(e -> {
            Component aba = abas.getSelectedComponent();
            if (aba instanceof Atualizavel) {
                ((Atualizavel) aba).atualizar();
            }
        });

        // ---- Threads ----
        // As duas rodam fora da thread da interface (EDT); por isso todo acesso aos componentes
        // Swing a partir dos ouvintes passa por SwingUtilities.invokeLater.
        threadBackup = new ThreadBackup(ctx.servicoBackup, INTERVALO_BACKUP_MS, 
                new ThreadBackup.Observador() 
                {
                    @Override
                    public void aoConcluir(Path arquivo) {
                        SwingUtilities.invokeLater(() -> indicadorBackup.setText("Último backup: "
                                + LocalTime.now().format(HORA) + " (" + arquivo.getFileName() + ")"));
                    }

                    @Override
                    public void aoFalhar(Exception erro) {
                        SwingUtilities.invokeLater(() -> indicadorBackup.setText("Backup falhou: " + erro.getMessage()));
                    }
                });

        monitorEstoque = new MonitorEstoque(ctx.produtos, INTERVALO_MONITOR_MS, LIMITE_ESTOQUE_BAIXO,
                new MonitorEstoque.Observador() {
                    @Override
                    public void aoVerificar(List<Produto> estoqueBaixo, int total, boolean houveMudanca) {
                        SwingUtilities.invokeLater(() -> mostrarEstoque(estoqueBaixo, total, houveMudanca));
                    }

                    @Override
                    public void aoFalhar(IOException erro) {
                        SwingUtilities.invokeLater(() -> indicadorEstoque.setText("Estoque: erro ao ler os produtos"));
                    }
                });
        threadMonitor = new Thread(monitorEstoque, "Monitor-Estoque");
        threadMonitor.setDaemon(true);

        setJMenuBar(criarMenu());
        add(abas, BorderLayout.CENTER);
        add(criarBarraDeStatus(), BorderLayout.SOUTH);

        // Ao fechar a janela, pede para as threads terminarem e espera (join) por elas
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                encerrarThreads();
            }
        });

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);

        threadBackup.start();
        threadMonitor.start();
    }

    // ---------- Interface ----------

    private JMenuBar criarMenu() {
        JMenu menu = new JMenu("Backup");

        JMenuItem agora = new JMenuItem("Fazer backup agora");
        agora.addActionListener(e -> {
            indicadorBackup.setText("Backup: em andamento...");
            threadBackup.fazerBackupAgora();   // quem faz é a thread de backup, a interface não trava
        });

        JCheckBoxMenuItem automatico = new JCheckBoxMenuItem("Backup automático (a cada "
                + (INTERVALO_BACKUP_MS / 60000) + " min)", threadBackup.isAutomatico());
        automatico.addActionListener(e -> threadBackup.setAutomatico(automatico.isSelected()));

        JMenuItem restaurar = new JMenuItem("Restaurar backup...");
        restaurar.addActionListener(e -> restaurarBackup());

        menu.add(agora);
        menu.add(automatico);
        menu.addSeparator();
        menu.add(restaurar);

        JMenuBar barra = new JMenuBar();
        barra.add(menu);
        return barra;
    }

    private JPanel criarBarraDeStatus() {
        JSpinner limite = new JSpinner(new SpinnerNumberModel(LIMITE_ESTOQUE_BAIXO, 0, 100_000, 1));
        limite.addChangeListener(e -> monitorEstoque.setLimite((Integer) limite.getValue()));

        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        esquerda.add(indicadorEstoque);
        esquerda.add(new JLabel("  Estoque baixo até:"));
        esquerda.add(limite);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBorder(new EmptyBorder(2, 8, 2, 8));
        barra.add(esquerda, BorderLayout.WEST);
        barra.add(indicadorBackup, BorderLayout.EAST);
        return barra;
    }

    /** Roda na EDT (chamado via invokeLater pelo ouvinte do monitor). */
    private void mostrarEstoque(List<Produto> baixo, int total, boolean houveMudanca) {
        if (baixo.isEmpty()) {
            indicadorEstoque.setForeground(new Color(0, 110, 0));
            indicadorEstoque.setText("Estoque OK (" + total + " produtos monitorados)");
            indicadorEstoque.setToolTipText(null);
        } else {
            StringBuilder dica = new StringBuilder("<html>");
            for (Produto p : baixo) {
                dica.append(p.getId()).append(" - ").append(p.getNome())
                        .append(": ").append(p.getQuantidadeEstoque()).append(" em estoque<br>");
            }
            indicadorEstoque.setForeground(new Color(190, 0, 0));
            indicadorEstoque.setText("ATENÇÃO: " + baixo.size() + " produto(s) com estoque baixo");
            indicadorEstoque.setToolTipText(dica.toString());
        }

        // Os arquivos mudaram desde a última leitura: recarrega a tabela via interface Atualizavel
        if (houveMudanca) {
            painelProdutos.atualizar();
        }
    }

    private void restaurarBackup() {
        JFileChooser escolha = new JFileChooser(ctx.servicoBackup.getPasta().toFile());
        escolha.setDialogTitle("Escolha o backup (.bak) a restaurar");
        escolha.setFileFilter(new FileNameExtensionFilter("Backups do Agropecas (*.bak)", "bak"));

        if (escolha.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        if (!Ui.confirmar(this, "Restaurar este backup?\nOs dados atuais serão substituídos "
                + "(uma cópia de segurança deles é feita antes).")) {
            return;
        }

        try {
            ctx.servicoBackup.restaurar(escolha.getSelectedFile().toPath());

            for (int i = 0; i < abas.getTabCount(); i++) {
                Component aba = abas.getComponentAt(i);
                if (aba instanceof Atualizavel) {
                    ((Atualizavel) aba).atualizar();
                }
            }
            Ui.info(this, "Backup restaurado.");
        } catch (IOException | ClassNotFoundException e) {
            Ui.erro(this, e);
        }
    }

    // ---------- Encerramento ----------

    private void encerrarThreads() {
        monitorEstoque.parar();
        threadMonitor.interrupt();
        threadBackup.parar();

        try {
            threadMonitor.join(2000);   // espera até 2 s cada uma terminar
            threadBackup.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
