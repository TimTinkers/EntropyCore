package us.rockhopper.entropy.gui;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.util.ArrayList;

import us.rockhopper.entropy.entities.Ship;
import us.rockhopper.entropy.screen.TestFlight;
import us.rockhopper.entropy.utility.Part;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class ShipSelectDialog extends Dialog {
	Skin skin;
	ArrayList<Part> parts = new ArrayList<Part>();
	String shipToPlay = "";
	ClickListener shipChosen = new ClickListener() {
		@Override
		public void clicked(final InputEvent event, float x, float y) {
			if (parts == null || parts.isEmpty()) {
				System.out.println("You chose " + event.getListenerActor().getName());
				((Game) Gdx.app.getApplicationListener()).setScreen(new TestFlight(event.getListenerActor().getName()));
			} else {
				new Dialog("", skin) {
					{
						text("Warning! All unsaved progress will be lost if you continue.");
						button("Okay", true);
						button("Cancel", false);
					}

					protected void result(Object object) {
						boolean bool = (Boolean) object;
						if (bool == true) {
							// TODO this is still null
							((Game) Gdx.app.getApplicationListener()).setScreen(new TestFlight(event.getListenerActor()
									.getName()));
						} else {
							this.addAction(sequence(alpha(1f), Actions.delay(0.3f), alpha(0f, 0.6f),
									Actions.removeActor()));
						}
					}
				}.show(getStage()).addAction(sequence(alpha(0f), alpha(1f, 0.4f)));
			}
		}
	};

	public ShipSelectDialog(String title, Skin skin, ArrayList<Ship> ships, ArrayList<Part> parts) {
		super(title, skin);
		this.skin = skin;
		this.getContentTable().defaults().fillX();
		this.parts = parts;
		if (!ships.isEmpty()) {
			text("Which ship would you like to test?\n");
			for (Ship ship : ships) {
				this.getContentTable().row();
				TextButton button = new TextButton(ship.getName(), skin);
				button.setName(ship.getName());
				button.addListener(shipChosen);
				this.getContentTable().add(button);
			}
		} else {
			text("You need to make a ship before you can test one.");
		}
		button("Cancel", "cancel");
	}

	@Override
	protected void result(Object object) {
		String result = (String) object;
		if (result.equals("cancel")) {
			this.addAction(sequence(alpha(1f), Actions.delay(0.3f), alpha(0f, 0.6f), Actions.removeActor()));
		}
	}

	public String getShipName() {
		return shipToPlay;
	}
}
