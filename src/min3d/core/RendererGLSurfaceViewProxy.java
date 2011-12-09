package min3d.core;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import min3d.vos.Ray;

public class RendererGLSurfaceViewProxy extends GLSurfaceView {

    protected min3d.core.Renderer mRenderer;

    public RendererGLSurfaceViewProxy(Context context) {
        super(context);
    }

    public RendererGLSurfaceViewProxy(Context context, AttributeSet attrs) {
        super(context, attrs);
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
        mRenderer.updateAABBCoord();
        for (int i = 0; i < mRenderer.getScene().children().size(); i++) {
            Object3d child = mRenderer.getScene().children().get(i);
            rursiveDispatchTouchEvent(child, ray ,e);
        }
    }

    private void rursiveDispatchTouchEvent(Object3d node, Ray ray, MotionEvent e) {
        node.processTouchEvent(ray,e);
        if (node instanceof Object3dContainer) {
            Object3dContainer container = (Object3dContainer) node;
            for (int i = 0; i < container.children().size(); i++) {
                Object3d child = container.children().get(i);
                rursiveDispatchTouchEvent(child, ray, e);
            }
        }
    }

    public void setRender(min3d.core.Renderer renderer) {
        mRenderer = renderer;
    }
}
