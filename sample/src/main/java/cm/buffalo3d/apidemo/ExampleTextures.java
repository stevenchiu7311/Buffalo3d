package cm.buffalo3d.apidemo;

import android.graphics.Bitmap;

import cm.buffalo3d.engine.core.Object3dContainer;
import cm.buffalo3d.engine.core.RendererActivity;
import cm.buffalo3d.engine.objectPrimitives.Box;
import cm.buffalo3d.engine.util.Utils;
import cm.buffalo3d.engine.vos.Light;
import cm.buffalo3d.engine.vos.TextureVo;

/**
 * Basic example using a texture on an object
 */
public class ExampleTextures extends RendererActivity
{
	Object3dContainer _cube;
	
	public void initScene() 
	{
		scene.lights().add(new Light());
		
		_cube = new Box(getGContext(),1.5f,1.5f,1.5f);
		scene.addChild(_cube);

		// Create a Bitmap. Here we're generating it from an embedded resource,
		// but the Bitmap could be created in any manner (eg, dynamically).
		
		Bitmap b = Utils.makeBitmapFromResourceId(this, R.drawable.uglysquares);
		
		// Upload the Bitmap via TextureManager and assign it a 
		// textureId ("uglysquares").
		
		getGContext().getTexureManager().addTextureId(b, "uglysquares", false);
		
		// Unless you have a specific reason for doing so, recycle the Bitmap,
		// as it is no longer necessary.
		
		b.recycle();

		// Create a TextureVo using the textureId that was previously added 
		// to the TextureManager ("uglysquares").
		
		TextureVo texture = new TextureVo("uglysquares");
		
		// Add it to the TexturesList held by the Object3d, 
		// and it will be duly rendered.

		_cube.getTextures().add(texture);
	}
	
	@Override 
	public void updateScene() 
	{
		_cube.rotation().y +=1;
		_cube.rotation().z += 0.2f;
	}
}
