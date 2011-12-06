package min3d.listeners;

import android.view.MotionEvent;

import min3d.core.Object3d;
import min3d.vos.Number3d;

import java.util.List;

public interface OnClickListener {

    /**
     * Get the clicked objects inside the {@link BeyondarGLSurfaceView}
     *
     * @param obj
     *            The 3d object selected
     * @param event
     *            The 2d motion event
     * @param list
     *            The list of intersected objects
     * @param coordinate
     *            The coordinate of 3d world
    */
    public void onClick(Object3d obj, MotionEvent event, List<Object3d> list, Number3d coordinate);

}
