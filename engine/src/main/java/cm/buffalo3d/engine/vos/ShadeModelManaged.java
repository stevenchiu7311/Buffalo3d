package cm.buffalo3d.engine.vos;

import cm.buffalo3d.engine.interfaces.IDirtyParent;


public class ShadeModelManaged extends AbstractDirtyManaged 
{
	private ShadeModel _shadeModel;

	public ShadeModelManaged(IDirtyParent $parent)
	{
		super($parent);
		set(ShadeModel.SMOOTH);
	}
	
	public ShadeModel get()
	{
		return _shadeModel;
	}
	public void set(ShadeModel $shadeModel)
	{
		_shadeModel = $shadeModel;
		_dirty = true; // no need for callback
	}
}
