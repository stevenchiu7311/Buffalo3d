package min3d.core;

import javax.microedition.khronos.opengles.GL;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.AttributeSet;

import min3d.Shared;
import min3d.interfaces.ISceneController;

public class RendererGLSurfaceView extends RendererGLSurfaceViewProxy implements ISceneController {

    protected Handler _initSceneHander;
    protected Handler _updateSceneHander;
    protected Scene scene;

    private boolean _renderContinuously;

    public RendererGLSurfaceView(Context context) {
        this(context, null);
    }

    public RendererGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        _initSceneHander = new Handler();
        _updateSceneHander = new Handler();

        // These 4 lines are important.
        Shared.context(context);
        scene = new Scene(this);
        mRenderer = new min3d.core.Renderer(scene);
        Shared.renderer(mRenderer);

        glSurfaceViewConfig();
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setGLWrapper(new GLSurfaceView.GLWrapper() {
            @Override
            public GL wrap(GL gl) {
                return new MatrixTrackingGL(gl);
            }
        });
    }

    final Runnable _initSceneRunnable = new Runnable() {
        public void run() {
            onInitScene();
        }
    };

    final Runnable _updateSceneRunnable = new Runnable() {
        public void run() {
            onUpdateScene();
        }
    };

    /**
     * Instantiation of Object3D's, setting their properties, and adding
     * Object3D's to the scene should be done here. Or any point thereafter.
     *
     * Note that this method is always called after GLCanvas is created, which
     * occurs not only on Activity.onCreate(), but on Activity.onResume() as
     * well. It is the user's responsibility to build the logic to restore state
     * on-resume.
     */
    public void initScene() {
    }

    /**
     * All manipulation of scene and Object3D instance properties should go
     * here. Gets called on every frame, right before rendering.
     */
    public void updateScene() {
    }

    /**
     * Called _after_ scene init (ie, after initScene). Unlike initScene(), gets
     * called from the UI thread.
     */
    public void onInitScene() {
    }

    /**
     * Called _after_ updateScene() Unlike initScene(), gets called from the UI
     * thread.
     */
    public void onUpdateScene() {
    }

    /**
     * Setting this to false stops the render loop, and initScene() and
     * onInitScene() will no longer fire. Setting this to true resumes it.
     */
    public void renderContinuously(boolean $b) {
        _renderContinuously = $b;
        if (_renderContinuously)
            setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        else
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public Handler getInitSceneHandler() {
        return _initSceneHander;
    }

    public Handler getUpdateSceneHandler() {
        return _updateSceneHander;
    }

    public Runnable getInitSceneRunnable() {
        return _initSceneRunnable;
    }

    public Runnable getUpdateSceneRunnable() {
        return _updateSceneRunnable;
    }

    protected void glSurfaceViewConfig() {
    }
}
