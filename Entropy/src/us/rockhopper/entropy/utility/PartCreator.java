package us.rockhopper.entropy.utility;

import us.rockhopper.entropy.entities.Hull;

import com.badlogic.gdx.math.Vector2;
import com.google.gson.GsonBuilder;

public class PartCreator {
	public static void main(String[] args) {
		Hull part = new Hull(new Vector2(1, 3), 1, 1, 2f,
				"assets/img/Gyroscope.png");
		part.setCost(100)
				.setHealth(10)
				.setName("Torque Wheel")
				.setDescription(
						"Extremely fragile piece which allows your ship to rotate about it.")
				.setAttachmentNodes(new int[] { 0, 1, 2, 3 });

		// Serialize and write to file
		GsonBuilder gson = new GsonBuilder();
		gson.registerTypeAdapter(Part.class, new PartClassAdapter());
		String partJSON = gson.setPrettyPrinting().create().toJson(part);
		FileIO.write(
				"C:\\Users\\Timothy\\Documents\\EntropyShips\\Parts\\Control\\"
						+ part.getName() + ".json", partJSON);
	}
}
