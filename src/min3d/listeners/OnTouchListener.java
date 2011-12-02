package min3d.listeners;


import android.view.MotionEvent;
import min3d.core.Object3d;
import min3d.vos.Number3d;

public interface OnTouchListener {

    /**
     * Get the clicked objects inside the {@link BeyondarGLSurfaceView}
     *
     * @param Object3d
     *            The the Object3d selected
     * @param x
     *            The X screen coordinates
     * @param y
     *            The Y screen coordinates
     * @param coordinates
     *            The coordinates of 3d world
    */
    public void onTouch(Object3d v, MotionEvent event, Number3d coordinates);

}
