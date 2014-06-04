package us.rockhopper.entropy.utility;

public class Resolution {
	public int x;
	public int y;

	public Resolution(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return x + " x " + y;
	}
}
