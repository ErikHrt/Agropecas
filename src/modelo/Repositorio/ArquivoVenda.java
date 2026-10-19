package modelo.Repositorio;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import modelo.Excecoes.ItemNaoEncontradoException;
import modelo.Excecoes.PessoaNaoEncontradaException;
import modelo.Operacoes.ItemOps;
import modelo.Operacoes.Venda;
import modelo.Pessoas.Cliente;
import modelo.Pessoas.Funcionario;

/**
 * Formato da linha (os itens vão na própria linha, separados por "|"):
 * id;cpfFuncionario;cpfCliente;data;metodoPagamento;idProduto:qtd:valorUn|idProduto:qtd:valorUn
 * O total não é gravado: é calculado a partir dos itens.
 */
public class ArquivoVenda extends ArquivoTexto<Venda, Integer> {

    private static final int NUMERO_CAMPOS = 6;
    private static final String SEPARADOR_ITENS = "|";
    private static final String SEPARADOR_CAMPOS_ITEM = ":";

    private final ArquivoCliente clientes;
    private final ArquivoFuncionario funcionarios;

    public ArquivoVenda(String caminho, ArquivoCliente clientes,
                        ArquivoFuncionario funcionarios) {
        super(caminho);
        this.clientes = clientes;
        this.funcionarios = funcionarios;
    }

    // Overrides 
    @Override
    protected Integer getChave(Venda venda) {
        return venda.getId();
    }

    @Override
    protected String nomeEntidade() {
        return "Venda";
    }

    @Override
    protected ItemNaoEncontradoException naoEncontrado(Integer id) {
        return new ItemNaoEncontradoException("Venda não encontrada com o ID: " + id);
    }

    @Override
    protected String formatar(Venda venda) {
        
        List<String> itens = new ArrayList<>();
        for (ItemOps item : venda.getCarrinho()) {
            itens.add(item.getIdProduto() + SEPARADOR_CAMPOS_ITEM
                    + item.getQtd() + SEPARADOR_CAMPOS_ITEM
                    + item.getValorUn());
        }

        return String.join(SEPARADOR,
                String.valueOf(venda.getId()),
                venda.getFuncionario().getCpf(),
                venda.getCliente().getCpf(),
                venda.getData().toString(),
                venda.getMetodoPagamento(),
                String.join(SEPARADOR_ITENS, itens));
    }

    @Override
    protected Venda converter(String linha, int numeroLinha) throws IOException {
        
        String[] d = linha.split(SEPARADOR, -1);
        
        if (d.length != NUMERO_CAMPOS) {
            throw linhaInvalida(numeroLinha, "esperados " + NUMERO_CAMPOS + " campos", null);
        }

        try {
            
            int id = Integer.parseInt(d[0]);
            Funcionario funcionario = funcionarios.buscarPorChave(d[1]);
            Cliente cliente = clientes.buscarPorChave(d[2]);
            LocalDate data = LocalDate.parse(d[3]);
            String metodoPagamento = d[4];

            List<ItemOps> itens = new ArrayList<>();
            
            if (!d[5].isEmpty()) {
               
                for (String texto : d[5].split("\\" + SEPARADOR_ITENS)) {
                    
                    String[] campos = texto.split(SEPARADOR_CAMPOS_ITEM, -1);
                    
                    if (campos.length != 3) {
                        throw linhaInvalida(numeroLinha, "item malformado '" + texto + "'", null);
                    }
                    
                    itens.add(new ItemOps(Integer.parseInt(campos[0]),
                            Integer.parseInt(campos[1]),
                            Double.parseDouble(campos[2])));
                }
            }

            return new Venda(id, cliente, funcionario, data, metodoPagamento, itens);

        } catch (NumberFormatException e) {
            throw linhaInvalida(numeroLinha, "número inválido", e);
        } catch (DateTimeParseException e) {
            throw linhaInvalida(numeroLinha, "data inválida", e);
        } catch (ItemNaoEncontradoException e) {
            throw linhaInvalida(numeroLinha, "cliente ou funcionário não encontrado", e);
        }
    }

    // Próximo id sequencial (maior id existente + 1)
    public int proximoId() throws IOException {
        int maior = 0;
        for (Venda venda : listarTodos()) {
            if (venda.getId() > maior) {
                maior = venda.getId();
            }
        }
        return maior + 1;
    }

    // Consultas específicas de Vendas

    public List<Venda> buscarVendaPorCliente(Cliente cliente) 
                throws PessoaNaoEncontradaException , IOException {
        
        if (cliente == null) {
            throw new PessoaNaoEncontradaException("Cliente inválido ou nulo para a busca de vendas.");
        }

        List<Venda> vendasDoCliente = new ArrayList<>();

        for (Venda venda : listarTodos()) {

            if (venda.getCliente() != null && venda.getCliente().getCpf().equals(cliente.getCpf())) {
                vendasDoCliente.add(venda);
            }
        }

        if( vendasDoCliente.isEmpty() ){
            throw new PessoaNaoEncontradaException("Cliente não encontrado");
        }

        return vendasDoCliente;
    }

    public List<Venda> buscarVendaPorFuncionario(Funcionario funcionario) 
                        throws PessoaNaoEncontradaException , IOException  {
        
        if (funcionario == null) {
            throw new PessoaNaoEncontradaException("Funcionário inválido ou nulo para a busca de vendas.");
        }

        List<Venda> vendasDoFuncionario = new ArrayList<>();

        for (Venda venda : listarTodos()) {
            
            if (venda.getFuncionario() != null && venda.getFuncionario().getCpf().equals(funcionario.getCpf())) {
                vendasDoFuncionario.add(venda);
            }
        }

        if( vendasDoFuncionario.isEmpty() ){
            throw new PessoaNaoEncontradaException("Funcionário não encontrado");
        }

        return vendasDoFuncionario;
    }
}