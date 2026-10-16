package modelo;

import java.io.IOException;
import modelo.Excecoes.*;
import modelo.Operacoes.*;
import modelo.Pessoas.*;
import modelo.Produtos.*;
import modelo.Repositorio.*;
import modelo.Servicos.*;

public class main 
{
    public static void main(String[] args) 
    {
        String caminhoProdutos = "dados/produtos.txt";
        String caminhoFuncionarios = "dados/pessoas/funcionarios.txt";
        String caminhoClientes = "dados/pessoas/clientes.txt";
        String caminhoVendas = "dados/operacoes/vendas.txt";
        String caminhoCompras = "dados/operacoes/compras.txt";
    

        System.out.println("Olá, Mundo!");
  
        Cliente c = new Cliente("Erik","1234","99123",
        new Endereco("Tiradentes","1666","Ponta Porã","MS"),0);

        Funcionario f = new Funcionario("Gabi","1231","2423523",
        new Endereco("Rua A","1245","Pouso Alegre","MG"),
        "CHEFE",9999,0.2);
        
        Peca p1 = new Peca(1,"Peça de teste","Descrição da peça de teste",
        new Preco(100,0.2),new EspecificacaoFisica(10,0.5,0.5,0.5));

        Peca p2 = new Peca(2,"Peça de teste","Descrição da peça de teste 2",
        new Preco(1000,0.45),new EspecificacaoFisica(10,0.5,0.5,0.5));
        
        p1.setQuantidadeEstoque(10);

        p2.setQuantidadeEstoque(100);

        ArquivoProduto produtos = new ArquivoProduto(caminhoProdutos);

        ArquivoFuncionario Funcionarios = new ArquivoFuncionario(caminhoFuncionarios);
        
        ArquivoCliente clientes = new ArquivoCliente(caminhoClientes);
        
    
        try {

            clientes.salvar(c);
            produtos.salvar(p1);
            produtos.salvar(p2);
            Funcionarios.salvar(f);

        } catch (CodigoDuplicadoException e) {
            System.out.println("Erro: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Erro de arquivo: " + e.getMessage());
        }
        

        ArquivoVenda vendas = new ArquivoVenda(caminhoVendas, clientes, Funcionarios);
        ServicoVenda servico = new ServicoVenda(produtos, Funcionarios, vendas);


        Venda v = new Venda( c ,f);
        try {
            
            v.adicionarAoCarrinho(produtos.buscarPorChave(1), 2);
            v.adicionarAoCarrinho(produtos.buscarPorChave(2), 10);
            v.adicionarAoCarrinho(produtos.buscarPorChave(50), 1);
        } 
        catch (ItemNaoEncontradoException | EstoqueInsuficienteException | IOException e) {
            System.out.println("Erro: " + e.getMessage());
        }

        v.setMetodoPagamento("PIX");
        
        try {

            servico.finalizarVenda(v);    

        } catch (IOException | EstoqueInsuficienteException | CodigoDuplicadoException | ItemNaoEncontradoException e) {

            System.out.println("Erro: " + e.getMessage());
        }
        
        try{
            for(Venda vendas_feitas : vendas.listarTodos()){

                System.out.println(vendas_feitas.toString());
            }
            
        }
        catch(IOException e){
            System.err.println("ERRO"+ e.getMessage());
        }

        ArquivoCompra compras = new ArquivoCompra(caminhoCompras, Funcionarios);
        ServicoCompra servicoCompra = new ServicoCompra(produtos, compras);

        Compra compra = new Compra(f);
        
        ItemOps item_comprado = new ItemOps(50, 50, 1200.0, "Agro Sul");
        ItemOps item_comprado_2 = new ItemOps(1, 1000, 100, "AgroPeças");

        compra.adicionarItemNosComprados(item_comprado);
        compra.adicionarItemNosComprados(item_comprado_2);
        
        try {
                servicoCompra.finalizarCompra(compra, CadastrarProdutos.criador);  

        } catch (IOException | ItemNaoEncontradoException | CodigoDuplicadoException e) {
            
            System.err.println("ERRO"+ e.getMessage());
        }
        
        try {
                Compra c1 = (Compra) compras.buscarPorChave(50);

                System.out.println(c1.toString());
            
        } catch (IOException | ItemNaoEncontradoException e) {
        }
    }
    }