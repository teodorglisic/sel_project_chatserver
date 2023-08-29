package chatroom.message;

import chatroom.server.Client;

public class Result extends Message {
	private String msgClass;
	private boolean result;

	public Result() {} // Required by Jackson

	/**
	 * This constructor is used by most messages
	 */
	public Result(Class<?> msgClass, boolean result) {
		this.msgClass = msgClass.getSimpleName();
		this.result = result;
	}
	
	/**
	 * This message type does no processing at all
	 */
	@Override
	public void process(Client client) {
	}
}
