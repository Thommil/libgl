package fr.kesk.libgl.tools;

import java.nio.ByteBuffer;

/**
 * Helper class to handle colors
 * <br/>
 * <br/>
 * TODO ByteOrder seems different on 2.2, to check !!! 
 * 
 * @author Thomas MILLET
 */
public final class ColorUtils {
	
	/**
	 * TAG log
	 */
	@SuppressWarnings("unused")
	private final static String TAG = ColorUtils.class.getName();
	
	/**
	 * Convert RGB color buffer to RGBA color buffer 
	 * 
	 * @param rgbBuffer The RGB buffer
	 * @return The converted RGBA buffer
	 */
	public static ByteBuffer RGBtoRGBA(final ByteBuffer rgbBuffer){
		//android.util.Log.d(TAG,"RGBtoRGBA()");
		final ByteBuffer rgbaBuffer = ByteBufferPool.getInstance().getDirectByteBuffer(rgbBuffer.capacity() * 4 / 3);
		rgbaBuffer.position(0);
		rgbBuffer.position(0);
		final byte[] pixelBytes = new byte[3];
		final byte opacity = 1;
		while(rgbaBuffer.hasRemaining()){
			((ByteBuffer)rgbBuffer).get(pixelBytes);
			rgbaBuffer.put(pixelBytes);
			rgbaBuffer.put(opacity);
		}
		
		ByteBufferPool.getInstance().returnDirectBuffer(rgbBuffer);
		
		return (ByteBuffer)rgbaBuffer.position(0);
	}
	
	/**
	 * Convert RGBA color buffer to RGB color buffer 
	 * 
	 * @param rgbaBuffer The RGBA buffer
	 * @return The converted RGB buffer
	 */
	public static ByteBuffer RGBAtoRGB(final ByteBuffer rgbaBuffer){
		//android.util.Log.d(TAG,"RGBAtoRGB()");
		final ByteBuffer rgbBuffer = ByteBufferPool.getInstance().getDirectByteBuffer(rgbaBuffer.capacity() * 3 / 4); 
		rgbBuffer.position(0);
		rgbaBuffer.position(0);
		final byte[] pixelBytes = new byte[3]; 
		while(rgbaBuffer.hasRemaining()){
			((ByteBuffer)rgbaBuffer).get(pixelBytes);
			rgbBuffer.put(pixelBytes);
			((ByteBuffer)rgbaBuffer).get();
		}
		
		ByteBufferPool.getInstance().returnDirectBuffer(rgbaBuffer);
		
		return (ByteBuffer)rgbBuffer.position(0);
	}
	
	/**
	 * Convert RGBA color buffer to RGB565 color buffer 
	 * 
	 * @param rgbaBuffer The RGBA buffer
	 * @return The converted RGB565 buffer
	 */
	public static ByteBuffer RGBAtoRGB565(final ByteBuffer rgbaBuffer){
		//TODO RGBAtoRGB565()
		return null;
	}
	
	/**
	 * Convert RGB color buffer to RGB565 color buffer 
	 * 
	 * @param rgbBuffer The RGB buffer
	 * @return The converted RGB565 buffer
	 */
	public static ByteBuffer RGBtoRGB565(final ByteBuffer rgbBuffer){
		//TODO RGBtoRGB565()
		return null;
	}
	
	/**
	 * Convert RGBA color buffer to RGB4444 color buffer 
	 * 
	 * @param rgbaBuffer The RGBA buffer
	 * @return The converted RGB4444 buffer
	 */
	public static ByteBuffer RGBAtoRGB4444(final ByteBuffer rgbaBuffer){
		//TODO RGBAtoRGB4444()
		return null;
	}
	
	/**
	 * Convert RGB color buffer to RGB4444 color buffer 
	 * 
	 * @param rgbBuffer The RGB buffer
	 * @return The converted RGB4444 buffer
	 */
	public static ByteBuffer RGBtoRGB4444(final ByteBuffer rgbBuffer){
		//TODO RGBtoRGB4444()
		return null;
	}
	
	/**
	 * Convert RGBA color buffer to RGB5551 color buffer 
	 * 
	 * @param rgbaBuffer The RGBA buffer
	 * @return The converted RGB5551 buffer
	 */
	public static ByteBuffer RGBAtoRGB5551(final ByteBuffer rgbaBuffer){
		//TODO RGBAtoRGB5551()
		return null;
	}
	
	/**
	 * Convert RGB color buffer to RGB5551 color buffer 
	 * 
	 * @param rgbBuffer The RGB buffer
	 * @return The converted RGB5551 buffer
	 */
	public static ByteBuffer RGBtoRGB5551(final ByteBuffer rgbBuffer){
		//TODO RGBtoRGB5551()
		return null;
	}
}
