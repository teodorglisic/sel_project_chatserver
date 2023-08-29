module ch.fhnw.richards.chatserver {
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires java.logging;
    exports chatroom.server;
    exports chatroom.testClient;
}