package fr.kesk.libgl.buffer;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import fr.kesk.libgl.GlContext;
import fr.kesk.libgl.tools.ByteBufferPool;
import fr.kesk.libgl.tools.GLES20Utils;

import android.opengl.GLES20;

/**
 * Buffer abstraction class :
 * 		<ul>
 * 			<li>build underlying buffer based on array of Chunks</li>
 * 			<li>fill the buffer using interleaves and stride</li>
 *  		<li>allows to upload buffer content to VBOs</li>
 *  		<li>underlined pools for preformances</li>
 *  		<li>not thread safe !</li>
 *  	</ul>
 *
 * <b>
 * <br/>
 * !!! Important !!!<br/> 
 * To use VBO on Android 2.2, you must include local libs/armeabi to your project
 * </b>
 * <br/>
 * <br/>
 * Typical calls :
 * <pre>{@code
 *  //Local No Index
 *  buffer.toVertexAttribute(handle, chunkIndex, false);
 *  GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, buffer.count);
 * 
 *  //VBO No Index
 *  buffer.toVertexAttribute(handle, chunkIndex, true);
 *  GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, buffer.count);
 *  
 *  //Local Indexed
 *  buffer.toVertexAttribute(handle, chunkIndex, false);
 *  indexBuffer.position(indexBuffer.chunks[chunkIndex]);
 *  GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexBuffer.count, indexBuffer.datatype, indexBuffer.data);
 *  
 *  //VBO Indexed
 *  buffer.toVertexAttribute(handle, chunkIndex, true);
 *  indexBuffer.bind()
 *  GLES20Ext.glDrawElements(GLES20.GL_TRIANGLES, indexBuffer.count, indexBuffer.datatype, indexBuffer.chunks[chunkIndex].position*indexBuffer.datasize);
 *  indexBuffer.unbind()
 *  
 * }</pre>
 * 
 * 
 * 
 * 
 * 
 * @author Thomas MILLET
 *
 * @param <E> Should be of type byte[], short[], int[], float[]
 *
 */
public class GlBuffer<E>{
	
	/**
	 * TAG log
	 */
	@SuppressWarnings("unused")
	private final static String TAG = GlBuffer.class.getName();
	
	/**
	 * Alias for BYTE in OpenGL for inner data type
	 */
	public static final int TYPE_BYTE = GLES20.GL_UNSIGNED_BYTE;
	
	/**
	 * Alias for SHORT in OpenGL for inner data type
	 */
	public static final int TYPE_SHORT = GLES20.GL_UNSIGNED_SHORT;
	
	/**
	 * Alias for INT in OpenGL for inner data type
	 */
	public static final int TYPE_INT = GLES20.GL_UNSIGNED_INT;
	
	/**
	 * Alias for FLOAT in OpenGL for inner data type
	 */
	public static final int TYPE_FLOAT = GLES20.GL_FLOAT;
	
	/**
	 * Define the inner size of a byte
	 */
	public static final int SIZEOF_JAVA_BYTE = Byte.SIZE/8;
	
	/**
	 * Define the inner size of a short
	 */
	public static final int SIZEOF_JAVA_SHORT = Short.SIZE/8;
	
	/**
	 * Define the inner size of a integer
	 */
	public static final int SIZEOF_JAVA_INT = Integer.SIZE/8;
	
	/**
	 * Define the inner size of a float
	 */
	public static final int SIZEOF_JAVA_FLOAT = Float.SIZE/8;
	
	/**
	 * Handle to use to unbind current buffer
	 */
	public static final int UNBIND_HANDLE = GLES20.GL_ZERO;
	
	/**
	 * Buffer type for client drawing
	 */
	public static final int TYPE_CLIENT_DRAW = GLES20.GL_ZERO;
	
	/**
	 * Buffer usage for server static drawing (no data update)
	 */
	public static final int USAGE_STATIC_DRAW = GLES20.GL_STATIC_DRAW;
	
	/**
	 * Buffer usage for server dynamic drawing (many data updates)
	 */
	public static final int USAGE_DYNAMIC_DRAW = GLES20.GL_DYNAMIC_DRAW;
	
	/**
	 * Buffer usage for server stream drawing (few data updates)
	 */
	public static final int USAGE_STREAM_DRAW = GLES20.GL_STREAM_DRAW;
	
	/**
	 * Buffer target for vertices data
	 */
	public static final int TARGET_ARRAY_BUFFER = GLES20.GL_ARRAY_BUFFER;
	
	/**
	 * Buffer target for index data
	 */
	public static final int TARGET_ELEMENT_ARRAY_BUFFER = GLES20.GL_ELEMENT_ARRAY_BUFFER;
			
	/**
	 * Contains all the Buffer chunks
	 */
	public final Chunk<E>[] chunks;
		
	/**
	 * The type of data in buffer (should be Chunk.TYPE_BYTE, Chunk.TYPE_SHORT, Chunk.TYPE_INT, Chunk.TYPE_FLOAT)
	 */
	public int datatype = TYPE_BYTE;
	
	/**
	 * The size of data
	 */
	public int datasize = SIZEOF_JAVA_BYTE;
	
	/**
	 * The number of elements in this buffer
	 */
	public int count = 0;
	
	/**
	 * The buffer size
	 */
	public int size = 0;
	
	/**
	 * The buffer stride
	 */
	public int stride = 0;
	
	/**
	 * The current GL usage (USAGE_STATIC_DRAW, USAGE_DYNAMIC_DRAW or USAGE_STREAM_DRAW)
	 */
	public int usage = USAGE_STATIC_DRAW;
	
	/**
	 * The current GL target (TARGET_ARRAY_BUFFER or TARGET_ELEMENT_ARRAY_BUFFER)
	 */
	public int target = TARGET_ARRAY_BUFFER;
	
	/**
	 * The bound buffer
	 */
	public Buffer data;
	
	/**
	 * Handle on server buffer if TYPE_SERVER_STATIC_DRAW or TYPE_SERVER_DYNAMIC_DRAW
	 */
	public int handle = GLES20.GL_FALSE;
	
	/**
	 * Cache the list of chunk ids
	 */
	private int[] indexCache;
	
	/**
	 * Constructor
	 */
	public GlBuffer(final Chunk<E>[] chunks){
		//android.util.Log.d(TAG,"NEW");
		this.chunks = chunks;
		
		//Init
		if(this.chunks != null && this.chunks.length > 0) {
			//Count
			this.count = this.chunks[0].size / this.chunks[0].datasize / this.chunks[0].components;
			//Datatype
			this.datatype = this.chunks[0].datatype;
			//Datasize
			this.datasize = this.chunks[0].datasize;
		} else {
			throw new IllegalArgumentException("failed to build buffer : no data provided");
		}
		int currentPosition = 0;
		this.indexCache = new int[this.chunks.length];
		int index=0;
		//First pass -> size & position
		for(Chunk<E> chunk : this.chunks){
			//BufferSize
			this.size += chunk.size;
			//Position
			chunk.position = currentPosition / chunk.datasize;
			currentPosition += chunk.datasize * chunk.components;
			//Index cache
			this.indexCache[index] = index++;
		}
		//Stride
		this.stride = currentPosition;
	
		this.update(false);
	}
	
	/**
	 * Bind current buffer to active buffer if GPU 
	 */
	public void bind(){
		//android.util.Log.d(TAG,"bind()");
		GLES20.glBindBuffer(this.target, this.handle);
	}
	
	/**
	 * Unbind current buffer from active buffer if GPU 
	 */
	public void unbind(){
		//android.util.Log.d(TAG,"unbind()");
		GLES20.glBindBuffer(this.target, UNBIND_HANDLE);
	}
	
	/**
	 * Write the buffer content to the given Vertex attribute using VBO if specified and possible
	 * 
	 * @param attrHandle The vertex shader attribute handle
	 * @param chunkIndex The chunk index to use
	 * @param useVBO Indicate if VBO must be used if possible (enabled using createVBO())
	 */
	public void toVertexAttribute(final int attrHandle, final int chunkIndex, final boolean useVBO){
		//android.util.Log.d(TAG,"toVertexAttribute("+attrHandle+", "+chunkIndex+", "+useVBO+")");
		
		//VBO
		if(useVBO && this.handle != UNBIND_HANDLE){
			this.bind();
			GLES20Utils.glVertexAttribPointer(attrHandle, this.chunks[chunkIndex].components, this.datatype, false, this.stride, this.chunks[chunkIndex].position*this.datasize);
		}
		//CPU -> GPU
		else{
			this.position(this.chunks[chunkIndex]);
			GLES20.glVertexAttribPointer(attrHandle, this.chunks[chunkIndex].components, this.datatype, false, this.stride, this.data);
		}
			
	}
	
	/**
	 * Update the whole buffer
	 * 
	 * @param updateVBO If true, buffer will be updated on VBO
	 */
	public void update(final boolean updateVBO){
		this.update(this.indexCache, updateVBO);
	}
	
	/**
	 * Update the whole buffer without commit
	 * 
	 */
	public void update(){
		this.update(this.indexCache, false);
	}
	
	/**
	 * Update buffer with the indicated chunk index
	 * 
	 * Data can be commited into VBO if queried.
	 * 
	 * @param chunksToUpdate The index/id of the chunk to update
	 * @param updateVBO If true, buffer will be updated on VBO
	 */
	public void update(final int chunkToUpdate, final boolean updateVBO){
		this.update(new int[]{chunkToUpdate}, updateVBO);
	}
	
	/**
	 * Update buffer with the indicated chunk index without commit
	 * 
	 * @param chunksToUpdate The index/id of the chunk to update
	 * @param commit If true, buffer will be updated on VBO
	 */
	public void update(final int chunkToUpdate){
		this.update(new int[]{chunkToUpdate}, false);
	}
	
	/**
	 * Update buffer with the list of chunks indicated.
	 * 
	 * Data can be commited into VBO if queried.
	 * 
	 * @param chunksToUpdate The list of chunks index to update
	 * @param updateVBO Update VBO too is set to true
	 */
	public void update(int chunksToUpdate[], boolean updateVBO){
		//android.util.Log.d(TAG,"update("+chunksToUpdate+", "+commit+")");
		
		switch(this.datatype){
			case TYPE_BYTE :
				if(this.data == null){
					this.data = ByteBufferPool.getInstance().getDirectByteBuffer(this.size);	
				}
				for(int id : chunksToUpdate){
					final Chunk<E> chunk = this.chunks[id];
					for(int elementIndex=0, compIndex=0; elementIndex < this.count ; elementIndex++, compIndex+=chunk.components){
						this.data.position((chunk.position+ ((elementIndex*this.stride))/chunk.datasize));
						((ByteBuffer)this.data).put(((byte[])chunk.data),compIndex,chunk.components);
					}
				}
				break;
			case TYPE_SHORT :
				if(this.data == null){
					this.data = ByteBufferPool.getInstance().getDirectShortBuffer(this.size / GlBuffer.SIZEOF_JAVA_SHORT);	
				}
				for(int id : chunksToUpdate){
					final Chunk<E> chunk = this.chunks[id];
					for(int elementIndex=0, compIndex=0; elementIndex < this.count ; elementIndex++, compIndex+=chunk.components){
						this.data.position((chunk.position+ ((elementIndex*this.stride))/chunk.datasize));
						((ShortBuffer)this.data).put(((short[])chunk.data),compIndex,chunk.components);
					}
				}	
				break;
			case TYPE_INT :
				if(this.data == null){
					this.data = ByteBufferPool.getInstance().getDirectIntBuffer(this.size / GlBuffer.SIZEOF_JAVA_INT);	
				}
				for(int id : chunksToUpdate){
					final Chunk<E> chunk = this.chunks[id];
					for(int elementIndex=0, compIndex=0; elementIndex < this.count ; elementIndex++, compIndex+=chunk.components){
						this.data.position((chunk.position+ ((elementIndex*this.stride))/chunk.datasize));
						((IntBuffer)this.data).put(((int[])chunk.data),compIndex,chunk.components);
					}
				}	
				break;
			default :
				if(this.data == null){
					this.data = ByteBufferPool.getInstance().getDirectFloatBuffer(this.size / GlBuffer.SIZEOF_JAVA_FLOAT);	
				}
				for(int id : chunksToUpdate){
					final Chunk<E> chunk = this.chunks[id];
					for(int elementIndex=0, compIndex=0; elementIndex < this.count ; elementIndex++, compIndex+=chunk.components){
						this.data.position((chunk.position+ ((elementIndex*this.stride))/chunk.datasize));
						((FloatBuffer)this.data).put(((float[])chunk.data),compIndex,chunk.components);
					}
				}
		}
		
		//Update server if needed
		if(updateVBO && this.handle != UNBIND_HANDLE){
			GLES20.glBindBuffer(this.target, this.handle);
			this.data.position(0);
			GLES20.glBufferSubData(this.target, 0, this.size, this.data);
		}	
	}
	
	/**
	 * Update buffer with the list of chunks indicated without commit
	 * 
	 * @param chunksToUpdate The list of chunks index to update
	 */
	public void update(int chunksToUpdate[]){
		this.update(chunksToUpdate, false);
	}
	
	/**
	 * Create a VBO on GPU and bind buffer data to it
	 * 
	 * @param usage Should be USAGE_STATIC_DRAW, USAGE_DYNAMIC_DRAW or USAGE_STREAM_DRAW
	 * @param target Should be GLES20.GL_ARRAY_BUFFER or GLES20.GL_ELEMENT_ARRAY_BUFFER
	 * @param freeLocal If true, the local buffer is released at binding
	 * 
	 * @return The buffer handle on server (available in handle attribute too)
	 */
	public int createVBO(final int usage, final int target, final boolean freeLocal){
		//android.util.Log.d(TAG,"createVBO("+usage+","+target+","+freeLocal+")");
		if(this.handle == GLES20.GL_FALSE){
			final int[] handles = new int[1];
			
			//Create buffer on server
			GLES20.glGenBuffers(1, handles, 0);
			this.handle = handles[0];
			this.target = target;
			
			//Bind it
			GLES20.glBindBuffer(target, this.handle);
			//Push data into it
			this.data.position(0);
			GLES20.glBufferData(target, this.size, this.data, usage);
			//Unbind it
			GLES20.glBindBuffer(target, UNBIND_HANDLE);
			
			//Check error on bind only
			GlContext.glCheckError();
			
			//Free local buffer is queried
			if(freeLocal){
				switch(this.datatype){
					case TYPE_BYTE :
						ByteBufferPool.getInstance().returnDirectBuffer((ByteBuffer)this.data);
						break;
					case TYPE_SHORT :
						ByteBufferPool.getInstance().returnDirectBuffer((ShortBuffer)this.data);
						break;
					case TYPE_INT :
						ByteBufferPool.getInstance().returnDirectBuffer((IntBuffer)this.data);
						break;
					default :
						ByteBufferPool.getInstance().returnDirectBuffer((FloatBuffer)this.data);
				}
				this.data = null;
			}	
		}
		return this.handle;
	}
	
	/**
	 * Remove previous VBO binding
	 */
	public void deleteVBO(){
		//android.util.Log.d(TAG,"deleteVBO()");
		if(this.handle != UNBIND_HANDLE){
			final int[] handles = new int[]{this.handle};
			this.handle = UNBIND_HANDLE;
			GLES20.glDeleteBuffers(1, handles, 0);
		}	
	}
	
	/**
	 * Free local and server buffers
	 */
	public void free(){
		//android.util.Log.d(TAG,"free()");
		this.deleteVBO();
		if(this.data != null){
			switch(this.datatype){
				case TYPE_BYTE :
					ByteBufferPool.getInstance().returnDirectBuffer((ByteBuffer)this.data);
					break;
				case TYPE_SHORT :
					ByteBufferPool.getInstance().returnDirectBuffer((ShortBuffer)this.data);
					break;
				case TYPE_INT :
					ByteBufferPool.getInstance().returnDirectBuffer((IntBuffer)this.data);
					break;
				default :
					ByteBufferPool.getInstance().returnDirectBuffer((FloatBuffer)this.data);
			}
			this.data = null;	
		}	
		this.size = 0;
	}
	
	/**
	 * @return
	 * @see java.nio.Buffer#flip()
	 */
	public final Buffer flip() {
		return this.data.flip();
	}

	/**
	 * @return
	 * @see java.nio.Buffer#position()
	 */
	public final int position() {
		return this.data.position();
	}

	/**
	 * @param newPosition
	 * @return
	 * @see java.nio.Buffer#position(int)
	 */
	public final Buffer position(final int newPosition) {
		return this.data.position(newPosition);
	}

	/**
	 * @param newChunk
	 * @return
	 * @see java.nio.Buffer#position(int)
	 */
	public final Buffer position(final Chunk<E> newChunk) {
		return this.data.position(newChunk.position);
	}
	
	/**
	 * @return
	 * @see java.nio.Buffer#hasRemaining()
	 */
	public final boolean hasRemaining() {
		return this.data.hasRemaining();
	}

	/**
	 * @return
	 * @see java.nio.Buffer#rewind()
	 */
	public final Buffer rewind() {
		return this.data.rewind();
	}
	
	

	/**
	 * @return
	 * @see java.nio.Buffer#capacity()
	 */
	public final int capacity() {
		return this.data.capacity();
	}

	/**
	 * @return
	 * @see java.nio.Buffer#clear()
	 */
	public final Buffer clear() {
		return this.data.clear();
	}

	/**
	 * @return
	 * @see java.nio.Buffer#isReadOnly()
	 */
	public boolean isReadOnly() {
		return this.data.isReadOnly();
	}

	/**
	 * @return
	 * @see java.nio.Buffer#limit()
	 */
	public final int limit() {
		return this.data.limit();
	}

	/**
	 * @param newLimit
	 * @return
	 * @see java.nio.Buffer#limit(int)
	 */
	public final Buffer limit(int newLimit) {
		return this.data.limit(newLimit);
	}

	/**
	 * @return
	 * @see java.nio.Buffer#mark()
	 */
	public final Buffer mark() {
		return this.data.mark();
	}

	/**
	 * @return
	 * @see java.nio.Buffer#remaining()
	 */
	public final int remaining() {
		return this.data.remaining();
	}

	/**
	 * @return
	 * @see java.nio.Buffer#reset()
	 */
	public final Buffer reset() {
		return this.data.reset();
	}	
}
