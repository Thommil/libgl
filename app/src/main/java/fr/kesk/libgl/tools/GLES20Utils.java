package fr.kesk.libgl.tools;

/**
 * Extensions and fixes of OpenGL ES2
 * 
 * @author Thomas MILLET
 *
 */
public class GLES20Utils {
	
	static
    {
        System.loadLibrary("LGL");
    }
	
	/**
	 * Workaround to use VBO on Android 2.2, call this method instead of GLES20.glVertexAttribPointer()
	 */
    native public static void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, int offset);

    /**
	 * Workaround to use VBO on Android 2.2, call this method instead of GLES20.glDrawElements()
	 */
    native public static void glDrawElements(int mode, int count, int type, int offset);

}
