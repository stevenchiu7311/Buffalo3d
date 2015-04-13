package buffalo3d.apidemo;

import android.graphics.Bitmap;

import buffalo3d.core.Object3dContainer;
import buffalo3d.core.RendererActivity;
import buffalo3d.objectPrimitives.Box;
import buffalo3d.util.Utils;
import buffalo3d.vos.Light;
import buffalo3d.vos.TextureVo;

/**
 * Not ready yet. :)
 * 
 * @author Lee
 */
public class ExampleAnimatedTexture extends RendererActivity
{
	Object3dContainer _cube;
	
	TextureVo _jupiterTexture;
	
	
	public void initScene() 
	{
		Light light = new Light();
		light.ambient.setAll(0xff888888);
		light.position.setAll(3,0,3);
		scene.lights().add(light);

		// Create objects
		
		// * Note:
		// ------- 
		// While the order in which objects are drawn is not important in terms of
		// z-ordering (which is taken care of automatically by OpenGL), the order 
		// _is_ important when it comes to transparency. Here, for the transparency
		// of the "_jupiter" sphere to be apparent, it must added as the second child
		// of the scene (which means it will be drawn second). The engine does not 
		// manage this automatically for you.

		_cube = new Box(getGContext(),1.3f,1.3f,1.3f);
		_cube.position().x = +0.4f;
		_cube.normalsEnabled(false);
		scene.addChild(_cube);

		// Add textures in TextureManager
		
		Bitmap b;
		
		b = Utils.makeBitmapFromResourceId(getGContext().getContext(),R.drawable.jupiter);
		getGContext().getTexureManager().addTextureId(b, "jupiter", false);
		b.recycle();
		
		// Add textures to sphere
		_cube.getTextures().addById("jupiter");
	}
	
	@Override 
	public void updateScene() 
	{
		_cube.rotation().x -= .4f;
		_cube.rotation().y -= .6f;
	}
}
