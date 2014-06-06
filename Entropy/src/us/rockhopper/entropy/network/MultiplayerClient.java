package us.rockhopper.entropy.network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import us.rockhopper.entropy.network.Packet.Packet0Player;
import us.rockhopper.entropy.network.Packet.Packet1Ship;
import us.rockhopper.entropy.network.Packet.Packet2InboundSize;
import us.rockhopper.entropy.network.Packet.Packet3ShipCompleted;
import us.rockhopper.entropy.utility.Account;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import com.google.common.primitives.Bytes;

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
		try {
			byte[] data = shipJSON.getBytes("UTF-8");
			List<Byte> originalList = Bytes.asList(data);

			// Tell server how large the incoming String is
			int dataSize = data.length;
			System.out.println("[CLIENT] Sending ship string of size " + dataSize);
			Packet2InboundSize packetSize = new Packet2InboundSize();
			packetSize.size = dataSize;
			packetSize.name = name;
			client.sendTCP(packetSize);

			// Partition the out-bound String
			int partitionSize = 256;
			for (int i = 0; i < originalList.size(); i += partitionSize) {
				List<Byte> subList = originalList.subList(i, i + Math.min(partitionSize, originalList.size() - i));
				byte[] byteArray = Bytes.toArray(subList);
				String shipPiece = new String(byteArray);
				Packet1Ship packet = new Packet1Ship();
				packet.name = name;
				packet.ship = shipPiece;
				client.sendTCP(packet);
			}

			// TODO add check here to see if the ship was actually entirely sent
			// Notify other clients that the ship was completely sent.
			Packet3ShipCompleted packetComplete = new Packet3ShipCompleted();
			packetComplete.name = name;
			packetComplete.signal = true;
			client.sendTCP(packetComplete);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public Account getUser() {
		return this.user;
	}
}
