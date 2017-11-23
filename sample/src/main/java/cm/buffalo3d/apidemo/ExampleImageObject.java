package cm.buffalo3d.apidemo;

import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView.ScaleType;

import cm.buffalo3d.engine.component.ImageObject;
import cm.buffalo3d.engine.component.TextObject;
import cm.buffalo3d.engine.core.Object3d;
import cm.buffalo3d.engine.core.RendererActivity;
import cm.buffalo3d.engine.objectPrimitives.Box;
import cm.buffalo3d.engine.objectPrimitives.Sphere;
import cm.buffalo3d.engine.vos.Color4;
import cm.buffalo3d.engine.vos.Light;


/**
 * Most simple text object example. Text object function will be extended soon.
 *
 * @author steven
 */
public class ExampleImageObject extends RendererActivity {
    final static float TEXT_WIDTH = 1.0f;
    final static float TEXT_HEIGHT = 1.0f;
    final static int TEXT_FONT_SIZE = 20;

    ImageObject mImageObject;
    ImageObject mImageObjectShpere;
    ImageObject mImageObjectBox;

    public void initScene() {
        scene.lights().add(new Light());
        Object3d objectBox= new Box(getGContext(),1,1,1, null, true,true,true);
        Object3d objectSphere = new Sphere(getGContext(), 0.4f, 15, 10, true,true,false);
        mImageObject = new ImageObject(getGContext(), TEXT_WIDTH,
                TEXT_HEIGHT, 0f);
        mImageObject.defaultColor(new Color4(255,0,0,255));
        mImageObject.setScaleType(ScaleType.CENTER);
        mImageObject.setBackgroundResource(R.drawable.jupiter);
        mImageObject.setImageResource(R.drawable.icon);
        mImageObject.doubleSidedEnabled(true);
        mImageObject.position().x = -1.0f;
        mImageObject.getTextures();

        mImageObjectShpere = new ImageObject(getGContext(), TEXT_WIDTH,
                TEXT_HEIGHT, 0f);
        mImageObjectShpere.defaultColor(new Color4(255,0,0,255));
        mImageObjectShpere.setBackgroundResource(R.drawable.jupiter);
        mImageObjectShpere.setShape(objectSphere);
        mImageObjectShpere.position().x = 0.0f;

        mImageObjectBox = new ImageObject(getGContext(), TEXT_WIDTH,
                TEXT_HEIGHT, 0f);
        mImageObjectBox.defaultColor(new Color4(255,0,0,255));
        mImageObjectBox.setBackgroundResource(R.drawable.jupiter);
        mImageObjectBox.setImageResource(R.drawable.icon);
        mImageObjectBox.setShape(objectBox);
        mImageObjectBox.position().x = 1.3f;

        scene.addChild(mImageObject);
        scene.addChild(mImageObjectShpere);
        scene.addChild(mImageObjectBox);

    }

    @Override
    public void updateScene()
    {
        /*
         * Do any manipulation of scene properties or to objects in the scene here.
         */
        if (mImageObject != null) {
            mImageObjectBox.rotation().y++;
            mImageObjectShpere.rotation().y++;
        }
    }
}