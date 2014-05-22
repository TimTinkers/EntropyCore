package us.rockhopper.entropy.utility;

import java.util.ArrayList;

public class Layout {

	int x;
	int y;
	Part[][] ids;
	ArrayList<Part> parts = new ArrayList<Part>();

	public Layout(int x, int y) {
		this.x = x;
		this.y = y;
		ids = new Part[x][y];
	}

	public void setPart(Part part, int x, int y) {
		ids[x][y] = part;
	}

	public Part getPart(int x, int y) {
		return ids[x][y];
	}

	public ArrayList<Part> getAdjacent(Part part) {
		System.out.println("Position is "
				+ part.getRelativePosition().toString());
		if (part.getRelativePosition().x > 0) {
			System.out.println((int) part.getRelativePosition().x - 1);
			System.out.println((int) part.getRelativePosition().y);
			parts.add(this.getPart((int) part.getRelativePosition().x - 1,
					(int) part.getRelativePosition().y));
		}
		if (part.getRelativePosition().y > 0) {
			System.out.println((int) part.getRelativePosition().x);
			System.out.println((int) part.getRelativePosition().y - 1);
			parts.add(this.getPart((int) part.getRelativePosition().x,
					(int) part.getRelativePosition().y - 1));
		}
		if (part.getRelativePosition().y < (y - 1)) {
			System.out.println((int) part.getRelativePosition().x);
			System.out.println((int) part.getRelativePosition().y + 1);
			parts.add(this.getPart((int) part.getRelativePosition().x,
					(int) part.getRelativePosition().y + 1));
		}
		if (part.getRelativePosition().x < (x - 1)) {
			System.out.println((int) part.getRelativePosition().x + 1);
			System.out.println((int) part.getRelativePosition().y);
			parts.add(this.getPart((int) part.getRelativePosition().x + 1,
					(int) part.getRelativePosition().y));
		}
		return parts;
	}
}
