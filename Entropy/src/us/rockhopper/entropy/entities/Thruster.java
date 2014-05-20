package us.rockhopper.entropy.entities;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public class Thruster extends Body {

	private int strength;
	private int forwardKey;
	private int backwardKey;
	private boolean canReverse;

	protected Thruster(World world, long addr) {
		super(world, addr);
	}

	public Thruster setStrength(int strength) {
		this.strength = strength;
		return this;
	}

	public Thruster setForward(int key) {
		this.forwardKey = key;
		return this;
	}

	public Thruster setBackward(int key) {
		this.backwardKey = key;
		return this;
	}

	public Thruster setCanReverse(boolean can) {
		this.canReverse = can;
		return this;
	}

	public void update() {

	}
}
