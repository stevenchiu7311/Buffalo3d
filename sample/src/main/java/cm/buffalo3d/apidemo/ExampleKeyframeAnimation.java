package cm.buffalo3d.apidemo;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import cm.buffalo3d.engine.animation.AnimationObject3d;
import cm.buffalo3d.engine.core.RendererActivity;
import cm.buffalo3d.engine.parser.IParser;
import cm.buffalo3d.engine.parser.Parser;
import cm.buffalo3d.engine.vos.Light;


public class ExampleKeyframeAnimation extends RendererActivity implements View.OnClickListener
{
	private AnimationObject3d ogre;	
	private Button flipButton;
	private Button saluteButton;
	private CheckBox checkBox;
	
	@Override
	protected void onCreateSetContentView()
	{
		setContentView(R.layout.keyframe_anim_layout);
		
        LinearLayout ll = (LinearLayout) this.findViewById(R.id.scene2Holder);
        ll.addView(getGlSurfaceView());
        
        flipButton = (Button) this.findViewById(R.id.FlipButton);
        flipButton.setOnClickListener(this);
        saluteButton = (Button) this.findViewById(R.id.SaluteButton);
        saluteButton.setOnClickListener(this);
        
        checkBox = (CheckBox) this.findViewById(R.id.CheckBox01);
	}

    public void onClick(View $v)
    {
    	if($v == flipButton && checkBox.isChecked())
    		ogre.play("flip", true);
    	else if($v == flipButton && !checkBox.isChecked())
    		ogre.play("flip", false);
    	else if($v == saluteButton && checkBox.isChecked())
    		ogre.play("salute", true);
    	else if($v == saluteButton && !checkBox.isChecked())
    		ogre.play("salute", false);
    }
    
    //
	
	public void initScene() 
	{
		scene.lights().add(new Light());
		
		IParser parser = Parser.createParser(getGContext(), Parser.Type.MD2,
				getResources(), getPackageName() + ":raw/ogro", false);
		parser.parse();

		ogre = parser.getParsedAnimationObject();
		ogre.scale().x = ogre.scale().y = ogre.scale().z = .07f;
		ogre.rotation().z = -90;
		ogre.rotation().x = -90;
		scene.addChild(ogre);
		ogre.setFps(70);
	}

	@Override 
	public void updateScene() 
	{
		
	}	
}

