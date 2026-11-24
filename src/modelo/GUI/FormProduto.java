package modelo.GUI;

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import modelo.Excecoes.ValorInvalidoException;
import modelo.Produtos.EspecificacaoFisica;
import modelo.Produtos.Implemento;
import modelo.Produtos.Maquinario;
import modelo.Produtos.Peca;
import modelo.Produtos.Preco;
import modelo.Produtos.Produto;

/** Formulário de produto, usado no cadastro/edição e no cadastro de produto novo durante uma compra. */
final class FormProduto extends JPanel {

    private static final String[] TIPOS = {"Peça", "Maquinário", "Implemento"};

    private final JTextField id = new JTextField(12);
    private final JComboBox<String> tipo = new JComboBox<>(TIPOS);
    private final JTextField nome = new JTextField(22);
    private final JTextField descricao = new JTextField(22);
    private final JTextField custo = new JTextField(12);
    private final JTextField margem = new JTextField(12);
    private final JTextField peso = new JTextField(12);
    private final JTextField altura = new JTextField(12);
    private final JTextField largura = new JTextField(12);
    private final JTextField profundidade = new JTextField(12);
    private final JTextField estoque = new JTextField(12);
    private final boolean pedirEstoque;

    private FormProduto(Produto p, int idSugerido, boolean idEditavel, Double custoSugerido, boolean pedirEstoque) {
        super(new java.awt.GridBagLayout());
        this.pedirEstoque = pedirEstoque;

        int y = 0;
        Ui.linha(this, y++, "ID:", id);
        Ui.linha(this, y++, "Tipo:", tipo);
        Ui.linha(this, y++, "Nome:", nome);
        Ui.linha(this, y++, "Descrição:", descricao);
        Ui.linha(this, y++, "Preço de custo (R$):", custo);
        Ui.linha(this, y++, "Margem de lucro (%):", margem);
        Ui.linha(this, y++, "Peso (kg):", peso);
        Ui.linha(this, y++, "Altura (m):", altura);
        Ui.linha(this, y++, "Largura (m):", largura);
        Ui.linha(this, y++, "Profundidade (m):", profundidade);
        if (pedirEstoque) {
            Ui.linha(this, y++, "Estoque:", estoque);
        }

        id.setText(String.valueOf(idSugerido));
        id.setEditable(idEditavel);
        estoque.setText("0");
        if (custoSugerido != null) {
            custo.setText(String.valueOf(custoSugerido));
        }

        if (p != null) {
            tipo.setSelectedIndex(indiceDoTipo(p.getTipo()));
            nome.setText(p.getNome());
            descricao.setText(p.getDescricao());
            custo.setText(String.valueOf(p.getPreco().getValorUnitario()));
            margem.setText(Ui.pct(p.getPreco().getMargemLucro()));
            peso.setText(String.valueOf(p.getEspecificacaoFisica().getPesoKg()));
            altura.setText(String.valueOf(p.getEspecificacaoFisica().getAlturaMetros()));
            largura.setText(String.valueOf(p.getEspecificacaoFisica().getLarguraMetros()));
            profundidade.setText(String.valueOf(p.getEspecificacaoFisica().getProfundidadeMetros()));
            estoque.setText(String.valueOf(p.getQuantidadeEstoque()));
        }
    }

    private static int indiceDoTipo(String tipo) {
        if (tipo.equals("PECA")) return 0;
        if (tipo.equals("MAQUINARIO")) return 1;
        return 2;
    }

    private Produto ler() throws ValorInvalidoException {
        int codigo = Ui.inteiro(id.getText(), "ID");
        if (codigo <= 0) {
            throw new ValorInvalidoException("O ID deve ser um inteiro positivo.");
        }
        String n = Ui.texto(nome.getText(), "Nome");
        String d = Ui.textoOpcional(descricao.getText(), "Descrição");

        double c = Ui.numero(custo.getText(), "Preço de custo");
        double m = Ui.numero(margem.getText(), "Margem de lucro") / 100.0;
        double pe = Ui.numero(peso.getText(), "Peso");
        double a = Ui.numero(altura.getText(), "Altura");
        double l = Ui.numero(largura.getText(), "Largura");
        double pr = Ui.numero(profundidade.getText(), "Profundidade");
        int est = pedirEstoque ? Ui.inteiro(estoque.getText(), "Estoque") : 0;

        if (c < 0 || m < 0 || pe < 0 || a < 0 || l < 0 || pr < 0 || est < 0) {
            throw new ValorInvalidoException("Preço, margem, medidas e estoque não podem ser negativos.");
        }

        Preco preco = new Preco(c, m);
        EspecificacaoFisica espec = new EspecificacaoFisica(pe, a, l, pr);

        Produto produto;
        switch (tipo.getSelectedIndex()) {
            case 0:  produto = new Peca(codigo, n, d, preco, espec); break;
            case 1:  produto = new Maquinario(codigo, n, d, preco, espec); break;
            default: produto = new Implemento(codigo, n, d, preco, espec); break;
        }
        produto.setQuantidadeEstoque(est);
        return produto;
    }

    /**
     * Abre o formulário e devolve o produto preenchido, ou null se o usuário cancelar.
     *
     * @param existente    produto a editar (null para um novo)
     * @param idEditavel   se o usuário pode alterar o ID
     * @param custoSugerido preço de custo já preenchido (null se não houver)
     * @param pedirEstoque se mostra o campo de estoque (numa compra o estoque entra pelo serviço)
     */
    static Produto abrir(Component pai, String titulo, Produto existente, int idSugerido,
                         boolean idEditavel, Double custoSugerido, boolean pedirEstoque) {
        FormProduto form = new FormProduto(existente, idSugerido, idEditavel, custoSugerido, pedirEstoque);
        while (true) {
            int r = JOptionPane.showConfirmDialog(pai, form, titulo,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) {
                return null;
            }
            try {
                return form.ler();
            } catch (ValorInvalidoException e) {
                Ui.erro(pai, e);   // mostra o erro e reabre o formulário com o que já foi digitado
            }
        }
    }
}
