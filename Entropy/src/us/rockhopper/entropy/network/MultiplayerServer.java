package us.rockhopper.entropy.network;

import java.io.IOException;
import java.util.ArrayList;

import us.rockhopper.entropy.network.Packet.Packet0Player;
import us.rockhopper.entropy.network.Packet.Packet1Ship;
import us.rockhopper.entropy.network.Packet.Packet2InboundSize;
import us.rockhopper.entropy.network.Packet.Packet3ShipCompleted;

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
	private ArrayList<Packet0Player> players;
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
			players = new ArrayList<Packet0Player>();
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
			if (!players.contains(player)) {
				players.add(player);
				server.sendToAllExceptTCP(c.getID(), player);
				System.out.println("[SERVER] Getting " + player.name + " up to speed.");
				for (Packet0Player playerOther : players) {
					server.sendToTCP(c.getID(), playerOther);
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
						// System.out.println(shipPackets.get(j).ship);
						++j;
					}
					Packet3ShipCompleted packetComplete = shipCompletedPackets.get(i);
					server.sendToTCP(c.getID(), packetComplete);
				}
			}
		} else if (o instanceof Packet1Ship) {
			// Packet1Ship packet = (Packet1Ship) o;
			// if(packet.name)
			shipPackets.add((Packet1Ship) o);
			server.sendToAllTCP(o);
		} else if (o instanceof Packet2InboundSize) {
			Packet2InboundSize packet = ((Packet2InboundSize) o);
			System.out.println("[SERVER] Will try processing ship of size " + packet.size + " from " + packet.name);
			shipSizePackets.add((Packet2InboundSize) o);
			// // TODO ideally this would check for duplicate packets, and if they are found, then the sending would be
			// // cancelled--including the sending of the following ship packets and shipCompleted packet.
			// for (int i = 0; i < shipSizePackets.size(); ++i) {
			// Packet2InboundSize startPacket = shipSizePackets.get(i);
			// // If we are receiving a new ship from a player, replace the header on record
			// if (packet.name.equals(startPacket.name)) {
			// shipSizePackets.set(i, packet);
			// // Remove the player's previous ship from record.
			// for (Packet1Ship shipPacket : shipPackets) {
			// if (shipPacket.name.equals(packet.name)) {
			// shipPackets.remove(shipPacket);
			// }
			// }
			// // Remove the marker for the shipEnd from record, as a new one will be sent.
			// for (int j = 0; j < shipCompletedPackets.size(); ++j) {
			// Packet3ShipCompleted endPacket = shipCompletedPackets.get(j);
			// if (endPacket.name.equals(packet.name)) {
			// shipCompletedPackets.remove(endPacket);
			// break;
			// }
			// }
			// break;
			// }
			// }
			// Packet4Continue continuePacket = new Packet4Continue();
			// continuePacket.flag = true;
			// server.sendToTCP(c.getID(), continuePacket);
			server.sendToAllTCP(o);
		} else if (o instanceof Packet3ShipCompleted) {
			System.out.println("[SERVER] " + ((Packet3ShipCompleted) o).name + " indicates that their ship sending is "
					+ ((Packet3ShipCompleted) o).signal);
			shipCompletedPackets.add((Packet3ShipCompleted) o);
			server.sendToAllTCP(o);
		}
	}
}
