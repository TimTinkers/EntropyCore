package us.rockhopper.entropy.gui;

import us.rockhopper.entropy.utility.Part;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class PartImageButton extends Image {

	private Part part;

	public PartImageButton(Part part, Texture texture) {
		super(texture);
		this.part = part;
	}

	public Part getPart() {
		return this.part;
	}

	public void setPart(Part part) {
		this.part = part;
	}
}
