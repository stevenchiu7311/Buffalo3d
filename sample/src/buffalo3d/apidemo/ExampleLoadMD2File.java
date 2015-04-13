package buffalo3d.apidemo;

import buffalo3d.animation.AnimationObject3d;
import buffalo3d.core.RendererActivity;
import buffalo3d.parser.IParser;
import buffalo3d.parser.Parser;
import buffalo3d.vos.Light;

public class ExampleLoadMD2File extends RendererActivity {
	private AnimationObject3d ogre;

	@Override
	public void initScene() {
		
		scene.lights().add(new Light());
		
		IParser parser = Parser.createParser(getGContext(), Parser.Type.MD2,
				getResources(), "buffalo3d.apidemo:raw/ogro", false);
		parser.parse();

		ogre = parser.getParsedAnimationObject();
		ogre.scale().x = ogre.scale().y = ogre.scale().z = .07f;
		ogre.rotation().z = -90;
		ogre.rotation().x = -90;
		scene.addChild(ogre);
		ogre.setFps(70);
		ogre.play();
	}

	@Override
	public void updateScene() {

	}

}
