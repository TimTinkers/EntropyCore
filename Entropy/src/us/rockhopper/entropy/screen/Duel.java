package us.rockhopper.entropy.screen;

import java.util.HashMap;

import net.dermetfan.utils.libgdx.graphics.Box2DSprite;
import us.rockhopper.entropy.entities.Ship;
import us.rockhopper.entropy.network.MultiplayerClient;
import us.rockhopper.entropy.network.Packet.Packet6Key;
import us.rockhopper.entropy.utility.Part;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class Duel extends ScreenAdapter {

	private TiledDrawable background;
	private World world;
	private SpriteBatch batch;
	private OrthographicCamera camera;

	private final float TIMESTEP = 1 / 60f;
	private final int VELOCITYITERATIONS = 8, POSITIONITERATIONS = 3;
	private float accumulator;

	private HashMap<String, Ship> allShips;
	private MultiplayerClient client;
	private Ship ship;

	Duel(HashMap<String, Ship> ships, MultiplayerClient client) {
		allShips = ships;
		this.client = client;
		this.ship = allShips.get(client.getUser().getName());
	}

	@Override
	public void render(float delta) {
		delta = MathUtils.clamp(delta, 0, 0.030f);
		accumulator += delta;

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		while (accumulator > TIMESTEP) {
			accumulator -= TIMESTEP;
			for (String playerName : allShips.keySet()) {
				Ship ship = allShips.get(playerName);
				ship.update();
			}
			world.step(TIMESTEP, VELOCITYITERATIONS, POSITIONITERATIONS);
		}

		camera.position.y = ship.getCockpitPosition().y;
		camera.position.x = ship.getCockpitPosition().x;
		camera.update();

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		// Draw background
		background.draw(batch, -Gdx.graphics.getWidth() / 2, -Gdx.graphics.getHeight() / 2,
				Gdx.graphics.getWidth() * 8, Gdx.graphics.getHeight() * 8);
		Box2DSprite.draw(batch, world);
		batch.end();
	}

	@Override
	public void resize(int width, int height) {
		camera.viewportWidth = width / 25f;
		camera.viewportHeight = height / 25f;
	}

	@Override
	public void show() {
		background = new TiledDrawable(new TextureRegion(new Texture("assets/img/grid.png")));

		world = new World(new Vector2(0, 0), true);
		batch = new SpriteBatch();

		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		// TODO move all ships to appropriate coordinates, currently they are just their ShipEditor coords
		for (String playerName : allShips.keySet()) {
			Ship ship = allShips.get(playerName);
			ship.setWorld(world);
			ship.create();
		}
		Gdx.input.setInputProcessor(new InputMultiplexer(new InputAdapter() {

			@Override
			public boolean scrolled(int amount) {
				camera.zoom += amount / 60f;
				return true;
			}

		},
		// ship,
				new InputAdapter() {
					@Override
					public boolean keyDown(int keycode) {
						client.sendKey(keycode, true);
						System.out.println(client.getUser().getName() + " sending down " + keycode);
						return true;
					}

					@Override
					public boolean keyUp(int keycode) {
						client.sendKey(keycode, false);
						System.out.println(client.getUser().getName() + " sending up " + keycode);
						return true;
					}
				}));

		this.client.addListener(new Listener() {

			@Override
			public void connected(Connection arg0) {
				System.out.println("[CLIENT] You connected.");
			}

			@Override
			public void disconnected(Connection arg0) {
				System.out.println("[CLIENT] You disconnected.");
			}

			// TODO need to figure out a proper solution for this...the server needs to handle processing updates and
			// then sending them to the clients at the same time
			@Override
			public void received(Connection c, Object o) {
				if (o instanceof Packet6Key) {
					// If an incoming key press is detected, act on it.
					Packet6Key keyPress = (Packet6Key) o;
					Ship keyedShip = allShips.get(keyPress.name);
					System.out.println("[CLIENT] Received key from " + keyPress.name + " for ship "
							+ keyedShip.getName());
					System.out.println("the size of this list is " + keyedShip.getParts().size());
					for (Part part : keyedShip.getParts()) {
						System.out.println("Checking a part for " + keyedShip.getName());
						if (keyPress.isDown) {
							part.trigger(keyPress.keyPress);
						} else {
							part.unTrigger(keyPress.keyPress);
						}
					}
				}
			}
		});
	}
}
