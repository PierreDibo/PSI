
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class Client {

	public static final int PACKET_SIZE = 576;
	private static final int ADRESS_PORT = 0, INDEX_PORT = 1;

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

		public void close() {

		}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		Socket tcp;
		if (args.length < 2 || args.length > 3) {
			System.err.println("Usage : java Client ip port_tcp [port_upd]");
		}
		try {
			InetAddress ia = InetAddress.getByName(args[ADRESS_PORT]);
			int port = Integer.parseInt(args[INDEX_PORT]);

			switch (args.length) {
			case 2:
				tcp = new Socket(ia, port);

				new Thread(new Ecrivain(tcp)).start();
				new Thread(new EcouteurTCP(tcp)).start();

				break;
			case 3:
				tcp = new Socket(ia, port);
				DatagramSocket udp = new DatagramSocket(port, ia);

				new Thread(new Ecrivain(tcp, udp)).start();
				new Thread(new EcouteurTCP(tcp)).start();
				new Thread(new EcouteurUDP(udp)).start();
				break;
			}
		} catch (UnknownHostException ex) {
			Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	static class Ecrivain implements Runnable {

		private final Socket sockettcp;
		private final DatagramSocket socketudp;

		private static final String ESP = " ";

		public Ecrivain(Socket socket) {
			this.sockettcp = socket;
			this.socketudp = null;
		}

		public Ecrivain(Socket tcp, DatagramSocket upd) {
			this.sockettcp = tcp;
			this.socketudp = upd;
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
			if (this.socketudp != null && !this.socketudp.isClosed()) {
				switch (message) {
				case CALL_OPEN:
				case CALL:
				case CALL_CLOSE:
					byte buf[] = content.getBytes();
					DatagramPacket packet
					= new DatagramPacket(buf, buf.length,
							this.socketudp.getInetAddress(),
							this.socketudp.getPort());
					this.socketudp.send(packet);
					break;
				case INVALID:
					System.out.println(MessageType.MSG_INVALID);
					break;
				default:
					writetcp(content);
					break;
				}
			} else {
				writetcp(content);
			}

		}

		private void writetcp(String content) throws IOException {
			MessageType message;
			String[] input = content.split("\\s+");
			int i = 0;
			try {
				message = MessageType.valueOf(input[i++]);
			} catch (IllegalArgumentException ex) {
				message = MessageType.INVALID;
			}
			switch (message) {
			case NEW:
				if(Integer.parseInt(input[i+4]) == this.socketudp.getLocalPort()) {
					System.out.println("OK");
				} else {
					System.out.println("NON");
				}
				break;
			default:
				break;
			}
			if (!this.sockettcp.isClosed()) {
				BufferedWriter output = new BufferedWriter(new OutputStreamWriter(this.sockettcp.getOutputStream()));
				output.write(content);
				output.newLine();
				output.flush();
			}
			}

			@Override
			public void run() {
				ConsoleInputReadTask console = new ConsoleInputReadTask();
				while (true) {
					try {
						String content = console.call();

						while (!content.contains(MessageType.END.getMessage())) {
							content += " " + console.call();
						}

						//content = content.replace(MessageType.END.getMessage(), ESP);

						parse(content);

						if (this.socketudp != null) {
							if (this.sockettcp.isClosed() && this.socketudp.isClosed()) {
								break;
							}
						} else {
							if (this.sockettcp.isClosed()) {
								break;
							}
						}
					} catch (IOException ex) {
						Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
		}

		static class EcouteurTCP implements Runnable {

			private final Socket socket;

			public EcouteurTCP(Socket socket) {
				this.socket = socket;
			}

			@Override
			public void run() {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String line;

					while ((line = br.readLine()) != null) {
						System.out.println(line);
						if (line.startsWith(MessageType.MSG_QUIT)) {
							this.socket.close();
							break;
						}
					}

				} catch (IOException ex) {
					Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}

		static class EcouteurUDP implements Runnable {

			private final DatagramSocket socketudp;

			public EcouteurUDP(DatagramSocket udp) {
				this.socketudp = udp;
			}

			@Override
			public void run() {

				// Create a byte buffer/array for the receive Datagram packet
				byte[] receiveData = new byte[PACKET_SIZE];

				// Set up a DatagramPacket to receive the data into
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				System.out.println("I am in the reader!");
				try {
					// Receive a packet from the server (blocks until the packets are received)
					socketudp.receive(receivePacket);
					System.out.println("Am i receiving?");
					// Extract the reply from the DatagramPacket      
					String serverReply = new String(receivePacket.getData(), 0, receivePacket.getLength());

					// print to the screen
					System.out.println("UDPClient: Response from Server: \"" + serverReply + "\"\n");

				} catch (IOException ex) {
					Logger.getLogger(Gestionnaire.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}

	}
