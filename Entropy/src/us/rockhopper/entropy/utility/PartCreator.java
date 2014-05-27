package us.rockhopper.entropy.utility;

import us.rockhopper.entropy.entities.Hull;

import com.badlogic.gdx.math.Vector2;
import com.google.gson.GsonBuilder;

public class PartCreator {
	public static void main(String[] args) {
		Hull part = new Hull(new Vector2(1, 3), 1, 1, 3f,
				"assets/img/ShieldModule.png");
		part.setCost(50)
				.setHealth(200)
				.setName("Shield Module")
				.setDescription(
						"Absorbs damage which would otherwise tear your ship to smithereens.")
				.setAttachmentNodes(new int[] { 3 });

		// Serialize and write to file
		GsonBuilder gson = new GsonBuilder();
		gson.registerTypeAdapter(Part.class, new PartClassAdapter());
		String partJSON = gson.setPrettyPrinting().create().toJson(part);
		FileIO.write(
				"C:\\Users\\Timothy\\Documents\\EntropyShips\\Parts\\Hull\\"
						+ part.getName() + ".json", partJSON);
	}
}
