package us.rockhopper.entropy.utility;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Part {

	private Vector2 relativePosition;
	private int height;
	private int width;
	private float density;
	private Sprite sprite;
	private ArrayList<Part> adjacent;
	private Body body;

	public Part(Vector2 relativePosition, int height, int width, float density,
			Sprite sprite, ArrayList<Part> adjacent) {
		this.relativePosition = relativePosition;
		this.height = height;
		this.width = width;
		this.density = density;
		this.sprite = sprite;
		this.adjacent = adjacent;
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

	public Part setTexture(Sprite texture) {
		this.sprite = texture;
		return this;
	}

	public Sprite getSprite() {
		return sprite;
	}

	public Part addAdjacentPart(Part adjacent) {
		this.adjacent.add(adjacent);
		return this;
	}

	public ArrayList<Part> getAdjacent() {
		return adjacent;
	}

	public Body getBody() {
		return this.body;
	}

	public Part setBody(Body body) {
		this.body = body;
		return this;
	}

	public void update() {
		// TODO Auto-generated method stub
	}
}
