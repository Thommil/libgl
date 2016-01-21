package fr.kesk.libgl.texture;

import java.nio.ByteBuffer;

import fr.kesk.libgl.tools.ByteBufferPool;
import fr.kesk.libgl.tools.ColorUtils;

import android.opengl.ETC1;
import android.opengl.ETC1Util.ETC1Texture;
import android.opengl.GLES20;
import android.opengl.GLException;

/**
 * GlTexture Decorator to build and encapsulate an ETC1 compressed texture on the fly
 * 
 * @author Thomas MILLET
 *
 */
public class ETC1GlTexture extends GlTexture{

	/**
	 * TAG log
	 */
	@SuppressWarnings("unused")
	private final static String TAG = ETC1GlTexture.class.getName();
	
	/**
	 * GlTextureSet.TextureSource decorated
	 */
	private final GlTexture source;
		
	/**
	 * Default decorator constructor using the most appropriate format
	 * 
	 * @param source The decorated TextureSource
	 */
	public ETC1GlTexture(final GlTexture source){
		super();
		//android.util.Log.d(TAG,"NEW");
		this.source = source;
	}
	
	/* (non-Javadoc)
	 * @see fr.kesk.libgl.assets.GlTextureSet.TextureSource#getBytes()
	 */
	@Override
	public ByteBuffer getBytes() {
		//android.util.Log.d(TAG,"getBytes()");
		final ByteBuffer buffer;
		final int texelSize;
		switch(this.source.getType()){
			//RGBA -> RGB
			case TYPE_UNSIGNED_BYTE :
				buffer = ColorUtils.RGBAtoRGB(this.source.getBytes());
				texelSize = 3; 
				break;
			//RGBA -> RGB
			case TYPE_UNSIGNED_SHORT_5_6_5 :
				buffer = this.source.getBytes();
				texelSize = 2;
				break;
			default :
				throw new GLException(GLES20.GL_INVALID_ENUM,"Unsupported source encoding type for ETC1 (use TYPE_UNSIGNED_BYTE or TYPE_UNSIGNED_SHORT_5_6_5)");
		}
		
		final int encodedImageSize = ETC1.getEncodedDataSize(this.source.getWidth(), this.source.getHeight());
		final ByteBuffer compressedImage = ByteBufferPool.getInstance().getDirectByteBuffer(encodedImageSize); 
		
		ETC1.encodeImage(buffer, this.source.getWidth(), this.source.getHeight(), texelSize, this.source.getWidth() * texelSize, compressedImage);
		
		final ETC1Texture texture = new ETC1Texture(this.source.getWidth(), this.source.getHeight(), compressedImage);
		
		ByteBufferPool.getInstance().returnDirectBuffer(compressedImage);
		ByteBufferPool.getInstance().returnDirectBuffer(buffer);
		
		return texture.getData();
	}

	/* (non-Javadoc)
	 * @see fr.kesk.libgl.assets.GlTextureSet.TextureSource#getId()
	 */
	@Override
	public int getId() {
		//android.util.Log.d(TAG,"getId()");
		return this.source.getId();
	}

	/* (non-Javadoc)
	 * @see fr.kesk.libgl.assets.GlTextureSet.TextureSource#getHeight()
	 */
	@Override
	public int getHeight() {
		//android.util.Log.d(TAG,"getHeight()");
		return this.source.getHeight();
	}

	/* (non-Javadoc)
	 * @see fr.kesk.libgl.assets.GlTextureSet.TextureSource#getWidth()
	 */
	@Override
	public int getWidth() {
		//android.util.Log.d(TAG,"getWidth()");
		return this.source.getWidth();
	}

	/* (non-Javadoc)
	 * @see fr.kesk.libgl.assets.GlTextureSet.TextureSource#getTarget()
	 */
	@Override
	public int getTarget() {
		//android.util.Log.d(TAG,"getTarget()");
		return this.source.getTarget();
	}

	/* (non-Javadoc)
	 * @see fr.kesk.libgl.assets.GlTextureSet.TextureSource#getFormat()
	 */
	@Override
	public int getFormat() {
		//android.util.Log.d(TAG,"getFormat()");
		return this.source.getFormat();
	}

	/* (non-Javadoc)
	 * @see fr.kesk.libgl.assets.GlTextureSet.TextureSource#getType()
	 */
	@Override
	public int getType() {
		//android.util.Log.d(TAG,"getType()");
		return this.source.getType();
	}

	/* (non-Javadoc)
	 * @see fr.kesk.libgl.assets.GlTextureSet.TextureSource#getCompressionFormat()
	 */
	@Override
	public int getCompressionFormat() {
		//android.util.Log.d(TAG,"getCompressionFormat()");
		return COMP_ETC1;
	}

	/* (non-Javadoc)
	 * @see fr.kesk.libgl.assets.GlTextureSet.TextureSource#getWrapMode(int)
	 */
	@Override
	public int getWrapMode(int axeId) {
		//android.util.Log.d(TAG,"getWrapMode()");
		return this.source.getWrapMode(axeId);
	}

	/* (non-Javadoc)
	 * @see fr.kesk.libgl.assets.GlTextureSet.TextureSource#getMagnificationFilter()
	 */
	@Override
	public int getMagnificationFilter() {
		//android.util.Log.d(TAG,"getMagnificationFilter()");
		return this.source.getMagnificationFilter();
	}

	/* (non-Javadoc)
	 * @see fr.kesk.libgl.assets.GlTextureSet.TextureSource#getMinificationFilter()
	 */
	@Override
	public int getMinificationFilter() {
		//android.util.Log.d(TAG,"getMinificationFilter()");
		switch(this.source.getMinificationFilter()){
			case MIN_FILTER_LOW:
			case MIN_FILTER_HIGH:
				return this.source.getMinificationFilter();
			default :
				return MIN_FILTER_LOW;
				
		}
	}

	/* (non-Javadoc)
	 * @see fr.kesk.libgl.texture.TextureSource#getSize()
	 */
	@Override
	public int getSize() {
		//android.util.Log.d(TAG,"getSize()");
		return ETC1.getEncodedDataSize(this.source.getWidth(), this.source.getHeight());
	}	
	
	
}