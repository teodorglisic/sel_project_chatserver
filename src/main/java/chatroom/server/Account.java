package chatroom.server;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class represents a registered client, i.e., one that has defined a
 * username and password.
 * At the class level, we maintain a list of all registered clients.
 * Passwords are hashed securely, using one of the algorithms built into Java.
 * If this algorithm somehow does not exist, this is catastrophic, and we stop the
 * server.
 */
public class Account implements Serializable {
	private static final Logger logger = Logger.getLogger("");

	private static final ArrayList<Account> accounts = new ArrayList<>();
	private static final SecureRandom rand = new SecureRandom();
	private static final int iterations = 127;

	private final String username;
	private final byte[] salt = new byte[64];
	private String hashedPassword;
	private Instant lastLogin;

    private List<Client.Message> cachedMessages;

    public List<Client.Message> getCachedMessages() {
        return cachedMessages;
    }

    public void setCachedMessages(List<Client.Message> cachedMessages) {
        this.cachedMessages = cachedMessages;
    }

	/**
	 * Add a new account to our list of valid accounts
	 */
	public static void add(Account account) {
		synchronized (accounts) {
			accounts.add(account);
		}
	}

	/**
	 * Remove an account from our list of valid accounts
	 */
	public static void remove(Account account) {
		synchronized (accounts) {
			accounts.removeIf(a -> a.equals(account));
		}
	}

	/**
	 * Return a list of all registered users
	 */
	public static List<String> listAccounts() {
		return accounts.stream().map( a -> a.username ).collect(Collectors.toList());
	}

	/**
	 * Find and return an existing account
	 */
	public static Account exists(String username) {
		synchronized (accounts) {
			for (Account account : accounts) {
				if (account.username.equals(username)) return account;
			}
		}
		return null;
	}

	/**
	 * Clean up old accounts -- called by cleanup thread
	 */
	public static void cleanupAccounts() {
		synchronized (accounts) {
			Instant expiry = Instant.now().minusSeconds(3 * 86400); // 3 days
			logger.fine("Cleanup accounts: " + accounts.size() + " accounts registered");
			accounts.removeIf(a -> a.lastLogin.isBefore(expiry));
			logger.fine("Cleanup accounts: " + accounts.size() + " accounts registered");
		}
	}

	/**
	 * This method is here, because we have a secure random number generator already
	 * set up. We have a 32-character token - enough to be reasonably secure.
	 */
	public static String getToken() {
		byte[] token = new byte[16];
		rand.nextBytes(token);
		return bytesToHex(token);
	}

	public Account(String username, String password) {
		this.username = username;
		rand.nextBytes(salt);
		this.hashedPassword = hash(password);
		this.lastLogin = Instant.now();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || o.getClass() != this.getClass()) return false;
		Account ol = (Account) o;
		return ol.username.equals(this.username);
	}

	public boolean checkPassword(String password) {
		String newHash = hash(password);
		boolean success = hashedPassword.equals(newHash);
		if (success) this.lastLogin = Instant.now();
		return success;
	}

	public void changePassword(String newPassword) {
		rand.nextBytes(salt); // Change the salt with the password!
		this.hashedPassword = hash(newPassword);
	}

	public String getUsername() {
		return username;
	}

	/**
	 * There are many sources of info on how to securely hash passwords. I'm not a crypto expert,
	 * so I follow the recommendations of the experts. Here are two examples:
	 * <a href="https://crackstation.net/hashing-security.htm">...</a>
	 * <a href="https://howtodoinjava.com/security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/">...</a>
	 */
	private String hash(String password) {
		try {
			char[] chars = password.toCharArray();
			PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] hash = skf.generateSecret(spec).getEncoded();
			return bytesToHex(hash);
		} catch (Exception e) {
			logger.severe("Secure password hashing not possible - stopping server");
			System.exit(0);
			return null; // Will never execute, but keeps Java happy
		}
	}

	/**
	 * Convert byte-array to a hex string
	 * <a href="https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java">...</a>
	 */
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
}
