package min3d.core;

import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import min3d.Min3d;

import android.opengl.GLES20;
import android.os.Build;
import android.util.Log;

/**
 * Simple static class holding values representing various capabilities of
 * hardware's concrete OpenGL capabilities that are relevant to min3d's
 * supported features.
 */

public class RenderCaps {
    private static float _openGlVersion;
    private static boolean _isGl10Only;
    private static int _maxTextureUnits;
    private static int _maxTextureSize;
    private static int _aliasedPointSizeMin;
    private static int _aliasedPointSizeMax;
    private static int _smoothPointSizeMin;
    private static int _smoothPointSizeMax;
    private static int _aliasedLineSizeMin;
    private static int _aliasedLineSizeMax;
    private static int _smoothLineSizeMin;
    private static int _smoothLineSizeMax;
    private static int _maxLights;

    public static float openGlVersion() {
        return _openGlVersion;
    }

    public static boolean isGl10Only() {
        return _isGl10Only;
    }

    public static int maxTextureUnits() {
        return _maxTextureUnits;
    }

    public static int aliasedPointSizeMin() {
        return _aliasedPointSizeMin;
    }

    public static int aliasedPointSizeMax() {
        return _aliasedPointSizeMax;
    }

    public static int smoothPointSizeMin() {
        return _smoothPointSizeMin;
    }

    public static int smoothPointSizeMax() {
        return _smoothPointSizeMax;
    }

    public static int aliasedLineSizeMin() {
        return _aliasedLineSizeMin;
    }

    public static int aliasedLineSizeMax() {
        return _aliasedLineSizeMax;
    }

    public static int smoothLineSizeMin() {
        return _smoothLineSizeMin;
    }

    public static int smoothLineSizeMax() {
        return _smoothLineSizeMax;
    }

    public static int maxLights() {
        return _maxLights;
    }

    static void setRenderCaps(GL10 $gl) /* package-private */
    {
        int version = RendererGLSurfaceViewProxy.getGlesVersion();
        if (version == RendererGLSurfaceViewProxy.GLES20) {
            _openGlVersion = 2.0f;
            setRenderCapsES2();
        } else {
            _openGlVersion = 1.1f;
            setRenderCapsES1($gl);
        }
    }
    /**
     * Called by Renderer.onSurfaceCreate()
     */
    static void setRenderCapsES1(GL10 $gl) /* package-private */
    {
        IntBuffer i;

        // Max texture units
        i = IntBuffer.allocate(1);
        $gl.glGetIntegerv(GL10.GL_MAX_TEXTURE_UNITS, i);
        _maxTextureUnits = i.get(0);

        // Max texture size
        i = IntBuffer.allocate(1);
        $gl.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, i);
        _maxTextureSize = i.get(0);

        // Aliased point size range
        i = IntBuffer.allocate(2);
        $gl.glGetIntegerv(GL10.GL_ALIASED_POINT_SIZE_RANGE, i);
        _aliasedPointSizeMin = i.get(0);
        _aliasedPointSizeMax = i.get(1);

        // Smooth point size range
        i = IntBuffer.allocate(2);
        $gl.glGetIntegerv(GL10.GL_SMOOTH_POINT_SIZE_RANGE, i);
        _smoothPointSizeMin = i.get(0);
        _smoothPointSizeMax = i.get(1);

        // Aliased line width range
        i = IntBuffer.allocate(2);
        $gl.glGetIntegerv(GL10.GL_ALIASED_LINE_WIDTH_RANGE, i);
        _aliasedLineSizeMin = i.get(0);
        _aliasedLineSizeMax = i.get(1);

        // Smooth line width range
        i = IntBuffer.allocate(2);
        $gl.glGetIntegerv(GL10.GL_SMOOTH_LINE_WIDTH_RANGE, i);
        _smoothLineSizeMin = i.get(0);
        _smoothLineSizeMax = i.get(1);

        // Max lights
        i = IntBuffer.allocate(1);
        $gl.glGetIntegerv(GL10.GL_MAX_LIGHTS, i);
        _maxLights = i.get(0);

        Log.v(Min3d.TAG, "RenderCaps - openGLVersion: " + _openGlVersion);
        Log.v(Min3d.TAG, "RenderCaps - maxTextureUnits: " + _maxTextureUnits);
        Log.v(Min3d.TAG, "RenderCaps - maxTextureSize: " + _maxTextureSize);
        Log.v(Min3d.TAG, "RenderCaps - maxLights: " + _maxLights);
    }

    /**
     * Called by Renderer.onSurfaceCreate()
     */
    static void setRenderCapsES2() /* package-private */
    {
        int caps[] = null;

        // Max texture units
        caps = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_IMAGE_UNITS, caps, 0);
        _maxTextureUnits = caps[0];

        // Max texture size
        caps = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, caps, 0);
        _maxTextureSize = caps[0];

        // Aliased point size range
        caps = new int[2];
        GLES20.glGetIntegerv(GLES20.GL_ALIASED_POINT_SIZE_RANGE, caps, 0);
        _aliasedPointSizeMin = caps[0];
        _aliasedPointSizeMax = caps[1];

        // Smooth point size range
        caps = new int[2];
        GLES20.glGetIntegerv(GL10.GL_SMOOTH_POINT_SIZE_RANGE, caps, 0);
        _smoothPointSizeMin = caps[0];
        _smoothPointSizeMax = caps[1];

        // Aliased line width range
        caps = new int[2];
        GLES20.glGetIntegerv(GLES20.GL_ALIASED_LINE_WIDTH_RANGE, caps, 0);
        _aliasedLineSizeMin = caps[0];
        _aliasedLineSizeMax = caps[1];

        // Smooth line width range
        caps = new int[2];
        GLES20.glGetIntegerv(GL10.GL_SMOOTH_LINE_WIDTH_RANGE, caps, 0);
        _smoothLineSizeMin = caps[0];
        _smoothLineSizeMax = caps[1];

        Log.v(Min3d.TAG, "RenderCaps - openGLVersion: " + _openGlVersion);
        Log.v(Min3d.TAG, "RenderCaps - maxTextureUnits: " + _maxTextureUnits);
        Log.v(Min3d.TAG, "RenderCaps - maxTextureSize: " + _maxTextureSize);
    }
}
