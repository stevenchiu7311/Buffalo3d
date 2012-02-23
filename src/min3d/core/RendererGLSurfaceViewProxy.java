package min3d.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;

import min3d.vos.Ray;

public class RendererGLSurfaceViewProxy extends GLSurfaceView {
    static final public int CURRENT_SDK_INT = Build.VERSION.SDK_INT;

    protected min3d.core.Renderer mRenderer;
    protected MultisampleConfigChooser mConfigChooser = null;
    protected SnapshotCallback mSnapshotCallback = null;

    public RendererGLSurfaceViewProxy(Context context) {
        super(context);
        int version = CURRENT_SDK_INT;
        if (version >= Build.VERSION_CODES.FROYO) {
            setEGLContextClientVersion(2);
            // Create an OpenGL ES 2.0 context.
            setEGLConfigChooser(mConfigChooser = new MultisampleConfigChooser());
        }
    }

    public RendererGLSurfaceViewProxy(Context context, AttributeSet attrs) {
        super(context, attrs);
        int version = CURRENT_SDK_INT;
        if (version >= Build.VERSION_CODES.FROYO) {
            setEGLContextClientVersion(2);
            // Create an OpenGL ES 2.0 context.
            setEGLConfigChooser(mConfigChooser = new MultisampleConfigChooser());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        dispatchTouchEventToChild(e);
        return true;
    }

    private void dispatchTouchEventToChild(MotionEvent e) {
        Ray ray = mRenderer.getViewRay(e.getX(), e.getY());
        Scene scene = mRenderer.getScene();
        mRenderer.updateAABBCoord();
        Object3dContainer root = (Object3dContainer) scene.root();
        ArrayList<Object3d> list = (ArrayList<Object3d>)mRenderer.getPickedObject(ray, root);
        root.dispatchTouchEvent(ray ,e, list);
    }

    public void setRender(min3d.core.Renderer renderer) {
        mRenderer = renderer;
    }

    /**
     * Register a SnapshotCall back. Return the callback when snapshot is ready.
     *
     * @param callback a callback used to get snapshot.
     */
    public void setSnapshotCallback(SnapshotCallback callback) {
        mSnapshotCallback = callback;
    }

    /**
     * Request GLSurfaceView to create snapShot.
     *
     * @param width the width of snapshot.
     * @param height the height of snapshot.
     * @param object an extra piece of information. Return the information when
     *            snapshot is ready.
     */
    public void requestSceneSnapshot(final int width, final int height, final Object object) {
        queueEvent(new Runnable() {
            public void run() {
                if (mSnapshotCallback != null) {
                    mSnapshotCallback.onGetSnapShot(mRenderer.savePixels(0, 0, width, height), object);
                }
            }
        });
    }

    /**
     * A callback interface used to return glSurfaceView snapshot.
     */
    public interface SnapshotCallback {
        void onGetSnapShot(Bitmap bitmap, Object object);
    }
}
