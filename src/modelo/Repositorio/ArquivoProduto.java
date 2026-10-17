package modelo.Repositorio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import modelo.Excecoes.*;
import modelo.Produtos.*;

/**
 * Formato da linha:
 * TIPO;id;nome;descricao;valorUnitario;margemLucro;pesoKg;altura;largura;profundidade;estoque
 */
public class ArquivoProduto extends ArquivoTexto<Produto , Integer> {

    private static final int NUMERO_CAMPOS = 11;

    public ArquivoProduto(String caminho) {
        super(caminho);
    }

    @Override
    protected Integer getChave(Produto produto) {
        return produto.getId();
    }

    @Override
    protected String nomeEntidade() {
        return "Produto";
    }

    @Override
    protected String formatar(Produto produto) {
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

    @Override
    protected Produto converter(String linha, int numeroLinha) throws IOException {
        String[] d = linha.split(SEPARADOR, -1);
        if (d.length != NUMERO_CAMPOS) {
            throw linhaInvalida(numeroLinha, "esperados " + NUMERO_CAMPOS + " campos", null);
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
            int estoque = Integer.parseInt(d[10]);

            Produto produto = switch (tipo) {
                case "PECA" -> new Peca(id, nome, descricao, preco, espec);
                case "MAQUINARIO" -> new Maquinario(id, nome, descricao, preco, espec);
                case "IMPLEMENTO" -> new Implemento(id, nome, descricao, preco, espec);
                default -> throw linhaInvalida(numeroLinha, "tipo desconhecido '" + tipo + "'", null);
            };

            produto.setQuantidadeEstoque(estoque);
            return produto;

        } catch (NumberFormatException e) {
            throw linhaInvalida(numeroLinha, "número inválido", e);
        }
    }

    // ---- Consultas específicas de produto ----

    public Produto buscarPorNome(String nome) throws IOException, ProdutoNaoEncontradoException {
        for (Produto produto : listarTodos()) {
            if (produto.getNome().equalsIgnoreCase(nome)) {
                return produto;
            }
        }
        throw new ProdutoNaoEncontradoException("Produto não encontrado com o nome: " + nome);
    }

    public List<Produto> listarPorTipo(String tipo) throws IOException {
        
        List<Produto> resultado = new ArrayList<>();
        
        for (Produto produto : listarTodos()) {
            if (produto.getTipo().equals(tipo)) {
                resultado.add(produto);
            }
        }
        return resultado;
    }
}