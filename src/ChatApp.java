import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;


// Sender procedure
class Sender implements Runnable {
	
	private int port;
	private String user_name;
	
	Sender(String user, int portNum){
		user_name = user;
	    port = portNum;
	
	}
	
	public String build_message(String msg, String com, String user){
		return "user: " + user + "\ncommand: " + com + "\nmessage: " + msg + "\n";		
	}
	
	public void sendTo(String app_msg, DatagramSocket sock) throws Exception{
		byte buffer[] = app_msg.getBytes();
	//	InetAddress address = InetAddress.getByName(ipAddr);
		InetSocketAddress address = new InetSocketAddress("255.255.255.255", 4321);
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address);
		sock.send(packet);
	}
	
	public void run() {
		DatagramSocket s = null;
		try {
			s = new DatagramSocket();
			s.setBroadcast(true);
			String application_message;
			String joinMsg;
			joinMsg = build_message("","join",user_name);
			try {
				sendTo(joinMsg, s);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			while(true){
				Scanner keyboard = new Scanner(System.in);
				String msg = keyboard.nextLine();
				
				if (msg.startsWith("/")){
					if(msg.substring(msg.indexOf("/")+1, msg.length()).equals("leave")){
						application_message = build_message(msg,"leave",user_name);
						try {
							sendTo(application_message, s);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					else if (msg.substring(msg.indexOf("/")+1, msg.length()).equals("who")){
						application_message = build_message("", "who", user_name);
						try {
							sendTo(application_message, s);
						}  catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
				application_message = build_message(msg,"talk",user_name);
				
				try {
					sendTo(application_message, s);
					Thread.sleep(50);
				} catch (Exception e) {
					e.printStackTrace();
				}
				}
					}
					
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		
	}
	
}

// Receiver procedure
class Receiver implements Runnable{
	
	private int port;
	private String user_name;
	private String command;
	private String user_message;
	List<String> connected;
	byte buffer[];
	
	Receiver(int portNum){
	//	s = sock;
		buffer = new byte[1024];
		port = portNum;
	}
	
	public void parse_message(String appMsg){
		Scanner scanner = new Scanner(appMsg);
		user_name = scanner.nextLine();
		command = scanner.nextLine();
		user_message = scanner.nextLine();
		user_name = user_name.substring(user_name.indexOf(':')+2, user_name.length());
		command = command.substring(command.indexOf(':')+2, command.length());
		user_message = user_message.substring(user_message.indexOf(':')+2, user_message.length());
	}
	
	public void print(String user, String msg){
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date dateobj = new Date();
		System.out.println(sdf.format(dateobj) + " [" + user_name + "]: " + msg);	
	}

	public void run() {
		DatagramSocket s = null;
		
		try {
			s = new DatagramSocket(4321);
			connected = new ArrayList<String>();
		//	s.setBroadcast(true);
				while(true){
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					s.receive(packet);
					String application_message = new String(packet.getData(), 0, packet.getLength());
			//		System.out.println(application_message);
					parse_message(application_message);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
					Date dateobj = new Date();
					if (command.equals("talk"))
						print(user_name, user_message);
					else if (command.equals("join")){
						System.out.println(sdf.format(dateobj) + " " + user_name + " joined!");
						connected.add(user_name);
					}
					else if (command.equals("leave")){
						System.out.println(sdf.format(dateobj) + " " + user_name + " left!");
						connected.remove(user_name);
						s.close();
					}
					else if (command.equals("who")){
						System.out.print(sdf.format(dateobj) + " Connected users: ");
						for (int i=0; i<connected.size(); ++i) {
							System.out.print(connected.get(i)+ " ");
						}
					}
						
		}
				} catch (Exception e) {
					System.err.println(e);
					
				}
		
		}

	}
	
public class ChatApp {
	
	public static void main(String args[]) throws Exception {
		Scanner keyboard = new Scanner(System.in);
		System.out.print("Enter your name: ");
		String user = keyboard.nextLine();
		int port = 1234;
	//	DatagramSocket socket = new DatagramSocket(port);
	//	socket.setBroadcast(true);
		Receiver r = new Receiver(port);
		Sender s = new Sender(user, port);
		Thread rt = new Thread(r);
		Thread st = new Thread(s);
		rt.start();
		st.start();
	}

}
