package us.rockhopper.entropy.utility;

public interface Triggerable {
	public int[] getKeys();

	public void trigger(int key);

	public void unTrigger(int key);
}
