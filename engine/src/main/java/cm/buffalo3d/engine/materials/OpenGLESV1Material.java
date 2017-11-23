package cm.buffalo3d.engine.materials;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import cm.buffalo3d.engine.core.GContext;
import cm.buffalo3d.engine.core.ManagedLightList;
import cm.buffalo3d.engine.core.Object3d;
import cm.buffalo3d.engine.core.Renderer;
import cm.buffalo3d.engine.materials.Uniforms.UNIFORM_LIST_ID;
import cm.buffalo3d.engine.vos.Light;
import cm.buffalo3d.engine.vos.TextureVo;


public class OpenGLESV1Material extends AMaterial {
    private static final String TAG = "OpenGLESV1Material";
    private static final String VERTEX_SHADER__FILE = "shader/openGLESV1_vs.txt";
    private static final String FRAGMENT_SHADER_FILE = "shader/openGLESV1_fs.txt";

    protected static String mVShader;
    protected static String mFShader;
    protected int muNormalMatrixHandle;
    protected int mLightEnableHandle;
    protected float[] mNormalUniformMatrix;
    protected boolean mLightEnable;

    android.graphics.Matrix mNormalMatrix = new android.graphics.Matrix();
    android.graphics.Matrix mvMatrix = new android.graphics.Matrix();
    float[] mValues = new float[9];
    float[] mNormalValue = new float[9];
    float[] mMMatrix = new float[16];

    public OpenGLESV1Material(GContext context) {
        super(context, VERTEX_SHADER__FILE, FRAGMENT_SHADER_FILE);
        mNormalUniformMatrix = new float[9];
    }

    @Override
    public void useProgram() {
        super.useProgram();
        GLES20.glUniform1i(mLightEnableHandle, mLightEnable ? 1 : 0);
    }

    public void setLightEnabled(boolean enable) {
        mLightEnable = enable;
    }

    @Override
    public void setShaders(String vertexShader, String fragmentShader) {
        super.setShaders(vertexShader, fragmentShader);

        if (mLightNumber > 0) {
            muNormalMatrixHandle = GLES20.glGetUniformLocation(mProgram,
                    "u_transposeAdjointModelViewMatrix");
            if (muNormalMatrixHandle == -1) {
                // throw new
                throw new RuntimeException(
                        "Could not get uniform location for uNMatrix");
            }
        }
        mLightEnableHandle = GLES20.glGetUniformLocation(mProgram,
                "u_lightingEnabled");
        if (mLightEnableHandle == -1) {
            Log.d(TAG, "Could not get uniform location for LightEnable");
        }
    }

    @Override
    public void setModelMatrix(float[] modelMatrix) {
        super.setModelMatrix(modelMatrix);
        if (mLightNumber == 0) {
            return;
        }
        mValues[0] = modelMatrix[0];
        mValues[1] = modelMatrix[1];
        mValues[2] = modelMatrix[2];
        mValues[3] = modelMatrix[4];
        mValues[4] = modelMatrix[5];
        mValues[5] = modelMatrix[6];
        mValues[6] = modelMatrix[8];
        mValues[7] = modelMatrix[9];
        mValues[8] = modelMatrix[10];
        mvMatrix.setValues(mValues);

        mNormalMatrix.reset();
        mvMatrix.invert(mNormalMatrix);

        mNormalMatrix.getValues(mNormalValue);
        mValues[0] = mNormalValue[0];
        mValues[1] = mNormalValue[3];
        mValues[2] = mNormalValue[6];
        mValues[3] = mNormalValue[1];
        mValues[4] = mNormalValue[4];
        mValues[5] = mNormalValue[7];
        mValues[6] = mNormalValue[2];
        mValues[7] = mNormalValue[5];
        mValues[8] = mNormalValue[8];

        mNormalMatrix.setValues(mValues);
        mNormalMatrix.getValues(mNormalUniformMatrix);

        GLES20.glUniformMatrix3fv(muNormalMatrixHandle, 1, false,
                mNormalUniformMatrix, 0);
    }

    protected void drawObject_textures(Object3d $o) {
        // iterate thru object's textures
        for (int i = 0; i < 2; i++) {
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

                    Matrix.setIdentityM(mMMatrix, 0);
                    Matrix.translateM(mMMatrix, 0, textureVo.offsetU, textureVo.offsetV, 0);
                    switch (i) {
                        case 0:
                            setUniformData(Uniforms.UNIFORM_LIST_ID.TEXTURE0, i);
                            setUniformMatrix(Uniforms.UNIFORM_LIST_ID.TEXTURE0_MATRIX, mMMatrix);
                            setUniformData(Uniforms.UNIFORM_LIST_ID.TEXTURE0_ENV_MODE, textureVo.textureEnvs.get(0).param);
                            setUniformData(Uniforms.UNIFORM_LIST_ID.TEXTURE0_ENABLED, 1);
                            break;
                        case 1:
                            setUniformData(Uniforms.UNIFORM_LIST_ID.TEXTURE1, i);
                            setUniformMatrix(Uniforms.UNIFORM_LIST_ID.TEXTURE1_MATRIX, mMMatrix);
                            setUniformData(Uniforms.UNIFORM_LIST_ID.TEXTURE1_ENV_MODE, textureVo.textureEnvs.get(0).param);
                            setUniformData(Uniforms.UNIFORM_LIST_ID.TEXTURE1_ENABLED, 1);
                            break;
                    }
                }
            } else {
                switch (i) {
                    case 0:
                        setUniformData(Uniforms.UNIFORM_LIST_ID.TEXTURE0_ENABLED, 0);
                        break;
                    case 1:
                        setUniformData(Uniforms.UNIFORM_LIST_ID.TEXTURE1_ENABLED, 0);
                        break;
                }
                GLES20.glBindTexture(type, 0);
                GLES20.glDisable(type);
            }
       }
   }

   public void setLightList(ManagedLightList lightList) {
       for (int glIndex = 0; glIndex < Renderer.NUM_GLLIGHTS; glIndex++){
           if (lightList.glIndexEnabledDirty()[glIndex] == true){
               UNIFORM_LIST_ID id = getUniformId("light_enable[" + Integer.toString(glIndex) + "]");
               if (lightList.glIndexEnabled()[glIndex] == true){
                   setUniformData(id, 1);
                   lightList.getLightByGlIndex(glIndex).setAllDirty();
               }
               else{
                   setUniformData(id, 0);
               }
               lightList.glIndexEnabledDirty()[glIndex] = false;
           }
       }

       for (int i = 0; i < lightList.size(); i++){
           Light light = lightList.get(i);
           UNIFORM_LIST_ID id;
           if (light.isDirty()){
               if (light.position.isDirty()){
                   id = getUniformId("light[" + Integer.toString(i) + "].position");
                   setUniformData(id , light.getLightPosition());
                   light.position.clearDirtyFlag();
               }

               if (light.ambient.isDirty()){
                   id = getUniformId("light[" + Integer.toString(i) + "].ambient");
                   setUniformData(id , light.ambient.toFloat());
                   light.ambient.clearDirtyFlag();
               }

               if (light.diffuse.isDirty()){
                   id = getUniformId("light[" + Integer.toString(i) + "].diffuse");
                   setUniformData(id , light.diffuse.toFloat());
                   light.diffuse.clearDirtyFlag();
               }

               if (light.specular.isDirty()){
                   id = getUniformId("light[" + Integer.toString(i) + "].specular");
                   setUniformData(id , light.specular.toFloat());
                   light.specular.clearDirtyFlag();
               }

               if (light._spotCutoffAngle.isDirty()){
                   id = getUniformId("light[" + Integer.toString(i) + "].spotCutoffAngleCos");
                   float spotCutoffAngleCos = (float) Math.cos(Math.PI * light.spotCutoffAngle() / 180.0);
                   setUniformData(id , new float[] {spotCutoffAngleCos});
               }

               if (light._spotExponent.isDirty()){
                   id = getUniformId("light[" + Integer.toString(i) + "].spotExponent");
                   setUniformData(id , new float[] {light.spotExponent()});
               }

               if (light._isVisible.isDirty()){
                   id = getUniformId("light_enable[" + Integer.toString(i) + "]");
                   if (light.isVisible()) {
                       setUniformData(id, 1);
                   } else {
                       setUniformData(id, 0);
                   }
                   light._isVisible.clearDirtyFlag();
               }

               if (light._attenuation.isDirty()){
                   id = getUniformId("light[" + Integer.toString(i) + "].constantAttenuation");
                   setUniformData(id, new float[] {light.attenuationConstant()});

                   id = getUniformId("light[" + Integer.toString(i) + "].linearAttenuation");
                   setUniformData(id, new float[] {light.attenuationLinear()});

                   id = getUniformId("light[" + Integer.toString(i) + "].quadraticAttenuation");
                   setUniformData(id, new float[] {light.attenuationQuadratic()});
               }
               light.clearDirtyFlag();
           }
       }
   }
}