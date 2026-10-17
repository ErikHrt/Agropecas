package modelo.Repositorio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import modelo.Excecoes.CodigoDuplicadoException;
import modelo.Excecoes.ItemNaoEncontradoException;


public abstract class ArquivoTexto<T, K> implements Repositorio<T, K> {

    protected static final String SEPARADOR = ";";

    private final Path caminho;

    protected ArquivoTexto(String caminho) {
        this.caminho = Paths.get(caminho);
    }

    // ---- Partes que cada subclasse deve fornecer ----

    protected abstract K getChave(T item);

    protected abstract String formatar(T item);                        // objeto -> linha

    protected abstract T converter(String linha, int numeroLinha) throws IOException; // linha -> objeto

    protected abstract String nomeEntidade();                          // ex.: "Produto", "Cliente"

    // ---- Gancho opcional: a subclasse pode devolver uma exceção mais específica ----

    protected ItemNaoEncontradoException naoEncontrado(K chave) {
        return new ItemNaoEncontradoException(nomeEntidade() + " não encontrado: " + chave);
    }

    // ---- CRUD ----

    @Override
    public void salvar(T item) throws IOException, CodigoDuplicadoException {
        
        K chave = getChave(item);
        if (existe(chave)) {
            throw new CodigoDuplicadoException(nomeEntidade() + " já cadastrado: " + chave);
        }

        criarPastaSeNecessario();
        
        try (BufferedWriter escritor = Files.newBufferedWriter(caminho,
                StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            escritor.write(formatar(item));
            escritor.newLine();
        }

    }

    @Override
    public T buscarPorChave(K chave) throws IOException, ItemNaoEncontradoException {
        
        for (T item : listarTodos()) {
            
            if (getChave(item).equals(chave)) {
                return item;
            }
        }
        throw naoEncontrado(chave);
    }

    @Override
    public List<T> listarTodos() throws IOException {
        
        List<T> itens = new ArrayList<>();
        if (!Files.exists(caminho)) {
            return itens; // nada cadastrado ainda
        }

        try (BufferedReader leitor = Files.newBufferedReader(caminho,StandardCharsets.UTF_8)) {
            String linha;
            int numeroLinha = 0;
            while ((linha = leitor.readLine()) != null) {
                numeroLinha++;
                if (linha.isBlank()) {
                    continue;
                }
                itens.add(converter(linha, numeroLinha));
            }
        }
        return itens;
    }

    @Override
    public void atualizar(T item) throws IOException, ItemNaoEncontradoException {
        
        List<T> itens = listarTodos();
        
        K chave = getChave(item);
        
        for (int i = 0; i < itens.size(); i++) {

            if (getChave(itens.get(i)).equals(chave)) {
                
                itens.set(i, item);
                regravar(itens);
                return;
            }
        }
        
        throw naoEncontrado(chave);
    }

    @Override
    public void remover(K chave) throws IOException, ItemNaoEncontradoException {
        
        List<T> itens = listarTodos();
        
        boolean removido = itens.removeIf(item -> getChave(item).equals(chave));
        if (!removido) {
            throw naoEncontrado(chave);
        }
        regravar(itens);
    }

    // ---- Auxiliares ----

    protected boolean existe(K chave) throws IOException {
        for (T item : listarTodos()) {
            if (getChave(item).equals(chave)) {
                return true;
            }
        }
        return false;
    }

    // Monta a exceção padrão para linha inválida; use dentro de converter(). 
    protected IOException linhaInvalida(int numeroLinha, String detalhe, Throwable causa) {
        
        return new IOException(nomeEntidade() + ": linha " + numeroLinha + " inválida ("
                + detalhe + ") em " + caminho, causa);
    }

    private void criarPastaSeNecessario() throws IOException {
       
        Path pasta = caminho.toAbsolutePath().getParent();
        
        if (pasta != null) {
            Files.createDirectories(pasta);
        }
    }

    // Grava num arquivo temporário e só então substitui o original, para não corrompê-lo
    private void regravar(List<T> itens) throws IOException {

        criarPastaSeNecessario();
        
        Path temporario = Paths.get(caminho + ".tmp");
        
        try (BufferedWriter escritor = Files.newBufferedWriter(temporario,StandardCharsets.UTF_8)) {
            
            for (T item : itens) {
                escritor.write(formatar(item));
                escritor.newLine();
            }
        }
        
        Files.move(temporario, caminho, StandardCopyOption.REPLACE_EXISTING);
    }
}