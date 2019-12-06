
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class ClientEcrivain extends Client implements Runnable {

    private Socket socket;

    public ClientEcrivain(Socket s) {
        this.socket = s;
    }

    @Override
    public void run() {
        ConsoleInputReadTask console = new ConsoleInputReadTask();
        for (;;) {
            try {
                String content = console.call();

                while (!content.contains(MessageType.END.getMessage())) {
                    content += " " + console.call();
                }

                parse(content);

                if (this.socket.isClosed()) {
                    break;
                }
            } catch (IOException ex) {
                Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void parse(String content) throws IOException {
        MessageType message;
        String[] input = content.split("\\s+");
        int i = 0;
        try {
            message = MessageType.valueOf(input[i++]);
        } catch (IllegalArgumentException ex) {
            message = MessageType.INVALID;
        }
        switch (message) {
            case CALL_OPEN:
                break;
            case CALL:
                break;
            case CALL_CLOSE:
                break;
            default:
                write(content, this.socket);
                break;
        }
    }

    private void write(String content, Socket socket) throws IOException {
        if (!socket.isClosed()) {
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            output.write(content);
            output.newLine();
            output.flush();
        }
    }

    public void setSocketClient(Socket socketClient) {
        this.socket = socketClient;
    }

    static class ConsoleInputReadTask implements Callable<String> {

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public String call() throws IOException {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(System.in));
            String input;
            do {
                try {
                    while (!br.ready()) {
                        Thread.sleep(200);
                    }
                    input = br.readLine();
                } catch (InterruptedException e) {
                    System.out.println("ConsoleInputReadTask() cancelled");
                    return null;
                }
            } while ("".equals(input));
            return input;
        }

    }

}
