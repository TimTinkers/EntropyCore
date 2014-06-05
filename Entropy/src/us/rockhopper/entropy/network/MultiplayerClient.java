package us.rockhopper.entropy.network;

import java.io.IOException;

import us.rockhopper.entropy.network.Packet.Packet0Player;
import us.rockhopper.entropy.network.Packet.Packet1Ship;
import us.rockhopper.entropy.utility.Account;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

public class MultiplayerClient {
	private Client client;
	private Account user;

	public MultiplayerClient(Account user, String ip) {
		Log.set(Log.LEVEL_DEBUG);
		this.user = user;
		client = new Client();
		this.registerPackets();
		// Client listening on its own thread.
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
		Network.register(kryo);
	}

	public void addListener(Listener listener) {
		client.addListener(new Listener.QueuedListener(listener) {
			protected void queue(Runnable runnable) {
				Gdx.app.postRunnable(runnable);
			}
		});
	}

	public void sendPlayer(String name) {
		Packet0Player packet = new Packet0Player();
		packet.name = name;
		client.sendTCP(packet);
	}

	public void sendShip(String shipJSON, String name) {
		Packet1Ship packet = new Packet1Ship();
		packet.ship = shipJSON;
		packet.name = name;
		client.sendTCP(packet);
		System.out.println("Sent");
	}

	public Account getUser() {
		return this.user;
	}
}
