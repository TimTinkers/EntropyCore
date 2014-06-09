package us.rockhopper.entropy.entities;

import us.rockhopper.entropy.utility.Part;

public class Gyroscope extends Part {

	private int strength;
	private int counterClockwiseKey;
	private int clockwiseKey;
	private boolean clockwiseThrust;
	private boolean counterClockwiseThrust;

	public Gyroscope(int gridX, int gridY, int height, int width,
			float density, String sprite) {
		super(gridX, gridY, height, width, density, sprite);
	}

	public Gyroscope setStrength(int strength) {
		this.strength = strength;
		return this;
	}

	public Gyroscope setClockwise(int key) {
		this.clockwiseKey = key;
		return this;
	}

	public Gyroscope setCounterClockwise(int key) {
		this.counterClockwiseKey = key;
		return this;
	}

	@Override
	public void update() {
		if (clockwiseThrust) {
			this.getBody().applyTorque(strength * -1, true);
		} else if (counterClockwiseThrust) {
			this.getBody().applyTorque(strength * 1, true);
		}
	}

	@Override
	public int[] getKeys() {
		int[] keys = { clockwiseKey, counterClockwiseKey };
		return keys;
	}

	@Override
	public void trigger(int key) {
		if (key == clockwiseKey) {
			clockwiseThrust = true;
		} else if (key == counterClockwiseKey) {
			counterClockwiseThrust = true;
		}
	}

	@Override
	public void unTrigger(int key) {
		if (key == clockwiseKey) {
			clockwiseThrust = false;
		} else if (key == counterClockwiseKey) {
			counterClockwiseThrust = false;
		}
	}

}
