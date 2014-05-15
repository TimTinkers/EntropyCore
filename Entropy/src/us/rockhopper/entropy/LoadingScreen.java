package us.rockhopper.entropy;

import org.mini2Dx.core.game.GameContainer;
import org.mini2Dx.core.graphics.Graphics;
import org.mini2Dx.core.screen.GameScreen;
import org.mini2Dx.core.screen.ScreenManager;
import org.mini2Dx.core.screen.Transition;
import org.mini2Dx.core.screen.transition.FadeInTransition;
import org.mini2Dx.core.screen.transition.FadeOutTransition;

public class LoadingScreen implements GameScreen {
    public static int ID = 1;

    private float loadingTime = 4f;

    public void initialise(GameContainer gc) {}

    public void update(GameContainer gc, ScreenManager<?> screenManager, float delta) {
        if(loadingTime > 0f) {
            loadingTime -= delta;
            if(loadingTime < 0f) {
                //Fade to InGameScreen after 4 seconds
                screenManager.enterGameScreen(InGameScreen.ID, new FadeOutTransition(),
            new FadeInTransition());
            }
        }
    }

    public void interpolate(GameContainer gc, float alpha) {
    }

    public void render(GameContainer gc, Graphics g) {
        g.drawString("Loading...", 32, 32);
    }

    public void preTransitionIn(Transition transitionIn) {
        // Called before transitioning in
    }

    public void postTransitionIn(Transition transitionIn) {
        // Called after transitioning in
    }

    public void preTransitionOut(Transition transitionOut) {
        // Called before transitioning out
    }

    public void postTransitionOut(Transition transitionOut) {
        // Called after transitioning out
    }

    public int getId() {
        return ID;
    }
}