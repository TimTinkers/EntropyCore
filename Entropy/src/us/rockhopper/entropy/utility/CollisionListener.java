package us.rockhopper.entropy.utility;

import us.rockhopper.entropy.entities.Projectile;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

public class CollisionListener implements ContactListener {

	public CollisionListener() {
	}

	@Override
	public void beginContact(Contact contact) {
		Part partA = (Part) contact.getFixtureA().getBody().getUserData();
		Part partB = (Part) contact.getFixtureB().getBody().getUserData();

		// Handle projectiles
		if (partA instanceof Projectile && !(partB instanceof Projectile)) {
			Projectile projectile = (Projectile) partA;
			partB.setHealth(partB.getHealth() - projectile.getDamage());
			projectile.remove();
		} else if (partB instanceof Projectile && !(partA instanceof Projectile)) {
			Projectile projectile = (Projectile) partB;
			partA.setHealth(partA.getHealth() - projectile.getDamage());
			projectile.remove();
		}
	}

	@Override
	public void endContact(Contact contact) {
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
	}
}
