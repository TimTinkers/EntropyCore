package us.rockhopper.entropy.screen;

import javax.swing.JFileChooser;

import net.dermetfan.utils.libgdx.graphics.Box2DSprite;
import us.rockhopper.entropy.entities.BasicShip;
import us.rockhopper.entropy.utility.FileIO;
import us.rockhopper.entropy.utility.Part;
import us.rockhopper.entropy.utility.PartClassAdapter;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.google.gson.GsonBuilder;

public class GameStart implements Screen {

	private String defaultFolder = new JFileChooser().getFileSystemView()
			.getDefaultDirectory().toString();
	private World world;
	private Box2DDebugRenderer debugRenderer;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private String shipName;

	private final float TIMESTEP = 1 / 60f;
	private final int VELOCITYITERATIONS = 8, POSITIONITERATIONS = 3;

	private BasicShip ship;

	public GameStart(String shipName) {
		this.shipName = shipName;
	}

	// TODO implement DeWitters loop
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		ship.update();
		world.step(TIMESTEP, VELOCITYITERATIONS, POSITIONITERATIONS);

		camera.position.y = ship.getCockpitPosition().y > camera.position.y ? ship
				.getCockpitPosition().y : camera.position.y;
		camera.update();

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		Box2DSprite.draw(batch, world);
		batch.end();

		//debugRenderer.render(world, camera.combined);
	}

	@Override
	public void resize(int width, int height) {
		camera.viewportWidth = width / 25;
		camera.viewportHeight = height / 25;
	}

	@Override
	public void show() {
		if (Gdx.app.getType() == ApplicationType.Desktop)
			Gdx.graphics.setDisplayMode(
					(int) (Gdx.graphics.getHeight() / 1.5f),
					Gdx.graphics.getHeight(), false);

		world = new World(new Vector2(0, 0), true);
		debugRenderer = new Box2DDebugRenderer();
		batch = new SpriteBatch();

		camera = new OrthographicCamera(Gdx.graphics.getWidth() / 25,
				Gdx.graphics.getHeight() / 25);

		// Deserialize and get ship from file
		String filePath = defaultFolder + "\\EntropyShips\\" + shipName
				+ ".json";
		String shipJSON = FileIO.read(filePath);
		GsonBuilder gson = new GsonBuilder();
		gson.registerTypeAdapter(Part.class, new PartClassAdapter());
		ship = gson.create().fromJson(shipJSON, BasicShip.class);

		ship.setWorld(world);
		ship.create();
		Gdx.input.setInputProcessor(new InputMultiplexer(new InputAdapter() {

			@Override
			public boolean keyDown(int keycode) {
				switch (keycode) {
				case Keys.ESCAPE:
					((Game) Gdx.app.getApplicationListener())
							.setScreen(new MainMenu());
					break;
				}
				return false;
			}

			@Override
			public boolean scrolled(int amount) {
				camera.zoom += amount / 25f;
				return true;
			}

		}, ship));
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		world.dispose();
		debugRenderer.dispose();
	}

}