package modelo.Servicos;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.function.Function;
import modelo.Operacoes.ItemOps;
import modelo.Produtos.EspecificacaoFisica;
import modelo.Produtos.Implemento;
import modelo.Produtos.Maquinario;
import modelo.Produtos.Peca;
import modelo.Produtos.Preco;
import modelo.Produtos.Produto;

public class CadastroProdutos {

    public static Function<ItemOps, Produto> criador = item -> {
        
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            
            System.out.println("\n=== CADASTRO DE PRODUTO (ID = " + item.getIdProduto() + ") ===\n");
            System.out.println("1 - Maquinário");
            System.out.println("2 - Implemento");
            System.out.println("3 - Peça");
            System.out.print("Selecione o tipo de produto: ");
            int tipo = Integer.parseInt(scanner.nextLine());

            // Se o ItemOps já trouxer o ID da operação, use item.getIdProduto(); caso contrário, solicite:
            System.out.print("Nome do Produto: ");
            String nome = scanner.nextLine();

            System.out.print("Descrição do Produto: ");
            String descricao = scanner.nextLine();

            // 1. Composição: Preço
            System.out.println("\n-- Dados Financeiros --");
            System.out.print("Preço de Custo/Compra (R$): ");
            double precoCompra = Double.parseDouble(scanner.nextLine());

            System.out.print("Margem de Lucro (ex: 0.30 para 30%): ");
            double margem = Double.parseDouble(scanner.nextLine());

            Preco preco = new Preco(precoCompra, margem);

            // 2. Composição: Especificação Física
            System.out.println("\n-- Dimensões e Medidas --");
            System.out.print("Peso (kg): ");
            double peso = Double.parseDouble(scanner.nextLine());

            System.out.print("Altura (m): ");
            double altura = Double.parseDouble(scanner.nextLine());

            System.out.print("Largura (m): ");
            double largura = Double.parseDouble(scanner.nextLine());

            System.out.print("Profundidade (m): ");
            double profundidade = Double.parseDouble(scanner.nextLine());

            EspecificacaoFisica especificacao = new EspecificacaoFisica(peso, altura, largura, profundidade);

            // 3. Identificador (ID)
            // Substitua pelo método getter de ItemOps se existir (ex: item.getIdProduto())
            int id = item.getIdProduto();

            switch (tipo) {
                case 1 -> {
                    return new Maquinario(id, nome, descricao, preco, especificacao);
                }
                case 2 -> {
                    return new Implemento(id, nome, descricao, preco, especificacao);
                }
                case 3 -> {
                    return new Peca(id, nome, descricao, preco, especificacao);
                }
                default -> {
                    throw new IllegalArgumentException("Tipo de produto inválido: " + tipo);
                }
            }
        }
    };
}