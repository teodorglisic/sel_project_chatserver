package chatroom.message;

import chatroom.server.Client;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Logger;

public abstract class Message {
	private static final Logger logger = Logger.getLogger("");

	/**
	 * Perform whatever actions are required for this particular type of message.
	 */
	public abstract void process(Client client);

	public void send(Socket socket) throws IOException {
		// Type validator for this class hierarchy
		PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
				.allowIfBaseType(Message.class)
				.build();

		// ObjectMapper for these classes
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
		// Don't require public getters and setters. To allow reflection access to
		// private members, we also have to "open chatroom.message to com.fasterxml.jackson.databind"
		objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
		String json = objectMapper.writeValueAsString(this);
		logger.info("JSON to write: " + json);

		// Write the object
		OutputStreamWriter out;
		out = new OutputStreamWriter(socket.getOutputStream());
		out.write(json + "\n");
		out.flush();
	}

	public static Message receive(Socket socket) {
		BufferedReader in;
		Message msg = null;
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String msgText = in.readLine(); // Will wait here for complete line
			logger.info("Receiving message: " + msgText);

			// Type validator for this class hierarchy
			PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
					.allowIfBaseType(Message.class)
					.build();

			// ObjectMapper for these classes
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
			// Don't require public getters and setters. To allow reflection access to
			// private members, we also have to "open chatroom.server.message to com.fasterxml.jackson.databind"
			objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
			msg = objectMapper.readValue(msgText, Message.class);
		} catch (IOException e) {
			logger.warning(e.toString());
		}
		return msg;
	}
}
