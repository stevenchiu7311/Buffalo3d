package min3d.sampleProject1;

import android.os.Bundle;
import android.view.MotionEvent;

import min3d.CustomScroller;
import min3d.component.ImageObject;
import min3d.component.ScrollContainer;
import min3d.component.ScrollContainer.ScrollContainerListener;
import min3d.core.Object3d;
import min3d.core.Object3dContainer;
import min3d.core.RendererActivity;
import min3d.core.RendererGLSurfaceView;
import min3d.listeners.OnTouchListener;
import min3d.objectPrimitives.Box;
import min3d.objectPrimitives.Rectangle;
import min3d.objectPrimitives.Sphere;
import min3d.vos.Color4;
import min3d.vos.Light;
import min3d.vos.Number3d;

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
        mScrollView = new ScrollContainer(getGContext(),(IS_VERTICAL_SCROLL_SAMPLE)?CustomScroller.Mode.Y:CustomScroller.Mode.X);
        mScrollView.setOverScrollRange(250);
        mScrollView.setScrollRange(100000);
        mScrollView.setScrollViewListener(new ScrollContainerListener() {
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
            public void onScrollChanged(int scrollX, int scrollY) {
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
        mObjectShpere = new ImageObject[200];
        mObjectBox = new ImageObject[200];
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
            mObjectBox[i].position().x = (IS_VERTICAL_SCROLL_SAMPLE)?0f:i;
            mObjectBox[i].position().y = (IS_VERTICAL_SCROLL_SAMPLE)?-i:0f;

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
