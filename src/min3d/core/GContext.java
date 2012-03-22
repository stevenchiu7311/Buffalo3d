package min3d.core;

import android.content.Context;

public class GContext {
    private Context mContext;
    private Renderer mRenderer;
    private TextureManager mTextureManager;

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

    public Context getContext() {
        return mContext;
    }

    public Renderer getRenderer() {
        return mRenderer;
    }

    public TextureManager getTexureManager() {
        return mTextureManager;
    }
}
