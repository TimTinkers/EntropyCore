package us.rockhopper.entropy.utility;

import us.rockhopper.entropy.entities.Thruster;

import com.badlogic.gdx.math.Vector2;
import com.google.gson.GsonBuilder;

public class PartCreator {
	public static void main(String[] args) {
		Thruster part = new Thruster(new Vector2(1, 3), 1, 1, 2f,
				"assets/img/Thruster.png");
		part.setCost(10).setHealth(30).setName("Thruster")
				.setDescription("Propels your ship.")
				.setAttachmentNodes(new int[] { 0 });

		// Serialize and write to file
		GsonBuilder gson = new GsonBuilder();
		gson.registerTypeAdapter(Part.class, new PartClassAdapter());
		String partJSON = gson.setPrettyPrinting().create().toJson(part);
		FileIO.write(
				"C:\\Users\\Timothy\\Documents\\EntropyShips\\Parts\\Thrust\\"
						+ part.getName() + ".json", partJSON);
	}
}
