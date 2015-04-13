package buffalo3d.util;

import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class GLHandler extends Handler {
    private final static String TAG = "GLHandler";
    private final static boolean DEBUG = false;

    GLSurfaceView mProxy;
    long mLastTime;
    GLHandlerCallback mGLHandlerCallback;

    public GLHandler(Looper looper, GLSurfaceView proxy) {
        super(looper);
        mProxy = proxy;
    }

    /**
     * Subclasses must implement this to receive messages.
     */
    public void handleMessage(Message msg) {
        if (DEBUG) {
            Log.i(TAG,"GLHandler handle message period:"+(System.currentTimeMillis() - mLastTime));
        }
        mLastTime = System.currentTimeMillis();
    }

    /**
     * Handle system messages here.
     */
    public void dispatchMessage(Message msg) {
        if (mGLHandlerCallback != null) {
            mGLHandlerCallback.onDispatchMessage(msg);
        }
        Runnable callback = msg.getCallback();
        if (callback != null) {
            if (mProxy != null) {
                mProxy.queueEvent(callback);
            }
        } else {
            Message newMsg = Message.obtain(msg);
            if (mProxy != null) {
                mProxy.queueEvent(new OneShotTask(this, newMsg));
            }
        }
    }

    class OneShotTask implements Runnable {
        GLHandler mHandler;
        Message mMsg;

        OneShotTask(GLHandler handler, Message msg) {
            mHandler = handler;
            mMsg = msg;
        }

        public void run() {
            if (mHandler != null) {
                mHandler.handleMessage(mMsg);
            }
            if (mMsg != null) {
                mMsg.recycle();
            }
        }
    }

    public void setCallback(GLHandlerCallback callback) {
        mGLHandlerCallback = callback;
    }

    public interface GLHandlerCallback {
        void onDispatchMessage(Message msg);
    }
}
