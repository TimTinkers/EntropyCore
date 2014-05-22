package us.rockhopper.entropy.utility;

import java.util.UUID;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public abstract class Part {

	private Vector2 gridPosition;
	private final UUID id;
	private int height;
	private int width;
	private float density;
	private String sprite;
	private Body body;

	public Part(Vector2 gridPosition, int height, int width, float density,
			String sprite) {
		this.gridPosition = gridPosition;
		this.height = height;
		this.width = width;
		this.density = density;
		this.sprite = sprite;
		this.id = UUID.randomUUID();
	}

	public Part setGridPosition(Vector2 vector) {
		this.gridPosition = vector;
		return this;
	}

	public int getGridX() {
		return (int) this.gridPosition.x;
	}

	public int getGridY() {
		return (int) this.gridPosition.y;
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

	public abstract void update();

	public Vector2 getGridPositionVector() {
		return this.gridPosition;
	}
}
