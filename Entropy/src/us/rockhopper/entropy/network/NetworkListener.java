package us.rockhopper.entropy.network;

import us.rockhopper.entropy.network.Packet.Packet0Chat;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class NetworkListener extends Listener {
	Server server;

	public void initialize(Server server) {
		this.server = server;
	}

	@Override
	public void connected(Connection c) {
		System.out.println("[SERVER] " + c.getID() + " connected.");
	}

	@Override
	public void disconnected(Connection c) {
		System.out.println("[SERVER] " + c.getID() + " disconnected.");
	}

	@Override
	public void received(Connection c, Object o) {
		if (o instanceof Packet0Chat) {
			System.out.println("[SERVER] Message received from " + c.getID() + ".");
			String message = ((Packet0Chat) o).message;
			System.out.println(message);
			Packet0Chat packet = new Packet0Chat();
			packet.message = message;
			server.sendToAllExceptTCP(c.getID(), packet);
		}
	}
}
