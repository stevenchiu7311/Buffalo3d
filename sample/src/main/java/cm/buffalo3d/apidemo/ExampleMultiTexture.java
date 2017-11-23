package cm.buffalo3d.apidemo;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;

import cm.buffalo3d.engine.core.Object3dContainer;
import cm.buffalo3d.engine.core.RendererActivity;
import cm.buffalo3d.engine.objectPrimitives.Box;
import cm.buffalo3d.engine.objectPrimitives.Sphere;
import cm.buffalo3d.engine.util.Utils;
import cm.buffalo3d.engine.vos.Light;
import cm.buffalo3d.engine.vos.TexEnvxVo;
import cm.buffalo3d.engine.vos.TextureVo;

/**
 * This is the most complex example, for now.
 * 
 * Demonstrates:
 * - How to add multiple textures to an object (2, in this case)
 * - How to change 'texture environent' settings on a texture 
 * 
 * In this example, a sphere has an image of jupiter as its first texture.
 * Its second texture is a plain white image with a transparent 'hole' in the middle.
 * We'll see what's the effect of applying different texture environment 'blend modes'
 * to the second texture.  
 *  
 * @author Lee
 */
public class ExampleMultiTexture extends RendererActivity
{
	Object3dContainer _cube;
	Object3dContainer _sphere;
	
	TextureVo _jupiterTexture;
	TextureVo _alphaTexture;
	
	TexEnvxVo _alphaTextureEnv;
	
	int _count = 0;
	
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

		_sphere = new Sphere(getGContext(), .8f, 12, 9);
		_sphere.position().x = -0.2f;
		_sphere.position().z = 1f;
		scene.addChild(_sphere);

		// Add textures in TextureManager
		
		Bitmap b;
		
		b = Utils.makeBitmapFromResourceId(getGContext().getContext(), R.drawable.jupiter);
		getGContext().getTexureManager().addTextureId(b, "jupiter", false);
		b.recycle();
		
		b = Utils.makeBitmapFromResourceId(getGContext().getContext(), R.drawable.white_with_alpha_hole);
		getGContext().getTexureManager().addTextureId(b, "alpha", false);
		b.recycle();
		
		// Add textures to sphere
		_sphere.getTextures().addById("jupiter");
		TextureVo texture = _sphere.getTextures().addById("alpha");
		
		// We saved a reference to the sphere's "alpha" texture above so we can target 
		// its texture environment VO, which we will change around in the loop below.
		_alphaTextureEnv = texture.textureEnvs.get(0);
		
		_count = 0;
	}
	
	@Override 
	public void updateScene() 
	{
		_cube.rotation().x -= .4f;
		_cube.rotation().y -= .6f;
		_sphere.rotation().y += 3;
		
		// Cycle thru the texture environment 'blend modes' just so
		// we can see what they all look like...
		
		if (_count % 1000 == 0) {
			_alphaTextureEnv.setAll(GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE); // .. default
		}
		else if (_count % 1000 == 200) {
			_alphaTextureEnv.setAll(GL10.GL_TEXTURE_ENV_MODE, GL10.GL_BLEND);
		}
		else if (_count % 1000 == 400) {
			_alphaTextureEnv.setAll(GL10.GL_TEXTURE_ENV_MODE, GL10.GL_ADD);
		}
		else if (_count % 1000 == 600) {
			// Note, this is the correct setting if you're just interested  
			// in layering one bitmap that has some transparency on top of another,
			// without altering the color or alpha of the either.
			_alphaTextureEnv.setAll(GL10.GL_TEXTURE_ENV_MODE, GL10.GL_DECAL);
		}
		else if (_count % 1000 == 800) {
			_alphaTextureEnv.setAll(GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
		}
			
		_count++;
		
		
		// *
		//
		// Lastly, note that TextureVo holds not just one TextureEnvVo, but an ArrayList of them. 
		// So you can send a series of "glTexEnvx" commands, as if you were typing them out  
		// manually, to create more complex blending/layering effects.
		//
		// See http://www.khronos.org/opengles/sdk/1.1/docs/man/glTexEnv.xml
	}
}
