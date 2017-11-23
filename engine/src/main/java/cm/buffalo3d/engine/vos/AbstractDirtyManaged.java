package cm.buffalo3d.engine.vos;

import cm.buffalo3d.engine.interfaces.IDirtyManaged;
import cm.buffalo3d.engine.interfaces.IDirtyParent;


public abstract class AbstractDirtyManaged implements IDirtyManaged
{
	protected IDirtyParent _parent;
	protected boolean _dirty;
	
	public AbstractDirtyManaged(IDirtyParent $parent)
	{
		_parent = $parent;
	}
	
	public boolean isDirty()
	{
		return _dirty;
	}
	
	public void setDirtyFlag()
	{
		_dirty = true;
		if (_parent != null) _parent.onDirty();
	}
	
	public void clearDirtyFlag()
	{
		_dirty = false;
	}
}
