package us.rockhopper.entropy.screen;

import net.dermetfan.utils.libgdx.graphics.Box2DSprite;
import us.rockhopper.entropy.entities.Ship;
import us.rockhopper.entropy.utility.FileIO;
import us.rockhopper.entropy.utility.Part;
import us.rockhopper.entropy.utility.PartClassAdapter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.google.gson.GsonBuilder;

public class TestFlight extends ScreenAdapter {

	private TiledDrawable background;

	private String defaultFolder = "data";
	private World world;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private String shipName;

	private final float TIMESTEP = 1 / 60f;
	private final int VELOCITYITERATIONS = 8, POSITIONITERATIONS = 3;

	private float accumulator;

	private Ship ship;

	public TestFlight(String shipName) {
		this.shipName = shipName;
	}

	@Override
	public void render(float delta) {
		delta = MathUtils.clamp(delta, 0, 0.030f);
		accumulator += delta;

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		while (accumulator > TIMESTEP) {
			accumulator -= TIMESTEP;
			ship.update();
			world.step(TIMESTEP, VELOCITYITERATIONS, POSITIONITERATIONS);
		}

		camera.position.y = ship.getCockpitPosition().y;
		camera.position.x = ship.getCockpitPosition().x;
		camera.update();

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		// Draw background
		background.draw(batch, -Gdx.graphics.getWidth() / 2, -Gdx.graphics.getHeight() / 2,
				Gdx.graphics.getWidth() * 8, Gdx.graphics.getHeight() * 8);
		Box2DSprite.draw(batch, world);
		batch.end();
	}

	@Override
	public void resize(int width, int height) {
		camera.viewportWidth = width / 25f;
		camera.viewportHeight = height / 25f;
	}

	@Override
	public void show() {
		background = new TiledDrawable(new TextureRegion(new Texture("assets/img/grid.png")));

		world = new World(new Vector2(0, 0), true);
		batch = new SpriteBatch();

		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		// Deserialize and get ship from file
		String filePath = defaultFolder + "/ships/" + shipName + ".json";
		String shipJSON = FileIO.read(filePath);
		GsonBuilder gson = new GsonBuilder();
		gson.registerTypeAdapter(Part.class, new PartClassAdapter());
		ship = gson.create().fromJson(shipJSON, Ship.class);
		ship.setWorld(world);
		ship.create();
		ship.release();

		Gdx.input.setInputProcessor(new InputMultiplexer(new InputAdapter() {

			@Override
			public boolean keyDown(int keycode) {
				switch (keycode) {
				case Keys.ESCAPE:
					((Game) Gdx.app.getApplicationListener()).setScreen(new MainMenu());
					break;
				}
				return false;
			}

			@Override
			public boolean scrolled(int amount) {
				camera.zoom += amount / 60f;
				return true;
			}

		}, ship));
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void dispose() {
		world.dispose();
	}
}