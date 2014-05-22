package us.rockhopper.entropy.utility;

import java.util.UUID;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Part {

	private Vector2 relativePosition;
	private final UUID id;
	private int height;
	private int width;
	private float density;
	private String sprite;
	private Body body;

	public Part(Vector2 relativePosition, int height, int width, float density,
			String sprite) {
		this.relativePosition = relativePosition;
		this.height = height;
		this.width = width;
		this.density = density;
		this.sprite = sprite;
		this.id = UUID.randomUUID();
	}

	public Part setRelativePosition(Vector2 vector) {
		this.relativePosition = vector;
		return this;
	}

	public Vector2 getRelativePosition() {
		return this.relativePosition;
	}

	public Part setHeight(int height) {
		this.height = height;
		return this;
	}

	public int getHeight() {
		return height;
	}

	public Part setWidth(int width) {
		this.width = width;
		return this;
	}

	public int getWidth() {
		return width;
	}

	public Part setDensity(float density) {
		this.density = density;
		return this;
	}

	public float getDensity() {
		return density;
	}

	public Part setTexture(String texture) {
		this.sprite = texture;
		return this;
	}

	public String getSprite() {
		return sprite;
	}

	public Body getBody() {
		return this.body;
	}

	public Part setBody(Body body) {
		this.body = body;
		return this;
	}

	public UUID getUUID() {
		return this.id;
	}

	public void update() {
		// TODO Auto-generated method stub
	}
}
