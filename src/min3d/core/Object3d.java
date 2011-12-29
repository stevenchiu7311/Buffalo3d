package min3d.core;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.MotionEvent;

import min3d.Shared;
import min3d.interfaces.IObject3dContainer;
import min3d.listeners.OnClickListener;
import min3d.listeners.OnLongClickListener;
import min3d.listeners.OnTouchListener;
import min3d.materials.AMaterial;
import min3d.materials.SimpleMaterial;
import min3d.vos.CameraVo;
import min3d.vos.Color4;
import min3d.vos.FrustumManaged;
import min3d.vos.Number3d;
import min3d.vos.Ray;
import min3d.vos.RenderType;
import min3d.vos.ShadeModel;

/**
 * @author Lee
 */
public class Object3d
{
    private final static String TAG = "Object3d";
    private final static boolean DEBUG = false;
	private String _name;
	
	private RenderType _renderType = RenderType.TRIANGLES;
	
    public final static Number3d UNIT_X = new Number3d(1, 0, 0);
    public final static Number3d UNIT_Y = new Number3d(0, 1, 0);
    public final static Number3d UNIT_Z = new Number3d(0, 0, 1);
    public final static Number3d UNIT_XYZ = new Number3d(1, 1, 1);

    public final static int TRANSLATE = 0x1;
    public final static int ROTATE = 0x2;
    public final static int SCALE = 0x4;

    private static final int PRESSED_STATE_DURATION = 125;
    private static final int DEFAULT_LONG_PRESS_TIMEOUT = 500;

    /**
     * Defines the duration in milliseconds we will wait to see if a touch event
     * is a tap or a scroll. If the user does not move within this interval, it is
     * considered to be a tap.
     */
    private static final int TAP_TIMEOUT = 180;

    private static final int PRESSED = 0x00004000;

    /**
     * This view is enabled. Intrepretation varies by subclass.
     * Use with ENABLED_MASK when calling setFlags.
     * {@hide}
     */
    static final int ENABLED = 0x00000000;

    /**
     * This view is disabled. Intrepretation varies by subclass.
     * Use with ENABLED_MASK when calling setFlags.
     * {@hide}
     */
    static final int DISABLED = 0x00000020;

   /**
    * Mask for use with setFlags indicating bits used for indicating whether
    * this view is enabled
    * {@hide}
    */
    static final int ENABLED_MASK = 0x00000020;

    /**
     * <p>Indicates this view can be clicked. When clickable, a View reacts
     * to clicks by notifying the OnClickListener.<p>
     * {@hide}
     */
    static final int CLICKABLE = 0x00004000;

    /**
     * <p>
     * Indicates this view can be long clicked. When long clickable, a View
     * reacts to long clicks by notifying the OnLongClickListener or showing a
     * context menu.
     * </p>
     * {@hide}
     */
    static final int LONG_CLICKABLE = 0x00200000;

    /**
     * Indicates that the view should filter touches when its window is obscured.
     * Refer to the class comments for more information about this security feature.
     * {@hide}
     */
    static final int FILTER_TOUCHES_WHEN_OBSCURED = 0x00000400;

    /**
     * Indicates a prepressed state;
     * the short time between ACTION_DOWN and recognizing
     * a 'real' press. Prepressed is used to recognize quick taps
     * even when they are shorter than ViewConfiguration.getTapTimeout().
     *
     * @hide
     */
    private static final int PREPRESSED = 0x02000000;

	private boolean _isVisible = true;
	private boolean _vertexColorsEnabled = true;
	private boolean _doubleSidedEnabled = false;
	private boolean _texturesEnabled = true;
	private boolean _normalsEnabled = true;
	private boolean _ignoreFaces = false;
	private boolean _colorMaterialEnabled = false;
	private boolean _lightingEnabled = true;
	private boolean mTransparent = true;
	private boolean mForcedDepth = true;

	private Number3d mPosition = new Number3d(0,0,0);
	private Number3d mRotation = new Number3d(0,0,0);
	private Number3d mScale = new Number3d(1,1,1);

	private Color4 _defaultColor = new Color4();
	
	private ShadeModel _shadeModel = ShadeModel.SMOOTH;
	private float _pointSize = 3f;
	private boolean _pointSmoothing = true;
	private float _lineWidth = 1f;
	private boolean _lineSmoothing = false;

	
	protected ArrayList<Object3d> _children;
	
	protected Vertices _vertices; 
	protected TextureList _textures;
	protected FacesBufferedList _faces;

	protected boolean _animationEnabled = false;
	
	private Scene _scene;
	private IObject3dContainer _parent;

    private Number3d mCenter = new Number3d();
    private Number3d mExtent = new Number3d();

    private float[] mRotMC = new float[16];
    private float[] mTransMC = new float[16];
    private float[] mScaleMC = new float[16];

    private float[] mRotMExt = new float[16];
    private float[] mScaleMExt = new float[16];

    private OnTouchListener mOnTouchListener;
    private OnClickListener mOnClickListener;
    private OnLongClickListener mOnLongClickListener;

    private List<Object3d> mDownList = null;
    private List<Object3d> mUpList = null;

    /* Variable for Touch handler */
    UnsetPressedState mUnsetPressedState;
    Handler mHandler = null;
    CheckForLongPress mPendingCheckForLongPress;
    CheckForTap mPendingCheckForTap = null;
    PerformClick mPerformClick;
    int mViewFlags = ENABLED;
    int mPrivateFlags;
    int mWindowAttachCount;
    boolean mHasPerformedLongPress;

    /* Variable for ES 2.0 */
    float[] mMVPMatrix = new float[16];
    float[] mMMatrix = new float[16];
    float[] mProjMatrix;

    float[] mScaleMatrix = new float[16];
    float[] mTranslateMatrix = new float[16];
    float[] mRotateMatrix = new float[16];
    float[] mRotateMatrixTmp = new float[16];
    float[] mTmpMatrix = new float[16];

    AMaterial mMaterial;

	/**
	 * Maximum number of vertices and faces must be specified at instantiation.
	 */
	public Object3d(int $maxVertices, int $maxFaces)
	{
		_vertices = new Vertices($maxVertices, true,true,true);
		_faces = new FacesBufferedList($maxFaces);
		_textures = new TextureList();
	}
	
	/**
	 * Adds three arguments 
	 */
	public Object3d(int $maxVertices, int $maxFaces, Boolean $useUvs, Boolean $useNormals, Boolean $useVertexColors)
	{
		_vertices = new Vertices($maxVertices, $useUvs,$useNormals,$useVertexColors);
		_faces = new FacesBufferedList($maxFaces);
		_textures = new TextureList();
	}
	
	/**
	 * This constructor is convenient for cloning purposes 
	 */
	public Object3d(Vertices $vertices, FacesBufferedList $faces, TextureList $textures)
	{
		_vertices = $vertices;
		_faces = $faces;
		_textures = $textures;
	}
	
	/**
	 * Holds references to vertex position list, vertex u/v mappings list, vertex normals list, and vertex colors list
	 */
	public Vertices vertices()
	{
		return _vertices;
	}

	/**
	 * List of object's faces (ie, index buffer) 
	 */
	public FacesBufferedList faces()
	{
		return _faces;
	}
	
	public TextureList textures()
	{
		return _textures;
	}
	
	/**
	 * Determines if object will be rendered.
	 * Default is true. 
	 */
	public boolean isVisible()
	{
		return _isVisible;
	}
	public void isVisible(Boolean $b)
	{
		_isVisible = $b;
	}
	
	/**
	 * Determines if backfaces will be rendered (ie, doublesided = true).
	 * Default is false.
	 */
	public boolean doubleSidedEnabled()
	{
		return _doubleSidedEnabled;
	}
	public void doubleSidedEnabled(boolean $b)
	{
		_doubleSidedEnabled = $b;
	}
	
	/**
	 * Determines if object uses GL_COLOR_MATERIAL or not.
	 * Default is false.
	 */
	public boolean colorMaterialEnabled()
	{
		return _colorMaterialEnabled;
	}
	
	public boolean lightingEnabled() {
		return _lightingEnabled;
	}

	public void lightingEnabled(boolean $b) {
		this._lightingEnabled = $b;
	}

	public void colorMaterialEnabled(boolean $b)
	{
		_colorMaterialEnabled = $b;
	}

	/**
	 * Determines whether animation is enabled or not. If it is enabled
	 * then this should be an AnimationObject3d instance.
	 * This is part of the Object3d class so there's no need to cast
	 * anything during the render loop when it's not necessary.
	 */
	public boolean animationEnabled()
	{
		return _animationEnabled;
	}
	public void animationEnabled(boolean $b)
	{
		_animationEnabled = $b;
	}
	/**
	 * Determines if per-vertex colors will be using for rendering object.
	 * If false, defaultColor property will dictate object color.
	 * If object has no per-vertex color information, setting is ignored.
	 * Default is true. 
	 */
	public boolean vertexColorsEnabled()
	{
		return _vertexColorsEnabled;
	}
	public void vertexColorsEnabled(Boolean $b)
	{
		_vertexColorsEnabled = $b;
	}

	/**
	 * Determines if textures (if any) will used for rendering object.
	 * Default is true.  
	 */
	public boolean texturesEnabled()
	{
		return _texturesEnabled;
	}
	public void texturesEnabled(Boolean $b)
	{
		_texturesEnabled = $b;
	}
	
	/**
	 * Determines if object will be rendered using vertex light normals.
	 * If false, no lighting is used on object for rendering.
	 * Default is true.
	 */
	public boolean normalsEnabled()
	{
		return _normalsEnabled;
	}
	public void normalsEnabled(boolean $b)
	{
		_normalsEnabled = $b;
	}

	/**
	 * When true, Renderer draws using vertex points list, rather than faces list.
	 * (ie, using glDrawArrays instead of glDrawElements) 
	 * Default is false.
	 */
	public boolean ignoreFaces()
	{
		return _ignoreFaces;
	}
	public void ignoreFaces(boolean $b)
	{
		_ignoreFaces = $b;
	}	
	
	/**
	 * Options are: TRIANGLES, LINES, and POINTS
	 * Default is TRIANGLES.
	 */
	public RenderType renderType()
	{
		return _renderType;
	}
	public void renderType(RenderType $type)
	{
		_renderType = $type;
	}
	
	/**
	 * Possible values are ShadeModel.SMOOTH and ShadeModel.FLAT.
	 * Default is ShadeModel.SMOOTH.
	 * @return
	 */
	public ShadeModel shadeModel()
	{
		return _shadeModel;
	}
	public void shadeModel(ShadeModel $shadeModel)
	{
		_shadeModel = $shadeModel;
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public Number3dBufferList points()
	{
		return _vertices.points();
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public UvBufferList uvs()
	{
		return _vertices.uvs();
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public Number3dBufferList normals()
	{
		return _vertices.normals();
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public Color4BufferList colors()
	{
		return _vertices.colors();
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public boolean hasUvs()
	{
		return _vertices.hasUvs();
	}

	/**
	 * Convenience 'pass-thru' method  
	 */
	public boolean hasNormals()
	{
		return _vertices.hasNormals();
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public boolean hasVertexColors()
	{
		return _vertices.hasColors();
	}


	/**
	 * Clear object for garbage collection.
	 */
	public void clear()
	{
		if (this.vertices().points() != null) 	this.vertices().points().clear();
		if (this.vertices().uvs() != null) 		this.vertices().uvs().clear();
		if (this.vertices().normals() != null) 	this.vertices().normals().clear();
		if (this.vertices().colors() != null) 	this.vertices().colors().clear();
		if (_textures != null) 					_textures.clear();
		
		if (this.parent() != null) 				this.parent().removeChild(this);
	}

	//

	/**
	 * Color used to render object, but only when colorsEnabled is false.
	 */
	public Color4 defaultColor()
	{
		return _defaultColor;
	}
	
	public void defaultColor(Color4 color) {
		_defaultColor = color;
	}

	/**
	 * X/Y/Z position of object. 
	 */
	public Number3d position()
	{
		return mPosition;
	}
	
	/**
	 * X/Y/Z euler rotation of object, using Euler angles.
	 * Units should be in degrees, to match OpenGL usage. 
	 */
	public Number3d rotation()
	{
		return mRotation;
	}

	/**
	 * X/Y/Z scale of object.
	 */
	public Number3d scale()
	{
		return mScale;
	}
	
	/**
	 * Point size (applicable when renderType is POINT)
	 * Default is 3. 
	 */
	public float pointSize()
	{
		return _pointSize; 
	}
	public void pointSize(float $n)
	{
		_pointSize = $n;
	}

	/**
	 * Point smoothing (anti-aliasing), applicable when renderType is POINT.
	 * When true, points look like circles rather than squares.
	 * Default is true.
	 */
	public boolean pointSmoothing()
	{
		return _pointSmoothing;
	}
	public void pointSmoothing(boolean $b)
	{
		_pointSmoothing = $b;
	}

	/**
	 * Line width (applicable when renderType is LINE)
	 * Default is 1. 
	 * 
	 * Remember that maximum line width is OpenGL-implementation specific, and varies depending 
	 * on whether lineSmoothing is enabled or not. Eg, on Nexus One,  lineWidth can range from
	 * 1 to 8 without smoothing, and can only be 1f with smoothing. 
	 */
	public float lineWidth()
	{
		return _lineWidth;
	}
	public void lineWidth(float $n)
	{
		_lineWidth = $n;
	}
	
	/**
	 * Line smoothing (anti-aliasing), applicable when renderType is LINE
	 * Default is false.
	 */
	public boolean lineSmoothing()
	{
		return _lineSmoothing;
	}
	public void lineSmoothing(boolean $b)
	{
		_lineSmoothing = $b;
	}
	
	/**
	 * Convenience property 
	 */
	public String name()
	{
		return _name;
	}
	public void name(String $s)
	{
		_name = $s;
	}
	
	public IObject3dContainer parent()
	{
		return _parent;
	}
	
	//
	
	void parent(IObject3dContainer $container) /*package-private*/
	{
		_parent = $container;
	}
	
	/**
	 * Called by Scene
	 */
	void scene(Scene $scene) /*package-private*/
	{
		_scene = $scene;
	}
	/**
	 * Called by DisplayObjectContainer
	 */
	Scene scene() /*package-private*/
	{
		return _scene;
	}
	
	/**
	 * Can be overridden to create custom draw routines on a per-object basis, 
	 * rather than using Renderer's built-in draw routine. 
	 * 
	 * If overridden, return true instead of false.
	 */
	public Boolean customRenderer(GL10 gl)
	{
		return false;
	}
	
	public Object3d clone()
	{
		Vertices v = _vertices.clone();
		FacesBufferedList f = _faces.clone();
			
		Object3d clone = new Object3d(v, f, _textures);
		
		clone.position().x = position().x;
		clone.position().y = position().y;
		clone.position().z = position().z;
		
		clone.rotation().x = rotation().x;
		clone.rotation().y = rotation().y;
		clone.rotation().z = rotation().z;
		
		clone.scale().x = scale().x;
		clone.scale().y = scale().y;
		clone.scale().z = scale().z;
		
		return clone;
	}

    public Number3d center() {
        return mCenter;
    }

    public boolean transparentEnabled() {
        return mTransparent;
    }

    public void transparentEnabled(Boolean transparent) {
        mTransparent = transparent;
    }

    public boolean depthEnabled() {
        return mForcedDepth;
    }

    public void forceDepthEnabled(Boolean forceDepth) {
        mForcedDepth = forceDepth;
    }

    public void render(CameraVo camera, float[] projMatrix, float[] vMatrix) {
        render(camera, projMatrix, vMatrix, null);
    }

    public void render(CameraVo camera, float[] projMatrix, float[] vMatrix, final float[] parentMatrix) {
        if (isVisible() == false) return;

        if (mMaterial == null) {
            mMaterial = new SimpleMaterial();
        }
        if(true) {
            mProjMatrix = projMatrix;
            if (doubleSidedEnabled()) {
                GLES20.glDisable(GL10.GL_CULL_FACE);
            } else {
                GLES20.glEnable(GL10.GL_CULL_FACE);
            }

            if(mTransparent) {
                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            }

            if (mForcedDepth) {
                GLES20.glEnable(GL10.GL_DEPTH_TEST);
                GLES20.glClearDepthf(1.0f);
                GLES20.glDepthFunc(GL10.GL_LESS);
                GLES20.glDepthRangef(0, 1f);
                GLES20.glDepthMask(true);
            }

            mMaterial.useProgram();
            mMaterial.bindTextures(this);
            mMaterial.setCamera(camera);
            mMaterial.setVertices(_vertices.points().buffer());
            if (_vertices.uvs() != null) {
                mMaterial.setTextureCoords(_vertices.uvs().buffer());
            }
            if (hasVertexColors() && vertexColorsEnabled() && _vertices.colors() != null) {
                mMaterial.setColors(_vertices.colors().buffer());
            } else {
                mMaterial.setColors(defaultColor());
            }
            if (hasNormals() && normalsEnabled() && _vertices.normals() != null) {
                mMaterial.setNormals(_vertices.normals().buffer());
            }
            setShaderParams();
        }

        //doTransformations();

        Matrix.setIdentityM(mMMatrix, 0);

        Matrix.setIdentityM(mTranslateMatrix, 0);
        Matrix.translateM(mTranslateMatrix, 0, mPosition.x, mPosition.y, mPosition.z);

        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.scaleM(mScaleMatrix, 0, mScale.x, mScale.y, mScale.z);

        Matrix.setIdentityM(mRotateMatrix, 0);

        rotateM(mRotateMatrix, 0, mRotation.x, 1.0f, 0.0f, 0.0f);
        rotateM(mRotateMatrix, 0, mRotation.y, 0.0f, 1.0f, 0.0f);
        rotateM(mRotateMatrix, 0, mRotation.z, 0.0f, 0.0f, 1.0f);

        System.arraycopy(mTranslateMatrix, 0, mMMatrix, 0, 16);
        Matrix.setIdentityM(mTmpMatrix, 0);
        Matrix.multiplyMM(mTmpMatrix, 0, mMMatrix, 0, mScaleMatrix, 0);
        Matrix.multiplyMM(mMMatrix, 0, mTmpMatrix, 0, mRotateMatrix, 0);
        if(parentMatrix != null)
        {
            Matrix.multiplyMM(mTmpMatrix, 0, parentMatrix, 0, mMMatrix, 0);
            System.arraycopy(mTmpMatrix, 0, mMMatrix, 0, 16);
        }
        Matrix.multiplyMM(mMVPMatrix, 0, vMatrix, 0, mMMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, projMatrix, 0, mMVPMatrix, 0);

        if(true) {
            mMaterial.setMVPMatrix(mMVPMatrix);
            mMaterial.setModelMatrix(mMMatrix);
            mMaterial.setViewMatrix(vMatrix);

            vertices().points().buffer().position(0);
            if (!ignoreFaces()) {
                int pos, len;

                if (! faces().renderSubsetEnabled()) {
                    pos = 0;
                    len = faces().size();
                }
                else {
                    pos = faces().renderSubsetStartIndex() * FacesBufferedList.PROPERTIES_PER_ELEMENT;
                    len = faces().renderSubsetLength();
                }

                faces().buffer().position(pos);

                GLES20.glDrawElements(
                        renderType().glValue(),
                        len * FacesBufferedList.PROPERTIES_PER_ELEMENT,
                        GLES20.GL_UNSIGNED_SHORT,
                        faces().buffer());
                GLES20.glDisable(GLES20.GL_BLEND);
                GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            } else {
                GLES20.glDrawArrays(renderType().glValue(), 0, vertices().size());
            }
        }

        if (this instanceof Object3dContainer)
        {
            Object3dContainer container = (Object3dContainer)this;

            for (int i = 0; i < container.children().size(); i++)
            {
                Object3d o = container.children().get(i);
                o.render(camera, projMatrix, vMatrix, mMMatrix);
            }
        }
    }

    protected void rotateM(float[] m, int mOffset, float a, float x, float y, float z) {
        Matrix.setIdentityM(mRotateMatrixTmp, 0);
        Matrix.setRotateM(mRotateMatrixTmp, 0, a, x, y, z);
        System.arraycopy(m, 0, mTmpMatrix, 0, 16);
        Matrix.multiplyMM(m, mOffset, mTmpMatrix, mOffset, mRotateMatrixTmp, 0);
    }

    protected void setShaderParams() {
        //mMaterial.setLight(mLight);
    };

    public void containAABB(FloatBuffer[] points) {
        Number3d compVect = new Number3d();
        if (points == null || points.length == 0) {
            Log.i(TAG, "contain nothing, return!!!");
            return;
        }

        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        float maxZ = Float.MIN_VALUE;
        for (int idx = 0; idx < points.length; idx++) {
            points[idx].rewind();
            if (points[idx] == null || points[idx].remaining() <= 2) {
                continue;
            }
            for (int i = 0, len = points[idx].remaining() / 3; i < len; i++) {
                populateFromBuffer(compVect, points[idx], i);

                if (compVect.x < minX)
                    minX = compVect.x;
                else if (compVect.x > maxX) {
                    maxX = compVect.x;
                }

                if (compVect.y < minY)
                    minY = compVect.y;
                else if (compVect.y > maxY) {
                    maxY = compVect.y;
                }

                if (compVect.z < minZ)
                    minZ = compVect.z;
                else if (compVect.z > maxZ) {
                    maxZ = compVect.z;
                }
            }
        }

        mCenter.x = (minX + maxX) / 2;
        mCenter.y = (minY + maxY) / 2;
        mCenter.z = (minZ + maxZ) / 2;

        mExtent.x = maxX - mCenter.x;
        mExtent.y = maxY - mCenter.y;
        mExtent.z = maxZ - mCenter.z;

        calcAABBPos();
    }

    private static void populateFromBuffer(Number3d vector, FloatBuffer buf,
            int index) {
        vector.x = buf.get(index * 3);
        vector.y = buf.get(index * 3 + 1);
        vector.z = buf.get(index * 3 + 2);
    }

    private void calcAABBPos() {
        Matrix.setIdentityM(mTransMC, 0);
        Matrix.translateM(mTransMC, 0, mPosition.x, mPosition.y, mPosition.z);

        Matrix.setIdentityM(mRotMC, 0);

        Matrix.rotateM(mRotMC, 0, mRotation.x, 1, 0, 0);
        Matrix.rotateM(mRotMC, 0, mRotation.y, 0, 1, 0);
        Matrix.rotateM(mRotMC, 0, mRotation.z, 0, 0, 1);

        Matrix.setIdentityM(mScaleMC, 0);
        Matrix.scaleM(mScaleMC, 0, mScale.x, mScale.y, mScale.z);

        float[] result = new float[4];
        result[0] = mCenter.x;
        result[1] = mCenter.y;
        result[2] = mCenter.z;
        result[3] = 1;

        Matrix.multiplyMV(result, 0, mTransMC, 0, result, 0);

        if (_parent != null && _parent instanceof Object3d) {
            calcAABBPos((Object3d) parent(), TRANSLATE | ROTATE | SCALE, result);
        }

        mCenter.x = result[0];
        mCenter.y = result[1];
        mCenter.z = result[2];

        result[0] = mExtent.x;
        result[1] = mExtent.y;
        result[2] = mExtent.z;
        result[3] = 1;

        Number3d accmlR = new Number3d(mRotation.x, mRotation.y, mRotation.z);
        Number3d accmlS = new Number3d(mScale.x, mScale.y, mScale.z);

        if (_parent != null && _parent instanceof Object3d) {
            accmlAABBTrans((Object3d) parent(), ROTATE, accmlR);
            accmlAABBTrans((Object3d) parent(), SCALE, accmlS);
        }

        Matrix.setIdentityM(mRotMExt, 0);

        Matrix.rotateM(mRotMExt, 0, accmlR.x, 1, 0, 0);
        Matrix.rotateM(mRotMExt, 0, accmlR.y, 0, 1, 0);
        Matrix.rotateM(mRotMExt, 0, accmlR.z, 0, 0, 1);

        float[] absRotMExt = new float[16];
        for (int i = 0; i < mRotMExt.length; i++) {
            absRotMExt[i] = Math.abs(mRotMExt[i]);
        }

        Matrix.setIdentityM(mScaleMExt, 0);
        Matrix.scaleM(mScaleMExt, 0, accmlS.x, accmlS.y, accmlS.z);

        Matrix.multiplyMV(result, 0, absRotMExt, 0, result, 0);
        Matrix.multiplyMV(result, 0, mScaleMExt, 0, result, 0);

        mExtent.x = result[0];
        mExtent.y = result[1];
        mExtent.z = result[2];

        if (DEBUG) Log.i(TAG, "Name:" + _name + " Center:" + mCenter + " Extent:"
                + mExtent);
    }

    private void calcAABBPos(Object3d parent, int mode, float[] result) {
        if ((mode & SCALE) != 0) {
            Matrix.multiplyMV(result, 0, parent.mScaleMC, 0, result, 0);
        }
        if ((mode & ROTATE) != 0) {
            Matrix.multiplyMV(result, 0, parent.mRotMC, 0, result, 0);
        }
        if ((mode & TRANSLATE) != 0) {
            Matrix.multiplyMV(result, 0, parent.mTransMC, 0, result, 0);
        }
        if (parent != null && parent.parent() instanceof Object3d) {
            calcAABBPos((Object3d) parent.parent(), mode, result);
        }
    }

    private void accmlAABBTrans(Object3d parent, int mode, Number3d result) {
        if (mode == SCALE) {
            Number3d multiply = Number3d.multiply(result,parent.scale());
            result.setAllFrom(multiply);
        }
        if (mode == ROTATE) {
            result.add(parent.rotation());
        }
        if (mode == TRANSLATE) {
            result.add(parent.position());
        }
        if (parent != null && parent.parent() instanceof Object3d) {
            accmlAABBTrans((Object3d) parent.parent(), mode, result);
        }
    }

    public boolean intersects(Ray ray) {
        if (!_isVisible) {
            return false;
        }

        float rhs;

        Number3d diff = new Number3d(ray.getPoint().x - mCenter.x,
                ray.getPoint().y - mCenter.y, ray.getPoint().z - mCenter.z);

        final float[] fWdU = new float[3];
        final float[] fAWdU = new float[3];
        final float[] fDdU = new float[3];
        final float[] fADdU = new float[3];
        final float[] fAWxDdU = new float[3];

        fWdU[0] = Number3d.dot(ray.getVector(), UNIT_X);
        fAWdU[0] = Math.abs(fWdU[0]);
        fDdU[0] = Number3d.dot(diff, UNIT_X);
        fADdU[0] = Math.abs(fDdU[0]);
        if (fADdU[0] > mExtent.x && fDdU[0] * fWdU[0] >= 0.0) {
            return false;
        }

        fWdU[1] = Number3d.dot(ray.getVector(), UNIT_Y);
        fAWdU[1] = Math.abs(fWdU[1]);
        fDdU[1] = Number3d.dot(diff, UNIT_Y);
        fADdU[1] = Math.abs(fDdU[1]);
        if (fADdU[1] > mExtent.y && fDdU[1] * fWdU[1] >= 0.0) {
            return false;
        }

        fWdU[2] = Number3d.dot(ray.getVector(), UNIT_Z);
        fAWdU[2] = Math.abs(fWdU[2]);
        fDdU[2] = Number3d.dot(diff, UNIT_Z);
        fADdU[2] = Math.abs(fDdU[2]);
        if (fADdU[2] > mExtent.z && fDdU[2] * fWdU[2] >= 0.0) {
            return false;
        }

        Number3d wCrossD = Number3d.cross(ray.getVector(), diff);

        fAWxDdU[0] = Math.abs(Number3d.dot(wCrossD, UNIT_X));
        rhs = mExtent.y * fAWdU[2] + mExtent.z * fAWdU[1];
        if (fAWxDdU[0] > rhs) {
            return false;
        }

        fAWxDdU[1] = Math.abs(Number3d.dot(wCrossD, UNIT_Y));
        rhs = mExtent.x * fAWdU[2] + mExtent.z * fAWdU[0];
        if (fAWxDdU[1] > rhs) {
            return false;
        }

        fAWxDdU[2] = Math.abs(Number3d.dot(wCrossD, UNIT_Z));
        rhs = mExtent.x * fAWdU[1] + mExtent.y * fAWdU[0];
        if (fAWxDdU[2] > rhs) {
            return false;
        }

        return true;
    }

    public boolean dispatchTouchEvent(Ray ray, MotionEvent event, ArrayList<Object3d> list) {
        if (!onFilterTouchEventForSecurity(event)) {
            return false;
        }

        Number3d coordinates = getIntersectPoint(event.getX(),event.getY(),mCenter.z);
        if (mOnTouchListener != null && (mViewFlags & ENABLED_MASK) == ENABLED &&
                mOnTouchListener.onTouch(this, event, list, coordinates)) {
            return true;
        }
        return onTouchEvent(ray, event, list);
    }

    public boolean onTouchEvent(Ray ray, MotionEvent event, ArrayList<Object3d> list) {
        Number3d coordinates = getIntersectPoint(event.getX(),event.getY(),mCenter.z);
        list = (ArrayList<Object3d>)Shared.renderer().getPickedObject(ray, this);
        final int viewFlags = mViewFlags;

        if ((viewFlags & ENABLED_MASK) == DISABLED) {
            if (event.getAction() == MotionEvent.ACTION_UP && (mPrivateFlags & PRESSED) != 0) {
                mPrivateFlags &= ~PRESSED;
                refreshDrawableState();
            }
            // A disabled view that is clickable still consumes the touch
            // events, it just doesn't respond to them.
            return (((viewFlags & CLICKABLE) == CLICKABLE ||
                    (viewFlags & LONG_CLICKABLE) == LONG_CLICKABLE));
        }

        if (mOnTouchListener != null) {
            if (mOnTouchListener.onTouch(this, event, list, coordinates)) {
                return true;
            }
        }

        if (((viewFlags & CLICKABLE) == CLICKABLE ||
                (viewFlags & LONG_CLICKABLE) == LONG_CLICKABLE)) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    boolean prepressed = (mPrivateFlags & PREPRESSED) != 0;
                    if ((mPrivateFlags & PRESSED) != 0 || prepressed) {
                        if (mDownList != null) {
                            mUpList = (List<Object3d>)list.clone();
                            for (int i = 0; i < mUpList.size(); i++) {
                                boolean intersected = false;
                                for (int j = 0; j < mDownList.size(); j++) {
                                    if (mUpList.contains(mDownList.get(j))) {
                                        intersected = true;
                                    }
                                }
                                if (!intersected) {
                                    mUpList.remove(i);
                                }
                            }
                            mDownList.clear();
                            mDownList = null;
                        }

                        // take focus if we don't have it already and we should in
                        // touch mode.
                        boolean focusTaken = false;
                        /*if (isFocusable() && isFocusableInTouchMode() && !isFocused()) {
                            focusTaken = requestFocus();
                        }*/

                        if (prepressed) {
                            // The button is being released before we actually
                            // showed it as pressed.  Make it show the pressed
                            // state now (before scheduling the click) to ensure
                            // the user sees it.
                            mPrivateFlags |= PRESSED;
                            refreshDrawableState();
                        }

                        if (!mHasPerformedLongPress) {
                            // This is a tap, so remove the longpress check
                            removeLongPressCallback();

                            // Only perform take click actions if we were in the pressed state
                            if (!focusTaken) {
                                // Use a Runnable and post this rather than calling
                                // performClick directly. This lets other visual state
                                // of the view update before click actions start.
                                if (mPerformClick == null) {
                                    mPerformClick = new PerformClick(event, mUpList, coordinates);
                                }

                                if (!post(mPerformClick)) {
                                    performClick(event, mUpList, coordinates);
                                }
                            }
                        }

                        if (mUnsetPressedState == null) {
                            mUnsetPressedState = new UnsetPressedState();
                        }

                        if (prepressed) {
                            postDelayed(mUnsetPressedState,
                                    PRESSED_STATE_DURATION);
                        } else if (!post(mUnsetPressedState)) {
                            // If the post failed, unpress right now
                            mUnsetPressedState.run();
                        }
                        removeTapCallback();
                    }
                    break;

                case MotionEvent.ACTION_DOWN:
                    mDownList = (List<Object3d>)list.clone();

                    mHasPerformedLongPress = false;

                    if (performButtonActionOnTouchDown(event)) {
                        break;
                    }

                    // Walk up the hierarchy to determine if we're inside a scrolling container.
                    boolean isInScrollingContainer = isInScrollingContainer();

                    // For views inside a scrolling container, delay the pressed feedback for
                    // a short period in case this is a scroll.
                    if (isInScrollingContainer) {
                        mPrivateFlags |= PREPRESSED;
                        if (mPendingCheckForTap == null) {
                            mPendingCheckForTap = new CheckForTap();
                        }
                        postDelayed(mPendingCheckForTap, TAP_TIMEOUT);
                    } else {
                        // Not inside a scrolling container, so show the feedback right away
                        mPrivateFlags |= PRESSED;
                        refreshDrawableState();
                        checkForLongClick(0);
                    }
                    break;

                case MotionEvent.ACTION_CANCEL:
                    if (mDownList != null) {
                        mDownList.clear();
                        mDownList = null;
                    }

                    mPrivateFlags &= ~PRESSED;
                    refreshDrawableState();
                    removeTapCallback();
                    break;

                case MotionEvent.ACTION_MOVE:
                    // Be lenient about moving outside of buttons
                    if (list.size() == 0) {
                        // Outside button
                        removeTapCallback();
                        if ((mPrivateFlags & PRESSED) != 0) {
                            // Remove any future long press/tap checks
                            removeLongPressCallback();

                            // Need to switch from pressed to not pressed
                            mPrivateFlags &= ~PRESSED;
                            refreshDrawableState();
                        }
                    }
                    break;
            }
            return true;
        }

        return false;
    }

    public Number3d getIntersectPoint(float x, float y, float z) {
        int w = Shared.renderer().getWidth();
        int h = Shared.renderer().getHeight();
        float[] eye = new float[4];
        int[] viewport = { 0, 0, w, h };
        FrustumManaged vf = _scene.camera().frustum;
        float distance = _scene.camera().position.z + z;
        float winZ = (1.0f / vf.zNear() - 1.0f / distance)
                / (1.0f / vf.zNear() - 1.0f / vf.zFar());
        GLU.gluUnProject(x, w - y ,winZ, Shared.renderer().getMatrixGrabber().mModelView, 0,
                Shared.renderer().getMatrixGrabber().mProjection, 0, viewport, 0, eye, 0);
        if (eye[3] != 0) {
            eye[0] = eye[0] / eye[3];
            eye[1] = eye[1] / eye[3];
            eye[2] = eye[2] / eye[3];
        }
        return new Number3d(eye[0], eye[1], -eye[2]);
    }

    public void setOnTouchListener(OnTouchListener listener) {
        mOnTouchListener = listener;
    }

    public void setOnClickListener(OnClickListener listener) {
        if (!isClickable()) {
            setClickable(true);
        }
        mOnClickListener = listener;
    }

    public void setOnLongClickListener(OnLongClickListener listener) {
        if (!isLongClickable()) {
            setLongClickable(true);
        }
        mOnLongClickListener = listener;
    }

    public boolean isClickable() {
        return (mViewFlags & CLICKABLE) == CLICKABLE;
    }

    public void setClickable(boolean clickable) {
        setFlags(clickable ? CLICKABLE : 0, CLICKABLE);
    }

    public boolean isLongClickable() {
        return (mViewFlags & LONG_CLICKABLE) == LONG_CLICKABLE;
    }

    public void setLongClickable(boolean longClickable) {
        setFlags(longClickable ? LONG_CLICKABLE : 0, LONG_CLICKABLE);
    }

    /**
     * Indicates whether the view is currently in pressed state. Unless
     * {@link #setPressed(boolean)} is explicitly called, only clickable views can enter
     * the pressed state.
     *
     * @see #setPressed(boolean)
     * @see #isClickable()
     * @see #setClickable(boolean)
     *
     * @return true if the view is currently pressed, false otherwise
     */
    public boolean isPressed() {
        return (mPrivateFlags & PRESSED) == PRESSED;
    }

    /**
     * Sets the pressed state for this view.
     *
     * @see #isClickable()
     * @see #setClickable(boolean)
     *
     * @param pressed Pass true to set the View's internal state to "pressed", or false to reverts
     *        the View's internal state from a previously set "pressed" state.
     */
    public void setPressed(boolean pressed) {
        if (pressed) {
            mPrivateFlags |= PRESSED;
        } else {
            mPrivateFlags &= ~PRESSED;
        }
        refreshDrawableState();
        dispatchSetPressed(pressed);
    }

    /**
     * Dispatch setPressed to all of this View's children.
     *
     * @see #setPressed(boolean)
     *
     * @param pressed The new pressed state
     */
    protected void dispatchSetPressed(boolean pressed) {
    }

    private final class PerformClick implements Runnable {
        MotionEvent mEvent;
        List<Object3d> mList;
        Number3d mCoord;

        public PerformClick(MotionEvent event, List<Object3d> list, Number3d coord) {
            mEvent = event;
            mList = list;
            mCoord = coord;
        }

        public void run() {
            performClick(mEvent,mList,mCoord);
        }
    }

    /**
     * Call this view's OnClickListener, if it is defined.
     *
     * @return True there was an assigned OnClickListener that was called, false
     *         otherwise is returned.
     */
    public boolean performClick(MotionEvent event, List<Object3d> list, Number3d coord) {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(this,event,list,coord);
            return true;
        }

        return false;
    }

    public boolean isInScrollingContainer() {
        return false;
    }

    /**
     * @return A handler associated with the thread running the View. This
     * handler can be used to pump events in the UI events queue.
     */
    public Handler getHandler() {
        if (mHandler == null) {
            HandlerThread ht = new HandlerThread("");
            ht.start();
            mHandler = new Handler(ht.getLooper());
        }
        return mHandler;
    }

    /**
     * <p>Causes the Runnable to be added to the message queue.
     * The runnable will be run on the user interface thread.</p>
     *
     * <p>This method can be invoked from outside of the UI thread
     * only when this View is attached to a window.</p>
     *
     * @param action The Runnable that will be executed.
     *
     * @return Returns true if the Runnable was successfully placed in to the
     *         message queue.  Returns false on failure, usually because the
     *         looper processing the message queue is exiting.
     */
    public boolean post(Runnable action) {
        Handler handler = getHandler();
        return handler.post(action);
    }

    /**
     * <p>Causes the Runnable to be added to the message queue, to be run
     * after the specified amount of time elapses.
     * The runnable will be run on the user interface thread.</p>
     *
     * <p>This method can be invoked from outside of the UI thread
     * only when this View is attached to a window.</p>
     *
     * @param action The Runnable that will be executed.
     * @param delayMillis The delay (in milliseconds) until the Runnable
     *        will be executed.
     *
     * @return true if the Runnable was successfully placed in to the
     *         message queue.  Returns false on failure, usually because the
     *         looper processing the message queue is exiting.  Note that a
     *         result of true does not mean the Runnable will be processed --
     *         if the looper is quit before the delivery time of the message
     *         occurs then the message will be dropped.
     */
    public boolean postDelayed(Runnable action, long delayMillis) {
        Handler handler = getHandler();
        return handler.postDelayed(action, delayMillis);
    }

    /**
     * <p>Removes the specified Runnable from the message queue.</p>
     *
     * <p>This method can be invoked from outside of the UI thread
     * only when this View is attached to a window.</p>
     *
     * @param action The Runnable to remove from the message handling queue
     *
     * @return true if this view could ask the Handler to remove the Runnable,
     *         false otherwise. When the returned value is true, the Runnable
     *         may or may not have been actually removed from the message queue
     *         (for instance, if the Runnable was not in the queue already.)
     */
    public boolean removeCallbacks(Runnable action) {
        Handler handler = getHandler();
        handler.removeCallbacks(action);
        return true;
    }

    /**
     * Remove the longpress detection timer.
     */
    private void removeLongPressCallback() {
        if (mPendingCheckForLongPress != null) {
            removeCallbacks(mPendingCheckForLongPress);
        }
    }

    /**
     * Remove the tap detection timer.
     */
    private void removeTapCallback() {
        if (mPendingCheckForTap != null) {
            mPrivateFlags &= ~PREPRESSED;
            removeCallbacks(mPendingCheckForTap);
        }
    }

    private final class UnsetPressedState implements Runnable {
        public void run() {
            setPressed(false);
        }
    }

    class CheckForLongPress implements Runnable {
        private int mOriginalWindowAttachCount;

        public void run() {
            if (isPressed() && (parent() != null)
                    /*&& mOriginalWindowAttachCount == mWindowAttachCount*/) {
                if (performLongClick()) {
                    mHasPerformedLongPress = true;
                }
            }
        }

        public void rememberWindowAttachCount() {
            mOriginalWindowAttachCount = mWindowAttachCount;
        }
    }

    /**
     * Call this view's OnLongClickListener, if it is defined. Invokes the context menu if the
     * OnLongClickListener did not consume the event.
     *
     * @return True if one of the above receivers consumed the event, false otherwise.
     */
    public boolean performLongClick() {
        boolean handled = false;
        if (mOnLongClickListener != null) {
            handled = mOnLongClickListener.onLongClick(this);
        }
        return handled;
    }

    private final class CheckForTap implements Runnable {
        public void run() {
            mPrivateFlags &= ~PREPRESSED;
            mPrivateFlags |= PRESSED;
            refreshDrawableState();
            checkForLongClick(TAP_TIMEOUT);
        }
    }

    private void checkForLongClick(int delayOffset) {
        if ((mViewFlags & LONG_CLICKABLE) == LONG_CLICKABLE) {
            mHasPerformedLongPress = false;

            if (mPendingCheckForLongPress == null) {
                mPendingCheckForLongPress = new CheckForLongPress();
            }
            mPendingCheckForLongPress.rememberWindowAttachCount();
            postDelayed(mPendingCheckForLongPress,
                    DEFAULT_LONG_PRESS_TIMEOUT - delayOffset);
        }
    }

    void setFlags(int flags, int mask) {
        int old = mViewFlags;
        mViewFlags = (mViewFlags & ~mask) | (flags & mask);

        int changed = mViewFlags ^ old;
        if (changed == 0) {
            return;
        }
    }

    public void refreshDrawableState() {
        // TODO: Handler for drawable state refresh.
    }

    /**
     * Performs button-related actions during a touch down event.
     *
     * @param event The event.
     * @return True if the down was consumed.
     *
     * @hide
     */
    protected boolean performButtonActionOnTouchDown(MotionEvent event) {
        // TODO: Button action for touch down.
        return false;
    }

    /**
     * Filter the touch event to apply security policies.
     *
     * @param event The motion event to be filtered.
     * @return True if the event should be dispatched, false if the event should be dropped.
     *
     * @see #getFilterTouchesWhenObscured
     */
    public boolean onFilterTouchEventForSecurity(MotionEvent event) {
        if ((mViewFlags & FILTER_TOUCHES_WHEN_OBSCURED) != 0
                && (event.getFlags() & MotionEvent.FLAG_WINDOW_IS_OBSCURED) != 0) {
            // Window is obscured, drop this touch.
            return false;
        }
        return true;
    }
}
