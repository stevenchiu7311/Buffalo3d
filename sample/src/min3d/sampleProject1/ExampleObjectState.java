package min3d.sampleProject1;

import android.os.Bundle;
import android.widget.ImageView.ScaleType;

import min3d.component.ImageObject;
import min3d.core.Object3d;
import min3d.core.RendererActivity;
import min3d.objectPrimitives.Box;
import min3d.objectPrimitives.Sphere;
import min3d.vos.Color4;
import min3d.vos.Light;

/**
 * Object state switching sample. (Focus, Press, Enabled)
 *
 * @author steven
 */
public class ExampleObjectState extends RendererActivity {
    final static float TEXT_WIDTH = 1.0f;
    final static float TEXT_HEIGHT = 1.0f;
    final static int TEXT_FONT_SIZE = 20;

    ImageObject mObject;
    ImageObject mObjectShpere;
    ImageObject mObjectBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void initScene() {
        scene.backgroundColor().setAll(new Color4(255, 255, 255, 255));
        scene.lights().add(new Light());
        Object3d objectBox = new Box(getGContext(), 1, 1, 1, null, true, true,
                true);
        Object3d objectSphere = new Sphere(getGContext(), 0.4f, 15, 10, true,
                true, false);

        mObject = new ImageObject(getGContext(), TEXT_WIDTH, TEXT_HEIGHT, 0f);
        mObject.setScaleType(ScaleType.CENTER);
        mObject.setBackgroundResource(R.drawable.btn_default_holo_light);
        mObject.position().x = -1.0f;
        mObject.getTextures();
        mObject.setFocusable(true);
        mObject.requestFocus();

        mObjectShpere = new ImageObject(getGContext(), TEXT_WIDTH, TEXT_HEIGHT,
                0f);
        mObjectShpere.defaultColor(new Color4(255, 0, 0, 255));
        mObjectShpere.setBackgroundResource(R.drawable.btn_default_holo_light);
        mObjectShpere.setShape(objectSphere);
        mObjectShpere.rotation().y = -90;
        mObjectShpere.position().x = 0.0f;
        mObjectShpere.setClickable(true);

        mObjectBox = new ImageObject(getGContext(), TEXT_WIDTH, TEXT_HEIGHT, 0f);
        mObjectBox.defaultColor(new Color4(255, 0, 0, 255));
        mObjectBox.setBackgroundResource(R.drawable.btn_default_holo_light);
        mObjectBox.setShape(objectBox);
        mObjectBox.position().x = 1.3f;
        mObjectBox.setEnabled(false);

        scene.addChild(mObject);
        scene.addChild(mObjectShpere);
        scene.addChild(mObjectBox);
    }

    @Override
    public void updateScene() {
    }
}