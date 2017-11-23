package cm.buffalo3d.apidemo;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import cm.buffalo3d.engine.core.Object3dContainer;
import cm.buffalo3d.engine.core.RendererActivity;
import cm.buffalo3d.engine.objectPrimitives.Box;
import cm.buffalo3d.engine.vos.Light;

/**
 * Example of adding an OpenGL scene within a conventional Android application layout.
 * Entails overriding RenderActivity's onCreateSetContentView() function, and
 * adding _glSurfaceView to the appropriate View...  
 * 
 * @author Lee
 */
public class ExampleInsideLayout extends RendererActivity implements View.OnClickListener
{
	Object3dContainer _cube;
	
	@Override
	protected void onCreateSetContentView()
	{
		setContentView(R.layout.custom_layout_example);
		
        LinearLayout ll = (LinearLayout) this.findViewById(R.id.scene1Holder);
        ll.addView(getGlSurfaceView());
        
        Button b;
        b = (Button) this.findViewById(R.id.layoutOkay);
        b.setOnClickListener(this);
        b = (Button) this.findViewById(R.id.layoutCancel);
        b.setOnClickListener(this);
	}

    public void onClick(View $v)
    {
    	finish();
    }
    
    //
	
	public void initScene() 
	{
		scene.lights().add(new Light());
		
		scene.backgroundColor().setAll(0xff444444);
		_cube = new Box(getGContext(),1,1,1);
		scene.addChild(_cube);
	}

	@Override 
	public void updateScene() 
	{
		_cube.rotation().y++;
	}
	
}

