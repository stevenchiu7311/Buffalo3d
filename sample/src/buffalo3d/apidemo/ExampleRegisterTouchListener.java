package buffalo3d.apidemo;

import android.view.MotionEvent;

import buffalo3d.core.Object3d;
import buffalo3d.core.Object3dContainer;
import buffalo3d.core.RendererActivity;
import buffalo3d.listeners.OnTouchListener;
import buffalo3d.objectPrimitives.Box;
import buffalo3d.objectPrimitives.Rectangle;
import buffalo3d.vos.Color4;
import buffalo3d.vos.Number3d;


import java.util.List;

/**
 * A simple touch listener registration demo. Touch panel object to receive
 * touch event in scene and reference the coordinate parameter for cube flickers
 * moves. And cube also listen touch event for dragging motion.
 *
 * @author steven
 */
public class ExampleRegisterTouchListener extends RendererActivity {
    Object3dContainer mCube;
    Object3dContainer mTouchPanel;

    final static float RECT_WIDTH = 3f;
    final static float RECT_HEIGHT = 2f;
    final static float CUBE_WIDTH = 0.5f;
    final static float CUBE_HEIGHT = 0.5f;
    final static float CUBE_DEPTH = 0.5f;

    public void initScene() {
        mTouchPanel = new Rectangle(getGContext(), RECT_WIDTH, RECT_HEIGHT, 1,
                1);
        mTouchPanel.lightingEnabled(false);
        mTouchPanel.doubleSidedEnabled(true);
        mTouchPanel.defaultColor(new Color4(100, 100, 100, 255));
        mTouchPanel.vertexColorsEnabled(true);
        mTouchPanel.setOnTouchListener(mTouchListener);
        mTouchPanel.position().z = -CUBE_DEPTH / 2;
        mCube = new Box(getGContext(), CUBE_WIDTH, CUBE_HEIGHT, CUBE_DEPTH);
        mCube.setOnTouchListener(mTouchListener);
        mCube.normalsEnabled(false);
        scene.addChild(mTouchPanel);
        scene.addChild(mCube);
    }

    @Override
    public void updateScene() {

    }

    private OnTouchListener mTouchListener = new OnTouchListener() {
        public boolean onTouch(Object3d obj, MotionEvent event,
                List<Object3d> list, Number3d coordinate) {
            mCube.position().x = coordinate.x;
            mCube.position().y = coordinate.y;
            return true;
        }
    };
}