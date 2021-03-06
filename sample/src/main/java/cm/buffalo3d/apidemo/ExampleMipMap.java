package cm.buffalo3d.apidemo;

import android.graphics.Bitmap;

import cm.buffalo3d.engine.core.Object3dContainer;
import cm.buffalo3d.engine.core.RendererActivity;
import cm.buffalo3d.engine.objectPrimitives.Box;
import cm.buffalo3d.engine.util.Utils;
import cm.buffalo3d.engine.vos.Light;

/**
 * Demonstrates visual difference between a texture with mipmapping versus a texture without.
 * The cube without the mipmapped texture displays distracting, aliasing artifacts.  
 * 
 * Note how mipmapping is set on or off at the TextureManager level.   
 * (It is not controlled at the Object3d level, or the TextureVO level)
 * 
 * Mipmapping requires the target hardware to support OpenGL ES 1.1.
 * If it does not, the request to generate the MIP maps is ignored.
 */
public class ExampleMipMap extends RendererActivity
{
	Object3dContainer _holder;
	Object3dContainer _cubeWithMipMap;
	Object3dContainer _cubeWithoutMipMap;
	
	public void initScene() 
	{
		scene.lights().add(new Light());
		
		_holder = new Object3dContainer(getGContext(), 0, 0);
		scene.addChild(_holder);
		
		_cubeWithMipMap = new Box(getGContext(),1.5f,1.5f,1.5f);
		_cubeWithMipMap.position().y = 1f;
		_holder.addChild(_cubeWithMipMap);

		_cubeWithoutMipMap = new Box(getGContext(),1.5f,1.5f,1.5f);
		_cubeWithoutMipMap.position().y = -1f;
		_holder.addChild(_cubeWithoutMipMap);
		
		//

		Bitmap b = Utils.makeBitmapFromResourceId(this, R.drawable.checkerboard);
		
		getGContext().getTexureManager().addTextureId(b, "checkerboard_with_mipmap", true);
		getGContext().getTexureManager().addTextureId(b, "checkerboard_without_mipmap", false);

		_cubeWithMipMap.getTextures().addById("checkerboard_with_mipmap");
		_cubeWithoutMipMap.getTextures().addById("checkerboard_without_mipmap");

		b.recycle();
	}
	
	@Override 
	public void updateScene() 
	{
		_holder.rotation().y +=1;
		_holder.rotation().z +=.25;
	}
}
