package buffalo3d.parser;

import buffalo3d.animation.AnimationObject3d;
import buffalo3d.core.Object3dContainer;

/**
 * Interface for 3D object parsers
 * 
 * @author dennis.ippel
 *
 */
public interface IParser {
    /**
     * Parse specified model and store into memory temporarily.
     * Override this in the concrete parser
     */
	public void parse();

    /**
     * Return a container which contain parsed model object.
     * Override this in the concrete parser
     *
     * @return container which contain parsed model object
     */
	public Object3dContainer getParsedObject();

    /**
     * Return a parsed animation object from animation model.
     * Override this in the concrete parser if applicable
     *
     * @return animation object which contain parsed model object
     */
	public AnimationObject3d getParsedAnimationObject();
}
