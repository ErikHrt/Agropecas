package modelo.Repositorio;

import java.io.IOException;
import modelo.Endereco;
import modelo.Pessoas.*;

public class ArquivoFuncionario extends ArquivoTexto<Funcionario,String>{

    private final int NUMERO_CAMPOS = 11;

    public ArquivoFuncionario(String caminho){
        super(caminho);
    }

    @Override 
    protected String getChave(Funcionario funcionario){
        return funcionario.getCpf();
    }

    @Override 
    protected String nomeEntidade(){
        return "Funcionario";
    }

    @Override 
    protected String formatar(Funcionario funcionario){
        Endereco e = funcionario.getEndereco();

        return String.join(SEPARADOR,
                        funcionario.getCpf(),
                        funcionario.getNome(),
                        funcionario.getContato(),
                        e.getRua(),
                        e.getNumero(),
                        e.getCidade(),
                        e.getEstado(),
                        funcionario.getCargo(),
                        String.valueOf(funcionario.getSalarioBase()),
                        String.valueOf(funcionario.getTaxaComissao()),
                        String.valueOf(funcionario.getTotalVendasNoMes())
        );
    }

    @Override 
    protected Funcionario converter(String linha, int numeroLinha) throws IOException{
        String[] d = linha.split(SEPARADOR, -1);

        if(d.length != NUMERO_CAMPOS){
            throw linhaInvalida(numeroLinha,"esperados" + NUMERO_CAMPOS + "campos", null);
        }

        try {
            Endereco endereco = new Endereco(d[3],d[4],d[5],d[6]);
            double TotalVendasnoMes = Double.parseDouble(d[10]);
            
            Funcionario f = new Funcionario(d[1], d[0], d[2], endereco, 
                d[7],Double.parseDouble(d[8]), Double.parseDouble(d[9]));

            f.setTotalVendasNoMes(TotalVendasnoMes);

            return f;            
        } catch (NumberFormatException e) {
            throw linhaInvalida(numeroLinha, linha, e);
        }
    }


    
}