package min3d.sampleProject1;

import min3d.Utils;
import min3d.core.Object3dContainer;
import min3d.core.RendererActivity;
import min3d.objectPrimitives.Sphere;
import min3d.vos.Light;
import min3d.vos.TextureVo;
import android.graphics.Bitmap;

/**
 * Shows how objects can be assigned textures dynamically.
 * 
 * Also demonstrates how the same "textureId" can be assigned to multiple objects 
 * (rather than, say, needing to create two instances of a texture 
 * for two separate objects) 
 */
public class ExampleAssigningTexturesDynamically extends RendererActivity
{
	Object3dContainer _object1;
	Object3dContainer _object2;
	
	TextureVo _jupiterTexture;
	TextureVo _earthTexture;
	TextureVo _moonTexture;
	
	int _count;
	
	
	public void initScene() 
	{
		scene.lights().add(new Light());
		
		// Add three textures to TextureManager 
		
		Bitmap b;
		
		b = Utils.makeBitmapFromResourceId(getGContext().getContext(), R.drawable.jupiter);
		getGContext().getTexureManager().addTextureId(b, "jupiter", false);
		b.recycle();
		
		b = Utils.makeBitmapFromResourceId(getGContext().getContext(), R.drawable.earth);
		getGContext().getTexureManager().addTextureId(b, "earth", false);
		b.recycle();
		
		b = Utils.makeBitmapFromResourceId(getGContext().getContext(), R.drawable.moon);
		getGContext().getTexureManager().addTextureId(b, "moon", false);
		b.recycle();
		
		// Create three TextureVo's
		
		_jupiterTexture = new TextureVo("jupiter");
		_earthTexture = new TextureVo("earth");
		_moonTexture = new TextureVo("moon");
		
		_object1 = new Sphere(getGContext(), 0.8f, 15, 10);
		_object1.position().y = 0.9f;
		scene.addChild(_object1);

		_object2 = new Sphere(getGContext(), 0.8f, 15, 10);
		_object2.position().y = -0.9f;
		scene.addChild(_object2);
		
		_count = 0;
	}
	
	@Override 
	public void updateScene() 
	{
		_count++;
		
		// Assign a different texture to the two objects
		// every second or so
		
		if (_count % 240 == 0) {
			_object1.getTextures().clear(); // ie, no texture
			_object2.getTextures().clear();
		}
		else if (_count % 240 == 60) { 
			_object1.getTextures().addReplace(_jupiterTexture);
			_object2.getTextures().addReplace(_jupiterTexture);
		}
		else if (_count % 240 == 120) { 
			_object1.getTextures().addReplace(_earthTexture);
			_object2.getTextures().addReplace(_earthTexture);
		}
		else if (_count % 240 == 180) { 
			_object1.getTextures().addReplace(_moonTexture);
			_object2.getTextures().addReplace(_moonTexture);
		}
		
		_object1.rotation().y +=1;
		_object2.rotation().y -=1;
	}
}
