package cm.buffalo3d.apidemo;

import cm.buffalo3d.engine.core.Object3dContainer;
import cm.buffalo3d.engine.core.RendererActivity;
import cm.buffalo3d.engine.objectPrimitives.Box;
import cm.buffalo3d.engine.vos.Light;

/**
 * Most minimal example I could think of.
 * 
 * @author Lee
 */
public class ExampleMostMinimal extends RendererActivity
{
	Object3dContainer _cube;
	
	public void initScene() 
	{
		/*
		 * Add a light to the Scene.
		 * The Scene must have light for Object3d's with normals  
		 * enabled (which is the default setting) to be visible.
		 */
		scene.lights().add( new Light() );
		
		/*
		 *  Create an Object3d and add it to the scene.
		 *  In this case, we're creating a cube using the Box class, which extends Object3d.
		 *  Any Object3d must be declared with booleans that determine whether its vertices store: 
		 *  	(a) U/V texture coordinates 
		 *  	(b) Normals (required for shading based on light source/s)
		 *  	(c) Per-vertex color information 
		 *  We're going to create a shaded cube without textures or colors, so for those arguments
		 *  we are using "false,true,false".  
		 */
		_cube = new Box(getGContext(),1,1,1, null, true,true,false);
		
		/*
		 * 	Since we're not using any colors on the cube, we're setting this to false.  
		 * (False is the default)
		 */
		_cube.colorMaterialEnabled(false);
		
		/*
		 * Add cube to the scene.
		 */
		scene.addChild(_cube);
	}

	@Override 
	public void updateScene() 
	{
		/*
		 * Do any manipulation of scene properties or to objects in the scene here.
		 */
		_cube.rotation().y++;
	}
}
