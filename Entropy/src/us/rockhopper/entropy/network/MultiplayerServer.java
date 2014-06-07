package us.rockhopper.entropy.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import us.rockhopper.entropy.network.Packet.Packet0Player;
import us.rockhopper.entropy.network.Packet.Packet1Ship;
import us.rockhopper.entropy.network.Packet.Packet2InboundSize;
import us.rockhopper.entropy.network.Packet.Packet3ShipCompleted;
import us.rockhopper.entropy.network.Packet.Packet4Ready;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

/**
 * The multiplayer server. Dumb as a sack of potatoes.
 * 
 * @author Tim Clancy
 * @version 6.5.14
 * 
 */
public class MultiplayerServer extends Listener {
	private Server server;
	private HashMap<String, Boolean> players;
	private ArrayList<Packet1Ship> shipPackets;
	private ArrayList<Packet2InboundSize> shipSizePackets;
	private ArrayList<Packet3ShipCompleted> shipCompletedPackets;

	public MultiplayerServer() {
		Log.set(Log.LEVEL_DEBUG);
		server = new Server();
		this.registerPackets();
		// Server listening on its own thread.
		server.addListener(new Listener.QueuedListener(this) {
			protected void queue(Runnable runnable) {
				Gdx.app.postRunnable(runnable);
			}
		});

		try {
			server.bind(7777);
			server.start();
			System.out.println("[SERVER] Started new server.");
			players = new HashMap<String, Boolean>();
			shipPackets = new ArrayList<Packet1Ship>();
			shipSizePackets = new ArrayList<Packet2InboundSize>();
			shipCompletedPackets = new ArrayList<Packet3ShipCompleted>();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void registerPackets() {
		Kryo kryo = server.getKryo();
		Network.register(kryo);
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
		if (o instanceof Packet0Player) {
			Packet0Player player = ((Packet0Player) o);
			System.out.println("[SERVER] Player " + player.name + " received from connection " + c.getID() + ".");
			// If this is a player just entering the lobby
			if (!players.keySet().contains(player.name)) {
				players.put(player.name, false);
				server.sendToAllExceptTCP(c.getID(), player);
				System.out.println("[SERVER] Getting " + player.name + " up to speed.");
				for (String playerOtherName : players.keySet()) {
					Packet0Player playerOther = new Packet0Player();
					playerOther.name = playerOtherName;
					server.sendToTCP(c.getID(), playerOther);
					Packet4Ready readyPacket = new Packet4Ready();
					readyPacket.name = playerOtherName;
					readyPacket.ready = players.get(playerOtherName);
					server.sendToTCP(c.getID(), readyPacket);
				}
				String currentShip = "";
				int j = 0;
				for (int i = 0; i < shipSizePackets.size(); ++i) {
					Packet2InboundSize start = shipSizePackets.get(i);
					currentShip = start.shipName;
					server.sendToTCP(c.getID(), start);
					System.out.println(currentShip);
					while (j < shipPackets.size() && shipPackets.get(j).name.equals(start.name)
							&& shipPackets.get(j).shipName.equals(currentShip)) {
						server.sendToTCP(c.getID(), shipPackets.get(j));
						++j;
					}
					Packet3ShipCompleted packetComplete = shipCompletedPackets.get(i);
					server.sendToTCP(c.getID(), packetComplete);
				}
			}
		} else if (o instanceof Packet1Ship) {
			shipPackets.add((Packet1Ship) o);
			server.sendToAllTCP(o);
		} else if (o instanceof Packet2InboundSize) {
			Packet2InboundSize packet = ((Packet2InboundSize) o);
			System.out.println("[SERVER] Will try processing ship of size " + packet.size + " from " + packet.name);
			shipSizePackets.add((Packet2InboundSize) o);
			// // TODO ideally this would check for duplicate packets, and if they are found, then the sending would be
			server.sendToAllTCP(o);
		} else if (o instanceof Packet3ShipCompleted) {
			System.out.println("[SERVER] " + ((Packet3ShipCompleted) o).name + " indicates that their ship sending is "
					+ ((Packet3ShipCompleted) o).signal);
			shipCompletedPackets.add((Packet3ShipCompleted) o);
			server.sendToAllTCP(o);
		} else if (o instanceof Packet4Ready) {
			Packet4Ready packet = (Packet4Ready) o;
			System.out.println("[SERVER] " + packet.name + " is ready " + packet.ready);
			players.put(packet.name, packet.ready);
			server.sendToAllExceptTCP(c.getID(), o);
		}
	}
}
