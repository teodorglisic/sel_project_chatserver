package chatroom.message;

import chatroom.server.Chatroom;
import chatroom.server.Client;

import java.util.ArrayList;

public class ListChatroomUsers extends Message {
	private String token;
	private String name;

	/**
	 * Only allowed if the chatorom is public, or the client is a member of the
	 * chatroom
	 */
	@Override
	public void process(Client client) {
		boolean result = false;
		ArrayList<String> names = null;
		if (client.getToken().equals(token)) {
			Chatroom chatroom = Chatroom.exists(name);
			if (chatroom != null) {
				names = chatroom.getUsers();
				if (chatroom.isPublic() || names.contains(client.getName())) {
					result = true;
				}
			}
		}

		if (result) {
			client.send(new ResultWithList(this.getClass(), true, names));
		} else {
			client.send(new Result(this.getClass(), false));
		}
	}
}
