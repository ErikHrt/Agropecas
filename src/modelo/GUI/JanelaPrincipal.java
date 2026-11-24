package modelo.GUI;

import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

final class JanelaPrincipal extends JFrame {

    JanelaPrincipal() {
        super("Agropecas — Maquinário, Peças e Implementos");

        Contexto ctx = new Contexto();
        JTabbedPane abas = new JTabbedPane();

        abas.addTab("Produtos", new PainelProdutos(ctx));
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

        add(abas);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 680);
        setLocationRelativeTo(null);
    }
}
