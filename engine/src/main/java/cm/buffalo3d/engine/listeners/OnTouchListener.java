package cm.buffalo3d.engine.listeners;


import android.view.MotionEvent;

import cm.buffalo3d.engine.core.Object3d;
import cm.buffalo3d.engine.vos.Number3d;


import java.util.List;

public interface OnTouchListener {

    /**
     * Interface definition for a callback to be invoked when a touch event is
     * dispatched to this object. The callback will be invoked before the touch
     * event is given to the object.
     *
     * @param obj
     *            Return main touched 3d object.
     *            If touched objects are different child layers, it will prefer youngest one.
     *            Otherwise returned one will be nearest(depth/Z) object in 3d world.
     * @param event
     *            the 2d touch motion event fed from surface view
     * @param list
     *            list which collects all touching-intersected objects
     * @param coordinate
     *            Intersected coordinate of main touched object in 3d world.
     *            It's determined with center plane intersection point of main one.
     * @return true if the callback consumed the touch, false otherwise.
    */
    public boolean onTouch(Object3d obj, MotionEvent event, List<Object3d> list,
                           Number3d coordinate);

}
