package us.rockhopper.entropy.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.dermetfan.utils.libgdx.graphics.Box2DSprite;
import us.rockhopper.entropy.entities.Ship;
import us.rockhopper.entropy.network.MultiplayerClient;
import us.rockhopper.entropy.network.Packet.Packet6Key;
import us.rockhopper.entropy.network.Packet.Packet7PositionUpdate;
import us.rockhopper.entropy.utility.CollisionListener;
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
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.JointEdge;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class Duel extends ScreenAdapter {

	private TiledDrawable background;
	private World world;
	private SpriteBatch batch;
	private OrthographicCamera camera;

	private final float TIMESTEP = 1 / 66.6667f;
	private final int VELOCITYITERATIONS = 8, POSITIONITERATIONS = 3;
	private float accumulator;

	private HashMap<String, Ship> allShips;
	private MultiplayerClient client;
	private Ship ship;

	private Box2DDebugRenderer debugRenderer;

	protected ConcurrentLinkedQueue<Packet6Key> clientMessageQueue;
	protected ConcurrentLinkedQueue<Packet7PositionUpdate> positionUpdateQueue;

	Duel(HashMap<String, Ship> ships, MultiplayerClient client) {
		allShips = ships;
		this.client = client;
		this.ship = allShips.get(client.getUser().getName());
		this.ship.setClient(client);
		this.clientMessageQueue = new ConcurrentLinkedQueue<Packet6Key>();
		this.positionUpdateQueue = new ConcurrentLinkedQueue<Packet7PositionUpdate>();
	}

	@Override
	public void render(float delta) { // TODO add interpolation to the DeWitter's loop (do so in all screens)
		delta = MathUtils.clamp(delta, 0, 0.030f);
		accumulator += delta;

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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

		while (accumulator > TIMESTEP) {
			accumulator -= TIMESTEP;
			processLocalKeys();
			for (String playerName : allShips.keySet()) {
				Ship ship = allShips.get(playerName);
				ship.update();
			}
			world.step(TIMESTEP, VELOCITYITERATIONS, POSITIONITERATIONS);
			processNewPositions();
			sweepDeadBodies();
		}

		debugRenderer.render(world, camera.combined);
	}

	// TODO more effectively merge the client with the server here...this method is duplicated
	public void sweepDeadBodies() {
		Array<Body> tempBodies = new Array<Body>();
		world.getBodies(tempBodies);
		for (Body body : tempBodies) {
			if (body != null) {
				Part part = (Part) body.getUserData();
				if (part.isDead() && !world.isLocked()) {
					body.setUserData(null);
					removeBodySafely(body);
					body = null;
				}
			}
		}
	}

	public void removeBodySafely(Body body) {
		// to prevent some obscure c assertion that happened randomly once in a blue moon
		final Array<JointEdge> list = body.getJointList();
		while (list.size > 0) {
			world.destroyJoint(list.get(0).joint);
		}
		// actual remove
		world.destroyBody(body);
	}

	public void processNewPositions() {
		Packet7PositionUpdate msg;
		while ((msg = positionUpdateQueue.poll()) != null) {
			Ship keyedShip = allShips.get(msg.name);
			for (Part part : keyedShip.getParts()) {
				if (part.getNumber() == msg.partNumber) {
					part.getBody().setTransform(new Vector2(msg.x, msg.y), msg.angle);
				}
			}
		}
	}

	public void processLocalKeys() {
		Packet6Key msg;
		while ((msg = clientMessageQueue.poll()) != null) {
			Ship keyedShip = allShips.get(msg.name);
			for (Part part : keyedShip.getParts()) {
				if (msg.isDown) {
					part.trigger(msg.keyPress);
				} else {
					part.unTrigger(msg.keyPress);
				}
			}
		}
	}

	@Override
	public void resize(int width, int height) {
		camera.viewportWidth = width / 25f;
		camera.viewportHeight = height / 25f;
	}

	@Override
	public void show() {
		debugRenderer = new Box2DDebugRenderer();

		background = new TiledDrawable(new TextureRegion(new Texture("assets/img/grid.png")));

		world = new World(new Vector2(0, 0), true);
		world.setContactListener(new CollisionListener());
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
		// ship, //TODO make the ships able to allow for client-side interpolation/prediction, for now just rely on the
		// server
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

			// TODO need to figure out a proper solution for this...the server needs to handle processing updates and
			// then sending them to the clients at the same time
			@Override
			public void received(Connection c, Object o) {
				if (o instanceof Packet6Key) {
					// If an incoming key press is detected, act on it.
					Packet6Key keyPress = (Packet6Key) o;
					clientMessageQueue.add(keyPress);
				} else if (o instanceof Packet7PositionUpdate) {
					// If the server indicates that we should update the position...
					Packet7PositionUpdate packet = (Packet7PositionUpdate) o;
					positionUpdateQueue.add(packet);
				}
			}
		});

		// Tell the server to initialize its ships
		client.sendDuelStart();
	}
}
