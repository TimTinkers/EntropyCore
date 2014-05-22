package us.rockhopper.entropy.screen;

import java.util.ArrayList;

import javax.swing.JFileChooser;

import us.rockhopper.entropy.entities.BasicShip;
import us.rockhopper.entropy.entities.Cockpit;
import us.rockhopper.entropy.entities.Thruster;
import us.rockhopper.entropy.utility.FileIO;
import us.rockhopper.entropy.utility.Layout;
import us.rockhopper.entropy.utility.Part;
import us.rockhopper.entropy.utility.PartClassAdapter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.google.gson.GsonBuilder;

public class ShipEditor implements Screen {

	ArrayList<Part> parts = new ArrayList<Part>();
	private String defaultFolder = new JFileChooser().getFileSystemView()
			.getDefaultDirectory().toString();

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
	}

	@Override
	public void show() {
		// Declare textures
		Cockpit partCockpit = new Cockpit(new Vector2(1, 2), 1, 1, 0.8f,
				"assets/img/sampleShip.png");
		Thruster partThruster = new Thruster(new Vector2(1, 1), 1, 1, 0.8f,
				"assets/img/thruster.png").setForward(Keys.W)
				.setReverse(Keys.S).setCanReverse(true).setStrength(5);
		parts.add(partCockpit);
		parts.add(partThruster);
		Layout setup = new Layout(4, 4);
		setup.setPart(partCockpit, partCockpit.getGridX(),
				partCockpit.getGridY());
		setup.setPart(partThruster, partThruster.getGridX(),
				partThruster.getGridY());
		BasicShip ship = new BasicShip(new Vector2(1, 2), 1, 2, parts, setup);

		// Serialize and write to file
		GsonBuilder gson = new GsonBuilder();
		gson.registerTypeAdapter(Part.class, new PartClassAdapter());
		String shipJSON = gson.setPrettyPrinting().create().toJson(ship);
		FileIO.write(defaultFolder + "\\EntropyShips\\" + "sample.json",
				shipJSON);

		// Switch screens
		((Game) Gdx.app.getApplicationListener()).setScreen(new GameStart());
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}
}
