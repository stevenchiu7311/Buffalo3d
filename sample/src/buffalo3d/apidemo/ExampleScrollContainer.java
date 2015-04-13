package buffalo3d.apidemo;

import android.os.Bundle;
import android.view.MotionEvent;

import buffalo3d.component.ImageObject;
import buffalo3d.component.ScrollContainer;
import buffalo3d.component.ScrollContainer.ScrollContainerListener;
import buffalo3d.core.Object3d;
import buffalo3d.core.Object3dContainer;
import buffalo3d.core.RendererActivity;
import buffalo3d.core.RendererGLSurfaceView;
import buffalo3d.listeners.OnTouchListener;
import buffalo3d.objectPrimitives.Box;
import buffalo3d.objectPrimitives.Rectangle;
import buffalo3d.objectPrimitives.Sphere;
import buffalo3d.vos.Color4;
import buffalo3d.vos.Light;
import buffalo3d.vos.Number3d;


import java.util.List;

/**
 * Scroll container sample
 *
 * @author steven
 */
public class ExampleScrollContainer extends RendererActivity {
    final static float TEXT_WIDTH = 1.0f;
    final static float TEXT_HEIGHT = 1.0f;
    final static int TEXT_FONT_SIZE = 20;

    final static float RECT_WIDTH = 6f;
    final static float RECT_HEIGHT = 8f;

    final static int ITEM_NUM = 200;
    final static float ITEM_SIZE = 1f;

    // Change this configuration to be vertical or horizontal scroll view sample.
    final static boolean IS_VERTICAL_SCROLL_SAMPLE = true;

    Object3dContainer mTouchPanel;

    Object3dContainer mContainer;
    Object3dContainer mShpereContainer;
    Object3dContainer mBoxContainer;
    ImageObject mObjectShpere[];
    ImageObject mObjectBox[];

    ScrollContainer mScrollView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RendererGLSurfaceView.setGlesVersion(RendererGLSurfaceView.GLES20);
        super.onCreate(savedInstanceState);
        mScrollView = new ScrollContainer(getGContext(),(IS_VERTICAL_SCROLL_SAMPLE)?ScrollContainer.Mode.Y:ScrollContainer.Mode.X);
        mScrollView.setOverScrollRange(1 * ITEM_SIZE);
        mScrollView.setScrollRange(ITEM_NUM * ITEM_SIZE);
        mScrollView.setScrollContainerListener(new ScrollContainerListener() {
            @Override
            public void onItemVisibilityChanged(List<Object3d> visibilityChanged) {
                for (Object3d obj:visibilityChanged) {
                    if (obj.getVisibility() == Object3d.VISIBLE) {
                        obj.setBackgroundResource(R.drawable.deadmickey);
                    } else if (obj.getVisibility() == Object3d.GONE) {
                        obj.setBackgroundResource(0);
                    }
                }
            }

            @Override
            public void onScrollChanged(float scrollX, float scrollY) {
            }

            @Override
            public void onScrollFinished() {
            }
        });
    }

    public void initScene() {
        mTouchPanel = new Rectangle(getGContext(), RECT_WIDTH, RECT_HEIGHT, 1,
                1);
        mTouchPanel.lightingEnabled(false);
        mTouchPanel.doubleSidedEnabled(true);
        mTouchPanel.defaultColor(new Color4(100, 100, 100, 255));
        mTouchPanel.vertexColorsEnabled(true);
        mTouchPanel.setOnTouchListener(mTouchListener);
        mTouchPanel.position().z = -2;

        scene.lights().add(new Light());

        Object3d objectSphere = new Sphere(getGContext(), 0.4f, 15, 10, true,
                true, false);
        Object3d objectBox = new Box(getGContext(), 1, 1, 1, null, true, true,
                true);

        mContainer = new Object3dContainer(getGContext());
        mShpereContainer = new Object3dContainer(getGContext());
        mBoxContainer = new Object3dContainer(getGContext());
        mObjectShpere = new ImageObject[ITEM_NUM];
        mObjectBox = new ImageObject[ITEM_NUM];
        mShpereContainer.position().x = (IS_VERTICAL_SCROLL_SAMPLE)?-1f:0f;
        mShpereContainer.position().y = (IS_VERTICAL_SCROLL_SAMPLE)?0f:1f;
        for (int i = 0; i < mObjectShpere.length; i++) {
            mObjectShpere[i] = new ImageObject(getGContext(), TEXT_WIDTH, TEXT_HEIGHT,
                    0f);
            mObjectShpere[i].name("Shpere_"+i);
            mObjectShpere[i].setShape(objectSphere);
            mObjectShpere[i].position().x = (IS_VERTICAL_SCROLL_SAMPLE)?0f:i;
            mObjectShpere[i].position().y = (IS_VERTICAL_SCROLL_SAMPLE)?-i:0f;

            mShpereContainer.addChild(mObjectShpere[i]);
        }

        mBoxContainer.position().x = (IS_VERTICAL_SCROLL_SAMPLE)?1f:0f;
        mBoxContainer.position().y = (IS_VERTICAL_SCROLL_SAMPLE)?0f:-1f;
        for (int i = 0; i < mObjectShpere.length; i++) {
            mObjectBox[i] = new ImageObject(getGContext(), TEXT_WIDTH, TEXT_HEIGHT,
                    0f);
            mObjectBox[i].name("Box_"+i);
            mObjectBox[i].setShape(objectBox);
            mObjectBox[i].position().x = (IS_VERTICAL_SCROLL_SAMPLE)?0f:i * ITEM_SIZE;
            mObjectBox[i].position().y = (IS_VERTICAL_SCROLL_SAMPLE)?-i * ITEM_SIZE:0f;

            mBoxContainer.addChild(mObjectBox[i]);
        }

        mContainer.addChild(mShpereContainer);
        mContainer.addChild(mBoxContainer);
        mScrollView.addChild(mContainer);
        scene.addChild(mTouchPanel);
        scene.addChild(mScrollView);
    }

    private OnTouchListener mTouchListener = new OnTouchListener() {
        public boolean onTouch(Object3d obj, MotionEvent event,
                List<Object3d> list, Number3d coordinate) {
            mScrollView.addMotionEvent(event);
            return true;
        }
    };
}
