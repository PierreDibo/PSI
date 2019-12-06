
import java.util.Arrays;

/**
 *
 * @author dibop
 */
public class Demo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String message = "Belle Moto||moto";
        String[] m = message.split("\\|\\|");
        
        System.out.println(Arrays.toString(m));
    }

}
