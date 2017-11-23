package cm.buffalo3d.engine.interfaces;

/**
 * Any GlSurfaceView settings that needs to be executed before
 * GLSurfaceView.setRenderer() can be done by overriding this method.
 * A couple examples are included in comments below.
 */
public interface IRendererGLSurfaceViewConfig {
    void onConfigSetting();
}
