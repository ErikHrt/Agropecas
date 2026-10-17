package modelo.Repositorio;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import modelo.Excecoes.*;
import modelo.Operacoes.*;
import modelo.Pessoas.Funcionario;

public class ArquivoCompra extends ArquivoTexto<Compra, Integer> {

        private static final int NUMERO_CAMPOS = 4;
        private static final String SEPARADOR_ITENS = "|";
        private static final String SEPARADOR_CAMPOS_ITEM = ":";
        
        private final ArquivoFuncionario funcionarios;

        public ArquivoCompra(String caminho, ArquivoFuncionario funcionarios){
            super(caminho);
             this.funcionarios = funcionarios;
        }


        @Override
        protected String nomeEntidade(){
            return "Compra";
        }

        @Override
        protected  Integer getChave(Compra compra){
            return compra.getId();
        }

        @Override
        protected ItemNaoEncontradoException naoEncontrado(Integer id) {
            return new ItemNaoEncontradoException("Compra não encontrada com o ID: " + id);
        }

        @Override
        protected String formatar(Compra compra){

            List<String> itens = new ArrayList<>();

            for(ItemOps item : compra.getItensComprados()){

                itens.add(  item.getIdProduto() + SEPARADOR_CAMPOS_ITEM +
                            item.getQtd() + SEPARADOR_CAMPOS_ITEM +
                            item.getValorUn() + SEPARADOR_CAMPOS_ITEM +
                            item.getLocalDaCompra());
            }

            return String.join(SEPARADOR,
                                String.valueOf(compra.getId()),
                                compra.getFuncionario().getCpf(),
                                compra.getData().toString(),
                                String.join(SEPARADOR_ITENS, itens));
        }

        @Override
        protected Compra converter(String linha, int numeroLinha) throws IOException{

            String[] d = linha.split(SEPARADOR, -1);
            if (d.length != NUMERO_CAMPOS) {
                throw linhaInvalida(numeroLinha, "esperados " + NUMERO_CAMPOS + " campos", null);
            }

            try {
                    int id = Integer.parseInt(d[0]);
                    Funcionario funcionario = funcionarios.buscarPorChave(d[1]);
                    LocalDate data = LocalDate.parse(d[2]);

                    List<ItemOps> itens = new ArrayList<>();
                    if (!d[3].isEmpty()) {
                        for (String texto : d[3].split("\\" + SEPARADOR_ITENS)) {
                            String[] campos = texto.split(SEPARADOR_CAMPOS_ITEM, -1);
                            if (campos.length != 4) {
                                throw linhaInvalida(numeroLinha, "item malformado '" + texto + "'", null);
                            }
                            itens.add(new ItemOps(Integer.parseInt(campos[0]),
                                    Integer.parseInt(campos[1]),
                                    Double.parseDouble(campos[2]),
                                    campos[3]));
                        }
                    }

                    return new Compra(id, funcionario, data , itens);

                } catch (NumberFormatException e) {
                    throw linhaInvalida(numeroLinha, "número inválido", e);
                } catch (DateTimeParseException e) {
                    throw linhaInvalida(numeroLinha, "data inválida", e);
                } catch (ItemNaoEncontradoException e) {
                    throw linhaInvalida(numeroLinha, "Funcionário não encontrado", e);
                }

        }

        public int proximoId() throws IOException {
        int maior = 0;
        for (Compra compra : listarTodos()) {
            if (compra.getId() > maior) {
                maior = compra.getId();
            }
        }
        return maior + 1;
    }
    
}
