package fr.kesk.libgl.ui;

import fr.kesk.libgl.ContextManager;
import android.opengl.GLException;

/**
 * Generic graphic element definition.
 * <br/>
 * <br/>
 * When used in SceneView, all method are called in GL Thread
 * 
 * @author Thomas MILLET
 *
 */
public abstract class GlElement {
	
	/**
	 * TAG log
	 */
	@SuppressWarnings("unused")
	private final static String TAG = GlElement.class.getName();
	
	/**
	 * Get the element ID
	 * 
	 * @return The element id
	 */
	public abstract int getId();
	
	/**
	 * Create and initialize the element.
	 * 
	 * Default implementation just call the onCreate method.
	 * 
	 * @param contextManager The ContextManager based on owner state
	 * 
	 * @throws GLException
	 */
	public final void create(final ContextManager contextManager) throws GLException{
		//android.util.Log.d(TAG,"create()");
		this.onCreate(contextManager);
	}
	
	/**
	 * Called at element creation, should be overridden by subclasses for extra init.
	 * 
	 * @param contextManager The ContextManager based on owner state
	 * 
	 * @throws GLException
	 */
	protected abstract void onCreate(final ContextManager contextManager) throws GLException;
	
	/**
	 * Called when viewport dimensions has changed
	 * 
	 * Default implementation just call the onLayout method.
	 * 
	 * @param width WIDTH of the viewport
	 * @param height HEIGHT of the viewport
	 * 
	 */
	public final void resize(int width, int height){
		//android.util.Log.d(TAG,"doResize("+width+", "+height+")");
		this.onResize(width, height);
	}
	
	/**
	 * Called when viewport dimensions has changed, should be overridden by subclasses
	 * to implement specific layout operations on resize.
	 * 
	 * @param width WIDTH of the viewport
	 * @param height HEIGHT of the viewport
	 * 
	 */
	protected abstract void onResize(int width, int height);
	
	/**
	 * Recalculate element assets based elapsed time since last draw call.
	 * 
	 * Default implementation just call the onLayout method.
	 * 
	 * @param elapsedTime The time elapsed since last draw call in ms
	 * 
	 */
	public final void layout(final long elapsedTime){
		//android.util.Log.d(TAG,"doLayout("+elapsedTime+")");
		this.onLayout(elapsedTime);
	}
	
	/**
	 * Called at element assets layout, should be overridden by subclasses to implement
	 * specific layout operations
	 * 
	 * @param elapsedTime The time elapsed since last draw call in ms
	 *
	 */
	protected abstract void onLayout(final long elapsedTime);
	
	/**
	 * Draws element on GL surface.
	 * 
	 * Default implementation just call the onDraw method.
	 *  
	 * @throws GLException
	 */
	public final void draw() throws GLException{
		//android.util.Log.d(TAG,"draw()");
		this.onDraw();
	}
	
	/**
	 * Called at element drawing
	 * 
	 * This method MUST be dedicated to drawing only. Size, position and
	 * any vertex attribute must be calculated in layout().
	 *
	 * @throws GLException
	 */
	protected abstract void onDraw() throws GLException;

	/**
	 * Called to free any resource attached to this element
	 *  
	 * Default implementation just call the onFree method.
	 *  
	 * @throws GLException
	 */
	public final void free() throws GLException{
		//android.util.Log.d(TAG,"draw()");
		this.onFree();
	}
	
	/**
	 * Called at element destruction
	 * 
	 * This method MUST be called in the GL Thread
	 * 
	 * @throws GLException
	 */
	protected abstract void onFree() throws GLException;
	
}
