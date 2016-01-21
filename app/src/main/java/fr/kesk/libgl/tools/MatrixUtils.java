package fr.kesk.libgl.tools;

import android.opengl.Matrix;

/**
 * Tools for matrix treatments.
 * <br/>
 * <br/>
 * Replace Android class with dedicated methods and perfs.
 * 
 * @author Thomas MILLET
 *
 */
public final class MatrixUtils extends Matrix{
	
	/**
	 * TAG log
	 */
	@SuppressWarnings("unused")
	private final static String TAG = MatrixUtils.class.getName();
	
	/**
	 * Cache matrix
	 */
	private final static float[]sTemp = new float[32];
	
	/**
	 * Cache lookat vector
	 */
	private final static float[]lookAtTemp = new float[8];
	
	/**
	 * Set a vector to the origin
	 * 
	 * @param vec The vector to set (vec4)
	 * @param vOffset index into vec where the result vector starts
	 */
	public static void setOrigin(float[] vec, int vOffset){
		//android.util.Log.d(TAG,"setOrigin()");
		vec[vOffset + 0] = vec[vOffset + 1] = vec[vOffset + 2] = 0f;
		vec[vOffset + 3] = 1f;
	}
	
	/**
     * Sets matrix m to the identity matrix.
     * 
     * @param sm returns the result
     * @param smOffset index into sm where the result matrix starts
     */
    public static void setIdentityM(float[] sm, int smOffset) {
    	//android.util.Log.d(TAG,"setIdentityM()");
    	sm[smOffset + 1] = sm[smOffset + 2] = sm[smOffset + 3]
    			= sm[smOffset + 4] = sm[smOffset + 6] = sm[smOffset + 7]
    			= sm[smOffset + 8] = sm[smOffset + 9] = sm[smOffset + 11]
    			= sm[smOffset + 12] = sm[smOffset + 13] = sm[smOffset + 14] = 0;
    	sm[smOffset + 0] = sm[smOffset + 5] = sm[smOffset + 10] = sm[smOffset + 15] = 1f;
    }
	
	/**
	 * Define a projection matrix in terms of a field of view angle, an aspect ratio, and z clip planes
	 *
	 * @param m the float array that holds the perspective matrix
	 * @param offset the offset into float array m where the perspective matrix data is written
	 * @param fovy field of view in y direction, in degrees
	 * @param aspect width to height aspect ratio of the viewport
	 * @param zNear Z for clip pane
	 * @param zFar Z for far pane
	 */
	public static void perspectiveM(final float[] m, final int offset, final float fovy, final float aspect, final float zNear, final float zFar) {
		//android.util.Log.d(TAG,"perspectiveM()");
		final float f = 1.0f / (float) Math.tan(fovy * (Math.PI / 360.0));
		final float rangeReciprocal = 1.0f / (zNear - zFar);

		m[offset + 0] = f / aspect;
		m[offset + 1] = 0.0f;
		m[offset + 2] = 0.0f;
		m[offset + 3] = 0.0f;
		
		m[offset + 4] = 0.0f;
		m[offset + 5] = f;
		m[offset + 6] = 0.0f;
		m[offset + 7] = 0.0f;
		
		m[offset + 8] = 0.0f;
		m[offset + 9] = 0.0f;
		m[offset + 10] = (zFar + zNear) * rangeReciprocal;
		m[offset + 11] = -1.0f;

		m[offset + 12] = 0.0f;
		m[offset + 13] = 0.0f;
		m[offset + 14] = 2.0f * zFar * zNear * rangeReciprocal;
		m[offset + 15] = 0.0f;
	}
	
	
	/**
	 * Matrix multiplication (Java version is far faster than JNI one !!!)
	 * 
	 * @param result The float array that holds the result.
	 * @param resultOffset The offset into result array
	 * @param lhs The float array that holds the left-hand-side matrix.
	 * @param lhsOffset The offset into the lhs array where the lhs is stored
	 * @param rhs The float array that holds the right-hand-side matrix
	 * @param rhsOffset The offset into the rhs array where the rhs is stored.
	 */
	public static void multiplyMM(final float[] result, final int resultOffset, final float[] lhs, final int lhsOffset, final float[] rhs, final int rhsOffset){
		//android.util.Log.d(TAG,"multiplyMM()");
		for (int i=0 ; i<4 ; i++) {
	        final float rhs_i0 = rhs[ rhsOffset + 4*(i) ];
	        float ri0 = lhs[ lhsOffset ] * rhs_i0;
	        float ri1 = lhs[ lhsOffset + 1 ] * rhs_i0;
	        float ri2 = lhs[ lhsOffset + 2 ] * rhs_i0;
	        float ri3 = lhs[ lhsOffset + 3 ] * rhs_i0;
	        for (int j=1 ; j<4 ; j++) {
	        	final float rhs_ij = rhs[ rhsOffset + ((j)+ 4*(i)) ];
	            ri0 += lhs[ lhsOffset + 4*(j) ] * rhs_ij;
	            ri1 += lhs[ lhsOffset + ((1)+ 4*(j)) ] * rhs_ij;
	            ri2 += lhs[ lhsOffset + ((2)+ 4*(j)) ] * rhs_ij;
	            ri3 += lhs[ lhsOffset + ((3)+ 4*(j)) ] * rhs_ij;
	        }
	        result[ resultOffset + 4*(i) ] = ri0;
	        result[ resultOffset + ((1)+ 4*(i)) ] = ri1;
	        result[ resultOffset + ((2)+ 4*(i)) ] = ri2;
	        result[ resultOffset + ((3)+ 4*(i)) ] = ri3;
	    }
	}
	
	/**
	 * Vector Matrix multiplication (Java version is far faster than JNI one !!!)
	 * 
	 * @param result The float array that holds the result.
	 * @param resultOffset The offset into result array
	 * @param lhs The float array that holds the left-hand-side matrix.
	 * @param lhsOffset The offset into the lhs array where the lhs is stored
	 * @param rhs The float array that holds the right-hand-side matrix
	 * @param rhsOffset The offset into the rhs array where the rhs is stored.
	 */
	public static void multiplyMV(final float[] result, final int resultOffset, final float[] lhs, final int lhsOffset, final float[] rhs, final int rhsOffset){
		//android.util.Log.d(TAG,"multiplyMV()");
		synchronized(sTemp){
			sTemp[0] = rhs[rhsOffset];
			sTemp[1] = rhs[rhsOffset + 1];
			sTemp[2] = rhs[rhsOffset + 2];
			sTemp[3] = rhs[rhsOffset + 3];
			System.arraycopy(lhs, lhsOffset, sTemp, 4, 16);
			result[3 + resultOffset] = (sTemp[7] * sTemp[0]) + (sTemp[11] * sTemp[1]) + (sTemp[15] * sTemp[2]) + (sTemp[19] * sTemp[3]);
			result[2 + resultOffset] = (sTemp[6] * sTemp[0]) + (sTemp[10] * sTemp[1]) + (sTemp[14] * sTemp[2]) + (sTemp[18] * sTemp[3]);
			result[1 + resultOffset] = (sTemp[5] * sTemp[0]) + (sTemp[9] * sTemp[1]) + (sTemp[13]  * sTemp[2]) + (sTemp[17] * sTemp[3]);
			result[0 + resultOffset] = (sTemp[4] * sTemp[0]) + (sTemp[8] * sTemp[1]) + (sTemp[12]  * sTemp[2]) + (sTemp[16] * sTemp[3]);
		}
	}

	/**
	 * Add 2 matrix and store result in a third one
	 * 
	 * @param dstM The destination matrix
	 * @param dstOffset The destination matrix start offset
	 * @param firstM The first matrix to add
	 * @param firstOffset The first matrix start offset
	 * @param secondM The first matrix to add
	 * @param secondOffset The first matrix start offset
	 */
	public static final void addM(final float[] dstM, final int dstOffset, final float[] firstM, final int firstOffset, final float[] secondM, final int secondOffset){
		//android.util.Log.d(TAG,"add()");
		for(int i=firstOffset, j = secondOffset, k = dstOffset; i < firstM.length; i++, j++, k++){
			dstM[k] = firstM[i] + secondM[j];
		}
	}
		
	/**
	 * Substract 2 matrix and store result in a third one
	 * 
	 * @param dstM The destination matrix
	 * @param dstOffset The destination matrix start offset
	 * @param firstM The first matrix to add
	 * @param firstOffset The first matrix start offset
	 * @param secondM The first matrix to add
	 * @param secondOffset The first matrix start offset
	 */
	public static final void minusM(final float[] dstM, final int dstOffset, final float[] firstM, final int firstOffset, final float[] secondM, final int secondOffset){
		//android.util.Log.d(TAG,"minus()");
		for(int i=firstOffset, j = secondOffset, k = dstOffset; i < firstM.length; i++, j++, k++){
			dstM[k] = firstM[i] - secondM[j];
		}
	}
		
	/**
	 * Normalize Vector srcM to dstM
	 * 
	 * @param dstM The destination vector
	 * @param dstOffset The destination vector offset
	 * @param srcM The source vector
	 * @param srcOffset The source vector offset
	 */
	public static final void normalizeV(final float[] dstV, final int dstOffset, final float[] srcV, final int srcOffset){
		//android.util.Log.d(TAG,"normalizeV()");
		final float length = Matrix.length(srcV[srcOffset], srcV[srcOffset+1], srcV[srcOffset+2]);
		if(length > 0){
			dstV[dstOffset] = srcV[srcOffset] / length;
			dstV[dstOffset+1] = srcV[srcOffset+1] / length;
			dstV[dstOffset+2] = srcV[srcOffset+2] / length;
		}
		dstV[dstOffset+3] = 1f;
	}
	
	/**
     * Rotates matrix m by angle a (in degrees) around the axis (x, y, z)
     * @param rm returns the result
     * @param rmOffset index into rm where the result matrix starts
     * @param m source matrix
     * @param mOffset index into m where the source matrix starts
     * @param a angle to rotate in degrees
     * @param x scale factor x
     * @param y scale factor y
     * @param z scale factor z
     */
	public static void rotateM(float[] rm, int rmOffset, float[] m, int mOffset, float a, float x, float y, float z) {
		//android.util.Log.d(TAG,"rotateM()");
		synchronized(sTemp) {
            setRotateM(sTemp, 0, a, x, y, z);
            MatrixUtils.multiplyMM(rm, rmOffset, m, mOffset, sTemp, 0);
        }
    }
	 
	/**
	 * Build a LookAt matrix from a model matrix
	 * 
	 * @param matrix The matrix to set
	 * @param matrixOffset The offset in the view matrix
	 * @param nodeModel The matrix model
	 * @param nodeModelOffset The matrix model offset
	 * @param xUp The camera X scale factor
	 * @param yUp The camera Y scale factor
	 * @param zUp The camera Z scale factor
	 */
	public static final void setLookAtMM(final float[] matrix, final int matrixOffset, final float[] nodeModel, final int nodeModelOffset, final float xUp, final float yUp, final float zUp){
		//android.util.Log.d(TAG,"setLookAtMM()");
		synchronized(lookAtTemp) {
			//eye
			lookAtTemp[0] = lookAtTemp[1] = lookAtTemp[2] = 0;
			lookAtTemp[3] = 1;
			//Direction
			lookAtTemp[4] = lookAtTemp[5] = lookAtTemp[7] = 0;
			lookAtTemp[6] = -1;
			MatrixUtils.multiplyMV(lookAtTemp, 0, nodeModel, nodeModelOffset, lookAtTemp, 0);
			MatrixUtils.multiplyMV(lookAtTemp,  4, nodeModel, nodeModelOffset, lookAtTemp, 4);
			Matrix.setLookAtM(matrix, matrixOffset, lookAtTemp[0], lookAtTemp[1], lookAtTemp[2], lookAtTemp[0]+lookAtTemp[4], lookAtTemp[1]+lookAtTemp[5], lookAtTemp[2]+lookAtTemp[6], xUp, yUp, zUp);
		}
	}
		
}
