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
	private String name;
	private String description;
	private int cost;
	private int health;
	private int[] attachmentNodes;
	private int rotation;

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

	public Vector2 getGridPositionVector() {
		return this.gridPosition;
	}

	public String getName() {
		return this.name;
	}

	public Part setName(String name) {
		this.name = name;
		return this;
	}

	public String getDescription() {
		return this.description;
	}

	public Part setDescription(String description) {
		this.description = description;
		return this;
	}

	public int getCost() {
		return this.cost;
	}

	public Part setCost(int cost) {
		this.cost = cost;
		return this;
	}

	public int getHealth() {
		return this.health;
	}

	public Part setHealth(int health) {
		this.health = health;
		return this;
	}

	public int[] getAttachmentNodes() {
		return this.attachmentNodes;
	}

	public Part setAttachmentNodes(int[] nodes) {
		this.attachmentNodes = nodes;
		return this;
	}

	public void rotateLeft() {
		this.rotation += 90;
		for (int i = 0; i < getAttachmentNodes().length; ++i) {
			int node = getAttachmentNodes()[i];
			switch (node) {
			case 0:
				getAttachmentNodes()[i] = 3;
				break;
			case 1:
				getAttachmentNodes()[i] = 0;
				break;
			case 2:
				getAttachmentNodes()[i] = 1;
				break;
			case 3:
				getAttachmentNodes()[i] = 2;
				break;
			}
		}
	}

	public void rotateRight() {
		this.rotation -= 90;
		for (int i = 0; i < getAttachmentNodes().length; ++i) {
			int node = getAttachmentNodes()[i];
			switch (node) {
			case 0:
				getAttachmentNodes()[i] = 1;
				break;
			case 1:
				getAttachmentNodes()[i] = 2;
				break;
			case 2:
				getAttachmentNodes()[i] = 3;
				break;
			case 3:
				getAttachmentNodes()[i] = 0;
				break;
			}
		}
	}

	public int getRotation() {
		return this.rotation;
	}

	public abstract void update();

	public abstract int[] getKeys();

	public abstract void trigger(int key);

	public abstract void unTrigger(int key);
}
