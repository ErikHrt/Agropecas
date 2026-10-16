package modelo.Repositorio;

import java.io.IOException;
import java.util.List;

import modelo.Excecoes.CodigoDuplicadoException;
import modelo.Excecoes.ItemNaoEncontradoException;

/**
 * Contrato de armazenamento.
 * T = tipo do item (Produto, Cliente...), K = tipo da chave (Integer, String/CPF...).
 */
public interface Repositorio<T, K> {

    void salvar(T item) throws IOException, CodigoDuplicadoException;

    T buscarPorChave(K chave) throws IOException, ItemNaoEncontradoException;

    List<T> listarTodos() throws IOException;

    void atualizar(T item) throws IOException, ItemNaoEncontradoException;

    void remover(K chave) throws IOException, ItemNaoEncontradoException;
}