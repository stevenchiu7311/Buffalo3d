package min3d.listeners;

import java.util.ArrayList;

import min3d.core.Object3d;



public interface OnActionMoveListener {

    /**
     * Get the clicked objects inside the {@link BeyondarGLSurfaceView}
     *
     * @param onActionDownGeoObjects
     *            The list with the geoObjects selected when the {@link OnActionDownListener} was processed.
     * @param x
     *            The X screen coordinates
     * @param y
     *            The Y screen coordinates
     */
    public void onActionMove(ArrayList<Object3d> onActionDownGeoObjects,
            float x, float y);

    /**
     * This function is executed when the actionMove is released.
     *
     * @param onActionDownGeoObjects
     *            The list with the geoObjects selected when the {@link OnActionDownListener} was processed.
     * @param x
     *            The X screen coordinates
     * @param y
     *            The Y screen coordinates
     */
    public void onStopMoving(ArrayList<Object3d> onActionDownGeoObjects,
            float x, float y);
}
