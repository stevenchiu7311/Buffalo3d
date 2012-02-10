package min3d.core;

import java.util.ArrayList;

import min3d.Min3d;
import min3d.interfaces.IDirtyParent;
import min3d.interfaces.IObject3dContainer;
import min3d.interfaces.ISceneController;
import min3d.materials.AMaterial;
import min3d.materials.SimpleMaterial;
import min3d.vos.CameraVo;
import min3d.vos.Color4;
import min3d.vos.Color4Managed;
import min3d.vos.FogType;
import android.util.Log;

/**
 * The top of a object3d hierarchy, storing all the needed configurations of GL
 * scene. For rendering objects, all top-level objects must be added into scene
 * instance. It also provides several get/set method for GL effect setting.
 */
public class Scene implements IObject3dContainer, IDirtyParent
{
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
	static AMaterial mMaterial;

	public Scene(ISceneController $sceneController) 
	{
		_sceneController = $sceneController;
		_lights = new ManagedLightList();
		_fogColor = new Color4(255, 255, 255, 255);
		_fogNear = 0;
		_fogFar = 10;
		_fogType = FogType.LINEAR;
		_fogEnabled = false;

        mObject3dContainer = new Object3dContainer();
        mObject3dContainer.name("Root");
        mObject3dContainer.scene(this);
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

        mObject3dContainer = new Object3dContainer();
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
    static AMaterial getDefaultMaterial() {
        if (mMaterial == null) {
            mMaterial = new SimpleMaterial();
        }
        return mMaterial;
    }

    /**
     * Reset scene's material instance.
     *
     * @hide
     */
    static void resetMaterial() {
        mMaterial = null;
    }
}
