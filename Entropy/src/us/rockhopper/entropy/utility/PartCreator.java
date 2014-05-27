package us.rockhopper.entropy.utility;

import us.rockhopper.entropy.entities.Hull;

import com.badlogic.gdx.math.Vector2;
import com.google.gson.GsonBuilder;

public class PartCreator {
	public static void main(String[] args) {
		Hull part = new Hull(new Vector2(1, 3), 1, 1, 10f,
				"assets/img/LargeMissileLauncher.png");
		part.setCost(75)
				.setHealth(100)
				.setName("Large Missile Launcher")
				.setDescription(
						"Heavy and expensive, but packs a powerful punch.")
				.setAttachmentNodes(new int[] { 2 });

		// Serialize and write to file
		GsonBuilder gson = new GsonBuilder();
		gson.registerTypeAdapter(Part.class, new PartClassAdapter());
		String partJSON = gson.setPrettyPrinting().create().toJson(part);
		FileIO.write(
				"C:\\Users\\Timothy\\Documents\\EntropyShips\\Parts\\Weaponry\\"
						+ part.getName() + ".json", partJSON);
	}
}
