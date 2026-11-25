package modelo.GUI;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Ponto de entrada da interface gráfica. Execute a partir da raiz do projeto (pasta "dados/"). */
public class AppGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignorada) {
                
            }
            new JanelaPrincipal().setVisible(true);
        });
    }
}
