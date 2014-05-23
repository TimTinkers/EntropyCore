package us.rockhopper.entropy.screen;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import us.rockhopper.entropy.entities.BasicShip;
import us.rockhopper.entropy.entities.Cockpit;
import us.rockhopper.entropy.entities.Gyroscope;
import us.rockhopper.entropy.entities.Thruster;
import us.rockhopper.entropy.gui.PartImageButton;
import us.rockhopper.entropy.utility.FileIO;
import us.rockhopper.entropy.utility.Layout;
import us.rockhopper.entropy.utility.Part;
import us.rockhopper.entropy.utility.PartClassAdapter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.google.gson.GsonBuilder;

public class ShipEditor implements Screen {

	private Stage stage;
	private Skin skin;
	private OrthographicCamera camera;
	private Sprite overlay;
	private SpriteBatch batch;

	private Table selections;
	private Table tabbed;

	private String defaultFolder = new JFileChooser().getFileSystemView()
			.getDefaultDirectory().toString();

	private ArrayList<Part> command = new ArrayList<Part>();
	private ArrayList<Part> control = new ArrayList<Part>();
	private ArrayList<Part> thrust = new ArrayList<Part>();

	private Part activePart;
	private float activePartX, activePartY;
	private int sWidth, sHeight;

	ArrayList<Part> parts = new ArrayList<Part>();

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();

		camera.update();

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		if (activePart != null) {
			Sprite sprite = (Sprite) new Sprite(new Texture(
					"assets/img/overlay.png"));
			sprite.setPosition(activePartX - sWidth / 2, activePartY - sHeight
					/ 2);
			sprite.draw(batch);
		}
		batch.end();

		Table.drawDebug(stage);
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
		selections.invalidateHierarchy();
		tabbed.invalidateHierarchy();
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		sWidth = width;
		sHeight = height;
	}

	@Override
	public void show() {
		// Sprite rendering
		batch = new SpriteBatch();
		camera = new OrthographicCamera(Gdx.graphics.getWidth() / 25,
				Gdx.graphics.getHeight() / 25);

		// Load all parts into their appropriate arrays.
		String commandPath = defaultFolder + "\\EntropyShips\\Parts\\Command\\";
		for (File file : FileIO.getFilesForFolder(new File(commandPath))) {
			String partJSON = FileIO.read(file.getAbsolutePath());
			GsonBuilder gson = new GsonBuilder();
			gson.registerTypeAdapter(Part.class, new PartClassAdapter());
			Part part = gson.create().fromJson(partJSON, Part.class);
			command.add(part);
		}

		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		skin = new Skin(Gdx.files.internal("assets/ui/uiskin.json"),
				new TextureAtlas("assets/ui/uiskin.pack"));

		selections = new Table(skin);
		selections.setFillParent(true);
		selections.debug();
		tabbed = new Table(skin);

		final TextButton buttonCommand = new TextButton("Command", skin,
				"default");
		final TextButton buttonControl = new TextButton("Control", skin,
				"default");
		final TextButton buttonThrust = new TextButton("Thrust", skin,
				"default");

		final TextButton buttonGo = new TextButton("Start", skin, "default");

		final ClickListener itemChooseListener = new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				PartImageButton active = (PartImageButton) event
						.getListenerActor();
				if (active.isChecked()) {
					activePart = active.getPart();
					System.out.println("On! " + active.getPart().getName());
					Vector2 buttonCoords = active
							.localToStageCoordinates(new Vector2(0, 0));
					activePartX = buttonCoords.x;
					activePartY = buttonCoords.y;
					System.out.println(activePartX + " " + activePartY);
				} else {
					System.out.println("Off!");
				}
			}
		};

		ClickListener buttonListener = new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (event.getListenerActor() == buttonCommand) {
					tabbed.clear();
					for (int i = 0; i < command.size(); ++i) {
						Part part = command.get(i);
						TextureRegion image = new TextureRegion(new Texture(
								part.getSprite()));
						ImageButtonStyle style = new ImageButtonStyle(
								skin.get(ImageButtonStyle.class));
						style.imageUp = new TextureRegionDrawable(image);
						PartImageButton selectPart = new PartImageButton(style,
								part);
						selectPart.addListener(itemChooseListener);
						tabbed.add(selectPart);
					}
				} else if (event.getListenerActor() == buttonControl) {
					tabbed.clear();
					TextButton buttonGo = new TextButton("Start", skin,
							"default");
					tabbed.add(buttonGo);
				} else if (event.getListenerActor() == buttonThrust) {
					tabbed.clear();

					// Instantiate the ship and move onto the next screen.
				} else if (event.getListenerActor() == buttonGo) {
					// Declare textures
					Cockpit partCockpit = new Cockpit(new Vector2(1, 3), 1, 1,
							0.8f, "assets/img/sampleShip.png");
					Gyroscope partGyro = new Gyroscope(new Vector2(1, 2), 1, 1,
							0.8f, "assets/img/gyroscope.png")
							.setClockwise(Keys.D).setCounterClockwise(Keys.A)
							.setStrength(5);
					Thruster partThruster = new Thruster(new Vector2(1, 0), 1,
							1, 0.8f, "assets/img/thruster.png")
							.setForward(Keys.W).setReverse(Keys.S)
							.setCanReverse(true).setStrength(1);
					Thruster partThruster1 = new Thruster(new Vector2(1, 1), 1,
							1, 0.8f, "assets/img/thruster.png")
							.setForward(Keys.W).setReverse(Keys.S)
							.setCanReverse(true).setStrength(1);
					parts.add(partCockpit);
					parts.add(partGyro);
					parts.add(partThruster);
					parts.add(partThruster1);
					Layout setup = new Layout(2, 4);
					setup.setPart(partCockpit, partCockpit.getGridX(),
							partCockpit.getGridY());
					setup.setPart(partGyro, partGyro.getGridX(),
							partGyro.getGridY());
					setup.setPart(partThruster1, partThruster1.getGridX(),
							partThruster1.getGridY());
					setup.setPart(partThruster, partThruster.getGridX(),
							partThruster.getGridY());
					BasicShip ship = new BasicShip(new Vector2(1, 3), 1, 3,
							parts, setup);

					// Serialize and write to file
					GsonBuilder gson = new GsonBuilder();
					gson.registerTypeAdapter(Part.class, new PartClassAdapter());
					String shipJSON = gson.setPrettyPrinting().create()
							.toJson(ship);
					FileIO.write(defaultFolder + "\\EntropyShips\\"
							+ "sample.json", shipJSON);

					// Switch screens
					((Game) Gdx.app.getApplicationListener())
							.setScreen(new GameStart());
				}
			}
		};

		buttonCommand.addListener(buttonListener);
		buttonControl.addListener(buttonListener);
		buttonThrust.addListener(buttonListener);
		buttonGo.addListener(buttonListener);

		selections.left().top();
		selections.add(buttonCommand);
		selections.add(buttonControl);
		selections.add(buttonThrust);
		selections.add(buttonGo);
		selections.row();
		selections.add(tabbed).colspan(3);

		stage.addActor(selections);

		stage.addAction(sequence(moveTo(0, stage.getHeight()),
				moveTo(0, 0, .5f))); // coming in from top animation
	}

	@Override
	public void hide() {
		dispose();
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
		stage.dispose();
		skin.dispose();
	}
}
