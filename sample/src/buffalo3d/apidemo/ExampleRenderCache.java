package buffalo3d.apidemo;

import android.os.Bundle;

import buffalo3d.component.ImageObject;
import buffalo3d.core.Object3d;
import buffalo3d.core.Object3dContainer;
import buffalo3d.core.RendererActivity;
import buffalo3d.core.RendererGLSurfaceView;
import buffalo3d.objectPrimitives.Box;
import buffalo3d.objectPrimitives.Sphere;
import buffalo3d.vos.Light;
import buffalo3d.vos.Number3d;



/**
 * Optimization example for magnificent number objects
 *
 * @author steven
 */
public class ExampleRenderCache extends RendererActivity {
    final static int ITEM_NUM = 500;
    final static float ITEM_SIZE = 0.1f;
    final static int SHPERE_SEGMENTS = 10;

    Object3dContainer mContainer;
    Object3dContainer mShpereContainer;
    Object3dContainer mBoxContainer;
    ImageObject mObjectShpere[];
    ImageObject mObjectBox[];
    Number3d mPlane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RendererGLSurfaceView.setGlesVersion(RendererGLSurfaceView.GLES20);
        super.onCreate(savedInstanceState);
    }

    public void updateScene() {
        if (mPlane == null) {
            mPlane = getGContext().getRenderer().getWorldPlaneSize(0);
        } else {
            if (mContainer != null) {
                mContainer.rotation().y += 0.1f;
            }
            return;
        }

        scene.lights().add(new Light());

        Object3d objectSphere = new Sphere(getGContext(), ITEM_SIZE / 2, SHPERE_SEGMENTS, SHPERE_SEGMENTS, true,
                true, false);
        Object3d objectBox = new Box(getGContext(), ITEM_SIZE, ITEM_SIZE, ITEM_SIZE, null, true, true,
                true);
        mContainer = new Object3dContainer(getGContext());
        mShpereContainer = new Object3dContainer(getGContext(), (int) (ITEM_NUM * Math.pow(SHPERE_SEGMENTS + 1, 2)), (int) (ITEM_NUM * Math.pow(SHPERE_SEGMENTS, 2) * 2));
        mShpereContainer.setBackgroundResource(R.drawable.icon);
        mShpereContainer.enableRenderCacheBuilder(true);
        mShpereContainer.invalidate();
        mBoxContainer = new Object3dContainer(getGContext(), ITEM_NUM * 24, ITEM_NUM * 12);
        mBoxContainer.setBackgroundResource(R.drawable.icon);
        mBoxContainer.enableRenderCacheBuilder(true);
        mBoxContainer.invalidate();
        mObjectShpere = new ImageObject[ITEM_NUM];
        mObjectBox = new ImageObject[ITEM_NUM];

        for (int i = 0; i < mObjectShpere.length; i++) {
            float x = (float) (-mPlane.x / 2 + (Math.random() * mPlane.x));
            float y = (float) (-mPlane.y / 2 + (Math.random() * mPlane.y));
            float z = (float) (-3f + (Math.random() * 6f));

            mObjectShpere[i] = new ImageObject(getGContext(), ITEM_SIZE, ITEM_SIZE,
                    0f);
            mObjectShpere[i].name("Shpere_" + i);
            mObjectShpere[i].setShape(objectSphere);
            mObjectShpere[i].position().setAll(x, y, z);
            mObjectShpere[i].setRenderCacheEnabled(true);

            mShpereContainer.addChild(mObjectShpere[i]);
        }

        for (int i = 0; i < mObjectBox.length; i++) {
            float x = (float) (-mPlane.x / 2 + (Math.random() * mPlane.x));
            float y = (float) (-mPlane.y / 2 + (Math.random() * mPlane.y));
            float z = (float) (-1f + (Math.random() * 2f));
            mObjectBox[i] = new ImageObject(getGContext(), ITEM_SIZE, ITEM_SIZE,
                    0f);
            mObjectBox[i].name("Box_" + i);
            mObjectBox[i].setShape(objectBox);
            mObjectBox[i].position().setAll(x, y, z);
            mObjectBox[i].setRenderCacheEnabled(true);

            mBoxContainer.addChild(mObjectBox[i]);
        }

        mContainer.addChild(mShpereContainer);
        mContainer.addChild(mBoxContainer);
        scene.addChild(mContainer);

        for (int i = 0; i < mObjectShpere.length; i++) {
            mObjectShpere[i].setVisibility(Object3d.GONE);
        }
        for (int i = 0; i < mObjectBox.length; i++) {
            mObjectBox[i].setVisibility(Object3d.GONE);
        }
    }
}
