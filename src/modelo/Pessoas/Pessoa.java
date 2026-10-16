package modelo.Pessoas;

import modelo.Endereco;

// Superclasse Abstrata
public abstract class Pessoa {

    private final String nome;
    private final String cpf;
    private final String contato;
    private final Endereco endereco;

    public Pessoa(String nome, String cpf, String contato, Endereco endereco) {
        this.nome = nome;
        this.cpf = cpf;
        this.contato = contato;
        this.endereco = endereco;
    }

    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getContato() { return contato; }
    public Endereco getEndereco() { return endereco; }

    public String getPerfil() {
        return "Nome: " + nome +
               "\nCPF: " + cpf +
               "\nTelefone: " + contato +
               "\nEndereço: " + endereco.getEnderecoCompleto();
    }
}