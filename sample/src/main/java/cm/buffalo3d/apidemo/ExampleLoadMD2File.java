package cm.buffalo3d.apidemo;

import cm.buffalo3d.engine.animation.AnimationObject3d;
import cm.buffalo3d.engine.core.RendererActivity;
import cm.buffalo3d.engine.parser.IParser;
import cm.buffalo3d.engine.parser.Parser;
import cm.buffalo3d.engine.vos.Light;

public class ExampleLoadMD2File extends RendererActivity {
	private AnimationObject3d ogre;

	@Override
	public void initScene() {
		
		scene.lights().add(new Light());
		
		IParser parser = Parser.createParser(getGContext(), Parser.Type.MD2,
				getResources(), getPackageName() + ":raw/ogro", false);
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
