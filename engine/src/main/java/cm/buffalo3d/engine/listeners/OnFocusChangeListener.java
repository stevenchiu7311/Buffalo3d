package cm.buffalo3d.engine.listeners;

import cm.buffalo3d.engine.core.Object3d;

/**
 * Interface definition for a callback to be invoked when the focus state of
 * a object changed.
 */
public interface OnFocusChangeListener {
    /**
     * Called when the focus state of a view has changed.
     *
     * @param obj The view whose state has changed.
     * @param hasFocus The new focus state of object.
     */
    void onFocusChange(Object3d obj, boolean hasFocus);
}
