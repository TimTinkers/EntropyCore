package us.rockhopper.entropy.entities;

import java.util.ArrayList;

import us.rockhopper.entropy.utility.Part;

import com.badlogic.gdx.math.Vector2;

/**
 * The class for a weapon component, which is in turn capable of creating its own type of weapon and tracking its own
 * projectiles.
 * 
 * @author Ian Tang
 * @author Tim Clancy
 * @version 6.9.2014
 */
public class Weapon extends Part {

	private final float RECOIL = 100;
	private String weaponType;
	private String projectileTexture;
	private int fire; // The fire key associated with this weapon.
	private int reload; // The reload progress of the weapon, measured in 1/60 frame units.
	private int reloadTime; // The reload time of the weapon, measured in 1/60 frame units.
	private boolean shouldFire;

	private ArrayList<Part> firedProjectiles; // A registry of all projectiles fired by this weapon.

	public Weapon(int gridX, int gridY, int height, int width, float density, String sprite, String weaponType,
			String projectileTexture, int reloadTime, ArrayList<Part> projectiles) {
		super(gridX, gridY, height, width, density, sprite);
		this.weaponType = weaponType;
		this.projectileTexture = projectileTexture;
		this.reloadTime = reloadTime;
		this.firedProjectiles = projectiles;
		reload = reloadTime;
		shouldFire = false;
	}

	public Weapon setFire(int key) {
		this.fire = key;
		return this;
	}

	@Override
	public void update() {
		super.update();
		if (reload < reloadTime) {
			reload++;
		}

		for (Part projectile : firedProjectiles) { // TODO the projectile might need to remove itself from this list
			projectile.update();
		}

		if (shouldFire == true) {
			if (reload == reloadTime && weaponType.equalsIgnoreCase("LargeMissileLauncher")) {
				MissileProjectile missile = new MissileProjectile(0, 0, 1.25f, .25f, 2f, projectileTexture, this
						.getBody().getAngle(), this
						.getBody()
						.getPosition()
						.add(new Vector2((float) (-1 * Math.sin(this.getBody().getAngle()) * this.getHeight()),
								(float) Math.cos(this.getBody().getAngle()) * this.getHeight())), this.getBody()
						.getLinearVelocity(), this.getBody().getWorld(), 15);
				missile.create();
				firedProjectiles.add((Part) missile);

				this.getBody().applyForceToCenter(
						new Vector2((float) Math.sin(this.getBody().getAngle()) * RECOIL, (float) Math.cos(this
								.getBody().getAngle()) * -1 * RECOIL), true);
				reload = 0;
			} else if (reload == reloadTime && weaponType.equalsIgnoreCase("MissileLauncher")) {
				MissileProjectile missile = new MissileProjectile(0, 0, 1.25f, .25f, 2f, projectileTexture, this
						.getBody().getAngle(), this
						.getBody()
						.getPosition()
						.add(new Vector2((float) (-1 * Math.sin(this.getBody().getAngle()) * this.getHeight()),
								(float) Math.cos(this.getBody().getAngle()) * this.getHeight())), this.getBody()
						.getLinearVelocity(), this.getBody().getWorld(), 15);
				missile.create();
				firedProjectiles.add((Part) missile);

				this.getBody().applyForceToCenter(
						new Vector2((float) Math.sin(this.getBody().getAngle()) * RECOIL, (float) Math.cos(this
								.getBody().getAngle()) * -1 * RECOIL), true);
				reload = 0;
			} else if (reload == reloadTime && weaponType.equalsIgnoreCase("TorpedoLauncher")) {
				MissileProjectile missile = new MissileProjectile(0, 0, 1.5f, .375f, 5f, projectileTexture, this
						.getBody().getAngle(), this
						.getBody()
						.getPosition()
						.add(new Vector2((float) (-1 * Math.sin(this.getBody().getAngle()) * this.getHeight()),
								(float) Math.cos(this.getBody().getAngle()) * this.getHeight())), this.getBody()
						.getLinearVelocity(), this.getBody().getWorld(), 25);
				missile.create();
				firedProjectiles.add((Part) missile);

				this.getBody().applyForceToCenter(
						new Vector2((float) Math.sin(this.getBody().getAngle()) * RECOIL, (float) Math.cos(this
								.getBody().getAngle()) * -1 * RECOIL), true);
				reload = 0;
			} else if (reload == reloadTime && weaponType.equalsIgnoreCase("BasicLaser")) {
				LaserProjectile laser = new LaserProjectile(0, 0, 1.125f, .125f, 0.01f, projectileTexture, this
						.getBody().getAngle(), this
						.getBody()
						.getPosition()
						.add(new Vector2((float) (-1 * Math.sin(this.getBody().getAngle()) * this.getHeight()),
								(float) Math.cos(this.getBody().getAngle()) * this.getHeight())), this.getBody()
						.getWorld(), 15f, 5);
				laser.create();
				firedProjectiles.add((Part) laser);
				reload = 0;
			} else if (reload == reloadTime && weaponType.equalsIgnoreCase("SnipeLaser")) {
				LaserProjectile laser = new LaserProjectile(0, 0, 1.125f, .125f, 0.01f, projectileTexture, this
						.getBody().getAngle(), this
						.getBody()
						.getPosition()
						.add(new Vector2((float) (-1 * Math.sin(this.getBody().getAngle()) * this.getHeight()),
								(float) Math.cos(this.getBody().getAngle()) * this.getHeight())), this.getBody()
						.getWorld(), 30f, 10);
				laser.create();
				firedProjectiles.add((Part) laser);
				reload = 0;
			}
		}
	}

	public ArrayList<Part> getProjectiles() {
		return this.firedProjectiles;
	}

	@Override
	public int[] getKeys() {
		int[] keys = { fire };
		return keys;
	}

	@Override
	public void trigger(int key) {
		if (!this.weaponType.equalsIgnoreCase("RammingSpike")) { // TODO make these strings into Enums?
			if (key == fire) {
				shouldFire = true;
			}
		}
	}

	@Override
	public void unTrigger(int key) {
		if (!this.weaponType.equalsIgnoreCase("RammingSpike")) {
			if (key == fire) {
				shouldFire = false;
			}
		}
	}
}
