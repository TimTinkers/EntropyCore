package us.rockhopper.entropy.entities;

import java.util.ArrayList;

import us.rockhopper.entropy.utility.Part;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class Cockpit extends Part {

	public Cockpit(Vector2 relativePosition, int height, int width,
			float density, Sprite sprite, ArrayList<Part> adjacent) {
		super(relativePosition, height, width, density, sprite, adjacent);
	}

	public Cockpit(Part part) {
		super(part.getRelativePosition(), part.getHeight(), part.getWidth(),
				part.getDensity(), part.getSprite(), part.getAdjacent());
	}
}
