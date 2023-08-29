package chatroom.message;

import chatroom.server.Account;
import chatroom.server.Client;

/**
 * Login to an existing account. If successful, return an authentication token
 * to the client.
 */
public class Login extends Message {
	private String username;
	private String password;

	@Override
	public void process(Client client) {
		Message reply;
		// Find existing login matching the username
		Account account = Account.exists(username);
		if (account != null && account.checkPassword(password)) {
			client.setAccount(account);
			String token = Account.getToken();
			client.setToken(token);
			reply = new ResultWithToken(this.getClass(), true, token);
		} else {
			reply = new Result(this.getClass(), false);
		}
		client.send(reply);
	}
}
