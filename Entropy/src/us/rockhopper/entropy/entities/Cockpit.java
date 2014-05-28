package us.rockhopper.entropy.entities;

import us.rockhopper.entropy.utility.Part;

public class Cockpit extends Part {

	public Cockpit(int gridX, int gridY, int height, int width, float density,
			String sprite) {
		super(gridX, gridY, height, width, density, sprite);
	}

	public Cockpit(Part part) {
		super(part.getGridX(), part.getGridY(), part.getHeight(), part
				.getWidth(), part.getDensity(), part.getSprite());
		this.setRotation(part.getRotation());
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
