package us.rockhopper.entropy.entities;

import java.util.ArrayList;
import java.util.HashMap;

import net.dermetfan.utils.libgdx.graphics.Box2DSprite;
import us.rockhopper.entropy.utility.Layout;
import us.rockhopper.entropy.utility.Part;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * The class for the player-controlled Ship.
 * 
 * @author Tim Clancy
 * @author Ian Tang
 * @version 5.21.14
 */
public class BasicShip extends InputAdapter implements Json.Serializable {

	private World world;
	private Vector2 cockpitPosition;
	private int width;
	private int height;
	private ArrayList<Part> parts = new ArrayList<Part>();
	private Layout setup;
	private HashMap<Integer, ArrayList<Part>> keyActions = new HashMap<Integer, ArrayList<Part>>();

	/**
	 * Creates a ship object, which contains all information it would need to
	 * later render itself.
	 * 
	 * @param cockpitX
	 *            The x-coordinate of the cockpit.
	 * @param cockpitY
	 *            The y-coordinate of the cockpit.
	 * @param width
	 *            The width of the ship.
	 * @param height
	 *            The height of the ship.
	 * @param parts
	 *            All parts on the ship. The first Part in this list MUST be the
	 *            Cockpit of the ship.
	 */
	public BasicShip(int cockpitX, int cockpitY, int width, int height,
			ArrayList<Part> parts, Layout setup) {
		this.cockpitPosition = new Vector2(cockpitX, cockpitY);
		this.parts = parts;
		this.width = width;
		this.height = height;
		this.setup = setup;
	}

	/**
	 * When the ship is told to update, each of its parts does.
	 */
	public void update() {
		for (Part part : parts) {
			part.update();
		}
	}

	/**
	 * When a key is pressed, all triggerables associated with the key are told
	 * that one of their keys have been pressed.
	 * 
	 * @param keycode
	 *            the key which was released.
	 */
	@Override
	public boolean keyDown(int keycode) {
		if (keyActions.containsKey(keycode)) {
			for (Part trigger : keyActions.get(keycode)) {
				System.out.println("The keyaction hashmap contains the code "
						+ keycode);
				trigger.trigger(keycode);
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * When a key is released, all triggerables associated with the key are told
	 * that one of their keys has been released.
	 * 
	 * @param keycode
	 *            the key which was released.
	 */
	@Override
	public boolean keyUp(int keycode) {
		if (keyActions.containsKey(keycode)) {
			for (Part trigger : keyActions.get(keycode)) {
				trigger.unTrigger(keycode);
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Sets the world for the ship. Must be done before calling create().
	 * 
	 * @param world
	 *            The Box2D world for this ship.
	 */
	public void setWorld(World world) {
		this.world = world;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public Vector2 getCockpitPosition() {
		return this.cockpitPosition;
	}

	/**
	 * The ship is created: all of its parts are created in the world that has
	 * been set for it, the parts are welded together according to their
	 * placement in the ship editor, and all triggers are associated with their
	 * keys.
	 */
	public void create() {
		// Things that don't need to change.
		Body body;
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.fixedRotation = false;

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.restitution = 0;
		fixtureDef.friction = 0f;

		WeldJointDef weldJointDef = new WeldJointDef();

		// The cockpit is the root of the ship, and then the rest of the parts
		// are positioned accordingly.
		Cockpit cockpit = new Cockpit(parts.get(0));

		// // Test some info in the parts array
		// for (int i = 0; i < parts.size(); ++i) {
		// if (parts.get(i) != null) {
		// System.out.println(parts.get(i) + " " + parts.get(i).getGridX()
		// + " " + parts.get(i).getGridY());
		// }
		// }

		bodyDef.position.set(cockpitPosition.x, cockpitPosition.y);
		bodyDef.angle = (float) Math.toRadians(cockpit.getRotation());
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(cockpit.getWidth() / 2f, cockpit.getHeight() / 2f);
		fixtureDef.density = cockpit.getDensity();
		fixtureDef.shape = shape;
		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef).setUserData(
				new Box2DSprite(new Sprite(new Texture(cockpit.getSprite()))));
		cockpit.setBody(body);
		parts.get(0).setBody(body);
		// Need to set this part's body in the Layout for the ship as well.
		System.out.println(cockpit.getGridX() + " " + cockpit.getGridY());
		setup.getPart(cockpit.getGridX(), cockpit.getGridY()).setBody(body);
		shape.dispose();

		// Creating and attaching remaining parts to Ship.
		for (int i = 1; i < parts.size(); ++i) {
			if (parts.get(i) != null) {
				Part part = parts.get(i);
				bodyDef.position.set(part.getGridX(), part.getGridY());
				bodyDef.angle = (float) Math.toRadians(part.getRotation());
				shape = new PolygonShape();
				shape.setAsBox(part.getWidth() / 2f, part.getHeight() / 2f);
				fixtureDef.density = part.getDensity();
				fixtureDef.shape = shape;
				body = world.createBody(bodyDef);
				body.createFixture(fixtureDef).setUserData(
						new Box2DSprite(new Sprite(
								new Texture(part.getSprite()))));
				part.setBody(body);
				setup.getPart(part.getGridX(), part.getGridY()).setBody(body);
				setup.setPart(part, part.getGridX(), part.getGridY());
				shape.dispose();
			}
		}

		// Weld all adjacent parts together.
		System.out.println(parts.size());
		for (Part part : parts) {
			System.out.println("Looking at " + part + " " + part.getGridX()
					+ " " + part.getGridY());
			if (!setup.getAdjacent(part).isEmpty()) {
				System.out.println("here");
				for (Part adjacent : setup.getAdjacent(part)) {
//					if (part.getGridX() != adjacent.getGridX()
//							&& part.getGridY() != adjacent.getGridY()) {
						System.out.println(part);
						System.out.println(adjacent);
						System.out.println(part.getBody().getPosition());
						System.out.println(adjacent.getBody().getPosition());
						weldJointDef.initialize(adjacent.getBody(), part
								.getBody(), new Vector2((part.getBody()
								.getPosition().x + adjacent.getBody()
								.getPosition().x) / 2, (part.getBody()
								.getPosition().y + adjacent.getBody()
								.getPosition().y) / 2));
						world.createJoint(weldJointDef);
//					}
				}
			}
		}

		// Record triggers
		if (!parts.isEmpty()) {
			for (Part trigger : parts) {
				if (trigger.getKeys() != null) {
					for (int i = 0; i < trigger.getKeys().length; ++i) {
						if (!keyActions.containsKey(trigger.getKeys()[i])) {
							ArrayList<Part> triggerList = new ArrayList<>();
							triggerList.add(trigger);
							keyActions.put(trigger.getKeys()[i], triggerList);
						} else {
							ArrayList<Part> triggerList = keyActions
									.get(trigger.getKeys()[i]);
							triggerList.add(trigger);
							keyActions.put(trigger.getKeys()[i], triggerList);
						}
					}
				}
			}
		}
	}

	@Override
	public void write(Json json) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, JsonValue jsonData) {
		world = json.readValue(World.class, jsonData);
		cockpitPosition = json.readValue(Vector2.class, jsonData);
		width = jsonData.getInt("width");
		height = jsonData.getInt("height");
		parts = json.readValue("triggers", ArrayList.class, Part.class,
				jsonData);
		parts = json.readValue("parts", ArrayList.class, Part.class, jsonData);
		keyActions = json.readValue("keyActions", HashMap.class,
				ArrayList.class, jsonData);
	}
}
