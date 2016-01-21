package fr.kesk.libgl.loader;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;


import fr.kesk.libgl.ContextManager;
import fr.kesk.libgl.GlAssets;
import fr.kesk.libgl.GlAssets.Camera;
import fr.kesk.libgl.GlAssets.Geometry;
import fr.kesk.libgl.GlAssets.Light;
import fr.kesk.libgl.GlAssets.Material;
import fr.kesk.libgl.GlAssets.Node;
import fr.kesk.libgl.GlAssets.Node.GeometryInstance;
import fr.kesk.libgl.GlAssets.Node.GeometryInstance.MaterialInstance;
import fr.kesk.libgl.GlAssets.Scene;
import fr.kesk.libgl.tools.ByteBufferPool;
import fr.kesk.libgl.tools.MatrixUtils;
import fr.kesk.libgl.tools.NodeUtils;
import fr.kesk.libgl.buffer.GlBuffer;
import fr.kesk.libgl.texture.ETC1GlTexture;
import fr.kesk.libgl.texture.GlTexture;
import fr.kesk.libgl.texture.GlTextureSet;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLException;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;

/**
 * Synchronous and Asynchronous loader for .lgl files.<br/>
 * <br/>
 * Asynchronous loading :
 *	<ul>
 *		<li>executed on a dedicated thread</li>
 *		<li>thread safe</li>
 *		<li>set assets in specified GlContext.assets and direct upload in GL server</li>
 *		<li>adapted to large assets</li>
 *		<li>not adapted to editable assets</li>
 *	</ul>
 * <br/>
 * Synchronous loading :
 *	<ul>
 *		<li>executed on calling thread</li>
 *		<li>thread safe</li>
 *		<li>set assets in specified GlContext.assets in GL client (excluding imagery)</li>
 *		<li>adapted to small assets</li>
 *		<li>adapted to editable assets</li>
 *	</ul>
 * 
 * 
 * @author Thomas MILLET
 *
 */
public class BinaryLoader {
	
	/**
	 * TAG log
	 */
	@SuppressWarnings("unused")
	private final static String TAG = BinaryLoader.class.getName();

	/**
	 * Timeout in ms in SYNC mode before error (long loading should be done in ASYNC mode)
	 */
	private final static int SYNC_TIMEOUT = 10000;
	
	/**
	 * Singleton
	 */
	private static final BinaryLoader binaryLoaderSingleton = new BinaryLoader();
	
	/**
	 * Indicates a synchronous loading
	 */
	private static final int SYNC = 0;
	
	/**
	 * Indicates a asynchronous loading
	 */
	private static final int ASYNC = 1;
	
	/**
	 * An android handler to start async tasks
	 */
	private final Handler handler = new Handler(Looper.getMainLooper());
	
	/**
	 * Singleton constructor
	 */
	protected BinaryLoader(){
		//android.util.Log.d(TAG,"NEW");
	}
	
	/**
	 * Singleton accessor
	 * 
	 * @return The BinaryLoader instance
	 */
	public static BinaryLoader getInstance(){
		//android.util.Log.d(TAG,"getInstance()");
		return BinaryLoader.binaryLoaderSingleton;
	}
	
	
	/**
	 * Load a resource asynchronously, set assets in assets 
	 * of current context using resources from ResourceResolver 
	 * Uploads assets on GPL sever and notifies listener of loading events<br/><br/>
	 * 
	 * @param in The source inputstream on .lgl file
	 * @param contextManager The current context manager implementation
	 * @param onBinaryLoaderEventListener The bound listener
	 * @param fullEvents If set to true, all events are sent to the listener,
	 * 		  only error and completion are handled	 otherwise
	 */
	public void loadAsynchronously(final InputStream in, final ContextManager contextManager, final OnBinaryLoaderEventListener onBinaryLoaderEventListener, final boolean fullEvents){
		//android.util.Log.d(TAG,"loadAsynchronously()");
		this.handler.post(new Runnable(){
			public void run(){
				try{
					doLoad(in, contextManager, null, onBinaryLoaderEventListener, fullEvents);
				}catch(LoaderException le){
					onBinaryLoaderEventListener.onError(le.getMessage(), le.getCause());
				}
			}
		});
	}
	
	/**
	 * Load a resource asynchronously, set assets in assets 
	 * of current context using resources from AssetManager 
	 * Uploads assets on GPL sever and notifies listener of loading events<br/><br/>
	 * 
	 * @param in The source inputstream on .lgl file
	 * @param contextManager The current context manager implementation
	 * @param resourceResolver The resource resolver to use or null for default one on Assets
	 * @param onBinaryLoaderEventListener The bound listener
	 * @param fullEvents If set to true, all events are sent to the listener,
	 * 		  only error and completion are handled otherwise
	 */
	public void loadAsynchronously(final InputStream in, final ContextManager contextManager, final ResourceResolver resourceResolver, final OnBinaryLoaderEventListener onBinaryLoaderEventListener, final boolean fullEvents){
		//android.util.Log.d(TAG,"loadAsynchronously()");
		this.handler.post(new Runnable(){
			public void run(){
				try{
					doLoad(in, contextManager, resourceResolver, onBinaryLoaderEventListener, fullEvents);
				}catch(LoaderException le){
					onBinaryLoaderEventListener.onError(le.getMessage(), le.getCause());
				}
			}
		});
	}
	
	/**
	 * Load resource synchronously and set assets in assets of current context using resources from ResourceResolver<br/><br/>
	 * 
	 * @param in The source inputstream on .lgl file
	 * @param contextManager The current context manager implementation
	 * @param resourceResolver The resource resolver to use or null for default one on Assets
	 */
	public void loadSynchronously(final InputStream in, final ContextManager contextManager, final ResourceResolver resourceResolver) throws LoaderException{
		//android.util.Log.d(TAG,"loadSynchronously()");
		this.doLoad(in, contextManager, resourceResolver, null, false);
	}
	
	/**
	 * Load resource synchronously and set assets in assets of current context using resources from AssetManager<br/><br/>
	 * 
	 * @param in The source inputstream on .lgl file
	 * @param contextManager The current context manager implementation
	 */
	public void loadSynchronously(final InputStream in, final ContextManager contextManager) throws LoaderException{
		//android.util.Log.d(TAG,"loadSynchronously()");
		this.doLoad(in, contextManager, null, null, false);
	}

	/**
	 * Do the effective loading
	 * 
	 * @param in The inputstream on .lgl stream
	 * @param contextManager The context manager implementation
	 * @param resourceResolver The resource resolver to use or null for default one on Assets
	 * @param onBinaryLoaderEventListener The bound listener
	 * @param fullEvents If set to true, all events are sent to the listener, only error and completion are handled otherwise 
	 */
	protected void doLoad(final InputStream in, final ContextManager contextManager, ResourceResolver resourceResolver,  final OnBinaryLoaderEventListener onBinaryLoaderEventListener, final boolean fullEvents) throws LoaderException{
		//android.util.Log.d(TAG,"doLoad()");
		final int MODE = (onBinaryLoaderEventListener == null) ? SYNC : ASYNC;
		final GlAssets assets = contextManager.getGLContext().assets;
		
		if(resourceResolver == null){
			resourceResolver = new ResourceResolver() {
				
				@Override
				public InputStream getResourceInputStream(int resourceType, String resourcePath, int readMode) throws IOException {
					if(resourcePath.startsWith("./")) resourcePath = resourcePath.substring(2);
					return contextManager.getUIContext().getAssets().open(resourcePath, readMode);
				}
			};
		}
		
		//Tmp List to build arrays dynamically
		final List<Camera> cameraInstances = new ArrayList<Camera>();
		final List<Light> lightInstances = new ArrayList<Light>();
		final List<Node> nodeInstances = new ArrayList<Node>();
		final List<GeometryInstance> geometryInstances = new ArrayList<GeometryInstance>();
		
		DataInputStream dataIn = null;
		try{
			final StringBuilder stringBuilder = new StringBuilder();
			char tmpChar;
			final GlTexture[] tmpTexture = new GlTexture[1];
			final int[] texturesLoaded = new int[]{0};
			dataIn = new DataInputStream(in);
			
			
			/************************************************************************************************
			 * HEADER - Header	
			 ************************************************************************************************
			 
			  [HEADER][$VERSION]
			  [$GEOMETRY_COUNT][$LIGHT_COUNT][$CAMERA_COUNT][$IMAGERY_COUNT][$MATERIAL_COUNT][$SCENE_COUNT]
			  
			 ************************************************************************************************/
			dataIn.readInt();
			float version = dataIn.readFloat();
			
			if(version != API.VERSION){
				throw new LoaderException("Bad file version "+version+ "(expecting "+API.VERSION+")");
			}
			
			final int geometryCount = dataIn.readInt();
			final int lightCount = dataIn.readInt();
			final int cameraCount = dataIn.readInt();
			final int imageryCount = dataIn.readInt();
			final int materialCount = dataIn.readInt();
			final int nodeCount = dataIn.readInt();
			final int sceneCount = dataIn.readInt();
			
			if(fullEvents && MODE == ASYNC){
				contextManager.runOnGLThread(new Runnable() {
					@Override
					public void run() {
						onBinaryLoaderEventListener.onHeader(geometryCount, lightCount, cameraCount, imageryCount, materialCount, nodeCount, sceneCount);
					}
				});
			}
			
			while(dataIn.available() > 0){
				final int entryType = dataIn.readInt();
				switch(entryType){
					case API.GEOMETRY:
						/************************************************************************************************
						 * GEOMETRY	- Describes the visual shapes and appearances of an object in a scene.	
						 ************************************************************************************************
						 
						  [GEOMETRY][$ID][$PRIMITIVES_COUNT]
							  [$PRIMITIVE_TYPE][$INPUT_COUNT][$VERTEX_COUNT]
							  [$SEMANTIC][$SET][$SIZE][$OFFSET]...[$SEMANTIC][$SET][$SIZE][$OFFSET]
							  [$VALUES]
							  ...
							  [$PRIMITIVE_TYPE][$INPUT_COUNT][$VERTEX_COUNT]
							  [$SEMANTIC][$SET][$SIZE][$OFFSET]...[$SEMANTIC][$SET][$SIZE][$OFFSET]
							  [$VALUES]
						  ...
						  [GEOMETRY][$ID][$PRIMITIVES_COUNT]
						  
						 ************************************************************************************************/
						//[GEOMETRY][$ID][$PRIMITIVES_COUNT]
						final int gId = dataIn.readInt();
						final int pCount = dataIn.readInt();
						final Geometry geometry = new Geometry(gId);
						
						//[$PRIMITIVE_TYPE][$INPUT_COUNT][$VERTEX_COUNT][$MATERIAL_ID]
						for(int pIndex=0; pIndex < pCount; pIndex++){
							final int pCurrent = pIndex;
							final int pType = dataIn.readInt();
							final int iCount = dataIn.readInt();
							final int vCount = dataIn.readInt();
							final int pMaterialId = dataIn.readInt();
							final int[][] inputs = new int[iCount][4];
							int dataSize = 0;
							
							//[$SEMANTIC][$SET][$SIZE][$OFFSET]...[$SEMANTIC][$SET][$SIZE][$OFFSET]
							for(int iIndex=0; iIndex < iCount; iIndex++){
								inputs[iIndex][GlAssets.Geometry.Element.SEMANTIC] = dataIn.readInt();
								inputs[iIndex][GlAssets.Geometry.Element.SET] = dataIn.readInt();
								inputs[iIndex][GlAssets.Geometry.Element.SIZE] = dataIn.readInt();
								inputs[iIndex][GlAssets.Geometry.Element.OFFSET] = dataIn.readInt() * GlBuffer.SIZEOF_JAVA_FLOAT;
								dataSize += vCount * inputs[iIndex][GlAssets.Geometry.Element.SIZE]; 
							}
							
							final int bufferSize = GlBuffer.SIZEOF_JAVA_FLOAT*dataSize;
							final ByteBuffer iBuffer = ByteBufferPool.getInstance().getDirectByteBuffer(bufferSize);
							final ReadableByteChannel inChannel = Channels.newChannel(in);
							inChannel.read(iBuffer);
							iBuffer.position(0);
							
							if(MODE == ASYNC){
								//Upload data and notify listener
								contextManager.runOnGLThread(new Runnable() {
									
									@Override
									public void run() {
										final int[] handles = new int[1];
										try{
											//Generate buffer
											GLES20.glGenBuffers(1, handles, 0);
											//Bint it
											GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, handles[0]);
											//Push data into it
											GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bufferSize, iBuffer, GLES20.GL_STATIC_DRAW);
											//Unbind it
											GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, GLES20.GL_ZERO);
										}finally{
											ByteBufferPool.getInstance().returnDirectBuffer(iBuffer);
										}
										geometry.elements.add(new GlAssets.Geometry.Element(pType, vCount,bufferSize, pMaterialId, inputs, handles[0]));
										
										//Append geometry in ASYNC mode
										if(pCurrent == (pCount-1)){
											if(fullEvents){
												onBinaryLoaderEventListener.onGeometryLoaded(gId);
											}
										}
									}
								});
							}
							else{
								int iIndex=0, vIndex=0;
								final float [][]values = new float[inputs.length][];
								for(iIndex=0; iIndex < inputs.length; iIndex++){
									values[iIndex] = new float[inputs[iIndex][GlAssets.Geometry.Element.SIZE]*vCount];
								}
								try{
									while(vIndex < vCount){
										for(iIndex=0; iIndex < inputs.length; iIndex++){
											final int baseIndex = inputs[iIndex][GlAssets.Geometry.Element.SIZE]*vIndex;
											for(int iiIndex=0; iiIndex< inputs[iIndex][GlAssets.Geometry.Element.SIZE]; iiIndex++){
												values[iIndex][baseIndex+iiIndex] = iBuffer.getFloat();
											}
										}
										vIndex++;
									}
								}finally{
									ByteBufferPool.getInstance().returnDirectBuffer(iBuffer);
								}	
								geometry.elements.add(new GlAssets.Geometry.Element(pType, vCount, bufferSize, pMaterialId, inputs, values)); 
							}
						}
						
						assets.geometries.append(gId, geometry);
						
						break;
					case API.LIGHT:
						/************************************************************************************************
						 * LIGHT	- Describes the light source in the scene	
						 ************************************************************************************************
						 
						  [LIGHT][$ID][$LIGHT_TYPE]
						  case AMBIENT : [$COLOR_R][$COLOR_G][$COLOR_B]
						  case DIRECTIONAL : [$COLOR_R][$COLOR_G][$COLOR_B]
						  case POINT : [$COLOR_R][$COLOR_G][$COLOR_B][$KC][$KL][$KQ]
						  case SPOT : [$COLOR_R][$COLOR_G][$COLOR_B][$KC][$KL][$KQ][$FALLOFF_ANGLE][$FALLOFF_EXP]
						  ...
						  [LIGHT][$ID]
						  
						 ************************************************************************************************/
						final int lId = dataIn.readInt();
						final int lType = dataIn.readInt();
						final float[] lColor = new float[]{dataIn.readFloat(), dataIn.readFloat(), dataIn.readFloat(), 1f};
						final float[] lSettings;
						
						switch(lType){
							case API.POINT:
								lSettings = new float[]{dataIn.readFloat(), dataIn.readFloat(), dataIn.readFloat()};
								break;
							case API.SPOT:
								lSettings = new float[]{dataIn.readFloat(), dataIn.readFloat(), dataIn.readFloat(), dataIn.readFloat(), dataIn.readFloat()};
								break;
							default:
								lSettings = new float[0];
						}
						
						final Light light = new Light(lId, lType, lColor, lSettings);
						assets.lights.append(lId, light);

						if(fullEvents && MODE == ASYNC){
							contextManager.runOnGLThread(new Runnable() {
								@Override
								public void run() {
									onBinaryLoaderEventListener.onLightLoaded(lId);
								}
							});
						}
						break;
					case API.CAMERA:
						/************************************************************************************************
						 * CAMERA	- Describes the optics in the scene	
						 ************************************************************************************************
						 
						  [CAMERA][$ID][$CAMERA_TYPE]
						  case PERSPECTIVE : [$XFOV][$YFOV][$ASPECT_RATIO][$ZNEAR][$ZFAR][$UP_X][$UP_Y][$UP_Z]
						  case ORTHOGRAPHIC : [$XMAG][$YMAG][$ASPECT_RATIO][$ZNEAR][$ZFAR][$UP_X][$UP_Y][$UP_Z]
						  ...
						  [CAMERA][$ID]
						  
						 ************************************************************************************************/
						final int cId = dataIn.readInt();
						final int cType = dataIn.readInt();
						final float[] cSettings = new float[]{dataIn.readFloat(), dataIn.readFloat(), dataIn.readFloat(), dataIn.readFloat(), dataIn.readFloat()};
						
						final Camera camera = new Camera(cId, cType, cSettings);
						camera.up[0] = dataIn.readFloat();
						camera.up[1] = dataIn.readFloat();
						camera.up[2] = dataIn.readFloat();
						assets.cameras.append(cId, camera);

						if(fullEvents && MODE == ASYNC){
							contextManager.runOnGLThread(new Runnable() {
								@Override
								public void run() {
									onBinaryLoaderEventListener.onCameraLoaded(cId);
								}
							});
						}
						
						break;
					case API.IMAGERY:
						/************************************************************************************************
						 * IMAGERY	- Describes the images in the scene	
						 ************************************************************************************************
						 
						  [IMAGE][$ID][$FORMAT][$INTERNAL_FORMAT][$TYPE][$WIDTH][$HEIGHT][$COMPRESS][$MAG][$MIN][$PATH]
						  ...
						  [IMAGE][$ID][$FORMAT][$INTERNAL_FORMAT][$TYPE][$WIDTH][$HEIGHT][$COMPRESS][$MAG][$MIN][$PATH]
						  
						 ************************************************************************************************/
						final int iId = dataIn.readInt();
						@SuppressWarnings("unused")
						final int iFormat = dataIn.readInt();
						final int iiFormat = dataIn.readInt();
						final int iType = dataIn.readInt();
						final int iWidth = dataIn.readInt();
						final int iHeight = dataIn.readInt();
						final boolean iCompress = dataIn.readBoolean();
						final int iMag = dataIn.readInt();
						final int iMin = dataIn.readInt();
						final int iSize;
						
						stringBuilder.delete(0, stringBuilder.length());
						while((tmpChar = dataIn.readChar()) != '\0'){
							stringBuilder.append(tmpChar);
						}

						ByteBuffer iBuffer = null;
						InputStream iIn = null;
						try{
							iIn = resourceResolver.getResourceInputStream(API.IMAGERY, stringBuilder.toString(), AssetManager.ACCESS_STREAMING);
							final Bitmap bitmap = BitmapFactory.decodeStream(iIn);
							iSize = bitmap.getRowBytes() * bitmap.getHeight();
							iBuffer = ByteBufferPool.getInstance().getDirectByteBuffer(iSize);
							bitmap.copyPixelsToBuffer(iBuffer);
							bitmap.recycle();
							
							final ByteBuffer texBuffer = (ByteBuffer)iBuffer.position(0); 
							tmpTexture[0] = new GlTexture() {
								
								@Override
								public ByteBuffer getBytes() {
									return texBuffer;
								}
								
								@Override
								public int getFormat() {
									return iiFormat;
								}
	
								@Override
								public int getType() {
									return iType;
								}
	
								@Override
								public int getWrapMode(int axeId) {
									return GlTexture.WRAP_CLAMP_TO_EDGE;
								}
	
								@Override
								public int getMagnificationFilter() {
									if(iMag == API.UNSPECIFIED){
										return GlTexture.MAG_FILTER_LOW;
									}
									return iMag;
								}
	
								@Override
								public int getMinificationFilter() {
									if(iMin == API.UNSPECIFIED){
										return GlTexture.MIN_FILTER_LOW;
									}
									return iMin;
								}
	
								@Override
								public int getWidth() {
									return iWidth;
								}
								
								@Override
								public int getSize() {
									return iSize;
								}
								
								@Override
								public int getId() {
									return iId;
								}
								
								@Override
								public int getHeight() {
									return iHeight;
								}
							};
							
							if(iCompress){
								tmpTexture[0] = new ETC1GlTexture(tmpTexture[0]);
							}
							
						}catch(final IOException ioe){
							contextManager.runOnGLThread(new Runnable() {
								@Override
								public void run() {
									onBinaryLoaderEventListener.onError("Failed to load texture "+iId, ioe);
								}
							});
						}finally{
							if(iIn != null){
								try{
									iIn.close();
								}catch(IOException ioe){}
							}
							if(iBuffer != null) ByteBufferPool.getInstance().returnDirectBuffer(iBuffer);
						}
						
						
						contextManager.runOnGLThread(new Runnable() {
							@Override
							public void run() {
								if(MODE == ASYNC){
									assets.textures.setOnTextureEventListener(new GlTextureSet.OnTextureEventListener() {
										@Override
										public void onTextureError(int textureIndex, GLException glException) {
											onBinaryLoaderEventListener.onError("Failed to load texture "+iId, glException);
										}
										
										@Override
										public void onTextureBound(int textureIndex, int textureHandle) {
											if(fullEvents){
												onBinaryLoaderEventListener.onImageLoaded(iId);
											}
										}
									});
								}
								else{
									assets.textures.setOnTextureEventListener(new GlTextureSet.OnTextureEventListener() {
										@Override
										public void onTextureError(int textureIndex, GLException glException) {
											texturesLoaded[0]++;
										}
										
										@Override
										public void onTextureBound(int textureIndex, int textureHandle) {
											texturesLoaded[0]++;
										}
									});
								}
								assets.textures.put(tmpTexture);
							}
						});
						break;
					case API.MATERIAL:
						/************************************************************************************************
						 * MATERIAL	- Describes the materials in the scene	
						 ************************************************************************************************
						 
						  [MATERIAL][$ID][$MATERIAL_TYPE]
							  [$EMISSION_VAR_TYPE][$EMISSION_VALUE]
							  [$REFLECTIVE_VAR_TYPE][$REFLECTIVE_VALUE]
							  [$REFLECTIVITY_VAR_TYPE][$REFLECTIVITY_VALUE]
							  [$TRANSPARENT_VAR_TYPE][$TRANSPARENT_VALUE]
							  [$TRANSPARENCY_VAR_TYPE][$TRANSPARENCY_VALUE]
							  [$REFRACTION_VAR_TYPE][$REFRACTION_VALUE]
							  [$AMBIENT_VAR_TYPE][$AMBIENT_VALUE]
							  [$DIFFUSE_VAR_TYPE][$DIFFUSE_VALUE]
							  [$SPECULAR_VAR_TYPE][$SPECULAR_VALUE]
							  [$SHININESS_VAR_TYPE][$SHININESS_VALUE]
						  ...
						  [MATERIAL][$ID][$MATERIAL_TYPE]
						  
						  $*_VAR_TYPE :
						  	case FLOAT : [$FLOAT_VALUE]
						  	case FLOAT3 : [$FLOAT_VALUE_0][$FLOAT_VALUE_1][$FLOAT_VALUE_2]
						  	case FLOAT4 : [$FLOAT_VALUE_0][$FLOAT_VALUE_1][$FLOAT_VALUE_2][$FLOAT_VALUE_3]
						  	case SAMPLER : [$IMAGERY_ID][$SEMANTIC]
						  	case UNSPECIFIED : -
						  
						 ************************************************************************************************/
						//[MATERIAL][$ID][$MATERIAL_TYPE]
						final int mId = dataIn.readInt();
						final int mType = dataIn.readInt();
						
						Material material = new Material(mId, mType);
						
						//[$EMISSION_VAR_TYPE][$EMISSION_VALUE]
						int varType = dataIn.readInt();
						if(varType == API.FLOAT4){
							material.emissionColor = new float[]{dataIn.readFloat(), dataIn.readFloat(), dataIn.readFloat(), dataIn.readFloat()};
						}
						else if(varType == API.SAMPLER){
							material.emissionBinding = new int[]{dataIn.readInt(), dataIn.readInt()};
							
						}
						
						//[$REFLECTIVE_VAR_TYPE][$REFLECTIVE_VALUE]
						varType = dataIn.readInt();
						if(varType == API.FLOAT4){
							material.reflectiveColor = new float[]{dataIn.readFloat(), dataIn.readFloat(), dataIn.readFloat(), dataIn.readFloat()};
						}
						else if(varType == API.SAMPLER){
							material.reflectiveBinding = new int[]{dataIn.readInt(), dataIn.readInt()};
						}
						
						//[$REFLECTIVITY_VAR_TYPE][$REFLECTIVITY_VALUE]
						varType = dataIn.readInt();
						if(varType == API.FLOAT){
							material.reflectivity = dataIn.readFloat();
						}
						 
						//[$TRANSPARENT_VAR_TYPE][$TRANSPARENT_VALUE]
						varType = dataIn.readInt();
						if(varType == API.FLOAT4){
							material.transparentColor = new float[]{dataIn.readFloat(), dataIn.readFloat(), dataIn.readFloat(), dataIn.readFloat()};
						}
						else if(varType == API.SAMPLER){
							material.transparentBinding = new int[]{dataIn.readInt(), dataIn.readInt()};
						}
						
						//[$TRANSPARENCY_VAR_TYPE][$TRANSPARENCY_VALUE]
						varType = dataIn.readInt();
						if(varType == API.FLOAT){
							material.transparency = dataIn.readFloat();
						}
						
						//[$REFRACTION_VAR_TYPE][$REFRACTION_VALUE]
						varType = dataIn.readInt();
						if(varType == API.FLOAT){
							material.refraction = dataIn.readFloat();
						}
						
						//[$AMBIENT_VAR_TYPE][$AMBIENT_VALUE]
						varType = dataIn.readInt();
						if(varType == API.FLOAT4){
							material.ambientColor = new float[]{dataIn.readFloat(), dataIn.readFloat(), dataIn.readFloat(), dataIn.readFloat()};
						}
						else if(varType == API.SAMPLER){
							material.ambientBinding = new int[]{dataIn.readInt(), dataIn.readInt()};
						}
						
						//[$DIFFUSE_VAR_TYPE][$DIFFUSE_VALUE]
						varType = dataIn.readInt();
						if(varType == API.FLOAT4){
							material.diffuseColor = new float[]{dataIn.readFloat(), dataIn.readFloat(), dataIn.readFloat(), dataIn.readFloat()};
						}
						else if(varType == API.SAMPLER){
							material.diffuseBinding = new int[]{dataIn.readInt(), dataIn.readInt()};
						}
						
						//[$SPECULAR_VAR_TYPE][$SPECULAR_VALUE]
						varType = dataIn.readInt();
						if(varType == API.FLOAT4){
							material.specularColor = new float[]{dataIn.readFloat(), dataIn.readFloat(), dataIn.readFloat(), dataIn.readFloat()};
						}
						else if(varType == API.SAMPLER){
							material.specularBinding = new int[]{dataIn.readInt(), dataIn.readInt()};
						}
						
						//[$SHININESS_VAR_TYPE][$SHININESS_VALUE]
						varType = dataIn.readInt();
						if(varType == API.FLOAT){
							material.shininess = dataIn.readFloat();
						}
						
						assets.materials.append(mId, material);

						if(fullEvents && MODE == ASYNC){
							contextManager.runOnGLThread(new Runnable() {
								@Override
								public void run() {
									onBinaryLoaderEventListener.onMaterialLoaded(mId);
								}
							});
						}
						
						break;
					case API.NODE:
						/************************************************************************************************
						 * NODE - Describes the nodes element in the scene	
						 ************************************************************************************************
						  
						  [NODE][$ID][$NODE_TYPE][$ELEMENT_COUNT]
						  		if LOOKAT : [LOOKAT][$EYE_X][$EYE_Y][$EYE_Z][$INTEREST_X][$INTEREST_Y][$INTEREST_Z][$UP_X][$UP_Y][$UP_Z]
						  		if MATRIX : [MATRIX][$0_0]...[$3_3]
						  		if ROTATE : [ROTATE][$ROTATE_X][$ROTATE_Y][$ROTATE_Z][$ROTATE_DEGRE]
						  		if SCALE : [SCALE][$SCALE_X][$SCALE_Y][$SCALE_Z]
						  		if SKEW : [SKEW][$ROTATE_DEGRE][$ROTATE_X][$ROTATE_Y][$ROTATE_Z][$TRANSLATE_X][$TRANSLATE_Y][$TRANSLATE_Z]
						  		if TRANSLATE : [TRANSLATE][$TRANSLATE_X][$TRANSLATE_Y][$TRANSLATE_Z]
						  		if CAMERA : [CAMERA][$CAMERA_ID]
						  		if GEOMETRY : [GEOMETRY][$GEOMETRY_ID][$MATERIAL_COUNT]
						  			[$MATERIAL_ID][$MATERIAL_TARGET_ID][$BIND_COUNT]
						  				[$INPUT_SEMANTIC][$INPUT_SET][$MATERIAL_SEMANTIC]
						  				...
						  				[$INPUT_SEMANTIC][$INPUT_SET][$MATERIAL_SEMANTIC]
						  			...
						  			[$MATERIAL_ID][$MATERIAL_TARGET_ID][$BIND_COUNT]
						  		if LIGHT : [LIGHT][$LIGHT_ID]
						  		if NODE : [NODE][$NODE_ID]
						  ...
						  [NODE][$ID][$NODE_TYPE][$ELEMENT_COUNT]
						 ************************************************************************************************/
						//[NODE][$ID][$NODE_TYPE][$ELEMENT_COUNT]
						final int nId = dataIn.readInt();
						final int nType = dataIn.readInt();
						final int nElements = dataIn.readInt();
						
						Node node = new Node(nId, nType);
						
						cameraInstances.clear();
						lightInstances.clear();
						nodeInstances.clear();
						geometryInstances.clear();
						
						for(int eIndex=0; eIndex < nElements; eIndex++){
							int nodeElement = dataIn.readInt();
							switch(nodeElement){
								//LOOKAT : [LOOKAT][$EYE_X][$EYE_Y][$EYE_Z][$INTEREST_X][$INTEREST_Y][$INTEREST_Z][$UP_X][$UP_Y][$UP_Z]
								case API.LOOKAT:
									Matrix.setLookAtM(node.model, 0, dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat());
									break;
								//MATRIX : [MATRIX][$0_0]...[$3_3]
								case API.MATRIX:
									MatrixUtils.multiplyMM(node.model, 0, new float[]{dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat()}, 0, node.model, 0);
									break;
								//ROTATE : [ROTATE][$ROTATE_X][$ROTATE_Y][$ROTATE_Z][$ROTATE_DEGRE]
								case API.ROTATE:
									final float[] rotate = new float[]{dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat()};
									MatrixUtils.rotateM(node.model, 0, rotate[3], rotate[0], rotate[1], rotate[2]);												
									break;
								//SCALE : [SCALE][$SCALE_X][$SCALE_Y][$SCALE_Z]
								case API.SCALE:
									Matrix.scaleM(node.model, 0, dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat());
									break;
								//SKEW : [SKEW][$ROTATE_DEGRE][$ROTATE_X][$ROTATE_Y][$ROTATE_Z][$TRANSLATE_X][$TRANSLATE_Y][$TRANSLATE_Z]
								case API.SKEW:
									float[] skew = new float[]{dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat()};
									MatrixUtils.rotateM(node.model, 0, skew[3], skew[0], skew[1], skew[2]);
									Matrix.translateM(node.model, 0, skew[4], skew[5], skew[6]);
									break;
								//TRANSLATE : [TRANSLATE][$TRANSLATE_X][$TRANSLATE_Y][$TRANSLATE_Z]
								case API.TRANSLATE:
									Matrix.translateM(node.model, 0, dataIn.readFloat(),dataIn.readFloat(),dataIn.readFloat());
									break;
								//CAMERA : [CAMERA][$CAMERA_ID]
								case API.CAMERA:
									cameraInstances.add(assets.cameras.get(dataIn.readInt()));
									break;
								//GEOMETRY : [GEOMETRY][$GEOMETRY_ID][$MATERIAL_COUNT]
								case API.GEOMETRY:
									final int ngId = dataIn.readInt();
									final MaterialInstance[] ngmInstances = new MaterialInstance[dataIn.readInt()];
									for(int ngmIndex=0; ngmIndex < ngmInstances.length; ngmIndex++){
										final int ngmId = dataIn.readInt();
										final int ngmTargetId = dataIn.readInt();
										final int[][] ngmbBindings = new int[dataIn.readInt()][];
										for(int ngmbIndex=0; ngmbIndex < ngmbBindings.length; ngmbIndex++){
											ngmbBindings[ngmbIndex] = new int[]{dataIn.readInt(),dataIn.readInt(),dataIn.readInt()};
										}
										ngmInstances[ngmIndex] = new MaterialInstance(ngmId,assets.materials.get(ngmTargetId),ngmbBindings);
									}
									
									final GeometryInstance geometryInstance = new GeometryInstance(assets.geometries.get(ngId),ngmInstances);
									geometryInstances.add(geometryInstance);
									break;
								//LIGHT : [LIGHT][$LIGHT_ID]
								case API.LIGHT:
									lightInstances.add(assets.lights.get(dataIn.readInt()));
									break;
								//NODE : [NODE][$NODE_ID]
								case API.NODE:
									nodeInstances.add(assets.nodes.get(dataIn.readInt()));
									break;
								default:
									throw new LoaderException("Unsupported node element "+nodeElement);
							}
						}
						
						int instanceIndex = 0;
						if(cameraInstances.size() > 0){
							node.cameraInstances = new Camera[cameraInstances.size()];
							for(Camera instance : cameraInstances){
								node.cameraInstances[instanceIndex++] = instance;	
							}
						}
						
						if(lightInstances.size() > 0){
							instanceIndex = 0;
							node.lightInstances = new Light[lightInstances.size()];
							for(Light instance : lightInstances){
								node.lightInstances[instanceIndex++] = instance;	
							}
						}
						
						if(geometryInstances.size() > 0){
							instanceIndex = 0;
							node.geometryInstances = new GeometryInstance[geometryInstances.size()];
							for(GeometryInstance instance : geometryInstances){
								node.geometryInstances[instanceIndex++] = instance;	
							}
						}
						
						if(nodeInstances.size() > 0){
							instanceIndex = 0;
							node.nodeInstances = new Node[nodeInstances.size()];
							for(Node instance : nodeInstances){
								node.nodeInstances[instanceIndex++] = instance;	
							}
						}
						
						assets.nodes.append(nId, node);
						
						if(fullEvents && MODE == ASYNC){
							contextManager.runOnGLThread(new Runnable() {
								@Override
								public void run() {
									onBinaryLoaderEventListener.onNodeLoaded(nId);
								}
							});
						}
						
						break;
					case API.SCENE:
						/************************************************************************************************
						 * SCENE - Describes the scene elements	
						 ************************************************************************************************
						  
						  [SCENE][$ID][$NODE_COUNT]
						  [NODE_ID]
						  ...		
						  [SCENE][$ID][$NODE_COUNT]
						 ************************************************************************************************/
						final int sId = dataIn.readInt();
						final Node[] sNodes = new Node[dataIn.readInt()];
						for(int nIndex=0; nIndex< sNodes.length; nIndex++){
							sNodes[nIndex] = assets.nodes.get(dataIn.readInt()).newInstance();
							NodeUtils.prepareNode(sNodes[nIndex]);
						}

						assets.scenes.append(sId, new Scene(sId, sNodes));

						if(fullEvents && MODE == ASYNC){
							contextManager.runOnGLThread(new Runnable() {
								@Override
								public void run() {
									onBinaryLoaderEventListener.onSceneLoaded(sId);
								}
							});
						}
						
						break;
						default:
							throw new LoaderException("Unsupported entry type "+entryType);
				}
			}
			
			//Wait for texture loading in SYNC mode
			int timer = 0;
			if(MODE == SYNC){
				while(texturesLoaded[0] < imageryCount){
					Thread.sleep(50);
					timer += 50;
					if(timer >= SYNC_TIMEOUT){
						throw new LoaderException("SYNC timeout ("+SYNC_TIMEOUT+"ms)"); 
					}
				}
			}
		}catch(Exception e){
			throw new LoaderException("Failed to load resources", e);
		}finally{
			if(MODE == ASYNC){
				contextManager.runOnGLThread(new Runnable() {
					@Override
					public void run() {
						onBinaryLoaderEventListener.onComplete(in);
					}
				});
			}
		}
	}
	
	/**
	 * This interface is used in parsing to find the resources from their path
	 * 
	 * @author Thomas MILLET
	 *
	 */
	public static interface ResourceResolver{
		
		/**
		 * Based on AssetManager access mode for small resources
		 */
		public static final int ACCESS_BUFFER = AssetManager.ACCESS_BUFFER;
		
		/**
		 * Based on AssetManager access mode for large resources
		 */
		public static final int ACCESS_STREAMING = AssetManager.ACCESS_STREAMING;
		
		/**
		 * Based on AssetManager access mode for full access mode for custom cases
		 */
		public static final int ACCESS_RANDOM = AssetManager.ACCESS_STREAMING;
		
		/**
		 * Get an input stream on the resource identified by its path
		 * 
		 * @param resourceType The type of resource (ex: API.GEOMETRY, API.LIGHT ...)
		 * @param resourcePath The resource path
		 * @param readMode The read mode based on ACCESS_BUFFER, ACCESS_STREAMING or ACCESS_RANDOM
		 * @return An input stream opened on the resource
		 */
		public InputStream getResourceInputStream(final int resourceType, final String resourcePath, final int readMode) throws IOException;
	}
	
	
	/**
	 * Implements this class to receive loading events for 
	 * asynchronous loading 
	 * 
	 * @author Thomas MILLET
	 *
	 */
	public static interface OnBinaryLoaderEventListener{
			
		
		/**
		 * Called on error
		 * 
		 * @param message The error message
		 * @param rootCause The error root cause
		 */
		public void onError(final String message, final Throwable rootCause);
			
		/**
		 * Called when all stream has been parsed
		 * 
		 * @param inputStream The input stream for post-treatments
		 */
		public void onComplete(final InputStream inputStream);
		
		/**
		 * Called when header has been parsed to indicate metadata (mainly for loading screen)
		 * 
		 * @param geometryCount The number of geometry elements
		 * @param lightCount The number of light elements
		 * @param cameraCount The number of camera elements
		 * @param imageryCount The number of imagery elements
		 * @param materialCount The number of material elements
		 * @param nodeCount The number of node elements
		 * @param sceneCount The number of scene elements
		 */
		public void onHeader(final int geometryCount, final int lightCount, final int cameraCount, final int imageryCount, final int materialCount, final int nodeCount, final int sceneCount);
		
		/**
		 * Called when a geometry entry has been loaded
		 * 
		 * @param geometryId The geometry id
		 */
		public void onGeometryLoaded(final int geometryId);
		
		/**
		 * Called when a light entry has been loaded
		 * 
		 * @param lightId The light id
		 */
		public void onLightLoaded(final int lightId);
		
		/**
		 * Called when a camera entry has been loaded
		 * 
		 * @param cameraId The camera id
		 */
		public void onCameraLoaded(final int cameraId);
		
		/**
		 * Called when a image entry has been loaded
		 * 
		 * @param imageId The image id
		 */
		public void onImageLoaded(final int imageId);
		
		/**
		 * Called when a material entry has been loaded
		 * 
		 * @param materialId The camera id
		 */
		public void onMaterialLoaded(final int materialId);
		
		/**
		 * Called when a node entry has been loaded
		 * 
		 * @param nodeId The camera id
		 */
		public void onNodeLoaded(final int nodeId);
		
		/**
		 * Called when a scene entry has been loaded
		 * 
		 * @param sceneId The camera id
		 */
		public void onSceneLoaded(final int sceneId);
	}
}
