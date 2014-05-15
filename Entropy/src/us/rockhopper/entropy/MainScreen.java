package us.rockhopper.entropy;

import org.mini2Dx.core.game.ScreenBasedGame;

public class MainScreen extends ScreenBasedGame {
    @Override
    public void initialise() {
        this.addScreen(new LoadingScreen());
        this.addScreen(new InGameScreen());
    }

    @Override
    public int getInitialScreenId() {
        return LoadingScreen.ID;
    }
}
