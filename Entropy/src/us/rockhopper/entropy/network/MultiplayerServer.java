package us.rockhopper.entropy.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import us.rockhopper.entropy.entities.Ship;
import us.rockhopper.entropy.network.Packet.Packet0Player;
import us.rockhopper.entropy.network.Packet.Packet1Ship;
import us.rockhopper.entropy.network.Packet.Packet2InboundSize;
import us.rockhopper.entropy.network.Packet.Packet3ShipCompleted;
import us.rockhopper.entropy.network.Packet.Packet4Ready;
import us.rockhopper.entropy.network.Packet.Packet5GameStart;
import us.rockhopper.entropy.network.Packet.Packet6Key;
import us.rockhopper.entropy.network.Packet.Packet7PositionUpdate;
import us.rockhopper.entropy.network.Packet.Packet8DuelStart;
import us.rockhopper.entropy.utility.Part;
import us.rockhopper.entropy.utility.PartClassAdapter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.google.gson.GsonBuilder;

/**
 * The multiplayer server. Dumb as a sack of potatoes.
 * 
 * @author Tim Clancy
 * @version 6.5.14
 * 
 */
public class MultiplayerServer extends Listener {
	private Server server;
	private World world;
	private HashMap<String, Boolean> players;
	private ArrayList<Packet1Ship> shipPackets;
	private ArrayList<Packet2InboundSize> shipSizePackets;
	private ArrayList<Packet3ShipCompleted> shipCompletedPackets;
	HashMap<String, String> shipStrings = new HashMap<String, String>();
	protected ConcurrentLinkedQueue<Packet6Key> clientMessageQueue;
	private HashMap<String, Ship> allShips;

	private final float TIMESTEP = 1 / 60f;
	private final int VELOCITYITERATIONS = 8, POSITIONITERATIONS = 3;

	private boolean gameLogic;

	public MultiplayerServer(String port) {
		Log.set(Log.LEVEL_DEBUG);
		server = new Server();
		world = new World(new Vector2(0, 0), true);
		this.clientMessageQueue = new ConcurrentLinkedQueue<Packet6Key>();
		allShips = new HashMap<String, Ship>();
		this.registerPackets();

		// Server listening on its own thread.
		server.addListener(new Listener.QueuedListener(this) {
			protected void queue(Runnable runnable) {
				Gdx.app.postRunnable(runnable);
			}
		});

		try {
			server.bind(Integer.parseInt(port));

			// TODO wtf is with all these threads this can probably be fixed
			// Start the server listening for input
			// TODO make server keep running when it loses focus
			new Thread(server).start();

			// Start the server gameloop
			Timer t = new Timer();
			t.schedule(new TimerTask() {
				int step = 0;

				Runnable runnable = new Runnable() {

					@Override
					public void run() {
						++step;
						processKeyPresses();
						updateShips();
						if (step == 2) {
							step = 0;
							shipToClient();
						}
					}
				};

				@Override
				public void run() {
					if (gameLogic) {
						Gdx.app.postRunnable(runnable);
					}
				}
			}, 0L, 16);

			System.out.println("[SERVER] Started new server.");
			players = new HashMap<String, Boolean>();
			shipPackets = new ArrayList<Packet1Ship>();
			shipSizePackets = new ArrayList<Packet2InboundSize>();
			shipCompletedPackets = new ArrayList<Packet3ShipCompleted>();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void shipToClient() {
		for (String playerName : allShips.keySet()) {
			Ship ship = allShips.get(playerName);
			for (Part part : ship.getParts()) {

				Packet7PositionUpdate updatePacket = new Packet7PositionUpdate();
				updatePacket.name = playerName;
				updatePacket.partNumber = part.getNumber();
				updatePacket.x = part.getBody().getPosition().x;
				updatePacket.y = part.getBody().getPosition().y;
				updatePacket.angle = part.getBody().getAngle();

				// TODO create the render-update packet?
				server.sendToAllTCP(updatePacket);
			}
		}
	}

	public void updateShips() {
		for (String playerName : allShips.keySet()) {
			Ship ship = allShips.get(playerName);
			ship.update();
		}
		world.step(TIMESTEP, VELOCITYITERATIONS, POSITIONITERATIONS);
	}

	public void processKeyPresses() {
		Packet6Key msg;
		while ((msg = clientMessageQueue.poll()) != null) {
			System.out.println("[SERVER] Sending " + msg.keyPress + " from " + msg.name);

			Ship keyedShip = allShips.get(msg.name);
			System.out.println("[CLIENT] Received key from " + msg.name + " for ship " + keyedShip.getName()
					+ " at time " + System.nanoTime());
			for (Part part : keyedShip.getParts()) {
				if (msg.isDown) {
					part.trigger(msg.keyPress);
				} else {
					part.unTrigger(msg.keyPress);
				}
			}
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
			Packet1Ship packet = ((Packet1Ship) o);
			shipPackets.add(packet);

			// Grab ship information
			String shipJSON = shipStrings.get(packet.name);
			shipJSON += packet.ship;
			shipStrings.put(packet.name, shipJSON);

			server.sendToAllTCP(o);
		} else if (o instanceof Packet2InboundSize) {
			Packet2InboundSize packet = ((Packet2InboundSize) o);
			shipStrings.put(packet.name, "");
			System.out.println("[SERVER] Will try processing ship of size " + packet.size + " from " + packet.name);
			shipSizePackets.add((Packet2InboundSize) o);
			// // TODO ideally this would check for duplicate packets, and if they are found, then the sending would be
			server.sendToAllTCP(o);
		} else if (o instanceof Packet3ShipCompleted) {
			Packet3ShipCompleted packetComplete = ((Packet3ShipCompleted) o);
			System.out.println("[SERVER] " + ((Packet3ShipCompleted) o).name + " indicates that their ship sending is "
					+ ((Packet3ShipCompleted) o).signal);
			shipCompletedPackets.add((Packet3ShipCompleted) o);

			// Server-side initialization of ship list
			String playerName = packetComplete.name;
			GsonBuilder gson = new GsonBuilder();
			gson.registerTypeAdapter(Part.class, new PartClassAdapter());
			System.out.println(shipStrings.get(playerName));
			Ship ship = gson.create().fromJson(shipStrings.get(playerName), Ship.class);
			System.out.println("[CLIENT] Received entire " + ship.getName() + " from " + playerName);
			allShips.put(playerName, ship);

			server.sendToAllTCP(o);
		} else if (o instanceof Packet4Ready) {
			Packet4Ready packet = (Packet4Ready) o;
			System.out.println("[SERVER] " + packet.name + " is ready " + packet.ready);
			players.put(packet.name, packet.ready);
			server.sendToAllExceptTCP(c.getID(), o);

			// If every player is ready
			boolean ready = true;
			for (String playerName : players.keySet()) {
				if (!players.get(playerName)) {
					ready = false;
				}
			}
			if (ready) {
				server.sendToAllTCP(new Packet5GameStart());
			}
		} else if (o instanceof Packet6Key) {
			// server.sendToAllExceptTCP(c.getID(), o);
			this.clientMessageQueue.add((Packet6Key) o);
		} else if (o instanceof Packet8DuelStart) {
			// create all ships
			for (String playerName : allShips.keySet()) {
				Ship ship = allShips.get(playerName);
				ship.setWorld(world);
				ship.create();
			}
			gameLogic = true;
		}
	}
}
