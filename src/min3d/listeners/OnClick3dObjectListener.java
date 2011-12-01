package min3d.listeners;

import java.util.ArrayList;

import min3d.core.Object3d;


public interface OnClick3dObjectListener {

    /**
     * Get the clicked objects inside the {@link BeyondarGLSurfaceView}
     *
     * @param geoObjects
     *            The list with the geoObjects selected
     * @param x
     *            The X screen coordinates
     * @param y
     *            The Y screen coordinates
     */
    public void onClick(ArrayList<Object3d> object3d, float x, float y);

}
