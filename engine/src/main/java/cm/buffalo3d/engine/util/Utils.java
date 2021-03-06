package cm.buffalo3d.engine.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import cm.buffalo3d.engine.core.Object3d;


public class Utils 
{
	public static final float DEG = (float)(Math.PI / 180f);
		
	private static final int BYTES_PER_FLOAT = 4;  
	
    /**
     * Convenience method to create a Bitmap given a Context's drawable resource
     * ID.
     *
     * @param $context
     * @param $id resource id
     * @param transparency determine if resource bitmap support alpha channel
     * @return bitmap of decoded resource
     */
    public static Bitmap makeBitmapFromResourceId(Context $context, int $id, boolean transparency) {
        Bitmap bitmap;
        if (transparency) {
            bitmap = BitmapFactory.decodeResource($context.getResources(), $id);
        } else {
            InputStream is = $context.getResources().openRawResource($id);

            try {
                bitmap = BitmapFactory.decodeStream(is);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    // Ignore.
                }
            }
        }

        return bitmap;
    }

    /**
     * Convenience method to create a Bitmap given a Context's drawable resource
     * ID.
     *
     * @param $context
     * @param $id resource id
     * @return bitmap of decoded resource
     */
    public static Bitmap makeBitmapFromResourceId(Context $context, int $id) {
        return makeBitmapFromResourceId($context, $id, false);
    }

	/**
	 * Add two triangles to the Object3d's faces using the supplied indices
	 */
	public static void addQuad(Object3d $o, int $upperLeft, int $upperRight, int $lowerRight, int $lowerLeft)
	{
		$o.getFaces().add((short)$upperLeft, (short)$lowerRight, (short)$upperRight);
		$o.getFaces().add((short)$upperLeft, (short)$lowerLeft, (short)$lowerRight);
	}
	
	public static FloatBuffer makeFloatBuffer3(float $a, float $b, float $c)
	{
		ByteBuffer b = ByteBuffer.allocateDirect(3 * BYTES_PER_FLOAT);
		b.order(ByteOrder.nativeOrder());
		FloatBuffer buffer = b.asFloatBuffer();
		buffer.put($a);
		buffer.put($b);
		buffer.put($c);
		buffer.position(0);
		return buffer;
	}

	public static FloatBuffer makeFloatBuffer4(float $a, float $b, float $c, float $d)
	{
		ByteBuffer b = ByteBuffer.allocateDirect(4 * BYTES_PER_FLOAT);
		b.order(ByteOrder.nativeOrder());
		FloatBuffer buffer = b.asFloatBuffer();
		buffer.put($a);
		buffer.put($b);
		buffer.put($c);
		buffer.put($d);
		buffer.position(0);
		return buffer;
	}
}
