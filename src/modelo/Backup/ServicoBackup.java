package modelo.Backup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import modelo.Excecoes.CodigoDuplicadoException;
import modelo.Operacoes.Compra;
import modelo.Operacoes.Frete;
import modelo.Operacoes.Venda;
import modelo.Pessoas.Cliente;
import modelo.Pessoas.Funcionario;
import modelo.Produtos.Produto;
import modelo.Repositorio.ArquivoCliente;
import modelo.Repositorio.ArquivoCompra;
import modelo.Repositorio.ArquivoFrete;
import modelo.Repositorio.ArquivoFuncionario;
import modelo.Repositorio.ArquivoProduto;
import modelo.Repositorio.ArquivoTexto;
import modelo.Repositorio.ArquivoVenda;
import modelo.Repositorio.BackupDados;

// Backup e restauração que envolvem todos os arquivos ao mesmo tempo.
public class ServicoBackup 
{

    public static final String PREFIXO_AUTOMATICO = "backup";
    public static final String PREFIXO_SEGURANCA = "pre_restauracao";

    private static final DateTimeFormatter CARIMBO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private final ArquivoProduto produtos;
    private final ArquivoCliente clientes;
    private final ArquivoFuncionario funcionarios;
    private final ArquivoVenda vendas;
    private final ArquivoCompra compras;
    private final ArquivoFrete fretes;
    private final Path pasta;

    public ServicoBackup(ArquivoProduto produtos, ArquivoCliente clientes, ArquivoFuncionario funcionarios,
                         ArquivoVenda vendas, ArquivoCompra compras, ArquivoFrete fretes, Path pasta) {
        this.produtos = produtos;
        this.clientes = clientes;
        this.funcionarios = funcionarios;
        this.vendas = vendas;
        this.compras = compras;
        this.fretes = fretes;
        this.pasta = pasta;
    }

    public Path getPasta() { return pasta; }

    /** Lê todos os arquivos, serializa o conjunto e devolve o caminho do .bak criado. */
    public Path criarBackup(String prefixo) throws IOException 
    {

        BackupDados dados;

        // A trava global impede que uma venda/compra/frete esteja "pela metade"
        synchronized (ArquivoTexto.TRAVA_GLOBAL) 
        {
            dados = new BackupDados(produtos.listarTodos(), clientes.listarTodos(),
                    funcionarios.listarTodos(), vendas.listarTodos(),
                    compras.listarTodos(), fretes.listarTodos());
        }

        Path destino = pasta.resolve(prefixo + "_" + dados.getData().format(CARIMBO) + ".bak");
        dados.gravar(destino);
        return destino;
    }

    /**
     * Restaura um backup
     */
    public void restaurar(Path arquivo) throws IOException, ClassNotFoundException 
    {
        BackupDados dados = BackupDados.ler(arquivo);

        synchronized (ArquivoTexto.TRAVA_GLOBAL) {

            criarBackup(PREFIXO_SEGURANCA);

            produtos.limpar();
            clientes.limpar();
            funcionarios.limpar();
            vendas.limpar();
            compras.limpar();
            fretes.limpar();

            try {
                // Pessoas e produtos primeiro: as operações os referenciam pelo CPF/id
                for (Produto p : dados.getProdutos()) produtos.salvar(p);
                for (Cliente c : dados.getClientes()) clientes.salvar(c);
                for (Funcionario f : dados.getFuncionarios()) funcionarios.salvar(f);
                for (Compra c : dados.getCompras()) compras.salvar(c);
                for (Venda v : dados.getVendas()) vendas.salvar(v);
                for (Frete f : dados.getFretes()) fretes.salvar(f);
            } catch (CodigoDuplicadoException e) {
                throw new IOException("Backup inconsistente: " + e.getMessage(), e);
            }
        }
    }
}
