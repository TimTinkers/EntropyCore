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
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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
	private SpriteBatch batch;

	private Table selections;
	private Table tabbed;
	private Table gridTable;
	private Table info;

	private String defaultFolder = new JFileChooser().getFileSystemView()
			.getDefaultDirectory().toString();

	private ArrayList<Part> command = new ArrayList<Part>();
	private ArrayList<Part> control = new ArrayList<Part>();
	private ArrayList<Part> thrust = new ArrayList<Part>();
	private ArrayList<Part> hull = new ArrayList<Part>();

	private Part activePart;
	private float activePartX, activePartY;
	private int sWidth, sHeight;

	ArrayList<PartImageButton> grid = new ArrayList<PartImageButton>();
	ArrayList<Part> parts = new ArrayList<Part>();
	private Layout setup;

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

	// TODO add the ability to rotate parts
	@Override
	public void show() {
		// Begin initializing the layout.
		setup = new Layout(1, 1);

		// Sprite rendering
		batch = new SpriteBatch();
		camera = new OrthographicCamera(Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());

		// Load all command parts into its list.
		String commandPath = defaultFolder + "\\EntropyShips\\Parts\\Command\\";
		for (File file : FileIO.getFilesForFolder(new File(commandPath))) {
			String partJSON = FileIO.read(file.getAbsolutePath());
			GsonBuilder gson = new GsonBuilder();
			gson.registerTypeAdapter(Part.class, new PartClassAdapter());
			Part part = gson.create().fromJson(partJSON, Part.class);
			command.add(part);
		}

		// Load all control parts into its list.
		String controlPath = defaultFolder + "\\EntropyShips\\Parts\\Control\\";
		for (File file : FileIO.getFilesForFolder(new File(controlPath))) {
			String partJSON = FileIO.read(file.getAbsolutePath());
			GsonBuilder gson = new GsonBuilder();
			gson.registerTypeAdapter(Part.class, new PartClassAdapter());
			Part part = gson.create().fromJson(partJSON, Part.class);
			control.add(part);
		}

		// Load all thrust parts into its list.
		String thrustPath = defaultFolder + "\\EntropyShips\\Parts\\Thrust\\";
		for (File file : FileIO.getFilesForFolder(new File(thrustPath))) {
			String partJSON = FileIO.read(file.getAbsolutePath());
			GsonBuilder gson = new GsonBuilder();
			gson.registerTypeAdapter(Part.class, new PartClassAdapter());
			Part part = gson.create().fromJson(partJSON, Part.class);
			thrust.add(part);
		}

		// Load all hull parts into its list.
		String hullPath = defaultFolder + "\\EntropyShips\\Parts\\Hull\\";
		for (File file : FileIO.getFilesForFolder(new File(hullPath))) {
			String partJSON = FileIO.read(file.getAbsolutePath());
			GsonBuilder gson = new GsonBuilder();
			gson.registerTypeAdapter(Part.class, new PartClassAdapter());
			Part part = gson.create().fromJson(partJSON, Part.class);
			hull.add(part);
		}

		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		skin = new Skin(Gdx.files.internal("assets/ui/uiskin.json"),
				new TextureAtlas("assets/ui/uiskin.pack"));

		selections = new Table(skin);
		gridTable = new Table(skin);
		info = new Table(skin);
		tabbed = new Table(skin);
		gridTable.setFillParent(true);
		selections.setFillParent(true);
		selections.debug();

		final TextButton buttonCommand = new TextButton("Command", skin,
				"default");
		final TextButton buttonControl = new TextButton("Control", skin,
				"default");
		final TextButton buttonThrust = new TextButton("Thrust", skin,
				"default");
		final TextButton buttonHull = new TextButton("Hull", skin, "default");
		final TextButton buttonGo = new TextButton("Start", skin, "default");

		final ClickListener partAddListener = new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (activePart != null) {
					if (grid.size() == 1 && !(activePart instanceof Cockpit)) {
						new Dialog("", skin) {
							{
								text("The first piece on your ship must be a command module.");
								button("Okay");
							}
						}.show(stage);
					} else {
						PartImageButton active = (PartImageButton) event
								.getListenerActor();
						for (int j = 0; j < activePart.getAttachmentNodes().length; ++j) {
							int[] nodes = activePart.getAttachmentNodes();
							// Add additional ship piece slots.
							ImageButtonStyle style = new ImageButtonStyle(
									skin.get(ImageButtonStyle.class));
							style.imageUp = new TextureRegionDrawable(
									new TextureRegion(new Texture(
											"assets/img/grid.png")));
							PartImageButton extra = new PartImageButton(style,
									null);
							for (int i = 0; i < active.getListeners().size; ++i) {
								extra.addListener(active.getListeners().get(i));
							}
							Vector2 buttonCoords = active
									.localToStageCoordinates(new Vector2(0, 0));
							if (nodes[j] == 0 && !hasAdjacent(grid, active, 0)) {
								extra.setPosition(buttonCoords.x,
										buttonCoords.y + active.getHeight());
								grid.add(extra);
								stage.addActor(extra);
							} else if (nodes[j] == 1
									&& !hasAdjacent(grid, active, 1)) {
								extra.setPosition(
										buttonCoords.x + active.getWidth(),
										buttonCoords.y);
								grid.add(extra);
								stage.addActor(extra);
							} else if (nodes[j] == 2
									&& !hasAdjacent(grid, active, 2)) {
								extra.setPosition(buttonCoords.x,
										buttonCoords.y - active.getHeight());
								grid.add(extra);
								stage.addActor(extra);
							} else if (nodes[j] == 3
									&& !hasAdjacent(grid, active, 3)) {
								extra.setPosition(
										buttonCoords.x - active.getWidth(),
										buttonCoords.y);
								grid.add(extra);
								stage.addActor(extra);
							}

							// Modify the existing button
							active.setPart(activePart);
							active.getStyle().imageUp = new TextureRegionDrawable(
									new TextureRegion(new Texture(active
											.getPart().getSprite())));
						}
					}
				}
			}
		};

		final ClickListener itemChooseListener = new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				PartImageButton active = (PartImageButton) event
						.getListenerActor();
				activePart = active.getPart();
				Vector2 buttonCoords = active
						.localToStageCoordinates(new Vector2(0, 0));
				activePartX = buttonCoords.x;
				activePartY = buttonCoords.y;
				info.clear();
				Label heading = new Label(activePart.getName(), skin, "default");
				Label description = new Label(activePart.getDescription(),
						skin, "default");
				description.setWrap(true);
				info.add(heading);
				info.row();
				info.add("Cost: " + activePart.getCost());
				info.row();
				info.add(description).width(200).center();
			}
		};

		ClickListener buttonListener = new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (event.getListenerActor() == buttonCommand) {
					tabbed.clear();
					info.clear();
					activePart = null;
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
					info.clear();
					activePart = null;
					for (int i = 0; i < control.size(); ++i) {
						Part part = control.get(i);
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
				} else if (event.getListenerActor() == buttonThrust) {
					tabbed.clear();
					info.clear();
					activePart = null;
					for (int i = 0; i < thrust.size(); ++i) {
						Part part = control.get(i);
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
				} else if (event.getListenerActor() == buttonHull) {
					tabbed.clear();
					info.clear();
					activePart = null;
					for (int i = 0; i < hull.size(); ++i) {
						Part part = hull.get(i);
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
				}
				// Instantiate the ship and move onto the next screen.
				else if (event.getListenerActor() == buttonGo) {
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
		buttonHull.addListener(buttonListener);
		buttonGo.addListener(buttonListener);
		selections.left().top();
		selections.add(buttonCommand);
		selections.add(buttonControl);
		selections.add(buttonThrust);
		selections.add(buttonHull);
		selections.add(buttonGo);
		selections.row();
		selections.add(tabbed).colspan(4);
		selections.row();
		selections.add(info).colspan(4);

		// Add the initial ship slot
		TextureRegion image = new TextureRegion(new Texture(
				"assets/img/grid.png"));
		ImageButtonStyle style = new ImageButtonStyle(
				skin.get(ImageButtonStyle.class));
		style.imageUp = new TextureRegionDrawable(image);
		PartImageButton addPart = new PartImageButton(style, null);
		addPart.addListener(partAddListener);
		gridTable.add(addPart).center();
		grid.add(addPart);
		stage.addActor(gridTable);
		stage.addActor(selections);

		stage.addAction(sequence(moveTo(0, stage.getHeight()),
				moveTo(0, 0, .5f))); // coming in from top animation
	}

	/**
	 * Returns whether or not the PartImageButton in the grid has an adjacent
	 * PartImageButton in the given direction.
	 * 
	 * @param grid
	 *            The grid to search for parts in.
	 * @param active
	 *            The PartImageButton of the grid to target.
	 * @param direction
	 *            The direction to search for adjacent buttons--0 for up, 1 for
	 *            right, 2 for down, 3 for left.
	 * @return Whether or not the piece has an adjacent piece.
	 */
	protected boolean hasAdjacent(ArrayList<PartImageButton> grid,
			PartImageButton active, int direction) {
		boolean result = false;
		Vector2 activeCoords = active
				.localToStageCoordinates(new Vector2(0, 0));
		for (int i = 0; i < grid.size(); ++i) {
			PartImageButton button = grid.get(i);
			Vector2 buttonCoords = button.localToStageCoordinates(new Vector2(
					0, 0));
			if (direction == 1
					&& activeCoords.x + button.getWidth() == buttonCoords.x
					&& activeCoords.y == buttonCoords.y) {
				result = true;
			} else if (direction == 0 && activeCoords.x == buttonCoords.x
					&& activeCoords.y + button.getHeight() == buttonCoords.y) {
				result = true;
			} else if (direction == 2 && activeCoords.x == buttonCoords.x
					&& activeCoords.y - button.getHeight() == buttonCoords.y) {
				result = true;
			} else if (direction == 3
					&& activeCoords.x - button.getWidth() == buttonCoords.x
					&& activeCoords.y == buttonCoords.y) {
				result = true;
			}
		}
		return result;
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
