package chatroom.server.handlers;

import org.json.JSONObject;

/**
 * Package-private, so we do not bother with getters and setters. Only handlers use this class,
 * and they must know what they are doing.
 */
class HandlerResponse {
    int statusCode = 200; // Optimistic default
    JSONObject jsonOut = new JSONObject();
}