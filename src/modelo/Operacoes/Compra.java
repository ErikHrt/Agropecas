package modelo.Operacoes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import modelo.Pessoas.Funcionario;


public class Compra extends  Operacao {
    
    private final List<ItemOps> itensComprados;

    public Compra(Funcionario funcionario){

        super(funcionario);

        this.itensComprados = new ArrayList<>();
    }

    public Compra(int id, Funcionario funcionario, LocalDate data, List<ItemOps> itensComprados){

        super(id,funcionario,data);
        this.itensComprados = new ArrayList<>(itensComprados);
    }

    public List<ItemOps> getItensComprados(){
        return Collections.unmodifiableList(itensComprados);
    }

    public void adicionarItemNosComprados(ItemOps item) {
        if (item.getQtd() <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva");
        }
        if (item.getValorUn() < 0) {
            throw new IllegalArgumentException("Valor unitário inválido");
        }

        ItemOps existente = buscarItem(item.getIdProduto(), item.getValorUn(), item.getLocalDaCompra());
        if (existente != null) {
            existente.setQtd(existente.getQtd() + item.getQtd());
        } else {
            itensComprados.add(item);
        }
    }   

    private ItemOps buscarItem(int idProduto, double valorUn, String local) {
        for (ItemOps i : itensComprados) {
            if (i.getIdProduto() == idProduto
                    && Double.compare(i.getValorUn(), valorUn) == 0
                    && Objects.equals(i.getLocalDaCompra(), local)) {
                return i;
            }
        }
        return null;
    }

    public void removerDaListaDeCompras(int idProduto) {
        itensComprados.removeIf(item -> item.getIdProduto() == idProduto);
    }

    public double calcularTotal() {
        double total = 0;
        for (ItemOps item : itensComprados) {
            total += item.getQtd() * item.getValorUn();
        }
        return total;
    }

    @Override
    public double getValor() {
        return calcularTotal();
    }
 
}