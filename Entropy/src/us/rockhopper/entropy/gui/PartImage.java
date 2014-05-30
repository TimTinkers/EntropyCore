package us.rockhopper.entropy.gui;

import java.util.ArrayList;

import us.rockhopper.entropy.utility.Part;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class PartImage extends Image {

	private Part part;
	private int gridX;
	private int gridY;

	public PartImage(Part part, Texture texture) {
		super(texture);
		this.part = part;
	}

	// TODO remove the public copy-constructor
	public PartImage(PartImage image) {
		super(new Texture(image.getPart().getSprite()));
		this.part = image.getPart().clone();
	}

	public Part getPart() {
		return this.part;
	}

	public void setPart(Part part) {
		this.part = part;
	}

	public int getGridX() {
		return this.gridX;
	}

	public void setGridX(int x) {
		this.gridX = x;
	}

	public int getGridY() {
		return this.gridY;
	}

	public void setGridY(int y) {
		this.gridY = y;
	}

	public ArrayList<Vector2> getOccupiedCells() {
		ArrayList<Vector2> cells = new ArrayList<Vector2>();
		int rotIndex = (int) (Math.abs(getRotation()) / 90) % 4;
		int tilesX = 0;
		int tilesY = 0;
		if (rotIndex == 1 || rotIndex == 3) {
			tilesX = (int) getHeight() / 16;
			tilesY = (int) getWidth() / 16;
		} else {
			tilesX = (int) getWidth() / 16;
			tilesY = (int) getHeight() / 16;
		}
		for (int i = 0; i < tilesX; ++i) {
			for (int j = 0; j < tilesY; ++j) {
				cells.add(new Vector2(gridX + i, gridY + j));
			}
		}
		return cells;
	}

	/**
	 * Determines if these two parts are able to connect together, given their attachment nodes
	 * 
	 * @param adjacent
	 *            Another partImage to attempt fitting with
	 * @param direction
	 *            The direction to seek for attachment in
	 * @return
	 */
	public boolean fits(PartImage adjacent, int direction) {
		if (adjacent == null) {
			return false;
		}
		int[] base = this.getPart().getAttachmentNodes();
		boolean result = false;
		for (int i = 0; i < base.length; ++i) {
			int node = base[i];
			switch (node) {
			case 0:
				System.out.println("Checking above!");
				for (int j = 0; j < adjacent.getPart().getAttachmentNodes().length; ++j) {
					System.out.println("Name of the adjacent part is " + adjacent);
					System.out.println("The node you are trying to attach to has points at "
							+ adjacent.getPart().getAttachmentNodes()[j]);
					if (direction == node && adjacent.getPart().getAttachmentNodes()[j] == 2) {
						System.out.println("Can attach to the above node");
						result = true;
						break;
					}
				}
				break;
			case 1:
				System.out.println("Checking right!");
				for (int j = 0; j < adjacent.getPart().getAttachmentNodes().length; ++j) {
					System.out.println("Name of the adjacent part is " + adjacent);
					System.out.println("The node you are trying to attach to has points at "
							+ adjacent.getPart().getAttachmentNodes()[j]);
					if (direction == node && adjacent.getPart().getAttachmentNodes()[j] == 3) {
						System.out.println("Can attach to the right node");
						result = true;
						break;
					}
				}
				break;
			case 2: // Attachment node for the piece being attached is 2
				System.out.println("Checking below!");
				for (int j = 0; j < adjacent.getPart().getAttachmentNodes().length; ++j) {
					System.out.println("Name of the adjacent part is " + adjacent);
					System.out.println("The node you are trying to attach to has points at "
							+ adjacent.getPart().getAttachmentNodes()[j]);
					if (direction == node && adjacent.getPart().getAttachmentNodes()[j] == 0) {
						System.out.println("Can attach to the bottom node");
						result = true;
						break;
					}
				}
				break;
			case 3:
				System.out.println("Checking left!");
				for (int j = 0; j < adjacent.getPart().getAttachmentNodes().length; ++j) {
					System.out.println("Name of the adjacent part is " + adjacent.getPart().getName());
					System.out.println("The node you are trying to attach to has points at "
							+ adjacent.getPart().getAttachmentNodes()[j]);
					System.out.println("direction " + direction + " node " + node);
					if (direction == node) {
						System.out.println("DIRECTION IS NODE");
						if (adjacent.getPart().getAttachmentNodes()[j] == 1) {
							System.out.println("Can attach to the left node");
							result = true;
							break;
						}
					}
				}
				break;
			}
		}
		System.out.println("Returning " + result);
		return result;
	}

}
