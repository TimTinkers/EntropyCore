package us.rockhopper.entropy.entities;

import us.rockhopper.entropy.utility.Part;

import com.badlogic.gdx.math.Vector2;

public class Thruster extends Part {

	private int strength;
	private int forwardKey;
	private int reverseKey;
	private boolean canReverse;
	private boolean shouldForward;
	private boolean shouldReverse;

	public Thruster(int gridX, int gridY, int height, int width, float density,
			String sprite) {
		super(gridX, gridY, height, width, density, sprite);
		shouldForward = false;
	}

	public Thruster setStrength(int strength) {
		this.strength = strength;
		return this;
	}

	public Thruster setForward(int key) {
		this.forwardKey = key;
		return this;
	}

	public Thruster setReverse(int key) {
		this.reverseKey = key;
		return this;
	}

	public Thruster setCanReverse(boolean can) {
		this.canReverse = can;
		return this;
	}

	public boolean getCanReverse() {
		return this.canReverse;
	}

	public void update() {
		if (shouldForward) {
			this.getBody().applyForceToCenter(
					new Vector2((float) Math.sin(this.getBody().getAngle())
							* -1 * strength, (float) Math.cos(this.getBody()
							.getAngle()) * strength), true);
		} else if (canReverse && shouldReverse) {
			this.getBody().applyForceToCenter(
					new Vector2((float) Math.sin(this.getBody().getAngle())
							* strength, (float) Math.cos(this.getBody()
							.getAngle()) * -1 * strength), true);
		}
	}

	@Override
	public int[] getKeys() {
		int[] keys = { forwardKey, reverseKey };
		return keys;
	}

	@Override
	public void trigger(int key) {
		if (key == forwardKey) {
			shouldForward = true;
		} else if (key == reverseKey) {
			shouldReverse = true;
		}
	}

	@Override
	public void unTrigger(int key) {
		if (key == forwardKey) {
			shouldForward = false;
		} else if (key == reverseKey) {
			shouldReverse = false;
		}
	}
}
