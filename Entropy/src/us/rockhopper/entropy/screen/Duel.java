package us.rockhopper.entropy.screen;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.dermetfan.utils.libgdx.graphics.Box2DSprite;
import us.rockhopper.entropy.entities.Cockpit;
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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.JointEdge;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class Duel extends ScreenAdapter {

	Texture starfield = new Texture("assets/img/starfield.png");
	// Texture nebula = new Texture("assets/img/nebula1.png");
	private Sprite starfieldSprite = new Sprite(starfield);
	// private Sprite nebulaSprite = new Sprite(nebula);
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private SpriteBatch backgroundBatch;
	private OrthographicCamera backgroundCam;

	private Vector2 starScrollTimer = new Vector2(0, 0);
	// private Vector2 nebulaScrollTimer = new Vector2(0, 0);
	private Vector2 cockpitVelocity = new Vector2(0, 0);

	private World world;
	private final float TIMESTEP = 1 / 66.6667f;
	private final int VELOCITYITERATIONS = 8, POSITIONITERATIONS = 3;
	private float accumulator;

	private HashMap<String, Ship> allShips;
	private MultiplayerClient client;
	private Ship ship;

	private Box2DDebugRenderer debugRenderer;

	protected ConcurrentLinkedQueue<Packet6Key> clientMessageQueue;
	protected ConcurrentLinkedQueue<Packet7PositionUpdate> positionUpdateQueue;

	private ParticleEffect effect;

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

		// Draw background
		starfield.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		backgroundBatch.setProjectionMatrix(backgroundCam.combined);
		backgroundBatch.begin();
		renderBackground();
		backgroundBatch.end();

		// Draw ship parts
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		Box2DSprite.draw(batch, world);
		effect.draw(batch, delta);
		batch.end();

		// Take care of physics simulation
		while (accumulator > TIMESTEP) {
			accumulator -= TIMESTEP;
			processLocalKeys();
			for (String playerName : allShips.keySet()) {
				Ship ship = allShips.get(playerName);
				ship.update();
				// System.out.println(ship.getParts().get(0) + " " +
				// ship.getParts().get(0).getBody().getLinearVelocity()
				// + " " + ship.getParts().get(0).getBody().getPosition());
			}
			world.step(TIMESTEP, VELOCITYITERATIONS, POSITIONITERATIONS);
			processNewPositions();

			// Get the linear velocity of the cockpit
			// cockpitVelocity = ship.getParts().get(0).getBody().getLinearVelocity();
			// System.out.println(cockpitVelocity);

			// Remove bodies that need deletion
			sweepDeadBodies();
		}

		// Draw debug boxes
		debugRenderer.render(world, camera.combined);
	}

	private void renderBackground() {
		backgroundBatch.disableBlending();
		starScrollTimer.add(0.00005f * cockpitVelocity.x, -0.00005f * cockpitVelocity.y); // Move texture
		// nebulaScrollTimer.add(0.0001f * cockpitVelocity.x, -0.0001f * cockpitVelocity.y);

		if (starScrollTimer.x > 1.0f)
			starScrollTimer.x = 0.0f;
		if (starScrollTimer.y > 1.0f)
			starScrollTimer.y = 0.0f;
		starfieldSprite.setU(starScrollTimer.x);
		starfieldSprite.setU2(starScrollTimer.x + 1);
		starfieldSprite.setV(starScrollTimer.y);
		starfieldSprite.setV2(starScrollTimer.y + 1);

		// if (nebulaScrollTimer.x > 1.0f)
		// nebulaScrollTimer.x = 0.0f;
		// if (nebulaScrollTimer.y > 1.0f)
		// nebulaScrollTimer.y = 0.0f;
		// starfieldSprite.setU(nebulaScrollTimer.x);
		// starfieldSprite.setU2(nebulaScrollTimer.x + 1);
		// starfieldSprite.setV(nebulaScrollTimer.y);
		// starfieldSprite.setV2(nebulaScrollTimer.y + 1);

		backgroundBatch.draw(starfieldSprite, -Gdx.graphics.getWidth(), -Gdx.graphics.getHeight());
		// backgroundBatch.draw(nebulaSprite, -Gdx.graphics.getWidth(), -Gdx.graphics.getHeight());

		backgroundBatch.enableBlending();
	}

	// TODO more effectively merge the client with the server here...this method is duplicated
	public void sweepDeadBodies() {
		Array<Body> tempBodies = new Array<Body>();
		world.getBodies(tempBodies);
		Iterator<Body> i = tempBodies.iterator();
		if (!world.isLocked()) {
			while (i.hasNext()) {
				Body b = i.next();
				if (b != null) {
					Part part = (Part) b.getUserData();
					if (part.isDead()) {
						b.setUserData(null);
						removeBodySafely(b);
						b = null;
						i.remove();
					}
				}
			}
		}
		// for (Body body : tempBodies) {
		// if (body != null) {
		// Part part = (Part) body.getUserData();
		// if (part.isDead() && !world.isLocked()) {
		// body.setUserData(null);
		// removeBodySafely(body);
		// body = null;
		// }
		// }
		// }
	}

	public void removeBodySafely(Body body) {
		// to prevent some obscure c assertion that happened randomly once in a blue moon
		final Array<JointEdge> list = body.getJointList();
		while (list.size > 0) {
			world.destroyJoint(list.get(0).joint);
		}
		// actual remove
		world.destroyBody(body);
		effect.setPosition(body.getPosition().x, body.getPosition().y);

		// Scale the particle effect to a proper size.
		float pScale = 0.2f;

		for (ParticleEmitter emitter : effect.getEmitters()) {
			float scaling = emitter.getScale().getHighMax();
			emitter.getScale().setHigh(scaling * pScale);

			scaling = emitter.getScale().getLowMax();
			emitter.getScale().setLow(scaling * pScale);

			scaling = emitter.getVelocity().getHighMax();
			emitter.getVelocity().setHigh(scaling * pScale);

			scaling = emitter.getVelocity().getLowMax();
			emitter.getVelocity().setLow(scaling * pScale);
		}

		effect.start();
	}

	public void processNewPositions() {
		Packet7PositionUpdate msg;
		while ((msg = positionUpdateQueue.poll()) != null) {
			Ship keyedShip = allShips.get(msg.name);
			for (Part part : keyedShip.getParts()) {
				if (part.getNumber() == msg.partNumber) {
					part.getBody().setTransform(new Vector2(msg.x, msg.y), msg.angle);
					if (msg.name.equals(client.getUser().getName()) && part instanceof Cockpit) {
						cockpitVelocity.x = msg.linearX;
						cockpitVelocity.y = msg.linearY;
						System.out.println("Cockpit linear velocity " + cockpitVelocity.x + " " + cockpitVelocity.y);
					}
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
		backgroundBatch.setProjectionMatrix(camera.combined);
		camera.viewportWidth = width / 25f;
		camera.viewportHeight = height / 25f;
		backgroundCam.viewportWidth = width;
		backgroundCam.viewportHeight = height;
	}

	@Override
	public void show() {
		debugRenderer = new Box2DDebugRenderer();

		world = new World(new Vector2(0, 0), true);
		world.setContactListener(new CollisionListener());

		batch = new SpriteBatch();
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		backgroundBatch = new SpriteBatch();
		backgroundCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		effect = new ParticleEffect();
		effect.load(new FileHandle("assets/effects/explosion.p"), new FileHandle("assets/effects"));

		// TODO move all ships to appropriate coordinates, currently they are just their ShipEditor coords
		int position = 0;
		for (String playerName : allShips.keySet()) {
			Ship ship = allShips.get(playerName);
			ship.setWorld(world);
			ship.moveTo(position * 10, position * 10);
			ship.create();
			++position;
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
