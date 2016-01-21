package fr.kesk.libgl.tools;

import fr.kesk.libgl.GlAssets.Node;

/**
 * Tools class for Node treatments
 * 
 * @author Thomas MILLET
 *
 */
public class NodeUtils {

	static
    {
        System.loadLibrary("LGL");
    }
	
	/**
	 * TAG log
	 */
	@SuppressWarnings("unused")
	private final static String TAG = NodeUtils.class.getName();
	
	/**
	 * TMP Matrix used for preparing nodes
	 */
	private final static float[] prepareMatrix = new float[16];
	
	/**
	 * TMP Matrix used for transformations
	 */
	private final static float[] tmpMatrix = new float[16];
		
	/**
	 * Prepare a node for Matrix operations by applying node transformations recursively.
	 * <br/>
	 * <br/>
	 * <b>Caution : </b>nodes must be prepared before any calls to multiplyMN, transateN, rotateN or scaleN
	 * <br/>
	 * <b>Caution : </b>nodes instances are automatically prepared by Node.newInstance() 
	 * 
	 * @param node The node to prepare
	 */
	public static void prepareNode(final Node node){
		//android.util.Log.d(TAG,"prepareNode()");
		synchronized(prepareMatrix) {
			MatrixUtils.setIdentityM(prepareMatrix, 0);
			_prepareNode(node, prepareMatrix);	
		}
	}
	
	/**
	 * Multiply a Node instance by a Matrix to tranform node model and also recursively subnodes
	 * 
	 * @param node The node to transform
	 * @param matrix The matrix to apply
	 * @param offset The matrix offset
	 */
	public static void multiplyMN(final Node node, final float[] matrix, final int offset){
		MatrixUtils.multiplyMM(node.model, 0, matrix, 0, node.model, 0);
		if(node.nodeInstances != null){
			for(Node subnnode : node.nodeInstances){
				multiplyMN(subnnode, matrix, 0);
			}
		}
	}
	
	/**
	 * Scale a node and its subnodes
	 * 
	 * @param node The node to scale
	 * @param x scale factor X
	 * @param y scale factor Y
	 * @param z scale factor Z
	 */
	public static void scaleN(final Node node, final float x, final float y, final float z){
		synchronized (tmpMatrix) {
			MatrixUtils.setIdentityM(tmpMatrix, 0);
			MatrixUtils.scaleM(tmpMatrix, 0, x, y, z);
			multiplyMN(node, tmpMatrix, 0);
		}
	}	
	
	/**
	 * Rotate a node and its subnodes
	 * 
	 * @param node The node to rotate
	 * @param a The angle in degrees
	 * @param x scale factor X
	 * @param y scale factor Y
	 * @param z scale factor Z
	 */
	public static void rotateN(final Node node, final float a, final float x, final float y, final float z){
		synchronized (tmpMatrix) {
			MatrixUtils.setRotateM(tmpMatrix, 0, a, x, y, z);
			multiplyMN(node, tmpMatrix, 0);
		}
	}
	
	/**
	 * Translate a node and its subnodes
	 * 
	 * @param node The node to translate
	 * @param x X translate coordinate
	 * @param y Y translate coordinate
	 * @param z Z translate coordinate
	 */
	public static void translateN(final Node node, final float x, final float y, final float z){
		synchronized (tmpMatrix) {
			MatrixUtils.setIdentityM(tmpMatrix, 0);
			MatrixUtils.translateM(tmpMatrix, 0, x, y, z);
			multiplyMN(node, tmpMatrix, 0);
		}
	}
	
	/**
	 * 	Inner recursive method to prepare node
	 */
	private static void _prepareNode(final Node node, final float[] parentModel){
		MatrixUtils.multiplyMM(node.model, 0, parentModel, 0, node.model, 0);
		if(node.nodeInstances != null){
			for(Node subnnode : node.nodeInstances){
				 _prepareNode(subnnode, node.model);
			}
		}
	}
	
	
}
