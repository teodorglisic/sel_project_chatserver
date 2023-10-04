module ch.fhnw.richards.chatserver {
    requires java.logging;
    requires jdk.httpserver;
    requires org.json;
    exports chatroom.server;
}