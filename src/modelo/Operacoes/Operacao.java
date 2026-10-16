package modelo.Operacoes;

import java.time.LocalDate;
import modelo.Pessoas.Funcionario;

/** Base de Venda, Compra, Conserto, Frete... */
public abstract class Operacao {

    private int id;                      // 0 = ainda não salva
    private final Funcionario funcionario;
    private final LocalDate data;

    /** Operação nova: data de hoje, id definido ao salvar. */
    protected Operacao(Funcionario funcionario) {
        this.funcionario = funcionario;
        this.data = LocalDate.now();
    }

    /** Operação carregada do arquivo. */
    protected Operacao(int id, Funcionario funcionario, LocalDate data) {
        this.id = id;
        this.funcionario = funcionario;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    /** Só pode ser definido uma vez (feito pelo serviço, ao salvar). */
    public void setId(int id) {
        if (this.id != 0) {
            throw new IllegalStateException("O id da operação já foi definido");
        }
        this.id = id;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public LocalDate getData() {
        return data;
    }

    /** Cada tipo de operação sabe calcular o seu valor. */
    public abstract double getValor();


    @Override
    public String toString(){
        return "Id : " + id + "\n" + 
                "Funcionário : " + funcionario.getNome() + " | "  + funcionario.getCpf() + "\n" +
                "Data : " + data.toString() + "\n";
    }
}