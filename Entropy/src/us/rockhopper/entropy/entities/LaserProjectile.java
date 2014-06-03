package us.rockhopper.entropy.entities;

import net.dermetfan.utils.libgdx.graphics.Box2DSprite;
import us.rockhopper.entropy.utility.Part;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class LaserProjectile extends Part {

	private float angle;
	private Vector2 position;
	private World world;

	public LaserProjectile(float height, float width, String sprite, float angle, Vector2 pos, World world) {
		super(0, 0, height, width, 0, sprite);
		this.angle = angle;
		this.position = pos;
		this.world = world;
	}

	public void update() {
	}

	public void create() {
		Body body;
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.KinematicBody;
		bodyDef.active = true;
		bodyDef.fixedRotation = true;
		bodyDef.position.set(position);
		bodyDef.angle = angle;
		bodyDef.gravityScale = 0;

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.restitution = 0;
		fixtureDef.friction = 0;
		fixtureDef.density = 0;
		fixtureDef.filter.categoryBits = 2;
		fixtureDef.filter.maskBits = ~2;

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(this.getWidth() / 2f, this.getHeight() / 2f);
		fixtureDef.shape = shape;

		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef).setUserData(new Box2DSprite(new Sprite(new Texture(this.getSprite()))));
		this.setBody(body);

		this.getBody().applyAngularImpulse(40, true);

		shape.dispose();
	}

	public void setWorld(World world) {
		this.world = world;
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
}
