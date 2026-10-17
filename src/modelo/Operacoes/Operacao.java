package modelo.Operacoes;

import java.time.LocalDate;
import modelo.Pessoas.Funcionario;

// Superclasse Abstrata como base de Venda, Compra ... 
public abstract class Operacao {

    // Id de qualquer operação será definido apenas quando salvá-la
    private int id;                      
    private final Funcionario funcionario;
    private final LocalDate data;

    // Um Operação nova será sempre realizada por um funcionária em um data
    protected Operacao(Funcionario funcionario) {
        this.funcionario = funcionario;
        this.data = LocalDate.now();
    }

    // Operação carregada do arquivo
    protected Operacao(int id, Funcionario funcionario, LocalDate data) {
        this.id = id;
        this.funcionario = funcionario;
        this.data = data;
    }
    
    // Getters e Setters
    public int getId() { return id; }
    public Funcionario getFuncionario() { return funcionario; }
    public LocalDate getData() { return data; }

    public void setId(int id){ this.id = id; }

    @Override
    public String toString(){
        return "Id : " + id + "\n" + 
                "Funcionário : " + funcionario.getNome() + " | "  + funcionario.getCpf() + "\n" +
                "Data : " + data.toString() + "\n";
    }

    /** Cada tipo de operação sabe calcular o seu valor. */
    public abstract double getValor();

}