package min3d.sampleProject1;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import min3d.Utils;
import min3d.core.Object3d;
import min3d.core.Object3dContainer;
import min3d.core.RendererActivity;
import min3d.listeners.OnClickListener;
import min3d.listeners.OnLongClickListener;
import min3d.objectPrimitives.Sphere;
import min3d.vos.Light;
import min3d.vos.Number3d;

import java.util.List;

/**
 * Bases on rotation planet example and demo how to register click listener
 * callback for multiple objects.
 *
 * @author steven
 */
public class ExampleRegisterClickListener extends RendererActivity {
    Object3dContainer _jupiter;
    Object3dContainer _earth;
    Object3dContainer _moon;
    int _count;

    public void initScene() {
        Light light = new Light();

        light.ambient.setAll((short) 64, (short) 64, (short) 64, (short) 255);
        light.position.setAll(3, 3, 3);
        scene.lights().add(light);

        // Add Jupiter to scene
        _jupiter = new Sphere(getGContext(), 0.8f, 15, 10, true, true, false);
        _jupiter.setOnClickListener(mClickListener);
        _jupiter.setOnLongClickListener(mLongClickListener);
        _jupiter.name("jupiter");
        scene.addChild(_jupiter);

        // Add Earth as a child of Jupiter
        _earth = new Sphere(getGContext(), 0.4f, 12, 9, true, true, false);
        _earth.setOnClickListener(mClickListener);
        _earth.setOnLongClickListener(mLongClickListener);
        _earth.name("earth");
        _earth.position().x = 1.6f;
        _earth.rotation().x = 23;
        _jupiter.addChild(_earth);

        // Add the Moon as a child of Earth
        _moon = new Sphere(getGContext(), 0.2f, 10, 8, true, true, false);
        _moon.setOnClickListener(mClickListener);
        _moon.setOnLongClickListener(mLongClickListener);
        _moon.name("moon");
        _moon.position().x = 0.6f;
        _earth.addChild(_moon);

        // Add textures to TextureManager
        Bitmap b = Utils.makeBitmapFromResourceId(this, R.drawable.jupiter);
        getGContext().getTexureManager().addTextureId(b, "jupiter", false);
        b.recycle();

        b = Utils.makeBitmapFromResourceId(this, R.drawable.earth);
        getGContext().getTexureManager().addTextureId(b, "earth", false);
        b.recycle();

        b = Utils.makeBitmapFromResourceId(this, R.drawable.moon);
        getGContext().getTexureManager().addTextureId(b, "moon", false);
        b.recycle();

        // Add textures to objects based on on the id's we assigned the textures in the texture manager
        _jupiter.getTextures().addById("jupiter");
        _earth.getTextures().addById("earth");
        _moon.getTextures().addById("moon");

        _count = 0;
    }

    @Override
    public void updateScene() {
        // Spin spheres
        _jupiter.rotation().y += 1.0f / 2;
        _earth.rotation().y += 3.0f / 2;
        _moon.rotation().y -= 12.0f / 2;

        // Wobble Jupiter a little just for fun
        _count++;
        float mag = (float) (Math.sin(_count * 0.2 * Utils.DEG)) * 15;
        _jupiter.rotation().z = (float) Math.sin(_count * .33 * Utils.DEG)
                * mag;

        // Move camera around
        scene.camera().position.z = 4.5f + (float) Math
                .sin(_jupiter.rotation().y * Utils.DEG);
        scene.camera().target.x = (float) Math.sin((_jupiter.rotation().y + 90)
                * Utils.DEG) * 0.8f;
    }

    private OnClickListener mClickListener = new OnClickListener() {
        public void onClick(Object3d v, List<Object3d> list,
                Number3d coordinates) {
            Log.i("ExampleTouchHandler", "OnClickListener:" + v.name());
            Toast.makeText(getBaseContext(), "Click: " + v.name(), 1000).show();
        }
    };

    private OnLongClickListener mLongClickListener = new OnLongClickListener() {
        public boolean onLongClick(Object3d v, List<Object3d> list,
                Number3d coordinates) {
            Log.i("ExampleTouchHandler", "OnLongClickListener:" + v.name());
            Toast.makeText(getBaseContext(), "Long Click: " + v.name(), 1000)
                    .show();
            return true;
        }
    };
}
