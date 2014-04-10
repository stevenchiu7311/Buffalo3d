package min3d.core;

import android.opengl.GLSurfaceView;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import min3d.GLConfiguration;
import min3d.GLHandler;
import min3d.Min3d;
import min3d.interfaces.IDirtyParent;
import min3d.interfaces.IObject3dContainer;
import min3d.interfaces.IObject3dParent;
import min3d.interfaces.ISceneController;
import min3d.materials.AMaterial;
import min3d.materials.OpenGLESV1Material;
import min3d.materials.SimpleMaterial;
import min3d.vos.CameraVo;
import min3d.vos.Color4;
import min3d.vos.Color4Managed;
import min3d.vos.FogType;
import min3d.vos.LightType;
import min3d.vos.Ray;

import java.util.ArrayList;

/**
 * The top of a object3d hierarchy, storing all the needed configurations of GL
 * scene. For rendering objects, all top-level objects must be added into scene
 * instance. It also provides several get/set method for GL effect setting.
 */
public class Scene implements IObject3dContainer, IDirtyParent
{
    private final static String TAG = "Scene";

    private static final boolean DBG = true;

    private final static int MSG_DISPATCH_INPUT_EVENT = 7;
    private final static int MSG_UPDATE_CONFIGURATION = 18;

	private ManagedLightList _lights;
	private CameraVo _camera;
	
	private Color4Managed _backgroundColor;
	private boolean _lightingEnabled;
	
	private Color4 _fogColor;
	private float _fogFar;
	private float _fogNear;
	private FogType _fogType;
	private boolean _fogEnabled;

	private ISceneController _sceneController;
	
	private Object3dContainer mObject3dContainer;

    // For opengl v2
    public final static int SIMPLE_MATERIAL = 0;
    public final static int OPENGLESV1_MATERIAL = 1;
    static AMaterial mMaterial;
    private boolean mSupportFullFeature = false;
    int mMaterialType = OPENGLESV1_MATERIAL;
    private boolean mFogDirty = true;
    private boolean mMaterialDirty = true;
    private boolean mInTouchMode = false;

    private GContext mGContext;

    Object3d mFocusedView;
    Object3d mRealFocusedView;  // this is not set to null in touch mode

    final Object3d.AttachInfo mAttachInfo;

    ArrayList<Object3d> mDownHitList = null;

    private final SceneHandler mHandler;

	public Scene(GContext context, ISceneController $sceneController)
	{
	    mGContext = context;
		_sceneController = $sceneController;
		_lights = new ManagedLightList();
		_fogColor = new Color4(255, 255, 255, 255);
		_fogNear = 0;
		_fogFar = 10;
		_fogType = FogType.LINEAR;
		_fogEnabled = false;

        mObject3dContainer = new Object3dContainer(mGContext);
        mObject3dContainer.name("Root");
        mObject3dContainer.scene(this);

        HandlerThread ht = new HandlerThread("");
        ht.start();
        GLHandler handler = new GLHandler(ht.getLooper(), context.getGLSurfaceView());

        mAttachInfo = new Object3d.AttachInfo(handler);

        mHandler = new SceneHandler(handler.getLooper(), context.getGLSurfaceView());
	}

    /**
     * Allows you to use any Class implementing ISceneController to drive the
     * Scene...
     */
	public ISceneController sceneController()
	{
		return _sceneController;
	}

    /**
     * Allows you to use any Class implementing ISceneController to drive the
     * Scene...
     */
	public void sceneController(ISceneController $sceneController)
	{
		_sceneController = $sceneController;
	}
	
	//
	
	/**
	 * Resets Scene to default settings.
	 * Removes and clears any attached Object3ds.
	 * Resets light list.
	 */
	public void reset()
	{
		clearChildren(this);

        mObject3dContainer = new Object3dContainer(mGContext);
        mObject3dContainer.name("Root");
        mObject3dContainer.scene(this);

		_camera = new CameraVo();
		
		_backgroundColor = new Color4Managed(0,0,0,255, this);
		
		_lights = new ManagedLightList();
		
		lightingEnabled(true);
	}
	
    /**
     * Adds object to scene's root object container. Objects must be added to
     * root object container in order to be rendered Returns always true.
     *
     * {@inheritDoc}
     */
	public void addChild(Object3d $o)
	{
        mObject3dContainer.addChild($o);
        performTraversals();
	}

    /**
     * Adds object to specified position of scene's root object container.
     * Objects must be added to root object container in order to be rendered
     * Returns always true.
     *
     * {@inheritDoc}
     */
	public void addChildAt(Object3d $o, int $index)
	{
        mObject3dContainer.addChildAt($o, $index);
        performTraversals();
	}

    /**
     * Removes object from scene's root object container. Returns false if it's
     * unsuccessful.
     *
     * {@inheritDoc}
     */
	public boolean removeChild(Object3d $o)
	{
        return mObject3dContainer.removeChild($o);
	}

    /**
     * Removes object from specified position of scene's root object container.
     *
     * {@inheritDoc}
     */
	public Object3d removeChildAt(int $index)
	{
        return mObject3dContainer.removeChildAt($index);
	}

    /**
     * Removes all objects of scene's root object container.
     *
     * {@inheritDoc}
     */
    public void removeAllChildren() {
        mObject3dContainer.removeAllChildren();
    }

    /**
     * Get object from specified position of scene's root object container.
     *
     * {@inheritDoc}
     */
	public Object3d getChildAt(int $index)
	{
        return mObject3dContainer.getChildAt($index);
	}
	
    /**
     * {@inheritDoc}
     */
	public Object3d getChildByName(String $name)
	{
        return mObject3dContainer.getChildByName($name);
	}

    /**
     * {@inheritDoc}
     */
	public int getChildIndexOf(Object3d $o)
	{
        return mObject3dContainer.getChildIndexOf($o);
	}

    /**
     * {@inheritDoc}
     */
	public int numChildren()
	{
        return mObject3dContainer.numChildren();
	}

    /**
     * Return scene's root object container.
     *
     * @return root object container
     */
    public Object3dContainer root()
    {
        return mObject3dContainer;
    }

    /**
     * Get scene's camera. Manipulating this returned instance will change camera
     * configuration.
     *
     * @return camera configuration instance
     */
	public CameraVo camera()
	{
		return _camera;
	}

    /**
     * Assign new scene's camera.
     *
     * @param $camera camera configuration instance
     */
	public void camera(CameraVo $camera)
	{
		_camera = $camera;
	}
	
    /**
     * Get scene instance's background color. Manipulating this returned instance
     * will change background color.
     *
     * @return background configuration instance
     */
	public Color4Managed backgroundColor()
	{
		return _backgroundColor;
	}

    /**
     * Get lights used by the scene
     *
     * @return light configuration instance list
     */
	public ManagedLightList lights()
	{
		return _lights;
	}

    /**
     * Return if lighting is enabled for scene.
     *
     * @return lighting enabled
     */
	public boolean lightingEnabled()
	{
		return _lightingEnabled;
	}

    /**
     * Determines if lighting is enabled for scene.
     *
     * @param $b lighting enabled
     */
	public void lightingEnabled(boolean $b)
	{
		_lightingEnabled = $b;
	}

    /**
     * Get fog color configuration instance. Manipulating this returned instance
     * will change fog color.
     *
     * @return fog color configuration instance
     */
	public Color4 fogColor() {
		return _fogColor;
	}

    /**
     * Assign fog color configuration instance.
     *
     * @param _fogColor color configuration instance
     */
	public void fogColor(Color4 _fogColor) {
		this._fogColor = _fogColor;
	}

    /**
     * Get farthest effect position of fog.
     *
     * @return farthest effect position of fog
     */
	public float fogFar() {
		return _fogFar;
	}

    /**
     * Set farthest effect position of fog.
     *
     * @param _fogFar farthest effect position of fog
     */
	public void fogFar(float _fogFar) {
		this._fogFar = _fogFar;
	}

    /**
     * Get nearest effect position of fog.
     *
     * @return nearest effect position of fog
     */
	public float fogNear() {
		return _fogNear;
	}

    /**
     * Set nearest effect position of fog.
     *
     * @param _fogNear nearest effect position of fog
     */
	public void fogNear(float _fogNear) {
		this._fogNear = _fogNear;
	}

    /**
     * Get current fog type.
     *
     * @return current fog type
     */
	public FogType fogType() {
		return _fogType;
	}

    /**
     * Set current fog type.
     *
     * @param _fogType current fog type
     */
	public void fogType(FogType _fogType) {
        if (this._fogType.glValue() != _fogType.glValue()) {
            mFogDirty = true;
        }
		this._fogType = _fogType;
	}

    /**
     * Return fog enabled state.
     *
     * @return fog enabled state.
     */
	public boolean fogEnabled() {
		return _fogEnabled;
	}

    /**
     * Set fog enabled state.
     *
     * @param _fogEnabled fog enabled state.
     */
	public void fogEnabled(boolean _fogEnabled) {
        if (this._fogEnabled != _fogEnabled) {
            mFogDirty = true;
        }
		this._fogEnabled = _fogEnabled;
	}

	/**
	 * Used by Renderer 
	 */
	void init() /*package-private*/ 
	{
		Log.i(Min3d.TAG, "Scene.init()");
		
		this.reset();
		
		_sceneController.initScene();
		_sceneController.getInitSceneHandler().post(_sceneController.getInitSceneRunnable());
	}
	
	void update()
	{
		_sceneController.updateScene();
		_sceneController.getUpdateSceneHandler().post(_sceneController.getUpdateSceneRunnable());
	}
	
	/**
	 * Used by Renderer 
	 */
	ArrayList<Object3d> children() /*package-private*/ 
	{
        return mObject3dContainer.children();
	}
	
	private void clearChildren(IObject3dContainer $c)
	{
		for (int i = $c.numChildren() - 1; i >= 0; i--)
		{
			Object3d o = $c.getChildAt(i);
			o.clear();
			
			if (o instanceof Object3dContainer)
			{
				clearChildren((Object3dContainer)o);
			}
		}
	}	
	
    public void onDirty() {
    }

    /**
     * Get scene's material instance. Material is a interface which control ES 2
     * shader.
     *
     * @hide
     */
    AMaterial getDefaultMaterial(GContext context) {
        if (mMaterial == null || mMaterialDirty || checkReCompilerShader()) {
            mMaterial = createMaterial(context);
            mMaterialDirty = false;
            String fragmeShaderDefine = "";
            String vertexShaderDefine = "";
            if (_fogEnabled) {
                fragmeShaderDefine += "#define ENABLE_FOG\n";
                vertexShaderDefine += "#define ENABLE_FOG\n";
                vertexShaderDefine += "#define FOG_TYPE 0x" + Integer.toString(_fogType.glValue(), 16)+"\n";
            }
            mFogDirty = false;

            if (mMaterialType == OPENGLESV1_MATERIAL) {
                String lightDefineString = getLightDefine();
                fragmeShaderDefine += lightDefineString;
                vertexShaderDefine += lightDefineString;
                fragmeShaderDefine += "#define COMMON_USED\n";
                vertexShaderDefine += "#define COMMON_USED\n";
// Disable multiple texture for GPU compatible.
//                fragmeShaderDefine += "#define MULTIPLE_TEXTURE\n";
//                vertexShaderDefine += "#define MULTIPLE_TEXTURE\n";
                mMaterial.setLightNumber(_lights.size());
            }

            mMaterial.setFragmeShaderDefine(fragmeShaderDefine);
            mMaterial.setVertexShaderDefine(vertexShaderDefine);
            mMaterial.compilerShaders();
        }
        return mMaterial;
    }

    private AMaterial createMaterial(GContext context) {
        if (mMaterialType == OPENGLESV1_MATERIAL) {
            return new OpenGLESV1Material(context);
        } else if (mMaterialType == SIMPLE_MATERIAL) {
            return new SimpleMaterial(context);
        } else {
            return new SimpleMaterial(context);
        }
    }

    private String getLightDefine() {
        String returnString = "";
        if (_lights.size() == 0) {
            return "#define NOLIGHT\n";
        }
        for (int glIndex = 0; glIndex < Renderer.NUM_GLLIGHTS; glIndex++) {
            if (lights().glIndexEnabled()[glIndex]) {
                returnString = returnString + "#define LIGHT" + Integer.toString(glIndex) + "\n";
                if (_lights.get(glIndex).type() != LightType.DIRECTIONAL) {
                    returnString = returnString + "#define LIGHT" + Integer.toString(glIndex) + "_W\n";
                }
            }
        }
        return returnString;
    }

    private boolean checkReCompilerShader() {
        if (mFogDirty) {
            return true;
        }

        if (mMaterialType == OPENGLESV1_MATERIAL) {
            for (int glIndex = 0; glIndex < Renderer.NUM_GLLIGHTS; glIndex++) {
                if (_lights.glIndexEnabledDirty()[glIndex]) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Reset scene's material instance.
     *
     * @hide
     */
    static void resetMaterial() {
        mMaterial = null;
    }

    public void setMaterialDirty() {
        mMaterialDirty = true;
    }

    /**
     * Determines if support full feature or common used feature.
     * The function is available if developer use openglES2.0 and OpenGLV1Material material.
     * If developer set ture, the OpenGLV1Material material support multi-texture, fog, teture mode, multi-lights ..
     * If developer set false, the OpenGLV1Material material support single-texture, fog, multi-lights
     *
     * @param supportFullFeature true to support Full Feature.
     */
    public void supportFullFeature(boolean supportFullFeature) {
        mSupportFullFeature = supportFullFeature;
    }

    /**
     * Indicates if support full feature.
     *
     * @return true if support Full Feature.
     */
    public boolean supportFullFeature() {
        return mSupportFullFeature;
    }

    /**
     * Determines which material will used for rendering object.
     * In current stage, only support Simple and OpenglESV1 Material.
     * Simple Material only support texture.
     * OpenglESV1 Material support multi-texture, fog, teture mode, multi-lights.
     *
     * @param type SIMPLE_MATERIAL or OPENGLESV1_MATERIAL
     *
     * @return true if type is a available type.
     */
    public boolean setMaterialType (int type) {
        if ((type != SIMPLE_MATERIAL) || (type != OPENGLESV1_MATERIAL)) {
            Log.d(TAG, "Material type:" + Integer.toString(type) + "is not available");
            return false;
        }
        if (mMaterialType != type) {
            mMaterialType = type;
            mMaterialDirty = true;
        }
        return true;
    }

    /**
     * Indicates which material is used.
     * In current stage, only support Simple and OpenglESV1 Material.
     * Simple Material only support single-texture.
     * OpenglESV1 Material support multi-texture, fog, teture mode, multi-lights.
     *
     * @return SIMPLE_MATERIAL or OPENGLESV1_MATERIAL
     */
    public int getMaterialType() {
        return mMaterialType;
    }

    public void dispatchTouchEventToChild(MotionEvent e) {
        // Enter touch mode on down or scroll.
        final int action = e.getAction();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            ensureTouchMode(true);
        }

        Ray ray = mGContext.getRenderer().getViewRay(e.getX(), e.getY());
        ArrayList<Object3d> list = null;
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            mGContext.getRenderer().updateAABBCoord();
            mDownHitList = (ArrayList<Object3d>) mGContext.getRenderer().getPickedObject(ray, mObject3dContainer);
            list = mDownHitList;
        } else {
            // Only check whether pointer is still on these object.
            for (Object3d obj : mDownHitList) {
                obj.containAABB();
            }
            list = (ArrayList<Object3d>) mGContext.getRenderer().getPickedObject(ray, mObject3dContainer);
        }
        mObject3dContainer.dispatchTouchEvent(ray ,e, list);
    }

    public void performTraversals() {
        final Object3d.AttachInfo attachInfo = mAttachInfo;
        mObject3dContainer.dispatchAttachedToWindow(attachInfo, Object3d.VISIBLE);
    }

    /**
     * <p>Finds the topmost view in the current view hierarchy.</p>
     *
     * @return the topmost object container
     */
    public Object3d getRootObjectContainer() {
        return mObject3dContainer;
    }

    /**
     * Indicates whether we are in touch mode. Calling this method triggers an IPC
     * call and should be avoided whenever possible.
     *
     * @return True, if the device is in touch mode, false otherwise.
     *
     * @hide
     */
    public boolean isInTouchMode() {
        return mInTouchMode;
    }

    /**
     * Something in the current window tells us we need to change the touch mode.  For
     * example, we are not in touch mode, and the user touches the screen.
     *
     * If the touch mode has changed, tell the window manager, and handle it locally.
     *
     * @param inTouchMode Whether we want to be in touch mode.
     * @return True if the touch mode changed and focus changed was changed as a result
     */
    boolean ensureTouchMode(boolean inTouchMode) {
        // handle the change
        return ensureTouchModeLocally(inTouchMode);
    }

    /**
     * Ensure that the touch mode for this window is set, and if it is changing,
     * take the appropriate action.
     * @param inTouchMode Whether we want to be in touch mode.
     * @return True if the touch mode changed and focus changed was changed as a result
     */
    private boolean ensureTouchModeLocally(boolean inTouchMode) {
        return (inTouchMode) ? enterTouchMode() : leaveTouchMode();
    }

    private boolean enterTouchMode() {
        if (mObject3dContainer != null) {
            if (mObject3dContainer.hasFocus()) {
                // note: not relying on mFocusedView here because this could
                // be when the window is first being added, and mFocused isn't
                // set yet.
                final Object3d focused = mObject3dContainer.findFocus();
                if (focused != null && !focused.isFocusableInTouchMode()) {

                    final Object3dContainer ancestorToTakeFocus =
                            findAncestorToTakeFocusInTouchMode(focused);
                    if (ancestorToTakeFocus != null) {
                        // there is an ancestor that wants focus after its descendants that
                        // is focusable in touch mode.. give it focus
                        return ancestorToTakeFocus.requestFocus();
                    } else {
                        // nothing appropriate to have focus in touch mode, clear it out
                        mObject3dContainer.unFocus();
                        //mAttachInfo.mTreeObserver.dispatchOnGlobalFocusChange(focused, null);
                        mFocusedView = null;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Find an ancestor of focused that wants focus after its descendants and is
     * focusable in touch mode.
     * @param focused The currently focused view.
     * @return An appropriate view, or null if no such view exists.
     */
    private Object3dContainer findAncestorToTakeFocusInTouchMode(Object3d focused) {
        IObject3dParent parent = focused.parent();
        while (parent instanceof Object3dContainer) {
            final Object3dContainer vgParent = (Object3dContainer) parent;
            if (vgParent.getDescendantFocusability() == Object3dContainer.FOCUS_AFTER_DESCENDANTS
                    && vgParent.isFocusableInTouchMode()) {
                return vgParent;
            }
            if (vgParent.isRootNamespace()) {
                return null;
            } else {
                parent = vgParent.getParent();
            }
        }
        return null;
    }

    private boolean leaveTouchMode() {
        if (mObject3dContainer != null) {
            if (mObject3dContainer.hasFocus()) {
                // i learned the hard way to not trust mFocusedView :)
                mFocusedView = mObject3dContainer.findFocus();
                if (!(mFocusedView instanceof Object3dContainer)) {
                    // some Object3d has focus, let it keep it
                    return false;
                } else if (((Object3dContainer)mFocusedView).getDescendantFocusability() !=
                        Object3dContainer.FOCUS_AFTER_DESCENDANTS) {
                    // some view group has focus, and doesn't prefer its children
                    // over itself for focus, so let them keep it.
                    return false;
                }
            }

            // find the best view to give focus to in this brave new non-touch-mode
            // world
            final Object3d focused = focusSearch(null, Object3d.FOCUS_DOWN);
            if (focused != null) {
                return focused.requestFocus(Object3d.FOCUS_DOWN);
            }
        }
        return false;
    }

    /**
     * Returns true if the key is used for keyboard navigation.
     * @param keyEvent The key event.
     * @return True if the key is used for keyboard navigation.
     */
    private static boolean isNavigationKey(KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
        case KeyEvent.KEYCODE_DPAD_LEFT:
        case KeyEvent.KEYCODE_DPAD_RIGHT:
        case KeyEvent.KEYCODE_DPAD_UP:
        case KeyEvent.KEYCODE_DPAD_DOWN:
        case KeyEvent.KEYCODE_DPAD_CENTER:
        case KeyEvent.KEYCODE_PAGE_UP:
        case KeyEvent.KEYCODE_PAGE_DOWN:
        case KeyEvent.KEYCODE_MOVE_HOME:
        case KeyEvent.KEYCODE_MOVE_END:
        case KeyEvent.KEYCODE_TAB:
        case KeyEvent.KEYCODE_SPACE:
        case KeyEvent.KEYCODE_ENTER:
            return true;
        }
        return false;
    }

    /**
     * Returns true if the key is used for typing.
     * @param keyEvent The key event.
     * @return True if the key is used for typing.
     */
    private static boolean isTypingKey(KeyEvent keyEvent) {
        return keyEvent.getUnicodeChar() > 0;
    }

    /**
     * See if the key event means we should leave touch mode (and leave touch mode if so).
     * @param event The key event.
     * @return Whether this key event should be consumed (meaning the act of
     *   leaving touch mode alone is considered the event).
     */
    public boolean checkForLeavingTouchModeAndConsume(KeyEvent event) {
        // Only relevant in touch mode.
        if (!isInTouchMode()) {
            return false;
        }

        // Only consider leaving touch mode on DOWN or MULTIPLE actions, never on UP.
        final int action = event.getAction();
        if (action != KeyEvent.ACTION_DOWN && action != KeyEvent.ACTION_MULTIPLE) {
            return false;
        }

        // Don't leave touch mode if the IME told us not to.
        if ((event.getFlags() & KeyEvent.FLAG_KEEP_TOUCH_MODE) != 0) {
            return false;
        }

        // If the key can be used for keyboard navigation then leave touch mode
        // and select a focused view if needed (in ensureTouchMode).
        // When a new focused view is selected, we consume the navigation key because
        // navigation doesn't make much sense unless a view already has focus so
        // the key's purpose is to set focus.
        if (isNavigationKey(event)) {
            return ensureTouchMode(false);
        }

        // If the key can be used for typing then leave touch mode
        // and select a focused view if needed (in ensureTouchMode).
        // Always allow the view to process the typing key.
        if (isTypingKey(event)) {
            ensureTouchMode(false);
            return false;
        }

        return false;
    }

    @Override
    public IObject3dParent getParent() {
        return null;
    }

    public GLHandler getGLHandler() {
        if (mAttachInfo != null) {
            return mAttachInfo.mHandler;
        }
        return null;
    }

    @Override
    public void requestChildFocus(Object3d child, Object3d focused) {
/*        if (mFocusedView != focused) {
            mAttachInfo.mTreeObserver.dispatchOnGlobalFocusChange(mFocusedView, focused);
            scheduleTraversals();
        }*/
        mFocusedView = mRealFocusedView = focused;
        if (DBG) Log.v(TAG, "Request root's child focus: focus now "
                + mFocusedView);
    }

    @Override
    public void clearChildFocus(Object3d child) {
        Object3d oldFocus = mFocusedView;

        if (DBG) Log.v(TAG, "Clearing root's child focus");
        mFocusedView = mRealFocusedView = null;
        if (mObject3dContainer != null && !mObject3dContainer.hasFocus()) {
            // If a view gets the focus, the listener will be invoked from requestChildFocus()
            if (!mObject3dContainer.requestFocus(Object3d.FOCUS_FORWARD)) {
                //mAttachInfo.mTreeObserver.dispatchOnGlobalFocusChange(oldFocus, null);
            }
        } else if (oldFocus != null) {
            //mAttachInfo.mTreeObserver.dispatchOnGlobalFocusChange(oldFocus, null);
        }
    }

    @Override
    public Object3d focusSearch(Object3d obj, int direction) {
        if (!(mObject3dContainer instanceof Object3dContainer)) {
            return null;
        }
        return null;
    }

    @Override
    public void focusableObjectAvailable(Object3d obj) {
        if (DBG) {
            Log.v(TAG, "Focus root's child available:" + obj);
        }
        if (mObject3dContainer != null) {
            if (!mObject3dContainer.hasFocus()) {
                obj.requestFocus();
            } else {
                // the one case where will transfer focus away from the current one
                // is if the current view is a view group that prefers to give focus
                // to its children first AND the view is a descendant of it.
                mFocusedView = mObject3dContainer.findFocus();
                boolean descendantsHaveDibsOnFocus =
                        (mFocusedView instanceof Object3dContainer) &&
                            (((Object3dContainer) mFocusedView).getDescendantFocusability() ==
                                Object3dContainer.FOCUS_AFTER_DESCENDANTS);
                if (descendantsHaveDibsOnFocus && isObjectDescendantOf(obj, mFocusedView)) {
                    // If a view gets the focus, the listener will be invoked from requestChildFocus()
                    obj.requestFocus();
                }
            }
        }
    }

    /**
     * Return true if child is an ancestor of parent, (or equal to the parent).
     */
    private static boolean isObjectDescendantOf(Object3d child, Object3d parent) {
        if (child == parent) {
            return true;
        }

        final IObject3dContainer theParent = child.parent();
        return (theParent instanceof Object3dContainer) && isObjectDescendantOf((Object3d) theParent, parent);
    }

    void updateConfiguration(GLConfiguration config, boolean force) {
        if (mObject3dContainer != null) {
            mObject3dContainer.dispatchConfigurationChanged(config);
        }
    }

    public void requestUpdateConfiguration(GLConfiguration config) {
        mGContext.setGLConfiguration(config);
        Message msg = mHandler.obtainMessage(MSG_UPDATE_CONFIGURATION, config);
        mHandler.sendMessage(msg);
    }

    public void dispatchInputEvent(MotionEvent event) {
        MotionEvent copy = MotionEvent.obtain(event);
        Message msg = mHandler.obtainMessage(MSG_DISPATCH_INPUT_EVENT, copy);
        mHandler.sendMessage(msg);
    }

    final class SceneHandler extends GLHandler {
        public SceneHandler(Looper looper, GLSurfaceView proxy) {
            super(looper, proxy);
        }

        @Override
        public String getMessageName(Message msg) {
            switch (msg.what) {
                case MSG_DISPATCH_INPUT_EVENT:
                    return "MSG_DISPATCH_INPUT_EVENT";
                case MSG_UPDATE_CONFIGURATION:
                    return "MSG_UPDATE_CONFIGURATION";
            }
            return super.getMessageName(msg);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_DISPATCH_INPUT_EVENT:
                MotionEvent event = (MotionEvent)msg.obj;
                dispatchTouchEventToChild(event);
                event.recycle();
                break;
            case MSG_UPDATE_CONFIGURATION:
                GLConfiguration config = (GLConfiguration)msg.obj;
                updateConfiguration(config, false);
                break;
            }
        }
    }
}
