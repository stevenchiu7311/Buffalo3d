package cm.buffalo3d.apidemo;

import cm.buffalo3d.engine.core.Object3dContainer;
import cm.buffalo3d.engine.core.RendererActivity;
import cm.buffalo3d.engine.parser.IParser;
import cm.buffalo3d.engine.parser.Parser;
import cm.buffalo3d.engine.vos.Light;

public class ExampleLoad3DSFile extends RendererActivity {
	private final float CAM_RADIUS_X = 20;
	private final float CAM_RADIUS_Y = 15;
	private final float CAM_RADIUS_Z = 30;
	private final float ROTATION_SPEED = 1;
	private Object3dContainer monster;
	private float degrees;

	@Override
	public void initScene() {
		
		scene.lights().add(new Light());

		IParser parser = Parser.createParser(getGContext(), Parser.Type.MAX_3DS,
				getResources(), getPackageName() + ":raw/monster_high", false);
		parser.parse();

		monster = parser.getParsedObject();
		monster.scale().x = monster.scale().y = monster.scale().z  = .5f;
		monster.position().y = -10;
		scene.addChild(monster);
		
		scene.camera().target = monster.position();
	}

	@Override
	public void updateScene() {
		float radians = degrees * ((float)Math.PI / 180);
		
		scene.camera().position.x = (float)Math.cos(radians) * CAM_RADIUS_X;
		scene.camera().position.y = (float)Math.sin(radians) * CAM_RADIUS_Y;
		scene.camera().position.z = (float)Math.sin(radians) * CAM_RADIUS_Z;

		degrees += ROTATION_SPEED;
	}
}