package modelo.GUI;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;
import modelo.Excecoes.ValorInvalidoException;

/** Funções de apoio compartilhadas pelos painéis (formatação, validação, diálogos e componentes). */
final class Ui {

    private Ui() {}

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(PT_BR);
    private static final DecimalFormat NUMERO = new DecimalFormat("#,##0.###", DecimalFormatSymbols.getInstance(PT_BR));

    // ---------- Formatação ----------

    static String moeda(double valor) { return MOEDA.format(valor); }

    static String num(double valor) { return NUMERO.format(valor); }

    /** Fração -> porcentagem sem ruído de ponto flutuante (0.07 -> "7.0"). */
    static String pct(double fracao) { return String.valueOf(Math.round(fracao * 10000) / 100.0); }

    static String nomeTipo(String tipo) {
        switch (tipo) {
            case "PECA": return "Peça";
            case "MAQUINARIO": return "Maquinário";
            case "IMPLEMENTO": return "Implemento";
            default: return tipo;
        }
    }

    // ---------- Validação de entrada ----------
    // Os arquivos usam ';' como separador de campos ("|" e ":" nos itens de compra),
    // então esses caracteres não podem aparecer nos textos digitados.

    static String texto(String txt, String campo) throws ValorInvalidoException {
        String t = textoOpcional(txt, campo);
        if (t.isEmpty()) {
            throw new ValorInvalidoException("Preencha o campo \"" + campo + "\".");
        }
        return t;
    }

    static String textoOpcional(String txt, String campo) throws ValorInvalidoException {
        String t = txt.trim();
        if (t.contains(";") || t.contains("\n") || t.contains("\r")) {
            throw new ValorInvalidoException("O campo \"" + campo + "\" não pode conter ponto e vírgula nem quebra de linha.");
        }
        return t;
    }

    static String textoLocal(String txt) throws ValorInvalidoException {
        String t = texto(txt, "Local da compra");
        if (t.contains("|") || t.contains(":")) {
            throw new ValorInvalidoException("O local da compra não pode conter \"|\" nem \":\".");
        }
        return t;
    }

    static double numero(String txt, String campo) throws ValorInvalidoException {
        try {
            double v = Double.parseDouble(txt.trim().replace(',', '.'));
            if (Double.isNaN(v) || Double.isInfinite(v)) {
                throw new NumberFormatException();
            }
            return v;
        } catch (NumberFormatException e) {
            throw new ValorInvalidoException("Valor inválido em \"" + campo + "\": '" + txt.trim() + "'");
        }
    }

    static int inteiro(String txt, String campo) throws ValorInvalidoException {
        try {
            return Integer.parseInt(txt.trim());
        } catch (NumberFormatException e) {
            throw new ValorInvalidoException("Número inteiro inválido em \"" + campo + "\": '" + txt.trim() + "'");
        }
    }

    // ---------- Diálogos ----------

    static void erro(Component pai, Exception e) {
        String msg = e.getMessage() == null ? e.toString() : e.getMessage();
        if (e instanceof IOException) {
            msg = "Erro de arquivo: " + msg;
        }
        erro(pai, msg);
    }

    static void erro(Component pai, String msg) {
        JOptionPane.showMessageDialog(pai, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    static void aviso(Component pai, String msg) {
        JOptionPane.showMessageDialog(pai, msg, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    static void info(Component pai, String msg) {
        JOptionPane.showMessageDialog(pai, msg, "Agropecas", JOptionPane.INFORMATION_MESSAGE);
    }

    static boolean confirmar(Component pai, String msg) {
        return JOptionPane.showConfirmDialog(pai, msg, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    // ---------- Componentes ----------

    static JButton botao(String texto, ActionListener acao) {
        JButton b = new JButton(texto);
        b.addActionListener(acao);
        return b;
    }

    static JPanel botoes(JButton... botoes) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        for (JButton b : botoes) {
            p.add(b);
        }
        return p;
    }

    /** Adiciona "rótulo + campo" numa linha de um formulário GridBagLayout. */
    static void linha(JPanel painel, int y, String rotulo, JComponent campo) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = y;
        g.insets = new Insets(3, 4, 3, 4);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0;
        painel.add(new JLabel(rotulo), g);

        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        painel.add(campo, g);
    }

    static JPanel formulario() {
        return new JPanel(new GridBagLayout());
    }

    /** Tabela somente leitura; colunas com Integer ordenam numericamente. */
    static DefaultTableModel modelo(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) { return false; }

            @Override
            public Class<?> getColumnClass(int coluna) {
                for (int l = 0; l < getRowCount(); l++) {
                    Object v = getValueAt(l, coluna);
                    if (v != null) {
                        return v.getClass();
                    }
                }
                return Object.class;
            }
        };
    }

    static JTable tabela(DefaultTableModel modelo) {
        JTable t = new JTable(modelo);
        t.setAutoCreateRowSorter(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setRowHeight(24);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    static void aoDigitar(JTextComponent campo, Runnable acao) {
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { acao.run(); }
            @Override public void removeUpdate(DocumentEvent e) { acao.run(); }
            @Override public void changedUpdate(DocumentEvent e) { acao.run(); }
        });
    }

    /** Filtra as linhas da tabela por qualquer coluna, sem diferenciar maiúsculas. */
    static void filtrar(JTextComponent campo, TableRowSorter<DefaultTableModel> sorter) {
        aoDigitar(campo, () -> {
            String t = campo.getText().trim();
            if (t.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(t)));
            }
        });
    }

    /** Define como cada item de um JComboBox aparece na lista. */
    static <T> void rotular(JComboBox<T> combo, Function<T, String> rotulo) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(JList<?> lista, Object valor,
                    int indice, boolean selecionado, boolean foco) {
                String texto = valor == null ? "" : rotulo.apply((T) valor);
                return super.getListCellRendererComponent(lista, texto, indice, selecionado, foco);
            }
        });
    }
}
