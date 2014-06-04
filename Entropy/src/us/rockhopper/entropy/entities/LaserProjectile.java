package us.rockhopper.entropy.entities;

import net.dermetfan.utils.libgdx.graphics.Box2DSprite;
import us.rockhopper.entropy.utility.Part;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class LaserProjectile extends Part{

	private int lifeTime;
	private float angle;
	private Vector2 position = new Vector2();
	private World world;
	
	public LaserProjectile(int gridX, int gridY, float height, float width,
			float density, String sprite, float angle, Vector2 pos, World world) {
		super(gridX, gridY, height, width, density, sprite);
		this.angle = angle;
		position.set(pos);
		this.world = world;
		lifeTime = 0;
	}
	
	public void update() {
		lifeTime++;
		
		if(lifeTime == 180) {
			this.getBody().getWorld().destroyBody(this.getBody());
		}
	}
	
	public void create() {
		Body body;
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.active = true;
		bodyDef.fixedRotation = true;
		bodyDef.position.set(position);
		bodyDef.angle = angle;
		bodyDef.gravityScale = 0;

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.restitution = 0;
		fixtureDef.friction = 0;
		fixtureDef.density = this.getDensity();
		fixtureDef.filter.categoryBits = 2;
		fixtureDef.filter.maskBits = ~2;
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(this.getWidth()/2f,this.getHeight()/2f);
		fixtureDef.shape = shape;
		
		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef).setUserData(
				new Box2DSprite(new Sprite(new Texture(this.getSprite()))));
		this.setBody(body);
		
		this.getBody().applyForceToCenter(
				new Vector2((float) Math.sin(this.getBody().getAngle())
						* -10, (float) Math.cos(this.getBody()
						.getAngle()) * 10), true);
		
		shape.dispose();
		
		System.out.println("A laser beam has been created at " + Math.toDegrees(this.getBody().getAngle() % (Math.PI * 2)) + " degrees!");
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
