package chatroom.server;

import chatroom.server.handlers.*;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Server {
	private static final Logger logger = Logger.getLogger("");
	private static int port = 50001;
	
	public static void main(String[] args) {
		// Setup logging, including a file handler
		setupLogging();
		
		try {
			// Read command-line parameter, if present
			if (args.length > 0) {
				logger.info("Process command-line parameter");
				int intValue = Integer.parseInt(args[0]);
				if (intValue > 0 && intValue < 65536) port = intValue;
			}
			logger.info("Port is " + port);

			// Start the clean-up thread: periodically delete accounts and chatrooms
			CleanupThread ct = new CleanupThread();
			ct.start();

			// Create the server and all valid mappings
			HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
			server.createContext("/ping", new PingHandler()); // ping with (POST) and without (GET) a token
			server.createContext("/chat", new ChatHandler()); // send and receive messages
			server.createContext("/user", new UserHandler()); // user online

			// If desired, use multiple threads for processing (here, with 4 threads)
			server.setExecutor(Executors.newFixedThreadPool(4));

			// Start the server
			server.start();
		} catch (IOException e) {
			logger.info(e.toString());
		}
	}
	
	private static void setupLogging() {
		logger.setLevel(Level.FINE);
		logger.getHandlers()[0].setLevel(Level.INFO); // Standard (console) handler
		try {
			FileHandler fh = new FileHandler("%h/ChatroomServer_%u_%g.log", 10000000, 2);
			fh.setFormatter(new SimpleFormatter());
			fh.setLevel(Level.FINE);
			logger.addHandler(fh);
		} catch (Exception e) {
			logger.severe("Unable to create file handler for logging: " + e);
			throw new RuntimeException("Unable to initialize log files: " + e);
		}
	}
}
