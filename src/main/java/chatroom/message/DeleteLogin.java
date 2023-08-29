package chatroom.message;

import chatroom.server.Account;
import chatroom.server.Client;

public class DeleteLogin extends Message {
	private String token;

	@Override
	public void process(Client client) {
		boolean result = false;
		if (client.getToken().equals(token)) {
			Account.remove(client.getAccount());
			client.setToken(null);
			client.setAccount(null);
			result = true;
		}
		client.send(new Result(this.getClass(), result));
	}
}
