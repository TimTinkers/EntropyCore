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
	
	public PartImageButton(PartImageButton button) {
		super(new Texture(button.getPart().getSprite()));
		this.part = button.getPart().clone();
	}

	public Part getPart() {
		return this.part;
	}

	public void setPart(Part part) {
		this.part = part;
	}
}
