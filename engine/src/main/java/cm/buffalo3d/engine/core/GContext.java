package cm.buffalo3d.engine.core;

import android.content.Context;
import android.opengl.GLSurfaceView;

import cm.buffalo3d.engine.vos.GLConfiguration;


public class GContext {
    private Context mContext;
    private Renderer mRenderer;
    private TextureManager mTextureManager;
    private GLSurfaceView mGLSurfaceView;
    private GLConfiguration mGLConfiguration;

    public GContext(Context context) {
        this(context, null);
    }

    public GContext(Context context, Renderer renderer) {
        mContext = context;
        mRenderer = renderer;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void setRenderer(Renderer renderer) {
        mRenderer = renderer;
    }

    public void setTexureManager(TextureManager manager) {
        mTextureManager = manager;
    }

    public void setGLSurfaceView(GLSurfaceView view) {
        mGLSurfaceView = view;
    }

    public void setGLConfiguration(GLConfiguration configuration) {
        mGLConfiguration = configuration;
    }

    public Context getContext() {
        return mContext;
    }

    public Renderer getRenderer() {
        return mRenderer;
    }

    public TextureManager getTexureManager() {
        return mTextureManager;
    }

    public GLSurfaceView getGLSurfaceView() {
        return mGLSurfaceView;
    }

    public GLConfiguration getGLConfiguration() {
        return mGLConfiguration;
    }
}
