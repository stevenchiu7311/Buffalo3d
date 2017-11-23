package cm.buffalo3d.engine.component;

import cm.buffalo3d.engine.core.FacesBufferedList;
import cm.buffalo3d.engine.core.GContext;
import cm.buffalo3d.engine.core.Object3d;
import cm.buffalo3d.engine.core.Vertices;

public abstract class ComponentBase extends Object3d {

    public ComponentBase(GContext context, int $maxVertices, int $maxFaces) {
        super(context, $maxVertices, $maxFaces);
        // TODO Auto-generated constructor stub
    }

    public void setShape(Vertices vertices, FacesBufferedList faces) {
        mVertices = vertices;
        mFaces = faces;
    }

    public void setShape(Object3d object3d) {
        mVertices = object3d.getVertices();
        mFaces = object3d.getFaces();
    }
}