package chatroom.message;

import chatroom.server.Client;

public class MessageError extends Message {
	private String errorMessage = "Invalid command";
	/**
	 * This message type does no processing at all
	 */
	@Override
	public void process(Client client) {
	}
}
