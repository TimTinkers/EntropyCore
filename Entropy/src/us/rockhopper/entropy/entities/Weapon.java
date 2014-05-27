package us.rockhopper.entropy.entities;

import us.rockhopper.entropy.utility.Part;

public class Weapon extends Part {

	public Weapon(int gridX, int gridY, int height, int width, float density,
			String sprite) {
		super(gridX, gridY, height, width, density, sprite);
	}

	@Override
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
