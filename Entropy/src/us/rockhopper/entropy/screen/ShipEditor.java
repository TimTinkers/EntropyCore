package us.rockhopper.entropy.screen;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import us.rockhopper.entropy.entities.BasicShip;
import us.rockhopper.entropy.entities.Cockpit;
import us.rockhopper.entropy.entities.Gyroscope;
import us.rockhopper.entropy.entities.Thruster;
import us.rockhopper.entropy.gui.PartImage;
import us.rockhopper.entropy.utility.FileIO;
import us.rockhopper.entropy.utility.Layout;
import us.rockhopper.entropy.utility.Part;
import us.rockhopper.entropy.utility.PartClassAdapter;

import com.badlogic.gdx.Game;
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
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
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

	private boolean deleteMode = false;
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
		final TextButton buttonTools = new TextButton("Editing", skin, "default");
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
					occupiedTiles.clear();
					for (PartImage image : partImages) {
						image.setGridY(image.getGridY() + dY);
						image.setGridX(image.getGridX() + dX);
						image.getPart().setGridPosition(image.getGridX() + dX, image.getGridY() + dY);

						for (Vector2 vector : image.getOccupiedCells()) {
							System.out.println("Marking as occupied " + vector.x + " " + vector.y);
							image.getPart().setOccupiedCells(image.getOccupiedCells());
							occupiedTiles.add(vector);
						}

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

				// If we aren't deleting pieces of the ship
				if (!deleteMode) {
					if (button == Buttons.LEFT
							&& !(contains(tabbed, screenX, screenY) || contains(info, screenX, screenY))) {
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
								image.setPart(activePart.clone().setGridPosition(gridX, gridY));
								image.getPart().setOccupiedCells(image.getOccupiedCells());

								// Image positioning is dependent on its rotation
								int rotIndex = (int) (Math.abs(image.getRotation()) / 90) % 4;
								if ((image.getWidth() != image.getHeight()) && (rotIndex == 1 || rotIndex == 3)) {
									image.setPosition((gridX * 16) - Gdx.graphics.getWidth() / 2f + image.getWidth()
											/ 2f, (gridY * 16) - Gdx.graphics.getHeight() / 2f - image.getHeight() / 4f);
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
										ArrayList<PartImage> temp = getAdjacent(image, i);
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

											// Mark all tiles covered by this part as occupied
											for (Vector2 vector : image.getOccupiedCells()) {
												System.out.println("Marking as occupied " + vector.x + " " + vector.y);
												occupiedTiles.add(vector);
											}

											// Add the part associated with the image into the parts ArrayList. Create a
											// new array of nodes to prevent reference errors.
											int[] newArray = new int[activePart.getAttachmentNodes().length];
											for (int i = 0; i < activePart.getAttachmentNodes().length; ++i) {
												newArray[i] = new Integer(activePart.getAttachmentNodes()[i]);
											}

											// Create new part to attach
											Part part = image.getPart().clone();

											// Add any part-specific actions
											if (part instanceof Thruster) {
												Thruster thruster = (Thruster) part;
												thruster.setForward(Keys.valueOf(forwardField.getText().toUpperCase()));
											} else if (part instanceof Gyroscope) {
												Gyroscope gyro = (Gyroscope) part;
												gyro.setClockwise(Keys.valueOf(forwardField.getText().toUpperCase()));
												gyro.setCounterClockwise(Keys.valueOf(reverseField.getText()
														.toUpperCase()));
											}

											// Now modify and add the part
											part.setAttachmentNodes(newArray);
											part.setGridPosition(gridX, gridY);

											// The command module for this ship was potentially deleted. If this is a
											// command module, insert it once more as the first element in the parts
											// array.
											boolean hasCockpit = false;
											if (parts.isEmpty()) {
												hasCockpit = true;
											}
											for (int i = 0; i < parts.size(); ++i) {
												Part partCockpit = parts.get(i);
												if (partCockpit instanceof Cockpit) {
													hasCockpit = true;
												}
											}
											if (!hasCockpit && (part instanceof Cockpit)) {
												parts.add(0, part);
											} else {
												parts.add(part);
											}

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
				} else {
					if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
						// We are in delete mode
						PartImage deleted = getPart(gridX, gridY);
						if (deleted != null) {
							partImages.remove(deleted);
							parts.remove(deleted.getPart());
							for (Vector2 vector : deleted.getOccupiedCells()) {
								occupiedTiles.remove(vector);
							}

						}// TODO seems like all of this is working. Now you need to figure out how to make it delete
							// everything left unattached to the primary command module. If the primary module is
							// deleted,
							// delete everything. Unless there's another command module, in which case, make that the
							// primary command module. And then check to delete anything not attached to it.
						deleted = null;
					}
				}
				return true;
			};

			/**
			 * Returns the PartImage at the given grid coordinates.
			 * 
			 * @param gridX
			 * @param gridY
			 * @return
			 */
			private PartImage getPart(int gridX, int gridY) {
				for (int i = 0; i < partImages.size(); ++i) {
					PartImage partImage = partImages.get(i);
					// If the part overlaps the given coordinates, then it occupies those coordinates.
					ArrayList<Vector2> occupied = partImage.getOccupiedCells();
					if (occupied.contains(new Vector2(gridX, gridY))) {
						return partImage;
					}
				}
				return null;
			}

			/**
			 * Takes an image, its root gridX and gridY, and a direction, and returns the Image from partsImage located
			 * in that direction. Being adjacent is defined as having any point along the edge in the given direction
			 * also touch another Image. Touching corners are not considered adjacent. Returns an empty list if no
			 * adjacent pieces are found.
			 * 
			 * @param image
			 * @param direction
			 * @return
			 */
			private ArrayList<PartImage> getAdjacent(PartImage image, int direction) {
				// Instantiate the list
				ArrayList<PartImage> list = new ArrayList<>();
				list.clear();
				// Get grid-Width and grid-Height
				Texture texture = new Texture(image.getPart().getSprite());
				int rotIndex = (int) (Math.abs(image.getRotation()) / 90) % 4;
				int tilesX = 0;
				int tilesY = 0;
				if (rotIndex == 1 || rotIndex == 3) {
					System.out.println();
					tilesX = (int) texture.getHeight() / 16;
					tilesY = (int) texture.getWidth() / 16;
				} else {
					tilesX = (int) texture.getWidth() / 16;
					tilesY = (int) texture.getHeight() / 16;
				}

				int gridX = image.getGridX();
				int gridY = image.getGridY();

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
					tilesX = (int) image.getHeight() / 16;
					tilesY = (int) image.getWidth() / 16;
					System.out.println("These coordinates are " + tilesX + " " + tilesY);
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
					clear();
					for (int i = 0; i < command.size(); ++i) {
						Part part = command.get(i);
						PartImage selectPart = new PartImage(part, new Texture(part.getSprite()));
						selectPart.addListener(itemChooseListener);
						tabbed.add(selectPart);
					}
				} else if (event.getListenerActor() == buttonControl) {
					clear();
					for (int i = 0; i < control.size(); ++i) {
						Part part = control.get(i);
						PartImage selectPart = new PartImage(part, new Texture(part.getSprite()));
						selectPart.addListener(itemChooseListener);
						tabbed.add(selectPart);
					}
				} else if (event.getListenerActor() == buttonThrust) {
					clear();
					for (int i = 0; i < thrust.size(); ++i) {
						Part part = thrust.get(i);
						PartImage selectPart = new PartImage(part, new Texture(part.getSprite()));
						selectPart.addListener(itemChooseListener);
						tabbed.add(selectPart);
					}
				} else if (event.getListenerActor() == buttonHull) {
					clear();
					for (int i = 0; i < hull.size(); ++i) {
						Part part = hull.get(i);
						PartImage selectPart = new PartImage(part, new Texture(part.getSprite()));
						selectPart.addListener(itemChooseListener);
						tabbed.add(selectPart);
					}
				} else if (event.getListenerActor() == buttonWeaponry) {
					clear();
					for (int i = 0; i < weaponry.size(); ++i) {
						Part part = weaponry.get(i);
						PartImage selectPart = new PartImage(part, new Texture(part.getSprite()));
						selectPart.addListener(itemChooseListener);
						tabbed.add(selectPart);
					}
				} else if (event.getListenerActor() == buttonTools) {
					clear();
					ImageButton remove = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
							"assets/img/remove.png"))));
					remove.addListener(new ClickListener() {
						@Override
						public void clicked(InputEvent event, float x, float y) {
							Label deleteState = new Label("Delete mode is enabled.", skin, "default");
							deleteState.addAction(sequence(alpha(0f), alpha(1f, 0.6f)));
							if (!deleteMode) {
								info.add(deleteState);
								deleteMode = true;
							} else {
								info.clear();
								deleteState.setText("Delete mode is disabled.");
								deleteState.addAction(sequence(alpha(1f), Actions.delay(0.3f), alpha(0f, 0.6f),
										Actions.removeActor()));
								info.add(deleteState);
								deleteMode = false;
							}
						}
					});
					tabbed.add(remove);
				}

				// Instantiate the ship and move onto the next screen.
				else if (event.getListenerActor() == buttonGo) {
					BasicShip ship = new BasicShip(parts.get(0).getGridX(), parts.get(0).getGridY(), parts);

					// Serialize and write to file
					GsonBuilder gson = new GsonBuilder();
					gson.registerTypeAdapter(Part.class, new PartClassAdapter());
					final String shipJSON = gson.setPrettyPrinting().create().toJson(ship);
					if (FileIO.exists(defaultFolder + "\\EntropyShips\\" + nameField.getText() + ".json")) {
						new Dialog("", skin) {
							{
								text("Ship " + nameField.getText() + " already exists. Would you like to overwrite it?");
								button("Yes", true);
								button("No", false);
							}

							protected void result(Object object) {
								System.out.println("Chosen: " + object);
								boolean bool = (Boolean) object;
								if (bool == true) {
									FileIO.write(defaultFolder + "\\EntropyShips\\" + nameField.getText() + ".json",
											shipJSON);
									((Game) Gdx.app.getApplicationListener()).setScreen(new GameStart(nameField
											.getText()));
								}
							}
						}.show(stage);
					}
				}
			}
		};

		buttonCommand.addListener(tabChooseListener);
		buttonControl.addListener(tabChooseListener);
		buttonThrust.addListener(tabChooseListener);
		buttonHull.addListener(tabChooseListener);
		buttonWeaponry.addListener(tabChooseListener);
		buttonTools.addListener(tabChooseListener);
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
		selections.add(buttonTools);
		selections.row();
		selections.add(info).colspan(5);

		stage.addActor(selections);

		stage.addAction(sequence(moveTo(0, stage.getHeight()), moveTo(0, 0, .5f))); // coming in from top animation
	}

	/**
	 * Clears all data that needs to be refreshed when switching tabs.
	 */
	private void clear() {
		tabbed.clear();
		info.clear();
		activePart = null;
		deleteMode = false;
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

	public void normalize() {
		// TODO take the cockpit's grid position and move it to 0, 0.
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
