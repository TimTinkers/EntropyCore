package us.rockhopper.entropy.utility;

import java.lang.reflect.Type;

import net.dermetfan.utils.libgdx.graphics.Box2DSprite;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.google.gson.InstanceCreator;

public class TextureDataInstanceCreator implements InstanceCreator<TextureData> {

	@Override
	public TextureData createInstance(Type type) {
		return new Box2DSprite(new Sprite(new Texture(
				"assets/img/sampleShip.png"))).getTexture().getTextureData();
	}
}
