package us.rockhopper.entropy.network;

import us.rockhopper.entropy.network.Packet.Packet0Chat;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class NetworkClient extends Listener {
	private Client client;
	private String message;

	public void initialize(Client client) {
		this.client = client;
	}

	@Override
	public void connected(Connection arg0) {
		System.out.println("[CLIENT] You connected.");
	}

	@Override
	public void disconnected(Connection arg0) {
		System.out.println("[CLIENT] You disconnected.");
	}

	@Override
	public void received(Connection c, Object o) {
		if (o instanceof Packet0Chat) {
			String message = ((Packet0Chat) o).message;
			System.out.println("You received the message " + message);
			this.message = message;
		}
	}

	public String getLine() {
		return this.message;
	}
}