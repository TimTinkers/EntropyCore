package us.rockhopper.entropy.entities;

import com.badlogic.gdx.math.Vector2;

import us.rockhopper.entropy.utility.Part;

/**
 * The class for a weapon component, which is in turn capable of creating its own type of weapon.
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
	private boolean shouldFire;

	public Weapon(int gridX, int gridY, int height, int width, float density,
			String sprite, String weaponType, String projectileTexture,
			int reloadTime) {
		super(gridX, gridY, height, width, density, sprite);
		this.weaponType = weaponType;
		this.projectileTexture = projectileTexture;
		this.reloadTime = reloadTime;
		reload = 0;
		shouldFire = false;
	}
	
	public Weapon setFire(int key) {
		this.fire = key;
		return this;
	}

	@Override
	public void update() {
		if (shouldFire == true) {
			reload++;
		}
		
		if (reload == reloadTime
				&& (weaponType.equalsIgnoreCase("LargeMissileLauncher"))) {
			MissileProjectile missile = new MissileProjectile(0,
					0, 1.25f, .25f, 2f, projectileTexture,
					this.getBody().getAngle(),
					new Vector2((float) Math.cos((double) this.getHeight()),
							(float) Math.sin((double) this.getWidth())),
							this.getBody().getWorld());
			missile.create();
			reload = 0;
		} else if (reload == reloadTime
				&& (weaponType.equalsIgnoreCase("MissileLauncher"))) {
			MissileProjectile missile = new MissileProjectile(0,
					0, 1.25f, .25f, 2f, projectileTexture,
					this.getBody().getAngle(),
					new Vector2((float) Math.cos((double) this.getHeight()),
							(float) Math.sin((double) this.getWidth())),
							this.getBody().getWorld());
			missile.create();
			reload = 0;
		} else if (reload == reloadTime
				&& (weaponType.equalsIgnoreCase("TorpedoLauncher"))) {
			MissileProjectile missile = new MissileProjectile(0,
					0, 1.5f, .375f, 5f, projectileTexture,
					this.getBody().getAngle(),
					new Vector2((float) Math.cos((double) this.getHeight()),
							(float) Math.sin((double) this.getWidth())),
							this.getBody().getWorld());
			missile.create();
			reload = 0;
		} else if (reload == reloadTime) {
			LaserProjectile laser = new LaserProjectile(1.125f, .125f, projectileTexture, this.getBody().getAngle(),
					this.getBody()
							.getPosition()
							.add(new Vector2((float) Math.cos((double) this.getHeight()), (float) Math
									.sin((double) this.getWidth()))), this.getBody().getWorld());

			laser.create();
			reload = 0;
			
			System.out.println("This weapon is pointing at " + Math.toDegrees(this.getBody().getAngle() % (Math.PI * 2)) + " degrees!");
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
			shouldFire = true;
		}
	}

	@Override
	public void unTrigger(int key) {
		if (key == fire) {
			shouldFire = false;
			reload = 0;
		}
	}
}
