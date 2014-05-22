package us.rockhopper.entropy.screen;

import java.util.ArrayList;

import javax.swing.JFileChooser;

import us.rockhopper.entropy.entities.BasicShip;
import us.rockhopper.entropy.utility.FileIO;
import us.rockhopper.entropy.utility.Layout;
import us.rockhopper.entropy.utility.Part;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.google.gson.Gson;

public class ShipEditor implements Screen {

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
		Part partCockpit = new Part(new Vector2(0, 1), 32 / 4, 32 / 4, 0.8f,
				"assets/img/sampleShip.png");
		Part partThruster = new Part(new Vector2(0, 0), 32 / 4, 32 / 4,
				0.8f, "assets/img/thruster.png");
		ArrayList<Part> parts = new ArrayList<>();
		parts.add(partCockpit);
		parts.add(partThruster);
		Layout setup = new Layout(1, 2);
		setup.setPart(partCockpit, 0, 1);
		setup.setPart(partThruster, 0, 0);
		BasicShip ship = new BasicShip(new Vector2(0, 0), 32 / 2, 32 / 2,
				parts, setup);

		// Gson gson = new
		// GsonBuilder().registerTypeHierarchyAdapter(BasicShip.class, new
		// InterfaceAdapter<TextureData>())
		// .create();
		Gson gson = new Gson();
		String shipJSON = gson.toJson(ship);
		FileIO.write(defaultFolder + "\\EntropyShips\\" + "sample.json",
				shipJSON);
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
