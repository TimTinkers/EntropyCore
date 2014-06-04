package us.rockhopper.entropy;

import us.rockhopper.entropy.screen.MainMenu;

import com.badlogic.gdx.Game;

public class Entropy extends Game {
	public static final String VERSION = "0.1";
	public static final String TITLE = "Entropy";

	@Override
	public void create() {
		setScreen(new MainMenu());
	}
}
