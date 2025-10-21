package src.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import src.io.interfaces.IInputService;

/** Simple line-based input over a Socket. */
public class SocketInput implements IInputService {
	private final BufferedReader reader;

	public SocketInput(Socket socket) throws IOException {
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	@Override
	public String readLine() {
		try {
			return reader.readLine();
		} catch (IOException e) {
			return null;
		}
	}
}
