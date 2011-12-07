package min3d.core;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import min3d.Shared;
import min3d.interfaces.IObject3dContainer;
import min3d.listeners.OnClickListener;
import min3d.listeners.OnTouchListener;
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

	private boolean _isVisible = true;
	private boolean _vertexColorsEnabled = true;
	private boolean _doubleSidedEnabled = false;
	private boolean _texturesEnabled = true;
	private boolean _normalsEnabled = true;
	private boolean _ignoreFaces = false;
	private boolean _colorMaterialEnabled = false;
	private boolean _lightingEnabled = true;

	private Number3d _position = new Number3d(0,0,0);
	private Number3d _rotation = new Number3d(0,0,0);
	private Number3d _scale = new Number3d(1,1,1);

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

    private List<Object3d> mDownList = null;
    private List<Object3d> mUpList = null;

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
		return _position;
	}
	
	/**
	 * X/Y/Z euler rotation of object, using Euler angles.
	 * Units should be in degrees, to match OpenGL usage. 
	 */
	public Number3d rotation()
	{
		return _rotation;
	}

	/**
	 * X/Y/Z scale of object.
	 */
	public Number3d scale()
	{
		return _scale;
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
        Matrix.translateM(mTransMC, 0, _position.x, _position.y, _position.z);

        Matrix.setIdentityM(mRotMC, 0);

        Matrix.rotateM(mRotMC, 0, _rotation.x, 1, 0, 0);
        Matrix.rotateM(mRotMC, 0, _rotation.y, 0, 1, 0);
        Matrix.rotateM(mRotMC, 0, _rotation.z, 0, 0, 1);

        Matrix.setIdentityM(mScaleMC, 0);
        Matrix.scaleM(mScaleMC, 0, _scale.x, _scale.y, _scale.z);

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

        Number3d accmlR = new Number3d(_rotation.x, _rotation.y, _rotation.z);
        Number3d accmlS = new Number3d(_scale.x, _scale.y, _scale.z);

        if (_parent != null && _parent instanceof Object3d) {
            accmlAABBTrans((Object3d) parent(), ROTATE, accmlR);
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
        // Only rotation value is inherited between parent and child
        // in 3d engine.
        if (mode == ROTATE) {
            result.add(parent.rotation());
        }
        if (parent != null && parent.parent() instanceof Object3d) {
            accmlAABBTrans((Object3d) parent.parent(), mode, result);
        }
    }

    public boolean intersects(Ray ray) {
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

    public void processTouchEvent(Ray ray, MotionEvent e) {
        if (mOnTouchListener == null && mOnClickListener == null) {
            return;
        }

        ArrayList<Object3d> list =
                (ArrayList<Object3d>)Shared.renderer().getPickedObject(ray, this).clone();

        Number3d coordinates = getIntersectPoint(e.getX(),e.getY(),mCenter.z);

        if (mOnTouchListener != null && list.size() > 0) {
            mOnTouchListener.onTouch(this, e, list, coordinates);
        }

        if (mOnClickListener != null) {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                mDownList = (List<Object3d>)list.clone();
            } else if (e.getAction() == MotionEvent.ACTION_UP) {
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

                if (mUpList.size() > 0) {
                    mOnClickListener.onClick(this, e, mUpList, coordinates);
                }
                mDownList.clear();
            }
        }
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
        mOnClickListener = listener;
    }
}
