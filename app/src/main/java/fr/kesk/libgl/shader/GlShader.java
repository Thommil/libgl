package fr.kesk.libgl.shader;

import fr.kesk.libgl.GlAssets.Node;

/**
 * Generic Shader API for drawing operations
 * 
 * @author Thomas MILLET
 *
 */
public interface GlShader {
			
	/**
	 * Compile the shader (implementation must authorized multiple calls)
	 */
	public void compile();
	
	/**
	 * Set the shader quality based on shader available quality modes
	 * 
	 * @param quality The quality indice depending on implementation
	 */
	public void setQuality(final int quality);
	
	/**
	 * Called on draw state to render a node and its children using current shader
	 * 
	 * @param node The nodes instance to render
	 */
	public void render(final Node nodeInstance);
		
	/**
	 * Free resources allocated by this shader
	 */
	public void free();
}
