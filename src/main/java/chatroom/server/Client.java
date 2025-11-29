package chatroom.server;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class represents a client, from the perspective of the server. A client
 * is a user with a currently valid token. We record here the username, the token
 * and an array of messages that need to be sent to the user.
 */
public class Client {
	private static final Logger logger = Logger.getLogger("");
	private static final ArrayList<Client> clients = new ArrayList<>();

	private final String username;
	private final String token;
	private List<Message> messages = new ArrayList<>();
	private Instant lastUsage = Instant.now();


	/**
	 * Add a new client to our list of active clients.
	 */
	public static void add(String username, String token) {
		synchronized (clients) {
			clients.add(new Client(username, token));
		}
	}

    /**
     *
     * @param username
     * @param token
     * @param cachedMessages
     *
     * Method to add a cached client using the second constructor
     */

    public static void addCachedClient(String username, String token, List<Message> cachedMessages){
        synchronized (clients) {
            clients.add(new Client(username, token, cachedMessages));
        }
    }

	/**
	 * Remove a client (e.g., when they logout)
	 */
	public static void remove(String token) {
		synchronized (clients) {
			clients.removeIf(c -> c.token.equals(token));
		}
	}

	/**
	 * Returns a client, found by username
	 */
	public static Client findByUsername(String username) {
		synchronized (clients) {
			for (Client c : clients) {
				if (c.username.equals(username)) return c;
			}
		}
		return null;
	}

	/**
	 * Returns a client, found by token
	 */
	public static Client findByToken(String token) {
		synchronized (clients) {
			for (Client c : clients) {
				if (c.token.equals(token)) return c;
			}
		}
		return null;
	}

	/**
	 * Clean up old clients -- called by cleanup thread
	 */
	public static void cleanupClients() {
		synchronized (clients) {
			Instant expiry = Instant.now().minusSeconds(3600); // Expiry one hour
			logger.fine("Cleanup clients: " + clients.size() + " clients registered");
			clients.removeIf( c -> c.lastUsage.isBefore(expiry));
			logger.fine("Cleanup clients: " + clients.size() + " clients registered");
		}
	}

	/**
	 * Return a list of all clients
	 */
	public static List<String> listClients() {
		return clients.stream().map( c -> c.username ).collect(Collectors.toList());
	}

	/**
	 * Create a new client object, communicating over the given socket. Immediately
	 * start a thread to receive messages from the client.
	 */
	public Client(String username, String token) {
		this.username = username;
		this.token = token;
	}

    /**
     * This constructor is for the accounts, that logged in again and created a new client session
     * @param username
     * @param token
     * @param cachedMessages
     */

    public Client(String username, String token, List<Message> cachedMessages) {
        this.username = username;
        this.token = token;
        this.messages = cachedMessages;
    }

	public String getName() {
		return username;
	}

	public String getToken() {
		return token;
	}

	public Instant getLastUsage() {
		return lastUsage;
	}

	// Called when the client takes an action
	private void updateLastUsage() {
		this.lastUsage = Instant.now();
	}

	/**
	 * Send a message to this client.
	 */
	public void send(String username, String message) {
		synchronized (messages) {
			messages.add(new Message(username, message, null));
		}
	}

	/**
	 * Retrieve messages for this client
	 */
	public JSONArray getMessages() {
		JSONArray jsonMessages = new JSONArray();
		synchronized (messages) {
			for (Message msg : messages) {
				JSONObject jsonMsg = (new JSONObject())
						.put("username", msg.username)
						.put("message", msg.message);
				if (msg.chatroom() != null){
					jsonMsg.put("chatroom", msg.chatroom());
				}
				jsonMessages.put(jsonMsg);
			}
		}
		updateLastUsage();
		return jsonMessages;
	}

	public void sendChatroom(String name, String message, String chatroomName) {
		synchronized (messages) {
			messages.add(new Message(name, message, chatroomName));
		}
	}

    public void clearMessages() {
        messages.clear();
    }

    public List<Message> getMessagesForCache() {
        return this.messages;
    }



    // Messages pending for this user. Chatroom is null for direct messages
	// from a user. The username is the sending user. The message is obvious.
	protected record Message(String username, String message, String chatroom) {}
}
