package cm.buffalo3d.engine.materials;

import cm.buffalo3d.engine.core.GContext;

public class SimpleMaterial extends AMaterial {
	public SimpleMaterial(GContext context) {
	    super(context, "shader/SimpleMaterial_vs.txt", "shader/SimpleMaterial_fs.txt");
	}
}
