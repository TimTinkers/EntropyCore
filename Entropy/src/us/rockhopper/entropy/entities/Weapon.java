package us.rockhopper.entropy.entities;

import us.rockhopper.entropy.utility.Part;

/**
 * The class for a weapon component, which is in turn capable of creating its own type of
 * 
 * @author Ian Tang
 * @author Tim Clancy
 */
public class Weapon extends Part {

	private String weaponType;
	private String projectileTexture;
	private int fire;
	private int reload;
	// The reload progress of the weapon, measured in 1/60 frame units.
	private int reloadTime;

	// The reload time of the weapon, measured in 1/60 frame units.

	public Weapon(int gridX, int gridY, int height, int width, float density, String sprite, String weaponType,
			String projectileTexture) {
		super(gridX, gridY, height, width, density, sprite);
		this.projectileTexture = projectileTexture;
		reload = 0;
	}

	@Override
	public void update() {
		if (reload == reloadTime
				&& (weaponType.equalsIgnoreCase("LargeMissileLauncher") || weaponType
						.equalsIgnoreCase("MissileLauncher"))) {
			MissileProjectile missile = new MissileProjectile(this.getGridX(), this.getGridY(), 20, 4, 2,
					projectileTexture, this.getBody().getAngle());
			missile.create();
		} else if (reload == reloadTime) {
			LaserProjectile laser = new LaserProjectile(this.getGridX(), this.getGridY(), 10, 2, 0, projectileTexture,
					this.getBody().getAngle());
			laser.create();
		}
	}

	@Override
	public int[] getKeys() {
		int[] keys = { fire };
		return keys;
	}

	@Override
	public void trigger(int key) {
		if (key == fire) {
			reload++;
		}
	}

	@Override
	public void unTrigger(int key) {
		if (key == fire) {
			reload = 0;
		}
	}
}
