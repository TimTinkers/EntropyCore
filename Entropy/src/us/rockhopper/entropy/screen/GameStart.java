package us.rockhopper.entropy.screen;

import net.dermetfan.utils.libgdx.graphics.Box2DSprite;
import us.rockhopper.entropy.entities.SampleShip;

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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class GameStart implements Screen {

	private World world;
	private Box2DDebugRenderer debugRenderer;
	private SpriteBatch batch;
	private OrthographicCamera camera;

	private final float TIMESTEP = 1 / 60f;
	private final int VELOCITYITERATIONS = 8, POSITIONITERATIONS = 3;

	private SampleShip ship;

	private Vector3 bottomLeft, bottomRight;

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (ship.getBody().getPosition().x < bottomLeft.x)
			ship.getBody().setTransform(bottomRight.x,
					ship.getBody().getPosition().y, ship.getBody().getAngle());
		else if (ship.getBody().getPosition().x > bottomRight.x)
			ship.getBody().setTransform(bottomLeft.x,
					ship.getBody().getPosition().y, ship.getBody().getAngle());

		ship.update();
		world.step(TIMESTEP, VELOCITYITERATIONS, POSITIONITERATIONS);

		camera.position.y = ship.getBody().getPosition().y > camera.position.y ? ship
				.getBody().getPosition().y : camera.position.y;
		camera.update();

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		Box2DSprite.draw(batch, world);
		batch.end();

		debugRenderer.render(world, camera.combined);
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

		ship = new SampleShip(world, 0, 1, 32 / 4, 25 / 4);
		world.setContactFilter(ship);
		world.setContactListener(ship);

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

		BodyDef bodyDef = new BodyDef();
		FixtureDef fixtureDef = new FixtureDef();

		// GROUND
		// body definition
		bodyDef.type = BodyType.StaticBody;
		bodyDef.position.set(0, 0);

		// ground shape
		ChainShape groundShape = new ChainShape();
		bottomLeft = new Vector3(0, Gdx.graphics.getHeight(), 0);
		bottomRight = new Vector3(Gdx.graphics.getWidth(), bottomLeft.y, 0);
		camera.unproject(bottomLeft);
		camera.unproject(bottomRight);

		groundShape.createChain(new float[] { bottomLeft.x, bottomLeft.y,
				bottomRight.x, bottomRight.y });

		// fixture definition
		fixtureDef.shape = groundShape;
		fixtureDef.friction = .5f;
		fixtureDef.restitution = 0;

		Body ground = world.createBody(bodyDef);
		ground.createFixture(fixtureDef);

		groundShape.dispose();
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