package cm.buffalo3d.apidemo;

import android.widget.LinearLayout;

import cm.buffalo3d.engine.core.Object3dContainer;
import cm.buffalo3d.engine.core.RendererActivity;
import cm.buffalo3d.engine.objectPrimitives.Box;
import cm.buffalo3d.engine.objectPrimitives.Sphere;
import cm.buffalo3d.engine.vos.Light;

/**
 * 'Scratch' - ignore.
 * 
 * @author Lee
 */
public class ScratchActivity extends RendererActivity 
{
	Object3dContainer _o1;
	Object3dContainer _o2;
	Object3dContainer _o3;
	
	Object3dContainer _k;
	
	Light _light;
	
	@Override
	protected void onCreateSetContentView()
	{
		setContentView(R.layout.scratch_layout);
		
		// Add OpenGL surface
        LinearLayout ll = (LinearLayout) this.findViewById(R.id.sceneHolder);
        ll.addView(getGlSurfaceView());
	}
	
	@Override
	public void initScene() 
	{
		scene.backgroundColor().setAll(0x0);

		_light = new Light();
		_light.position.setAll(0, 0, +3);
		_light.diffuse.setAll(255, 255, 255, 255);
		_light.ambient.setAll(0, 0, 0, 0);
		_light.specular.setAll(0, 0, 0, 0);
		_light.emissive.setAll(0, 0, 0, 0);
		scene.lights().add(_light);

		_o1 = new Box(getGContext(),1f,1f,1f);
		scene.addChild(_o1);
		
		_o2 = new Sphere(getGContext(),0.5f, 10,10);
		_o2.position().x = 1.0f;
		_o1.addChild(_o2);
		
		_o3 = new Sphere(getGContext(),0.5f, 10,10);
		_o3.position().x = 0.75f;
		_o2.addChild(_o3);
		
		_k = _o1.clone();
		_k.position().y = -2f;
		scene.addChild(_k);
	}

	@Override 
	public void updateScene() 
	{
		_o1.rotation().y += 0.33;
		_k.rotation().y -= 0.33;
	}
}
