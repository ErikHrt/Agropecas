package modelo;

import java.io.IOException;
import modelo.Excecoes.*;
import modelo.ManipulacaoArquivos.ArquivoProduto;
import modelo.Produtos.*;

public class main 
{
    public static void main(String[] args) 
    {
        System.out.println("Olá, Mundo! Meu primeiro programa no VS Code.");

        //Cliente c = new Cliente("Erik","1234","99123",new Endereco("Tiradentes","1666","Ponta Porã","MS"));
        Peca p1 = new Peca(1,"Peça de teste","Descrição da peça de teste",new Preco(100,0.2),new EspecificacaoFisica(10,0.5,0.5,0.5));
        
        ArquivoProduto arquivoProduto = new ArquivoProduto("dados/peca.txt");
        
        try {
            arquivoProduto.salvar(p1);
        } catch (IOException e) {
            System.out.println("Erro ao salvar produto: " + e.getMessage());
        }
        
        try { 
            Produto p = arquivoProduto.buscarPorNome("Peça de MERDA");
            System.out.print(p.getTodaDescricao());
        } catch (IOException e) {
            System.out.println("Erro ao buscar produto: " + e.getMessage());
        } catch (ProdutoNaoEncontradoException e) {
            System.out.println("Produto não encontrado: " + e.getMessage());
        }
        
        //System.out.print(c.getPerfil());
        System.exit(0);
    }
}