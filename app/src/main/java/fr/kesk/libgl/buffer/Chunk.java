package fr.kesk.libgl.buffer;

/**
 * 
 * Represents a buffer chunk. This class must be used
 * to exchange data between buffer, application and OpenGL
 * 
 * @author Thomas MILLET
 *
 * @param <T> Should be of type byte[], short[], int[], float[]
 */
public class Chunk<T>{
	
	/**
	 * The data contained in this chunk (set by Application)
	 */
	public final T data;
	
	/**
	 * The number of components per data (set by Application)
	 */
	public final int components;
	
	/**
	 * The size of a chunk element (set by GlBuffer)
	 */
	public final int datasize;
			
	/**
	 * The type of data in GL constant (set by GlBuffer)
	 */
	public final int datatype;
	
	/**
	 * The overall size of the chunk (set by GlBuffer)
	 */
	public final int size;
	
	/**
	 * The start position in buffer (set by GlBuffer)
	 */
	public int position;
			
	/**
	 * Default constructor
	 * 
	 * @param data The data elements in byte[], short[], int[], float[]
	 * @param components The number of components per data entry (1, 2, 4 or 4)
	 */
	public Chunk(final T data, final int components){
		this.data = data;
		this.components = components;
		
		//Byte data
		if(data instanceof byte[]){
			this.datatype = GlBuffer.TYPE_BYTE;
			this.datasize = GlBuffer.SIZEOF_JAVA_BYTE;
			this.size = this.datasize * ((byte[])this.data).length; 
		}
		//Short data
		else if(data instanceof short[]){
			this.datatype = GlBuffer.TYPE_SHORT;
			this.datasize = GlBuffer.SIZEOF_JAVA_SHORT;
			this.size = this.datasize * ((short[])this.data).length;
		}
		//Int data
		else if(data instanceof int[]){
			this.datatype = GlBuffer.TYPE_INT;
			this.datasize = GlBuffer.SIZEOF_JAVA_INT;
			this.size = this.datasize * ((int[])this.data).length;
		}
		//Foat data
		else {
			this.datatype = GlBuffer.TYPE_FLOAT;
			this.datasize = GlBuffer.SIZEOF_JAVA_FLOAT;
			this.size = this.datasize * ((float[])this.data).length;
		}
	}
}