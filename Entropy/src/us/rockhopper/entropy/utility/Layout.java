package us.rockhopper.entropy.utility;

import java.util.ArrayList;

public class Layout {

	public int x;
	public int y;
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
		if (part.getGridX() > 0
				&& this.getPart((int) part.getGridX() - 1,
						(int) part.getGridY()) != null) {
			parts.add(this.getPart((int) part.getGridX() - 1,
					(int) part.getGridY()));
		}
		if (part.getGridY() > 0
				&& this.getPart((int) part.getGridX(),
						(int) part.getGridY() - 1) != null) {
			parts.add(this.getPart((int) part.getGridX(),
					(int) part.getGridY() - 1));
		}
		if (part.getGridY() < (y - 1)
				&& this.getPart((int) part.getGridX(),
						(int) part.getGridY() + 1) != null) {
			parts.add(this.getPart((int) part.getGridX(),
					(int) part.getGridY() + 1));
		}
		if (part.getGridX() < (x - 1)
				&& this.getPart((int) part.getGridX() + 1,
						(int) part.getGridY()) != null) {
			parts.add(this.getPart((int) part.getGridX() + 1,
					(int) part.getGridY()));
		}
		return parts;
	}
}
