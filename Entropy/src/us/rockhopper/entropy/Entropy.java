package us.rockhopper.entropy;

import us.rockhopper.entropy.screen.Splash;

import com.badlogic.gdx.Game;

public class Entropy extends Game {
	public static final String VERSION = "0.0.2";

	@Override
	public void create() {
		setScreen(new Splash());
	}
}
