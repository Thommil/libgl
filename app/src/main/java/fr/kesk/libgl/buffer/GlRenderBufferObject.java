package fr.kesk.libgl.buffer;

import android.opengl.GLES20;

/**
 * Abstraction class of a renderbuffer for FBO use
 * 
 * @author Thomas MILLET
 *
 */
public class GlRenderBufferObject implements GlFrameBufferObject.Attachment{

	/**
	 * TAG log
	 */
	@SuppressWarnings("unused")
	private final static String TAG = GlRenderBufferObject.class.getName();
	
	/**
	 * Handle to use to unbind current buffer
	 */
	public static final int UNBIND_HANDLE = GLES20.GL_ZERO;
	
	/**
	 * RenderBuffer format for color buffer based on RGB565
	 */
	public static final int FORMAT_COLOR_RGB565 = GLES20.GL_RGB565;
	
	/**
	 * RenderBuffer format for color buffer based on RGBA4
	 */
	public static final int FORMAT_COLOR_RGBA4 = GLES20.GL_RGBA4;
	
	/**
	 * RenderBuffer format for color buffer based on RGB5_A1
	 */
	public static final int FORMAT_COLOR_RGB5_A1 = GLES20.GL_RGB5_A1;
	
	/**
	 * RenderBuffer format for 16bits depth buffer
	 */
	public static final int FORMAT_DEPTH_COMPONENT16 = GLES20.GL_DEPTH_COMPONENT16;
	
	/**
	 * RenderBuffer format for 8bits stencil buffer
	 */
	public static final int FORMAT_STENCIL_INDEX8 = GLES20.GL_STENCIL_INDEX8;
	
	/**
	 * The associated handle
	 */
	public final int handle;
	
	/**
	 * The format of the renderbuffer (FORMAT_*)
	 */
	public final int format;
	
	/**
	 * The width of the renderbuffer
	 */
	public final int width;
	
	/**
	 * The height of the renderbuffer
	 */
	public final int height;
	
	/**
	 * Default constructor
	 */
	public GlRenderBufferObject(final int format, final int width, final int height){
		//android.util.Log.d(TAG,"NEW");
		final int[]handles = new int[1];
		GLES20.glGenRenderbuffers(1, handles, 0);
		this.handle = handles[0];
		this.format = format;
		this.width = width;
		this.height = height;
		
		//Create buffer
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, this.handle);
		GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, this.format, this.width, this.height);
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, UNBIND_HANDLE);
	}
	
	/**
	 * Bind the current RenderBufferObject
	 */
	public void bind(){
		//android.util.Log.d(TAG,"bind()");
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, this.handle);
	}
	
	/**
	 * Unbind the current RenderBufferObject
	 */
	public void unbind(){
		//android.util.Log.d(TAG,"unbind()");
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, UNBIND_HANDLE);
	}
	/**
	 * Free resources associated with current RenderBuffer
	 */
	public void free(){
		//android.util.Log.d(TAG,"free()");
		GLES20.glDeleteRenderbuffers(1, new int[]{this.handle}, 0);
	}
	
	/* (non-Javadoc)
	 * @see fr.kesk.libgl.buffer.FrameBufferObject.Attachment#getAttachmentHandle()
	 */
	@Override
	public int getAttachmentHandle() {
		//android.util.Log.d(TAG,"getAttachmentHandle()");
		return this.handle;
	}

	/* (non-Javadoc)
	 * @see fr.kesk.libgl.buffer.FrameBufferObject.Attachment#getAttachmentTarget()
	 */
	@Override
	public int getAttachmentTarget() {
		//android.util.Log.d(TAG,"getAttachmentTarget()");
		return GLES20.GL_RENDERBUFFER;
	}

	/* (non-Javadoc)
	 * @see fr.kesk.libgl.buffer.FrameBufferObject.Attachment#getAttachmentLevel()
	 */
	@Override
	public int getAttachmentLevel() {
		//android.util.Log.d(TAG,"getAttachmentLevel()");
		return 0;
	}	
}
