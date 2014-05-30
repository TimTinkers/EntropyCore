package us.rockhopper.entropy.utility;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public abstract class Part implements Cloneable {

	private int gridX;
	private int gridY;
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
	private ArrayList<Vector2> occupiedTiles = new ArrayList<Vector2>();

	public Part(int gridX, int gridY, int height, int width, float density, String sprite) {
		this.gridX = gridX;
		this.gridY = gridY;
		this.height = height;
		this.width = width;
		this.density = density;
		this.sprite = sprite;
		this.id = UUID.randomUUID();
	}

	public Part setOccupiedCells(ArrayList<Vector2> tiles) {
		this.occupiedTiles = tiles;
		return this;
	}

	public ArrayList<Vector2> getOccupiedCells() {
		return this.occupiedTiles;
	}

	public int getGridX() {
		return gridX;
	}

	public int getGridY() {
		return gridY;
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

	public Part setGridPosition(int x, int y) {
		this.gridX = x;
		this.gridY = y;
		return this;
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

	/**
	 * THIS IS ONLY TO BE USED AFTER SHIP CREATION, WHEN ATTACHMENT NODES NO LONGER MATTER. IN ALL CONSTRUCTION ASPECTS
	 * USE rotateLeft() and rotateRight() ONLY.
	 * 
	 * @param rotation
	 *            the integer rotation to set to the part.
	 */
	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	public void clearRotation() {
		int rotations = this.getRotation() / 90;
		if (rotations < 0) {
			for (int i = 0; i < Math.abs(rotations); ++i) {
				rotateLeft();
			}
		} else if (rotations > 0) {
			for (int i = 0; i < rotations; ++i) {
				rotateRight();
			}
		}
		System.out.println("Cleared rotation");
	}

	@Override
	public Part clone() {
		try {
			Part newPart = (Part) super.clone();
			newPart.setBody(body);
			newPart.setGridPosition(gridX, gridY);
			return newPart;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public abstract void update();

	public abstract int[] getKeys();

	public abstract void trigger(int key);

	public abstract void unTrigger(int key);
}
