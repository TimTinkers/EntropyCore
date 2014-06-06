package us.rockhopper.entropy.screen;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import us.rockhopper.entropy.entities.Ship;
import us.rockhopper.entropy.network.MultiplayerClient;
import us.rockhopper.entropy.network.Packet.Packet0Player;
import us.rockhopper.entropy.network.Packet.Packet1Ship;
import us.rockhopper.entropy.network.Packet.Packet2InboundSize;
import us.rockhopper.entropy.network.Packet.Packet3ShipCompleted;
import us.rockhopper.entropy.utility.FileIO;
import us.rockhopper.entropy.utility.Part;
import us.rockhopper.entropy.utility.PartClassAdapter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.google.gson.GsonBuilder;

public class DuelLobby extends ScreenAdapter {
	private Stage stage;
	private Table table;
	private Skin skin;

	private MultiplayerClient client;

	private ArrayList<String> playerNames;
	private HashMap<String, Ship> allShips;
	private String clientShipName = "";
	private String clientPlayerName;

	DuelLobby(MultiplayerClient client) {
		allShips = new HashMap<String, Ship>();
		playerNames = new ArrayList<String>();
		this.client = client;
		clientPlayerName = client.getUser().getName();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();

		Table.drawDebug(stage);
	}

	public void updateTable() {
		for (String name : playerNames) {
			if (table.findActor(name) == null) {
				Label playerEntry = new Label(name, skin);
				Label shipEntry = new Label("Hasn't Chosen", skin);
				playerEntry.setName(name);
				shipEntry.setName(name + "ship");
				table.add(playerEntry);
				table.add(shipEntry).row();
			}
		}
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
		table.invalidateHierarchy();
	}

	@Override
	public void show() {
		skin = new Skin(Gdx.files.internal("assets/ui/uiskin.json"), new TextureAtlas("assets/ui/uiskin.pack"));
		stage = new Stage();
		table = new Table(skin);
		table.setFillParent(true);
		table.debug();

		Gdx.input.setInputProcessor(stage);

		TextButton selectShip = new TextButton("Select a Ship", skin, "default");
		TextButton closeServer = new TextButton("Shutdown Server", skin, "default");

		ClickListener shipSelectListener = new ClickListener() {
			ArrayList<Ship> clientShips = new ArrayList<Ship>();

			@Override
			public void clicked(InputEvent event, float x, float y) {
				clientShips.clear();
				// Load all ships into this list.
				String shipPath = "data/ships/";
				for (File file : FileIO.getFilesForFolder(new File(shipPath))) {
					String shipJSON = FileIO.read(file.getAbsolutePath());
					GsonBuilder gson = new GsonBuilder();
					gson.registerTypeAdapter(Part.class, new PartClassAdapter());
					Ship ship = gson.create().fromJson(shipJSON, Ship.class);
					clientShips.add(ship);
				}
				new Dialog("", skin) {
					ClickListener shipChosen = new ClickListener() {
						@Override
						public void clicked(final InputEvent event, float x, float y) {
							// Prevent spamming ship requests
							if (!clientShipName.equals(event.getListenerActor().getName())) {
								clientShipName = event.getListenerActor().getName();
								client.sendShip(FileIO.read("data/ships/" + clientShipName + ".json"),
										clientPlayerName, clientShipName);
								// ((Label) table.findActor(clientPlayerName + "ship")).setText(clientShipName);
							}
						}
					};

					{
						this.getContentTable().defaults().fillX();
						text("Which ship would you like to fight in?\n");
						for (Ship ship : clientShips) {
							this.getContentTable().row();
							TextButton button = new TextButton(ship.getName(), skin);
							button.setName(ship.getName());
							button.addListener(shipChosen);
							this.getContentTable().add(button);
						}
						button("Confirm");
					}
				}.show(stage);
			}
		};
		ClickListener serverCloseListener = new ClickListener() {
			@Override
			public void clicked(final InputEvent event, float x, float y) {
				// TODO close server. Also close clients when they disconnect from server
			}
		};

		selectShip.addListener(shipSelectListener);
		closeServer.addListener(serverCloseListener);
		table.defaults().fillX();
		table.add(selectShip);
		table.add(closeServer);
		stage.addActor(table);

		// Client listeners
		this.client.addListener(new Listener() {
			HashMap<String, String> shipStrings = new HashMap<String, String>();

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
				if (o instanceof Packet0Player) {
					// Add player labels to list upon them joining
					Packet0Player player = (Packet0Player) o;
					System.out.println("[CLIENT] " + clientPlayerName + " received information from " + player.name);
					if (!playerNames.contains(player.name)) {
						playerNames.add(player.name);
					}
					updateTable();
				} else if (o instanceof Packet1Ship) {
					// Grab ship information
					Packet1Ship packet = ((Packet1Ship) o);
					String shipJSON = shipStrings.get(packet.name);
					shipJSON += packet.ship;
					shipStrings.put(packet.name, shipJSON);
					System.out.println(packet.name + " " + shipJSON);

				} else if (o instanceof Packet2InboundSize) {
					// TODO I don't think we even care about the size of the ship string...
					Packet2InboundSize packetSize = ((Packet2InboundSize) o);
					System.out.println("[CLIENT] Attempting to read ship of size " + packetSize.size + " from "
							+ packetSize.name);
					shipStrings.put(packetSize.name, "");
				} else if (o instanceof Packet3ShipCompleted) {
					Packet3ShipCompleted packetComplete = ((Packet3ShipCompleted) o);

					String playerName = packetComplete.name;
					GsonBuilder gson = new GsonBuilder();
					gson.registerTypeAdapter(Part.class, new PartClassAdapter());
					System.out.println(shipStrings.get(playerName));
					Ship ship = gson.create().fromJson(shipStrings.get(playerName), Ship.class);
					System.out.println("[CLIENT] Received entire " + ship.getName() + " from " + playerName);
					allShips.put(playerName, ship);

					// Update ship labels
					((Label) table.findActor(playerName + "ship")).setText(ship.getName() + " Cost: " + ship.getCost());

				}
				// else if (o instanceof Packet4Continue) {
				// client.setCleared(((Packet4Continue) o).flag);
				// }
			}
		});

		client.sendPlayer(clientPlayerName);
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void dispose() {
		stage.dispose();
		skin.dispose();
	}
}
