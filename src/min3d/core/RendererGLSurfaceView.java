package min3d.core;

import javax.microedition.khronos.opengles.GL;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.AttributeSet;

import min3d.GLConfiguration;
import min3d.interfaces.IRendererGLSurfaceViewConfig;
import min3d.interfaces.ISceneController;

/**
 * Extend this class when creating your min3d-based GLSurfaceView.
 * Then, override initScene() and updateScene() for your main
 * 3D logic.
 *
 * Override onCreateSetContentView() to change layout, if desired.
 *
 * To update 3d scene-related variables from within the the main UI thread,
 * override onUpdateScene() and onUpdateScene() as needed.
 */
public class RendererGLSurfaceView extends RendererGLSurfaceViewProxy implements ISceneController, IRendererGLSurfaceViewConfig {

    protected Handler _initSceneHander;
    protected Handler _updateSceneHander;
    protected Scene scene;

    private boolean _renderContinuously;

    private GContext mGContext;

    private ISceneController mSceneController;

    public RendererGLSurfaceView(Context context) {
        this(context, null);
    }

    public RendererGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        _initSceneHander = new Handler();
        _updateSceneHander = new Handler();

        onConfigSetting();
        mGContext = new GContext(context);
        mGContext.setGLSurfaceView(this);
        GLConfiguration newGLConfig = new GLConfiguration();
        newGLConfig.mConfiguration = getResources().getConfiguration();
        newGLConfig.mOrientation = newGLConfig.mConfiguration.orientation;
        mGContext.setGLConfiguration(newGLConfig);
        min3d.core.Renderer r = new min3d.core.Renderer(mGContext);
        setGContext(mGContext);
        scene = new Scene(mGContext, this);
        r.setScene(scene);

        setRenderer(r);

        renderContinuously(true);
        setGLWrapper(new GLSurfaceView.GLWrapper() {
            public GL wrap(GL gl) {
                return new MatrixTrackingGL(gl);
            }
        });
    }

    /**
     * Allows you to use any Class implementing ISceneController to drive the
     * Scene...
     */
    public void setSceneController(ISceneController controller)
    {
        mSceneController = controller;
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
        if (mSceneController != null) {
            mSceneController.initScene();
        }
    }

    /**
     * All manipulation of scene and Object3D instance properties should go
     * here. Gets called on every frame, right before rendering.
     */
    public void updateScene() {
        if (mSceneController != null) {
            mSceneController.updateScene();
        }
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
     *
     * @param $b true to resumes the render loop
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

    public GContext getGContext() {
        return mGContext;
    }

    public Scene getScene() {
        return scene;
    }

    @Override
    public void onConfigSetting() {
        // Example which makes glSurfaceView transparent (along with setting scene.backgroundColor to 0x0)
        // _glSurfaceView.setEGLConfigChooser(8,8,8,8, 16, 0);
        // _glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        // Example of enabling logging of GL operations
        // _glSurfaceView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);
    }
}
