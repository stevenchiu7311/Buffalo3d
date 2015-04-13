package buffalo3d.materials;

import buffalo3d.core.GContext;

public class SimpleMaterial extends AMaterial {
	public SimpleMaterial(GContext context) {
	    super(context, "shader/SimpleMaterial_vs.txt", "shader/SimpleMaterial_fs.txt");
	}
}
