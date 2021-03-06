package cm.buffalo3d.engine.listeners;

import cm.buffalo3d.engine.core.Object3d;
import cm.buffalo3d.engine.vos.Number3d;


import java.util.List;

public interface OnLongClickListener {

    /**
     * Interface definition for a callback to be invoked when a object has been clicked and held.
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
     *
     * @return true if the callback consumed the long click, false otherwise.
     */
    public boolean onLongClick(Object3d obj, List<Object3d> list, Number3d coordinate);

}
