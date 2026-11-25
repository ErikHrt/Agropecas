package modelo;

import java.io.Serializable;

public class Endereco implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String rua;
    private final String numero;
    private final String cidade;
    private final String estado;

    public Endereco(String rua, String numero, String cidade, String estado) {
        this.rua = rua;
        this.numero = numero;
        this.cidade = cidade;
        this.estado = estado;
    }

    // Getters
    public String getRua(){ return rua; }
    public String getNumero(){ return numero; }
    public String getCidade(){ return cidade; }
    public String getEstado(){ return estado; }
    public String getEnderecoCompleto() {
        return rua + ", " + numero + " - " + cidade + "/" + estado;
    }
    
}
