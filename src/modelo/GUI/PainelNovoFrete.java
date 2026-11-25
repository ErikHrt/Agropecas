package modelo.GUI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import modelo.Endereco;
import modelo.Excecoes.CapacidadeTransporteExcedidaException;
import modelo.Excecoes.CodigoDuplicadoException;
import modelo.Excecoes.ItemNaoEncontradoException;
import modelo.Excecoes.ValorInvalidoException;
import modelo.Operacoes.Frete;
import modelo.Operacoes.ItemFrete;
import modelo.Operacoes.Veiculo;
import modelo.Pessoas.Cliente;
import modelo.Pessoas.Funcionario;
import modelo.Produtos.Produto;
import modelo.Repositorio.ArquivoTexto;

final class PainelNovoFrete extends JPanel implements Atualizavel {

    private final Contexto ctx;

    private final JComboBox<Cliente> cliente = new JComboBox<>();
    private final JComboBox<Funcionario> funcionario = new JComboBox<>();
    private final JTextField rua = new JTextField(14);
    private final JTextField numero = new JTextField(5);
    private final JTextField cidade = new JTextField(11);
    private final JTextField estado = new JTextField(3);
    private final JTextField veiculo = new JTextField(12);
    private final JTextField capacidadeKg = new JTextField(7);
    private final JTextField capacidadeM3 = new JTextField(6);
    private final JTextField distancia = new JTextField(6);
    private final JTextField valorKm = new JTextField(6);
    private final JTextField valorKg = new JTextField(6);

    private final JComboBox<Produto> produto = new JComboBox<>();
    private final JSpinner quantidade = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));

    private final DefaultTableModel modelo = Ui.modelo("ID", "Produto", "Quantidade", "Peso total (kg)", "Volume total (m³)");
    private final JTable tabela = Ui.tabela(modelo);
    private final JLabel carga = new JLabel(" ");
    private final JLabel total = new JLabel();

    // Campos do cabeçalho: ficam travados enquanto há frete em andamento
    private final List<JComponent> cabecalho = List.<JComponent>of(cliente, funcionario, rua, numero, cidade,
            estado, veiculo, capacidadeKg, capacidadeM3, distancia, valorKm, valorKg);

    private final Map<Integer, Produto> porId = new HashMap<>();
    private Frete frete;              // null enquanto não há frete em andamento
    private boolean carregando;       // evita preencher o destino ao recarregar o combo de clientes

    PainelNovoFrete(Contexto ctx) {
        super(new BorderLayout(8, 8));
        this.ctx = ctx;
        setBorder(new EmptyBorder(10, 10, 10, 10));

        Ui.rotular(cliente, c -> c.getNome() + " — " + c.getCpf());
        Ui.rotular(funcionario, f -> f.getNome() + " — " + f.getCpf());
        Ui.rotular(produto, p -> p.getId() + " - " + p.getNome() + "  ("
                + Ui.num(p.getEspecificacaoFisica().getPesoKg()) + " kg, "
                + Ui.num(p.getEspecificacaoFisica().calcularVolumeMetrosCubicos()) + " m³)");
        total.setFont(total.getFont().deriveFont(Font.BOLD, 16f));

        // O destino começa como o endereço do cliente, mas pode ser trocado
        cliente.addActionListener(e -> {
            Cliente c = (Cliente) cliente.getSelectedItem();
            if (!carregando && frete == null && c != null) {
                preencherDestino(c);
            }
        });

        JPanel linhaPessoas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        linhaPessoas.add(new JLabel("Cliente:"));
        linhaPessoas.add(cliente);
        linhaPessoas.add(new JLabel("Funcionário:"));
        linhaPessoas.add(funcionario);

        JPanel linhaDestino = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        linhaDestino.add(new JLabel("Destino — Rua:"));
        linhaDestino.add(rua);
        linhaDestino.add(new JLabel("Nº:"));
        linhaDestino.add(numero);
        linhaDestino.add(new JLabel("Cidade:"));
        linhaDestino.add(cidade);
        linhaDestino.add(new JLabel("UF:"));
        linhaDestino.add(estado);

        JPanel linhaVeiculo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        linhaVeiculo.add(new JLabel("Veículo:"));
        linhaVeiculo.add(veiculo);
        linhaVeiculo.add(new JLabel("Capacidade (kg):"));
        linhaVeiculo.add(capacidadeKg);
        linhaVeiculo.add(new JLabel("(m³):"));
        linhaVeiculo.add(capacidadeM3);
        linhaVeiculo.add(new JLabel("Distância (km):"));
        linhaVeiculo.add(distancia);
        linhaVeiculo.add(new JLabel("R$/km:"));
        linhaVeiculo.add(valorKm);
        linhaVeiculo.add(new JLabel("R$/kg taxável:"));
        linhaVeiculo.add(valorKg);

        JPanel linhaProduto = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        linhaProduto.add(new JLabel("Produto:"));
        linhaProduto.add(produto);
        linhaProduto.add(new JLabel("Qtd:"));
        linhaProduto.add(quantidade);
        linhaProduto.add(Ui.botao("Adicionar item", e -> adicionar()));

        JPanel norte = new JPanel(new java.awt.GridLayout(0, 1));
        norte.add(linhaPessoas);
        norte.add(linhaDestino);
        norte.add(linhaVeiculo);
        norte.add(linhaProduto);

        JPanel baixo = new JPanel(new BorderLayout());
        baixo.add(Ui.botoes(
                Ui.botao("Remover item", e -> removerItem()),
                Ui.botao("Cancelar frete", e -> cancelar()),
                Ui.botao("Finalizar frete", e -> finalizar())), BorderLayout.WEST);
        baixo.add(total, BorderLayout.EAST);

        JPanel sul = new JPanel(new BorderLayout());
        sul.add(carga, BorderLayout.NORTH);
        sul.add(baixo, BorderLayout.CENTER);

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(sul, BorderLayout.SOUTH);

        atualizar();
    }

    @Override
    public void atualizar() {
        try {
            carregando = true;
            // Cliente e funcionário ficam travados enquanto há frete em andamento
            if (frete == null) {
                cliente.setModel(new DefaultComboBoxModel<>(new Vector<>(ctx.clientes.listarTodos())));
                funcionario.setModel(new DefaultComboBoxModel<>(new Vector<>(ctx.funcionarios.listarTodos())));
            }
            List<Produto> produtos = ctx.produtos.listarTodos();
            porId.clear();
            for (Produto p : produtos) {
                porId.put(p.getId(), p);
            }
            produto.setModel(new DefaultComboBoxModel<>(new Vector<>(produtos)));
            carregando = false;

            Cliente c = (Cliente) cliente.getSelectedItem();
            if (frete == null && c != null) {
                preencherDestino(c);
            }
            redesenhar();
        } catch (IOException e) {
            carregando = false;
            Ui.erro(this, e);
        }
    }

    private void preencherDestino(Cliente c) {
        Endereco e = c.getEndereco();
        rua.setText(e.getRua());
        numero.setText(e.getNumero());
        cidade.setText(e.getCidade());
        estado.setText(e.getEstado());
    }

    private void redesenhar() {
        modelo.setRowCount(0);
        if (frete != null) {
            for (ItemFrete i : frete.getItens()) {
                Produto p = porId.get(i.getIdProduto());
                modelo.addRow(new Object[] {
                        i.getIdProduto(),
                        p == null ? "?" : p.getNome(),
                        i.getQtd(),
                        Ui.num(i.getPesoTotalKg()),
                        Ui.num(i.getVolumeTotalM3())
                });
            }
            Veiculo v = frete.getVeiculo();
            carga.setText("Carga: " + Ui.num(frete.getPesoRealKg()) + " de " + Ui.num(v.getCapacidadeKg()) + " kg   |   "
                    + Ui.num(frete.getVolumeM3()) + " de " + Ui.num(v.getCapacidadeM3()) + " m³   |   "
                    + "Peso cubado (" + Ui.num(Frete.FATOR_CUBAGEM) + " kg/m³): " + Ui.num(frete.getPesoCubadoKg())
                    + " kg   |   Peso taxável: " + Ui.num(frete.getPesoTaxavelKg()) + " kg");
        } else {
            carga.setText(" ");
        }
        total.setText("Valor do frete: " + Ui.moeda(frete == null ? 0 : frete.getValor()));
        for (JComponent campo : cabecalho) {
            campo.setEnabled(frete == null);
        }
    }

    /** Monta o frete com o que está digitado no cabeçalho. */
    private Frete criarFrete(Cliente c, Funcionario f) throws ValorInvalidoException {
        Endereco destino = new Endereco(
                Ui.texto(rua.getText(), "Rua do destino"),
                Ui.texto(numero.getText(), "Número do destino"),
                Ui.texto(cidade.getText(), "Cidade do destino"),
                Ui.texto(estado.getText(), "Estado do destino"));

        Veiculo v = new Veiculo(
                Ui.texto(veiculo.getText(), "Veículo"),
                Ui.numero(capacidadeKg.getText(), "Capacidade (kg)"),
                Ui.numero(capacidadeM3.getText(), "Capacidade (m³)"));

        double km = Ui.numero(distancia.getText(), "Distância (km)");
        double porKm = Ui.numero(valorKm.getText(), "Tarifa por km");
        double porKg = Ui.numero(valorKg.getText(), "Tarifa por kg taxável");

        return new Frete(f, c, destino, v, km, porKm, porKg);
    }

    private void adicionar() {
        Cliente c = (Cliente) cliente.getSelectedItem();
        Funcionario f = (Funcionario) funcionario.getSelectedItem();
        Produto p = (Produto) produto.getSelectedItem();
        if (c == null || f == null || p == null) {
            Ui.aviso(this, "Cadastre e selecione um cliente, um funcionário e um produto.");
            return;
        }

        boolean nova = (frete == null);
        try {
            if (nova) {
                frete = criarFrete(c, f);
            }
            frete.adicionarItem(p, (Integer) quantidade.getValue());
            quantidade.setValue(1);
        } catch (ValorInvalidoException | CapacidadeTransporteExcedidaException | IllegalArgumentException e) {
            if (nova) {
                frete = null;
            }
            Ui.erro(this, e);
        }
        redesenhar();
    }

    private void removerItem() {
        int linha = tabela.getSelectedRow();
        if (frete == null || linha < 0) {
            Ui.aviso(this, "Selecione um item da carga.");
            return;
        }
        int id = (Integer) modelo.getValueAt(tabela.convertRowIndexToModel(linha), 0);
        frete.removerItem(id);
        if (frete.getItens().isEmpty()) {
            frete = null;   // carga vazia: libera o cabeçalho para edição
        }
        redesenhar();
    }

    private void cancelar() {
        if (frete != null && Ui.confirmar(this, "Descartar o frete em andamento?")) {
            frete = null;
            redesenhar();
        }
    }

    private void finalizar() {
        if (frete == null || frete.getItens().isEmpty()) {
            Ui.aviso(this, "A carga está vazia.");
            return;
        }
        try {
            synchronized (ArquivoTexto.TRAVA_GLOBAL) {   // operação em vários arquivos: foto do backup nunca fica pela metade
                ctx.servicoFrete.finalizarFrete(frete);
            }
            Ui.info(this, "Frete #" + frete.getId() + " registrado.\nValor: " + Ui.moeda(frete.getValor()));
            frete = null;
            atualizar();
        } catch (IOException | ItemNaoEncontradoException | CodigoDuplicadoException e) {
            Ui.erro(this, e);
        } catch (RuntimeException e) {
            Ui.erro(this, e);   // IllegalStateException (frete sem itens)
        }
    }
}
