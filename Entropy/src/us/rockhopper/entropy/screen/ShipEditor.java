package us.rockhopper.entropy.screen;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import us.rockhopper.entropy.entities.Cockpit;
import us.rockhopper.entropy.entities.Gyroscope;
import us.rockhopper.entropy.entities.Thruster;
import us.rockhopper.entropy.gui.PartImage;
import us.rockhopper.entropy.utility.FileIO;
import us.rockhopper.entropy.utility.Layout;
import us.rockhopper.entropy.utility.Part;
import us.rockhopper.entropy.utility.PartClassAdapter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
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
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.google.gson.GsonBuilder;

public class ShipEditor extends ScreenAdapter {

	private TiledDrawable background;

	private Stage stage;
	private Skin skin;
	private OrthographicCamera camera;
	private SpriteBatch batch;

	private Table selections;
	private Table tabbed;
	private Table info;

	private String defaultFolder = new JFileChooser().getFileSystemView().getDefaultDirectory().toString();

	private ArrayList<Part> command = new ArrayList<Part>();
	private ArrayList<Part> control = new ArrayList<Part>();
	private ArrayList<Part> thrust = new ArrayList<Part>();
	private ArrayList<Part> hull = new ArrayList<Part>();
	private ArrayList<Part> weaponry = new ArrayList<Part>();

	private Part activePart;
	private float activePartX, activePartY;
	private int sWidth, sHeight;

	ArrayList<Vector2> occupiedTiles = new ArrayList<Vector2>();
	ArrayList<PartImage> partImages = new ArrayList<PartImage>();
	ArrayList<Part> parts = new ArrayList<Part>();
	private Image activeImage;
	private ClickListener itemChooseListener;

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		stage.act(delta);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		// Draw background
		background.draw(batch, -sWidth / 2, -sHeight / 2, sWidth, sHeight);
		for (Image image : partImages) {
			image.draw(batch, 1f);
		}
		batch.end();
		stage.draw();
		batch.begin();
		// Draw part overlay
		if (activePart != null) {
			Sprite sprite = new Sprite(new Texture("assets/img/overlay.png"));
			sprite.setPosition(activePartX - sWidth / 2, activePartY - sHeight / 2);
			sprite.setSize(activeImage.getWidth(), activeImage.getHeight());
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
		info.invalidateHierarchy();
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		sWidth = width;
		sHeight = height;
		System.out.println("Resized to " + sWidth + " " + sHeight);
	}

	@Override
	public void show() {
		// Sprite rendering
		background = new TiledDrawable(new TextureRegion(new Texture("assets/img/grid.png")));
		batch = new SpriteBatch();
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		skin = new Skin(Gdx.files.internal("assets/ui/uiskin.json"), new TextureAtlas("assets/ui/uiskin.pack"));

		// Button initializing
		final TextButton buttonCommand = new TextButton("Command", skin, "default");
		final TextButton buttonControl = new TextButton("Control", skin, "default");
		final TextButton buttonThrust = new TextButton("Thrust", skin, "default");
		final TextButton buttonHull = new TextButton("Hull", skin, "default");
		final TextButton buttonWeaponry = new TextButton("Weaponry", skin, "default");
		final TextButton buttonGo = new TextButton("Start", skin, "default");

		final TextField nameField = new TextField("Ship Name", skin, "default");
		final TextField forwardField = new TextField("Forward", skin, "default");
		final TextField reverseField = new TextField("Reverse", skin, "default");

		// Initialize input processing
		InputMultiplexer multiplexer = new InputMultiplexer();
		stage = new Stage();
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(new InputAdapter() {

			int lastGridX;
			int lastGridY;

			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				// Get the grid position the mouse starts in, get the one it ends in, and every time it crosses into a
				// new grid cell move all pieces accordingly?
				if (Gdx.input.isButtonPressed(Buttons.RIGHT)) {
					int gridX = (int) screenX / 16;
					int gridY = (int) (Gdx.graphics.getHeight() - screenY) / 16;
					int dX = gridX - lastGridX;
					int dY = gridY - lastGridY;
					System.out.println("Difference " + dX + ", " + dY);

					for (PartImage image : partImages) {
						image.setGridY(image.getGridY() + dY);
						image.setGridX(image.getGridX() + dX);

						// Reposition the image.
						int rotIndex = (int) (Math.abs(image.getRotation()) / 90) % 4;
						if ((image.getWidth() != image.getHeight()) && (rotIndex == 1 || rotIndex == 3)) {
							image.setPosition((image.getGridX() * 16) - Gdx.graphics.getWidth() / 2f + image.getWidth()
									/ 2f, (image.getGridY() * 16) - Gdx.graphics.getHeight() / 2f - image.getHeight()
									/ 4f);
						} else {
							image.setPosition((image.getGridX() * 16) - Gdx.graphics.getWidth() / 2f,
									(image.getGridY() * 16) - Gdx.graphics.getHeight() / 2f);
						}
					}

					lastGridX = gridX;
					lastGridY = gridY;
				}
				return true;
			}

			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				int gridX = (int) screenX / 16;
				int gridY = (int) (Gdx.graphics.getHeight() - screenY) / 16;
				lastGridX = gridX;
				lastGridY = gridY;

				if (button == Buttons.LEFT && !(contains(tabbed, screenX, screenY) || contains(info, screenX, screenY))) {
					System.out.println("Click! at " + gridX + " " + gridY);
					if (activePart != null) {
						if (partImages.isEmpty() && !(activePart instanceof Cockpit)) {
							new Dialog("", skin) {
								{
									text("The first piece on your ship must be a command module.");
								}
							}.show(stage).addAction(
									sequence(alpha(1f, 0.3f), Actions.delay(0.4f), alpha(0f, 0.3f),
											Actions.removeActor()));
						} else {
							// This is the tile clicked.
							PartImage image = new PartImage(null, new Texture(activePart.getSprite()));
							image.setOrigin(image.getWidth() / 2f, image.getHeight() / 2f);
							image.setRotation(activePart.getRotation());
							image.setGridX(gridX);
							image.setGridY(gridY);
							image.setPart(activePart.clone());

							// Image positioning is dependent on its rotation
							int rotIndex = (int) (Math.abs(image.getRotation()) / 90) % 4;
							if ((image.getWidth() != image.getHeight()) && (rotIndex == 1 || rotIndex == 3)) {
								image.setPosition((gridX * 16) - Gdx.graphics.getWidth() / 2f + image.getWidth() / 2f,
										(gridY * 16) - Gdx.graphics.getHeight() / 2f - image.getHeight() / 4f);
							} else {
								image.setPosition((gridX * 16) - Gdx.graphics.getWidth() / 2f, (gridY * 16)
										- Gdx.graphics.getHeight() / 2f);
							}

							// If this is a valid place to put the part
							// 1. This is not overlapping another part.
							if (!hasOverlap(image, gridX, gridY)) {

								// 2. This piece is adjacent to another piece.
								// Check all four directions
								ArrayList<PartImage> adjacentImages = new ArrayList<PartImage>();
								ArrayList<Integer> directions = new ArrayList<Integer>();
								adjacentImages.clear();
								for (int i = 0; i < 4; ++i) {
									ArrayList<PartImage> temp = getAdjacent(image, gridX, gridY, i);
									if (!temp.isEmpty()) {
										System.out.println("Found " + temp.size() + " pieces in direction " + i);
										for (PartImage part : temp) {
											System.out.println("There is a part here, " + part.getPart().getName());
											adjacentImages.add(part);
											directions.add(i);
										}
									}
								}

								// If any four directions contain any part, but
								// only if there's at least one part down.
								if (partImages.isEmpty() || !adjacentImages.isEmpty()) {

									// 3. If the rotation is appropriate given
									// any surrounding pieces.
									// If this is a valid means of attaching a
									// piece
									boolean match = false;
									if (!partImages.isEmpty()) {
										for (int i = 0; i < adjacentImages.size(); ++i) {
											PartImage adjacent = adjacentImages.get(i);
											int direction = directions.get(i);
											if (image.fits(adjacent, direction)) {
												match = true;
											}
										}
									} else {
										match = true;
									}
									if (match) {

										// Mark all tiles covered by this part
										// as
										// occupied
										for (Vector2 vector : image.getOccupiedCells()) {
											System.out.println("Adding cells " + vector.toString());
											occupiedTiles.add(vector);
										}

										// Add the part associated with the
										// image
										// into
										// the parts ArrayList.
										// Create a new array of nodes to
										// prevent
										// reference errors
										int[] newArray = new int[activePart.getAttachmentNodes().length];
										for (int i = 0; i < activePart.getAttachmentNodes().length; ++i) {
											newArray[i] = new Integer(activePart.getAttachmentNodes()[i]);
										}

										// Create new part to attach
										Part part = image.getPart();

										// Add any part-specific actions
										if (part instanceof Thruster) {
											Thruster thruster = (Thruster) part;
											thruster.setForward(Keys.valueOf(forwardField.getText().toUpperCase()));
										} else if (part instanceof Gyroscope) {
											Gyroscope gyro = (Gyroscope) part;
											gyro.setClockwise(Keys.valueOf(forwardField.getText().toUpperCase()));
											gyro.setCounterClockwise(Keys.valueOf(reverseField.getText().toUpperCase()));
										}

										// Now modify and add the part
										part.setAttachmentNodes(newArray);
										part.setGridPosition(gridX, gridY);
										parts.add(part);

										// Designate that this image be rendered
										partImages.add(image);
									} else {
										new Dialog("", skin) {
											{
												text("Those pieces don't line up like that.");
											}
										}.show(stage).addAction(
												sequence(alpha(1f, 0.3f), Actions.delay(0.6f), alpha(0f, 0.3f),
														Actions.removeActor()));
									}
								} else {
									new Dialog("", skin) {
										{
											text("Your piece must be adjacent to another.");
										}
									}.show(stage).addAction(
											sequence(alpha(1f, 0.3f), Actions.delay(0.6f), alpha(0f, 0.3f),
													Actions.removeActor()));
								}
							} else {
								new Dialog("", skin) {
									{
										text("Your pieces cannot overlap.");
									}
								}.show(stage).addAction(
										sequence(alpha(1f, 0.3f), Actions.delay(0.4f), alpha(0f, 0.3f),
												Actions.removeActor()));
							}

						}
					}
				}
				return true;
			}

			/**
			 * Takes an image, its root gridX and gridY, and a direction, and returns the Image from partsImage located
			 * in that direction. Being adjacent is defined as having any point along the edge in the given direction
			 * also touch another Image. Touching corners are not considered adjacent. Returns an empty list if no
			 * adjacent pieces are found.
			 * 
			 * @param image
			 * @param gridX
			 * @param gridY
			 * @param direction
			 * @return
			 */
			private ArrayList<PartImage> getAdjacent(PartImage image, int gridX, int gridY, int direction) {
				// Instantiate the list
				ArrayList<PartImage> list = new ArrayList<>();
				list.clear();
				// Get grid-Width and grid-Height
				Texture texture = new Texture(image.getPart().getSprite());
				System.out.println("Rotation " + image.getRotation());
				int rotIndex = (int) (Math.abs(image.getRotation()) / 90) % 4;
				int tilesX = 0;
				int tilesY = 0;
				System.out.println("RotIndex " + rotIndex);
				if (rotIndex == 1 || rotIndex == 3) {
					System.out.println();
					tilesX = (int) texture.getHeight() / 16;
					tilesY = (int) texture.getWidth() / 16;
				} else {
					tilesX = (int) texture.getWidth() / 16;
					tilesY = (int) texture.getHeight() / 16;
				}

				System.out.println("Grid: " + gridX + " " + gridY + " from direction " + direction + " at widths "
						+ tilesX + " " + tilesY);

				// Find all grid tiles along the given edge.
				switch (direction) {
				case 0:
					// For the entire width of the image
					for (int i = 0; i < tilesX; ++i) {
						// x is each x grid
						int adjacentX = gridX + i;
						// for every image thus far
						for (int j = 0; j < partImages.size(); ++j) {
							PartImage partImage = partImages.get(j);
							// if it overlaps any of the points along the top
							// edge, add it to the list of images adjacent in
							// that direction
							ArrayList<Vector2> occupied = partImage.getOccupiedCells();
							if (occupied.contains(new Vector2(adjacentX, (gridY + tilesY)))
									&& !list.contains(partImage)) {
								list.add(partImage);
							}
						}
					}
					break;
				case 1:
					// For the entire height of the image
					for (int i = 0; i < tilesY; ++i) {
						// y is each y grid
						int adjacentY = gridY + i;
						// for every image thus far
						for (int j = 0; j < partImages.size(); ++j) {
							PartImage partImage = partImages.get(j);
							// if it overlaps any of the points along the right
							// edge, add it to the list of images adjacent in
							// that direction
							ArrayList<Vector2> occupied = partImage.getOccupiedCells();
							if (occupied.contains(new Vector2(gridX + tilesX, adjacentY)) && !list.contains(partImage)) {
								list.add(partImage);
							}
						}
					}
					break;
				case 2:
					// For the entire width of the image
					for (int i = 0; i < tilesX; ++i) {
						// x is each x grid
						int adjacentX = gridX + i;
						// for every image thus far
						for (int j = 0; j < partImages.size(); ++j) {
							PartImage partImage = partImages.get(j);
							// if it overlaps any of the points along the bottom
							// edge, add it to the list of images adjacent in
							// that direction
							ArrayList<Vector2> occupied = partImage.getOccupiedCells();
							if (occupied.contains(new Vector2(adjacentX, gridY - 1)) && !list.contains(partImage)) {
								list.add(partImage);
							}
						}
					}
					break;
				case 3:
					// For the entire height of the image
					for (int i = 0; i < tilesY; ++i) {
						// y is each y grid
						int adjacentY = gridY + i;
						// for every image thus far
						for (int j = 0; j < partImages.size(); ++j) {
							PartImage partImage = partImages.get(j);
							// if it overlaps any of the points along the left
							// edge, add it to the list of images adjacent in
							// that direction
							ArrayList<Vector2> occupied = partImage.getOccupiedCells();
							if (occupied.contains(new Vector2(gridX - 1, adjacentY)) && !list.contains(partImage)) {
								list.add(partImage);
							}
						}
					}
				}
				return list;
			}

			/**
			 * Determines whether or not the image, rooted at the gridX and gridY position, overlaps any of the grid
			 * tiles marked as occupied.
			 * 
			 * @param image
			 * @param gridX
			 * @param gridY
			 * @param occupiedTiles
			 * @return
			 */
			private boolean hasOverlap(PartImage image, int gridX, int gridY) {
				int rotIndex = (int) (Math.abs(image.getRotation()) / 90) % 4;
				int tilesX = 0;
				int tilesY = 0;
				if (rotIndex == 1 || rotIndex == 3) {
					System.out.println();
					tilesX = (int) image.getHeight() / 16;
					tilesY = (int) image.getWidth() / 16;
				} else {
					tilesX = (int) image.getWidth() / 16;
					tilesY = (int) image.getHeight() / 16;
				}
				for (int i = 0; i < tilesX; ++i) {
					for (int j = 0; j < tilesY; ++j) {
						if (occupiedTiles.contains(new Vector2(gridX + i, gridY + j))) {
							return true;
						}
					}
				}
				return false;
			}

			/**
			 * Returns whether or not the given table contains the screen-coordinate point.
			 * 
			 * @param table
			 * @param screenX
			 * @param screenY
			 * @return
			 */
			private boolean contains(Table table, int screenX, int screenY) {
				if (screenX < (table.getX() + table.getWidth()) && screenX > table.getX()
						&& (Gdx.graphics.getHeight() - screenY) < (table.getY() + table.getHeight())
						&& (Gdx.graphics.getHeight() - screenY) > table.getY()) {
					return true;
				}
				return false;
			}

			@Override
			public boolean keyDown(int keycode) {
				if (activePart != null && activeImage != null && keycode == Keys.Q) {
					activeImage.setRotation(activeImage.getRotation() + 90);
					activePart.rotateLeft();
				}
				if (activePart != null && activeImage != null && keycode == Keys.E) {
					activeImage.setRotation(activeImage.getRotation() - 90);
					activePart.rotateRight();
				}
				// Use WASD to move pieces around the grid.
				if (keycode == Keys.W) {
					for (PartImage image : partImages) {
						image.setGridY(image.getGridY() - 1);

						// Reposition the image.
						int rotIndex = (int) (Math.abs(image.getRotation()) / 90) % 4;
						if ((image.getWidth() != image.getHeight()) && (rotIndex == 1 || rotIndex == 3)) {
							image.setPosition((image.getGridX() * 16) - Gdx.graphics.getWidth() / 2f + image.getWidth()
									/ 2f, (image.getGridY() * 16) - Gdx.graphics.getHeight() / 2f - image.getHeight()
									/ 4f);
						} else {
							image.setPosition((image.getGridX() * 16) - Gdx.graphics.getWidth() / 2f,
									(image.getGridY() * 16) - Gdx.graphics.getHeight() / 2f);
						}
					}
				}

				if (keycode == Keys.A) {
					for (PartImage image : partImages) {
						image.setGridX(image.getGridX() + 1);

						// Reposition the image.
						int rotIndex = (int) (Math.abs(image.getRotation()) / 90) % 4;
						if ((image.getWidth() != image.getHeight()) && (rotIndex == 1 || rotIndex == 3)) {
							image.setPosition((image.getGridX() * 16) - Gdx.graphics.getWidth() / 2f + image.getWidth()
									/ 2f, (image.getGridY() * 16) - Gdx.graphics.getHeight() / 2f - image.getHeight()
									/ 4f);
						} else {
							image.setPosition((image.getGridX() * 16) - Gdx.graphics.getWidth() / 2f,
									(image.getGridY() * 16) - Gdx.graphics.getHeight() / 2f);
						}
					}
				}

				if (keycode == Keys.S) {
					for (PartImage image : partImages) {
						image.setGridY(image.getGridY() + 1);

						// Reposition the image.
						int rotIndex = (int) (Math.abs(image.getRotation()) / 90) % 4;
						if ((image.getWidth() != image.getHeight()) && (rotIndex == 1 || rotIndex == 3)) {
							image.setPosition((image.getGridX() * 16) - Gdx.graphics.getWidth() / 2f + image.getWidth()
									/ 2f, (image.getGridY() * 16) - Gdx.graphics.getHeight() / 2f - image.getHeight()
									/ 4f);
						} else {
							image.setPosition((image.getGridX() * 16) - Gdx.graphics.getWidth() / 2f,
									(image.getGridY() * 16) - Gdx.graphics.getHeight() / 2f);
						}
					}
				}

				if (keycode == Keys.D) {
					for (PartImage image : partImages) {
						image.setGridX(image.getGridX() - 1);

						// Reposition the image.
						int rotIndex = (int) (Math.abs(image.getRotation()) / 90) % 4;
						if ((image.getWidth() != image.getHeight()) && (rotIndex == 1 || rotIndex == 3)) {
							image.setPosition((image.getGridX() * 16) - Gdx.graphics.getWidth() / 2f + image.getWidth()
									/ 2f, (image.getGridY() * 16) - Gdx.graphics.getHeight() / 2f - image.getHeight()
									/ 4f);
						} else {
							image.setPosition((image.getGridX() * 16) - Gdx.graphics.getWidth() / 2f,
									(image.getGridY() * 16) - Gdx.graphics.getHeight() / 2f);
						}
					}
				}
				return true;
			}
		});
		Gdx.input.setInputProcessor(multiplexer);

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

		// Load all weapon parts into its list.
		String weaponPath = defaultFolder + "\\EntropyShips\\Parts\\Weaponry\\";
		for (File file : FileIO.getFilesForFolder(new File(weaponPath))) {
			String partJSON = FileIO.read(file.getAbsolutePath());
			GsonBuilder gson = new GsonBuilder();
			gson.registerTypeAdapter(Part.class, new PartClassAdapter());
			Part part = gson.create().fromJson(partJSON, Part.class);
			weaponry.add(part);
		}

		selections = new Table(skin);
		info = new Table(skin);
		info.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture("assets/img/tableBack.png"))));
		tabbed = new Table(skin);
		tabbed.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture("assets/img/tableBack.png"))));
		selections.setFillParent(true);
		selections.debug();

		itemChooseListener = new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				PartImage active = (PartImage) event.getListenerActor();
				activePart = active.getPart();
				activePart.clearRotation();
				Vector2 buttonCoords = active.localToStageCoordinates(new Vector2(0, 0));
				activePartX = buttonCoords.x;
				activePartY = buttonCoords.y;
				info.clear();
				Label heading = new Label(activePart.getName(), skin, "default");
				Label description = new Label(activePart.getDescription(), skin, "default");
				description.setWrap(true);
				info.add(heading);
				info.row();
				info.add("Cost: " + activePart.getCost());
				info.row();
				info.add(description).width(200).center();
				info.row();
				Image activeSprite = new Image(new Texture(activePart.getSprite()));
				activeSprite.setOrigin(activeSprite.getWidth() / 2f, activeSprite.getHeight() / 2f);
				activeImage = activeSprite;
				info.add(activeSprite);
				info.row();

				// Handling part-specific actions
				if (activePart instanceof Thruster) {
					info.add(forwardField);
					Thruster thruster = (Thruster) activePart;
					if (thruster.getCanReverse()) {
						info.row();
						info.add(reverseField);
					}
				} else if (activePart instanceof Gyroscope) {
					info.add(forwardField).row();
					info.add(reverseField);
				}
			}
		};

		ClickListener tabChooseListener = new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (event.getListenerActor() == buttonCommand) {
					tabbed.clear();
					info.clear();
					activePart = null;
					for (int i = 0; i < command.size(); ++i) {
						Part part = command.get(i);
						PartImage selectPart = new PartImage(part, new Texture(part.getSprite()));
						selectPart.addListener(itemChooseListener);
						tabbed.add(selectPart);
					}
				} else if (event.getListenerActor() == buttonControl) {
					tabbed.clear();
					info.clear();
					activePart = null;
					for (int i = 0; i < control.size(); ++i) {
						Part part = control.get(i);
						PartImage selectPart = new PartImage(part, new Texture(part.getSprite()));
						selectPart.addListener(itemChooseListener);
						tabbed.add(selectPart);
					}
				} else if (event.getListenerActor() == buttonThrust) {
					tabbed.clear();
					info.clear();
					activePart = null;
					for (int i = 0; i < thrust.size(); ++i) {
						Part part = thrust.get(i);
						PartImage selectPart = new PartImage(part, new Texture(part.getSprite()));
						selectPart.addListener(itemChooseListener);
						tabbed.add(selectPart);
					}
				} else if (event.getListenerActor() == buttonHull) {
					tabbed.clear();
					info.clear();
					activePart = null;
					for (int i = 0; i < hull.size(); ++i) {
						Part part = hull.get(i);
						PartImage selectPart = new PartImage(part, new Texture(part.getSprite()));
						selectPart.addListener(itemChooseListener);
						tabbed.add(selectPart);
					}
				} else if (event.getListenerActor() == buttonWeaponry) {
					tabbed.clear();
					info.clear();
					activePart = null;
					for (int i = 0; i < weaponry.size(); ++i) {
						Part part = weaponry.get(i);
						PartImage selectPart = new PartImage(part, new Texture(part.getSprite()));
						selectPart.addListener(itemChooseListener);
						tabbed.add(selectPart);
					}
				}
				// Instantiate the ship and move onto the next screen.
				else if (event.getListenerActor() == buttonGo) {
					for (Part part : parts) {
						System.out.println(part.getName() + " " + part.getAttachmentNodes().length + " nodes, ("
								+ part.getGridX() + ", " + part.getGridY() + ")");
					}
				}
			}
		};

		buttonCommand.addListener(tabChooseListener);
		buttonControl.addListener(tabChooseListener);
		buttonThrust.addListener(tabChooseListener);
		buttonHull.addListener(tabChooseListener);
		buttonWeaponry.addListener(tabChooseListener);
		buttonGo.addListener(tabChooseListener);
		selections.left().top();
		selections.defaults().fillX();
		selections.add(buttonCommand);
		selections.add(buttonControl);
		selections.add(buttonThrust);
		selections.add(buttonHull);
		selections.add(buttonWeaponry);
		selections.add(nameField);
		selections.add(buttonGo);
		selections.row();
		selections.add(tabbed).colspan(5);
		selections.row();
		selections.add(info).colspan(5);

		stage.addActor(selections);

		stage.addAction(sequence(moveTo(0, stage.getHeight()), moveTo(0, 0, .5f))); // coming in from top animation
	}

	/**
	 * Takes a list of Images which hold part data, and returns the parts appropriately sorted into a layout.
	 * 
	 * @param grid
	 *            the grid of parts.
	 * @return a sorted layout of the parts.
	 */
	protected Layout toLayout(ArrayList<PartImage> grid) {

		// Find the bottom-left-corner of the ship.
		float lowX = Integer.MAX_VALUE;
		float lowY = Integer.MAX_VALUE;

		// Find the top-right-corner of the ship.
		float highX = Integer.MIN_VALUE;
		float highY = Integer.MIN_VALUE;

		// Find the lowest x-coordinate of the pieces.
		for (int i = 0; i < grid.size(); ++i) {
			PartImage button = grid.get(i);
			Vector2 buttonCoords = button.localToStageCoordinates(new Vector2(button.getWidth() / 2f, button
					.getHeight() / 2f));
			if (buttonCoords.x < lowX) {
				lowX = buttonCoords.x;
			}
		}

		// Find the lowest y-coordinate of the pieces.
		for (int i = 0; i < grid.size(); ++i) {
			PartImage button = grid.get(i);
			Vector2 buttonCoords = button.localToStageCoordinates(new Vector2(button.getWidth() / 2f, button
					.getHeight() / 2f));
			if (buttonCoords.y < lowY) {
				lowY = buttonCoords.y;
			}
		}

		// Find the highest x-coordinate of the pieces.
		for (int i = 0; i < grid.size(); ++i) {
			PartImage button = grid.get(i);
			Vector2 buttonCoords = button.localToStageCoordinates(new Vector2(button.getWidth() / 2f, button
					.getHeight() / 2f));
			if (buttonCoords.x > highX) {
				highX = buttonCoords.x;
			}
		}

		// Find the highest y-coordinate of the pieces.
		for (int i = 0; i < grid.size(); ++i) {
			PartImage button = grid.get(i);
			Vector2 buttonCoords = button.localToStageCoordinates(new Vector2(button.getWidth() / 2f, button
					.getHeight() / 2f));
			if (buttonCoords.y > highY) {
				highY = buttonCoords.y;
			}
		}

		// Figure out the width and height of the required layout.
		int width = 1 + (int) (highX - lowX) / 32;
		int height = 1 + (int) (highY - lowY) / 32;
		Layout layout = new Layout(width, height);

		// Run through every piece in the grid and determine their spots in the
		// layout.
		for (int i = 0; i < (width); ++i) {
			for (int j = 0; j < (height); ++j) {
				for (int k = 0; k < grid.size(); ++k) {
					PartImage button = grid.get(k);
					Vector2 buttonCoords = button.localToStageCoordinates(new Vector2(button.getWidth() / 2f, button
							.getHeight() / 2f));
					// If an Image exists with these exact coordinates...
					if (button.getPart() != null && buttonCoords.x == lowX + (i * 32)
							&& buttonCoords.y == lowY + (j * 32)) {
						// Set its location in the Layout.
						grid.get(k).getPart().setGridPosition(new Integer(i), new Integer(j));
						layout.setPart(grid.get(k).getPart(), new Integer(i), new Integer(j));
						System.out.println(button.getPart() + " is at " + button.getPart().getGridX() + " "
								+ button.getPart().getGridY() + " or " + i + " " + j);
						break;
					} else {
						// No part here, set null.
						layout.setPart(null, i, j);
					}
				}
			}
		}

		return layout;
	}

	/**
	 * Returns whether or not the PartImageButton in the grid has an adjacent PartImageButton in the given direction.
	 * 
	 * @param grid
	 *            The grid to search for parts in.
	 * @param active
	 *            The PartImageButton of the grid to target.
	 * @param direction
	 *            The direction to search for adjacent buttons--0 for up, 1 for right, 2 for down, 3 for left.
	 * @return Whether or not the piece has an adjacent piece.
	 */
	protected boolean hasAdjacent(ArrayList<PartImage> grid, PartImage active, int direction) {
		boolean result = false;
		Vector2 activeCoords = active.localToStageCoordinates(new Vector2(active.getWidth() / 2f,
				active.getHeight() / 2f));
		for (int i = 0; i < grid.size(); ++i) {
			PartImage button = grid.get(i);
			Vector2 buttonCoords = button.localToStageCoordinates(new Vector2(button.getWidth() / 2f, button
					.getHeight() / 2f));
			if (direction == 1 && activeCoords.x + button.getWidth() == buttonCoords.x
					&& activeCoords.y == buttonCoords.y) {
				result = true;
			} else if (direction == 0 && activeCoords.x == buttonCoords.x
					&& activeCoords.y + button.getHeight() == buttonCoords.y) {
				result = true;
			} else if (direction == 2 && activeCoords.x == buttonCoords.x
					&& activeCoords.y - button.getHeight() == buttonCoords.y) {
				result = true;
			} else if (direction == 3 && activeCoords.x - button.getWidth() == buttonCoords.x
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
	public void dispose() {
		stage.dispose();
		skin.dispose();
	}
}
