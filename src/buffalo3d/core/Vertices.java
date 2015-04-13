package buffalo3d.core;

import buffalo3d.vos.Color4;
import buffalo3d.vos.Number3d;
import buffalo3d.vos.Uv;


public class Vertices
{
	private Number3dBufferList mPoints;
	private UvBufferList mUvs;
	private Number3dBufferList mNormals;
	private Color4BufferList mColors;
	
	private boolean _hasUvs;
	private boolean _hasNormals;
	private boolean _hasColors;
	
	
	/**
	 * Used by Object3d to hold the lists of vertex points, texture coordinates (UV), normals, and vertex colors. 
	 * Use "addVertex()" to build the vertex data for the Object3d instance associated with this instance. 
	 * 
	 * Direct manipulation of position, UV, normal, or color data can be done directly through the associated 
	 * 'buffer list' instances contained herein.
	 */
	public Vertices(int $maxElements)
	{
		mPoints = new Number3dBufferList($maxElements);
		
		_hasUvs = true;
		_hasNormals = true;
		_hasColors = true;
		
		if (_hasUvs) mUvs = new UvBufferList($maxElements);
		if (_hasNormals) mNormals = new Number3dBufferList($maxElements);
		if (_hasColors) mColors = new Color4BufferList($maxElements);
	}

	/**
	 * This version of the constructor adds 3 boolean arguments determine whether 
	 * uv, normal, and color lists will be used by this instance.
	 * Set to false when appropriate to save memory, increase performance. 
	 */
	public Vertices(int $maxElements, Boolean $useUvs, Boolean $useNormals, Boolean $useColors)
	{
		mPoints = new Number3dBufferList($maxElements);
		
		_hasUvs = $useUvs;
		_hasNormals = $useNormals;
		_hasColors = $useColors;
		
		if (_hasUvs) mUvs = new UvBufferList($maxElements);
		if (_hasNormals) mNormals = new Number3dBufferList($maxElements);
		if (_hasColors) mColors = new Color4BufferList($maxElements);
	}
	
	public Vertices(Number3dBufferList $points, UvBufferList $uvs, Number3dBufferList $normals,
			Color4BufferList $colors)
	{
		mPoints = $points;
		mUvs = $uvs;
		mNormals = $normals;
		mColors = $colors;
		
		_hasUvs = mUvs != null && mUvs.size() > 0;
		_hasNormals = mNormals != null && mNormals.size() > 0;
		_hasColors = mColors != null && mColors.size() > 0;
	}
	
	public int size()
	{
		return mPoints.size();
	}
	
	public int capacity()
	{
		return mPoints.capacity();
	}
	
	public boolean hasUvs()
	{
		return _hasUvs;
	}

	public boolean hasNormals()
	{
		return _hasNormals;
	}
	
	public boolean hasColors()
	{
		return _hasColors;
	}
	
	
	/**
	 * Use this to populate an Object3d's vertex data.
	 * Return's newly added vertex's index 
	 * 
	 *  	If hasUvs, hasNormals, or hasColors was set to false, 
	 * 		their corresponding arguments are just ignored.
	 */
	public short addVertex(
		float $pointX, float $pointY, float $pointZ,  
		float $textureU, float $textureV,  
		float $normalX, float $normalY, float $normalZ,  
		short $colorR, short $colorG, short $colorB, short $colorA)
	{
		mPoints.add($pointX, $pointY, $pointZ);
		
		if (_hasUvs) mUvs.add($textureU, $textureV);
		if (_hasNormals) mNormals.add($normalX, $normalY, $normalZ);
		if (_hasColors) mColors.add($colorR, $colorG, $colorB, $colorA);
		
		return (short)(mPoints.size()-1);
	}
	
	/**
	 * More structured-looking way of adding a vertex (but potentially wasteful).
	 * The VO's taken in as arguments are only used to read the values they hold
	 * (no references are kept to them).  
	 * Return's newly added vertex's index 
	 * 
	 * 		If hasUvs, hasNormals, or hasColors was set to false, 
	 * 		their corresponding arguments are just ignored.
	 */
	public short addVertex(Number3d $point, Uv $textureUv, Number3d $normal, Color4 $color)
	{
		mPoints.add($point);
		
		if (_hasUvs) mUvs.add($textureUv);
		if (_hasNormals) mNormals.add($normal);
		if (_hasColors) mColors.add($color);
		
		return (short)(mPoints.size()-1);
	}
	
	public void overwriteVerts(float[] $newVerts)
	{
		mPoints.overwrite($newVerts);
	}
	
	public void overwriteNormals(float[] $newNormals)
	{
		mNormals.overwrite($newNormals);
	}
	
	public Number3dBufferList getPoints()
	{
		return mPoints;
	}
	
	/**
	 * List of texture coordinates
	 */
	public UvBufferList getUvs()
	{
		return mUvs;
	}
	
	/**
	 * List of normal values 
	 */
	public Number3dBufferList getNormals()
	{
		return mNormals;
	}
	
	/**
	 * List of color values
	 */
	public Color4BufferList getColors()
	{
		return mColors;
	}
	
	public Vertices clone()
	{
		Vertices v = new Vertices(mPoints.clone(), mUvs.clone(), mNormals.clone(), mColors.clone());
		return v;
	}

    public void setUv(int x, float u, float v) {
        mUvs.set(x,u,v);
    }

    public void clear() {
        mPoints.clear();
        mUvs.clear();
        mNormals.clear();
        mColors.clear();
    }
}
