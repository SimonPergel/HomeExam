package src.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import src.io.interfaces.IOutputService;

/** Simple println-based output over a Socket. */
public class SocketOutput implements IOutputService {
	private final PrintWriter out;

	public SocketOutput(Socket socket) throws IOException {
		this.out = new PrintWriter(socket.getOutputStream(), true);
	}

	@Override
	public void println(String s) { out.println(s); }
}
