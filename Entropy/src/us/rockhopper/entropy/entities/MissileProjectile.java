package us.rockhopper.entropy.entities;

import net.dermetfan.utils.libgdx.graphics.Box2DSprite;
import us.rockhopper.entropy.utility.Part;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

/**
 * The class for a missile projectile.
 * 
 * @author Ian Tang
 * @author Tim Clancy
 * @version 6.9.2014
 * 
 */
public class MissileProjectile extends Part implements Projectile {

	private int lifeTime;
	private float angle;
	private final float THRUST = 40;
	private int damage;

	private Vector2 position = new Vector2();
	private Vector2 launchVelocity;
	private World world;

	public MissileProjectile(int gridX, int gridY, float height, float width, float density, String sprite,
			float angle, Vector2 pos, Vector2 launchVelocity, World world, int damage) {
		super(gridX, gridY, height, width, density, sprite);
		this.angle = angle;
		this.position.set(pos);
		this.launchVelocity = launchVelocity;
		this.world = world;
		lifeTime = 0;
		this.damage = damage;
	}

	public void update() {
		lifeTime++;
		if (lifeTime == 600) {
			this.getBody().getWorld().destroyBody(this.getBody());
		}

		this.getBody().applyForceToCenter(
				new Vector2((float) Math.sin(this.getBody().getAngle()) * -1 * THRUST, (float) Math.cos(this.getBody()
						.getAngle()) * THRUST), true);
	}

	public void create() {
		Body body;
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.active = true;
		bodyDef.fixedRotation = false;
		bodyDef.position.set(position);
		bodyDef.angle = angle;
		bodyDef.gravityScale = 0f;

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.restitution = 0;
		fixtureDef.friction = 0;
		fixtureDef.density = this.getDensity();

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(this.getWidth() / 2f, this.getHeight() / 2f);
		fixtureDef.shape = shape;

		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef).setUserData(new Box2DSprite(new Sprite(new Texture(this.getSprite()))));
		this.setBody(body);
		body.setUserData(this);
		this.getBody().setLinearVelocity(launchVelocity);

		this.getBody().applyForceToCenter(
				new Vector2((float) Math.sin(this.getBody().getAngle()) * -100, (float) Math.cos(this.getBody()
						.getAngle()) * 100), true);

		System.out.println("Created missile");

		shape.dispose();
	}

	@Override
	public int[] getKeys() {
		return new int[0];
	}

	@Override
	public void trigger(int key) {
	}

	@Override
	public void unTrigger(int key) {
	}

	@Override
	public void remove() {
		this.die();
	}

	@Override
	public int getDamage() {
		return this.damage;
	}
}
