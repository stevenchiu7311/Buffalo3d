package min3d.materials;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.util.Log;

import min3d.Shared;
import min3d.core.Object3d;
import min3d.core.RenderCaps;
import min3d.vos.ALight;
import min3d.vos.CameraVo;
import min3d.vos.Color4;
import min3d.vos.TextureVo;

public abstract class AMaterial {
    static final private String TAG = "AMaterial";

    protected String mVertexShader;
    protected String mFragmentShader;

    protected int mProgram;
    protected int muMVPMatrixHandle;
    protected int maPositionHandle;
    protected int maTextureHandle;
    protected int maColorHandle;
    protected int maNormalHandle;
    protected int muCameraPositionHandle;
    protected int muUseTextureHandle;
    protected int muMMatrixHandle;
    protected int muVMatrixHandle;
    protected ALight mLight;

    protected int numTextures = 0;
    protected float[] mModelViewMatrix;
    protected float[] mViewMatrix;
    protected boolean usesCubeMap = false;

    public AMaterial(String vertexShader, String fragmentShader) {
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;
        setShaders(vertexShader, fragmentShader);
    }

    public void setShaders(String vertexShader, String fragmentShader) {
        mProgram = createProgram(mVertexShader, fragmentShader);
        if (mProgram == 0)
            return;

        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for aPosition");
        }

        maNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");
        if (maNormalHandle == -1) {
            // Log.d(TAG, "Could not get attrib location for aNormal " + getClass().toString());
        }

        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for aTextureCoord");
        }

        maColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        if (maColorHandle == -1) {
            Log.d(TAG, "Could not get attrib location for aColor");
        }

        muCameraPositionHandle = GLES20.glGetUniformLocation(mProgram,
                "uCameraPosition");
        if (muCameraPositionHandle == -1) {
            // throw new RuntimeException("Could not get attrib location for uCameraPosition");
        }

        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }

        muMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMMatrix");
        if (muMMatrixHandle == -1) {
            // Log.d(TAG, "Could not get attrib location for uMMatrix");
        }

        muVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uVMatrix");
        if (muVMatrixHandle == -1) {
            // Log.d(TAG, "Could not get attrib location for uVMatrix");
        }

        muUseTextureHandle = GLES20.glGetUniformLocation(mProgram,
                "uUseTexture");
        if (muUseTextureHandle == -1) {
            Log.d(TAG, "Could not get uniform location for uUseTexture");
        }
    }

    protected int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile "
                        + (shaderType == 35632 ? "fragment" : "vertex")
                        + " shader:");
                Log.e(TAG, "Shader log: " + GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    protected int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            GLES20.glAttachShader(program, pixelShader);
            GLES20.glLinkProgram(program);

            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    public void useProgram() {
        GLES20.glUseProgram(mProgram);
    }

    public void bindTextures(Object3d obj) {
        int num = obj.textures().size();
        drawObject_textures(obj);
        GLES20.glUniform1i(muUseTextureHandle, num == 0 ? 0 : 1);
    }

    private void drawObject_textures(Object3d $o) {
        // iterate thru object's textures
        for (int i = 0; i < RenderCaps.maxTextureUnits(); i++) {
            GLES20.glActiveTexture(GL10.GL_TEXTURE0 + i);
            int type = usesCubeMap ? GLES20.GL_TEXTURE_CUBE_MAP
                    : GLES20.GL_TEXTURE_2D;
            if ($o.hasUvs() && $o.texturesEnabled()) {
                TextureVo textureVo = ((i < $o.textures().size())) ? textureVo = $o
                        .textures().get(i) : null;

                if (textureVo != null) {
                    // activate texture
                    int glId = Shared.textureManager()
                            .getGlTextureId(textureVo.textureId);
                    GLES20.glBindTexture(type, glId);
                    GLES20.glEnable(type);

                    int minFilterType = Shared.textureManager().hasMipMap(textureVo.textureId) ?
                            GLES20.GL_LINEAR_MIPMAP_NEAREST
                            : GLES20.GL_NEAREST;
                    GLES20.glTexParameterf(type,
                            GLES20.GL_TEXTURE_MIN_FILTER,
                            minFilterType);
                    GLES20.glTexParameterf(type,
                            GLES20.GL_TEXTURE_MAG_FILTER,
                            GLES20.GL_LINEAR); // (OpenGL default)
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                            GLES20.GL_TEXTURE_WRAP_S,
                            (textureVo.repeatU ? GLES20.GL_REPEAT : GLES20.GL_CLAMP_TO_EDGE));
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                            GLES20.GL_TEXTURE_WRAP_T,
                            (textureVo.repeatU ? GLES20.GL_REPEAT : GLES20.GL_CLAMP_TO_EDGE));

                    if (minFilterType == GLES20.GL_LINEAR_MIPMAP_NEAREST) {
                        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
                    }

                    int textureHandle = GLES20.glGetUniformLocation(mProgram,
                            "uTexture0");
                    if (textureHandle == -1) {
                        Log.d(TAG, toString());
                        throw new RuntimeException("Could not get attrib location for uTexture0");
                    }

                    GLES20.glUniform1i(textureHandle, i);
                }
            } else {
                GLES20.glBindTexture(type, 0);
                GLES20.glDisable(type);
            }
        }
    }

    public void setVertices(FloatBuffer vertices) {
        vertices.position(0);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertices);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
    }

    public void setVertices() {
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
    }

    public void setTextureCoords(FloatBuffer textureCoords) {
        textureCoords.position(0);
        GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false, 0, textureCoords);
        GLES20.glEnableVertexAttribArray(maTextureHandle);
    }

    public void setTextureCoords() {
        GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glEnableVertexAttribArray(maTextureHandle);
    }

    public void setColors(ByteBuffer colors) {
        colors.position(0);
        GLES20.glVertexAttribPointer(maColorHandle, 4, GLES20.GL_UNSIGNED_BYTE, false, 0, colors);
        GLES20.glEnableVertexAttribArray(maColorHandle);
    }

    public void setColors(Color4 color) {
        GLES20.glVertexAttrib4f(maColorHandle, color.r, color.g, color.b, color.a);
    }

    public void setColors() {
        GLES20.glVertexAttribPointer(maColorHandle, 4, GLES20.GL_UNSIGNED_BYTE, false, 0, 0);
        GLES20.glEnableVertexAttribArray(maColorHandle);
    }

    public void setNormals(FloatBuffer normals) {
        normals.position(0);
        GLES20.glVertexAttribPointer(maNormalHandle, 3, GLES20.GL_FLOAT, false, 0, normals);
        GLES20.glEnableVertexAttribArray(maNormalHandle);
    }

    public void setNormals() {
        GLES20.glVertexAttribPointer(maNormalHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glEnableVertexAttribArray(maNormalHandle);
    }

    public void setMVPMatrix(float[] mvpMatrix) {
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mvpMatrix, 0);
    }

    public void setModelMatrix(float[] modelMatrix) {
        mModelViewMatrix = modelMatrix;
        if (muMMatrixHandle > -1)
            GLES20.glUniformMatrix4fv(muMMatrixHandle, 1, false, modelMatrix, 0);
    }

    public void setViewMatrix(float[] viewMatrix) {
        mViewMatrix = viewMatrix;
        if (muVMatrixHandle > -1)
            GLES20.glUniformMatrix4fv(muVMatrixHandle, 1, false, viewMatrix, 0);
    }

    public void setLight(ALight light) {
        mLight = light;
    }

    public void setCamera(CameraVo camera) {
        if (muCameraPositionHandle > -1)
            GLES20.glUniform3fv(muCameraPositionHandle,
                    1,
                    new float[] { camera.position.x, camera.position.y, camera.position.z },
                    0);
    }

    public String toString() {
        StringBuffer out = new StringBuffer();
        out.append("____ VERTEX SHADER ____\n");
        out.append(mVertexShader);
        out.append("____ FRAGMENT SHADER ____\n");
        out.append(mFragmentShader);
        return out.toString();
    }

    public float[] getModelViewMatrix() {
        return mModelViewMatrix;
    }
}
