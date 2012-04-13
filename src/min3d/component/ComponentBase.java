package min3d.component;

import min3d.core.FacesBufferedList;
import min3d.core.GContext;
import min3d.core.Object3d;
import min3d.core.Vertices;

public abstract class ComponentBase extends Object3d {

    public ComponentBase(GContext context, int $maxVertices, int $maxFaces) {
        super(context, $maxVertices, $maxFaces);
        // TODO Auto-generated constructor stub
    }

    public void setShape(Vertices vertices, FacesBufferedList faces) {
        _vertices = vertices;
        _faces = faces;
    }

    public void setShape(Object3d object3d) {
        _vertices = object3d.vertices();
        _faces = object3d.faces();
    }
}