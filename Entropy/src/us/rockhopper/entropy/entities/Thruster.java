package us.rockhopper.entropy.entities;

import java.util.ArrayList;

import us.rockhopper.entropy.utility.Part;
import us.rockhopper.entropy.utility.Triggerable;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class Thruster extends Part implements Triggerable{

	private int strength;
	private int forwardKey;
	private int backwardKey;
	private boolean canReverse;

	protected Thruster(Vector2 relativePosition, int height, int width,
			float density, Sprite sprite, ArrayList<Part> adjacent) {
		super(relativePosition, height, width, density, sprite, adjacent);
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

	@Override
	public int[] getKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void trigger(int key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unTrigger(int key) {
		// TODO Auto-generated method stub
		
	}
}
