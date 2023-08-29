package chatroom.message;

import chatroom.server.Client;

public class Ping extends Message {
	/**
	 * The data may optionally contain a token
	 */
	private String token = null;

	/**
	 * - If no token is present, we answer with success
	 * - If a token is present, we answer with success if the token matches this client
	 */
	@Override
	public void process(Client client) {
		boolean result = (token == null || token.equals(client.getToken()));
		client.send(new Result(this.getClass(), result));
	}

}
