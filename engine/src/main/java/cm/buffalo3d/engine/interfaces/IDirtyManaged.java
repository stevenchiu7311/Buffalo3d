package cm.buffalo3d.engine.interfaces;

public interface IDirtyManaged 
{
	public boolean isDirty();
	public void setDirtyFlag();
	public void clearDirtyFlag();
}
