package modelo.ManipulacaoArquivos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import modelo.Excecoes.ProdutoNaoEncontradoException;
import modelo.Produtos.*;

/**
 * Persiste produtos em um único arquivo texto. Cada linha tem o formato:
 * TIPO;id;nome;descricao;valorUnitario;margemLucro;pesoKg;altura;largura;profundidade;estoque
 */
public class ArquivoProduto {

    private static final String SEPARADOR = ";";
    private static final int NUMERO_CAMPOS = 11;

    private final String nomeArquivo;

    public ArquivoProduto(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public void salvar(Produto produto) throws IOException {
    File pasta = new File(nomeArquivo).getParentFile();
    if (pasta != null) {
        pasta.mkdirs();   // cria a pasta (e as pais) se ainda não existirem
    }

    try (BufferedWriter escritor = new BufferedWriter(new FileWriter(nomeArquivo, true))) {
        escritor.write(formatar(produto));
        escritor.newLine();
    }
}

    public Produto buscarPorId(int id) throws IOException, ProdutoNaoEncontradoException {
        for (Produto produto : listarTodos()) {
            if (produto.getId() == id) {
                return produto;
            }
        }
        throw new ProdutoNaoEncontradoException("Produto não encontrado com o ID: " + id);
    }

    public Produto buscarPorNome(String nome) throws IOException, ProdutoNaoEncontradoException {
        for (Produto produto : listarTodos()) {
            if (produto.getNome().equalsIgnoreCase(nome)) {
                return produto;
            }
        }
        throw new ProdutoNaoEncontradoException("Produto não encontrado com o nome: " + nome);
    }

    public List<Produto> listarTodos() throws IOException {
        
        List<Produto> produtos = new ArrayList<>();
        
        File arquivo = new File(nomeArquivo);
        
        if (!arquivo.exists()) {
            return produtos; // ainda não há nenhum produto salvo
        }

        try (BufferedReader leitor = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            int numeroLinha = 0;
            while ((linha = leitor.readLine()) != null) {
                numeroLinha++;
                if (linha.isBlank()) {
                    continue;
                }
                produtos.add(converterLinha(linha, numeroLinha));
            }
        }
        return produtos;
    }

    private String formatar(Produto produto) {
        Preco preco = produto.getPreco();
        EspecificacaoFisica espec = produto.getEspecificacaoFisica();
        return String.join(SEPARADOR,
                produto.getTipo(),
                String.valueOf(produto.getId()),
                produto.getNome(),
                produto.getDescricao(),
                String.valueOf(preco.getValorUnitario()),
                String.valueOf(preco.getMargemLucro()),
                String.valueOf(espec.getPesoKg()),
                String.valueOf(espec.getAlturaMetros()),
                String.valueOf(espec.getLarguraMetros()),
                String.valueOf(espec.getProfundidadeMetros()),
                String.valueOf(produto.getQuantidadeEstoque()));
    }

    private Produto converterLinha(String linha, int numeroLinha) throws IOException {
        String[] d = linha.split(SEPARADOR, -1);
        if (d.length != NUMERO_CAMPOS) {
            throw new IOException("Linha " + numeroLinha + " malformada em " + nomeArquivo);
        }

        try {
            String tipo = d[0];
            int id = Integer.parseInt(d[1]);
            String nome = d[2];
            String descricao = d[3];
            Preco preco = new Preco(Double.parseDouble(d[4]), Double.parseDouble(d[5]));
            EspecificacaoFisica espec = new EspecificacaoFisica(
                    Double.parseDouble(d[6]), Double.parseDouble(d[7]),
                    Double.parseDouble(d[8]), Double.parseDouble(d[9]));
            double estoque = Double.parseDouble(d[10]);

            Produto produto = switch (tipo) {
                case "PECA" -> new Peca(id, nome, descricao, preco, espec);
                case "MAQUINARIO" -> new Maquinario(id, nome, descricao, preco, espec);
                case "IMPLEMENTO" -> new Implemento(id, nome, descricao, preco, espec);
                default -> throw new IOException(
                        "Tipo desconhecido '" + tipo + "' na linha " + numeroLinha);
            };

            produto.setQuantidadeEstoque(estoque);
            return produto;

        } catch (NumberFormatException e) {
            throw new IOException("Número inválido na linha " + numeroLinha + " de " + nomeArquivo, e);
        }
    }
}