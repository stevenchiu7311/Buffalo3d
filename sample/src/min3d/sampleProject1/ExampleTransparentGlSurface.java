package min3d.sampleProject1;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import min3d.core.RendererGLSurfaceView;
import min3d.core.Scene;
import min3d.interfaces.ISceneController;
import min3d.objectPrimitives.Box;
import min3d.vos.Light;

/**
 * Example of implementing a transparent GLSurfaceView.
 * 
 *  (a) Override glSurfaceViewConfig() to configure glSurfaceView for transparency as shown
 *  (b) Set the scene's backgroundColor to 0x00000000 (black with 0-alpha)
 * 
 * If you were to place any 2D elements (images, etc) in the Activity "behind" glSurface,
 * they would be now visible.
 * 
 * In this example, instead of doing that, the Activity itself is set to transparent,  
 * allowing the underlying Activity to show through... (See the manifest xml -- 
 * "android:theme="@android:style/Theme.Translucent.NoTitleBar")
 * 
 * @author Lee
 */
public class ExampleTransparentGlSurface extends Activity implements
        ISceneController {
    private final int NUM = 25;

    private Box[] boxes;
    RendererGLSurfaceView mGlSurfaceView;

    @Override
    public void initScene() {
        Scene scene = mGlSurfaceView.getScene();
        // !important
        scene.backgroundColor().setAll(0x00000000);

        Light light = new Light();
        scene.lights().add(light);

        boxes = new Box[NUM];
        for (int i = 0; i < NUM; i++) {
            Box box = new Box(mGlSurfaceView.getGContext(), 0.5f, 0.5f, 0.5f);
            box.vertexColorsEnabled(false);
            box.position().x = (float) (-1.5 + Math.random() * 3);
            box.position().y = (float) (-4 + Math.random() * 8);
            box.position().z = (float) (-1.5 + Math.random() * 3);
            box.rotation().x = (float) (Math.random() * 360);
            box.rotation().y = (float) (Math.random() * 360);
            boxes[i] = box;
            scene.addChild(box);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGlSurfaceView = new RendererGLSurfaceView(this) {
            @Override
            public void onConfigSetting() {
                // !important
                setEGLConfigChooser(8, 8, 8, 8, 16, 0);
                getHolder().setFormat(PixelFormat.TRANSLUCENT);
            }
        };
        mGlSurfaceView.setSceneController(this);

        mGlSurfaceView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        setContentView(mGlSurfaceView);
    }

    @Override
    public void updateScene() {
        for (int i = 0; i < NUM; i++) {
            boxes[i].rotation().x += 2;
            boxes[i].rotation().y += 1;
            boxes[i].position().y -= .075;
            if (boxes[i].position().y < -4) {
                boxes[i].position().y = 4;
                boxes[i].position().x = (float) (-1.5 + Math.random() * 3);
                boxes[i].position().z = (float) (-1.5 + Math.random() * 3);
            }
        }
    }

    @Override
    public Handler getInitSceneHandler() {
        return null;
    }

    @Override
    public Runnable getInitSceneRunnable() {
        return null;
    }

    @Override
    public Handler getUpdateSceneHandler() {
        return null;
    }

    @Override
    public Runnable getUpdateSceneRunnable() {
        return null;
    }
}
