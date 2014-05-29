package us.rockhopper.entropy.entities;

import us.rockhopper.entropy.utility.Part;

public class Weapon extends Part {
	
	private String weaponType;
	private String missileTexture;
	private String laserTexture;
	private int fire;
	private int reload;
		//The reload progress of the weapon, measured in 1/60 frame units. 
	private int reloadTime;
		//The reload time of the weapon, measured in 1/60 frame units.

	public Weapon(int gridX, int gridY, int height, int width, float density,
			String sprite, String missileTexture, String laserTexture) {
		super(gridX, gridY, height, width, density, sprite);
		this.missileTexture = missileTexture;
		this.laserTexture = laserTexture;
		reload = 0;
	}

	@Override
	public void update() {
		if (reload == reloadTime
				&& (weaponType.equalsIgnoreCase("LargeMissileLauncher")
						|| weaponType.equalsIgnoreCase("MissileLauncher"))) {
			MissileProjectile missile = new MissileProjectile(
					this.getGridX(), this.getGridY(), 20, 4, 2,
					missileTexture, this.getBody().getAngle());
			missile.create();
		} else if (reload == reloadTime) {
			LaserProjectile laser = new LaserProjectile(
					this.getGridX(), this.getGridY(), 10, 2, 0, laserTexture,
					this.getBody().getAngle());
			laser.create();
		}
	}

	@Override
	public int[] getKeys() {
		int[] keys = {fire};
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
