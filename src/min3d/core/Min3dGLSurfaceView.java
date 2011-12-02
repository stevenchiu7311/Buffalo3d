package min3d.core;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import min3d.listeners.OnActionDownListener;
import min3d.listeners.OnActionMoveListener;
import min3d.listeners.OnActionUpListener;
import min3d.listeners.OnClick3dObjectListener;
import min3d.vos.Ray;

import java.util.ArrayList;

public class Min3dGLSurfaceView extends GLSurfaceView {

    private static final int PREPRESSED = 0x02000000;
    private static final int PREMOVE = 0x04000000;
    private Context mContext;
    private min3d.core.Renderer mRenderer;

    // Listeners stuff
    private OnClick3dObjectListener mOnClickObjectListener = null;
    private OnActionDownListener mOnActionDownListener = null;
    private OnActionMoveListener mOnActionMoveListener = null;
    private OnActionUpListener mOnActionUpListener = null;
    private int mPrivateFlags;

    // End listeners stuff

    public Min3dGLSurfaceView(Context context) {
        super(context);
        mContext = context;

    }

    public Min3dGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public void onPause() {
        // Log.d(StaticValues.TAG, "Pause");
        super.onPause();
    }

    @Override
    public void onResume() {
        // Log.d(StaticValues.TAG, "resume");
        super.onResume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        dispatchTouchEventToChild(e);

        switch (e.getAction()) {
        case MotionEvent.ACTION_UP:
            boolean prepressed = (mPrivateFlags & PREPRESSED) != 0;
            if (prepressed) {
                processScreenActionClick(e.getX(), e.getY());
            }
            processScreenActionUp(x, y);
            break;
        case MotionEvent.ACTION_DOWN:
            mPrivateFlags |= PREPRESSED;
            processScreenAtionDown(x, y);
            break;

        case MotionEvent.ACTION_MOVE:
            mPrivateFlags &= ~PREPRESSED;
            processScreenActionMove(x, y);
            break;
        case MotionEvent.ACTION_CANCEL:
            mPrivateFlags &= ~PREPRESSED;
            break;

        }

        return true;

    }

    private void dispatchTouchEventToChild(MotionEvent e) {
        Ray ray = mRenderer.getViewRay(e.getX(), e.getY());
        for (int i = 0; i < mRenderer.getScene().children().size(); i++) {
            Object3d o = mRenderer.getScene().children().get(i);
            rursiveDispatchTouchEvent(o, ray ,e);
        }
    }

    private void rursiveDispatchTouchEvent(Object3d node, Ray ray, MotionEvent e) {
        node.processTouchEvent(ray,e);
        if (node instanceof Object3dContainer) {
            Object3dContainer container = (Object3dContainer) node;
            for (int i = 0; i < container.children().size(); i++) {
                Object3d o = container.children().get(i);
                rursiveDispatchTouchEvent(o, ray, e);
            }
        }
    }

    public void setOnActionUpListener(OnActionMoveListener listener) {
        mOnActionMoveListener = listener;
    }

    public OnActionMoveListener getOnActionUpListener() {
        return mOnActionMoveListener;
    }

    public void setOnActionDownListener(OnActionDownListener listener) {
        mOnActionDownListener = listener;
    }

    public OnActionDownListener getOnActionDownListener() {
        return mOnActionDownListener;
    }

    public void setOnActionMoveListener(OnActionMoveListener listener) {
        mOnActionMoveListener = listener;
    }

    public OnActionMoveListener getOnActionMoveListener() {
        return mOnActionMoveListener;
    }

    public void setOnClicObject3dListener(OnClick3dObjectListener listener) {
        mOnClickObjectListener = listener;
    }

    public OnClick3dObjectListener getOnClickObject3dListener() {
        return mOnClickObjectListener;
    }

    public void processScreenAtionDown(float x, float y) {
        // If there are no listeners defined don't calculate anything.
        if (mOnActionDownListener == null) {
            return;
        }
        Ray ray = mRenderer.getViewRay(x, y);
        ArrayList<Object3d> object3DList = mRenderer.getPickedObject(ray);
        if (object3DList.size() == 0) {
            return;
        }
        mOnActionDownListener.onActionDown(object3DList, x, y);
    }

    public void processScreenActionMove(float x, float y) {
        if (mOnActionMoveListener == null) {
            return;
        }
        Ray ray = mRenderer.getViewRay(x, y);
        ArrayList<Object3d> object3DList = mRenderer.getPickedObject(ray);
        if (object3DList.size() == 0) {
            return;
        }
        mOnActionMoveListener.onActionMove(object3DList, x, y);
    }

    public void processScreenActionUp(float x, float y) {
        if (mOnActionUpListener == null) {
            return;
        }
        Ray ray = mRenderer.getViewRay(x, y);
        ArrayList<Object3d> object3DList = mRenderer.getPickedObject(ray);
        if (object3DList.size() == 0) {
            return;
        }
        mOnActionUpListener.OnActionUp(object3DList, x, y);
    }

    public void processScreenActionClick(float x, float y) {

        // If there are no listeners defined don't calculate anything.
        if (mOnClickObjectListener == null) {
            return;
        }

        Ray ray = mRenderer.getViewRay(x, y);

        ArrayList<Object3d> object3DList = mRenderer.getPickedObject(ray);
        if (object3DList.size() == 0) {
            return;
        }

        if (mOnClickObjectListener != null) {
            mOnClickObjectListener.onClick(object3DList, x, y);
        }

    }

    public void setRender(min3d.core.Renderer renderer) {
        mRenderer = renderer;
    }
}
