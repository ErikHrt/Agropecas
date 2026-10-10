public abstract class Produto {
    
    private final int id;
    private final String nome;
    private String descricao;
    
    private Preco preco; 
    private final EspecificacaoFisica especificacaoFisica;

    public Produto(int id, String nome,String descricao, Preco preco, EspecificacaoFisica especificacaoFisica) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.especificacaoFisica = especificacaoFisica;
    }

    // Getters e setters
    public int getId() { return id; }

    public String getNome() { return nome; }
    
    public double getPreco() { return preco.getValordeVenda();}

    public String getTodaDescricao() {
        return "ID: " + id + 
               "\nNome: " + nome + 
               "\nDescrição: " + descricao + 
               "\nPreço: " + preco.getValordeVenda() +
               "\nPeso: " + especificacaoFisica.getPesoKg() + " kg" +
               "\nVolume: " + especificacaoFisica.calcularVolumeMetrosCubicos() + "m³";
    }

    public void setDescricao(String descricao) { this.descricao = descricao; }

    public void setPreco(Preco preco) { this.preco = preco; }
    
    public abstract double calcularFrete();
}