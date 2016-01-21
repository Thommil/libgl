package fr.kesk.libgl;

import android.content.Context;

/**
 * Implementation classes should store and give access to
 * application contexts and threads :
 * 	<ul>
 * 		<li>OpenGL Context as GPU Server</li>
 * 		<li>Main UI Thread on CPU as GPU client</li>
 *	</ul>
 * 
 * @author Thomas MILLET
 *
 */
public interface ContextManager {

	/**
	 * Gets the main thread and UI application context
	 * 
	 * @return The context in a Context instance
	 */
	public Context getUIContext();

	/**
	 * Runs Runnable object on the main UI thread
	 * 
	 * @param r The Runnable implementation to execute
	 */
	public void runOnUIThread(Runnable r);
	
	/**
	 * Gets the GL thread and GPU context
	 * 
	 * @return The context in a GlContext instance
	 */
	public GlContext getGLContext();
	
	/**
	 * Runs Runnable object on the GL thread
	 * 
	 * @param r The Runnable implementation to execute
	 */
	public void runOnGLThread(Runnable r);

}
