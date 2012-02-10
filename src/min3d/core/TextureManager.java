package min3d.core;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Set;

import min3d.Min3d;
import min3d.Shared;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * TextureManager is responsible for managing textures for the whole environment. 
 * It maintains a list of id's that are mapped to the GL texture names (id's).
 *
 * You add a Bitmap to the TextureManager, which adds a textureId to its list.
 * Then, you assign one or more TextureVo's to your Object3d's using id's that 
 * exist in the TextureManager.
 * 
 * Note that the _idToTextureName and _idToHasMipMap HashMaps used below
 * don't test for exceptions. 
 */
public class TextureManager 
{
	private HashMap<String, Integer> _idToTextureName;
	private HashMap<String, Boolean> _idToHasMipMap;
	private static int _counter = 1000001;
	private static int _atlasId = 0;
	
	
	public TextureManager()
	{
		reset();
	}

    /**
     * Clear all the texture information added in texture manager.
     */
	public void reset()
	{
		// Delete any extant textures

		if (_idToTextureName != null) 
		{
			Set<String> s = _idToTextureName.keySet();
			Object[] a = s.toArray(); 
			for (int i = 0; i < a.length; i++) {
				int glId = getGlTextureId((String)a[i]);
				Shared.renderer().deleteTexture(glId);
			}
			// ...pain
		}
		
		_idToTextureName = new HashMap<String, Integer>();
		_idToHasMipMap = new HashMap<String, Boolean>();
	}

    /**
     * 'Uploads' a texture via OpenGL which is mapped to a textureId to the
     * TextureManager, which can subsequently be used to assign textures to
     * Object3d's.
     * (Forbid calling it out of GL thread)
     *
     * @param $b bitmap of target texture
     * @param $id texture id (it's should be unique string)
     * @param $generateMipMap enable texture mip map (mip map generation is
     *            pretty heavy job)
     * @return id of texture added to TextureManager, which is identical to $id
     */
	public String addTextureId(Bitmap $b, String $id, boolean $generateMipMap)
	{
		if (_idToTextureName.containsKey($id)) throw new Error("Texture id \"" + $id + "\" already exists."); 

		int glId = Shared.renderer().uploadTextureAndReturnId($b, $generateMipMap);

		String s = $id;
		_idToTextureName.put(s, glId);
		_idToHasMipMap.put(s, $generateMipMap);
	
		_counter++;
		
		// For debugging purposes (potentially adds a lot of chatter)
		// logContents();
		
		return s;
	}

    /**
     * 'Uploads' a texture via OpenGL which is mapped to a textureId to the
     * TextureManager, which can subsequently be used to assign textures to
     * Object3d's. (For ETC1 file)
     * (Forbid calling it out of GL thread)
     *
     * @param input input stream of target texture
     * @param $id texture id (it's must an unique string)
     * @param $generateMipMap enable texture mip map (mip map generation is
     *            pretty heavy job)
     * @return id of texture added to TextureManager, which is identical to $id
     */
    public String addTextureId(InputStream input, String $id, boolean $generateMipMap)
    {
        if (_idToTextureName.containsKey($id)) throw new Error("Texture id \"" + $id + "\" already exists.");

        int glId = Shared.renderer().uploadTextureAndReturnId(input, $generateMipMap);

        String s = $id;
        _idToTextureName.put(s, glId);
        _idToHasMipMap.put(s, $generateMipMap);

        _counter++;

        // For debugging purposes (potentially adds a lot of chatter)
        // logContents();

        return s;
    }

    /**
     * Alternate signature for "addTextureId", with MIP mapping set to false by
     * default. Kept for API backward-compatibility.
     * (Forbid calling it out of GL thread)
     *
     * @param $b bitmap of target texture
     * @param $id texture id (it's must an unique string)
     */
	public String addTextureId(Bitmap $b, String $id)
	{
		return this.addTextureId($b, $id, false);
	}
	
    /**
     * 'Uploads' texture via OpenGL and returns an autoassigned textureId, which
     * can be used to assign textures to Object3d's.
     * (Forbid calling it out of GL thread)
     *
     * @param $b bitmap of target texture
     * @param $generateMipMap enable texture mip map (mip map generation is
     *            pretty heavy job)
     */
	public String createTextureId(Bitmap $b, boolean $generateMipMap)
	{
		return addTextureId($b, (_counter+""), $generateMipMap);
	}
	
    /**
     * Deletes a textureId from the TextureManager, and deletes the
     * corresponding texture from the GPU
     * (Forbid calling it out of GL thread)
     *
     * @param $id texture id to be removed
     */
	public void deleteTexture(String $id)
	{
		int glId = _idToTextureName.get($id);
		Shared.renderer().deleteTexture(glId);
		_idToTextureName.remove($id);
		_idToHasMipMap.remove($id);
		
		// logContents();
		
		//xxx needs error check
	}

    /**
     * Returns a String Array of textureId's in the TextureManager
     *
     * @return string array which contain all textures' id.
     */
	public String[] getTextureIds()
	{
		Set<String> set = _idToTextureName.keySet();
		String[] a = new String[set.size()];
		set.toArray(a);
		return a;
	}
	
    /**
     * Used by Renderer
     */
	public int getGlTextureId(String $id) /*package-private*/
	{
		return _idToTextureName.get($id);
	}

    /**
     * Used by Renderer
     */
	public boolean hasMipMap(String $id) /*package-private*/
	{
		return _idToHasMipMap.get($id);
	}

    /**
     * Check whether texture has been added in texture manager with id.
     *
     * @param $id id of target texture to be checked.
     */
	public boolean contains(String $id)
	{
		return _idToTextureName.containsKey($id);
	}
	
	
	private String arrayToString(String[] $a)
	{
		String s = "";
		for (int i = 0; i < $a.length; i++)
		{
			s += $a[i].toString() + " | ";
		}
		return s;
	}
	
	private void logContents()
	{
		Log.v(Min3d.TAG, "TextureManager contents updated - " + arrayToString( getTextureIds() ) );		
	}
	
	public String getNewAtlasId() {
		return "atlas".concat(Integer.toString(_atlasId++));
	}
}
