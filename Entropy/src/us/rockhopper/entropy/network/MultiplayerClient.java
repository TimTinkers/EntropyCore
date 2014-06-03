package us.rockhopper.entropy.network;

import java.io.IOException;

import us.rockhopper.entropy.network.Packet.Packet0Chat;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.minlog.Log;

public class MultiplayerClient {
	private Client client;
	private NetworkClient nc;

	public MultiplayerClient(String ip) {
		Log.set(Log.LEVEL_DEBUG);
		client = new Client();
		this.registerPackets();
		nc = new NetworkClient();
		nc.initialize(client);
		client.addListener(nc);
		new Thread(client).start();
		try {
			client.connect(5000, ip, 7777);
		} catch (IOException e) {
			e.printStackTrace();
			client.stop();
		}
	}

	private void registerPackets() {
		Kryo kryo = client.getKryo();
		kryo.register(Packet0Chat.class);
	}

	public void sendMessage(String message) {
		Packet0Chat chat = new Packet0Chat();
		chat.message = message;
		client.sendTCP(chat);
	}

	public String getLine() {
		return nc.getLine();
	}
}
