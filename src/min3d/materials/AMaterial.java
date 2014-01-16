package min3d.materials;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.util.Log;

import min3d.core.GContext;
import min3d.core.ManagedLightList;
import min3d.core.Object3d;
import min3d.core.RenderCaps;
import min3d.materials.Uniforms.UNIFORM_LIST_ID;
import min3d.vos.CameraVo;
import min3d.vos.Color4;
import min3d.vos.FogType;
import min3d.vos.TextureVo;

public abstract class AMaterial {
    static final private String TAG = "AMaterial";
    static final private String MAIN_VERTEX_SHADER__FILE = "shader/main_vs.txt";
    static final private String MAIN_FRAGMENT_SHADER_FILE = "shader/main_fs.txt";

    protected String mVertexShader;
    protected String mFragmentShader;
    protected String mVertexShaderDefine = "";
    protected String mFragmeShaderDefine = "";

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

    protected int numTextures = 0;
    protected float[] mModelViewMatrix;
    protected float[] mViewMatrix;
    protected boolean usesCubeMap = false;
    protected int mLightNumber = 0;

    protected GContext mGContext = null;

    HashMap<UNIFORM_LIST_ID, Object> mLocationId = new HashMap<UNIFORM_LIST_ID, Object>();
    boolean init = true;

    public AMaterial(GContext context, String vertexShader, String fragmentShader) {
        mGContext = context;
        mVertexShader = readShader(MAIN_VERTEX_SHADER__FILE) + readShader(vertexShader);
        mFragmentShader = readShader(MAIN_FRAGMENT_SHADER_FILE) + readShader(fragmentShader);
    }

    public void compilerShaders() {
        setShaders(mVertexShaderDefine + mVertexShader, mFragmeShaderDefine + mFragmentShader);
    }

    public void setVertexShaderDefine(String vertexShaderDefine) {
        mVertexShaderDefine = vertexShaderDefine;
    }

    public void setFragmeShaderDefine(String fragmeShaderDefine) {
        mFragmeShaderDefine = fragmeShaderDefine;
    }

    public void setShaders(String vertexShader, String fragmentShader) {
        mProgram = createProgram(vertexShader, fragmentShader);
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

    private void initUniform() {
        for (UNIFORM_LIST_ID uniform : UNIFORM_LIST_ID.values()) {
            int handleID = GLES20.glGetUniformLocation(mProgram,
                    uniform.getName());
            if (handleID == -1) {
                continue;
            }
            mLocationId.put(uniform, handleID);
            float data[] = uniform.getDefaultFloatData();
            if (data == null) {
                continue;
            }
            switch (uniform.getDefaultFloatData().length) {
            case 1:
                GLES20.glUniform1fv(handleID, 1, new float[] { data[0] }, 0);
                break;
            case 2:
                GLES20.glUniform2fv(handleID, 1,
                        new float[] { data[0], data[1] }, 0);
                break;
            case 3:
                GLES20.glUniform3fv(handleID, 1, new float[] { data[0],
                        data[1], data[2] }, 0);
                break;
            case 4:
                GLES20.glUniform4fv(handleID, 1, new float[] { data[0],
                        data[1], data[2], data[3] }, 0);
                break;
            }
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
        if (init) {
            initUniform();
            init = false;
        }
    }

    public void bindTextures(Object3d obj) {
        int num = obj.getTextures().size();
        drawObject_textures(obj);
        GLES20.glUniform1i(muUseTextureHandle, num == 0 ? 0 : 1);
    }

    protected void drawObject_textures(Object3d $o) {
        // iterate thru object's textures
        for (int i = 0; i < RenderCaps.maxTextureUnits(); i++) {
            GLES20.glActiveTexture(GL10.GL_TEXTURE0 + i);
            int type = usesCubeMap ? GLES20.GL_TEXTURE_CUBE_MAP
                    : GLES20.GL_TEXTURE_2D;
            if ($o.hasUvs() && $o.texturesEnabled()) {
                TextureVo textureVo = ((i < $o.getTextures().size())) ? textureVo = $o
                        .getTextures().get(i) : null;

                if (textureVo != null) {
                    // activate texture
                    int glId = mGContext.getTexureManager()
                            .getGlTextureId(textureVo.textureId);
                    GLES20.glBindTexture(type, glId);
                    GLES20.glEnable(type);

                    int minFilterType = mGContext.getTexureManager()
                            .hasMipMap(textureVo.textureId) ? GLES20.GL_LINEAR_MIPMAP_NEAREST
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

    public void setLightEnabled(boolean enable) {}

    public void setLightList(ManagedLightList lightList) {}

    public UNIFORM_LIST_ID getUniformId(String name) {
        for (UNIFORM_LIST_ID uniform : UNIFORM_LIST_ID.values()) {
            if (uniform.getName().equals(name)) {
                return uniform;
            }
        }
        return null;
    }

    public void setUniformData(UNIFORM_LIST_ID id, float[] value) {
        if (value == null || mLocationId.get(id) == null) {
            return;
        }
        int locationId = (Integer) mLocationId.get(id);
        switch (value.length) {
        case 1: GLES20.glUniform1f(locationId, value[0]);
                break;
        case 2: GLES20.glUniform2fv(locationId, 1, new float[] { value[0],value[1] }, 0);
                break;
        case 3: GLES20.glUniform3fv(locationId, 1, new float[] { value[0],value[1], value[2] }, 0);
                break;
        case 4: GLES20.glUniform4fv(locationId, 1, new float[] { value[0],value[1], value[2], value[3] }, 0);
                break;
        }
    }

    public void setUniformData(UNIFORM_LIST_ID id, int value) {
        if (mLocationId.get(id) == null) {
            return;
        }
        int locationId = (Integer) mLocationId.get(id);
        if (locationId == -1) {
            return;
        }
        GLES20.glUniform1i(locationId, value);
    }

    void setUniformMatrix(UNIFORM_LIST_ID id, float[] matrix) {
        if (mLocationId.get(id) == null) {
            return;
        }
        int locationId = (Integer) mLocationId.get(id);
        if (locationId == -1) {
            return;
        }
        GLES20.glUniformMatrix4fv(locationId, 1, false, matrix, 0);
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

    public void setFog(boolean fogEnabled, Color4 fogColor, float fogNear,
            float fogFar, FogType fogType) {
        setUniformData(UNIFORM_LIST_ID.FOG_ENABLED, fogEnabled? 1:0);
        if (fogEnabled) {
            setUniformData(UNIFORM_LIST_ID.FOG_COLOR, new float[]{fogColor.r, fogColor.g, fogColor.b});
            setUniformData(UNIFORM_LIST_ID.FOG_START, new float[]{fogNear});
            setUniformData(UNIFORM_LIST_ID.FOG_END, new float[]{fogFar});
            setUniformData(UNIFORM_LIST_ID.FOG_MODE, fogType.glValue());
        }
    }

    public void setMaterialColorEnable(boolean colorMaterialEnabled) {
        // TODO Auto-generated method stub
        setUniformData(UNIFORM_LIST_ID.MATERIAL_COLOR_ENABLE, colorMaterialEnabled? 1:0);
    }

    private String readShader(String path) {
        StringBuffer vs = new StringBuffer();
        try {

            InputStream inputStream = null ;
            inputStream = getClass().getResourceAsStream(
                    path);
            if (inputStream == null) {
                inputStream = mGContext.getContext().getResources().getAssets().open(path.substring(path.lastIndexOf("/")+1));
            }
            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(inputStream));

            String read = in.readLine();
            while (read != null) {
                vs.append(read + "\n");
                read = in.readLine();
            }
            vs.deleteCharAt(vs.length() - 1);
            return vs.toString();
        } catch (Exception e) {
            Log.d("ERROR-readingShader", "Could not read shader: " + e.getLocalizedMessage());
        }
        return null;
    }

    public void setLightNumber(int number) {
        mLightNumber = number;
    }
}
