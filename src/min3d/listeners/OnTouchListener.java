package min3d.listeners;


import android.view.MotionEvent;

import min3d.core.Object3d;
import min3d.vos.Number3d;

import java.util.List;

public interface OnTouchListener {

    /**
     * Interface definition for a callback to be invoked when a touch event is
     * dispatched to this object. The callback will be invoked before the touch
     * event is given to the object.
     *
     * @param obj
     *            The 3d object selected
     * @param event
     *            The 2d motion event
     * @param list
     *            The list of intersected objects
     * @param coordinate
     *            The coordinate of 3d world
     * @return true if the callback consumed the touch, false otherwise.
    */
    public boolean onTouch(Object3d obj, MotionEvent event, List<Object3d> list, Number3d coordinate);

}
