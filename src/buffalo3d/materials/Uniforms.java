package buffalo3d.materials;

public class Uniforms {
    String mName;
    int mUniformLocation;
    float[] mData;

    public static enum UNIFORM_LIST_ID
    {
        POSITION_ENABLED("u_positionEnabled"),
        NORMAL_ENABLED("u_normalEnabled"),
        COLOR_ENABLED("u_colorEnabled"),
        TEXCOORD0_ENABLED("TEXCOORD0_ENABLED"),
        TEXCOORD1_ENABLED("TEXCOORD1_ENABLED"),
        TEXCOORD2_ENABLED("u_texCoord2Enabled"),
        LIGHTING_ENABLED("u_lightingEnabled"),
        LIGHT_MODEL_LOCAL_VIEWER_ENABLED("u_lightModelLocalViewerEnabled"),
        LIGHT_MODEL_TWO_SIDE_ENABLED("u_lightingEnabled"),
        LIGHT0_ENABLED("light_enable[0]"),
        LIGHT1_ENABLED("light_enable[1]"),
        LIGHT2_ENABLED("light_enable[2]"),
        LIGHT3_ENABLED("light_enable[3]"),
        LIGHT4_ENABLED("light_enable[4]"),
        LIGHT5_ENABLED("light_enable[5]"),
        LIGHT6_ENABLED("light_enable[6]"),
        LIGHT7_ENABLED("light_enable[7]"),
        TEXTURE0_ENABLED("textureEnabled[0]"),
        TEXTURE1_ENABLED("textureEnabled[1]"),
        TEXTURE2_ENABLED("u_texture0Enabled"),
        TEXTURE0_MATRIX_ENABLED("u_texture0MatrixEnabled"),
        TEXTURE1_MATRIX_ENABLED("u_texture1MatrixEnabled"),
        TEXTURE2_MATRIX_ENABLED("u_texture2MatrixEnabled"),
        FOG_ENABLED("u_fogEnabled"),
        FOG_MODE("u_fogMode"),
        FOG_HINT("u_fogHint"),
        TEXTURE0("uTexture[0]"),
        TEXTURE1("uTexture[1]"),
        TEXTURE0_MATRIX("u_textureMatrix[0]"),
        TEXTURE1_MATRIX("u_textureMatrix[1]"),
        TEXTURE0_ENV_MODE("textureEnvMode[0]"),
        TEXTURE1_ENV_MODE("textureEnvMode[1]"),
/*      ALPHA_TEST_ENABLED("u_alphaTestEnabled"),
        CLIP_PLANE0_ENABLED("u_clipPlane0Enabled"),
        CLIP_PLANE1_ENABLED("u_clipPlane1Enabled"),
        CLIP_PLANE2_ENABLED("u_clipPlane2Enabled"),
        CLIP_PLANE3_ENABLED("u_clipPlane3Enabled"),
        CLIP_PLANE4_ENABLED("u_clipPlane4Enabled"),
        CLIP_PLANE5_ENABLED("u_clipPlane5Enabled"),
        RESCALE_NORMAL_ENABLED("u_rescaleNormalEnabled"),
        NORMALIZE_ENABLED("u_normalizeEnabled"),
        ALPHA_FUNC("u_alphaFunc"),
        TEXTURE0_FORMAT("u_texture0Format"),
        TEXTURE1_FORMAT("u_texture1Format"),
        TEXTURE2_FORMAT("u_texture2Format"),
        TEXTURE0_ENV_MODE("u_texture0EnvMode"),
        TEXTURE1_ENV_MODE("u_texture1EnvMode"),
        TEXTURE2_ENV_MODE("u_texture2EnvMode"),
        TEXTURE0_ENV_COMBINE_RGB("u_texture0EnvCombineRGB"),
        TEXTURE1_ENV_COMBINE_RGB("u_texture1EnvCombineRGB"),
        TEXTURE2_ENV_COMBINE_RGB("u_texture2EnvCombineRGB"),
        TEXTURE0_ENV_COMBINE_ALPHA("u_texture0EnvCombineAlpha"),
        TEXTURE1_ENV_COMBINE_ALPHA("u_texture1EnvCombineAlpha"),
        TEXTURE2_ENV_COMBINE_ALPHA("u_texture2EnvCombineAlpha"),
        TEXTURE0_ENV_SRC0_RGB,
        TEXTURE0_ENV_SRC1_RGB,
        TEXTURE0_ENV_SRC2_RGB,
        TEXTURE1_ENV_SRC0_RGB,
        TEXTURE1_ENV_SRC1_RGB,
        TEXTURE1_ENV_SRC2_RGB,
        TEXTURE2_ENV_SRC0_RGB,
        TEXTURE2_ENV_SRC1_RGB,
        TEXTURE2_ENV_SRC2_RGB,
        TEXTURE0_ENV_OPERAND0_RGB,
        TEXTURE0_ENV_OPERAND1_RGB,
        TEXTURE0_ENV_OPERAND2_RGB,
        TEXTURE1_ENV_OPERAND0_RGB,
        TEXTURE1_ENV_OPERAND1_RGB,
        TEXTURE1_ENV_OPERAND2_RGB,
        TEXTURE2_ENV_OPERAND0_RGB,
        TEXTURE2_ENV_OPERAND1_RGB,
        TEXTURE2_ENV_OPERAND2_RGB,
        TEXTURE0_ENV_SRC0_ALPHA,
        TEXTURE0_ENV_SRC1_ALPHA,
        TEXTURE0_ENV_SRC2_ALPHA,
        TEXTURE1_ENV_SRC0_ALPHA,
        TEXTURE1_ENV_SRC1_ALPHA,
        TEXTURE1_ENV_SRC2_ALPHA,
        TEXTURE2_ENV_SRC0_ALPHA,
        TEXTURE2_ENV_SRC1_ALPHA,
        TEXTURE2_ENV_SRC2_ALPHA,
        TEXTURE0_ENV_OPERAND0_ALPHA,
        TEXTURE0_ENV_OPERAND1_ALPHA,
        TEXTURE0_ENV_OPERAND2_ALPHA,
        TEXTURE1_ENV_OPERAND0_ALPHA,
        TEXTURE1_ENV_OPERAND1_ALPHA,
        TEXTURE1_ENV_OPERAND2_ALPHA,
        TEXTURE2_ENV_OPERAND0_ALPHA,
        TEXTURE2_ENV_OPERAND1_ALPHA,
        TEXTURE2_ENV_OPERAND2_ALPHA,
        LIGHTING_HINT("u_lightingHint"),
        TEXTURE0_MATRIX,
        TEXTURE1_MATRIX,
        TEXTURE2_MATRIX,
        TEXTURE0_SAMPLER,
        TEXTURE1_SAMPLER,
        TEXTURE2_SAMPLER,
*/
        TEXTURE0_ENV_COLOR("u_texture0EnvColor",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        TEXTURE1_ENV_COLOR("u_texture1EnvColor",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        TEXTURE2_ENV_COLOR("u_texture2EnvColor",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        TEXTURE0_ENV_RGB_SCALE("u_texture0EnvRGBScale",UNIFORM_TYPE.FLOAT, new float[] {1.0f}, false),
        TEXTURE1_ENV_RGB_SCALE("u_texture1EnvRGBScale",UNIFORM_TYPE.FLOAT, new float[] {1.0f}, false),
        TEXTURE2_ENV_RGB_SCALE("u_texture2EnvRGBScale",UNIFORM_TYPE.FLOAT, new float[] {1.0f}, false),
        TEXTURE0_ENV_ALPHA_SCALE("u_texture0EnvAlphaScale",UNIFORM_TYPE.FLOAT, new float[] {1.0f}, false),
        TEXTURE1_ENV_ALPHA_SCALE("u_texture1EnvAlphaScale",UNIFORM_TYPE.FLOAT, new float[] {1.0f}, false),
        TEXTURE2_ENV_ALPHA_SCALE("u_texture2EnvAlphaScale",UNIFORM_TYPE.FLOAT, new float[] {1.0f}, false),
        TEXTURE0_ENV_BLUR_AMOUNT("u_texture0EnvBlurAmount",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        TEXTURE1_ENV_BLUR_AMOUNT("u_texture1EnvBlurAmount",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        TEXTURE2_ENV_BLUR_AMOUNT("u_texture2EnvBlurAmount",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        RESCALE_NORMAL_FACTOR("u_rescaleNormalFactor",UNIFORM_TYPE.FLOAT, new float[] {1.0f}, false),
        LIGHT0_AMBIENT("light[0].ambient",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 1.0f}, false),
        LIGHT1_AMBIENT("light[1].ambient",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 1.0f}, false),
        LIGHT2_AMBIENT("light[2].ambient",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 1.0f}, false),
        LIGHT3_AMBIENT("light[3].ambient",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 1.0f}, false),
        LIGHT4_AMBIENT("light[4].ambient",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 1.0f}, false),
        LIGHT5_AMBIENT("light[5].ambient",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 1.0f}, false),
        LIGHT6_AMBIENT("light[6].ambient",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 1.0f}, false),

        LIGHT0_DIFFUSE("light[0].diffuse",UNIFORM_TYPE.FLOAT, new float[] {1.0f, 1.0f, 1.0f, 1.0f}, false),
        LIGHT1_DIFFUSE("light[1].diffuse",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        LIGHT2_DIFFUSE("light[2].diffuse",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        LIGHT3_DIFFUSE("light[3].diffuse",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        LIGHT4_DIFFUSE("light[4].diffuse",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        LIGHT5_DIFFUSE("light[5].diffuse",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        LIGHT6_DIFFUSE("light[6].diffuse",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        LIGHT7_DIFFUSE("light[7].diffuse",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),

        LIGHT0_SPECULAR("light[0].specular",UNIFORM_TYPE.FLOAT, new float[] {1.0f, 1.0f, 1.0f, 1.0f}, false),
        LIGHT1_SPECULAR("light[1].specular",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        LIGHT2_SPECULAR("light[2].specular",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        LIGHT3_SPECULAR("light[3].specular",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        LIGHT4_SPECULAR("light[4].specular",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        LIGHT5_SPECULAR("light[5].specular",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        LIGHT6_SPECULAR("light[6].specular",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        LIGHT7_SPECULAR("light[7].specular",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),

        LIGHT0_POSITION("light[0].position",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 1.0f, 0.0f}, false),
        LIGHT1_POSITION("light[1].position",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 1.0f, 0.0f}, false),
        LIGHT2_POSITION("light[2].position",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 1.0f, 0.0f}, false),
        LIGHT3_POSITION("light[3].position",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 1.0f, 0.0f}, false),
        LIGHT4_POSITION("light[4].position",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 1.0f, 0.0f}, false),
        LIGHT5_POSITION("light[5].position",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 1.0f, 0.0f}, false),
        LIGHT6_POSITION("light[6].position",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 1.0f, 0.0f}, false),
        LIGHT7_POSITION("light[7].position",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 1.0f, 0.0f}, false),

        LIGHT0_SPOT_DIRECTION("light[0].spotDirection",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, -1.0f}, false),
        LIGHT1_SPOT_DIRECTION("light[1].spotDirection",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, -1.0f}, false),
        LIGHT2_SPOT_DIRECTION("light[2].spotDirection",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, -1.0f}, false),
        LIGHT3_SPOT_DIRECTION("light[3].spotDirection",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, -1.0f}, false),
        LIGHT4_SPOT_DIRECTION("light[4].spotDirection",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, -1.0f}, false),
        LIGHT5_SPOT_DIRECTION("light[5].spotDirection",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, -1.0f}, false),
        LIGHT6_SPOT_DIRECTION("light[6].spotDirection",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, -1.0f}, false),
        LIGHT7_SPOT_DIRECTION("light[7].spotDirection",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, -1.0f}, false),

        LIGHT0_SPOT_EXPONENT("light[0].spotExponent",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT1_SPOT_EXPONENT("light[1].spotExponent",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT2_SPOT_EXPONENT("light[2].spotExponent",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT3_SPOT_EXPONENT("light[3].spotExponent",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT4_SPOT_EXPONENT("light[4].spotExponent",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT5_SPOT_EXPONENT("light[5].spotExponent",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT6_SPOT_EXPONENT("light[6].spotExponent",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT7_SPOT_EXPONENT("light[7].spotExponent",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),

        LIGHT0_SPOT_CUTOFF_ANGLE_COS("light[0].spotCutoffAngleCos",UNIFORM_TYPE.FLOAT, new float[] {-1.0f}, false),
        LIGHT1_SPOT_CUTOFF_ANGLE_COS("light[1].spotCutoffAngleCos",UNIFORM_TYPE.FLOAT, new float[] {-1.0f}, false),
        LIGHT2_SPOT_CUTOFF_ANGLE_COS("light[2].spotCutoffAngleCos",UNIFORM_TYPE.FLOAT, new float[] {-1.0f}, false),
        LIGHT3_SPOT_CUTOFF_ANGLE_COS("light[3].spotCutoffAngleCos",UNIFORM_TYPE.FLOAT, new float[] {-1.0f}, false),
        LIGHT4_SPOT_CUTOFF_ANGLE_COS("light[4].spotCutoffAngleCos",UNIFORM_TYPE.FLOAT, new float[] {-1.0f}, false),
        LIGHT5_SPOT_CUTOFF_ANGLE_COS("light[5].spotCutoffAngleCos",UNIFORM_TYPE.FLOAT, new float[] {-1.0f}, false),
        LIGHT6_SPOT_CUTOFF_ANGLE_COS("light[6].spotCutoffAngleCos",UNIFORM_TYPE.FLOAT, new float[] {-1.0f}, false),
        LIGHT7_SPOT_CUTOFF_ANGLE_COS("light[7].spotCutoffAngleCos",UNIFORM_TYPE.FLOAT, new float[] {-1.0f}, false),

        LIGHT0_CONSTANT_ATTENUATION("light[0].constantAttenuation",UNIFORM_TYPE.FLOAT, new float[] {1.0f}, false),
        LIGHT1_CONSTANT_ATTENUATION("light[1].constantAttenuation",UNIFORM_TYPE.FLOAT, new float[] {1.0f}, false),
        LIGHT2_CONSTANT_ATTENUATION("light[2].constantAttenuation",UNIFORM_TYPE.FLOAT, new float[] {1.0f}, false),
        LIGHT3_CONSTANT_ATTENUATION("light[3].constantAttenuation",UNIFORM_TYPE.FLOAT, new float[] {1.0f}, false),
        LIGHT4_CONSTANT_ATTENUATION("light[4].constantAttenuation",UNIFORM_TYPE.FLOAT, new float[] {1.0f}, false),
        LIGHT5_CONSTANT_ATTENUATION("light[5].constantAttenuation",UNIFORM_TYPE.FLOAT, new float[] {1.0f}, false),
        LIGHT6_CONSTANT_ATTENUATION("light[6].constantAttenuation",UNIFORM_TYPE.FLOAT, new float[] {1.0f}, false),
        LIGHT7_CONSTANT_ATTENUATION("light[7].constantAttenuation",UNIFORM_TYPE.FLOAT, new float[] {1.0f}, false),

        LIGHT0_LINEAR_ATTENUATION("light[0].linearAttenuation",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT1_LINEAR_ATTENUATION("light[1].linearAttenuation",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT2_LINEAR_ATTENUATION("light[2].linearAttenuation",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT3_LINEAR_ATTENUATION("light[3].linearAttenuation",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT4_LINEAR_ATTENUATION("light[4].linearAttenuation",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT5_LINEAR_ATTENUATION("light[5].linearAttenuation",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT6_LINEAR_ATTENUATION("light[6].linearAttenuation",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT7_LINEAR_ATTENUATION("light[7].linearAttenuation",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),

        LIGHT0_QUADRATIC_ATTENUATION("light[0].quadraticAttenuation",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT1_QUADRATIC_ATTENUATION("light[1].quadraticAttenuation",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT2_QUADRATIC_ATTENUATION("light[2].quadraticAttenuation",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT3_QUADRATIC_ATTENUATION("light[3].quadraticAttenuation",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT4_QUADRATIC_ATTENUATION("light[4].quadraticAttenuation",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT5_QUADRATIC_ATTENUATION("light[5].quadraticAttenuation",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT6_QUADRATIC_ATTENUATION("light[6].quadraticAttenuation",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        LIGHT7_QUADRATIC_ATTENUATION("light[7].quadraticAttenuation",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),

        MATERIAL_COLOR_ENABLE("u_material_color_enable"),
        MATERIAL_AMBIENT("u_material.ambient",UNIFORM_TYPE.FLOAT, new float[] {0.2f, 0.2f, 0.2f, 1.0f}, false),
        MATERIAL_DIFFUSE("u_material.diffuse",UNIFORM_TYPE.FLOAT, new float[] {0.8f, 0.8f, 0.8f, 1.0f}, false),
        MATERIAL_SPECULAR("u_material.specular",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 1.0f}, false),
        MATERIAL_EMISSION("u_material.emission",UNIFORM_TYPE.FLOAT, new float[] {0.2f, 0.2f, 0.2f, 1.0f}, false),
        MATERIAL_SHININESS("u_material.shininess",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        FOG_COLOR("u_fogColor",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f}, false),
        FOG_DENSITY("u_fogDensity",UNIFORM_TYPE.FLOAT, new float[] {1.0f}, false),
        FOG_START("u_fogStart",UNIFORM_TYPE.FLOAT, new float[] {1.0f}, false),
        FOG_END("u_fogEnd",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        ALPHA_FUNC_VALUE("u_alphaFuncValue",UNIFORM_TYPE.FLOAT, new float[] {0.0f}, false),
        CLIP_PLANE0_EQUATION("u_clipPlane0Equation",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        CLIP_PLANE1_EQUATION("u_clipPlane1Equation",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        CLIP_PLANE2_EQUATION("u_clipPlane2Equation",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        CLIP_PLANE3_EQUATION("u_clipPlane3Equation",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        CLIP_PLANE4_EQUATION("u_clipPlane4Equation",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        CLIP_PLANE5_EQUATION("u_clipPlane5Equation",UNIFORM_TYPE.FLOAT, new float[] {0.0f, 0.0f, 0.0f, 0.0f}, false),
        GLOBAL_AMBIENT_COLOR("u_globalAmbientColor",UNIFORM_TYPE.FLOAT, new float[] {0.2f, 0.2f, 0.2f, 1.0f}, false);

        private String mName = null;
        private UNIFORM_TYPE mType = null;
        private float mDefaultValueFloatData[] = null;
        private boolean mDefaultBooleanData = false;

        UNIFORM_LIST_ID (String name, UNIFORM_TYPE type, float[] floatData, boolean boolData) {
            mName = name;
            mType = type;
            switch (mType) {
                case FLOAT: mDefaultValueFloatData = floatData;
                            break;
                case BOOL: mDefaultBooleanData = boolData;
                            break;
            }
        }

        UNIFORM_LIST_ID (String uniformName) {
            mName = uniformName;
        }

        public String getName() {
            return mName;
        }

        public UNIFORM_TYPE getType() {
            return mType;
        }

        public float[] getDefaultFloatData() {
            return mDefaultValueFloatData;
        }

        public boolean getDefaultBoolData() {
            return mDefaultBooleanData;
        }
    }

    public static enum UNIFORM_TYPE
    {
        BOOL,FLOAT;
    }

    public Uniforms(String name, int uniformLocation, float[] data) {
        mName = name;
        mUniformLocation = uniformLocation;
        mData = data;
    }
}
