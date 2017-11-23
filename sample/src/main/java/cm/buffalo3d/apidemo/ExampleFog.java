package cm.buffalo3d.apidemo;

import android.graphics.Bitmap;

import cm.buffalo3d.engine.core.RendererActivity;
import cm.buffalo3d.engine.objectPrimitives.Box;
import cm.buffalo3d.engine.objectPrimitives.Rectangle;
import cm.buffalo3d.engine.util.Utils;
import cm.buffalo3d.engine.vos.Color4;
import cm.buffalo3d.engine.vos.Light;

public class ExampleFog extends RendererActivity {
	private Box[] boxes;
	
	@Override
	public void initScene() {
		Light light = new Light();
    	scene.lights().add(light);
    	scene.camera().position.x = 0;
    	scene.camera().position.y = 0;
    	scene.camera().position.z = 10;
    	
		Bitmap b = Utils.makeBitmapFromResourceId(getGContext().getContext(), R.drawable.barong);
		getGContext().getTexureManager().addTextureId(b, "barong", false);
		b.recycle();
		
		b = Utils.makeBitmapFromResourceId(getGContext().getContext(),R.drawable.wood);
		getGContext().getTexureManager().addTextureId(b, "wood", false);
		b.recycle();
    	
		boxes = new Box[5];
		
		for(int i=0; i<5; i++)
		{
			Box box = new Box(getGContext(), 1, 1, 1);
			box.position().x = (float) (-4 + ( Math.random() * 8));
			box.position().y = (float) (-4 + ( Math.random() * 8));
			box.position().z = (i + 1) * -8;
			box.getTextures().addById("barong");
			box.vertexColorsEnabled(false);
			boxes[i] = box;
	   		scene.addChild(box);
		}
   		
   		Color4 planeColor = new Color4(255, 255, 255, 255);
		Rectangle east = new Rectangle(getGContext(), 40, 12, 2, 2, planeColor);
		Rectangle west = new Rectangle(getGContext(), 40, 12, 2, 2, planeColor);
		Rectangle up = new Rectangle(getGContext(), 40, 12, 2, 2, planeColor);
		Rectangle down = new Rectangle(getGContext(), 40, 12, 2, 2, planeColor);
   		
		east.position().x = -6;
		east.rotation().y = -90;
		east.position().z = -20;
		east.lightingEnabled(false);
		east.getTextures().addById("wood");
		
		west.position().x = 6;
		west.rotation().y = 90;
		west.position().z = -20;
		west.lightingEnabled(false);
		west.getTextures().addById("wood");
		
		up.rotation().x = -90;
		up.rotation().z = 90;
		up.position().y = 6;
		up.position().z = -20;
		up.lightingEnabled(false);
		up.getTextures().addById("wood");
		
		down.rotation().x = 90;
		down.rotation().z = 90;
		down.position().y = -6;
		down.position().z = -20;
		down.lightingEnabled(false);
		down.getTextures().addById("wood");

   		scene.addChild(east);
   		scene.addChild(west);
   		scene.addChild(up);
   		scene.addChild(down);
   		
   		scene.fogColor(new Color4(0, 0, 0, 255) );
   		scene.fogNear(10);
   		scene.fogFar(40);
   		scene.fogEnabled(true);
	}
	
	@Override 
	public void updateScene() 
	{
		for(int i=0; i<5; i++)
		{
			Box box = boxes[i];
			box.position().z += .25;
			box.rotation().x++;
			box.rotation().y++;
			if( box.position().z > scene.camera().position.z)
				box.position().z = -40;
		}
	}
}