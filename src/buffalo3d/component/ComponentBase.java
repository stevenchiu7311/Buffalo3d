package buffalo3d.component;

import buffalo3d.core.FacesBufferedList;
import buffalo3d.core.GContext;
import buffalo3d.core.Object3d;
import buffalo3d.core.Vertices;

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