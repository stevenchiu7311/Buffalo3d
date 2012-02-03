package min3d.materials;

import javax.microedition.khronos.opengles.GL10;

import min3d.Shared;
import min3d.core.ManagedLightList;
import min3d.core.Object3d;
import min3d.core.Renderer;
import min3d.materials.Uniforms.UNIFORM_LIST_ID;
import min3d.vos.Light;
import min3d.vos.TextureVo;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;


public class OpenGLESV1Material extends AMaterial {
    private static final String TAG = "PhongMaterial";
    private static final String VERTEX_SHADER__FILE = "shader/openGLESV1_vs.txt";
    private static final String FRAGMENT_SHADER_FILE = "shader/openGLESV1_fs.txt";

    protected static String mVShader;
    protected static String mFShader;
    protected int muNormalMatrixHandle;
    protected int muUseObjectTransformHandle;
    protected int muSpecularColorHandle;
    protected int muAmbientColorHandle;
    protected int muShininessHandle;
    protected int mLightEnableHandle;
    protected float[] mNormalMatrix;
    protected boolean mLightEnable;

    public OpenGLESV1Material() {
        super(VERTEX_SHADER__FILE, FRAGMENT_SHADER_FILE);
        mNormalMatrix = new float[9];
    }

    public OpenGLESV1Material(float[] specularColor, float[] ambientColor,
            float shininess) {
        this();
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

        muNormalMatrixHandle = GLES20.glGetUniformLocation(mProgram,
                "u_transposeAdjointModelViewMatrix");
        if (muNormalMatrixHandle == -1) {
            // throw new
            throw new RuntimeException("Could not get uniform location for uNMatrix");
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
        android.graphics.Matrix normalMatrix = new android.graphics.Matrix();
        android.graphics.Matrix mvMatrix = new android.graphics.Matrix();

        mvMatrix.setValues(new float[] { modelMatrix[0], modelMatrix[1],
                modelMatrix[2], modelMatrix[4], modelMatrix[5], modelMatrix[6],
                modelMatrix[8], modelMatrix[9], modelMatrix[10] });

        normalMatrix.reset();
        mvMatrix.invert(normalMatrix);
        float[] values = new float[9];
        normalMatrix.getValues(values);

        normalMatrix.setValues(new float[] { values[0], values[3], values[6],
                values[1], values[4], values[7], values[2], values[5],
                values[8] });
        normalMatrix.getValues(mNormalMatrix);

        GLES20.glUniformMatrix3fv(muNormalMatrixHandle, 1, false,
                mNormalMatrix, 0);
    }

    protected void drawObject_textures(Object3d $o) {
       // iterate thru object's textures
       for (int i = 0; i < 2; i++) {
           GLES20.glActiveTexture(GL10.GL_TEXTURE0 + i);
           int type = usesCubeMap ? GLES20.GL_TEXTURE_CUBE_MAP
                   : GLES20.GL_TEXTURE_2D;

           int textureEnableHandle = GLES20.glGetUniformLocation(mProgram, "textureEnabled[" + Integer.toString(i) + "]");
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
                           "uTexture[" + Integer.toString(i) + "]");
                   if (textureHandle == -1) {
                       Log.d(TAG, "Could not get uniform location for uTexture");
                   }

                   GLES20.glUniform1i(textureHandle, i);


                   float[] mMMatrix = new float[16];
                   Matrix.setIdentityM(mMMatrix, 0);
                   Matrix.translateM(mMMatrix, 0, textureVo.offsetU, textureVo.offsetV,0);
                   int textrureMatrix = GLES20.glGetUniformLocation(mProgram, "u_textureMatrix[" + Integer.toString(i) + "]");
                   if (textrureMatrix > -1) {
                       GLES20.glUniformMatrix4fv(textrureMatrix, 1, false, mMMatrix, 0);
                   }
                   if (textureEnableHandle > -1) {
                       GLES20.glUniform1i(textureEnableHandle, 1);
                   }

                   int textureMode = GLES20.glGetUniformLocation(mProgram, "textureEnvMode[" + Integer.toString(i) + "]");

                   if (textureMode > -1) {
                       GLES20.glUniform1i(textureMode, textureVo.textureEnvs.get(0).param);
                   }
               }
           } else {
               if (textureEnableHandle > -1) {
                   GLES20.glUniform1i(textureEnableHandle, 0);
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

       Light[] lights = lightList.toArray();
       for (int i = 0; i < lights.length; i++){
           Light light = lights[i];
           UNIFORM_LIST_ID id;
           if (light.isDirty()){
               if (light.position.isDirty()){
                   id = getUniformId("light[" + Integer.toString(i) + "].position");
                   setUniformData(id , light.getLightPosition());
                   light.position.clearDirtyFlag();
               }

               if (light.ambient.isDirty()){
                   id = getUniformId("light[" + Integer.toString(i) + "].ambient");
                   setUniformData(id , new float[] {0.06f,0.0f,0.0f,1.0f});
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
                   setUniformData(id , new float[] {light.spotCutoffAngle()});
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