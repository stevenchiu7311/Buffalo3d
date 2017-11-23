package cm.buffalo3d.apidemo;

import android.graphics.Color;
import android.view.Gravity;

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
public class ExampleTextObject extends RendererActivity {
    final static float TEXT_WIDTH = 1f;
    final static float TEXT_HEIGHT = 1f;
    final static int TEXT_FONT_SIZE = 20;

    TextObject mTextObject;
    TextObject mTextObjectShpere;
    TextObject mTextObjectBox;

    public void initScene() {
        scene.lights().add(new Light());
        Object3d objectBox= new Box(getGContext(),1,1,1, null, true,true,true);
        Object3d objectSphere = new Sphere(getGContext(), 0.4f, 15, 10, true,true,false);
        mTextObject = new TextObject(getGContext(), TEXT_WIDTH,
                TEXT_HEIGHT, 0f);
        mTextObject.defaultColor(new Color4(255,0,0,255));
        mTextObject.doubleSidedEnabled(true);
        mTextObject.setTextSize(TEXT_FONT_SIZE);

        mTextObject.setGravity(Gravity.CENTER);
        mTextObject.setBackgroundResource(R.drawable.jupiter);
        mTextObject.setTextColor(Color.BLUE);
        mTextObject.setText("I love GL");
        mTextObject.position().x = -1.0f;

        mTextObjectShpere = new TextObject(getGContext(), TEXT_WIDTH,
                TEXT_HEIGHT, 0f);
        mTextObjectShpere.defaultColor(new Color4(255,0,0,255));
        mTextObjectShpere.setTextSize(TEXT_FONT_SIZE);
        mTextObjectShpere.setTextColor(Color.BLUE);
        mTextObjectShpere.setGravity(Gravity.CENTER);
        mTextObjectShpere.setBackgroundResource(R.drawable.jupiter);;
        mTextObjectShpere.setText("I love GL");
        mTextObjectShpere.setShape(objectSphere);

        mTextObjectBox = new TextObject(getGContext(), TEXT_WIDTH,
                TEXT_HEIGHT, 0f);
        mTextObjectBox.defaultColor(new Color4(255,0,0,255));
        mTextObjectBox.setTextSize(TEXT_FONT_SIZE);
        mTextObjectBox.setTextColor(Color.BLUE);
        mTextObjectBox.setBackgroundResource(R.drawable.jupiter);
        mTextObjectBox.setGravity(Gravity.CENTER);
        mTextObjectBox.setText("I love GL");
        mTextObjectBox.setShape(objectBox);
        mTextObjectBox.position().x = 1.4f;

        scene.addChild(mTextObject);
        scene.addChild(mTextObjectShpere);
        scene.addChild(mTextObjectBox);

    }

    public void updateScene()
    {
        if (mTextObjectShpere != null) {
            mTextObjectShpere.rotation().y++;
            mTextObjectBox.rotation().y++;
        }
    }
}