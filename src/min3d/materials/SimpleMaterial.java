package min3d.materials;

import min3d.core.GContext;

public class SimpleMaterial extends AMaterial {
	public SimpleMaterial(GContext context) {
	    super(context, "shader/SimpleMaterial_vs.txt", "shader/SimpleMaterial_fs.txt");
	}
}
