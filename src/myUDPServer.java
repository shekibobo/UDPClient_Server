import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;


public class myUDPServer {
	public static void main(String[] args) throws Exception {
		DatagramSocket serverSocket = new DatagramSocket(getPort());
		
		while (true) {
			byte[] recvData = new byte[1024];
			DatagramPacket recvPacket = new DatagramPacket(recvData, recvData.length);
			serverSocket.receive(recvPacket);
			
			String slash = File.separator;
			
			String clientRequest = new String(recvPacket.getData());
			// Find server's home directory
			File homeDirectory = new File(System.getProperty("user.home") + slash + "server");
			System.out.println("User requested file: " + new File(homeDirectory + slash + clientRequest));
			File requestedFile = new File(homeDirectory + slash + clientRequest);
			
			BufferedInputStream fileBytes = new BufferedInputStream(new FileInputStream(requestedFile));
			
			//send the client the size of the file
			byte[] sendData = new byte[1024];
			sendData = ("" + requestedFile.length()).getBytes();
			
			
			InetAddress IPAddr = recvPacket.getAddress();
			int port = recvPacket.getPort();
			
			DatagramPacket sendPacket = 
				new DatagramPacket(sendData, sendData.length, IPAddr, port);
			serverSocket.send(sendPacket);
			
			//start sending the file
			int offset = 0;
			int length = (int)requestedFile.length();
			sendData = new byte[1024];
			serverSocket.setSoTimeout(500);
			while (offset <= length) {
				fileBytes.read(sendData, 0, 1024);
				offset += 1024;
				
				byte[] ack = new byte[0];
				while (ack.length == 0) {
					/*
					 * this is going to try and send the same data packet over 
					 * and over again until it receives an "acknowledgment" from 
					 * the client.  The ack, in this case, is simply a non-empty
					 * byte array; for the purposes of this application, the
					 * contents are trivial.  If nothing is received from the 
					 * client within the specified timeout (ms), we catch an 
					 * exception and try again.
					 */
					try {
						sendPacket = new DatagramPacket(sendData, sendData.length, IPAddr, port);
						serverSocket.send(sendPacket);
						System.out.println("packet#: " + offset/1024);
						serverSocket.receive(recvPacket);
						ack = recvPacket.getData();
					} catch (SocketTimeoutException ste) {
						System.out.println("Ack not received, resending...");
					}
				}
			}
			serverSocket.setSoTimeout(0);
			System.out.println("File transfer complete: " + clientRequest);
			
			
		}
	}
	
	private static int getPort() {
		Scanner scan = new Scanner(System.in);
		System.out.print("Enter port number: ");
		return scan.nextInt();
	}

}
