# Chat Server

This project is an implementation of a simple chat server, using a RESTful protocol with JSON. It should have companion
repositories for compatible clients.

The repository is currently private, because the students have the job of implementing a compatible server and/or
compatible clients. And we don't just want to see our code with variables renamed...

## Documentation

Documentation is included in the repository, in LibreOffice format.

## Building the server

IntelliJ:
- In "Project Structure" create a new artifact ("+")
- Select type "JAR" and then "From module with dependencies"
- Ensure the Directory for the manifest is <code>/src/main/resources</code>
- Now select the "Build" menu and choose "Build artifacts"
- This should create a runnable JAR file in the <code>/out</code> project directory

## Running the server

The server runs as a standalong Java application. By default, it uses port 50001.
To run the server from the command line, enter: <code>java -jar chat-server.jar</code>

To run the server as a permanent service <code>xyz</code>
- Create a new account that will run the service: <code>adduser xyz</code>
- Copy the JAR file to <code>/home/xyz</code> and change the ownership to <code>xyz:xyz</code>
- Create a new service script for the service in <code>/usr/local/bin</code>
- Create a new systemd service definition in: <code>/etc/systemd/system/xyz.service</code>
- Tell systemd about this service by running: <code>systemctl enable xyz</code>
- Now you can use: <code>service xyz start/stop/status/restart</code>

## License

This is open source software, licensed under the BSD 3-clause license.
