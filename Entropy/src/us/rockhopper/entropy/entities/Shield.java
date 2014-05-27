package us.rockhopper.entropy.entities;

import com.badlogic.gdx.math.Vector2;

import us.rockhopper.entropy.utility.Part;

public class Shield extends Part {

	public Shield(Vector2 gridPosition, int height, int width, float density,
			String sprite) {
		super(gridPosition, height, width, density, sprite);
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
