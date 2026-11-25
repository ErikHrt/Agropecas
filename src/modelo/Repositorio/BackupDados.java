package modelo.Repositorio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.Operacoes.Compra;
import modelo.Operacoes.Frete;
import modelo.Operacoes.Venda;
import modelo.Pessoas.Cliente;
import modelo.Pessoas.Funcionario;
import modelo.Produtos.Produto;


public class BackupDados implements Serializable 
{

    private static final long serialVersionUID = 1L;

    private final LocalDateTime data;
    private final ArrayList<Produto> produtos;
    private final ArrayList<Cliente> clientes;
    private final ArrayList<Funcionario> funcionarios;
    private final ArrayList<Venda> vendas;
    private final ArrayList<Compra> compras;
    private final ArrayList<Frete> fretes;

    public BackupDados(List<Produto> produtos, List<Cliente> clientes, List<Funcionario> funcionarios,
                       List<Venda> vendas, List<Compra> compras, List<Frete> fretes) 
    {
        this.data = LocalDateTime.now();
        this.produtos = new ArrayList<>(produtos);
        this.clientes = new ArrayList<>(clientes);
        this.funcionarios = new ArrayList<>(funcionarios);
        this.vendas = new ArrayList<>(vendas);
        this.compras = new ArrayList<>(compras);
        this.fretes = new ArrayList<>(fretes);
    }

    // Getters
    public LocalDateTime getData() { return data; }
    public List<Produto> getProdutos() { return Collections.unmodifiableList(produtos); }
    public List<Cliente> getClientes() { return Collections.unmodifiableList(clientes); }
    public List<Funcionario> getFuncionarios() { return Collections.unmodifiableList(funcionarios); }
    public List<Venda> getVendas() { return Collections.unmodifiableList(vendas); }
    public List<Compra> getCompras() { return Collections.unmodifiableList(compras); }
    public List<Frete> getFretes() { return Collections.unmodifiableList(fretes); }

    /** Serializa este objeto num arquivo (grava num temporário e só então substitui o destino). */
    public void gravar(Path destino) throws IOException 
    {

        Path pasta = destino.toAbsolutePath().getParent();
        
        if (pasta != null) 
        {
            Files.createDirectories(pasta);
        }

        Path temporario = Paths.get(destino + ".tmp");

        try (ObjectOutputStream saida = new ObjectOutputStream( 
            new BufferedOutputStream(Files.newOutputStream(temporario)))) 
        {
            saida.writeObject(this);
        }

        Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
    }

    /** Desserializa um backup gravado por gravar(). */
    public static BackupDados ler(Path origem) throws IOException, ClassNotFoundException 
    {

        try (ObjectInputStream entrada = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(origem)))) {

            
            Object lido = entrada.readObject();

            if (!(lido instanceof BackupDados)) {
                throw new IOException("O arquivo não é um backup do sistema: " + origem);
            }
            return (BackupDados) lido;
        }
    }
}
