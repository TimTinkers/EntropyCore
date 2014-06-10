package us.rockhopper.entropy.utility;

import us.rockhopper.entropy.entities.MissileProjectile;
import us.rockhopper.entropy.entities.LaserProjectile;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class CollisionListener implements ContactListener{

	public void beginContact(Contact contact) {
		Part partA = null;
		Part partB = null;

		for (Fixture fixture: contact.getFixtureA().getBody().getFixtureList()) {
			if (fixture.getUserData() instanceof Part) {
				partA = (Part) fixture.getUserData();
			}
		}
		
		for (Fixture fixture: contact.getFixtureB().getBody().getFixtureList()) {
			if (fixture.getUserData() instanceof Part) {
				partB = (Part) fixture.getUserData();
			}
		}

		LaserProjectile laser;
		MissileProjectile missile;

		if (partA instanceof LaserProjectile) {
			laser = (LaserProjectile) partA;
			partB.setHealth((int) Math.round(partB.getHealth() - laser.getDamage() * (0.75 + Math.random() * 0.5)));
			laser.getBody().getWorld().destroyBody(laser.getBody());
		} else if (partA instanceof MissileProjectile) {
			missile = (MissileProjectile) partA;
			partB.setHealth((int) Math.round(partB.getHealth() - missile.getDamage() * (0.75 + Math.random() * 0.5)));
			missile.getBody().getWorld().destroyBody(missile.getBody());
		} else if (partB instanceof LaserProjectile) {
			laser = (LaserProjectile) partB;
			partA.setHealth((int) Math.round(partB.getHealth() - laser.getDamage() * (0.75 + Math.random() * 0.5)));
			laser.getBody().getWorld().destroyBody(laser.getBody());
		} else if (partB instanceof MissileProjectile) {
			missile = (MissileProjectile) partB;
			partA.setHealth((int) Math.round(partB.getHealth() - missile.getDamage() * (0.75 + Math.random() * 0.5)));
			missile.getBody().getWorld().destroyBody(missile.getBody());
		}
	}

	public void endContact(Contact contact) {
	}

	public void preSolve(Contact contact, Manifold manifold) {
	}

	public void postSolve(Contact contact, ContactImpulse impulse) {
	}
}
