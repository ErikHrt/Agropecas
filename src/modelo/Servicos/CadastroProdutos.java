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

    private static final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

    public static Function<ItemOps, Produto> criador = item -> {

        int id = item.getIdProduto();

        System.out.println("\n=== CADASTRO DE PRODUTO (ID = " + id + ") ===\n");
        System.out.println("1 - Maquinário");
        System.out.println("2 - Implemento");
        System.out.println("3 - Peça");
        int tipo = lerTipo();

        System.out.print("Nome do Produto: ");
        String nome = scanner.nextLine();

        System.out.print("Descrição do Produto: ");
        String descricao = scanner.nextLine();

        // Composição: Preço
        System.out.println("\n-- Dados Financeiros --");
        double precoCompra = lerDouble("Preço de Custo/Compra (R$): ");
        double margem = lerDouble("Margem de Lucro (ex: 0.30 para 30%): ");
        Preco preco = new Preco(precoCompra, margem);

        // Composição: Especificação Física
        System.out.println("\n-- Dimensões e Medidas --");
        double peso = lerDouble("Peso (kg): ");
        double altura = lerDouble("Altura (m): ");
        double largura = lerDouble("Largura (m): ");
        double profundidade = lerDouble("Profundidade (m): ");
        EspecificacaoFisica especificacao =
                new EspecificacaoFisica(peso, altura, largura, profundidade);

        return switch (tipo) {
            case 1 -> new Maquinario(id, nome, descricao, preco, especificacao);
            case 2 -> new Implemento(id, nome, descricao, preco, especificacao);
            default -> new Peca(id, nome, descricao, preco, especificacao); // 3
        };
    };

    // ---- Leituras com validação: repetem a pergunta até a entrada ser válida ----

    private static int lerTipo() {
        while (true) {
            System.out.print("Selecione o tipo de produto (1-3): ");
            try {
                int tipo = Integer.parseInt(scanner.nextLine().trim());
                if (tipo >= 1 && tipo <= 3) {
                    return tipo;
                }
            } catch (NumberFormatException e) {
                // cai na mensagem abaixo
            }
            System.out.println("Opção inválida. Digite 1, 2 ou 3.");
        }
    }

    private static double lerDouble(String pergunta) {
        while (true) {
            System.out.print(pergunta);
            try {
                // aceita vírgula decimal (0,30) além de ponto (0.30)
                return Double.parseDouble(scanner.nextLine().trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número.");
            }
        }
    }
}