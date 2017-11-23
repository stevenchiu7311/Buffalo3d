package cm.buffalo3d.engine.parser;

import android.content.res.Resources;

import cm.buffalo3d.engine.core.GContext;


/**
 * Parser factory class. Specify the parser type and the corresponding
 * specialized class will be returned.
 * @author dennis.ippel
 *
 */
public class Parser {
	/**
	 * Parser types enum
	 * @author dennis.ippel
	 *
	 */
	public static enum Type { OBJ, MAX_3DS, MD2 };
	
	/**
	 * Create a parser of the specified type.
	 * @param context
	 * @param type
	 * @param resources
	 * @param resourceID
	 * @return parser interface
	 */
	public static IParser createParser(GContext context, Type type, Resources resources, String resourceID, boolean generateMipMap)
	{
		switch(type)
		{
			case OBJ:
				return new ObjParser(context, resources, resourceID, generateMipMap);
			case MAX_3DS:
				return new Max3DSParser(context, resources, resourceID, generateMipMap);
			case MD2:
				return new MD2Parser(context, resources, resourceID, generateMipMap);
		}
		
		return null;
	}
}
