package buffalo3d.objectPrimitives;

import android.graphics.Bitmap;

import buffalo3d.core.GContext;
import buffalo3d.core.Object3dContainer;
import buffalo3d.listeners.OnTouchListener;
import buffalo3d.util.Utils;
import buffalo3d.vos.Color4;

import java.io.InputStream;


public class SkyBox extends Object3dContainer {
	private float size;
	private float halfSize;
	private int quality;
	private Color4 color;
	private Rectangle[] faces;
	
	public enum Face {
		North,
		East,
		South,
		West,
		Up,
		Down,
		All
	}
	
	public SkyBox(GContext context, float size, int quality) {
		super(context, 0, 0);
		this.size = size;
		this.halfSize = size *.5f;
		this.quality = quality;
		build();
	}
	
	private void build() {
		color = new Color4();
		faces = new Rectangle[6];
		Rectangle north = new Rectangle(mGContext, size, size, quality, quality, color);
		Rectangle east = new Rectangle(mGContext, size, size, quality, quality, color);
		Rectangle south = new Rectangle(mGContext, size, size, quality, quality, color);
		Rectangle west = new Rectangle(mGContext, size, size, quality, quality, color);
		Rectangle up = new Rectangle(mGContext, size, size, quality, quality, color);
		Rectangle down = new Rectangle(mGContext, size, size, quality, quality, color);
		
		north.position().z = halfSize;
		north.lightingEnabled(false);
		
		east.rotation().y = -90;
		east.position().x = halfSize;
		east.doubleSidedEnabled(true);
		east.lightingEnabled(false);
		
		south.rotation().y = 180;
		south.position().z = -halfSize;
		south.lightingEnabled(false);
		
		west.rotation().y = 90;
		west.position().x = -halfSize;
		west.doubleSidedEnabled(true);
		west.lightingEnabled(false);
		
		up.rotation().x = 90;
		up.position().y = halfSize;
		up.doubleSidedEnabled(true);
		up.lightingEnabled(false);
		
		down.rotation().x = -90;
		down.position().y = -halfSize;
		down.doubleSidedEnabled(true);
		down.lightingEnabled(false);
		
		faces[Face.North.ordinal()] = north;
		faces[Face.East.ordinal()] = east;
		faces[Face.South.ordinal()] = south;
		faces[Face.West.ordinal()] = west;
		faces[Face.Up.ordinal()] = up;
		faces[Face.Down.ordinal()] = down;
		
		addChild(north);
		addChild(east);
		addChild(south);
		addChild(west);
		addChild(up);
		addChild(down);

        name(SkyBox.class.getName());
        for (int i = 0; i < numChildren(); i++) {
            getChildAt(i).name(SkyBox.class.getName());
        }
	}

    public void setOnTouchListener(OnTouchListener listener) {
        for (int i = 0; i < numChildren(); i++) {
            getChildAt(i).setOnTouchListener(listener);
        }
    }

	public void addTexture(Face face, int resourceId, String id) {
		Bitmap bitmap = Utils.makeBitmapFromResourceId(mGContext.getContext(),resourceId);
		if (mGContext.getTexureManager().contains(id) == false) {
		    mGContext.getTexureManager().addTextureId(bitmap, id, true);
		}
		bitmap.recycle();
		addTexture(face, bitmap, id);
	}
	
    public void addTextureETC1(Face face, int resourceId, String id) {
        InputStream input = mGContext.getContext().getResources().openRawResource(resourceId);
        if (mGContext.getTexureManager().contains(id) == false) {
            mGContext.getTexureManager().addTextureId(input, id, false);
        }
        addTexture(face, null, id);
    }

	public void addTexture(Face face, Bitmap bitmap, String id) {
		if(face == Face.All)
		{
			for(int i=0; i<6; i++)
			{
				faces[i].getTextures().addById(id);
			}
		}
		else
		{
			faces[face.ordinal()].getTextures().addById(id);
		}
	}
}
