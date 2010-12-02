import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class myUDPClient {
	public static void main (String[] args) throws Exception {
		InetAddress serverAddress = InetAddress.getByName(getAddress());
		int port = getPort();
		
		String slash = File.separator;
		//create socket for udp
		//doesn't establish connection, so no address given
		DatagramSocket clientSocket = new DatagramSocket();
		
		// Find server's home directory
		File homeDirectory = new File(System.getProperty("user.home") + slash + "client");
		
		byte[] sendData = new byte[1024];
		
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.print("Enter loss probability: ");
		double p = Double.parseDouble(inFromUser.readLine());
		if (p > 1.0) p = 1.0;
		
		System.out.print("Enter filename to download: ");
		String fileRequest = inFromUser.readLine();
		File writeFile = new File(homeDirectory + slash + fileRequest);
		writeFile.setWritable(true);
		sendData = fileRequest.getBytes();
		
		DatagramPacket sendPacket = 
			new DatagramPacket(sendData, sendData.length, serverAddress, port);
		
		clientSocket.send(sendPacket);
		
		//this is where we begin receiving things from the server
		
		byte[] recvData = new byte[1024];
		DatagramPacket recvPacket = new DatagramPacket(recvData, recvData.length);
		clientSocket.receive(recvPacket);
		
		int requestedFileSize = Integer.parseInt(new String(recvPacket.getData()).trim());
		System.out.println("File Size: " + requestedFileSize/1024.0 + "Kb");

		
		BufferedOutputStream fileBytes = new BufferedOutputStream(new FileOutputStream(writeFile));
		
		
		
		int offset = 0;
		int packetSize = 1024;
		
		while (offset <= requestedFileSize) {
			if (offset  + 1024 > requestedFileSize) {	//check for end of file
				packetSize = requestedFileSize - offset;	//change size of the last packet
			}
			
			byte[] packetBytes = new byte[packetSize];			
			DatagramPacket filePacket = new DatagramPacket(packetBytes, packetBytes.length);
			
			byte[] ackDat = ("y").getBytes();
			DatagramPacket ackPack = new DatagramPacket(ackDat, ackDat.length, serverAddress, port);
			
			clientSocket.receive(filePacket);
			
			if (Math.random() > p) {
				
				fileBytes.write(filePacket.getData(), 0, packetSize);
				offset += 1024;	//cumulative byte count
				
				//send acknowledgments
				clientSocket.send(ackPack);
				
				System.out.println("Received packet: " + offset/1024);
				//System.out.println("last packet size: " + packetSize);
			}
			else {
				System.out.println("Discarding packet...");
			}
		}
		

		fileBytes.flush();
		fileBytes.close();
		clientSocket.close();
		
	}
	
	private static int getPort() {
		Scanner scan = new Scanner(System.in);
		System.out.print("Enter port number: ");
		return scan.nextInt();
	}
	
	private static String getAddress() {
		Scanner scan = new Scanner(System.in);
		System.out.print("Enter IP Address: ");
		return scan.nextLine();
	}
}
