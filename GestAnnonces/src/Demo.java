
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
        String message = "Belle Moto moto***";
        String msg = message.substring(message.length() - 3, message.length());
        System.out.println(message = message.replace("***", " ***"));
        String[] m = message.split("\\s+");
        
        System.out.println(Arrays.toString(m));
    }

}
