package cm.buffalo3d.apidemo;

import cm.buffalo3d.engine.core.Object3dContainer;
import cm.buffalo3d.engine.core.RendererActivity;
import cm.buffalo3d.engine.objectPrimitives.Box;

/**
 * @author Lee
 */
public class ExampleVertexColors extends RendererActivity
{
	Object3dContainer _cube;
	
	public void initScene() 
	{
		/**
		 * Rem, the Box class automatically adds vertex colors (a different color for each side).
		 */
		_cube = new Box(getGContext(),1,1,1);
		_cube.colorMaterialEnabled(true);
		scene.addChild(_cube);

		/**
		 * Turn off lighting so that colors come thru "as-is", without any changes in brightness based on
		 * any extant lights or light settings.
		 */
		scene.lightingEnabled(false);
	}
	
	@Override 
	public void updateScene() 
	{
		_cube.rotation().y +=1;
		_cube.rotation().z += 0.2f;
	}
}
