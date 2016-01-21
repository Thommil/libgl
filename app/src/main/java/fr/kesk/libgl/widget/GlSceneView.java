package fr.kesk.libgl.widget;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

import fr.kesk.libgl.ContextManager;
import fr.kesk.libgl.GlContext;
import fr.kesk.libgl.ui.GlElement;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Fork of {@link GLSurfaceView} to render GlElement using FPS
 * 
 * Last check on 4.1.1
 * 
 * By default GLSceneView chooses a EGLConfig that has an RGB_888 pixel format, with at least a 16-bit depth buffer and no stencil.
 * 
 * @author Thomas MILLET
 *
 */
public class GlSceneView extends SurfaceView implements SurfaceHolder.Callback, ContextManager {
    
	private final static String TAG = GlSceneView.class.getName();
    
    private static final GLThreadManager sGLThreadManager = new GLThreadManager();
    
    private boolean listenFps = false;
    
    private FPSListener fpsListener; 
    
    private final WeakReference<GlSceneView> mThisWeakRef = new WeakReference<GlSceneView>(this);
    private GLThread mGLThread;
    protected GlElement rootElement;
    private boolean mDetached;
    private EGLConfigChooser mEGLConfigChooser;
    private EGLContextFactory mEGLContextFactory;
    private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
    private boolean mPreserveEGLContextOnPause;
    protected final GlContext glContext; 
    
    /**
     * Standard View constructor. In order to render something, you
     * must call {@link #setRenderer} to register a renderer.
     */
    public GlSceneView(Context context) {
        super(context);
        this.glContext = new GlContext();
        init();
    }

    /**
     * Standard View constructor. In order to render something, you
     * must call {@link #setRenderer} to register a renderer.
     */
    public GlSceneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.glContext = new GlContext();
        init();
    }
    
    /**
     * Set the FPS listener for debug/QoS purpose
     * 
     * @param fpsListener The FPS listener receiving FPS events
     */
    public void setFPSListener(final FPSListener fpsListener){
    	this.listenFps = true;
    	this.fpsListener = fpsListener;
    	if(this.mGLThread != null){
    		this.mGLThread.listenFPS = this.listenFps;
    		this.mGLThread.fpsListener = this.fpsListener;
    	}
    	
    }
    
    /**
     * Removes this current FPSListener
     */
    public void removeFPSListener(){
    	this.listenFps = false;
    	this.fpsListener = null;
    }
    
	@Override
    protected void finalize() throws Throwable {
        try {
            if (mGLThread != null) {
                // GLThread may still be running if this view was never
                // attached to a window.
                mGLThread.requestExitAndWait();
            }
        } finally {
            super.finalize();
        }
    }

    private void init() {
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        // setFormat is done by SurfaceView in SDK 2.3 and newer. Uncomment
        // this statement if back-porting to 2.2 or older:
        holder.setFormat(PixelFormat.RGB_565);
        //
        // setType is not needed for SDK 2.0 or newer. Uncomment this
        // statement if back-porting this code to older SDKs.
        // holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
    }

    /**
     * Control whether the EGL context is preserved when the GLSceneView is paused and
     * resumed.
     * <p>
     * If set to true, then the EGL context may be preserved when the GLSceneView is paused.
     * Whether the EGL context is actually preserved or not depends upon whether the
     * Android device that the program is running on can support an arbitrary number of EGL
     * contexts or not. Devices that can only support a limited number of EGL contexts must
     * release the  EGL context in order to allow multiple applications to share the GPU.
     * <p>
     * If set to false, the EGL context will be released when the GLSceneView is paused,
     * and recreated when the GLSceneView is resumed.
     * <p>
     *
     * The default is false.
     *
     * @param preserveOnPause preserve the EGL context when paused
     */
    public void setPreserveEGLContextOnPause(boolean preserveOnPause) {
        mPreserveEGLContextOnPause = preserveOnPause;
    }

    /**
     * @return true if the EGL context will be preserved when paused
     */
    public boolean getPreserveEGLContextOnPause() {
        return mPreserveEGLContextOnPause;
    }

    /**
     * Set the renderer associated with this view. Also starts the thread that
     * will call the renderer, which in turn causes the rendering to start.
     * <p>This method should be called once and only once in the life-cycle of
     * a GLSceneView.
     * <p>The following GLSceneView methods can only be called <em>before</em>
     * setRootElement is called:
     * <ul>
     * <li>{@link #setEGLConfigChooser(boolean)}
     * <li>{@link #setEGLConfigChooser(EGLConfigChooser)}
     * <li>{@link #setEGLConfigChooser(int, int, int, int, int, int)}
     * </ul>
     * <p>
     * The following GLSceneView methods can only be called <em>after</em>
     * setRootElement is called:
     * <ul>
     * <li>{@link #getRenderMode()}
     * <li>{@link #onPause()}
     * <li>{@link #onResume()}
     * <li>{@link #queueEvent(Runnable)}
     * <li>{@link #requestRender()}
     * <li>{@link #setRenderMode(int)}
     * </ul>
     *
     * @param renderer the renderer to use to perform OpenGL drawing.
     */
    public void setRootElement(final GlElement rootElement) {
        if (mEGLConfigChooser == null) {
            mEGLConfigChooser = new SimpleEGLConfigChooser(true);
        }
        if (mEGLContextFactory == null) {
            mEGLContextFactory = new DefaultContextFactory();
        }
        if (mEGLWindowSurfaceFactory == null) {
            mEGLWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
        }

		this.rootElement = rootElement;
		this.play();
    }

    /* (non-Javadoc)
   	 * @see fr.kesk.libgl.ContextManager#getUIContext()
   	 */
   	@Override
   	public Context getUIContext() {
   		return this.getContext();
   	}

   	/* (non-Javadoc)
   	 * @see fr.kesk.libgl.ContextManager#getGLContext()
   	 */
   	@Override
   	public GlContext getGLContext() {
   		return this.glContext;
   	}

	/**
     * Install a custom EGLContextFactory.
     * <p>If this method is
     * called, it must be called before {@link #setRenderer(Renderer)}
     * is called.
     * <p>
     * If this method is not called, then by default
     * a context will be created with no shared context and
     * with a null attribute list.
     */
    public void setEGLContextFactory(EGLContextFactory factory) {
        mEGLContextFactory = factory;
    }

	/**
     * Install a custom EGLWindowSurfaceFactory.
     * <p>If this method is
     * called, it must be called before {@link #setRenderer(Renderer)}
     * is called.
     * <p>
     * If this method is not called, then by default
     * a window surface will be created with a null attribute list.
     */
    public void setEGLWindowSurfaceFactory(EGLWindowSurfaceFactory factory) {
        mEGLWindowSurfaceFactory = factory;
    }

    /**
     * Install a custom EGLConfigChooser.
     * <p>If this method is
     * called, it must be called before {@link #setRenderer(Renderer)}
     * is called.
     * <p>
     * If no setEGLConfigChooser method is called, then by default the
     * view will choose an EGLConfig that is compatible with the current
     * android.view.Surface, with a depth buffer depth of
     * at least 16 bits.
     * @param configChooser
     */
    public void setEGLConfigChooser(EGLConfigChooser configChooser) {
        mEGLConfigChooser = configChooser;
    }

    /**
     * Install a config chooser which will choose a config
     * as close to 16-bit RGB as possible, with or without an optional depth
     * buffer as close to 16-bits as possible.
     * <p>If this method is
     * called, it must be called before {@link #setRenderer(Renderer)}
     * is called.
     * <p>
     * If no setEGLConfigChooser method is called, then by default the
     * view will choose an RGB_565 surface with a depth buffer depth of
     * at least 16 bits.
     *
     * @param needDepth
     */
    public void setEGLConfigChooser(boolean needDepth) {
        setEGLConfigChooser(new SimpleEGLConfigChooser(needDepth));
    }

    /**
     * Install a config chooser which will choose a config
     * with at least the specified depthSize and stencilSize,
     * and exactly the specified redSize, greenSize, blueSize and alphaSize.
     * <p>If this method is
     * called, it must be called before {@link #setRenderer(Renderer)}
     * is called.
     * <p>
     * If no setEGLConfigChooser method is called, then by default the
     * view will choose an RGB_565 surface with a depth buffer depth of
     * at least 16 bits.
     *
     */
    public void setEGLConfigChooser(int redSize, int greenSize, int blueSize,
            int alphaSize, int depthSize, int stencilSize) {
        setEGLConfigChooser(new ComponentSizeChooser(redSize, greenSize,
                blueSize, alphaSize, depthSize, stencilSize));
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of GLSceneView.
     */
    public void surfaceCreated(SurfaceHolder holder) {
    	if (mGLThread != null) {
    		mGLThread.surfaceCreated();
    	}	
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of GLSceneView.
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return
    	if (mGLThread != null) {
    		mGLThread.surfaceDestroyed();
    	}	
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of GLSceneView.
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    	if (mGLThread != null) {
    		mGLThread.onWindowResize(w, h);
    	}	
    }
    
    /**
     * Inform the view that the activity is paused. The owner of this view must
     * call this method when the activity is paused. Calling this method will
     * pause the rendering thread.
     * Must not be called before a renderer has been set.
     */
    public void pause() {
    	if (mGLThread != null) {
    		mGLThread.onPause();
    	}	
    }
    
    /**
     * Inform the view that the activity is resumed. The owner of this view must
     * call this method when the activity is resumed. Calling this method will
     * recreate the OpenGL display and resume the rendering
     * thread.
     * Must not be called before a renderer has been set.
     */
    public void play() {
    	if (mGLThread != null) {
    		mGLThread.onResume();
    	}	
    	else{
    		mGLThread = new GLThread(mThisWeakRef);
    		mGLThread.listenFPS = this.listenFps;
    		mGLThread.fpsListener = this.fpsListener;
            mGLThread.start();
    	}
    }

    /**
     * Queue a runnable to be run on the GL rendering thread. This can be used
     * to communicate with the Renderer on the rendering thread.
     * Must not be called before a renderer has been set.
     * @param r the runnable to be run on the GL rendering thread.
     */
   	@Override
    public void runOnGLThread(Runnable r) {
    	if (mGLThread != null) {
    		mGLThread.queueEvent(r);
    	}	
    }
    
    /**
     * Queue a runnable to be run on the UI rendering thread.
     * @param r the runnable to be run on the GL rendering thread.
     */
   	@Override
    public void runOnUIThread(Runnable r) {
        ((Activity)this.getContext()).runOnUiThread(r);
    }

    /**
     * This method is used as part of the View class and is not normally
     * called or subclassed by clients of GLSceneView.
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mDetached && (rootElement != null)) {
           this.play();
        }
        mDetached = false;
    }

    /**
     * This method is used as part of the View class and is not normally
     * called or subclassed by clients of GLSceneView.
     * Must not be called before a renderer has been set.
     */
    @Override
    protected void onDetachedFromWindow() {
        if (mGLThread != null) {
            mGLThread.requestExitAndWait();
        }
        mDetached = true;
        super.onDetachedFromWindow();
    }

    // ----------------------------------------------------------------------

    /**
     * An interface for customizing the eglCreateContext and eglDestroyContext calls.
     * <p>
     * This interface must be implemented by clients wishing to call
     * {@link GlSceneView#setEGLContextFactory(EGLContextFactory)}
     */
    public interface EGLContextFactory {
        EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig);
        void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context);
    }

    private class DefaultContextFactory implements EGLContextFactory {
        private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
        private static final int GL_VERSION = 2;
    	
        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
            int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, GL_VERSION,
                    EGL10.EGL_NONE };

            return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, attrib_list);
        }

        public void destroyContext(EGL10 egl, EGLDisplay display,
                EGLContext context) {
            if (!egl.eglDestroyContext(display, context)) {
            	android.util.Log.e("DefaultContextFactory", "display:" + display + " context: " + context);
                EglHelper.throwEglException("eglDestroyContex", egl.eglGetError());
            }
        }
    }

    /**
     * An interface for customizing the eglCreateWindowSurface and eglDestroySurface calls.
     * <p>
     * This interface must be implemented by clients wishing to call
     * {@link GlSceneView#setEGLWindowSurfaceFactory(EGLWindowSurfaceFactory)}
     */
    public interface EGLWindowSurfaceFactory {
        /**
         *  @return null if the surface cannot be constructed.
         */
        EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config,
                Object nativeWindow);
        void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface);
    }

    private static class DefaultWindowSurfaceFactory implements EGLWindowSurfaceFactory {

        public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display,
                EGLConfig config, Object nativeWindow) {
            EGLSurface result = null;
            try {
                result = egl.eglCreateWindowSurface(display, config, nativeWindow, null);
            } catch (IllegalArgumentException e) {
                // This exception indicates that the surface flinger surface
                // is not valid. This can happen if the surface flinger surface has
                // been torn down, but the application has not yet been
                // notified via SurfaceHolder.Callback.surfaceDestroyed.
                // In theory the application should be notified first,
                // but in practice sometimes it is not. See b/4588890
            	android.util.Log.e(TAG, "eglCreateWindowSurface", e);
            }
            return result;
        }

        public void destroySurface(EGL10 egl, EGLDisplay display,
                EGLSurface surface) {
            egl.eglDestroySurface(display, surface);
        }
    }

    /**
     * An interface for choosing an EGLConfig configuration from a list of
     * potential configurations.
     * <p>
     * This interface must be implemented by clients wishing to call
     * {@link GlSceneView#setEGLConfigChooser(EGLConfigChooser)}
     */
    public interface EGLConfigChooser {
        /**
         * Choose a configuration from the list. Implementors typically
         * implement this method by calling
         * {@link EGL10#eglChooseConfig} and iterating through the results. Please consult the
         * EGL specification available from The Khronos Group to learn how to call eglChooseConfig.
         * @param egl the EGL10 for the current display.
         * @param display the current display.
         * @return the chosen configuration.
         */
        EGLConfig chooseConfig(EGL10 egl, EGLDisplay display);
    }

    private abstract class BaseConfigChooser
            implements EGLConfigChooser {
    	
        public BaseConfigChooser(int[] configSpec) {
            mConfigSpec = filterConfigSpec(configSpec);
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] num_config = new int[1];
            if (!egl.eglChooseConfig(display, mConfigSpec, null, 0,
                    num_config)) {
                throw new IllegalArgumentException("eglChooseConfig failed");
            }

            int numConfigs = num_config[0];

            if (numConfigs <= 0) {
                throw new IllegalArgumentException(
                        "No configs match configSpec");
            }

            EGLConfig[] configs = new EGLConfig[numConfigs];
            if (!egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs,
                    num_config)) {
                throw new IllegalArgumentException("eglChooseConfig#2 failed");
            }
            EGLConfig config = chooseConfig(egl, display, configs);
            if (config == null) {
                throw new IllegalArgumentException("No config chosen");
            }
            return config;
        }

        abstract EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                EGLConfig[] configs);

        protected int[] mConfigSpec;

        private int[] filterConfigSpec(int[] configSpec) {
            /* We know none of the subclasses define EGL_RENDERABLE_TYPE.
             * And we know the configSpec is well formed.
             */
            int len = configSpec.length;
            int[] newConfigSpec = new int[len + 2];
            System.arraycopy(configSpec, 0, newConfigSpec, 0, len-1);
            newConfigSpec[len-1] = EGL10.EGL_RENDERABLE_TYPE;
            newConfigSpec[len] = 4; /* EGL_OPENGL_ES2_BIT */
            newConfigSpec[len+1] = EGL10.EGL_NONE;
            return newConfigSpec;
        }
    }

    /**
     * Choose a configuration with exactly the specified r,g,b,a sizes,
     * and at least the specified depth and stencil sizes.
     */
    private class ComponentSizeChooser extends BaseConfigChooser {
        public ComponentSizeChooser(int redSize, int greenSize, int blueSize,
                int alphaSize, int depthSize, int stencilSize) {
            super(new int[] {
                    EGL10.EGL_RED_SIZE, redSize,
                    EGL10.EGL_GREEN_SIZE, greenSize,
                    EGL10.EGL_BLUE_SIZE, blueSize,
                    EGL10.EGL_ALPHA_SIZE, alphaSize,
                    EGL10.EGL_DEPTH_SIZE, depthSize,
                    EGL10.EGL_STENCIL_SIZE, stencilSize,
                    EGL10.EGL_NONE});
            mValue = new int[1];
            mRedSize = redSize;
            mGreenSize = greenSize;
            mBlueSize = blueSize;
            mAlphaSize = alphaSize;
            mDepthSize = depthSize;
            mStencilSize = stencilSize;
       }

        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                EGLConfig[] configs) {
            for (EGLConfig config : configs) {
                int d = findConfigAttrib(egl, display, config,
                        EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config,
                        EGL10.EGL_STENCIL_SIZE, 0);
                if ((d >= mDepthSize) && (s >= mStencilSize)) {
                    int r = findConfigAttrib(egl, display, config,
                            EGL10.EGL_RED_SIZE, 0);
                    int g = findConfigAttrib(egl, display, config,
                             EGL10.EGL_GREEN_SIZE, 0);
                    int b = findConfigAttrib(egl, display, config,
                              EGL10.EGL_BLUE_SIZE, 0);
                    int a = findConfigAttrib(egl, display, config,
                            EGL10.EGL_ALPHA_SIZE, 0);
                    if ((r == mRedSize) && (g == mGreenSize)
                            && (b == mBlueSize) && (a == mAlphaSize)) {
                        return config;
                    }
                }
            }
            return null;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display,
                EGLConfig config, int attribute, int defaultValue) {

            if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
                return mValue[0];
            }
            return defaultValue;
        }

        private int[] mValue;
        // Subclasses can adjust these values:
        protected int mRedSize;
        protected int mGreenSize;
        protected int mBlueSize;
        protected int mAlphaSize;
        protected int mDepthSize;
        protected int mStencilSize;
        }

    /**
     * This class will choose a RGB_565 surface with
     * or without a depth buffer.
     *
     */
    private class SimpleEGLConfigChooser extends ComponentSizeChooser {
        public SimpleEGLConfigChooser(boolean withDepthBuffer) {
            super(5, 6, 5, 0, withDepthBuffer ? 16 : 0, 0);
        }
    }

    /**
     * An EGL helper class.
     */

    private static class EglHelper {
        public EglHelper(WeakReference<GlSceneView> GLSceneViewWeakRef) {
            mGLSceneViewWeakRef = GLSceneViewWeakRef;
        }

        /**
         * Initialize EGL for a given configuration spec.
         * @param configSpec
         */
        public void start() {
            /*
             * Get an EGL instance
             */
            mEgl = (EGL10) EGLContext.getEGL();

            /*
             * Get to the default display.
             */
            mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

            if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
                throw new RuntimeException("eglGetDisplay failed");
            }

            /*
             * We can now initialize EGL for that display
             */
            int[] version = new int[2];
            if(!mEgl.eglInitialize(mEglDisplay, version)) {
                throw new RuntimeException("eglInitialize failed");
            }
            GlSceneView view = mGLSceneViewWeakRef.get();
            if (view == null) {
                mEglConfig = null;
                mEglContext = null;
            } else {
                mEglConfig = view.mEGLConfigChooser.chooseConfig(mEgl, mEglDisplay);

                /*
                * Create an EGL context. We want to do this as rarely as we can, because an
                * EGL context is a somewhat heavy object.
                */
                mEglContext = view.mEGLContextFactory.createContext(mEgl, mEglDisplay, mEglConfig);
            }
            if (mEglContext == null || mEglContext == EGL10.EGL_NO_CONTEXT) {
                mEglContext = null;
                throwEglException("createContext");
            }

            mEglSurface = null;
        }

        /**
         * Create an egl surface for the current SurfaceHolder surface. If a surface
         * already exists, destroy it before creating the new surface.
         *
         * @return true if the surface was created successfully.
         */
        public boolean createSurface() {
            /*
             * Check preconditions.
             */
            if (mEgl == null) {
                throw new RuntimeException("egl not initialized");
            }
            if (mEglDisplay == null) {
                throw new RuntimeException("eglDisplay not initialized");
            }
            if (mEglConfig == null) {
                throw new RuntimeException("mEglConfig not initialized");
            }

            /*
             *  The window size has changed, so we need to create a new
             *  surface.
             */
            destroySurfaceImp();

            /*
             * Create an EGL surface we can render into.
             */
            GlSceneView view = mGLSceneViewWeakRef.get();
            if (view != null) {
                mEglSurface = view.mEGLWindowSurfaceFactory.createWindowSurface(mEgl,
                        mEglDisplay, mEglConfig, view.getHolder());
            } else {
                mEglSurface = null;
            }

            if (mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE) {
                int error = mEgl.eglGetError();
                if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                	android.util.Log.e("EglHelper", "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.");
                }
                return false;
            }

            /*
             * Before we can issue GL commands, we need to make sure
             * the context is current and bound to a surface.
             */
            if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
                /*
                 * Could not make the context current, probably because the underlying
                 * SurfaceView surface has been destroyed.
                 */
                logEglErrorAsWarning("EGLHelper", "eglMakeCurrent", mEgl.eglGetError());
                return false;
            }

            return true;
        }

        /**
         * Create a GL object for the current EGL context.
         * @return
         */
        GL createGL() {
            return mEglContext.getGL();
        }

        /**
         * Display the current render surface.
         * @return the EGL error code from eglSwapBuffers.
         */
        public int swap() {
            if (! mEgl.eglSwapBuffers(mEglDisplay, mEglSurface)) {
                return mEgl.eglGetError();
            }
            return EGL10.EGL_SUCCESS;
        }

        public void destroySurface() {
        	final GlSceneView view = mGLSceneViewWeakRef.get();
        	if(view != null){
        		view.rootElement.free();
        	}
            destroySurfaceImp();
        }

        private void destroySurfaceImp() {
            if (mEglSurface != null && mEglSurface != EGL10.EGL_NO_SURFACE) {
                mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_CONTEXT);
                GlSceneView view = mGLSceneViewWeakRef.get();
                if (view != null) {
                    view.mEGLWindowSurfaceFactory.destroySurface(mEgl, mEglDisplay, mEglSurface);
                }
                mEglSurface = null;
            }
        }

        public void finish() {
            if (mEglContext != null) {
                GlSceneView view = mGLSceneViewWeakRef.get();
                if (view != null) {
                    view.mEGLContextFactory.destroyContext(mEgl, mEglDisplay, mEglContext);
                }
                mEglContext = null;
            }
            if (mEglDisplay != null) {
                mEgl.eglTerminate(mEglDisplay);
                mEglDisplay = null;
            }
        }

        private void throwEglException(String function) {
            throwEglException(function, mEgl.eglGetError());
        }

        public static void throwEglException(String function, int error) {
            String message = formatEglError(function, error);
            throw new RuntimeException(message);
        }

        public static void logEglErrorAsWarning(String tag, String function, int error) {
        	android.util.Log.w(tag, formatEglError(function, error));
        }

        public static String formatEglError(String function, int error) {
            return function + " failed: " + error;
        }

        private WeakReference<GlSceneView> mGLSceneViewWeakRef;
        EGL10 mEgl;
        EGLDisplay mEglDisplay;
        EGLSurface mEglSurface;
        EGLConfig mEglConfig;
        EGLContext mEglContext;

    }

    /**
     * A generic GL Thread. Takes care of initializing EGL and GL. Delegates
     * to a Renderer instance to do the actual drawing. Can be configured to
     * render continuously or on request.
     *
     * All potentially blocking synchronization is done through the
     * sGLThreadManager object. This avoids multiple-lock ordering issues.
     *
     */
    static class GLThread extends Thread {
    	
    	public boolean listenFPS = false;
    	
    	public FPSListener fpsListener;
    	
    	
    	GLThread(WeakReference<GlSceneView> GLSceneViewWeakRef) {
            super();
            mWidth = 0;
            mHeight = 0;
            mGLSceneViewWeakRef = GLSceneViewWeakRef;
        }

        @Override
        public void run() {
            setName("GLThread " + getId());

            try {
                guardedRun();
            } catch (InterruptedException e) {
                // fall thru and exit normally
            } finally {
                sGLThreadManager.threadExiting(this);
            }
        }

        /*
         * This private method should only be called inside a
         * synchronized(sGLThreadManager) block.
         */
        private void stopEglSurfaceLocked() {
            if (mHaveEglSurface) {
                mHaveEglSurface = false;
                mEglHelper.destroySurface();
            }
        }

        /*
         * This private method should only be called inside a
         * synchronized(sGLThreadManager) block.
         */
        private void stopEglContextLocked() {
            if (mHaveEglContext) {
                mEglHelper.finish();
                mHaveEglContext = false;
                sGLThreadManager.releaseEglContextLocked(this);
            }
        }
        private void guardedRun() throws InterruptedException {
            mEglHelper = new EglHelper(mGLSceneViewWeakRef);
            mHaveEglContext = false;
            mHaveEglSurface = false;
            try {
                GL10 gl = null;
                final int[] fpsBuffer= new int[100];;
				int fpsBufferIndex=0;
                boolean createEglContext = false;
                boolean createEglSurface = false;
                boolean createGlInterface = false;
                boolean lostEglContext = false;
                boolean sizeChanged = false;
                boolean wantRenderNotification = false;
                boolean doRenderNotification = false;
                boolean askedToReleaseEglContext = false;
                long lastDrawTime = 0;
                long elapsedTime = 0;
                int w = 0;
                int h = 0;
                Runnable event = null;

                while (true) {
                    synchronized (sGLThreadManager) {
                        while (true) {
                            if (mShouldExit) {
                                return;
                            }

                            if (! mEventQueue.isEmpty()) {
                                event = mEventQueue.remove(0);
                                break;
                            }

                            // Update the pause state.
                            boolean pausing = false;
                            if (mPaused != mRequestPaused) {
                                pausing = mRequestPaused;
                                mPaused = mRequestPaused;
                                sGLThreadManager.notifyAll();
                            }

                            // Do we need to give up the EGL context?
                            if (mShouldReleaseEglContext) {
                                stopEglSurfaceLocked();
                                stopEglContextLocked();
                                mShouldReleaseEglContext = false;
                                askedToReleaseEglContext = true;
                            }

                            // Have we lost the EGL context?
                            if (lostEglContext) {
                                stopEglSurfaceLocked();
                                stopEglContextLocked();
                                lostEglContext = false;
                            }

                            if(pausing){
	                            // When pausing, release the EGL surface:
	                            if (mHaveEglSurface) {
	                                stopEglSurfaceLocked();
	                            }
	
	                            // When pausing, optionally release the EGL Context:
	                            if (mHaveEglContext) {
	                                GlSceneView view = mGLSceneViewWeakRef.get();
	                                boolean preserveEglContextOnPause = view == null ?
	                                        false : view.mPreserveEGLContextOnPause;
	                                if (!preserveEglContextOnPause || sGLThreadManager.shouldReleaseEGLContextWhenPausing()) {
	                                    stopEglContextLocked();
	                                }
	                            }
                            }

                            // Have we lost the SurfaceView surface?
                            if ((! mHasSurface) && (! mWaitingForSurface)) {
                                if (mHaveEglSurface) {
                                    stopEglSurfaceLocked();
                                }
                                mWaitingForSurface = true;
                                sGLThreadManager.notifyAll();
                            }

                            // Have we acquired the surface view surface?
                            if (mHasSurface && mWaitingForSurface) {
                                mWaitingForSurface = false;
                                sGLThreadManager.notifyAll();
                            }

                            if (doRenderNotification) {
                                wantRenderNotification = false;
                                doRenderNotification = false;
                                mRenderComplete = true;
                                sGLThreadManager.notifyAll();
                            }

                            // Ready to draw?
                            if (readyToDraw()) {

                                // If we don't have an EGL context, try to acquire one.
                                if (! mHaveEglContext) {
                                    if (askedToReleaseEglContext) {
                                        askedToReleaseEglContext = false;
                                    } else if (sGLThreadManager.tryAcquireEglContextLocked(this)) {
                                        try {
                                            mEglHelper.start();
                                        } catch (RuntimeException t) {
                                            sGLThreadManager.releaseEglContextLocked(this);
                                            throw t;
                                        }
                                        mHaveEglContext = true;
                                        createEglContext = true;
                                        mContextChanged = true;

                                        sGLThreadManager.notifyAll();
                                    }
                                }

                                if (mHaveEglContext && !mHaveEglSurface) {
                                    mHaveEglSurface = true;
                                    createEglSurface = true;
                                    mContextChanged = true;
                                    createGlInterface = true;
                                    sizeChanged = true;
                                }

                                if (mHaveEglSurface) {
                                    if (mSizeChanged) {
                                        sizeChanged = true;
                                        w = mWidth;
                                        h = mHeight;
                                        wantRenderNotification = true;

                                        // Destroy and recreate the EGL surface.
                                        createEglSurface = true;
                                        mContextChanged = true;

                                        mSizeChanged = false;
                                    }
                                    sGLThreadManager.notifyAll();
                                    break;
                                }
                            }

                            sGLThreadManager.wait();
                        }
                    } // end of synchronized(sGLThreadManager)

                    if(mContextChanged){
	                    if (createEglSurface) {
	                        if (!mEglHelper.createSurface()) {
	                            continue;
	                        }
	                        createEglSurface = false;
	                    }
	
	                    if (createGlInterface) {
	                        gl = (GL10) mEglHelper.createGL();
	
	                        sGLThreadManager.checkGLDriver(gl);
	                        createGlInterface = false;
	                    }
	
	                    if (createEglContext) {
	                        GlSceneView view = mGLSceneViewWeakRef.get();
	                        if (view != null) {
	                            view.rootElement.create(view);
	                        }
	                        createEglContext = false;
	                    }
	
	                    if (sizeChanged) {
	                        GlSceneView view = mGLSceneViewWeakRef.get();
	                        if (view != null) {
	                        	view.rootElement.resize(w, h);
	                        }
	                        sizeChanged = false;
	                    }
	                    
	                    lastDrawTime = 0;
                        elapsedTime = 0;
	                    mContextChanged = false;
                    } 

                    {
                        GlSceneView view = mGLSceneViewWeakRef.get();
                        if (view != null) {
                        	final long currentTime = SystemClock.elapsedRealtime(); 
                        	if(lastDrawTime > 0){
                            	elapsedTime = currentTime - lastDrawTime;
                            }
                            
                            if(this.listenFPS){
	                        	if(fpsBufferIndex < fpsBuffer.length){
	                        		fpsBuffer[fpsBufferIndex++] = (int)elapsedTime;
	                        	}
	                        	else{
	                        		int total = 0;
	                        		for(int v : fpsBuffer){
	                        			total += v;
	                        		}
	                        		if(total > fpsBufferIndex){
	                        			this.fpsListener.onFpsRefresh(1000/(total/fpsBufferIndex));
	                        		}
	                        		fpsBufferIndex = 0;
	                        	}
                            }
                            
                            lastDrawTime = currentTime;
                            
                        	view.rootElement.layout(elapsedTime);
                            
                        	view.rootElement.draw();
                        }
                    }
                    
                    {
	                    if (event != null) {
	                        event.run();
	                        event = null;
	                    }
                    }    
	                 
                    try{
                    	int swapError = mEglHelper.swap();
                    	switch (swapError) {
                        case EGL10.EGL_SUCCESS:
                            break;
                        case EGL11.EGL_CONTEXT_LOST:
                            lostEglContext = true;
                            break;
                    }
                    }catch(IllegalArgumentException ile){
                    	lostEglContext = true;
                        break;
                    }
                    
                    if (wantRenderNotification) {
                        doRenderNotification = true;
                    }
                }

            } finally {
                /*
                 * clean-up everything...
                 */
                synchronized (sGLThreadManager) {
                	//mGLSceneViewWeakRef.get().rootElement.free();	
                    stopEglSurfaceLocked();
                    stopEglContextLocked();
                }
            }
        }
        
        public boolean ableToDraw() {
            return mHaveEglContext && mHaveEglSurface && readyToDraw();
        }

        private boolean readyToDraw() {
            return !mPaused && mHasSurface && mWidth > 0;
        }

        public void surfaceCreated() {
            synchronized(sGLThreadManager) {
                mHasSurface = true;
                sGLThreadManager.notifyAll();
                while((mWaitingForSurface) && (!mExited)) {
                    try {
                        sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void surfaceDestroyed() {
            synchronized(sGLThreadManager) {
                mHasSurface = false;
                sGLThreadManager.notifyAll();
                while((!mWaitingForSurface) && (!mExited)) {
                    try {
                        sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onPause() {
            synchronized (sGLThreadManager) {
                mRequestPaused = true;
                sGLThreadManager.notifyAll();
                while ((! mExited) && (! mPaused)) {
                    try {
                        sGLThreadManager.wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onResume() {
            synchronized (sGLThreadManager) {
                mRequestPaused = false;
                mRenderComplete = false;
                sGLThreadManager.notifyAll();
                while ((! mExited) && mPaused && (!mRenderComplete)) {
                    try {
                        sGLThreadManager.wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onWindowResize(int w, int h) {
            synchronized (sGLThreadManager) {
                mWidth = w;
                mHeight = h;
                mSizeChanged = true;
                mRenderComplete = false;
                sGLThreadManager.notifyAll();

                // Wait for thread to react to resize and render a frame
                while (! mExited && !mPaused && !mRenderComplete
                        && ableToDraw()) {
                    try {
                        sGLThreadManager.wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void requestExitAndWait() {
            // don't call this from GLThread thread or it is a guaranteed
            // deadlock!
            synchronized(sGLThreadManager) {
                mShouldExit = true;
                sGLThreadManager.notifyAll();
                while (! mExited) {
                    try {
                        sGLThreadManager.wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void requestReleaseEglContextLocked() {
            mShouldReleaseEglContext = true;
            sGLThreadManager.notifyAll();
        }

        /**
         * Queue an "event" to be run on the GL rendering thread.
         * @param r the runnable to be run on the GL rendering thread.
         */
        public void queueEvent(Runnable r) {
            synchronized(sGLThreadManager) {
                mEventQueue.add(r);
                sGLThreadManager.notifyAll();
            }
        }

        // Once the thread is started, all accesses to the following member
        // variables are protected by the sGLThreadManager monitor
        private boolean mContextChanged;
        private boolean mShouldExit;
        private boolean mExited;
        private boolean mRequestPaused;
        private boolean mPaused;
        private boolean mHasSurface;
        private boolean mWaitingForSurface;
        private boolean mHaveEglContext;
        private boolean mHaveEglSurface;
        private boolean mShouldReleaseEglContext;
        private int mWidth;
        private int mHeight;

        private boolean mRenderComplete;
        private ArrayList<Runnable> mEventQueue = new ArrayList<Runnable>();
        private boolean mSizeChanged = true;

        // End of member variables protected by the sGLThreadManager monitor.

        private EglHelper mEglHelper;

        /**
         * Set once at thread construction time, nulled out when the parent view is garbage
         * called. This weak reference allows the GLSceneView to be garbage collected while
         * the GLThread is still alive.
         */
        private WeakReference<GlSceneView> mGLSceneViewWeakRef;

    }

    private static class GLThreadManager {
    	
        public synchronized void threadExiting(GLThread thread) {
            thread.mExited = true;
            if (mEglOwner == thread) {
                mEglOwner = null;
            }
            notifyAll();
        }

        /*
         * Tries once to acquire the right to use an EGL
         * context. Does not block. Requires that we are already
         * in the sGLThreadManager monitor when this is called.
         *
         * @return true if the right to use an EGL context was acquired.
         */
        public boolean tryAcquireEglContextLocked(GLThread thread) {
            if (mEglOwner == thread || mEglOwner == null) {
                mEglOwner = thread;
                notifyAll();
                return true;
            }

            if (mMultipleGLESContextsAllowed) {
                return true;
            }
            // Notify the owning thread that it should release the context.
            // TODO: implement a fairness policy. Currently
            // if the owning thread is drawing continuously it will just
            // reacquire the EGL context.
            if (mEglOwner != null) {
                mEglOwner.requestReleaseEglContextLocked();
            }
            return false;
        }

        /*
         * Releases the EGL context. Requires that we are already in the
         * sGLThreadManager monitor when this is called.
         */
        public void releaseEglContextLocked(GLThread thread) {
            if (mEglOwner == thread) {
                mEglOwner = null;
            }
            notifyAll();
        }

        public synchronized boolean shouldReleaseEGLContextWhenPausing() {
            // Release the EGL context when pausing even if
            // the hardware supports multiple EGL contexts.
            // Otherwise the device could run out of EGL contexts.
            return mLimitedGLESContexts;
        }


        public synchronized void checkGLDriver(GL10 gl) {
            if (! mGLESDriverCheckComplete) {
                String renderer = gl.glGetString(GL10.GL_RENDERER);
                 mMultipleGLESContextsAllowed = ! renderer.startsWith(kMSM7K_RENDERER_PREFIX);
                 notifyAll();
            }
        }

               /**
         * This check was required for some pre-Android-3.0 hardware. Android 3.0 provides
         * support for hardware-accelerated views, therefore multiple EGL contexts are
         * supported on all Android 3.0+ EGL drivers.
         */
        private boolean mGLESDriverCheckComplete;
        private boolean mMultipleGLESContextsAllowed;
        private boolean mLimitedGLESContexts;
        private static final String kMSM7K_RENDERER_PREFIX =
            "Q3Dimension MSM7500 ";
        private GLThread mEglOwner;
    }
    
    /**
     * Listener notified of FPS QoS
     * 
     * @author Thomas MILLET
     *
     */
    public static interface FPSListener{
    	
    	/**
    	 * Called each time the FPS is calculated
    	 * 
    	 * @param fps The current FPS
    	 */
    	public void onFpsRefresh(final int fps);
    }

}
