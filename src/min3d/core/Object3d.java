package min3d.core;

import android.R;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.graphics.drawable.NinePatchDrawable;
import android.opengl.GLES20;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import min3d.GLConfiguration;
import min3d.GLHandler;
import min3d.interfaces.IObject3dContainer;
import min3d.interfaces.IObject3dParent;
import min3d.listeners.OnClickListener;
import min3d.listeners.OnFocusChangeListener;
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
import min3d.vos.TextureVo;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

/**
 * @author Lee
 */

/**
 * This class represents the basic building block for user interface components. A Object3d
 * occupies a polygon area on the screen and is responsible for drawing and
 * event handling.
 */
public class Object3d implements Callback
{
    private final static String TAG = "Object3d";
    private static final boolean DBG = false;
	private String _name;
	
	private RenderType _renderType = RenderType.TRIANGLES;
	
    private final static Number3d UNIT_X = new Number3d(1, 0, 0);
    private final static Number3d UNIT_Y = new Number3d(0, 1, 0);
    private final static Number3d UNIT_Z = new Number3d(0, 0, 1);
    private final static Number3d UNIT_XYZ = new Number3d(1, 1, 1);

    private final static int TRANSLATE = 0x1;
    private final static int ROTATE = 0x2;
    private final static int SCALE = 0x4;

    /**
     * This view does not want keystrokes. Use with TAKES_FOCUS_MASK when
     * calling setFlags.
     */
    private static final int NOT_FOCUSABLE = 0x00000000;

    /**
     * This view wants keystrokes. Use with TAKES_FOCUS_MASK when calling
     * setFlags.
     */
    private static final int FOCUSABLE = 0x00000001;

    /**
     * Mask for use with setFlags indicating bits used for focus.
     */
    private static final int FOCUSABLE_MASK = 0x00000001;

    /**
     * This view is visible.
     * Use with {@link #setVisibility} and <a href="#attr_android:visibility">{@code
     * android:visibility}.
     */
    public static final int VISIBLE = 0x00000000;

    /**
     * This view is invisible, but it still takes up space for layout purposes.
     * Use with {@link #setVisibility} and <a href="#attr_android:visibility">{@code
     * android:visibility}.
     */
    public static final int INVISIBLE = 0x00000004;

    /**
     * This view is invisible, and it doesn't take any space for layout
     * purposes. Use with {@link #setVisibility} and <a href="#attr_android:visibility">{@code
     * android:visibility}.
     */
    public static final int GONE = 0x00000008;

    /**
     * Mask for use with setFlags indicating bits used for visibility.
     * {@hide}
     */
    static final int VISIBILITY_MASK = 0x0000000C;

    /* Masks for mPrivateFlags2 */

    /**
     * Indicates that this view has reported that it can accept the current drag's content.
     * Cleared when the drag operation concludes.
     * @hide
     */
    static final int DRAG_CAN_ACCEPT              = 0x00000001;

    /**
     * Indicates that this view is currently directly under the drag location in a
     * drag-and-drop operation involving content that it can accept.  Cleared when
     * the drag exits the view, or when the drag operation concludes.
     * @hide
     */
    static final int DRAG_HOVERED                 = 0x00000002;

    /**
     * Indicates whether the view layout direction has been resolved and drawn to the
     * right-to-left direction.
     *
     * @hide
     */
    static final int LAYOUT_DIRECTION_RESOLVED_RTL = 0x00000004;

    /**
     * Indicates whether the view layout direction has been resolved.
     *
     * @hide
     */
    static final int LAYOUT_DIRECTION_RESOLVED = 0x00000008;


    /* End of masks for mPrivateFlags2 */

    static final int DRAG_MASK = DRAG_CAN_ACCEPT | DRAG_HOVERED;

    private static final int PRESSED_STATE_DURATION = 125;
    private static final int DEFAULT_LONG_PRESS_TIMEOUT = 500;

    /**
     * Defines the duration in milliseconds we will wait to see if a touch event
     * is a tap or a scroll. If the user does not move within this interval, it is
     * considered to be a tap.
     */
    private static final int TAP_TIMEOUT = 180;

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
     * <p>Indicates this view is caching its drawing into a bitmap.</p>
     * {@hide}
     */
    static final int RENDER_CACHE_ENABLED = 0x00008000;

    /**
     * <p>
     * Indicates this view can be long clicked. When long clickable, a View
     * reacts to long clicks by notifying the OnLongClickListener or showing a
     * context menu.
     * </p>
     * {@hide}
     */
    static final int LONG_CLICKABLE = 0x00200000;

    // for mPrivateFlags:
    /** {@hide} */
    static final int WANTS_FOCUS                    = 0x00000001;
    /** {@hide} */
    static final int FOCUSED                        = 0x00000002;
    /** {@hide} */
    static final int SELECTED                       = 0x00000004;
    /** {@hide} */
    static final int IS_ROOT_NAMESPACE              = 0x00000008;
    /** {@hide} */
    static final int HAS_BOUNDS                     = 0x00000010;
    /** {@hide} */
    static final int DRAWN                          = 0x00000020;
    /**
     * When this flag is set, this view is running an animation on behalf of its
     * children and should therefore not cancel invalidate requests, even if they
     * lie outside of this view's bounds.
     *
     * {@hide}
     */
    static final int DRAW_ANIMATION                 = 0x00000040;
    /** {@hide} */
    static final int SKIP_DRAW                      = 0x00000080;
    /** {@hide} */
    static final int ONLY_DRAWS_BACKGROUND          = 0x00000100;
    /** {@hide} */
    static final int REQUEST_TRANSPARENT_REGIONS    = 0x00000200;
    /** {@hide} */
    static final int DRAWABLE_STATE_DIRTY           = 0x00000400;
    /** {@hide} */
    static final int MEASURED_DIMENSION_SET         = 0x00000800;
    /** {@hide} */
    static final int FORCE_LAYOUT                   = 0x00001000;
    /** {@hide} */
    static final int LAYOUT_REQUIRED                = 0x00002000;

    private static final int PRESSED                = 0x00004000;

    /**
     * View flag indicating whether this view was invalidated (fully or partially.)
     *
     * @hide
     */
    static final int DIRTY                          = 0x00200000;

    /**
     * View flag indicating whether this view was invalidated by an opaque
     * invalidate request.
     *
     * @hide
     */
    static final int DIRTY_OPAQUE                   = 0x00400000;

    /**
     * Mask for {@link #DIRTY} and {@link #DIRTY_OPAQUE}.
     *
     * @hide
     */
    static final int DIRTY_MASK                     = 0x00600000;

    /**
     * Indicates whether the background is opaque.
     *
     * @hide
     */
    static final int OPAQUE_BACKGROUND              = 0x00800000;

    /**
     * Indicates whether the scrollbars are opaque.
     *
     * @hide
     */
    static final int OPAQUE_SCROLLBARS              = 0x01000000;

    /**
     * Indicates whether the view is opaque.
     *
     * @hide
     */
    static final int OPAQUE_MASK                    = 0x01800000;

    /**
     * Indicates that the view has received HOVER_ENTER.  Cleared on HOVER_EXIT.
     * @hide
     */
    private static final int HOVERED              = 0x10000000;

    /** {@hide} */
    static final int ACTIVATED                    = 0x40000000;

    /**
     * <p>Indicates that this view gets its drawable states from its direct parent
     * and ignores its original internal states.</p>
     *
     * @hide
     */
    static final int DUPLICATE_PARENT_STATE = 0x00400000;

    /**
     * View flag indicating whether {@link #addFocusables(ArrayList, int, int)}
     * should add all focusable Views regardless if they are focusable in touch mode.
     */
    public static final int FOCUSABLES_ALL = 0x00000000;

    /**
     * View flag indicating whether {@link #addFocusables(ArrayList, int, int)}
     * should add only Views focusable in touch mode.
     */
    public static final int FOCUSABLES_TOUCH_MODE = 0x00000001;

    /**
     * Use with {@link #focusSearch(int)}. Move focus to the previous selectable
     * item.
     */
    public static final int FOCUS_BACKWARD = 0x00000001;

    /**
     * Use with {@link #focusSearch(int)}. Move focus to the next selectable
     * item.
     */
    public static final int FOCUS_FORWARD = 0x00000002;

    /**
     * Use with {@link #focusSearch(int)}. Move focus to the left.
     */
    public static final int FOCUS_LEFT = 0x00000011;

    /**
     * Use with {@link #focusSearch(int)}. Move focus up.
     */
    public static final int FOCUS_UP = 0x00000021;

    /**
     * Use with {@link #focusSearch(int)}. Move focus to the right.
     */
    public static final int FOCUS_RIGHT = 0x00000042;

    /**
     * Use with {@link #focusSearch(int)}. Move focus down.
     */
    public static final int FOCUS_DOWN = 0x00000082;

    /**
     * <p>Indicates this view can take / keep focus when int touch mode.</p>
     * {@hide}
     */
    static final int FOCUSABLE_IN_TOUCH_MODE = 0x00040000;

    /**
     * The order here is very important to {@link #getDrawableState()}
     */
    private static final int[][] VIEW_STATE_SETS;

    static final int VIEW_STATE_WINDOW_FOCUSED = 1;
    static final int VIEW_STATE_SELECTED = 1 << 1;
    static final int VIEW_STATE_FOCUSED = 1 << 2;
    static final int VIEW_STATE_ENABLED = 1 << 3;
    static final int VIEW_STATE_PRESSED = 1 << 4;
    static final int VIEW_STATE_ACTIVATED = 1 << 5;
    static final int VIEW_STATE_ACCELERATED = 1 << 6;
    static final int VIEW_STATE_HOVERED = 1 << 7;
    static final int VIEW_STATE_DRAG_CAN_ACCEPT = 1 << 8;
    static final int VIEW_STATE_DRAG_HOVERED = 1 << 9;

    static final int[] VIEW_STATE_IDS = new int[] {
        R.attr.state_window_focused,    VIEW_STATE_WINDOW_FOCUSED,
        R.attr.state_selected,          VIEW_STATE_SELECTED,
        R.attr.state_focused,           VIEW_STATE_FOCUSED,
        R.attr.state_enabled,           VIEW_STATE_ENABLED,
        R.attr.state_pressed,           VIEW_STATE_PRESSED,
        R.attr.state_activated,         VIEW_STATE_ACTIVATED,
        R.attr.state_accelerated,       VIEW_STATE_ACCELERATED,
/*        R.attr.state_hovered,           VIEW_STATE_HOVERED,
        R.attr.state_drag_can_accept,   VIEW_STATE_DRAG_CAN_ACCEPT,
        R.attr.state_drag_hovered,      VIEW_STATE_DRAG_HOVERED,*/
    };

    static {
        int[] orderedIds = new int[VIEW_STATE_IDS.length];
        for (int j = 0; j<VIEW_STATE_IDS.length; j += 2) {
            int viewState = VIEW_STATE_IDS[j];
            if (VIEW_STATE_IDS[j] == viewState) {
                orderedIds[j] = viewState;
                orderedIds[j + 1] = VIEW_STATE_IDS[j + 1];
            }
        }
        final int NUM_BITS = VIEW_STATE_IDS.length / 2;
        VIEW_STATE_SETS = new int[1 << NUM_BITS][];
        for (int i = 0; i < VIEW_STATE_SETS.length; i++) {
            int numBits = Integer.bitCount(i);
            int[] set = new int[numBits];
            int pos = 0;
            for (int j = 0; j < orderedIds.length; j += 2) {
                if ((i & orderedIds[j+1]) != 0) {
                    set[pos++] = orderedIds[j];
                }
            }
            VIEW_STATE_SETS[i] = set;
        }
    }

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

    /**
     * Indicates whether the view is temporarily detached.
     *
     * @hide
     */
    static final int CANCEL_NEXT_UP_EVENT = 0x04000000;

    private static final String PREFIX_BACKGROUND = "background_";

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

	protected Vertices mVertices;
    protected Color4BufferList mColors;
	protected TextureList mTextures;
	protected FacesBufferedList mFaces;

	protected boolean _animationEnabled = false;
	
	private Scene _scene;
	protected IObject3dContainer mParent;
    protected GContext mGContext;

    AttachInfo mAttachInfo;

    private Bitmap mRenderingCache;

    /* Background setting */
    private Drawable mBGDrawable;
    private int mBackgroundResource;
    private boolean mBackgroundSizeChanged;
    private int[] mDrawableState = null;

    /* Variable for Touch handler */
    Number3d mCoordinate = null;
    ArrayList<Object3d> mDownList = null;
    ArrayList<Object3d> mUpList = null;
    List<Object3d> mLongClickList = null;
    UnsetPressedState mUnsetPressedState;
    CheckForLongPress mPendingCheckForLongPress;
    CheckForTap mPendingCheckForTap = null;
    PerformClick mPerformClick;
    int mViewFlags = ENABLED;
    int mPrivateFlags;
    int mPrivateFlags2;
    int mWindowAttachCount;
    boolean mHasPerformedLongPress;
    OnTouchListener mOnTouchListener;
    OnClickListener mOnClickListener;
    OnLongClickListener mOnLongClickListener;
    OnFocusChangeListener mOnFocusChangeListener;

    /* For vertex buffer object */
    boolean mVertexBufferObject = false;
    boolean mBuffered = !mVertexBufferObject;
    int mBuffers[] = {0,0,0,0,0};

    /* For Bounding box */
    Number3d mCenter = new Number3d(0, 0, 0);
    Number3d mExtent = new Number3d(0, 0, 0);

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

    float mWidth;
    float mHeight;

	/**
	 * Maximum number of vertices and faces must be specified at instantiation.
	 */
	public Object3d(GContext context, int $maxVertices, int $maxFaces)
	{
        mGContext = context;
		mVertices = new Vertices($maxVertices, true,true,true);
		mFaces = new FacesBufferedList($maxFaces);
		mTextures = new TextureList(context);
	}
	
	/**
	 * Adds three arguments 
	 */
	public Object3d(GContext context, int $maxVertices, int $maxFaces, Boolean $useUvs, Boolean $useNormals, Boolean $useVertexColors)
	{
        mGContext = context;
		mVertices = new Vertices($maxVertices, $useUvs,$useNormals,$useVertexColors);
		mFaces = new FacesBufferedList($maxFaces);
		mTextures = new TextureList(context);
	}
	
	/**
	 * This constructor is convenient for cloning purposes 
	 */
	public Object3d(GContext context, Vertices $vertices, FacesBufferedList $faces, TextureList $textures)
	{
        mGContext = context;
		mVertices = $vertices;
		mFaces = $faces;
		mTextures = $textures;
	}
	
	/**
	 * Holds references to vertex position list, vertex u/v mappings list, vertex normals list, and vertex colors list
	 */
	public Vertices getVertices()
	{
		return mVertices;
	}

	/**
	 * List of object's faces (ie, index buffer) 
	 */
	public FacesBufferedList getFaces()
	{
		return mFaces;
	}
	
	public TextureList getTextures()
	{
		return mTextures;
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
		return mVertices.getPoints();
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public UvBufferList uvs()
	{
		return mVertices.getUvs();
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public Number3dBufferList normals()
	{
		return mVertices.getNormals();
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public Color4BufferList colors()
	{
		return mVertices.getColors();
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public boolean hasUvs()
	{
		return mVertices.hasUvs();
	}

	/**
	 * Convenience 'pass-thru' method  
	 */
	public boolean hasNormals()
	{
		return mVertices.hasNormals();
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public boolean hasVertexColors()
	{
		return mVertices.hasColors();
	}


	/**
	 * Clear object for garbage collection.
	 */
	public void clear()
	{
		if (this.getVertices().getPoints() != null) 	this.getVertices().getPoints().clear();
		if (this.getVertices().getUvs() != null) 		this.getVertices().getUvs().clear();
		if (this.getVertices().getNormals() != null) 	this.getVertices().getNormals().clear();
		if (this.getVertices().getColors() != null) 	this.getVertices().getColors().clear();
		if (mTextures != null) 					mTextures.clear();
		
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
        if (hasVertexColors() && vertexColorsEnabled() && mVertices.getColors() != null) {
            for (int i = 0; i < mVertices.capacity(); i++) {
                mVertices.getColors().set(i, color);
            }
        } else {
            mColors = new Color4BufferList(mVertices.capacity());
            for (int i = 0; i < mVertices.capacity(); i++) {
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
		return mParent;
	}

	void parent(IObject3dContainer $container) /*package-private*/
	{
		mParent = $container;
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
		Vertices v = mVertices.clone();
		FacesBufferedList f = mFaces.clone();
			
		Object3d clone = new Object3d(mGContext, v, f, mTextures);
		
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

    protected void onRender() {
    }

    protected void render(CameraVo camera, float[] projMatrix, float[] vMatrix) {
        render(camera, projMatrix, vMatrix, null);
    }

    protected void render(CameraVo camera, float[] projMatrix, float[] vMatrix, final float[] parentMatrix) {
        int visibility = getVisibility() & VISIBILITY_MASK;
        if (visibility == Object3d.GONE || scene() == null) {
            return;
        }

        if(isLayerTextureDirty() && visibility == Object3d.VISIBLE) {
            onManageLayerTexture();
        }

        onRender();

        mProjMatrix = projMatrix;

        prepareRenderingShader(camera);
        prepareRenderingBuffer();
        computeRenderingMatrix(projMatrix, vMatrix, parentMatrix);
        doRenderingTask(vMatrix);

        if (this instanceof Object3dContainer) {
            Object3dContainer container = (Object3dContainer)this;

            for (int i = 0; i < container.children().size(); i++) {
                Object3d o = container.children().get(i);
                o.render(camera, projMatrix, vMatrix, mMMatrix);
            }
        }
    }

    protected void prepareRenderingShader(CameraVo camera) {
        int visibility = getVisibility() & VISIBILITY_MASK;
        if (getVertices() == null || getVertices().size() == 0
                || visibility == INVISIBLE) {
            return;
        }

        mMaterial = scene().getDefaultMaterial(mGContext);
        mMaterial.setLightEnabled(scene().lightingEnabled() && hasNormals() && normalsEnabled() && lightingEnabled());
        mMaterial.useProgram();
        mMaterial.bindTextures(this);
        mMaterial.setCamera(camera);

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

        setShaderParams();
    }

    protected void prepareRenderingBuffer() {
        int visibility = getVisibility() & VISIBILITY_MASK;
        if (getVertices() == null || getVertices().size() == 0
                || visibility == INVISIBLE) {
            return;
        }

        if (!mBuffered) {
            makeVertextBufferObject();
            mBuffered = true;
        }

        if (mVertexBufferObject) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers[VBO_ID.POINT.ordinal()]);
            mMaterial.setVertices();
        } else {
            mMaterial.setVertices(mVertices.getPoints().buffer());
        }

        if (mVertices.getUvs() != null) {
            if (mVertexBufferObject) {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers[VBO_ID.UV.ordinal()]);
                mMaterial.setTextureCoords();
            } else {
                mMaterial.setTextureCoords(mVertices.getUvs().buffer());
            }
        }

        if (hasVertexColors() && vertexColorsEnabled() && mVertices.getColors() != null) {
            if (mVertexBufferObject) {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers[VBO_ID.COLOR.ordinal()]);
                mMaterial.setColors();
            } else {
                mMaterial.setColors(mVertices.getColors().buffer());
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

        if (hasNormals() && normalsEnabled() && mVertices.getNormals() != null) {
            if (mVertexBufferObject) {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers[VBO_ID.NORMAL.ordinal()]);
                mMaterial.setNormals();
            } else {
                mMaterial.setNormals(mVertices.getNormals().buffer());
            }
        }
    }

    protected void computeRenderingMatrix(float[] projMatrix, float[] vMatrix, final float[] parentMatrix) {
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
    }

    protected void doRenderingTask(float[] vMatrix) {
        int visibility = getVisibility() & VISIBILITY_MASK;
        if (getVertices() == null || getVertices().size() == 0
                || visibility == INVISIBLE) {
            return;
        }

        mMaterial.setMVPMatrix(mMVPMatrix);
        mMaterial.setModelMatrix(mMMatrix);
        mMaterial.setViewMatrix(vMatrix);

        getVertices().getPoints().buffer().position(0);
        if (!ignoreFaces()) {
            int pos, len;

            if (!getFaces().renderSubsetEnabled()) {
                pos = 0;
                len = getFaces().size();
            } else {
                pos = getFaces().renderSubsetStartIndex() * FacesBufferedList.PROPERTIES_PER_ELEMENT;
                len = getFaces().renderSubsetLength();
            }

            if (mVertexBufferObject) {
                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mBuffers[VBO_ID.FACE.ordinal()]);
                GLES20.glDrawElements(
                        renderType().glValue(),
                        len * FacesBufferedList.PROPERTIES_PER_ELEMENT,
                        GLES20.GL_UNSIGNED_SHORT,
                        0);
            } else {
                getFaces().buffer().position(pos);
                GLES20.glDrawElements(
                        renderType().glValue(),
                        len * FacesBufferedList.PROPERTIES_PER_ELEMENT,
                        GLES20.GL_UNSIGNED_SHORT,
                        getFaces().buffer());
            }
        } else {
            GLES20.glDrawArrays(renderType().glValue(), 0, getVertices().size());
        }

        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    /**
     * This is called when the object is attached to a window.  At this point it
     * has a Surface and will start drawing.  Note that this function is
     * guaranteed to be called before {@link #onDraw(android.graphics.Canvas)},
     * however it may be called any time before the first onDraw -- including
     * before or after {@link #onMeasure(int, int)}.
     *
     * @see #onDetachedFromWindow()
     */
    protected void onAttachedToWindow() {
/*        if ((mPrivateFlags & REQUEST_TRANSPARENT_REGIONS) != 0) {
            mParent.requestTransparentRegion(this);
        }
        if ((mPrivateFlags & AWAKEN_SCROLL_BARS_ON_ATTACH) != 0) {
            initialAwakenScrollBars();
            mPrivateFlags &= ~AWAKEN_SCROLL_BARS_ON_ATTACH;
        }
        jumpDrawablesToCurrentState();
        // Order is important here: LayoutDirection MUST be resolved before Padding
        // and TextDirection
        resolveLayoutDirectionIfNeeded();
        resolvePadding();
        resolveTextDirection();
        if (isFocused()) {
            InputMethodManager imm = InputMethodManager.peekInstance();
            imm.focusIn(this);
        }*/
    }

    public boolean isLayerTextureDirty() {
        return (mPrivateFlags & DIRTY_MASK) != 0;
    }

    protected void onManageLayerTexture() {
        mPrivateFlags = (mPrivateFlags & ~DIRTY_MASK) | DRAWN;

        String backgroundTexId = (mBGDrawable != null)?PREFIX_BACKGROUND + mBGDrawable.toString() + mBGDrawable.getState():PREFIX_BACKGROUND;
        String replaced = null;
        for (String id:getTextures().getIds()) {
            if (id.contains(PREFIX_BACKGROUND) && !id.equals(PREFIX_BACKGROUND)) {
                if (id.equals(backgroundTexId)) {
                    return;
                } else {
                    replaced = id;
                }
                break;
            }
        }

        if (replaced != null) {
            getGContext().getTexureManager().deleteTexture(replaced);
            getTextures().removeById(replaced);
        }

        if (mBGDrawable != null) {
            Drawable current = mBGDrawable.getCurrent();
            Bitmap bitmap = null;
            Bitmap bitmapNeedRecycled = null;
            if (current instanceof BitmapDrawable) {
                BitmapDrawable currentDrawable = (BitmapDrawable)current;
                bitmap = currentDrawable.getBitmap();
            } else if (current instanceof NinePatchDrawable) {
                float width = (getWidth() > 0) ? getWidth() : 1.0f;
                float height = (getHeight() > 0) ? getHeight() : 1.0f;
                float x = getGContext().getRenderer().getWorldPlaneSize(position().z).x;
                float ratio = x / getGContext().getRenderer().getWidth();
                NinePatchDrawable currentDrawable = (NinePatchDrawable)current.getCurrent();
                Rect rect = new Rect(0, 0, (int)(width / ratio), (int)(height / ratio));
                currentDrawable.setBounds(rect);
                bitmapNeedRecycled = Bitmap.createBitmap(rect.width(), rect.height(), Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmapNeedRecycled);
                currentDrawable.draw(canvas);
            }

            if (!getGContext().getTexureManager().contains(backgroundTexId)) {
                bitmap = (bitmapNeedRecycled != null) ? bitmapNeedRecycled : bitmap;
                if (bitmap != null) {
                    getGContext().getTexureManager().addTextureId(bitmap, backgroundTexId, false);
                }
            }

            if (isRenderCacheEnabled()) {
                mRenderingCache = bitmap;
            } else if (bitmapNeedRecycled != null && !bitmapNeedRecycled.isRecycled()) {
                bitmapNeedRecycled.recycle();
            }

            TextureVo textureVo = new TextureVo(backgroundTexId);
            textureVo.repeatU = false;
            textureVo.repeatV = false;
            getTextures().add(0, textureVo);
        }
    }

    protected Bitmap createTextureBitmap(int width, int height, Bitmap.Config config) {
        return null;
    }

    protected void rotateM(float[] m, int mOffset, float a, float x, float y, float z) {
        Matrix.setIdentityM(mRotateMatrixTmp, 0);
        Matrix.setRotateM(mRotateMatrixTmp, 0, a, x, y, z);
        System.arraycopy(m, 0, mTmpMatrix, 0, 16);
        Matrix.multiplyMM(m, mOffset, mTmpMatrix, mOffset, mRotateMatrixTmp, 0);
    }

    protected void setShaderParams() {
        if (scene() != null) {
            mMaterial.setLightList(scene().lights());
            mMaterial.setFog(scene().fogEnabled(), scene().fogColor(), scene().fogNear(), scene().fogFar(), scene().fogType() );
            mMaterial.setMaterialColorEnable(colorMaterialEnabled());
        }
    };

    void makeVertextBufferObject() {
        GLES20.glDeleteBuffers(VBO_ID.TOTAL.ordinal(), mBuffers, 0);
        GLES20.glGenBuffers(VBO_ID.TOTAL.ordinal(), mBuffers, 0);

        mVertices.getPoints().buffer().position(0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers[VBO_ID.POINT.ordinal()]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
                mVertices.getPoints().capacity() * Number3dBufferList.PROPERTIES_PER_ELEMENT * Number3dBufferList.BYTES_PER_PROPERTY,
                mVertices.getPoints().buffer(),
                GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers[VBO_ID.UV.ordinal()]);
        if (mVertices.getUvs() != null) {
            mVertices.getUvs().buffer().position(0);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
                    mVertices.getUvs().capacity() * UvBufferList.PROPERTIES_PER_ELEMENT * UvBufferList.BYTES_PER_PROPERTY,
                    mVertices.getUvs().buffer(),
                    GLES20.GL_STATIC_DRAW);
        }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers[VBO_ID.COLOR.ordinal()]);
        if (hasVertexColors() && vertexColorsEnabled() && mVertices.getColors() != null) {
            mVertices.getColors().buffer().position(0);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
                    mVertices.getColors().capacity() * Color4BufferList.PROPERTIES_PER_ELEMENT,
                    mVertices.getColors().buffer(),
                    GLES20.GL_STATIC_DRAW);
        } else if (mColors != null) {
            mColors.buffer().position(0);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
                    mColors.capacity() * Color4BufferList.PROPERTIES_PER_ELEMENT,
                    mColors.buffer(),
                    GLES20.GL_STATIC_DRAW);
        }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers[VBO_ID.NORMAL.ordinal()]);
        if (hasNormals() && normalsEnabled() && mVertices.getNormals() != null) {
            mVertices.getNormals().buffer().position(0);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
                    mVertices.getNormals().capacity() * Number3dBufferList.PROPERTIES_PER_ELEMENT * Number3dBufferList.BYTES_PER_PROPERTY,
                    mVertices.getNormals().buffer(),
                    GLES20.GL_STATIC_DRAW);
        }

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mBuffers[VBO_ID.FACE.ordinal()]);
        if (!ignoreFaces()) {
            mFaces.buffer().position(0);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER,
                    mFaces.capacity() * FacesBufferedList.PROPERTIES_PER_ELEMENT * 2,
                    mFaces.buffer(),
                    GLES20.GL_STATIC_DRAW);
        }
    }

    void containAABB() {
        FloatBuffer[] points = mAabbBuffer;
        points[0] = getVertices().getPoints().buffer();

        if (points == null || points.length == 0) {
            Log.i(TAG, "contain nothing, return!!!");
            return;
        }

        if (getVertices().size() > 0) {
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
        }

        calcAABBPos();
    }

    private static void populateFromBuffer(Number3d vector, FloatBuffer buf,
            int index) {
        vector.x = buf.get(index * 3);
        vector.y = buf.get(index * 3 + 1);
        vector.z = buf.get(index * 3 + 2);
    }

    private void calcAABBPos() {
        // Reserve vectors generation whatever vertices size is
        // because those will be referenced by children's contain box calculating.
        Matrix.setIdentityM(mTransMC, 0);
        Matrix.translateM(mTransMC, 0, mPosition.x, mPosition.y, mPosition.z);

        Matrix.setIdentityM(mRotMC, 0);

        Matrix.rotateM(mRotMC, 0, mRotation.x, 1, 0, 0);
        Matrix.rotateM(mRotMC, 0, mRotation.y, 0, 1, 0);
        Matrix.rotateM(mRotMC, 0, mRotation.z, 0, 0, 1);

        Matrix.setIdentityM(mScaleMC, 0);
        Matrix.scaleM(mScaleMC, 0, mScale.x, mScale.y, mScale.z);

        if (getVertices().size() > 0) {
            mResult[0] = mCenter.x;
            mResult[1] = mCenter.y;
            mResult[2] = mCenter.z;
            mResult[3] = 1;

            Matrix.multiplyMV(mResult, 0, mTransMC, 0, mResult, 0);

            if (mParent != null && mParent instanceof Object3d) {
                calcAABBPos((Object3d) parent(), TRANSLATE | ROTATE | SCALE, mResult);
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

            if (mParent != null && mParent instanceof Object3d) {
                accmlAABBTrans((Object3d) parent(), ROTATE, mAccmlR);
                accmlAABBTrans((Object3d) parent(), SCALE, mAccmlS);
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
        }

        if (DBG) Log.i(TAG, "Name:" + _name + " Center:" + mCenter + " Extent:"
                + mExtent);
    }

    private void calcAABBPos(Object3d parent, int mode, float[] result) {
        if (parent == null) {
            Log.w(TAG,"Ignore un-linked object. (In calcAABBPos)");
            return;
        }

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
        if (parent == null) {
            Log.w(TAG,"Ignore un-linked object. (In accmlAABBTrans)");
            return;
        }

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
        int visibility = getVisibility() & VISIBILITY_MASK;
        if (visibility == GONE) {
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
        mCoordinate = getIntersectPoint(event.getX(),event.getY(),mCenter.z);

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
            if (mOnTouchListener.onTouch(this, event, list, mCoordinate)) {
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
                            mUpList = (ArrayList<Object3d>) list.clone();
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
                                    mPerformClick = new PerformClick(mUpList, mCoordinate);
                                } else {
                                    mPerformClick.attachInfo(mUpList, mCoordinate);
                                }

                                if (!post(mPerformClick)) {
                                    performClick(mUpList, mCoordinate);
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
                    // Check whether this object appear in pick list.
                    if (!isExist(list)) return false;

                    mDownList = (ArrayList<Object3d>)list.clone();

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
                    if (!isExist(list)) {
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

    private boolean isExist(ArrayList<Object3d> list) {
        for (Object3d obj : list) {
            if (obj.equals(this)) {
                return true;
            }
        }
        return false;
    }

    private Number3d getIntersectPoint(float x, float y, float z) {
        int w = mGContext.getRenderer().getWidth();
        int h = mGContext.getRenderer().getHeight();
        float[] eye = new float[4];
        int[] viewport = { 0, 0, w, h };
        if (scene() != null) {
            FrustumManaged vf = scene().camera().frustum;
            float distance = scene().camera().position.z + z;
            float winZ = (1.0f / vf.zNear() - 1.0f / distance)
                    / (1.0f / vf.zNear() - 1.0f / vf.zFar());
            GLU.gluUnProject(x, h - y ,winZ, mGContext.getRenderer().getViewMatrix(), 0,
                    mGContext.getRenderer().getProjectMatrix(), 0, viewport, 0, eye, 0);
            if (eye[3] != 0) {
                eye[0] = eye[0] / eye[3];
                eye[1] = eye[1] / eye[3];
                eye[2] = eye[2] / eye[3];
            }
            return new Number3d(eye[0], eye[1], -eye[2]);
        }
        return null;
    }

    public final GContext getGContext() {
        return mGContext;
    }

    public void setWidth(float width) {
        if (width != mWidth) {
            mWidth = width;
            invalidate();
        }
    }

    public float getWidth() {
        return mWidth;
    }

    public void setHeight(float height) {
        if (height != mHeight) {
            mHeight = height;
            invalidate();
        }
    }

    public float getHeight() {
        return mHeight;
    }

    /**
     * True if this view has changed since the last time being drawn.
     *
     * @return The dirty state of this view.
     */
    public boolean isDirty() {
        return (mPrivateFlags & DIRTY_MASK) != 0;
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
        List<Object3d> mList;
        Number3d mCoord;

        public PerformClick(List<Object3d> list, Number3d coord) {
            mList = list;
            mCoord = coord;
        }

        public void attachInfo(List<Object3d> list, Number3d coord) {
            mList = list;
            mCoord = coord;
        }

        public void run() {
            performClick(mList,mCoord);
        }
    }

    /**
     * Call this object's OnClickListener, if it is defined.
     *
     * @return True there was an assigned OnClickListener that was called, false
     *         otherwise is returned.
     */
    public boolean performClick(List<Object3d> list, Number3d coord) {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(this,list,coord);
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
    public GLHandler getHandler() {
        if (mAttachInfo != null) {
            return mAttachInfo.mHandler;
        }
        return null;
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
        GLHandler handler = getHandler();
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
        GLHandler handler = getHandler();
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
        List<Object3d> mList;
        Number3d mCoord;

        public CheckForLongPress(List<Object3d> list, Number3d coord) {
            mList = list;
            mCoord = coord;
        }

        public void attachInfo(List<Object3d> list, Number3d coord) {
            mList = list;
            mCoord = coord;
        }

        public void run() {
            if (isPressed() && (parent() != null)
                    /*&& mOriginalWindowAttachCount == mWindowAttachCount*/) {
                if (performLongClick(mList,mCoord)) {
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
    public boolean performLongClick(List<Object3d> list, Number3d coord) {
        boolean handled = false;
        if (mOnLongClickListener != null) {
            handled = mOnLongClickListener.onLongClick(this,list,coord);
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

            mLongClickList = (ArrayList<Object3d>)mDownList.clone();

            if (mPendingCheckForLongPress == null) {
                mPendingCheckForLongPress = new CheckForLongPress(mLongClickList,mCoordinate);
            } else {
                mPendingCheckForLongPress.attachInfo(mLongClickList,mCoordinate);
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

        int privateFlags = mPrivateFlags;

        /* Check if the FOCUSABLE bit has changed */
        if (((changed & FOCUSABLE_MASK) != 0) &&
                ((privateFlags & HAS_BOUNDS) !=0)) {
            if (((old & FOCUSABLE_MASK) == FOCUSABLE)
                    && ((privateFlags & FOCUSED) != 0)) {
                /* Give up focus if we are no longer focusable */
                clearFocus();
            } else if (((old & FOCUSABLE_MASK) == NOT_FOCUSABLE)
                    && ((privateFlags & FOCUSED) == 0)) {
                /*
                 * Tell the view system that we are now available to take focus
                 * if no one else already has it.
                 */
                if (mParent != null) mParent.focusableObjectAvailable(this);
            }
        }

        if ((flags & VISIBILITY_MASK) == VISIBLE) {
            if ((changed & VISIBILITY_MASK) != 0) {
                // a view becoming visible is worth notifying the parent
                // about in case nothing has focus.  even if this specific view
                // isn't focusable, it may contain something that is, so let
                // the root view try to give this focus if nothing else does.
                if ((mParent != null)/* && (mBottom > mTop) && (mRight > mLeft)*/) {
                    mParent.focusableObjectAvailable(this);
                }
            }
        }

        /* Check if the GONE bit has changed */
        if ((changed & GONE) != 0) {
/*            requestLayout();*/

            if (((mViewFlags & VISIBILITY_MASK) == GONE)) {
                if (hasFocus()) clearFocus();
/*                destroyDrawingCache();*/
                // Mark the view drawn to ensure that it gets invalidated properly the next
                // time it is visible and gets invalidated
                mPrivateFlags |= DRAWN;
            }
        }

        /* Check if the VISIBLE bit has changed */
        if ((changed & INVISIBLE) != 0) {
            if (((mViewFlags & VISIBILITY_MASK) == INVISIBLE)) {
                if (hasFocus()) {
                    // root view becoming invisible shouldn't clear focus
                    Scene scene = mGContext.getRenderer().getScene();
                    if (scene.getRootObjectContainer() != this) {
                        clearFocus();
                    }
                }
            }
        }
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

    /**
     * Sets the background color for this view.
     * @param color the color of the background
     */
    public void setBackgroundColor(int color) {
        if (mBGDrawable instanceof ColorDrawable) {
            ((ColorDrawable) mBGDrawable).setColor(color);
        } else {
            setBackgroundDrawable(new ColorDrawable(color));
        }
    }

    /**
     * Set the background to a given resource. The resource should refer to
     * a Drawable object or 0 to remove the background.
     * @param resid The identifier of the resource.
     */
    public void setBackgroundResource(int resid) {
        if (resid != 0 && resid == mBackgroundResource) {
            return;
        }

        Drawable d= null;
        if (resid != 0) {
            d = mGContext.getContext().getResources().getDrawable(resid);
        }
        setBackgroundDrawable(d);

        mBackgroundResource = resid;
    }

    /**
     * Invalidate the whole view. If the view is visible,
     * {@link #onDraw(android.graphics.Canvas)} will be called at some point in
     * the future. This must be called from a UI thread. To call from a non-UI thread,
     * call {@link #postInvalidate()}.
     */
    public void invalidate() {
        invalidate(true);
    }

    /**
     * This is where the invalidate() work actually happens. A full invalidate()
     * causes the drawing cache to be invalidated, but this function can be called with
     * invalidateCache set to false to skip that invalidation step for cases that do not
     * need it (for example, a component that remains at the same dimensions with the same
     * content).
     *
     * @param invalidateCache Whether the drawing cache for this view should be invalidated as
     * well. This is usually true for a full invalidate, but may be set to false if the
     * View's contents or dimensions have not changed.
     */
    void invalidate(boolean invalidateCache) {
        mPrivateFlags &= ~DRAWN;
        mPrivateFlags |= DIRTY;
    }

    /**
     * Called by a parent to request that a child update its values for mScrollX
     * and mScrollY if necessary. This will typically be done if the child is
     * animating a scroll using a {@link android.widget.Scroller Scroller}
     * object.
     */
    public void computeScroll() {
    }

    /**
     * Set the background to a given Drawable, or remove the background. If the
     * background has padding, this View's padding is set to the background's
     * padding. However, when a background is removed, this View's padding isn't
     * touched. If setting the padding is desired, please use
     * {@link #setPadding(int, int, int, int)}.
     *
     * @param d The Drawable to use as the background, or null to remove the
     *        background
     */
    public void setBackgroundDrawable(Drawable d) {
        if (d == mBGDrawable) {
            return;
        }

        boolean requestLayout = false;

        mBackgroundResource = 0;

        /*
         * Regardless of whether we're setting a new background or not, we want
         * to clear the previous drawable.
         */
        if (mBGDrawable != null) {
            mBGDrawable.setCallback(null);
            //unscheduleDrawable(mBGDrawable);
        }

        String backgroundTexId = (mBGDrawable != null)?PREFIX_BACKGROUND + mBGDrawable.toString() + mBGDrawable.getState():PREFIX_BACKGROUND;
        if (d != null) {
            getGContext().getTexureManager().cancelTextureDeletion(backgroundTexId);
            // Compare the minimum sizes of the old Drawable and the new.  If there isn't an old or
            // if it has a different minimum size, we should layout again
            if (mBGDrawable == null || mBGDrawable.getMinimumHeight() != d.getMinimumHeight() ||
                    mBGDrawable.getMinimumWidth() != d.getMinimumWidth()) {
                requestLayout = true;
            }

            d.setCallback(this);
            if (d.isStateful()) {
                d.setState(getDrawableState());
            }
            d.setVisible(getVisibility() == VISIBLE, false);
            mBGDrawable = d;
/*            if ((mPrivateFlags & SKIP_DRAW) != 0) {
                mPrivateFlags &= ~SKIP_DRAW;
                mPrivateFlags |= ONLY_DRAWS_BACKGROUND;
                requestLayout = true;
            }*/
        } else {
            getGContext().getTexureManager().scheduleTextureDeletion(backgroundTexId);
            if (getTextures().getById(backgroundTexId) != null) {
                getTextures().removeById(backgroundTexId);
            }

            /* Remove the background */
            mBGDrawable = null;

            /*
             * When the background is set, we try to apply its padding to this
             * View. When the background is removed, we don't touch this View's
             * padding. This is noted in the Javadocs. Hence, we don't need to
             * requestLayout(), the invalidate() below is sufficient.
             */

            // The old background's minimum size could have affected this
            // View's layout, so let's requestLayout
            requestLayout = true;
        }

/*        if (requestLayout) {
            requestLayout();
        }*/

        mBackgroundSizeChanged = true;
        invalidate(true);
    }

    /**
     * Gets the background drawable
     * @return The drawable used as the background for this view, if any.
     */
    public Drawable getBackground() {
        return mBGDrawable;
    }

    /**
     * This function is called whenever the state of the view changes in such
     * a way that it impacts the state of drawables being shown.
     *
     * <p>Be sure to call through to the superclass when overriding this
     * function.
     *
     * @see Drawable#setState(int[])
     */
    protected void drawableStateChanged() {
        Drawable d = mBGDrawable;
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
    }

    /**
     * Call this to force a view to update its drawable state. This will cause
     * drawableStateChanged to be called on this view. Views that are interested
     * in the new state should call getDrawableState.
     *
     * @see #drawableStateChanged
     * @see #getDrawableState
     */
    public void refreshDrawableState() {
        mPrivateFlags |= DRAWABLE_STATE_DIRTY;
        drawableStateChanged();

        Object3dContainer parent = (Object3dContainer)parent();
        if (parent != null) {
            parent.childDrawableStateChanged(this);
        }
    }

    /**
     * Return an array of resource IDs of the drawable states representing the
     * current state of the view.
     *
     * @return The current drawable state
     *
     * @see Drawable#setState(int[])
     * @see #drawableStateChanged()
     * @see #onCreateDrawableState(int)
     */
    public final int[] getDrawableState() {
        if ((mDrawableState != null) && ((mPrivateFlags & DRAWABLE_STATE_DIRTY) == 0)) {
            return mDrawableState;
        } else {
            mDrawableState = onCreateDrawableState(0);
            mPrivateFlags &= ~DRAWABLE_STATE_DIRTY;
            return mDrawableState;
        }
    }

    /**
     * Generate the new {@link android.graphics.drawable.Drawable} state for
     * this view. This is called by the view
     * system when the cached Drawable state is determined to be invalid.  To
     * retrieve the current state, you should use {@link #getDrawableState}.
     *
     * @param extraSpace if non-zero, this is the number of extra entries you
     * would like in the returned array in w\\
     *  you can place your own
     * states.
     *
     * @return Returns an array holding the current {@link Drawable} state of
     * the view.
     *
     * @see #mergeDrawableStates(int[], int[])
     */
    protected int[] onCreateDrawableState(int extraSpace) {
        if ((mViewFlags & DUPLICATE_PARENT_STATE) == DUPLICATE_PARENT_STATE &&
                parent() instanceof Object3d) {
            return ((Object3d) parent()).onCreateDrawableState(extraSpace);
        }

        int[] drawableState;

        int privateFlags = mPrivateFlags;

        int viewStateIndex = 0;
        if ((privateFlags & PRESSED) != 0) viewStateIndex |= VIEW_STATE_PRESSED;
        if ((mViewFlags & ENABLED_MASK) == ENABLED) viewStateIndex |= VIEW_STATE_ENABLED;
        if (isFocused()) viewStateIndex |= VIEW_STATE_FOCUSED;
        if ((privateFlags & SELECTED) != 0) viewStateIndex |= VIEW_STATE_SELECTED;
        if ((privateFlags & ACTIVATED) != 0) viewStateIndex |= VIEW_STATE_ACTIVATED;
        if ((privateFlags & HOVERED) != 0) viewStateIndex |= VIEW_STATE_HOVERED;

        final int privateFlags2 = mPrivateFlags2;
        if ((privateFlags2 & DRAG_CAN_ACCEPT) != 0) viewStateIndex |= VIEW_STATE_DRAG_CAN_ACCEPT;
        if ((privateFlags2 & DRAG_HOVERED) != 0) viewStateIndex |= VIEW_STATE_DRAG_HOVERED;

        drawableState = VIEW_STATE_SETS[viewStateIndex];

        //noinspection ConstantIfStatement
        if (false) {
            Log.i("View", "drawableStateIndex=" + viewStateIndex);
            Log.i("View", toString()
                    + " pressed=" + ((privateFlags & PRESSED) != 0)
                    + " en=" + ((mViewFlags & ENABLED_MASK) == ENABLED)
                    + " fo=" + hasFocus()
                    + " sl=" + ((privateFlags & SELECTED) != 0)
                    + ": " + Arrays.toString(drawableState));
        }

        if (extraSpace == 0) {
            return drawableState;
        }

        final int[] fullState;
        if (drawableState != null) {
            fullState = new int[drawableState.length + extraSpace];
            System.arraycopy(drawableState, 0, fullState, 0, drawableState.length);
        } else {
            fullState = new int[extraSpace];
        }

        return fullState;
    }

    /**
     * Merge your own state values in <var>additionalState</var> into the base
     * state values <var>baseState</var> that were returned by
     * {@link #onCreateDrawableState(int)}.
     *
     * @param baseState The base state values returned by
     * {@link #onCreateDrawableState(int)}, which will be modified to also hold your
     * own additional state values.
     *
     * @param additionalState The additional state values you would like
     * added to <var>baseState</var>; this array is not modified.
     *
     * @return As a convenience, the <var>baseState</var> array you originally
     * passed into the function is returned.
     *
     * @see #onCreateDrawableState(int)
     */
    protected static int[] mergeDrawableStates(int[] baseState, int[] additionalState) {
        final int N = baseState.length;
        int i = N - 1;
        while (i >= 0 && baseState[i] == 0) {
            i--;
        }
        System.arraycopy(additionalState, 0, baseState, i + 1, additionalState.length);
        return baseState;
    }

    /**
     * Call {@link Drawable#jumpToCurrentState() Drawable.jumpToCurrentState()}
     * on all Drawable objects associated with this view.
     */
    public void jumpDrawablesToCurrentState() {
        if (mBGDrawable != null) {
            mBGDrawable.jumpToCurrentState();
        }
    }

    /**
     * Register a callback to be invoked when focus of this view changed.
     *
     * @param l The callback that will run.
     */
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        mOnFocusChangeListener = l;
    }

    /**
     * Invoked whenever this view loses focus, either by losing window focus or by losing
     * focus within its window. This method can be used to clear any state tied to the
     * focus. For instance, if a button is held pressed with the trackball and the window
     * loses focus, this method can be used to cancel the press.
     *
     * Subclasses of View overriding this method should always call super.onFocusLost().
     *
     * @see #onFocusChanged(boolean, int, android.graphics.Rect)
     * @see #onWindowFocusChanged(boolean)
     *
     * @hide pending API council approval
     */
    protected void onFocusLost() {
        resetPressedState();
    }

    private void resetPressedState() {
        if ((mViewFlags & ENABLED_MASK) == DISABLED) {
            return;
        }

        if (isPressed()) {
            setPressed(false);

            if (!mHasPerformedLongPress) {
                removeLongPressCallback();
            }
        }
    }

    /**
     * Returns the visibility status for this view.
     *
     * @return One of {@link #VISIBLE}, {@link #INVISIBLE}, or {@link #GONE}.
     */
    public int getVisibility() {
        return mViewFlags & VISIBILITY_MASK;
    }

    /**
     * Set the enabled state of this view.
     *
     * @param visibility One of {@link #VISIBLE}, {@link #INVISIBLE}, or {@link #GONE}.
     */
    public void setVisibility(int visibility) {
        setFlags(visibility, VISIBILITY_MASK);
        if (mBGDrawable != null) mBGDrawable.setVisible(visibility == VISIBLE, false);
    }

    /**
     * Returns the enabled status for this view. The interpretation of the
     * enabled state varies by subclass.
     *
     * @return True if this view is enabled, false otherwise.
     */
    public boolean isEnabled() {
        return (mViewFlags & ENABLED_MASK) == ENABLED;
    }

    /**
     * Set the enabled state of this view. The interpretation of the enabled
     * state varies by subclass.
     *
     * @param enabled True if this view is enabled, false otherwise.
     */
    public void setEnabled(boolean enabled) {
        if (enabled == isEnabled()) return;

        setFlags(enabled ? ENABLED : DISABLED, ENABLED_MASK);

        /*
         * The View most likely has to change its appearance, so refresh
         * the drawable state.
         */
        refreshDrawableState();

        // Invalidate too, since the default behavior for views is to be
        // be drawn at 50% alpha rather than to change the drawable.
        invalidate(true);
    }

    /**
     * Returns true if this view has focus
     *
     * @return True if this view has focus, false otherwise.
     */
    public boolean isFocused() {
        return (mPrivateFlags & FOCUSED) != 0;
    }

    /**
     * Find the view in the hierarchy rooted at this view that currently has
     * focus.
     *
     * @return The view that currently has focus, or null if no focused view can
     *         be found.
     */
    public Object3d findFocus() {
        return (mPrivateFlags & FOCUSED) != 0 ? this : null;
    }

    /**
     * Set whether this view can receive the focus.
     *
     * Setting this to false will also ensure that this view is not focusable
     * in touch mode.
     *
     * @param focusable If true, this view can receive the focus.
     *
     * @see #setFocusableInTouchMode(boolean)
     */
    public void setFocusable(boolean focusable) {
        if (!focusable) {
            setFlags(0, FOCUSABLE_IN_TOUCH_MODE);
        }
        setFlags(focusable ? FOCUSABLE : NOT_FOCUSABLE, FOCUSABLE_MASK);
    }

    /**
     * Set whether this view can receive focus while in touch mode.
     *
     * Setting this to true will also ensure that this view is focusable.
     *
     * @param focusableInTouchMode If true, this view can receive the focus while
     *   in touch mode.
     *
     * @see #setFocusable(boolean)
     */
    public void setFocusableInTouchMode(boolean focusableInTouchMode) {
        // Focusable in touch mode should always be set before the focusable flag
        // otherwise, setting the focusable flag will trigger a focusableViewAvailable()
        // which, in touch mode, will not successfully request focus on this view
        // because the focusable in touch mode flag is not set
        setFlags(focusableInTouchMode ? FOCUSABLE_IN_TOUCH_MODE : 0, FOCUSABLE_IN_TOUCH_MODE);
        if (focusableInTouchMode) {
            setFlags(FOCUSABLE, FOCUSABLE_MASK);
        }
    }

    /**
     * Called when this view wants to give up focus. This will cause
     * {@link #onFocusChanged(boolean, int, android.graphics.Rect)} to be called.
     */
    public void clearFocus() {
        if ((mPrivateFlags & FOCUSED) != 0) {
            mPrivateFlags &= ~FOCUSED;

            if (parent() != null) {
                parent().clearChildFocus(this);
            }

            onFocusChanged(false, 0, null);
            refreshDrawableState();
        }
    }

    /**
     * Called to clear the focus of a view that is about to be removed.
     * Doesn't call clearChildFocus, which prevents this view from taking
     * focus again before it has been removed from the parent
     */
    void clearFocusForRemoval() {
        if ((mPrivateFlags & FOCUSED) != 0) {
            mPrivateFlags &= ~FOCUSED;

            onFocusChanged(false, 0, null);
            refreshDrawableState();
        }
    }

    /**
     * Called internally by the view system when a new view is getting focus.
     * This is what clears the old focus.
     */
    void unFocus() {
        if (DBG) {
            System.out.println(this + " unFocus()");
        }

        if ((mPrivateFlags & FOCUSED) != 0) {
            mPrivateFlags &= ~FOCUSED;

            onFocusChanged(false, 0, null);
            refreshDrawableState();
        }
    }

    /**
     * Returns true if this view has focus iteself, or is the ancestor of the
     * view that has focus.
     *
     * @return True if this view has or contains focus, false otherwise.
     */
    public boolean hasFocus() {
        return (mPrivateFlags & FOCUSED) != 0;
    }

    /**
     * Returns true if this view is focusable or if it contains a reachable View
     * for which {@link #hasFocusable()} returns true. A "reachable hasFocusable()"
     * is a View whose parents do not block descendants focus.
     *
     * Only {@link #VISIBLE} views are considered focusable.
     *
     * @return True if the view is focusable or if the view contains a focusable
     *         View, false otherwise.
     *
     * @see ViewGroup#FOCUS_BLOCK_DESCENDANTS
     */
    public boolean hasFocusable() {
        return (mViewFlags & VISIBILITY_MASK) == VISIBLE && isFocusable();
    }

    /**
     * Called by the view system when the focus state of this view changes.
     * When the focus change event is caused by directional navigation, direction
     * and previouslyFocusedRect provide insight into where the focus is coming from.
     * When overriding, be sure to call up through to the super class so that
     * the standard focus handling will occur.
     *
     * @param gainFocus True if the View has focus; false otherwise.
     * @param direction The direction focus has moved when requestFocus()
     *                  is called to give this view focus. Values are
     *                  {@link #FOCUS_UP}, {@link #FOCUS_DOWN}, {@link #FOCUS_LEFT},
     *                  {@link #FOCUS_RIGHT}, {@link #FOCUS_FORWARD}, or {@link #FOCUS_BACKWARD}.
     *                  It may not always apply, in which case use the default.
     * @param previouslyFocusedRect The rectangle, in this view's coordinate
     *        system, of the previously focused view.  If applicable, this will be
     *        passed in as finer grained information about where the focus is coming
     *        from (in addition to direction).  Will be <code>null</code> otherwise.
     */
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        if (!gainFocus) {
            if (isPressed()) {
                setPressed(false);
            }
            onFocusLost();
        }

        invalidate(true);
        if (mOnFocusChangeListener != null) {
            mOnFocusChangeListener.onFocusChange(this, gainFocus);
        }
    }

    /**
     * Returns whether this View is able to take focus.
     *
     * @return True if this view can take focus, or false otherwise.
     */
    public final boolean isFocusable() {
        return FOCUSABLE == (mViewFlags & FOCUSABLE_MASK);
    }

    /**
     * When a view is focusable, it may not want to take focus when in touch mode.
     * For example, a button would like focus when the user is navigating via a D-pad
     * so that the user can click on it, but once the user starts touching the screen,
     * the button shouldn't take focus
     * @return Whether the view is focusable in touch mode.
     */
    public final boolean isFocusableInTouchMode() {
        return FOCUSABLE_IN_TOUCH_MODE == (mViewFlags & FOCUSABLE_IN_TOUCH_MODE);
    }

    /**
     * Find the nearest view in the specified direction that can take focus.
     * This does not actually give focus to that view.
     *
     * @param direction One of FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, and FOCUS_RIGHT
     *
     * @return The nearest focusable in the specified direction, or null if none
     *         can be found.
     */
    public Object3d focusSearch(int direction) {
        if (parent() != null) {
            return parent().focusSearch(this, direction);
        } else {
            return null;
        }
    }

    /**
     * Find and return all touchable views that are descendants of this view,
     * possibly including this view if it is touchable itself.
     *
     * @return A list of touchable views
     */
    public ArrayList<Object3d> getTouchables() {
        ArrayList<Object3d> result = new ArrayList<Object3d>();
        addTouchables(result);
        return result;
    }

    /**
     * Add any touchable views that are descendants of this view (possibly
     * including this view if it is touchable itself) to views.
     *
     * @param views Touchable views found so far
     */
    public void addTouchables(ArrayList<Object3d> views) {
        final int viewFlags = mViewFlags;

        if (((viewFlags & CLICKABLE) == CLICKABLE || (viewFlags & LONG_CLICKABLE) == LONG_CLICKABLE)
                && (viewFlags & ENABLED_MASK) == ENABLED) {
            views.add(this);
        }
    }

    /**
     * Call this to try to give focus to a specific view or to one of its
     * descendants.
     *
     * A view will not actually take focus if it is not focusable ({@link #isFocusable} returns
     * false), or if it is focusable and it is not focusable in touch mode
     * ({@link #isFocusableInTouchMode}) while the device is in touch mode.
     *
     * See also {@link #focusSearch(int)}, which is what you call to say that you
     * have focus, and you want your parent to look for the next one.
     *
     * This is equivalent to calling {@link #requestFocus(int, Rect)} with arguments
     * {@link #FOCUS_DOWN} and <code>null</code>.
     *
     * @return Whether this view or one of its descendants actually took focus.
     */
    public final boolean requestFocus() {
        return requestFocus(Object3d.FOCUS_DOWN);
    }


    /**
     * Call this to try to give focus to a specific view or to one of its
     * descendants and give it a hint about what direction focus is heading.
     *
     * A view will not actually take focus if it is not focusable ({@link #isFocusable} returns
     * false), or if it is focusable and it is not focusable in touch mode
     * ({@link #isFocusableInTouchMode}) while the device is in touch mode.
     *
     * See also {@link #focusSearch(int)}, which is what you call to say that you
     * have focus, and you want your parent to look for the next one.
     *
     * This is equivalent to calling {@link #requestFocus(int, Rect)} with
     * <code>null</code> set for the previously focused rectangle.
     *
     * @param direction One of FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, and FOCUS_RIGHT
     * @return Whether this view or one of its descendants actually took focus.
     */
    public final boolean requestFocus(int direction) {
        return requestFocus(direction, null);
    }

    /**
     * Call this to try to give focus to a specific view or to one of its descendants
     * and give it hints about the direction and a specific rectangle that the focus
     * is coming from.  The rectangle can help give larger views a finer grained hint
     * about where focus is coming from, and therefore, where to show selection, or
     * forward focus change internally.
     *
     * A view will not actually take focus if it is not focusable ({@link #isFocusable} returns
     * false), or if it is focusable and it is not focusable in touch mode
     * ({@link #isFocusableInTouchMode}) while the device is in touch mode.
     *
     * A View will not take focus if it is not visible.
     *
     * A View will not take focus if one of its parents has
     * {@link android.view.ViewGroup#getDescendantFocusability()} equal to
     * {@link ViewGroup#FOCUS_BLOCK_DESCENDANTS}.
     *
     * See also {@link #focusSearch(int)}, which is what you call to say that you
     * have focus, and you want your parent to look for the next one.
     *
     * You may wish to override this method if your custom {@link View} has an internal
     * {@link View} that it wishes to forward the request to.
     *
     * @param direction One of FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, and FOCUS_RIGHT
     * @param previouslyFocusedRect The rectangle (in this View's coordinate system)
     *        to give a finer grained hint about where focus is coming from.  May be null
     *        if there is no hint.
     * @return Whether this view or one of its descendants actually took focus.
     */
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        if (DBG) {
            System.out.println(this + " View.requestFocus direction="
                    + direction + " Name:" + name());
        }
        // need to be focusable
        if ((mViewFlags & FOCUSABLE_MASK) != FOCUSABLE ||
                (mViewFlags & VISIBILITY_MASK) != VISIBLE) {
            return false;
        }

        // need to be focusable in touch mode if in touch mode
        if (isInTouchMode() &&
            (FOCUSABLE_IN_TOUCH_MODE != (mViewFlags & FOCUSABLE_IN_TOUCH_MODE))) {
               return false;
        }

        // need to not have any parents blocking us
        if (hasAncestorThatBlocksDescendantFocus()) {
            return false;
        }

        handleFocusGainInternal(direction, previouslyFocusedRect);
        return true;
    }

    /**
     * Returns whether the device is currently in touch mode.  Touch mode is entered
     * once the user begins interacting with the device by touch, and affects various
     * things like whether focus is always visible to the user.
     *
     * @return Whether the device is in touch mode.
     */
    public boolean isInTouchMode() {
        return getGContext().getRenderer().getScene().isInTouchMode();
    }

    /**
     * Give this view focus. This will cause
     * {@link #onFocusChanged(boolean, int, android.graphics.Rect)} to be called.
     *
     * Note: this does not check whether this {@link View} should get focus, it just
     * gives it focus no matter what.  It should only be called internally by framework
     * code that knows what it is doing, namely {@link #requestFocus(int, Rect)}.
     *
     * @param direction values are {@link View#FOCUS_UP}, {@link View#FOCUS_DOWN},
     *        {@link View#FOCUS_LEFT} or {@link View#FOCUS_RIGHT}. This is the direction which
     *        focus moved when requestFocus() is called. It may not always
     *        apply, in which case use the default View.FOCUS_DOWN.
     * @param previouslyFocusedRect The rectangle of the view that had focus
     *        prior in this View's coordinate system.
     */
    void handleFocusGainInternal(int direction, Rect previouslyFocusedRect) {
        if (DBG) {
            System.out.println(this + " requestFocus()");
        }

        if ((mPrivateFlags & FOCUSED) == 0) {
            mPrivateFlags |= FOCUSED;

            if (parent() != null) {
                parent().requestChildFocus(this, this);
            }

            onFocusChanged(true, direction, previouslyFocusedRect);
            refreshDrawableState();
            invalidate(true);
        }
    }

    /**
     * Call this to try to give focus to a specific view or to one of its descendants. This is a
     * special variant of {@link #requestFocus() } that will allow views that are not focuable in
     * touch mode to request focus when they are touched.
     *
     * @return Whether this view or one of its descendants actually took focus.
     *
     * @see #isInTouchMode()
     *
     */
    public final boolean requestFocusFromTouch() {
        // Leave touch mode if we need to
        if (isInTouchMode()) {
            Scene viewRoot = mGContext.getRenderer().getScene();
            if (viewRoot != null) {
                viewRoot.ensureTouchMode(false);
            }
        }
        return requestFocus(Object3d.FOCUS_DOWN);
    }

    /**
     * @return Whether any ancestor of this view blocks descendant focus.
     */
    private boolean hasAncestorThatBlocksDescendantFocus() {
        IObject3dParent ancestor = mParent;
        while (ancestor instanceof Object3dContainer) {
            final Object3dContainer vgAncestor = (Object3dContainer) ancestor;
            if (vgAncestor.getDescendantFocusability() == Object3dContainer.FOCUS_BLOCK_DESCENDANTS) {
                return true;
            } else {
                ancestor = vgAncestor.getParent();
            }
        }
        return false;
    }

    /**
     * Find and return all focusable views that are descendants of this view,
     * possibly including this view if it is focusable itself.
     *
     * @param direction The direction of the focus
     * @return A list of focusable views
     */
    public ArrayList<Object3d> getFocusables(int direction) {
        ArrayList<Object3d> result = new ArrayList<Object3d>(24);
        addFocusables(result, direction);
        return result;
    }

    /**
     * Add any focusable views that are descendants of this view (possibly
     * including this view if it is focusable itself) to views.  If we are in touch mode,
     * only add views that are also focusable in touch mode.
     *
     * @param views Focusable views found so far
     * @param direction The direction of the focus
     */
    public void addFocusables(ArrayList<Object3d> views, int direction) {
        addFocusables(views, direction, FOCUSABLES_TOUCH_MODE);
    }

    /**
     * Adds any focusable views that are descendants of this view (possibly
     * including this view if it is focusable itself) to views. This method
     * adds all focusable views regardless if we are in touch mode or
     * only views focusable in touch mode if we are in touch mode depending on
     * the focusable mode paramater.
     *
     * @param views Focusable views found so far or null if all we are interested is
     *        the number of focusables.
     * @param direction The direction of the focus.
     * @param focusableMode The type of focusables to be added.
     *
     * @see #FOCUSABLES_ALL
     * @see #FOCUSABLES_TOUCH_MODE
     */
    public void addFocusables(ArrayList<Object3d> views, int direction, int focusableMode) {
        if (!isFocusable()) {
            return;
        }

        if ((focusableMode & FOCUSABLES_TOUCH_MODE) == FOCUSABLES_TOUCH_MODE &&
                isInTouchMode() && !isFocusableInTouchMode()) {
            return;
        }

        if (views != null) {
            views.add(this);
        }
    }

    /**
     * {@hide}
     *
     * @return true if the view belongs to the root namespace, false otherwise
     */
    public boolean isRootNamespace() {
        return (mPrivateFlags&IS_ROOT_NAMESPACE) != 0;
    }

    /**
     * Changes the selection state of this view. A view can be selected or not.
     * Note that selection is not the same as focus. Views are typically
     * selected in the context of an AdapterView like ListView or GridView;
     * the selected view is the view that is highlighted.
     *
     * @param selected true if the view must be selected, false otherwise
     */
    public void setSelected(boolean selected) {
        if (((mPrivateFlags & SELECTED) != 0) != selected) {
            mPrivateFlags = (mPrivateFlags & ~SELECTED) | (selected ? SELECTED : 0);
            if (!selected) resetPressedState();
            refreshDrawableState();
            invalidate(true);
            dispatchSetSelected(selected);
        }
    }

    /**
     * Indicates the selection state of this view.
     *
     * @return true if the view is selected, false otherwise
     */
    public boolean isSelected() {
        return (mPrivateFlags & SELECTED) != 0;
    }

    /**
     * Dispatch setSelected to all of this View's children.
     *
     * @see #setSelected(boolean)
     *
     * @param selected The new selected state
     */
    protected void dispatchSetSelected(boolean selected) {
    }

    /**
     * <p>Enables or disables the duplication of the parent's state into this view. When
     * duplication is enabled, this view gets its drawable state from its parent rather
     * than from its own internal properties.</p>
     *
     * <p>Note: in the current implementation, setting this property to true after the
     * view was added to a ViewGroup might have no effect at all. This property should
     * always be used from XML or set to true before adding this view to a ViewGroup.</p>
     *
     * <p>Note: if this view's parent addStateFromChildren property is enabled and this
     * property is enabled, an exception will be thrown.</p>
     *
     * <p>Note: if the child view uses and updates additionnal states which are unknown to the
     * parent, these states should not be affected by this method.</p>
     *
     * @param enabled True to enable duplication of the parent's drawable state, false
     *                to disable it.
     *
     * @see #getDrawableState()
     * @see #isDuplicateParentStateEnabled()
     */
    public void setDuplicateParentStateEnabled(boolean enabled) {
        setFlags(enabled ? DUPLICATE_PARENT_STATE : 0, DUPLICATE_PARENT_STATE);
    }

    /**
     * <p>Indicates whether this duplicates its drawable state from its parent.</p>
     *
     * @return True if this view's drawable state is duplicated from the parent,
     *         false otherwise
     *
     * @see #getDrawableState()
     * @see #setDuplicateParentStateEnabled(boolean)
     */
    public boolean isDuplicateParentStateEnabled() {
        return (mViewFlags & DUPLICATE_PARENT_STATE) == DUPLICATE_PARENT_STATE;
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        invalidate();
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
    }

    /**
     * Call this when something has changed which has invalidated the
     * layout of this view. This will schedule a layout pass of the view
     * tree.
     */
    public void requestLayout() {
        mPrivateFlags |= FORCE_LAYOUT;
/*        mPrivateFlags |= INVALIDATED;

        if (mParent != null) {
            if (mLayoutParams != null) {
                mLayoutParams.resolveWithDirection(getResolvedLayoutDirection());
            }
            if (!mParent.isLayoutRequested()) {
                mParent.requestLayout();
            }
        }*/
    }

    /**
     * Forces this view to be laid out during the next layout pass.
     * This method does not call requestLayout() or forceLayout()
     * on the parent.
     */
    public void forceLayout() {
        mPrivateFlags |= FORCE_LAYOUT;
/*        mPrivateFlags |= INVALIDATED;*/
    }

    /**
     * <p>Indicates whether or not this view's layout will be requested during
     * the next hierarchy layout pass.</p>
     *
     * @return true if the layout will be forced during next layout pass
     */
    public boolean isLayoutRequested() {
        return (mPrivateFlags & FORCE_LAYOUT) == FORCE_LAYOUT;
    }

    public void layout(/*int l, int t, int r, int b*/) {
/*        int oldL = mLeft;
        int oldT = mTop;
        int oldB = mBottom;
        int oldR = mRight;
        boolean changed = setFrame(l, t, r, b);
        if (changed || (mPrivateFlags & LAYOUT_REQUIRED) == LAYOUT_REQUIRED) {
            if (ViewDebug.TRACE_HIERARCHY) {
                ViewDebug.trace(this, ViewDebug.HierarchyTraceType.ON_LAYOUT);
            }

            onLayout(changed, l, t, r, b);
            mPrivateFlags &= ~LAYOUT_REQUIRED;

            ListenerInfo li = mListenerInfo;
            if (li != null && li.mOnLayoutChangeListeners != null) {
                ArrayList<OnLayoutChangeListener> listenersCopy =
                        (ArrayList<OnLayoutChangeListener>)li.mOnLayoutChangeListeners.clone();
                int numListeners = listenersCopy.size();
                for (int i = 0; i < numListeners; ++i) {
                    listenersCopy.get(i).onLayoutChange(this, l, t, r, b, oldL, oldT, oldR, oldB);
                }
            }
        }*/
        mPrivateFlags &= ~FORCE_LAYOUT;
    }

    /**
     * <p>Enables or disables the drawing cache. When the rendering cache is enabled, the next call
     * to {@link #getRenderingCache()} or {@link #layout()} will draw the view in a
     * bitmap. Calling {@link #draw(android.graphics.Canvas)} will not draw from the cache when
     * the cache is enabled. To benefit from the cache, you must request the rendering cache by
     * calling {@link #getRenderingCache()} and draw it on screen if the returned bitmap is not
     * null.</p>
     *
     * <p>Enabling the rendering cache is similar to
     * {@link #setLayerType(int, android.graphics.Paint) setting a layer} when hardware
     * acceleration is turned off. When hardware acceleration is turned on, enabling the
     * rendering cache has no effect on rendering because the system uses a different mechanism
     * for acceleration which ignores the flag. If you want to use a Bitmap for the view, even
     * when hardware acceleration is enabled, see {@link #setLayerType(int, android.graphics.Paint)}
     * for information on how to enable software and hardware layers.</p>
     *
     * <p>This API can be used to manually generate
     * a bitmap copy of this view, by setting the flag to <code>true</code> and calling
     * {@link #getRenderingCache()}.</p>
     *
     * @param enabled true to enable the rendering cache, false otherwise
     *
     * @see #isRenderCacheEnabled()
     * @see #getRenderingCache()
     * @see #layout()
     * @see #setLayerType(int, android.graphics.Paint)
     */
    public void setRenderCacheEnabled(boolean enabled) {
        setFlags(enabled ? RENDER_CACHE_ENABLED : 0, RENDER_CACHE_ENABLED);
    }

    /**
     * <p>Indicates whether the rendering cache is enabled for this view.</p>
     *
     * @return true if the rendering cache is enabled
     *
     * @see #setRenderCacheEnabled(boolean)
     * @see #getRenderingCache()
     */
    public boolean isRenderCacheEnabled() {
        return (mViewFlags & RENDER_CACHE_ENABLED) == RENDER_CACHE_ENABLED;
    }

    public void setRenderingCache(Bitmap bitmap) {
        mRenderingCache = bitmap;
    }

    /**
     * <p>Calling this method is equivalent to calling <code>getRenderingCache(false)</code>.</p>
     *
     * @return A non-scaled bitmap representing this view or null if cache is disabled.
     *
     * @see #getRenderingCache(boolean)
     */
    public Bitmap getRenderingCache() {
        return mRenderingCache;
    }

    /**
     * Dispatch a notification about a resource configuration change down
     * the view hierarchy.
     * Object3dContainer should override to route to their children.
     *
     * @param newConfig The GL configuration.
     */
    public void dispatchConfigurationChanged(GLConfiguration newConfig) {
        onConfigurationChanged(newConfig);
    }

    /**
     * Called when the current configuration of the resources being used
     * by the application have changed.  You can use this to decide when
     * to reload resources that can changed based on orientation and other
     * configuration characterstics.  You only need to use this if you are
     * not relying on the normal {@link android.app.Activity} mechanism of
     * recreating the activity instance upon a configuration change.
     *
     * @param newConfig The GL configuration.
     */
    protected void onConfigurationChanged(GLConfiguration newConfig) {
    }

    /**
     * @param info the {@link android.view.View.AttachInfo} to associated with
     *        this view
     */
    void dispatchAttachedToWindow(AttachInfo info, int visibility) {
        mAttachInfo = info;
        onAttachedToWindow();
    }

    /**
     * A set of information given to a view when it is attached to its parent
     * window.
     */
    static class AttachInfo {
        /**
         * A Handler supplied by a view's {@link android.view.ViewRootImpl}. This
         * handler can be used to pump events in the UI events queue.
         */
        final GLHandler mHandler;

        /**
         * Creates a new set of attachment information with the specified
         * events handler and thread.
         *
         * @param handler the events handler the view must use
         */
        AttachInfo(GLHandler handler) {
            mHandler = handler;
        }
    }
}
