package min3d.listeners;

import android.view.MotionEvent;

import min3d.core.Object3d;
import min3d.vos.Number3d;

import java.util.List;

public interface OnLongClickListener {

    /**
     * Interface definition for a callback to be invoked when a object has been clicked and held.
     *
     * @param obj The view that was clicked and held.
     *
     * @return true if the callback consumed the long click, false otherwise.
     */
    public boolean onLongClick(Object3d obj);

}
