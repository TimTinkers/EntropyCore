package us.rockhopper.entropy.screen;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import us.rockhopper.entropy.network.MultiplayerClient;
import us.rockhopper.entropy.network.MultiplayerServer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class ConnectionScreen extends ScreenAdapter {

	private Stage stage;
	private Table table;
	private Skin skin;
	private MultiplayerServer server;
	private MultiplayerClient client;

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();

		Table.drawDebug(stage);
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
		table.invalidateHierarchy();
	}

	@Override
	public void show() {
		skin = new Skin(Gdx.files.internal("assets/ui/uiskin.json"), new TextureAtlas("assets/ui/uiskin.pack"));
		stage = new Stage();
		table = new Table(skin);
		table.setFillParent(true);
		table.debug();

		Gdx.input.setInputProcessor(stage);

		final TextField ip = new TextField("Enter an IP", skin);

		TextButton serverStartButton = new TextButton("Host a Game", skin, "default");
		ClickListener serverStartListener = new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				server = new MultiplayerServer();
				client = new MultiplayerClient("localhost");
				stage.addAction(sequence(alpha(1f), alpha(0f, .6f), run(new Runnable() {

					@Override
					public void run() {
						table.clear();
					}
				})));
			}
		};

		TextButton clientStartButton = new TextButton("Join a Game", skin, "default");
		ClickListener clientStartListener = new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				client = new MultiplayerClient(ip.getText());
			}
		};

		serverStartButton.addListener(serverStartListener);
		clientStartButton.addListener(clientStartListener);
		table.add(ip);
		table.add(clientStartButton).row();
		table.add(serverStartButton).row();

		stage.addActor(table);
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
