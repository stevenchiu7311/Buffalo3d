package min3d.core;

import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import min3d.interfaces.IObject3dContainer;
import min3d.vos.Ray;

/**
 * A Object3dContainer is a special object3d that can contain other object3d
 * (called children.) The object3d container is the base class for layouts and objects
 * containers.
 */
public class Object3dContainer extends Object3d implements IObject3dContainer
{
	protected ArrayList<Object3d> _children = new ArrayList<Object3d>();

    private Object3d mMotionTarget = null;

	public Object3dContainer(GContext context)
	{
		super(context, 0, 0, false, false, false);
	}

	/**
	 * Adds container functionality to Object3d.
	 * 
	 * Subclass Object3dContainer instead of Object3d if you
	 * believe you may want to add children to that object. 
	 */
	public Object3dContainer(GContext context, int $maxVerts, int $maxFaces)
	{
		super(context, $maxVerts, $maxFaces,true,true, true);
	}

	public Object3dContainer(GContext context, int $maxVerts,  int $maxFaces, Boolean $useUvs, Boolean $useNormals, Boolean $useVertexColors)
	{
		super(context, $maxVerts, $maxFaces,$useUvs,$useNormals, $useVertexColors);
	}
	
	/**
	 * This constructor is convenient for cloning purposes 
	 */
	public Object3dContainer(GContext context, Vertices $vertices, FacesBufferedList $faces, TextureList $textures)
	{
		super(context, $vertices, $faces, $textures);
	}

    /**
     * {@inheritDoc}
     */
	public void addChild(Object3d $o)
	{
		_children.add($o);
		
		$o.parent(this);
		$o.scene(mGContext.getRenderer().getScene());
	}

    /**
     * {@inheritDoc}
     */
	public void addChildAt(Object3d $o, int $index) 
	{
		_children.add($index, $o);
		
		$o.parent(this);
		$o.scene(mGContext.getRenderer().getScene());
	}

    /**
     * {@inheritDoc}
     */
    public boolean removeChild(Object3d $o) {
        boolean b;
        synchronized (this) {
            b = _children.remove($o);

            if (b) {
                $o.parent(null);
                $o.scene(null);
            }
        }
        return b;
    }

    /**
     * {@inheritDoc}
     */
	public Object3d removeChildAt(int $index) 
	{
		Object3d o = _children.remove($index);
		if (o != null) {
			o.parent(null);
			o.scene(null);
		}
		return o;
	}

    /**
     * {@inheritDoc}
     */
	public Object3d getChildAt(int $index) 
	{
		return _children.get($index);
	}

    /**
     * {@inheritDoc}
     */
	public Object3d getChildByName(String $name)
	{
		for (int i = 0; i < _children.size(); i++)
		{
			if (_children.get(i).name().equals($name)) return _children.get(i); 
		}
		return null;
	}

    /**
     * {@inheritDoc}
     */
	public int getChildIndexOf(Object3d $o) 
	{
		return _children.indexOf($o);		
	}

    /**
     * {@inheritDoc}
     */
	public int numChildren() 
	{
		return _children.size();
	}
	
	/*package-private*/ 
	ArrayList<Object3d> children()
	{
		return _children;
	}
	
	public Object3dContainer clone()
	{
		Vertices v = _vertices.clone();
		FacesBufferedList f = _faces.clone();

		Object3dContainer clone = new Object3dContainer(mGContext, v, f, _textures);
		
		clone.position().x = position().x;
		clone.position().y = position().y;
		clone.position().z = position().z;
		
		clone.rotation().x = rotation().x;
		clone.rotation().y = rotation().y;
		clone.rotation().z = rotation().z;
		
		clone.scale().x = scale().x;
		clone.scale().y = scale().y;
		clone.scale().z = scale().z;
		
		for(int i = 0; i< this.numChildren();i++)
		{
			 clone.addChild(this.getChildAt(i));
		}
		 
		return clone;
	}

    /**
     * {@inheritDoc}
     */
    public boolean dispatchTouchEvent(Ray ray, MotionEvent ev, ArrayList<Object3d> list) {
        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            if (mMotionTarget != null) {
                // this is weird, we got a pen down, but we thought it was
                // already down!
                // XXX: We should probably send an ACTION_UP to the current
                // target.
                mMotionTarget = null;
            }
            Object3dContainer container = (Object3dContainer) this;
            ArrayList<Object3d> children = (ArrayList<Object3d>) container.children().clone();
            Collections.sort(children, new DepthSort(DepthSort.UP));
            for (int i = children.size() - 1; i >= 0; i--) {
                Object3d child = null;
                for (int j = 0; j < container.numChildren(); j++) {
                    if (children.get(i).equals(container.getChildAt(j))) {
                        child = container.getChildAt(j);
                    }
                }

                boolean find = false;
                for (Object3d obj : list) {
                    find = contain(child,obj);
                }

                if (child != null && child.isVisible() && find) {
                    if (child.dispatchTouchEvent(ray, ev, list)) {
                        mMotionTarget = child;
                        return true;
                    }
                }
            }
        }

        final Object3d target = mMotionTarget;
        if (target == null) {
            return super.dispatchTouchEvent(ray, ev, list);
        }

        boolean isUpOrCancel = (action == MotionEvent.ACTION_UP)
                || (action == MotionEvent.ACTION_CANCEL);

        if (isUpOrCancel) {
            mMotionTarget = null;
        }

        return target.dispatchTouchEvent(ray, ev, list);
    }

    /**
     * Check whether there is group-contained relationship between specified two objects.
     *
     * @param ancestors object to be the ancestors container
     * @param descendants object to be the descendants containee of ancestors object
     * @return true if there is group-contained relationship
     */
    public boolean contain(Object3d ancestors, Object3d descendants) {
        if (descendants.equals(ancestors)) {
            return true;
        }
        if (ancestors instanceof Object3dContainer) {
            Object3dContainer container = (Object3dContainer) ancestors;
            for (int i = 0; i < container.children().size(); i++) {
                Object3d child = container.children().get(i);
                boolean find = contain(child,descendants);
                if (find) {
                    return true;
                }
            }
        }
        return false;
    }

    private class DepthSort implements Comparator<Object3d> {
        private final static int UP = 1;
        private final static int DOWM = -1;

        private int state;

        public DepthSort(int state) {
            this.state = state;
        }

        public int compare(Object3d o1, Object3d o2) {
            if (state == DepthSort.DOWM) {
                return sortDown(o1, o2);
            }
            return sortUp(o1, o2);
        }

        private int sortUp(Object3d o1, Object3d o2) {
            if (o1.center().z < o2.center().z) {
                return -1;
            } else if (o1.center().z > o2.center().z) {
                return 1;
            } else {
                return 0;
            }
        }

        private int sortDown(Object3d o1, Object3d o2) {
            if (o1.center().z > o2.center().z) {
                return -1;
            } else if (o1.center().z < o2.center().z) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
