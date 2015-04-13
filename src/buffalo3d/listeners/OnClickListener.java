package buffalo3d.listeners;

import android.view.MotionEvent;

import buffalo3d.core.Object3d;
import buffalo3d.vos.Number3d;


import java.util.List;

public interface OnClickListener {

    /**
     * Interface definition for a callback to be invoked when a object is clicked.
     *
     * @param obj
     *            Return main touched 3d object.
     *            If touched objects are different child layers, it will prefer youngest one.
     *            Otherwise returned one will be nearest(depth/Z) object in 3d world.
     * @param list
     *            list which collects all touching-intersected objects
     * @param coordinate
     *            Intersected coordinate of main touched object in 3d world.
     *            It's determined with center plane intersection point of main one.
    */
    public void onClick(Object3d obj, List<Object3d> list, Number3d coordinate);

}
