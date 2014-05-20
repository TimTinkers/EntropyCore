package us.rockhopper.entropy.screen;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import us.rockhopper.entropy.Entropy;
import us.rockhopper.entropy.utility.Resolution;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class Settings implements Screen {

	private Stage stage;
	private Table table;
	private Skin skin;

	/** @return if vSync is enabled */
	public static boolean vSync() {
		return Gdx.app.getPreferences(Entropy.TITLE).getBoolean("vsync");
	}

	/** @return if fullscreen is enabled */
	public static boolean fullscreen() {
		return Gdx.app.getPreferences(Entropy.TITLE).getBoolean("fullscreen");
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
		table.invalidateHierarchy();
	}

	@Override
	public void show() {
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		skin = new Skin(Gdx.files.internal("assets/ui/uiskin.json"),
				new TextureAtlas("assets/ui/uiskin.pack"));
		table = new Table(skin);
		table.setFillParent(true);

		final CheckBox vSyncCheckBox = new CheckBox("vSync", skin);
		vSyncCheckBox.setChecked(vSync());

		final CheckBox fullscreenCheckBox = new CheckBox("Fullscreen", skin);
		fullscreenCheckBox.setChecked(fullscreen());

		final TextButton back = new TextButton("BACK", skin);
		back.pad(10);

		final SelectBox<Resolution> resolutions = new SelectBox<Resolution>(
				skin);
		resolutions.setItems(new Resolution(852, 480),
				new Resolution(1280, 720), new Resolution(1365, 768),
				new Resolution(1600, 900), new Resolution(1920, 1080));
		final TextButton apply = new TextButton("APPLY", skin);

		ClickListener buttonHandler = new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				// event.getListenerActor() returns the source of the event,
				// e.g. a button that was clicked
				if (event.getListenerActor() == vSyncCheckBox) {
					// save vSync
					Gdx.app.getPreferences(Entropy.TITLE).putBoolean("vsync",
							vSyncCheckBox.isChecked());

					// set vSync
					Gdx.graphics.setVSync(vSync());

					Gdx.app.log(Entropy.TITLE, "vSync "
							+ (vSync() ? "enabled" : "disabled"));
				} else if (event.getListenerActor() == back) {
					// save the settings to preferences file
					// (Preferences.flush() writes the preferences in memory to
					// the file)
					Gdx.app.getPreferences(Entropy.TITLE).flush();
					Gdx.app.log(Entropy.TITLE, "settings saved");

					stage.addAction(sequence(moveTo(0, stage.getHeight(), .5f),
							run(new Runnable() {

								@Override
								public void run() {
									((Game) Gdx.app.getApplicationListener())
											.setScreen(new MainMenu());
								}
							})));
				} else if (event.getListenerActor() == apply) {
					Gdx.app.getPreferences(Entropy.TITLE).flush();
					Resolution resolution = resolutions.getSelected();
					Gdx.app.log(Entropy.TITLE,
							"resolution of " + resolution.toString()
									+ " selected");

					Gdx.app.getPreferences(Entropy.TITLE).putString(
							"resolution", resolution.toString());
					// set resolution
					Gdx.graphics.setDisplayMode(resolution.x, resolution.y,
							fullscreenCheckBox.isChecked());
				} else if (event.getListenerActor() == fullscreenCheckBox) {
					Resolution resolution = resolutions.getSelected();
					// save fullscreen
					Gdx.app.getPreferences(Entropy.TITLE).putBoolean(
							"fullscreen", vSyncCheckBox.isChecked());

					// set fullscreen
					Gdx.graphics.setDisplayMode(resolution.x, resolution.y,
							fullscreen());

					Gdx.app.log(Entropy.TITLE, "fullscren "
							+ (fullscreen() ? "enabled" : "disabled"));
				}
			}
		};

		vSyncCheckBox.addListener(buttonHandler);
		back.addListener(buttonHandler);
		apply.addListener(buttonHandler);
		fullscreenCheckBox.addListener(buttonHandler);

		// putting everything in the table
		table.add(new Label("SETTINGS", skin, "default")).spaceBottom(50)
				.colspan(3).expandX().row();
		table.add(vSyncCheckBox).top().expandY();
		table.add(fullscreenCheckBox).top().expandY();
		table.row();
		table.add(resolutions).left().expandY();
		table.add(apply);
		table.add(back).bottom().right();

		stage.addActor(table);

		stage.addAction(sequence(moveTo(0, stage.getHeight()),
				moveTo(0, 0, .5f))); // coming in from top animation
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
		stage.dispose();
		skin.dispose();
	}

}
