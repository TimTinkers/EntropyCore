package us.rockhopper.entropy.gui;

import us.rockhopper.entropy.utility.Part;

import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;

public class PartImageButton extends ImageButton {

	private Part part;

	public PartImageButton(ImageButtonStyle style, Part part) {
		super(style);
		this.part = part;
	}

	public Part getPart() {
		return this.part;
	}

	public void setPart(Part part) {
		this.part = part;
	}
}
