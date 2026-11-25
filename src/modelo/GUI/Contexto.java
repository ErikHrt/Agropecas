package modelo.GUI;

import modelo.Backup.ServicoBackup;
import modelo.Repositorio.ArquivoCliente;
import modelo.Repositorio.ArquivoCompra;
import modelo.Repositorio.ArquivoFrete;
import modelo.Repositorio.ArquivoFuncionario;
import modelo.Repositorio.ArquivoProduto;
import modelo.Repositorio.ArquivoVenda;
import java.nio.file.Paths;

import modelo.Servicos.ServicoCompra;
import modelo.Servicos.ServicoFrete;
import modelo.Servicos.ServicoVenda;

/** Reúne repositórios e serviços para que todos os painéis usem os mesmos objetos. */
final class Contexto {

    final ArquivoProduto produtos = new ArquivoProduto("dados/produtos.txt");
    final ArquivoCliente clientes = new ArquivoCliente("dados/pessoas/clientes.txt");
    final ArquivoFuncionario funcionarios = new ArquivoFuncionario("dados/pessoas/funcionarios.txt");
    final ArquivoVenda vendas = new ArquivoVenda("dados/operacoes/vendas.txt", clientes, funcionarios);
    final ArquivoCompra compras = new ArquivoCompra("dados/operacoes/compras.txt", funcionarios);
    final ArquivoFrete fretes = new ArquivoFrete("dados/operacoes/fretes.txt", clientes, funcionarios);

    final ServicoVenda servicoVenda = new ServicoVenda(produtos, funcionarios, vendas);
    final ServicoCompra servicoCompra = new ServicoCompra(produtos, compras);
    final ServicoFrete servicoFrete = new ServicoFrete(produtos, clientes, funcionarios, fretes);
    final ServicoBackup servicoBackup = new ServicoBackup(produtos, clientes, funcionarios, vendas, compras, fretes,
            Paths.get("dados/backup"));
}
