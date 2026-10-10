
public class main 
{
    public static void main(String[] args) 
    {
        System.out.println("Olá, Mundo! Meu primeiro programa no VS Code.");

        Cliente c = new Cliente("Erik","1234","99123",new Endereco("Tiradentes","1666","Ponta Porã","MS"));

        System.out.print(c.getPerfil());
        System.exit(0);
    }
}