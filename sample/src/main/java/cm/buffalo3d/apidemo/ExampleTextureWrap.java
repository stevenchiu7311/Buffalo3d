package cm.buffalo3d.apidemo;

import android.graphics.Bitmap;

import cm.buffalo3d.engine.core.Object3dContainer;
import cm.buffalo3d.engine.core.RendererActivity;
import cm.buffalo3d.engine.objectPrimitives.HollowCylinder;
import cm.buffalo3d.engine.util.Utils;
import cm.buffalo3d.engine.vos.TextureVo;

/**
 * Demonstrates setting U/V texture wrapping 
 * (TextureVo.repeatU and TextureVo.repeatV)
 * 
 * @author Lee
 */
public class ExampleTextureWrap extends RendererActivity
{
	Object3dContainer _object;
	TextureVo _texture;
	int _counter;
	
	public void initScene() 
	{
		_object = new HollowCylinder(getGContext(), 1f, 0.5f, 0.66f, 25);
		_object.normalsEnabled(false);
		_object.vertexColorsEnabled(false);
		scene.addChild(_object);
		
		Bitmap b = Utils.makeBitmapFromResourceId(getGContext().getContext(), R.drawable.uglysquares);
		getGContext().getTexureManager().addTextureId(b, "texture", true);
		b.recycle();
		
		_texture = new TextureVo("texture");
		
		_object.getTextures().add(_texture);
		
		_counter = 0;
	}

	@Override 
	public void updateScene() 
	{
		_object.rotation().y = (float)(Math.sin(_counter*0.02f) * 45);
		
		if (_counter % 40 == 0) 
		{
			_texture.repeatU = ! _texture.repeatU;
		}
		if (_counter % 80 == 0) 
		{
			_texture.repeatV = ! _texture.repeatV;
		}
		
		_counter++;
	}
}
