package chatroom.message;

import chatroom.server.Client;

public class MessageText extends Message {
	private String name;
	private String target;
	private String message;

	public MessageText() {} // Required by Jackson
	public MessageText(String name, String target, String message) {
		this.name = name;
		this.target = target;
		this.message = message;
	}
	
	/**
	 * This message type does no processing at all
	 */
	@Override
	public void process(Client client) {
	}
}
