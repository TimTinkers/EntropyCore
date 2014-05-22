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
		System.out.println("UPDATING COCKPIT");
	}
}
