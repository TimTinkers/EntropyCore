package us.rockhopper.entropy.gui;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.util.ArrayList;

import us.rockhopper.entropy.entities.Ship;
import us.rockhopper.entropy.screen.ShipEditor;
import us.rockhopper.entropy.utility.FileIO;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class ShipLoadDialog extends Dialog {

	TextButton selected;
	String shipToLoad;
	ButtonGroup toggleGroup = new ButtonGroup();
	Skin skin;
	ClickListener shipChosen = new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			TextButton button = (TextButton) event.getListenerActor();
			button.setStyle(skin.get("toggle", TextButtonStyle.class));
			selected = button;
			shipToLoad = button.getName();
		}
	};

	public ShipLoadDialog(String title, Skin skin, ArrayList<Ship> ships) {
		super(title, skin);
		this.getContentTable().defaults().fillX();
		this.skin = skin;
		this.setName("LoadDialog");
		if (!ships.isEmpty()) {
			text("Please select a ship to load.\n");
			for (Ship ship : ships) {
				this.getContentTable().row();
				TextButton button = new TextButton(ship.getName(), skin);
				button.setName(ship.getName());
				button.addListener(shipChosen);
				toggleGroup.add(button);
				this.getContentTable().add(button);
			}
			button("Load", "load");
			button("Delete", "delete");
		} else {
			text("You need to make a ship before you can load one.");
		}
		button("Cancel", "cancel");
	}

	@Override
	protected void result(Object object) {
		String result = (String) object;
		if (result.equals("cancel")) {
			this.addAction(sequence(alpha(1f), Actions.delay(0.3f), alpha(0f, 0.6f), Actions.removeActor()));
		} else if (result.equals("load")) {
			new Dialog("", skin) {
				{
					text("Warning! All unsaved progress will be lost if you continue.");
					button("Okay", true);
					button("Cancel", false);
				}

				protected void result(Object object) {
					boolean bool = (Boolean) object;
					if (bool == true) {
						((Game) Gdx.app.getApplicationListener()).setScreen(new ShipEditor(shipToLoad));
					} else {
						this.addAction(sequence(alpha(1f), Actions.delay(0.3f), alpha(0f, 0.6f), Actions.removeActor()));
					}
				}
			}.show(getStage()).addAction(sequence(alpha(0f), alpha(1f, 0.4f)));
			this.addAction(sequence(alpha(1f), Actions.delay(0.3f), alpha(0f, 0.6f), Actions.removeActor()));
		} else if (result.equals("delete")) {
			Dialog warning = new Dialog("", skin) {
				{
					text("Warning! Are you sure you want to delete this ship?");
					button("Yes", true);
					button("Cancel", false);
				}

				protected void result(Object object) {
					boolean bool = (Boolean) object;
					if (bool == true) {
						FileIO.delete("data/ships/" + shipToLoad + ".json");
						toggleGroup.remove(selected);
					} else {
						this.addAction(sequence(alpha(1f), Actions.delay(0.3f), alpha(0f, 0.6f), Actions.removeActor()));
					}
				}
			};
			warning.show(getStage()).addAction(sequence(alpha(0f), alpha(1f, 0.4f)));
			this.pack();
			this.cancel();
		}
	}

	public void refresh() {
		if (!toggleGroup.getButtons().contains(selected, true)) {
			this.getContentTable().removeActor(selected);
		}
	}
}
