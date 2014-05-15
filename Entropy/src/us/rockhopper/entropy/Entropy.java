package us.rockhopper.entropy;

import us.rockhopper.entropy.screen.Splash;

import com.badlogic.gdx.Game;

public class Entropy extends Game {
	
	@Override
	public void create() {
		setScreen(new Splash());
	}
}
