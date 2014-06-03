package us.rockhopper.entropy.entities;

import com.badlogic.gdx.math.Vector2;

import us.rockhopper.entropy.utility.Part;

/**
 * The class for a weapon component, which is in turn capable of creating its
 * own type of weapon.
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

	public Weapon(int gridX, int gridY, int height, int width, float density,
			String sprite, String weaponType, String projectileTexture) {
		super(gridX, gridY, height, width, density, sprite);
		this.projectileTexture = projectileTexture;
		reload = 0;
	}

	@Override
	public void update() {
		if (reload == reloadTime
				&& (weaponType.equalsIgnoreCase("LargeMissileLauncher"))) {
			MissileProjectile missile = new MissileProjectile(0,
					0, 1.25f, .25f, 2f, projectileTexture,
					this.getBody().getAngle(),
					new Vector2((float) Math.cos((double) this.getHeight()),
							(float) Math.sin((double) this.getWidth())));
			missile.create();
		} else if (reload == reloadTime
				&& (weaponType.equalsIgnoreCase("MissileLauncher"))) {
			MissileProjectile missile = new MissileProjectile(0,
					0, 1.25f, .25f, 2f, projectileTexture,
					this.getBody().getAngle(),
					new Vector2((float) Math.cos((double) this.getHeight()),
							(float) Math.sin((double) this.getWidth())));
			missile.create();
		} else if (reload == reloadTime) {
			LaserProjectile laser = new LaserProjectile(0, 0, 1.125f, .125f, 0,
					projectileTexture, this.getBody().getAngle(),
					this.getBody().getPosition().add(new Vector2(
							(float) Math.cos((double) this.getHeight()),
							(float) Math.sin((double) this.getWidth()))));
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
