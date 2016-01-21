package fr.kesk.libgl.texture;

import fr.kesk.libgl.GlContext;

import android.opengl.GLES20;
import android.opengl.GLException;

import android.util.SparseArray;

/**
 * Container class to upload and handle a list of textures on GPU
 * 
 * @author Thomas MILLET
 *
 */
public class GlTextureSet {

	/**
	 * TAG log
	 */
	@SuppressWarnings("unused")
	private final static String TAG = GlTextureSet.class.getName();
	
	/**
	 * Stores the list of bitmap providers
	 */
	private final SparseArray<GlTexture> providers;
	
	/**
	 * The current texture listener
	 */
	private OnTextureEventListener onTextureEventListener;
	
	/**
	 * Store current number of stored textures 
	 */
	public int size = 0;
		
	/**
	 * Array buffer used to add a single texture
	 */
	public final GlTexture[]tmpTextureArray = new GlTexture[1];
	
	/**
	 * Default constructor
	 */
	public GlTextureSet(){
		//android.util.Log.d(TAG,"NEW");
		this.providers = new SparseArray<GlTexture>();
		this.size = 0;
	}
	
	/**
	 * Add textures to the current GlTextures
	 * 
	 * @param providers The list of providers to register
	 * @return The list of handles for input
	 */
	public int[] put(final GlTexture[] providers){
		//android.util.Log.d(TAG,"put()");
		final int[] textureHandles = new int[providers.length];
		
		synchronized(this.providers){
			GLES20.glGenTextures(textureHandles.length, textureHandles, 0);
			
			for(int index=0; index < providers.length; index++){
				if(textureHandles[index] != GlTexture.UNBIND_HANDLE){
					// Bind to the texture in OpenGL
					GLES20.glBindTexture(providers[index].getTarget(), textureHandles[index]);
					
					//Apply parameters (do it first to allow hard optim)
					GLES20.glTexParameteri(providers[index].getTarget(), GLES20.GL_TEXTURE_MIN_FILTER, providers[index].getMinificationFilter());
					GLES20.glTexParameteri(providers[index].getTarget(), GLES20.GL_TEXTURE_MAG_FILTER, providers[index].getMagnificationFilter());
					GLES20.glTexParameteri(providers[index].getTarget(), GLES20.GL_TEXTURE_WRAP_S, providers[index].getWrapMode(GLES20.GL_TEXTURE_WRAP_S));
					GLES20.glTexParameteri(providers[index].getTarget(), GLES20.GL_TEXTURE_WRAP_T, providers[index].getWrapMode(GLES20.GL_TEXTURE_WRAP_T));
					
					//Additional parameters
					providers[index].onUpload();
					
					// Load the bitmap into the bound texture.
					if(providers[index].getCompressionFormat() != GlTexture.COMP_FALSE){
						if(GlTexture.isCompressionFormatSupported(providers[index].getCompressionFormat())){
							GLES20.glCompressedTexImage2D(providers[index].getTarget(), 
															0, 
															providers[index].getCompressionFormat(),
															providers[index].getWidth(), 
															providers[index].getHeight(), 
															0, 
															providers[index].getSize(), 
															providers[index].getBytes());
						}
						else{
							//Error when compression is not supported
							if(this.onTextureEventListener != null){
								this.onTextureEventListener.onTextureError(index,new GLException(GLES20.GL_INVALID_OPERATION));
							}
							continue;
						}
					}
					else{
						//No compression
						GLES20.glTexImage2D(providers[index].getTarget(),
											0, 
											providers[index].getFormat(), 
											providers[index].getWidth(),
											providers[index].getHeight(),
											0, 
											providers[index].getFormat(),
											providers[index].getType(),
											providers[index].getBytes());
					}
					
					
					//Generate mipmap
					if(providers[index].getMinificationFilter() != GlTexture.MIN_FILTER_HIGH 
							&& providers[index].getMinificationFilter() != GlTexture.MIN_FILTER_LOW){
						GLES20.glGenerateMipmap(providers[index].getTarget());
					}
					
					//Check for errors
					try{
						GlContext.glCheckError();
					}catch(GLException gle){
						if(this.onTextureEventListener != null){	
							this.onTextureEventListener.onTextureError(index,gle);
						}
						continue;
					}
					
					//Indexing
					providers[index].handle = textureHandles[index];
					this.providers.append(providers[index].getId(), providers[index]);
					this.size++;
					
					//Callback on listener
					if(this.onTextureEventListener != null){
						this.onTextureEventListener.onTextureBound(index,textureHandles[index]);
					}
				}
			}
		}
		
		return textureHandles;
	}
	
	/**
	 * Add a texture to the current GlTextures
	 * 
	 * @param provider The provider to register
	 * @return The handle on GL server
	 */ 
	public int put(final GlTexture provider){
		synchronized (this.tmpTextureArray) {
			this.tmpTextureArray[0] = provider;
			return this.put(this.tmpTextureArray)[0];
		}
	}
	
	/**
	 * @return the onTextureEventListener
	 */
	public OnTextureEventListener getOnTextureEventListener() {
		return onTextureEventListener;
	}

	/**
	 * @param onTextureEventListener the onTextureEventListener to set
	 */
	public void setOnTextureEventListener(OnTextureEventListener onTextureEventListener) {
		this.onTextureEventListener = onTextureEventListener;
	}

	/**
	 * Get the texture specified from its id
	 * 
	 * @param id The texture id 
	 * @return The texture 
	 */
	public GlTexture getTexture(final int id){
		//android.util.Log.d(TAG,"getTexture("+id+")");
		return this.providers.get(id);
	}
	
	/**
	 * Get the texture specified from its provider
	 * 
	 * @param provider The bitmap provider of this texture
	 * @return The texture
	 */
	public GlTexture getTexture(final GlTexture provider){
		return this.getTexture(this.providers.indexOfValue(provider));
	}
	
	/**
	 * Get the texture specified from its name
	 * 
	 * @param name The name of the provider to get
	 * @return The texture
	 */
	public GlTexture getTextureByIndex(final int index){
		//android.util.Log.d(TAG,"getTextureByIndex("+index+")");
		return this.providers.valueAt(index);
	}
	
	/**
	 * Remove a BitmapProvider from current GlTextures based on its id
	 * 
	 * @param id The id of the provider to remove
	 */
	public void remove(final int id){
		//android.util.Log.d(TAG,"remove("+name+")");
		synchronized(this.providers){
			this.getTexture(id).free();
			this.providers.delete(id);
			this.size--;
		}	
	}
		
	/**
	 * Remove all textures from memory
	 */
	public void free(){
		//android.util.Log.d(TAG,"free()");
		synchronized(this.providers){
			for(int index=0; index <this.size; index++){
				this.remove(index);
			}
		}	
	}
	
	/**
	 * @return the size
	 */
	public int size() {
		return size;
	}	
	
	/**
	 * Listener on texture events
	 * 
	 * @author Thomas MILLET
	 *
	 */
	public static interface OnTextureEventListener {
		
		/**
		 * Callback listener when a texture failed to bind 
		 * 
		 * @param textureIndex The index of the texture in the array input
		 * @param glException The generated error
		 */
		public void onTextureError(final int textureIndex, final GLException glException);
		
		/**
		 * Callback listener when a new texture as been bound
		 * 
		 * @param textureIndex The index of the texture in the array input
		 */
		public void onTextureBound(final int textureIndex, final int textureHandle);
	}
}
