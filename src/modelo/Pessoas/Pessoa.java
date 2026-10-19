package modelo.Pessoas;

import modelo.Endereco;

// Superclasse Abstrata
public abstract class Pessoa {

    protected final String nome;
    protected final String cpf;
    protected String contato;
    protected Endereco endereco;

    public Pessoa(String nome, String cpf, String contato, Endereco endereco) {
        this.nome = nome;
        this.cpf = cpf;
        this.contato = contato;
        this.endereco = endereco;
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getContato() { return contato; }
    public Endereco getEndereco() { return endereco; }

    public void setContato( String contato){ this.contato = contato; }
    public void setEndereco(Endereco endereco ){this.endereco = endereco;}

    public String getPerfil() {
        return "Nome: " + nome +
               "\nCPF: " + cpf +
               "\nTelefone: " + contato +
               "\nEndereço: " + endereco.getEnderecoCompleto();
    }
}