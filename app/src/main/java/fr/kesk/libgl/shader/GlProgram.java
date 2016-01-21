package fr.kesk.libgl.shader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.opengl.GLES20;
import android.opengl.GLException;

/**
 * Helper class to get shaders source code and link program
 * <br/><br/>
 *
 *  This approach allows automatic linking based on parsing. 
 *  
 * @author Thomas MILLET
 *
 */
public class GlProgram {

	/**
	 * TAG log
	 */
	@SuppressWarnings("unused")
	private final static String TAG = GlProgram.class.getName();
	
	/**
	 * Handle to use to unbind current program
	 */
	public static final int UNBIND_HANDLE = GLES20.GL_ZERO;
	
	/**
	 * Attribute pattern for parsing
	 */
	private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("attribute\\s*[^\\s]*\\s*([^\\s;]*)\\s*;",Pattern.MULTILINE|Pattern.DOTALL);
	
	/**
	 * Uniform pattern for parsing
	 */
	private static final Pattern UNIFORM_PATTERN = Pattern.compile("uniform\\s*[^\\s]*\\s*([^\\s;]*)\\s*;",Pattern.MULTILINE|Pattern.DOTALL);
	
	/**
	 * Prefix for GSGL uniforms (see header for convention)
	 */
	public static final String UNIFORM_PREFIX = "u_";
			
	/**
	 * Indicates if current program is enabled on pipeline
	 */
	private boolean isEnabled;
	
	/**
	 * Handle on current program
	 */
	public final int programHandle;
	
	/**
	 * Handle of the vertex shader
	 */
	public final int vertexShaderHandle;
	
	/**
	 *  Handle of the fragment shader
	 */
	public final int fragmentShaderHandle;
	
	/**
	 *  Handles on GSGL attributes
	 */
	private final Map<String,Integer> attributeHandles;
	
	/**
	 *  Store the attributes handles for direct access
	 */
	private int[] attributeHandlesArray;
	
	/**
	 *  Handles on GSGL uniforms
	 */
	private final Map<String,Integer> uniformHandles;
	
	/**
	 * Constructor, creates and link program based on specified shaders
	 * 
	 * @param vertexShaderLocation The vertex shader inputstream
	 * @param fragmentShaderLocation The fragment shader inputstream
	 * @param varList The list of variable names to bind (null for no binding)
	 * @throws GLException
	 */
	public GlProgram(final InputStream vertexShaderInputStream, final InputStream fragmentShaderInputStream){
		//android.util.Log.d(TAG,"NEW");
		this.attributeHandles = new HashMap<String, Integer>();
		this.uniformHandles = new HashMap<String, Integer>();
		this.vertexShaderHandle = this.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderInputStream);
		this.fragmentShaderHandle = this.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderInputStream);
		this.programHandle = this.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle);
	}
	
	/**
	 * Enables all attributes of vertex shader
	 */
	public void enableAttributes(){
		for(int handle : this.attributeHandlesArray){
			this.enableAttribute(handle);
		}
	}
		
	/**
	 * Enables shader attribute by its name
	 * 
	 * @param attributeName The name of the attribute to enable
	 */
	public void enableAttribute(final String attributeName){
		//android.util.Log.d(TAG,"enableAttribute("+attributeName+")");
		GLES20.glEnableVertexAttribArray(this.getAttributeHandle(attributeName));
	}
	
	/**
	 * Enables shader attribute by its handle
	 * 
	 * @param attrList The handle of attribute to enable
	 */
	public void enableAttribute(final int attributeId){
		//android.util.Log.d(TAG,"enableAttribute("+attributeId+")");
		GLES20.glEnableVertexAttribArray(attributeId);
	}
	
	/**
	 * Disables all attributes of vertex shader
	 */
	public void disableAttributes(){
		for(int handle : this.attributeHandlesArray){
			this.disableAttribute(handle);
		}
	}
	
	/**
	 * Disables shader attribute by its name
	 * 
	 * @param attributeName The name of attribute to disable
	 */
	public void disableAttribute(final String attributeName){
		//android.util.Log.d(TAG,"enableAttribute("+attributeName+")");
		GLES20.glDisableVertexAttribArray(this.getAttributeHandle(attributeName));
	}
	
	/**
	 * Disables shader attribute by its handle
	 * 
	 * @param attributeId The handle of attribute to disable
	 */
	public void disableAttribute(final int attributeId){
		//android.util.Log.d(TAG,"enableAttribute("+attributeId+")");
		GLES20.glDisableVertexAttribArray(attributeId);
	}
	
	/**
	 * Enabled the program on pipeline.<br/>
	 * <br/>
	 * <br/>
	 * Caution : only calls to start/stop are taken into account for current state
	 */
	public void start(){
		//android.util.Log.d(TAG,"start()");
		if(!this.isEnabled){
			GLES20.glUseProgram(this.programHandle);
		}
	}
	
	/**
	 * Disable the program on pipeline.<br/>
	 * <br/>
	 * <br/>
	 * Caution : only calls to start/stop are taken into account for current state
	 */
	public void stop(){
		//android.util.Log.d(TAG,"stop()");
		if(this.isEnabled){
			GLES20.glUseProgram(UNBIND_HANDLE);
		}
	}
	
	/*
     * Simple loader used to get shaders from specified location
     * 
     * @param type The vertex shader type GLES20.GL_VERTEX_SHADER | GLES20.GL_FRAGMENT_SHADER
     * @param inputStream The shader code InputStream
     * @return return a compiled shader OpenGL ID
     */
	protected int loadShader(final int type, final InputStream inputStream){
		//android.util.Log.d(TAG,"loadShader("+type+", "+location+")");
		
		final int shader = GLES20.glCreateShader(type);
		
		BufferedReader bufIn = null;
	    
	    try{
	    	bufIn = new BufferedReader(new InputStreamReader(inputStream), 8192);

	    	final StringBuilder shaderCode = new StringBuilder();
	    	String line = null;
	    	while((line = bufIn.readLine()) != null){
	    		shaderCode.append(line).append("\n");
	    	}

	    	// add the source code to the shader and compile it
	    	GLES20.glShaderSource(shader, shaderCode.toString());
	    	GLES20.glCompileShader(shader);
	    	
	    	// Get the compilation status.
	        final int[] compileStatus = new int[1];
	        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
	     
	        // If the compilation failed, delete the shader.
	        if (compileStatus[0] == GLES20.GL_FALSE) {
	            GLES20.glDeleteShader(shader);
	            throw new GLException(GLES20.GL_INVALID_OPERATION, "Failed to compile "+((type == GLES20.GL_VERTEX_SHADER)? "vertex":"fragment")+" shader");
	        }
	        
	        //Parse shader to store attributes and uniform handles
	    	final Matcher attributeMatcher = ATTRIBUTE_PATTERN.matcher(shaderCode);
	    	while(attributeMatcher.find()){
	    		final String attributeName = attributeMatcher.group(1);
	    		if(!this.attributeHandles.containsKey(attributeName)){
	    			this.attributeHandles.put(attributeName, UNBIND_HANDLE);
	    		}
	    	}
	    	final Matcher uniformMatcher = UNIFORM_PATTERN.matcher(shaderCode);
	    	while(uniformMatcher.find()){
	    		final String uniformName = uniformMatcher.group(1);
	    		if(!this.uniformHandles.containsKey(uniformName)){
	    			this.uniformHandles.put(uniformName, UNBIND_HANDLE);
	    		}
	    	}
	        
	    }catch(IOException ioe){
	    	throw new GLException(GLES20.GL_INVALID_OPERATION, "Failed to compile "+((type == GLES20.GL_VERTEX_SHADER)? "vertex":"fragment")+" shader");
	    }	
		
		return shader;
	}
	
    
    /**
	 * Helper function to compile and link a program
	 * 
	 * @param vertexShaderHandle An OpenGL handle to an already-compiled vertex shader.
	 * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader.
	 * @return An OpenGL handle to the program.
	 */
	protected int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle){
		//android.util.Log.d(TAG,"createAndLinkProgram("+vertexShaderHandle+", "+fragmentShaderHandle+")");
		final int programHandle = GLES20.glCreateProgram();
		
		if (programHandle != UNBIND_HANDLE) {
			// Bind the vertex shader to the program.
			GLES20.glAttachShader(programHandle, vertexShaderHandle);			

			// Bind the fragment shader to the program.
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);
			
			// Link the two shaders together into a program.
			GLES20.glLinkProgram(programHandle);

			// Get the link status.
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

			// If the link failed, delete the program.
			if (linkStatus[0] == GLES20.GL_FALSE) {				
				final String error = GLES20.glGetProgramInfoLog(programHandle);
				GLES20.glDeleteProgram(programHandle);
				throw new GLException(GLES20.GL_INVALID_OPERATION, "Failed to link program : "+error);
			}
			
			//Update handles and fill the attributes cache
			int index=0;
			this.attributeHandlesArray = new int[this.attributeHandles.size()];
			for(String attributeName : this.attributeHandles.keySet()){
				final int attributeHandle = GLES20.glGetAttribLocation(programHandle, attributeName);
				this.attributeHandles.put(attributeName, attributeHandle);
				this.attributeHandlesArray[index++] = attributeHandle;	
			}
			for(String uniformName : this.uniformHandles.keySet()){
				final int uniformHandle = GLES20.glGetUniformLocation(programHandle, uniformName);
				this.uniformHandles.put(uniformName, uniformHandle);	
			}
		}
		
		return programHandle;
	}
	
	
	/**
	 * Free resources
	 */
	public void free(){
		//android.util.Log.d(TAG,"free()");
		if(this.programHandle != UNBIND_HANDLE) {
			GLES20.glDeleteProgram(this.programHandle);
		}
		if(this.vertexShaderHandle != UNBIND_HANDLE) {
			GLES20.glDeleteShader(this.vertexShaderHandle);
		}
		if(this.fragmentShaderHandle != UNBIND_HANDLE) {
			GLES20.glDeleteShader(this.fragmentShaderHandle);
		}
	}
	 
	/**
	 * Get the handle of a specified attribute
	 * 
	 * @param name The attribute name
	 * @return The handle ID, 0 if not found
	 */
	public int getAttributeHandle(final String name){
		//android.util.Log.d(TAG,"getAttributeHandle("+name+")");
		Integer handle = this.attributeHandles.get(name); 
		return (handle == null) ? UNBIND_HANDLE : handle;
	}
	
	/**
	 * Get the handle of a specified uniform
	 * 
	 * @param name The uniform name
	 * @return The handle ID, 0 if not found
	 */
	public int getUniformHandle(final String name){
		//android.util.Log.d(TAG,"getUniformHandle("+name+")");
		Integer handle = this.uniformHandles.get(name); 
		return (handle == null) ? UNBIND_HANDLE : handle;
	}
	
}
