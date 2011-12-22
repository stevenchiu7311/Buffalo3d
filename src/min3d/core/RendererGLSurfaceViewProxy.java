package min3d.core;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;

import min3d.Shared;
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
        Object3dContainer root = (Object3dContainer) mRenderer.getScene().root();
        ArrayList<Object3d> list = (ArrayList<Object3d>)Shared.renderer().getPickedObject(ray, root);
        root.dispatchTouchEvent(ray ,e, list);
    }

    public void setRender(min3d.core.Renderer renderer) {
        mRenderer = renderer;
    }
}
