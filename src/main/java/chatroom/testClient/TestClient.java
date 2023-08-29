package chatroom.testClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * This is a really simple test client: It reads from the scanner, sends to the
 * server, and prints the server's response. This is all in plain-text - it is
 * up to the user to type in correctly formatted messages.
 */
public class TestClient {

	public static void main(String[] args) {
		String ipAddress = null;
		int portNumber = 0;

		try (Scanner in = new Scanner(System.in)) {
			boolean valid = false;

			// Read IP address
			while (!valid) {
				System.out.println("Enter the address of the server");
				ipAddress = in.nextLine();
				valid = validateIpAddress(ipAddress);
			}

			// Read port number
			valid = false;
			while (!valid) {
				System.out.println("Enter the port number on the server (1024-65535)");
				String strPort = in.nextLine();
				valid = validatePortNumber(strPort);
				if (valid) portNumber = Integer.parseInt(strPort);
			}

			Socket socket = null;
			try {
				socket = new Socket(ipAddress, portNumber);
					
				System.out.println("Connected");
				try (BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						OutputStreamWriter socketOut = new OutputStreamWriter(socket.getOutputStream())) {
					// Create thread to read incoming messages
					Runnable r = new Runnable() {
						@Override
						public void run() {
							while (true) {
								String msg;
								try {
									msg = socketIn.readLine();
									System.out.println("Received: " + msg);
								} catch (IOException e) {
									break;
								}
								if (msg == null) break; // In case the server closes the socket
							}
						}
					};
					Thread t = new Thread(r);
					t.start();

					// Loop, allowing the user to send messages to the server
					// Note: We still have our scanner
					System.out.println("Enter commands to server or ctrl-D to quit");
					while (in.hasNext()) {
						String line = in.nextLine();
						socketOut.write(line + "\n");
						socketOut.flush();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (socket != null) try {
					socket.close();
				} catch (IOException e) {
					// we don't care
				}
			}
		}
	}

	private static boolean validateIpAddress(String ipAddress) {
		boolean valid = false;
		String[] parts = ipAddress.split("\\.", -1);

		// check for a numeric address first: 4 parts, each an integer 0 to 255
		if (parts.length == 4) {
			valid = true;
			for (String part : parts) {
				try {
					int value = Integer.parseInt(part);
					if (value < 0 || value > 255) valid = false;
				} catch (NumberFormatException e) {
					// wasn't an integer
					valid = false;
				}
			}
		}

		// If not valid, try for a symbolic address: at least two parts, each
		// part at least two characters. We don't bother checking what kinds of
		// characters they are.
		if (!valid) {
			if (parts.length >= 2) {
				valid = true;
				for (String part : parts) {
					if (part.length() < 2) valid = false;
				}
			}
		}
		
		return valid;
	}

	private static boolean validatePortNumber(String portText) {
		boolean formatOK = false;
		try {
			int portNumber = Integer.parseInt(portText);
			if (portNumber >= 1024 & portNumber <= 65535) {
				formatOK = true;
			}
		} catch (NumberFormatException e) {
		}
		return formatOK;
	}
}
