package min3d.sampleProject1;

import android.graphics.Bitmap;

import min3d.Utils;
import min3d.core.Object3dContainer;
import min3d.core.RendererActivity;
import min3d.objectPrimitives.Box;
import min3d.vos.Color4;

/**
 * An example for transparency setting. Keep changing cube alpha value in update
 * scene callback.
 *
 * @author steven
 */
public class ExampleObjectTransparency extends RendererActivity {
    Object3dContainer mCube;

    final static float CUBE_WIDTH = 1f;
    final static float CUBE_HEIGHT = 1f;
    final static float CUBE_DEPTH = 1f;

    public void initScene() {
        mCube = new Box(getGContext(), CUBE_WIDTH, CUBE_HEIGHT, CUBE_DEPTH);
        mCube.normalsEnabled(false);
        mCube.rotation().y = 30;
        mCube.vertexColorsEnabled(false);

        Bitmap b = Utils.makeBitmapFromResourceId(getGContext().getContext(),
                R.drawable.wood);
        getGContext().getTexureManager().addTextureId(b, "wood", false);
        b.recycle();
        mCube.getTextures().addById("wood");

        scene.addChild(mCube);
    }

    int count = 0;

    @Override
    public void updateScene() {
        count = ++count % 360;
        int alpha = (int) (255 * Math.sin(count * Math.PI / 360));
        mCube.defaultColor(new Color4(255, 255, 255, alpha));
    }
}