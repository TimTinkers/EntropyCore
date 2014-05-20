package us.rockhopper.entropy.entities;

import java.util.ArrayList;
import java.util.HashMap;

import net.dermetfan.utils.libgdx.graphics.Box2DSprite;
import us.rockhopper.entropy.utility.Triggerable;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactFilter;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;

/**
 * The class for the player-controlled Ship.
 * 
 * @author Tim Clancy
 * @author Ian Tang
 * @version 5.20.14
 */
public class SampleShip extends InputAdapter implements ContactFilter,
		ContactListener {

	private HashMap<Integer, ArrayList<Triggerable>> keyActions = new HashMap<Integer, ArrayList<Triggerable>>();
	private World world;
	private Body body;
	private Thruster thruster;
	private Fixture fixture;
	public final float WIDTH, HEIGHT;
	private Vector2 velocity = new Vector2(), velocityTurn = new Vector2();
	private float movementForce = 50;
	private Joint joint;
	private ArrayList<Triggerable> triggers;

	public SampleShip(World world, float x, float y, float width, float height,
			ArrayList<Triggerable> triggers) {
		this.triggers = triggers;
		WIDTH = width;
		HEIGHT = height;
		this.world = world;

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(x, y);
		bodyDef.fixedRotation = false;

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width / 2, height / 2);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.restitution = 0;
		fixtureDef.friction = .8f;
		fixtureDef.density = 3;

		Sprite shipSprite = new Sprite(new Texture("assets/img/sampleShip.png"));
		Sprite thrusterSprite = new Sprite(new Texture(
				"assets/img/thruster.png"));

		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef).setUserData(new Box2DSprite(shipSprite));

		BodyDef bodyDef2 = new BodyDef();
		bodyDef2.type = BodyType.DynamicBody;
		bodyDef2.position.set(x, y - height);
		bodyDef2.fixedRotation = false;

		thruster = (Thruster) world.createBody(bodyDef2);
		thruster.createFixture(fixtureDef).setUserData(
				new Box2DSprite(thrusterSprite));
		thruster.setCanReverse(false).setStrength(19).setForward(Keys.W)
				.setBackward(Keys.S);

		WeldJointDef weldJointDef = new WeldJointDef();
		weldJointDef.initialize(body, thruster, new Vector2(x, y - height));
		joint = world.createJoint(weldJointDef);

		shape.dispose();

		// Record triggers
		for (Triggerable trigger : triggers) {
			for (int i = 0; i < trigger.getKeys().length; ++i) {
				if (!keyActions.containsKey(trigger.getKeys()[i])) {
					ArrayList<Triggerable> triggerList = new ArrayList<>();
					triggerList.add(trigger);
					keyActions.put(trigger.getKeys()[i], triggerList);
				} else {
					ArrayList<Triggerable> triggerList = keyActions.get(trigger
							.getKeys()[i]);
					triggerList.add(trigger);
					keyActions.put(trigger.getKeys()[i], triggerList);
				}
			}
		}
	}

	public void update() {
		for (Triggerable trigger : triggers) {
			trigger.update();
		}
		body.applyForceToCenter(velocityTurn, true);
	}

	@Override
	public boolean shouldCollide(Fixture fixtureA, Fixture fixtureB) {
		if (fixtureA == fixture || fixtureB == fixture)
			return body.getLinearVelocity().y < 0;
		return false;
	}

	@Override
	public void beginContact(Contact contact) {
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
	}

	@Override
	public void endContact(Contact contact) {
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keyActions.containsKey(keycode)) {
			for (Triggerable trigger : keyActions.get(keycode)) {
				trigger.trigger(keycode);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean keyUp(int keycode) {
		if (keyActions.containsKey(keycode)) {
			for (Triggerable trigger : keyActions.get(keycode)) {
				trigger.unTrigger(keycode);
			}
			return true;
		} else {
			return false;
		}
	}

	public float getRestitution() {
		return fixture.getRestitution();
	}

	public void setRestitution(float restitution) {
		fixture.setRestitution(restitution);
	}

	public Body getBody() {
		return body;
	}

	public Fixture getFixture() {
		return fixture;
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
	}
}
