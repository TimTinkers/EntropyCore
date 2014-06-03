package us.rockhopper.entropy.screen;

import us.rockhopper.entropy.network.MultiplayerClient;
import us.rockhopper.entropy.network.MultiplayerServer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class DuelLobby extends ScreenAdapter {

	private Label label;
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
		if (label != null && client != null) {
			label.setText(client.getLine());
		}
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

		final TextField chat = new TextField("Enter a message", skin);
		final TextField ip = new TextField("Enter an IP", skin);
		label = new Label("Empty message", skin);

		TextButton send = new TextButton("Send", skin, "default");
		ClickListener sendListener = new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				client.sendMessage(chat.getText());
			}
		};

		TextButton serverStartButton = new TextButton("Start Server", skin, "default");
		ClickListener serverStartListener = new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				server = new MultiplayerServer();
			}
		};

		TextButton clientStartButton = new TextButton("Start Client", skin, "default");
		ClickListener clientStartListener = new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				client = new MultiplayerClient(ip.getText());
			}
		};

		send.addListener(sendListener);
		serverStartButton.addListener(serverStartListener);
		clientStartButton.addListener(clientStartListener);
		table.add(serverStartButton).row().row();
		table.add(ip).row();
		table.add(clientStartButton).row().row();
		table.add(chat).colspan(10);
		table.add(send);
		table.row();
		table.add().row();
		table.add().row();
		table.add().row();
		table.add(label).row();
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
