package modelo.Produtos;

public abstract class Produto {

    private final int id;
    private final String nome;
    private String descricao;
    private Preco preco;
    private final EspecificacaoFisica especificacaoFisica;
    private int quantidadeEstoque;

    protected Produto(int id, String nome, String descricao, Preco preco,
                      EspecificacaoFisica especificacaoFisica) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.especificacaoFisica = especificacaoFisica;
        this.quantidadeEstoque = 0;
    }

    // Getters e Setters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public Preco getPreco() { return preco; }
    public EspecificacaoFisica getEspecificacaoFisica() { return especificacaoFisica; }
    public int getQuantidadeEstoque() { return quantidadeEstoque; }

    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setPreco(Preco preco) { this.preco = preco; }
    public void setQuantidadeEstoque(int quantidadeEstoque) { this.quantidadeEstoque = quantidadeEstoque; }

    public String getTodaDescricao() {
        return "Tipo: " + getTipo() +
               "\nID: " + id +
               "\nNome: " + nome +
               "\nDescrição: " + descricao +
               "\nPreço: " + preco.getValordeVenda() +
               "\nPeso: " + especificacaoFisica.getPesoKg() + " kg" +
               "\nVolume: " + especificacaoFisica.calcularVolumeMetrosCubicos() + " m³" +
               "\nQuantidade em estoque: " + quantidadeEstoque;
    }
    
    // O tipo do produto será colocado na primeira posição das infos do produto no ArquivoProduto
    public abstract String getTipo();
}