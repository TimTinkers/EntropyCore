package us.rockhopper.entropy.entities;

import net.dermetfan.utils.libgdx.graphics.Box2DSprite;

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
 * @version 5.16.14
 */
public class SampleShip extends InputAdapter implements ContactFilter,
		ContactListener {

	private World world;
	private Body body;
	private Body body2;
	private Fixture fixture;
	public final float WIDTH, HEIGHT;
	private Vector2 velocity= new Vector2(), velocityTurn = new Vector2();
	private float movementForce = 50, jumpPower = 45;
	private Joint joint;

	public SampleShip(World world, float x, float y, float width, float height) {
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

		body2 = world.createBody(bodyDef2);
		body2.createFixture(fixtureDef).setUserData(
				new Box2DSprite(thrusterSprite));

		WeldJointDef weldJointDef = new WeldJointDef();
		weldJointDef.initialize(body, body2, new Vector2(x, y - height));
		joint = world.createJoint(weldJointDef);

		shape.dispose();
	}

	public void update() {
		body2.applyForceToCenter(velocity, true);
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
	public void postSolve(Contact contact, ContactImpulse impulse) {
		if (contact.getFixtureA() == fixture
				|| contact.getFixtureB() == fixture)
			if (contact.getWorldManifold().getPoints()[0].y <= body
					.getPosition().y - HEIGHT / 2)
				body.applyLinearImpulse(0, jumpPower, body.getWorldCenter().x,
						body.getWorldCenter().y, true);
	}

	@Override
	public void endContact(Contact contact) {
	}

	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
		case Keys.W:
			velocity.y = movementForce;
			break;
		case Keys.A:
			velocityTurn.x = -movementForce;
			break;
		case Keys.D:
			velocityTurn.x = movementForce;
			break;
		case Keys.S:
			velocity.y = -movementForce;
			break;
		case Keys.E:
			world.destroyJoint(joint);
			break;
		default:
			return false;
		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
		case Keys.A:
		case Keys.D:
			velocityTurn.x = 0;
			return true;
		case Keys.W:
		case Keys.S:
			velocity.y = 0;
			return true;
		default:
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
}
