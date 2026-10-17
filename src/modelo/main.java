package modelo;

import java.io.IOException;

import modelo.Produtos.*;
import modelo.Repositorio.*;
import modelo.Excecoes.*;
import modelo.Pessoas.*;
import modelo.Operacoes.*;
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

        // =====================
        // 1. TESTE DE PRODUTOS 
        // =====================

        ArquivoProduto produtos = new ArquivoProduto(caminhoProdutos);
        

        Peca p1 = new Peca(1,"Peça Teste","Descrição da Peça de teste",
                            new Preco(10.0,0.4),
                            new EspecificacaoFisica(10,7,7,7));

        Maquinario m1 = new Maquinario(2,"Trator","Descrição Trator",
                            new Preco(1000000, 0.5),
                            new EspecificacaoFisica(9999, 50, 25, 25)
        );

        Implemento i1 = new Implemento(3, "Arado", "Descrição do Arado",
                            new Preco(5000,0.7),
                            new EspecificacaoFisica(750,2,10,10)
        );
        
        p1.setQuantidadeEstoque(50);
        m1.setQuantidadeEstoque(3);
        i1.setQuantidadeEstoque(1);

        try {
            
            produtos.salvar(p1);
            produtos.salvar(m1);
            produtos.salvar(i1);

        } 
        catch (CodigoDuplicadoException e) {
            System.out.println("Erro: " + e.getMessage());
        } 
        catch (IOException e) {
            System.out.println("Erro de arquivo: " + e.getMessage());
        }
        
        // Teste de Código Duplicado

        Peca p2_mesmoID = new Peca(1, "Peça com ID duplicado","Descrição da Peça de teste",
                            new Preco(10.0,0.4),
                            new EspecificacaoFisica(10,7,7,7));
                            
        try{
            produtos.salvar(p2_mesmoID);
        }
        catch (CodigoDuplicadoException e) {
            System.out.println("Erro: " + e.getMessage());
        } 
        catch (IOException e) {
            System.out.println("Erro de arquivo: " + e.getMessage());
        }
        
        // =====================
        // 2. TESTE DE PESSOAS 
        // =====================

        ArquivoCliente clientes = new ArquivoCliente(caminhoClientes);
        ArquivoFuncionario funcionarios = new ArquivoFuncionario(caminhoFuncionarios);

        Cliente c1 = new Cliente("FULANO","CPFdoCliente1","Fulano@gmail.com",
                                    new Endereco("Rua A","100","São Carlos","SP"),
                                    0);
        
        Funcionario f1 = new Funcionario("Alguém", "CPFdoFuncionario","(11) 0800",
                                        new Endereco("Rua B","10", "São Paulo","SP") , "Vendedor",
                                        5000,0.1);
        
        try {

            clientes.salvar(c1);
            funcionarios.salvar(f1);

        } catch (CodigoDuplicadoException e) {
            System.out.println("Erro: " + e.getMessage());
        } 
        catch (IOException e) {
            System.out.println("Erro de arquivo: " + e.getMessage());
        }

        // Teste Atualizar Campo

        c1.setSaldoDevedor(1000);

        try{

            clientes.atualizar(c1);
        }
        catch (IOException | ItemNaoEncontradoException e) {
            System.out.println("Erro: " + e.getMessage());
        }

        // Teste Buscar por chave não existente
        try {
            
            // A chave para arquivos de Pessoas é o CPF

            clientes.buscarPorChave("CPF_de_cliente_não_cadastrado");

        } catch (IOException | ItemNaoEncontradoException e) {

            System.out.println("Erro: " + e.getMessage());
        }
        
        // ===================
        // 3. TESTE DE VENDA 
        // ===================

        ArquivoVenda vendas = new ArquivoVenda(caminhoVendas,clientes,funcionarios);

        Venda venda1 = new Venda(c1,f1);

        try {

            venda1.adicionarAoCarrinho(p1, 1);
            venda1.adicionarAoCarrinho(p1, 2);
            venda1.adicionarAoCarrinho(m1, 1);
            
        } catch (ValorInvalidoException | EstoqueInsuficienteException e) {

            System.out.println("ERRO: " + e.getMessage() );
        }

        // Teste Estoque Insufuciente

        try {

            venda1.adicionarAoCarrinho(produtos.buscarPorChave(1), 100000);
            
        } catch ( ItemNaoEncontradoException | ValorInvalidoException | IOException | EstoqueInsuficienteException e){

            System.out.println("ERRO: " + e.getMessage() );

        }


        System.out.println(venda1.toString());
        venda1.removerDoCarrinho(2);
        System.out.println(venda1.toString());

        System.out.print("VALOR DA VENDA = " + venda1.getValor());

        venda1.setMetodoPagamento("PIX");

        ServicoVenda VendaCompleta = new ServicoVenda( produtos,funcionarios, vendas);
        
        try{
             VendaCompleta.finalizarVenda(venda1);
        }
        catch( IOException | ItemNaoEncontradoException | EstoqueInsuficienteException | CodigoDuplicadoException e){
            System.err.print("Erro : " + e.getMessage());
        }

        // ===================
        // 4. TESTE DE COMPRA
        // ===================

        ArquivoCompra compras = new ArquivoCompra(caminhoCompras,funcionarios);
        
        Compra compra1 = new Compra(f1);
        
        ItemOps item1 = new ItemOps(1,10,1000);
        item1.setLocalDaCompra("LOCAL 1");
        ItemOps item2 = new ItemOps(1010, 5, 1000);
        item2.setLocalDaCompra("Local 2");
        
        compra1.adicionarItemNosComprados(item1);
        compra1.adicionarItemNosComprados(item2);

        System.out.print("\n VALOR DA COMPRA" + compra1.getValor() + "\n");

        ServicoCompra CompraCompleta = new ServicoCompra(produtos, compras);

        try{
            CompraCompleta.finalizarCompra(compra1, CadastroProdutos.criador);
        }
        catch(IOException | ItemNaoEncontradoException | CodigoDuplicadoException e){
            System.err.print("Erro: "+ e.getMessage());
        }

    }
}