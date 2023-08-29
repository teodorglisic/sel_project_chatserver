package chatroom.message;

import chatroom.server.Account;
import chatroom.server.Client;

public class ChangePassword extends Message {
	private String token;
	private String password;

	@Override
	public void process(Client client) {
		boolean result = false;
		if (client.getToken().equals(token)) {
			Account account = client.getAccount();
			account.changePassword(password);
			result = true;
		}
		client.send(new Result(this.getClass(), result));
	}

}
