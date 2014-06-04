package us.rockhopper.entropy.network;

import java.io.IOException;

import us.rockhopper.entropy.network.Packet.Packet0Chat;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

public class MultiplayerServer {
	private Server server;

	public MultiplayerServer() {
		Log.set(Log.LEVEL_DEBUG);
		server = new Server();
		this.registerPackets();
		NetworkListener nl = new NetworkListener();
		nl.initialize(server);
		server.addListener(nl);
		try {
			server.bind(7777);
			server.start();
			System.out.println("[SERVER] Started new server.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void registerPackets() {
		Kryo kryo = server.getKryo();
		Network.register(kryo);
	}
}
