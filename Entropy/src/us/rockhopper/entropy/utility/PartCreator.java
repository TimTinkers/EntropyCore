package us.rockhopper.entropy.utility;

import us.rockhopper.entropy.entities.Thruster;

import com.google.gson.GsonBuilder;

public class PartCreator {
	public static void main(String[] args) {
		Thruster part = new Thruster(0, 0, 2, 2, 4f, "assets/img/RightTwinThruster.png");
		part.setCost(50).setHealth(100).setName("Right Twin Thruster").setDescription("For bidirectional propulsion.")
				.setAttachmentNodes(new int[] { 3 });
		part.setCanReverse(true);
		part.setStrength(5);

		// Serialize and write to file
		GsonBuilder gson = new GsonBuilder();
		gson.registerTypeAdapter(Part.class, new PartClassAdapter());
		String partJSON = gson.setPrettyPrinting().create().toJson(part);
		FileIO.write("C:\\Users\\Timothy\\Documents\\EntropyShips\\Parts\\Thrust\\" + part.getName() + ".json",
				partJSON);
	}
}
