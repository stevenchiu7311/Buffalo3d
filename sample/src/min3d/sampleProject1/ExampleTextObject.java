package min3d.sampleProject1;

import android.graphics.Color;
import android.view.Gravity;

import min3d.core.RendererActivity;
import min3d.objectPrimitives.TextObject;
import min3d.vos.Light;

/**
 * Most simple text object example. Text object function will be extended soon.
 *
 * @author steven
 */
public class ExampleTextObject extends RendererActivity {
    final static float TEXT_WIDTH = 1f;
    final static float TEXT_HEIGHT = 0.2f;
    final static int TEXT_FONT_SIZE = 20;

    TextObject mTextObject;

    public void initScene() {
        scene.lights().add(new Light());
        TextObject mTextObject = new TextObject(getGContext(), TEXT_WIDTH,
                TEXT_HEIGHT, 0f);
        mTextObject.doubleSidedEnabled(true);
        mTextObject.setGravity(Gravity.CENTER);
        mTextObject.setTextSize(TEXT_FONT_SIZE);
        mTextObject.setTextColor(Color.WHITE);
        mTextObject.setText("I love acer~ lalala");
        scene.addChild(mTextObject);
    }
}