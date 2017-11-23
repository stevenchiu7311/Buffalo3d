package cm.buffalo3d.apidemo;

import cm.buffalo3d.engine.core.Object3dContainer;
import cm.buffalo3d.engine.core.RendererActivity;
import cm.buffalo3d.engine.parser.IParser;
import cm.buffalo3d.engine.parser.Parser;
import cm.buffalo3d.engine.vos.Light;

/**
 * How to load a model from a .obj file
 * 
 * @author dennis.ippel
 * 
 */
public class ExampleLoadObjFile extends RendererActivity {
	private Object3dContainer objModel;

	@Override
	public void initScene() {
		
		scene.lights().add(new Light());
		
		IParser parser = Parser.createParser(getGContext(), Parser.Type.OBJ,
				getResources(), getPackageName() + ":raw/camaro_obj", true);
		parser.parse();

		objModel = parser.getParsedObject();
		objModel.scale().x = objModel.scale().y = objModel.scale().z = .7f;
		scene.addChild(objModel);
	}

	@Override
	public void updateScene() {
		objModel.rotation().x++;
		objModel.rotation().z++;
	}
}
