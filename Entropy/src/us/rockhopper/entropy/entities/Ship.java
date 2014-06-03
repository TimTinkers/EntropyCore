package us.rockhopper.entropy.entities;

import java.util.ArrayList;
import java.util.HashMap;

import net.dermetfan.utils.libgdx.graphics.Box2DSprite;
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
public class Ship extends InputAdapter implements Json.Serializable {

	private String name;
	private World world;
	private ArrayList<Part> parts = new ArrayList<Part>();
	private HashMap<Integer, ArrayList<Part>> keyActions = new HashMap<Integer, ArrayList<Part>>();

	/**
	 * Creates a ship object, which contains all information it would need to later render itself.
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
	 *            All parts on the ship. The first Part in this list MUST be the Cockpit of the ship.
	 */
	public Ship(String name, ArrayList<Part> parts) {
		this.parts = parts;
		this.name = name;
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
	 * When a key is pressed, all triggerables associated with the key are told that one of their keys have been
	 * pressed.
	 * 
	 * @param keycode
	 *            the key which was released.
	 */
	@Override
	public boolean keyDown(int keycode) {
		if (keyActions.containsKey(keycode)) {
			for (Part trigger : keyActions.get(keycode)) {
				System.out.println("The keyaction hashmap contains the code " + keycode);
				trigger.trigger(keycode);
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * When a key is released, all triggerables associated with the key are told that one of their keys has been
	 * released.
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

	public Vector2 getCockpitPosition() {
		return new Vector2(parts.get(0).getBody().getPosition().x, parts.get(0).getBody().getPosition().y);
	}

	/**
	 * The ship is created: all of its parts are created in the world that has been set for it, the parts are welded
	 * together according to their placement in the ship editor, and all triggers are associated with their keys.
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

		bodyDef.angle = (float) Math.toRadians(cockpit.getRotation());
		// Reposition the image.
		int rotIndex = (int) (Math.abs(cockpit.getRotation()) / 90) % 4;
		if ((cockpit.getWidth() != cockpit.getHeight()) && (rotIndex == 1 || rotIndex == 3)) {
			bodyDef.position.set(cockpit.getGridX() + (2f * cockpit.getWidth()),
					cockpit.getGridY() + (cockpit.getHeight() * 0.75f));
		} else {
			bodyDef.position.set(cockpit.getGridX() + cockpit.getWidth() / 2f, cockpit.getGridY() + cockpit.getHeight()
					/ 2f);
		}

		System.out.println("Cockpit with info: " + cockpit.getGridX() + " " + cockpit.getGridY() + " rotation "
				+ cockpit.getRotation());
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(cockpit.getWidth() / 2f, cockpit.getHeight() / 2f);
		fixtureDef.density = cockpit.getDensity();
		fixtureDef.shape = shape;
		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef).setUserData(new Box2DSprite(new Sprite(new Texture(cockpit.getSprite()))));
		cockpit.setBody(body);
		parts.get(0).setBody(body);
		shape.dispose();

		// Creating and attaching remaining parts to Ship.
		for (int i = 1; i < parts.size(); ++i) {
			if (parts.get(i) != null) {
				Part part = parts.get(i);
				bodyDef.angle = (float) Math.toRadians(part.getRotation());
				// Reposition the image.
				rotIndex = (int) (Math.abs(part.getRotation()) / 90) % 4;
				if ((part.getWidth() != part.getHeight()) && (rotIndex == 1)) {
					bodyDef.position.set(part.getGridX() + (part.getWidth()), part.getGridY()
							+ (part.getHeight() * 0.25f));
				} else if ((part.getWidth() != part.getHeight()) && (rotIndex == 3)) {
					bodyDef.position.set(part.getGridX() + (part.getWidth() / 2f), part.getGridY()
							+ (part.getHeight() * 0.5f));
				} else {
					bodyDef.position.set(part.getGridX() + part.getWidth() / 2f, part.getGridY() + part.getHeight()
							/ 2f);
				}

				shape = new PolygonShape();
				shape.setAsBox(part.getWidth() / 2f, part.getHeight() / 2f);
				fixtureDef.density = part.getDensity();
				fixtureDef.shape = shape;
				body = world.createBody(bodyDef);
				body.createFixture(fixtureDef).setUserData(new Box2DSprite(new Sprite(new Texture(part.getSprite()))));
				part.setBody(body);
				shape.dispose();
			}
		}

		// Weld all adjacent parts together.
		System.out.println(parts.size());
		for (Part part : parts) {
			System.out.println("Looking at " + part + " " + part.getGridX() + " " + part.getGridY());
			for (int i = 0; i < 4; ++i) {
				ArrayList<Part> adjacents = getAdjacent(part, i);
				if (!adjacents.isEmpty()) {
					for (Part adjacent : adjacents) {
						System.out.println(part);
						System.out.println(adjacent);
						System.out.println(part.getBody().getPosition());
						System.out.println(adjacent.getBody().getPosition());
						weldJointDef.initialize(adjacent.getBody(), part.getBody(), new Vector2((part.getBody()
								.getPosition().x + adjacent.getBody().getPosition().x) / 2, (part.getBody()
								.getPosition().y + adjacent.getBody().getPosition().y) / 2));
						world.createJoint(weldJointDef);
					}
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
							ArrayList<Part> triggerList = keyActions.get(trigger.getKeys()[i]);
							triggerList.add(trigger);
							keyActions.put(trigger.getKeys()[i], triggerList);
						}
					}
				}
			}
		}
	}

	public String getName() {
		return this.name;
	}

	private ArrayList<Part> getAdjacent(Part basePart, int direction) {
		// Instantiate the list
		ArrayList<Part> list = new ArrayList<>();
		list.clear();
		// Get grid-Width and grid-Height
		Texture texture = new Texture(basePart.getSprite());
		int rotIndex = (int) (Math.abs(basePart.getRotation()) / 90) % 4;
		int tilesX = 0;
		int tilesY = 0;
		if (rotIndex == 1 || rotIndex == 3) {
			System.out.println();
			tilesX = (int) texture.getHeight() / 16;
			tilesY = (int) texture.getWidth() / 16;
		} else {
			tilesX = (int) texture.getWidth() / 16;
			tilesY = (int) texture.getHeight() / 16;
		}

		int gridX = basePart.getGridX();
		int gridY = basePart.getGridY();

		// Find all grid tiles along the given edge.
		switch (direction) {
		case 0:
			// For the entire width of the image
			for (int i = 0; i < tilesX; ++i) {
				// x is each x grid
				int adjacentX = gridX + i;
				// for every image thus far
				for (int j = 0; j < parts.size(); ++j) {
					Part partImage = parts.get(j);
					// if it overlaps any of the points along the top
					// edge, add it to the list of images adjacent in
					// that direction
					ArrayList<Vector2> occupied = partImage.getOccupiedCells();
					if (occupied.contains(new Vector2(adjacentX, (gridY + tilesY))) && !list.contains(partImage)) {
						list.add(partImage);
					}
				}
			}
			break;
		case 1:
			// For the entire height of the image
			for (int i = 0; i < tilesY; ++i) {
				// y is each y grid
				int adjacentY = gridY + i;
				// for every image thus far
				for (int j = 0; j < parts.size(); ++j) {
					Part partImage = parts.get(j);
					// if it overlaps any of the points along the right
					// edge, add it to the list of images adjacent in
					// that direction
					ArrayList<Vector2> occupied = partImage.getOccupiedCells();
					if (occupied.contains(new Vector2(gridX + tilesX, adjacentY)) && !list.contains(partImage)) {
						list.add(partImage);
					}
				}
			}
			break;
		case 2:
			// For the entire width of the image
			for (int i = 0; i < tilesX; ++i) {
				// x is each x grid
				int adjacentX = gridX + i;
				// for every image thus far
				for (int j = 0; j < parts.size(); ++j) {
					Part partImage = parts.get(j);
					// if it overlaps any of the points along the bottom
					// edge, add it to the list of images adjacent in
					// that direction
					ArrayList<Vector2> occupied = partImage.getOccupiedCells();
					if (occupied.contains(new Vector2(adjacentX, gridY - 1)) && !list.contains(partImage)) {
						list.add(partImage);
					}
				}
			}
			break;
		case 3:
			// For the entire height of the image
			for (int i = 0; i < tilesY; ++i) {
				// y is each y grid
				int adjacentY = gridY + i;
				// for every image thus far
				for (int j = 0; j < parts.size(); ++j) {
					Part partImage = parts.get(j);
					// if it overlaps any of the points along the left
					// edge, add it to the list of images adjacent in
					// that direction
					ArrayList<Vector2> occupied = partImage.getOccupiedCells();
					if (occupied.contains(new Vector2(gridX - 1, adjacentY)) && !list.contains(partImage)) {
						list.add(partImage);
					}
				}
			}
		}
		return list;
	}

	public ArrayList<Part> getParts() {
		return this.parts;
	}

	@Override
	public void write(Json json) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, JsonValue jsonData) {
		world = json.readValue(World.class, jsonData);
		parts = json.readValue("triggers", ArrayList.class, Part.class, jsonData);
		parts = json.readValue("parts", ArrayList.class, Part.class, jsonData);
		keyActions = json.readValue("keyActions", HashMap.class, ArrayList.class, jsonData);
	}
}
