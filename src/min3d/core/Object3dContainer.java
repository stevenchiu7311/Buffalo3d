package min3d.core;

import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import min3d.Shared;
import min3d.interfaces.IObject3dContainer;
import min3d.vos.Ray;

public class Object3dContainer extends Object3d implements IObject3dContainer
{
	protected ArrayList<Object3d> _children = new ArrayList<Object3d>();

    private Object3d mMotionTarget = null;

	public Object3dContainer()
	{
		super(0, 0, false, false, false);
	}
	/**
	 * Adds container functionality to Object3d.
	 * 
	 * Subclass Object3dContainer instead of Object3d if you
	 * believe you may want to add children to that object. 
	 */
	public Object3dContainer(int $maxVerts, int $maxFaces)
	{
		super($maxVerts, $maxFaces, true,true,true);
	}

	public Object3dContainer(int $maxVerts, int $maxFaces,  Boolean $useUvs, Boolean $useNormals, Boolean $useVertexColors)
	{
		super($maxVerts, $maxFaces, $useUvs,$useNormals,$useVertexColors);
	}
	
	/**
	 * This constructor is convenient for cloning purposes 
	 */
	public Object3dContainer(Vertices $vertices, FacesBufferedList $faces, TextureList $textures)
	{
		super($vertices, $faces, $textures);
	}
	
	public void addChild(Object3d $o)
	{
		_children.add($o);
		
		$o.parent(this);
		$o.scene(Shared.renderer().getScene());
	}
	
	public void addChildAt(Object3d $o, int $index) 
	{
		_children.add($index, $o);
		
		$o.parent(this);
		$o.scene(Shared.renderer().getScene());
	}

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
	
	public Object3d removeChildAt(int $index) 
	{
		Object3d o = _children.remove($index);
		if (o != null) {
			o.parent(null);
			o.scene(null);
		}
		return o;
	}
	
	public Object3d getChildAt(int $index) 
	{
		return _children.get($index);
	}

	/**
	 * TODO: Use better lookup 
	 */
	public Object3d getChildByName(String $name)
	{
		for (int i = 0; i < _children.size(); i++)
		{
			if (_children.get(i).name().equals($name)) return _children.get(i); 
		}
		return null;
	}

	public int getChildIndexOf(Object3d $o) 
	{
		return _children.indexOf($o);		
	}


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

		Object3dContainer clone = new Object3dContainer(v, f, _textures);
		
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
                    find = contain(this,obj);
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

    public boolean contain(Object3d src, Object3d node) {
        if (node.equals(src)) {
            return true;
        }
        if (src instanceof Object3dContainer) {
            Object3dContainer container = (Object3dContainer) src;
            for (int i = 0; i < container.children().size(); i++) {
                Object3d child = container.children().get(i);
                boolean find = contain(child,node);
                if (find) {
                    return true;
                }
            }
        }
        return false;
    }

    public class DepthSort implements Comparator<Object3d> {
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
