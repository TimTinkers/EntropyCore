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

			if (!players.contains(player)) {
				players.add(player);
				server.sendToAllExceptTCP(c.getID(), player);
				for (Packet0Player playerOther : players) {
					server.sendToTCP(c.getID(), playerOther);
				}
				for (Packet1Ship shipData : shipPackets) {
					server.sendToTCP(c.getID(), shipData);
				}
			}

			// TODO rework so that old players only get new players, and new players get a list of all players

		} else if (o instanceof Packet1Ship) {
			shipPackets.add((Packet1Ship) o);
			server.sendToAllTCP(o);
		} else if (o instanceof Packet2InboundSize) {
			System.out.println("[SERVER] Will try processing ship of size " + ((Packet2InboundSize) o).size + " from "
					+ ((Packet2InboundSize) o).name);
			server.sendToAllTCP(o);
		} else if (o instanceof Packet3ShipCompleted) {
			System.out.println("[SERVER] " + ((Packet3ShipCompleted) o).name + " indicates that their ship sending is "
					+ ((Packet3ShipCompleted) o).signal);
			server.sendToAllTCP(o);
		}
	}
}
