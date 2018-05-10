import java.io.IOException;

/**
 *
 * @author Gabriel Franzoni 15105090
 */
public class Main {

    //Início da execução
    public static void main(String[] args) throws IOException {
    
        Escalonador sch = new Escalonador();
        sch.schRead();
        sch.schExec();
        sch.calculateMetrics();
        
    }
    
}
