package modelo.Operacoes;

public class ItemOps {

    private final int idProduto;
    private final double valorUn;
    private int qtd;

    private String LocalDaCompra;

    public ItemOps(int idProduto, int qtd, double valorUn){

        this.idProduto = idProduto;
        this.qtd = qtd;
        this.valorUn = valorUn;
    }

    public ItemOps(int idProduto, int qtd, double valorUn,String LocalDaCompra){

        this.idProduto = idProduto;
        this.qtd = qtd;
        this.valorUn = valorUn;
        this.LocalDaCompra = LocalDaCompra;
    }

    public int getIdProduto(){ return idProduto;}
    public int getQtd(){ return  qtd;}
    public double getValorUn(){return valorUn;}
    public String getLocalDaCompra(){return LocalDaCompra;}
    
    public void setQtd(int qtd){ this.qtd = qtd; }
    public void setLocalDaCompra(String Local){LocalDaCompra = Local;}

    @Override 
    public String toString(){
        return "Id Produto : " + String.valueOf(idProduto) + " | " +
                "Valor unitário: " + String.valueOf(valorUn) +  " | " +
                "Quantidade: " + String.valueOf(qtd) + " | "+ 
                "Local da Compra: " + LocalDaCompra ; 
    }
    
}
