package min3d.interfaces;

import java.util.ArrayList;

import min3d.core.Object3d;

/**
 * Using Actionscript 3 nomenclature for what are essentially "pass-thru" methods to an underlying ArrayList  
 */
public interface IObject3dContainer 
{
    /**
     * Adds a child object.
     * (Not recommend call it out of GL thread)
     *
     * @param $child the child object to be added
     */
	public void addChild(Object3d $child);

    /**
     * Adds a child object with specified position.
     * (Not recommend call it out of GL thread)
     *
     * @param $child the child object to add
     * @param $index the position at which to add the child
     */
	public void addChildAt(Object3d $child, int $index);

    /**
     * Removes the specified child.
     * (Not recommend call it out of GL thread)
     *
     * @param $child the child object to remove
     */
	public boolean removeChild(Object3d $child);

    /**
     * Removes the child with specified position.
     * (Not recommend call it out of GL thread)
     *
     * @param $index the child object to remove
     * @return true if removing is successful.
     */
	public Object3d removeChildAt(int $index);

    /**
     * Returns the object at the specified position in the group.
     * (Not recommend call it out of GL thread)
     *
     * @param $index the position at which to get the object from
     * @return the object at the specified position or null if the position
     *         does not exist within the group
     */
	public Object3d getChildAt(int $index);

    /**
     * Returns the object with the matched name in the group.
     * (Not recommend call it out of GL thread)
     *
     * @param $name name of the child which to get
     * @return the object at the specified position or null if the position
     *         does not exist within the group
     */
	public Object3d getChildByName(String $name);

    /**
     * Returns the position index of specified child object.
     * (Not recommend call it out of GL thread)
     *
     * @param $child specified child object
     * @return index the position at which to get the object from
     */
	public int getChildIndexOf(Object3d $child);

    /**
     * Returns the number of children in the group.
     * (Not recommend call it out of GL thread)
     *
     * @return a positive integer representing the number of children in
     *         the group
     */
	public int numChildren();
}
