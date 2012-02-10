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

/**
 * This class represents the basic building block for user interface components. A Object3d
 * occupies a polygon area on the screen and is responsible for drawing and
 * event handling.
 */
public class Object3d
{
    private final static String TAG = "Object3d";
    private final static boolean DEBUG = false;
	private String _name;
	
	private RenderType _renderType = RenderType.TRIANGLES;
	
    private final static Number3d UNIT_X = new Number3d(1, 0, 0);
    private final static Number3d UNIT_Y = new Number3d(0, 1, 0);
    private final static Number3d UNIT_Z = new Number3d(0, 0, 1);
    private final static Number3d UNIT_XYZ = new Number3d(1, 1, 1);

    private final static int TRANSLATE = 0x1;
    private final static int ROTATE = 0x2;
    private final static int SCALE = 0x4;

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

    public enum VBO_ID {POINT,UV,COLOR,NORMAL,FACE,TOTAL};

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
    protected Color4BufferList mColors;
	protected TextureList _textures;
	protected FacesBufferedList _faces;

	protected boolean _animationEnabled = false;
	
	private Scene _scene;
	private IObject3dContainer _parent;

    /* Variable for Touch handler */
    List<Object3d> mDownList = null;
    List<Object3d> mUpList = null;
    UnsetPressedState mUnsetPressedState;
    Handler mHandler = null;
    CheckForLongPress mPendingCheckForLongPress;
    CheckForTap mPendingCheckForTap = null;
    PerformClick mPerformClick;
    int mViewFlags = ENABLED;
    int mPrivateFlags;
    int mWindowAttachCount;
    boolean mHasPerformedLongPress;
    OnTouchListener mOnTouchListener;
    OnClickListener mOnClickListener;
    OnLongClickListener mOnLongClickListener;

    /* For vertex buffer object */
    boolean mVertexBufferObject = true;
    boolean mBuffered = !mVertexBufferObject;
    int mBuffers[] = {0,0,0,0,0};

    /* For Bounding box */
    Number3d mCenter = new Number3d();
    Number3d mExtent = new Number3d();

    float[] mRotMC = new float[16];
    float[] mTransMC = new float[16];
    float[] mScaleMC = new float[16];

    float[] mRotMExt = new float[16];
    float[] mScaleMExt = new float[16];

    float[] mResult = new float[4];
    Number3d mAccmlR = new Number3d();
    Number3d mAccmlS = new Number3d();
    float[] mAbsRotMExt = new float[16];

    final float[] mWdU = new float[3];
    final float[] mAWdU = new float[3];
    final float[] mDdU = new float[3];
    final float[] mADdU = new float[3];
    final float[] mAWxDdU = new float[3];
    Number3d mDirect = new Number3d();
    Number3d mCrossD = new Number3d();

    float mMinX,mMinY,mMinZ;
    float mMaxX,mMaxY,mMaxZ;
    Number3d mCompVect = new Number3d();
    FloatBuffer[] mAabbBuffer = new FloatBuffer[1];
    boolean mObtainAABB = false;

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
     * Indicates if object will be rendered.
     * Default is true.
     *
     * @return true if object will be rendered
     */
	public boolean isVisible()
	{
		return _isVisible;
	}

    /**
     * Determines if object will be rendered.
     * Default is true.
     *
     * @param $b true if object will be rendered
     */
	public void isVisible(Boolean $b)
	{
		_isVisible = $b;
	}
	
    /**
     * Indicates if backfaces will be rendered (ie, doublesided = true).
     * Default is false.
     *
     * @return true if backfaces will be rendered
     */
	public boolean doubleSidedEnabled()
	{
		return _doubleSidedEnabled;
	}

    /**
     * Determines if backfaces will be rendered (ie, doublesided = true).
     * Default is false.
     *
     * @param $b true to apply lighting on object
     */
	public void doubleSidedEnabled(boolean $b)
	{
		_doubleSidedEnabled = $b;
	}

    /**
     * Indicates if object uses GL_COLOR_MATERIAL or not.
     * Default is false.
     *
     * @return true if object uses GL_COLOR_MATERIAL
     */
	public boolean colorMaterialEnabled()
	{
		return _colorMaterialEnabled;
	}
	
    /**
     * Determines if object uses GL_COLOR_MATERIAL or not.
     * Default is false.
     *
     * @param $b true to uses GL_COLOR_MATERIAL on object
     */
    public void colorMaterialEnabled(boolean $b)
    {
        _colorMaterialEnabled = $b;
    }

    /**
     * Indicates if object apply lighting.
     * Default is true.
     *
     * @return true if object apply lighting
     */
	public boolean lightingEnabled() {
		return _lightingEnabled;
	}

    /**
     * Determines if object apply lighting or not.
     * Default is true.
     *
     * @param $b true to apply lighting on object
     */
	public void lightingEnabled(boolean $b) {
		this._lightingEnabled = $b;
	}

    /**
     * Indicates whether animation is enabled or not. If it is enabled then this
     * should be an AnimationObject3d instance. This is part of the Object3d
     * class so there's no need to cast anything during the render loop when
     * it's not necessary.
     *
     * @return true if object animation is enabled
     */
	public boolean animationEnabled()
	{
		return _animationEnabled;
	}

    /**
     * Determines if animation will be enabled. If it is enabled then this
     * should be an AnimationObject3d instance. This is part of the Object3d
     * class so there's no need to cast anything during the render loop when
     * it's not necessary.
     *
     * @param $b true to make object animation enabled
     */
	public void animationEnabled(boolean $b)
	{
		_animationEnabled = $b;
	}

    /**
     * Indicates if per-vertex colors will be using for rendering object. If false,
     * defaultColor property will dictate object color. If object has no
     * per-vertex color information, setting is ignored. Default is true.
     *
     * @return true if per-vertex colors will be using for rendering object
     */
	public boolean vertexColorsEnabled()
	{
		return _vertexColorsEnabled;
	}

    /**
     * Determines if per-vertex colors will be using for rendering object. If
     * false, defaultColor property will dictate object color. If object has no
     * per-vertex color information, setting is ignored. Default is true.
     *
     * @param $b true to make per-vertex colors be used for rendering object
     */
	public void vertexColorsEnabled(Boolean $b)
	{
		_vertexColorsEnabled = $b;
	}

    /**
     * Indicates if textures (if any) will used for rendering object.
     * Default is true.
     *
     * @return true if textures (if any) will used for rendering object
     */
	public boolean texturesEnabled()
	{
		return _texturesEnabled;
	}

    /**
     * Determines if textures (if any) will used for rendering object.
     * Default is true.
     *
     * @param $b true to make textures (if any) be used for rendering object
     */
	public void texturesEnabled(Boolean $b)
	{
		_texturesEnabled = $b;
	}
	
    /**
     * Indicates if object will be rendered using vertex light normals. If false,
     * no lighting is used on object for rendering.
     * Default is true.
     *
     * @return true to make object be rendered using vertex light normals
     */
	public boolean normalsEnabled()
	{
		return _normalsEnabled;
	}

    /**
     * Determines if object will be rendered using vertex light normals. If
     * false, no lighting is used on object for rendering.
     * Default is true.
     *
     * @param $b true to make textures (if any) be used for rendering object
     */
	public void normalsEnabled(boolean $b)
	{
		_normalsEnabled = $b;
	}

    /**
     * When true, Renderer draws using vertex points list, rather than faces
     * list. (ie, using glDrawArrays instead of glDrawElements)
     * Default is false.
     *
     * @return renderer draws using vertex points list, rather than faces list
     */
	public boolean ignoreFaces()
	{
		return _ignoreFaces;
	}

    /**
     * When true, Renderer draws using vertex points list, rather than faces
     * list. (ie, using glDrawArrays instead of glDrawElements)
     * Default is false.
     *
     * @param $b true to make that Renderer draws using vertex points list,
     *           rather than faces list
     */
	public void ignoreFaces(boolean $b)
	{
		_ignoreFaces = $b;
	}	
	
    /**
     * Options are: TRIANGLES, LINES, and POINTS.
     * Default is TRIANGLES.
     *
     * @return render shape type
     */
	public RenderType renderType()
	{
		return _renderType;
	}

    /**
     * Options are: TRIANGLES, LINES, and POINTS.
     * Default is TRIANGLES.
     *
     * @param $type render shape type
     */
	public void renderType(RenderType $type)
	{
		_renderType = $type;
	}
	
    /**
     * Returns current used shading model type.
     * Possible values are ShadeModel.SMOOTH and ShadeModel.FLAT.
     * Default is ShadeModel.SMOOTH.
     *
     * @return the current value of the shading model
     */
	public ShadeModel shadeModel()
	{
		return _shadeModel;
	}

    /**
     * Specifies current used shading model type.
     * Possible values are ShadeModel.SMOOTH and ShadeModel.FLAT.
     * Default is ShadeModel.SMOOTH.
     *
     * @param $shadeModel specified value of the shading model
     */
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

    /**
     * Returns color used to render object.
     * Note: Color's rgb attributes only work when vertex colors is enabled.
     *
     * @return the color used to render object
     */
	public Color4 defaultColor()
	{
		return _defaultColor;
	}

    /**
     * Sets color used to render object.
     * Note: Color's rgb attributes only work when vertex colors is enabled.
     *
     * @param color the color used to render object
     */
    public void defaultColor(Color4 color) {
        _defaultColor = color;
        if (hasVertexColors() && vertexColorsEnabled() && _vertices.colors() != null) {
            for (int i = 0; i < _vertices.capacity(); i++) {
                _vertices.colors().set(i, color);
            }
        } else {
            mColors = new Color4BufferList(_vertices.capacity());
            for (int i = 0; i < _vertices.capacity(); i++) {
                mColors.set(i, color);
            }
        }
    }

    /**
     * Returns X/Y/Z position of object.
     *
     * @return position vector of object
     */
	public Number3d position()
	{
		return mPosition;
	}
	
    /**
     * Returns X/Y/Z euler rotation of object, using Euler angles. Units should
     * be in degrees, to match OpenGL usage.
     *
     * @return rotation vector of object
     */
	public Number3d rotation()
	{
		return mRotation;
	}

    /**
     * Returns X/Y/Z scale of object.
     *
     * @return scale vector of object
     */
	public Number3d scale()
	{
		return mScale;
	}
	
    /**
     * Returns point size. (applicable when renderType is POINT)
     * Default is 3.
     *
     * @return point size
     */
	public float pointSize()
	{
		return _pointSize; 
	}

    /**
     * Sets point size. (applicable when renderType is POINT)
     * Default is 3.
     *
     * @param $n point size
     */
	public void pointSize(float $n)
	{
		_pointSize = $n;
	}

    /**
     * Indicates if point smoothing enabled (anti-aliasing), applicable when
     * renderType is POINT. When true, points look like circles rather than
     * squares.
     * Default is true.
     *
     * @return true if point will be applied smoothing
     */
	public boolean pointSmoothing()
	{
		return _pointSmoothing;
	}

    /**
     * Enabled point smoothing (anti-aliasing), applicable when renderType is
     * POINT. When true, points look like circles rather than squares.
     * Default is true.
     *
     * @param $b true to make that point will be applied smoothing
     */
	public void pointSmoothing(boolean $b)
	{
		_pointSmoothing = $b;
	}

    /**
     * Return line width. (applicable when renderType is LINE)
     * Default is 1.
     *
     * Remember that maximum line width is OpenGL-implementation specific, and
     * varies depending on whether lineSmoothing is enabled or not. Eg, on Nexus
     * One, lineWidth can range from 1 to 8 without smoothing, and can only be
     * 1f with smoothing.
     *
     * @return return line width
     */
	public float lineWidth()
	{
		return _lineWidth;
	}

    /**
     * Set line width. (applicable when renderType is LINE)
     * Default is 1.
     *
     * Remember that maximum line width is OpenGL-implementation specific, and
     * varies depending on whether lineSmoothing is enabled or not. Eg, on Nexus
     * One, lineWidth can range from 1 to 8 without smoothing, and can only be
     * 1f with smoothing.
     *
     * @param $n line width
     */
	public void lineWidth(float $n)
	{
		_lineWidth = $n;
	}
	
    /**
     * True if line smoothing enabled (anti-aliasing), applicable when
     * renderType is LINE.
     * Default is false.
     *
     * @return true if line will be applied smoothing
     */
	public boolean lineSmoothing()
	{
		return _lineSmoothing;
	}

    /**
     * Enabled line smoothing (anti-aliasing), applicable when renderType is
     * LINE.
     * Default is false.
     *
     * @param $b true if line will be applied smoothing
     */
	public void lineSmoothing(boolean $b)
	{
		_lineSmoothing = $b;
	}
	
    /**
     * Returns object name.
     *
     * @return object name
     */
	public String name()
	{
		return _name;
	}

    /**
     * Sets object name.
     *
     * @param $name object name
     */
	public void name(String $name)
	{
		_name = $name;
	}
	
	public IObject3dContainer parent()
	{
		return _parent;
	}

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

    Number3d center() {
        return mCenter;
    }

    /**
     * Indicates if object support transparency.
     * Default is true. (Keep transparent enabled in ES 1.1 whatever parameter is.)
     *
     * @return true if object support transparency
     */
    public boolean transparentEnabled() {
        return mTransparent;
    }

    /**
     * Enables object transparency support.
     * Default is true. (Keep transparent enabled in ES 1.1 whatever parameter is.)
     *
     * @param transparent true to enable object transparency support
     */
    public void transparentEnabled(Boolean transparent) {
        mTransparent = transparent;
    }

    /**
     * Indicates if object depth test support enabled.
     * Default is true. (Keep depth test enabled in ES 1.1 whatever parameter is.)
     *
     * @return transparent true to enable object transparency support
     */
    public boolean forcedDepthEnabled() {
        return mForcedDepth;
    }

    /**
     * Enables object depth test support.
     * Default is true. (Keep depth test enabled in ES 1.1 whatever parameter is.)
     *
     * @param forcedDepth true to enable object transparency support
     */
    public void forcedDepthEnabled(Boolean forcedDepth) {
        mForcedDepth = forcedDepth;
    }

    boolean vertexBufferObjectEnabled() {
        return mVertexBufferObject;
    }

    void vertexBufferObjectEnabled(Boolean vertexBufferObject) {
        mVertexBufferObject = vertexBufferObject;
        mBuffered = !mVertexBufferObject;
        if (!vertexBufferObject) {
            GLES20.glDeleteBuffers(VBO_ID.TOTAL.ordinal(), mBuffers, 0);
        }
    }

    protected void render(CameraVo camera, float[] projMatrix, float[] vMatrix) {
        render(camera, projMatrix, vMatrix, null);
    }

    protected void render(CameraVo camera, float[] projMatrix, float[] vMatrix, final float[] parentMatrix) {
        if (!isVisible()) return;
        mMaterial = Scene.getDefaultMaterial();
        mMaterial.setLightEnabled(scene().lightingEnabled() && hasNormals() && normalsEnabled() && lightingEnabled());

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

        if (!mBuffered) {
            makeVertextBufferObject();
            mBuffered = true;
        }

        if (mVertexBufferObject) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers[VBO_ID.POINT.ordinal()]);
            mMaterial.setVertices();
        } else {
            mMaterial.setVertices(_vertices.points().buffer());
        }

        if (_vertices.uvs() != null) {
            if (mVertexBufferObject) {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers[VBO_ID.UV.ordinal()]);
                mMaterial.setTextureCoords();
            } else {
                mMaterial.setTextureCoords(_vertices.uvs().buffer());
            }
        }

        if (hasVertexColors() && vertexColorsEnabled() && _vertices.colors() != null) {
            if (mVertexBufferObject) {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers[VBO_ID.COLOR.ordinal()]);
                mMaterial.setColors();
            } else {
                mMaterial.setColors(_vertices.colors().buffer());
            }
        } else if (mColors != null) {
            if (mVertexBufferObject) {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers[VBO_ID.COLOR.ordinal()]);
                mMaterial.setColors();
            } else {
                mMaterial.setColors(mColors.buffer());
            }
        } else {
            mMaterial.setColors(defaultColor());
        }

        if (hasNormals() && normalsEnabled() && _vertices.normals() != null) {
            if (mVertexBufferObject) {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers[VBO_ID.NORMAL.ordinal()]);
                mMaterial.setNormals();
            } else {
                mMaterial.setNormals(_vertices.normals().buffer());
            }
        }
        setShaderParams();

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
        if(parentMatrix != null) {
            Matrix.multiplyMM(mTmpMatrix, 0, parentMatrix, 0, mMMatrix, 0);
            System.arraycopy(mTmpMatrix, 0, mMMatrix, 0, 16);
        }
        Matrix.multiplyMM(mMVPMatrix, 0, vMatrix, 0, mMMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, projMatrix, 0, mMVPMatrix, 0);

        mMaterial.setMVPMatrix(mMVPMatrix);
        mMaterial.setModelMatrix(mMMatrix);
        mMaterial.setViewMatrix(vMatrix);

        vertices().points().buffer().position(0);
        if (!ignoreFaces()) {
            int pos, len;

            if (!faces().renderSubsetEnabled()) {
                pos = 0;
                len = faces().size();
            } else {
                pos = faces().renderSubsetStartIndex() * FacesBufferedList.PROPERTIES_PER_ELEMENT;
                len = faces().renderSubsetLength();
            }

            if (mVertexBufferObject) {
                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mBuffers[VBO_ID.FACE.ordinal()]);
                GLES20.glDrawElements(
                        renderType().glValue(),
                        len * FacesBufferedList.PROPERTIES_PER_ELEMENT,
                        GLES20.GL_UNSIGNED_SHORT,
                        0);
            } else {
                faces().buffer().position(pos);
                GLES20.glDrawElements(
                        renderType().glValue(),
                        len * FacesBufferedList.PROPERTIES_PER_ELEMENT,
                        GLES20.GL_UNSIGNED_SHORT,
                        faces().buffer());
            }
        } else {
            GLES20.glDrawArrays(renderType().glValue(), 0, vertices().size());
        }

        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        if (this instanceof Object3dContainer) {
            Object3dContainer container = (Object3dContainer)this;

            for (int i = 0; i < container.children().size(); i++) {
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
        mMaterial.setLightList(scene().lights());
        mMaterial.setFog(scene().fogEnabled(), scene().fogColor(), scene().fogNear(), scene().fogFar(), scene().fogType() );
        mMaterial.setMaterialColorEnable(colorMaterialEnabled());
    };

    void makeVertextBufferObject() {
        GLES20.glDeleteBuffers(VBO_ID.TOTAL.ordinal(), mBuffers, 0);
        GLES20.glGenBuffers(VBO_ID.TOTAL.ordinal(), mBuffers, 0);

        _vertices.points().buffer().position(0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers[VBO_ID.POINT.ordinal()]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
                _vertices.points().capacity() * Number3dBufferList.PROPERTIES_PER_ELEMENT * Number3dBufferList.BYTES_PER_PROPERTY,
                _vertices.points().buffer(),
                GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers[VBO_ID.UV.ordinal()]);
        if (_vertices.uvs() != null) {
            _vertices.uvs().buffer().position(0);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
                    _vertices.uvs().capacity() * UvBufferList.PROPERTIES_PER_ELEMENT * UvBufferList.BYTES_PER_PROPERTY,
                    _vertices.uvs().buffer(),
                    GLES20.GL_STATIC_DRAW);
        }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers[VBO_ID.COLOR.ordinal()]);
        if (hasVertexColors() && vertexColorsEnabled() && _vertices.colors() != null) {
            _vertices.colors().buffer().position(0);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
                    _vertices.colors().capacity() * Color4BufferList.PROPERTIES_PER_ELEMENT,
                    _vertices.colors().buffer(),
                    GLES20.GL_STATIC_DRAW);
        } else if (mColors != null) {
            mColors.buffer().position(0);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
                    mColors.capacity() * Color4BufferList.PROPERTIES_PER_ELEMENT,
                    mColors.buffer(),
                    GLES20.GL_STATIC_DRAW);
        }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers[VBO_ID.NORMAL.ordinal()]);
        if (hasNormals() && normalsEnabled() && _vertices.normals() != null) {
            _vertices.normals().buffer().position(0);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
                    _vertices.normals().capacity() * Number3dBufferList.PROPERTIES_PER_ELEMENT * Number3dBufferList.BYTES_PER_PROPERTY,
                    _vertices.normals().buffer(),
                    GLES20.GL_STATIC_DRAW);
        }

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mBuffers[VBO_ID.FACE.ordinal()]);
        if (!ignoreFaces()) {
            _faces.buffer().position(0);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER,
                    _faces.capacity() * FacesBufferedList.PROPERTIES_PER_ELEMENT * 2,
                    _faces.buffer(),
                    GLES20.GL_STATIC_DRAW);
        }
    }

    void containAABB() {
        FloatBuffer[] points = mAabbBuffer;
        points[0] = vertices().points().buffer();

        if (points == null || points.length == 0) {
            Log.i(TAG, "contain nothing, return!!!");
            return;
        }

        if (!mObtainAABB) {
            mMinX = mMinY = mMinZ = Float.MAX_VALUE;
            mMaxX = mMaxY = mMaxZ = Float.MIN_VALUE;
            for (int idx = 0; idx < points.length; idx++) {
                points[idx].rewind();
                if (points[idx] == null || points[idx].remaining() <= 2) {
                    continue;
                }
                for (int i = 0, len = points[idx].remaining() / 3; i < len; i++) {
                    populateFromBuffer(mCompVect, points[idx], i);

                    if (mCompVect.x < mMinX)
                        mMinX = mCompVect.x;
                    else if (mCompVect.x > mMaxX) {
                        mMaxX = mCompVect.x;
                    }

                    if (mCompVect.y < mMinY)
                        mMinY = mCompVect.y;
                    else if (mCompVect.y > mMaxY) {
                        mMaxY = mCompVect.y;
                    }

                    if (mCompVect.z < mMinZ)
                        mMinZ = mCompVect.z;
                    else if (mCompVect.z > mMaxZ) {
                        mMaxZ = mCompVect.z;
                    }
                }
            }
            mObtainAABB = true;
        }

        mCenter.x = (mMinX + mMaxX) / 2;
        mCenter.y = (mMinY + mMaxY) / 2;
        mCenter.z = (mMinZ + mMaxZ) / 2;

        mExtent.x = mMaxX - mCenter.x;
        mExtent.y = mMaxY - mCenter.y;
        mExtent.z = mMaxZ - mCenter.z;

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

        mResult[0] = mCenter.x;
        mResult[1] = mCenter.y;
        mResult[2] = mCenter.z;
        mResult[3] = 1;

        Matrix.multiplyMV(mResult, 0, mTransMC, 0, mResult, 0);

        synchronized (this) {
            if (_parent != null && _parent instanceof Object3d) {
                calcAABBPos((Object3d) parent(), TRANSLATE | ROTATE | SCALE, mResult);
            }
        }
        mCenter.x = mResult[0];
        mCenter.y = mResult[1];
        mCenter.z = mResult[2];

        mResult[0] = mExtent.x;
        mResult[1] = mExtent.y;
        mResult[2] = mExtent.z;
        mResult[3] = 1;

        mAccmlR.setAll(mRotation.x, mRotation.y, mRotation.z);
        mAccmlS.setAll(mScale.x, mScale.y, mScale.z);
        synchronized (this) {
            if (_parent != null && _parent instanceof Object3d) {
                accmlAABBTrans((Object3d) parent(), ROTATE, mAccmlR);
                accmlAABBTrans((Object3d) parent(), SCALE, mAccmlS);
            }
        }
        Matrix.setIdentityM(mRotMExt, 0);

        Matrix.rotateM(mRotMExt, 0, mAccmlR.x, 1, 0, 0);
        Matrix.rotateM(mRotMExt, 0, mAccmlR.y, 0, 1, 0);
        Matrix.rotateM(mRotMExt, 0, mAccmlR.z, 0, 0, 1);

        for (int i = 0; i < mRotMExt.length; i++) {
            mAbsRotMExt[i] = Math.abs(mRotMExt[i]);
        }

        Matrix.setIdentityM(mScaleMExt, 0);
        Matrix.scaleM(mScaleMExt, 0, mAccmlS.x, mAccmlS.y, mAccmlS.z);

        Matrix.multiplyMV(mResult, 0, mAbsRotMExt, 0, mResult, 0);
        Matrix.multiplyMV(mResult, 0, mScaleMExt, 0, mResult, 0);

        mExtent.x = mResult[0];
        mExtent.y = mResult[1];
        mExtent.z = mResult[2];

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
            Number3d.multiply(result,result,parent.scale());
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

        mDirect.setAll(ray.getPoint().x - mCenter.x,
                ray.getPoint().y - mCenter.y, ray.getPoint().z - mCenter.z);

        mWdU[0] = Number3d.dot(ray.getVector(), UNIT_X);
        mAWdU[0] = Math.abs(mWdU[0]);
        mDdU[0] = Number3d.dot(mDirect, UNIT_X);
        mADdU[0] = Math.abs(mDdU[0]);
        if (mADdU[0] > mExtent.x && mDdU[0] * mWdU[0] >= 0.0) {
            return false;
        }

        mWdU[1] = Number3d.dot(ray.getVector(), UNIT_Y);
        mAWdU[1] = Math.abs(mWdU[1]);
        mDdU[1] = Number3d.dot(mDirect, UNIT_Y);
        mADdU[1] = Math.abs(mDdU[1]);
        if (mADdU[1] > mExtent.y && mDdU[1] * mWdU[1] >= 0.0) {
            return false;
        }

        mWdU[2] = Number3d.dot(ray.getVector(), UNIT_Z);
        mAWdU[2] = Math.abs(mWdU[2]);
        mDdU[2] = Number3d.dot(mDirect, UNIT_Z);
        mADdU[2] = Math.abs(mDdU[2]);
        if (mADdU[2] > mExtent.z && mDdU[2] * mWdU[2] >= 0.0) {
            return false;
        }

        Number3d.cross(mCrossD, ray.getVector(), mDirect);

        mAWxDdU[0] = Math.abs(Number3d.dot(mCrossD, UNIT_X));
        rhs = mExtent.y * mAWdU[2] + mExtent.z * mAWdU[1];
        if (mAWxDdU[0] > rhs) {
            return false;
        }

        mAWxDdU[1] = Math.abs(Number3d.dot(mCrossD, UNIT_Y));
        rhs = mExtent.x * mAWdU[2] + mExtent.z * mAWdU[0];
        if (mAWxDdU[1] > rhs) {
            return false;
        }

        mAWxDdU[2] = Math.abs(Number3d.dot(mCrossD, UNIT_Z));
        rhs = mExtent.x * mAWdU[1] + mExtent.y * mAWdU[0];
        if (mAWxDdU[2] > rhs) {
            return false;
        }

        return true;
    }

    /**
     * Pass the touch screen motion event down to the target object, or this
     * object if it is the target.
     *
     * @param ray a geometric ray from the touch
     * @param event The motion event to be dispatched
     * @param list the touched object(ray intersected ones)
     * @return True if the event was handled by the object, false otherwise.
     */
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

    /**
     * Implement this method to handle touch screen motion events.
     *
     * @param ray a geometric ray from the touch
     * @param event The motion event to be dispatched
     * @param list the touched object(ray intersected ones)
     * @return True if the event was handled by the object, false otherwise.
     */
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

    private Number3d getIntersectPoint(float x, float y, float z) {
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

    /**
     * Register a callback to be invoked when a touch event is sent to this object.
     *
     * @param listener the touch listener to attach to this object
     */
    public void setOnTouchListener(OnTouchListener listener) {
        mOnTouchListener = listener;
    }

    /**
     * Register a callback to be invoked when this object is clicked. If this object is not
     * clickable, it becomes clickable.
     *
     * @param listener The callback that will run
     *
     * @see #setClickable(boolean)
     */
    public void setOnClickListener(OnClickListener listener) {
        if (!isClickable()) {
            setClickable(true);
        }
        mOnClickListener = listener;
    }

    /**
     * Register a callback to be invoked when this object is clicked and held. If this object is not
     * long clickable, it becomes long clickable.
     *
     * @param listener The callback that will run
     *
     * @see #setLongClickable(boolean)
     */
    public void setOnLongClickListener(OnLongClickListener listener) {
        if (!isLongClickable()) {
            setLongClickable(true);
        }
        mOnLongClickListener = listener;
    }

    /**
     * Indicates whether this object reacts to click events or not.
     *
     * @return true if the object is clickable, false otherwise
     *
     * @see #setClickable(boolean)
     */
    public boolean isClickable() {
        return (mViewFlags & CLICKABLE) == CLICKABLE;
    }

    /**
     * Enables or disables click events for this object. When a object
     * is clickable it will change its state to "pressed" on every click.
     * Subclasses should set the object clickable to visually react to
     * user's clicks.
     *
     * @param clickable true to make the object clickable, false otherwise
     *
     * @see #isClickable()
     */
    public void setClickable(boolean clickable) {
        setFlags(clickable ? CLICKABLE : 0, CLICKABLE);
    }

    /**
     * Indicates whether this object reacts to long click events or not.
     *
     * @return true if the object is long clickable, false otherwise
     *
     * @see #setLongClickable(boolean)
     */
    public boolean isLongClickable() {
        return (mViewFlags & LONG_CLICKABLE) == LONG_CLICKABLE;
    }

    /**
     * Enables or disables long click events for this object. When a object is long
     * clickable it reacts to the user holding down the button for a longer
     * duration than a tap. This event can either launch the listener or a
     * context menu.
     *
     * @param longClickable true to make the object long clickable, false otherwise
     * @see #isLongClickable()
     */
    public void setLongClickable(boolean longClickable) {
        setFlags(longClickable ? LONG_CLICKABLE : 0, LONG_CLICKABLE);
    }

    /**
     * Indicates whether the object is currently in pressed state. Unless
     * {@link #setPressed(boolean)} is explicitly called, only clickable objects can enter
     * the pressed state.
     *
     * @see #setPressed(boolean)
     * @see #isClickable()
     * @see #setClickable(boolean)
     *
     * @return true if the object is currently pressed, false otherwise
     */
    public boolean isPressed() {
        return (mPrivateFlags & PRESSED) == PRESSED;
    }

    /**
     * Sets the pressed state for this object.
     *
     * @see #isClickable()
     * @see #setClickable(boolean)
     *
     * @param pressed Pass true to set the object's internal state to "pressed", or false to reverts
     *        the object's internal state from a previously set "pressed" state.
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
     * Dispatch setPressed to all of this object's children.
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
     * Call this object's OnClickListener, if it is defined.
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
     * @return A handler associated with the thread running the object. This
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
     * only when this object is attached to a window.</p>
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
     * only when this object is attached to a window.</p>
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
     * only when this object is attached to a window.</p>
     *
     * @param action The Runnable to remove from the message handling queue
     *
     * @return true if this object could ask the Handler to remove the Runnable,
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
     * Call this object's OnLongClickListener, if it is defined. Invokes the context menu if the
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
