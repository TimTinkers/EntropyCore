package us.rockhopper.entropy.utility;

import java.util.ArrayList;

import us.rockhopper.entropy.entities.Cockpit;

import com.badlogic.gdx.math.Vector2;

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
		parts.clear();
		System.out.println("Finding parts adjacent to " + part);
		if (part.getGridX() > 0
				&& this.getPart((int) part.getGridX() - 1,
						(int) part.getGridY()) != null) {
			System.out.println("Adding "
					+ this.getPart((int) part.getGridX() - 1,
							(int) part.getGridY()).getName()
					+ " "
					+ this.getPart((int) part.getGridX() - 1,
							(int) part.getGridY()));
			parts.add(this.getPart((int) part.getGridX() - 1,
					(int) part.getGridY()));
		}
		if (part.getGridY() > 0
				&& this.getPart((int) part.getGridX(),
						(int) part.getGridY() - 1) != null) {
			System.out.println("Adding "
					+ this.getPart((int) part.getGridX(),
							(int) part.getGridY() - 1)
					+ " "
					+ this.getPart((int) part.getGridX(),
							(int) part.getGridY() - 1));
			parts.add(this.getPart((int) part.getGridX(),
					(int) part.getGridY() - 1));
		}
		if (part.getGridY() < (y - 1)
				&& this.getPart((int) part.getGridX(),
						(int) part.getGridY() + 1) != null) {
			System.out.println("Adding "
					+ this.getPart((int) part.getGridX(),
							(int) part.getGridY() + 1)
					+ " "
					+ this.getPart((int) part.getGridX(),
							(int) part.getGridY() + 1));
			parts.add(this.getPart((int) part.getGridX(),
					(int) part.getGridY() + 1));
		}
		if (part.getGridX() < (x - 1)
				&& this.getPart((int) part.getGridX() + 1,
						(int) part.getGridY()) != null) {
			System.out.println("Adding "
					+ this.getPart((int) part.getGridX() + 1,
							(int) part.getGridY())
					+ " "
					+ this.getPart((int) part.getGridX() + 1,
							(int) part.getGridY()));
			parts.add(this.getPart((int) part.getGridX() + 1,
					(int) part.getGridY()));
		}
		return parts;
	}

	public int getCockpitX() {
		for (int i = 0; i < x; ++i) {
			for (int j = 0; j < y; ++j) {
				if (getPart(i, j) instanceof Cockpit) {
					return getPart(i, j).getGridX();
				}
			}
		}
		throw new RuntimeException();
	}

	public int getCockpitY() {
		for (int i = 0; i < x; ++i) {
			for (int j = 0; j < y; ++j) {
				if (getPart(i, j) instanceof Cockpit) {
					return getPart(i, j).getGridY();
				}
			}
		}
		throw new RuntimeException();
	}

	public void poll() {
		for (int i = 0; i < x; ++i) {
			for (int j = 0; j < y; ++j) {
				if (getPart(i, j) != null) {
					System.out.print(getPart(i, j).getName() + "| ");
				} else {
					System.out.print(" null | ");
				}
			}
			System.out.println("");
		}
	}
}
