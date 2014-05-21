package us.rockhopper.entropy.utility;

import java.lang.reflect.Type;

import us.rockhopper.entropy.entities.BasicShip;

import com.badlogic.gdx.math.Vector2;
import com.google.gson.InstanceCreator;

public class BasicShipInstanceCreator implements InstanceCreator<BasicShip> {
	@Override
	public BasicShip createInstance(Type type) {
		return new BasicShip(new Vector2(0, 0), 32 / 2, 32 / 2, null);
	}
}