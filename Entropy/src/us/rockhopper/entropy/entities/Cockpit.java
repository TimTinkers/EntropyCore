package us.rockhopper.entropy.entities;

import us.rockhopper.entropy.utility.Part;

import com.badlogic.gdx.math.Vector2;

public class Cockpit extends Part {

	public Cockpit(Vector2 relativePosition, int height, int width,
			float density, String sprite) {
		super(relativePosition, height, width, density, sprite);
	}

	public Cockpit(Part part) {
		super(part.getGridPositionVector(), part.getHeight(), part.getWidth(),
				part.getDensity(), part.getSprite());
	}

	public void update() {
	}

	@Override
	public int[] getKeys() {
		return null;
	}

	@Override
	public void trigger(int key) {
	}

	@Override
	public void unTrigger(int key) {
	}
}
