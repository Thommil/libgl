package fr.kesk.libgl;

import java.util.ArrayList;
import java.util.List;

import fr.kesk.libgl.buffer.Chunk;
import fr.kesk.libgl.buffer.GlBuffer;
import fr.kesk.libgl.loader.API;
import fr.kesk.libgl.texture.GlTextureSet;
import fr.kesk.libgl.tools.MatrixUtils;

import android.opengl.GLES20;
import android.util.SparseArray;

/**
 * Class representation of assets loaded using LGL loaders in
 * synchronous mode.
 * 
 * @author Thomas MILLET
 */
public class GlAssets {

	/**
	 * TAG log
	 */
	@SuppressWarnings("unused")
	private final static String TAG = GlAssets.class.getName();
	
	/**
	 * Contains the list of geometries by their IDs
	 */
	public final SparseArray<GlAssets.Geometry> geometries;
	
	/**
	 * Contains the list of lights by their IDs
	 */
	public final SparseArray<GlAssets.Light> lights;
	
	/**
	 * Contains the list of cameras by their IDs
	 */
	public final SparseArray<GlAssets.Camera> cameras;
	
	/**
	 * Contains the list of textures by their IDs
	 */
	public final GlTextureSet textures;
	
	/**
	 * Contains the list of materials by their IDs
	 */
	public final SparseArray<GlAssets.Material> materials;
	
	/**
	 * Contains the list of nodes by their IDs
	 */
	public final SparseArray<GlAssets.Node> nodes;
	
	/**
	 * Contains the list of scenes by their IDs
	 */
	public final SparseArray<GlAssets.Scene> scenes;
	
	/**
	 * @param id
	 */
	public GlAssets() {
		//android.util.Log.d(TAG,"NEW - "+id);
		this.geometries = new SparseArray<GlAssets.Geometry>();
		this.lights = new SparseArray<GlAssets.Light>();
		this.cameras = new SparseArray<GlAssets.Camera>();
		this.textures = new GlTextureSet();
		this.materials = new SparseArray<GlAssets.Material>();
		this.nodes = new SparseArray<GlAssets.Node>();
		this.scenes = new SparseArray<GlAssets.Scene>();
	}
	
	/**
	 *	Free all GPU/CPU resources associated with these assets
	 */
	public void free(){
		//android.util.Log.d(TAG,"free()");
		this.freeGeometries();
		this.freeLights();
		this.freeCameras();
		this.freeTextures();
		this.freeMaterials();
	}
	
	/**
	 *	Free all GPU/CPU resources associated with current geometries
	 */
	public void freeGeometries(){
		final int size = this.geometries.size();
		for(int index=0; index < size; index++){
			final int key = this.geometries.keyAt(index);
			this.geometries.get(key).free();
			this.geometries.delete(key);
		}
	}
	
	/**
	 *	Free GPU/CPU resources associated with specified geometry
	 *
	 *	@param id The geometry to free ID
	 */
	public void freeGeometry(final int id){
		//android.util.Log.d(TAG,"freeGeometry("+id+")");
		this.geometries.get(id).free();
	}
	
	/**
	 *	Free all GPU/CPU resources associated with current lights
	 */
	public void freeLights(){
		//android.util.Log.d(TAG,"freeLights()");
		final int size = this.lights.size();
		for(int index=0; index < size; index++){
			this.lights.delete(this.lights.keyAt(index));
		}
	}
	
	/**
	 *	Free GPU/CPU resources associated with specified light
	 *
	 *	@param id The light to free ID
	 */
	public void freeLight(final int id){
		//android.util.Log.d(TAG,"freeLight("+id+")");
		this.lights.delete(id);
	}
	
	/**
	 *	Free all GPU/CPU resources associated with current cameras
	 */
	public void freeCameras(){
		//android.util.Log.d(TAG,"freeCameras()");
		final int size = this.cameras.size();
		for(int index=0; index < size; index++){
			this.cameras.delete(this.cameras.keyAt(index));
		}
	}
	
	/**
	 *	Free GPU/CPU resources associated with specified camera
	 *
	 *	@param id The camera to free ID
	 */
	public void freeCamera(final int id){
		//android.util.Log.d(TAG,"freeCamera("+id+")");
		this.cameras.delete(id);
	}
	
	/**
	 *	Free all GPU/CPU resources associated with current textures
	 */
	public void freeTextures(){
		//android.util.Log.d(TAG,"freeTextures()");
		this.textures.free();
	}
	
	/**
	 *	Free GPU/CPU resources associated with specified texture
	 *
	 *	@param id The texture to free ID
	 */
	public void freeTexture(final int id){
		//android.util.Log.d(TAG,"freeTexture("+id+")");
		this.textures.remove(id);
	}
	
	/**
	 *	Free all GPU/CPU resources associated with current materials
	 */
	public void freeMaterials(){
		//android.util.Log.d(TAG,"freeMaterials()");
		final int size = this.materials.size();
		for(int index=0; index < size; index++){
			this.materials.delete(this.materials.keyAt(index));
		}
	}
	
	/**
	 *	Free GPU/CPU resources associated with specified materials
	 *
	 *	@param id The material to free ID
	 */
	public void freeMaterial(final int id){
		//android.util.Log.d(TAG,"freeMaterials("+id+")");
		this.materials.delete(id);
	}
	
	/**
	 *	Free all GPU/CPU resources associated with current nodes
	 */
	public void freeNodes(){
		//android.util.Log.d(TAG,"freeNodes()");
		final int size = this.nodes.size();
		for(int index=0; index < size; index++){
			this.nodes.delete(this.nodes.keyAt(index));
		}
	}
	
	/**
	 *	Free GPU/CPU resources associated with specified nodes
	 *
	 *	@param id The node to free ID
	 */
	public void freeNode(final int id){
		//android.util.Log.d(TAG,"freeNodes("+id+")");
		this.nodes.delete(id);
	}
	
	/**
	 *	Free all GPU/CPU resources associated with current scenes
	 */
	public void freeScenes(){
		//android.util.Log.d(TAG,"freeScenes()");
		final int size = this.scenes.size();
		for(int index=0; index < size; index++){
			this.scenes.delete(this.scenes.keyAt(index));
		}
	}
	
	/**
	 *	Free GPU/CPU resources associated with specified scenes
	 *
	 *	@param id The scene to free ID
	 */
	public void freeScene(final int id){
		//android.util.Log.d(TAG,"freeScene("+id+")");
		this.scenes.delete(id);
	}

	/**
	 * 
	 * Geometry abstraction class
	 * 
	 * @author Thomas MILLET
	 *
	 */
	public final static class Geometry{
	
		/**
		 * The geometry ID
		 */
		public final int id;
		
		/**
		 * The list of elements
		 */
		public final List<GlAssets.Geometry.Element> elements;
		
		/**
		 * Default constructor
		 * 
		 * @param id The geometry ID
		 */
		public Geometry(final int id){
			//android.util.Log.d(TAG+".Geometry","NEW - "+id);
			this.id = id;
			this.elements = new ArrayList<GlAssets.Geometry.Element>();
		}
		
		
		/**
		 * Abstraction class for an element of a Geometry
		 * 
		 * @author Thomas MILLET
		 *
		 */
		public final static class Element{
			
			/**
			 * Index of SEMANTIC in input settings
			 */
			public static final int SEMANTIC = 0;
			
			/**
			 * Index of SET in input settings
			 */
			public static final int SET = 1;
			
			/**
			 * Index of SIZE in input settings
			 */
			public static final int SIZE = 2;
			
			/**
			 * Index of OFFSET in input settings
			 */
			public static final int OFFSET = 3;
			
			/**
			 * Types of elements
			 */
			
			/**
			 * Type for LINES geometry
			 */
			public static final int LINES = API.LINES;
			
			/**
			 * Type for LINESTRIPS geometry
			 */
			public static final int LINESTRIPS = API.LINESTRIPS;
			
			/**
			 * Type for POLYGONS geometry
			 */
			public static final int POLYGONS = API.POLYGONS;
			
			/**
			 * Type for POLYLIST geometry
			 */
			public static final int POLYLIST = API.POLYLIST;
			
			/**
			 * Type for TRIANGLES geometry
			 */
			public static final int TRIANGLES = API.TRIANGLES;
			
			/**
			 * Type for TRIFANS geometry
			 */
			public static final int TRIFANS = API.TRIFANS;
			
			/**
			 * Type for TRISTRIPS geometry
			 */
			public static final int TRISTRIPS = API.TRISTRIPS;
			
			/**
			 * Types of input semantic
			 */
			
			/**
			 * Geometric binormal (bitangent) vector
			 */
			public static final int BINORMAL = API.BINORMAL;
			
			/**
			 * Color coordinate vector
			 */
			public static final int COLOR = API.COLOR;
			
			/**
			 * Continuity constraint at the control vertex (CV)
			 */
			public static final int CONTINUITY = API.CONTINUITY;
			
			/**
			 * Raster or MIP-level input
			 */
			public static final int IMAGE = API.IMAGE;
			
			/**
			 * Sampler input
			 */
			public static final int INPUT = API.INPUT;
			
			/**
			 * Tangent vector for preceding control point
			 */
			public static final int IN_TANGENT = API.IN_TANGENT;
			
			/**
			 * Sampler interpolation type
			 */
			public static final int INTERPOLATION = API.INTERPOLATION;
			
			/**
			 * Inverse of local-to-world matrix
			 */
			public static final int INV_BIND_MATRIX = API.INV_BIND_MATRIX;
			
			/**
			 * Skin influence identifier
			 */
			public static final int JOINT = API.JOINT;
			
			/**
			 * Number of piece-wise linear approximation steps to use
			 * for the spline segment that follows this CV
			 */
			public static final int LINEAR_STEPS = API.LINEAR_STEPS;
			
			/**
			 * Morph targets for mesh morphing
			 */
			public static final int MORPH_TARGET = API.MORPH_TARGET;
			
			/**
			 * Weights for mesh morphing
			 */
			public static final int MORPH_WEIGHT = API.MORPH_WEIGHT;
			
			/**
			 * Normal vector
			 */
			public static final int NORMAL = API.NORMAL;
			
			/**
			 * Sampler output
			 */
			public static final int OUTPUT = API.OUTPUT;
			
			/**
			 * Tangent vector for succeeding control point
			 */
			public static final int OUT_TANGENT = API.OUT_TANGENT;
			
			/**
			 * Geometric coordinate vector
			 */
			public static final int POSITION = API.POSITION;
			
			/**
			 * Geometric tangent vector
			 */
			public static final int TANGENT = API.TANGENT;
			
			/**
			 * Texture binormal (bitangent) vector
			 */
			public static final int TEXBINORMAL = API.TEXBINORMAL;
			
			/**
			 * Texture coordinate vector
			 */
			public static final int TEXCOORD = API.TEXCOORD;
			
			/**
			 * Texture tangent vector
			 */
			public static final int TEXTANGENT = API.TEXTANGENT;
			
			/**
			 * Generic parameter vector
			 */
			public static final int UV = API.UV;
			
			/**
			 * Mesh vertex
			 */
			public static final int VERTEX = API.VERTEX;
			
			/**
			 * Skin influence weighting value
			 */
			public static final int WEIGHT = API.WEIGHT;
			
			/**
			 * The type of element (LINES, TRIANGLES ...)
			 */
			public final int type;
			
			/**
			 * The number of vertices in element
			 */
			public final int count;
			
			/**
			 * The bound material instance id
			 */
			public final int materialId;
			
			/**
			 * The list of inputs based on Loader API format
			 */
			public final int inputs[][];
			
			/**
			 * The size of data in bytes
			 */
			public final int size;
			
			/**
			 * The stride of data in bytes
			 */
			public int stride = 0;
			
			/**
			 * The values bound to inputs
			 */
			public final float[][]values;
			
			/**
			 * Indicates if current element is only available in VBO 
			 */
			public final boolean vboOnly;

			/**
			 * Store the GlBuffer version if asked once
			 */
			private GlBuffer<float[]> cachedGlBuffer;
			
			/**
			 * The VBO handle if available 
			 */
			public int handle = GlBuffer.UNBIND_HANDLE;
			
			/**
			 * Constructor for local and VBO use
			 * 
			 * @param type The type of element
			 * @param count The number of vertices in element
			 * @param size The size of the element in bytes
			 * @param inputs The list of inputs based on Loader API format
			 * @param values The values bound to inputs
			 */
			public Element(final int type, final int count, final int size, final int materialId, final int[][] inputs, final float[][] values) {
				//android.util.Log.d(TAG+".Geometry.Element","NEW");
				this.type = type;
				this.count = count;
				this.size = size;
				this.materialId = materialId;
				this.inputs = inputs;
				this.values = values;
				this.vboOnly = false;
				for(int[]input : inputs){
					this.stride += input[SIZE];
				}
				this.stride *= GlBuffer.SIZEOF_JAVA_FLOAT;
			}
			
			/**
			 * Constructor for VBO use only
			 * 
			 * @param type The type of element
			 * @param count The number of vertices in element
			 * @param size The size of the element in bytes
			 * @param inputs The list of inputs based on Loader API format
			 * @param handles The VBO handles  
			 */
			public Element(final int type, final int count, final int size, final int materialId, final int[][] inputs, final int handle) {
				//android.util.Log.d(TAG+".Geometry.Element","NEW");
				this.type = type;
				this.count = count;
				this.size = size;
				this.materialId = materialId;
				this.inputs = inputs;
				this.values = null;
				this.vboOnly = true;
				this.handle = handle;
				for(int[]input : inputs){
					this.stride += input[SIZE];
				}
				this.stride *= GlBuffer.SIZEOF_JAVA_FLOAT;
			}
			
			/**
			 * Convert this class to a GlBuffer 
			 * 
			 * @param useVBO Indicates to binds buffers to VBO (USAGE_DYNAMIC_DRAW mode only)
			 * 
			 * @return A GlBuffer based on instance parameters and values
			 */
			public GlBuffer<float[]> toGlBuffer(){
				//android.util.Log.d(TAG+".Geometry.Element","toGlBuffer("+useVBO+")");
				if(this.cachedGlBuffer == null){
					if(this.vboOnly) return null;
					
					@SuppressWarnings("unchecked")
					final Chunk<float[]>[] chunks = new Chunk[this.inputs.length];
					for(int iIndex=0; iIndex < this.inputs.length; iIndex++){
						chunks[iIndex] = new Chunk<float[]>(this.values[iIndex], this.inputs[iIndex][SIZE]);
					}
					this.cachedGlBuffer = new GlBuffer<float[]>(chunks);
				}
				return this.cachedGlBuffer;
			}
			
			/**
			 * Helper method to free GPU resources bound to this element 
			 */
			public void free(){
				if(this.vboOnly){
					if(this.handle != GlBuffer.UNBIND_HANDLE){
						final int[] handles = new int[]{this.handle};
						this.handle = GlBuffer.UNBIND_HANDLE;
						GLES20.glDeleteBuffers(1, handles, 0);
					}
				}
				else if(this.cachedGlBuffer != null){
					this.cachedGlBuffer.free();
				}
			}
		}
		
		/**
		 * Helper method to free GPU resources bound to this geometry 
		 */
		public void free(){
			for(Element element : this.elements){
				element.free();
			}
			this.elements.clear();
		}
		
	}
	
	/**
	 * 
	 * Light abstraction class
	 * 
	 * @author Thomas MILLET
	 *
	 */
	public final static class Light{
	
		/**
		 * Index of KC in settings
		 */
		public static final int KC = 0;
		
		/**
		 * Index of KL in settings
		 */
		public static final int KL = 1;
		
		/**
		 * Index of KQ in settings
		 */
		public static final int KQ = 2;
		
		/**
		 * Index of FALLOFF_ANGLE in settings (spot only)
		 */
		public static final int FALLOFF_ANGLE = 3;
		
		/**
		 * Index of FALLOFF_EXP in settings (spot only)
		 */
		public static final int FALLOFF_EXP = 4;
		
		/**
		 * Ambient light source
		 */
		public static final int AMBIENT = API.AMBIENT;
		
		/**
		 * Directional light source
		 */
		public static final int DIRECTIONAL = API.DIRECTIONAL;
		
		/**
		 * Point light source
		 */
		public static final int POINT = API.POINT;
		
		/**
		 * Spot light source
		 */
		public static final int SPOT = API.SPOT;
		
		/**
		 * The light ID
		 */
		public final int id;
		
		/**
		 * The light type
		 */
		public final int type;
		
		/**
		 * The light color (vec4)
		 */
		public final float[] color;
		
		/**
		 * The light settings see {@link API}
		 */
		public final float[] settings;
		
		/**
		 * @param id The light ID
		 * @param type The light type
		 * @param color The light color
		 * @param settings The light settings
		 */
		public Light(int id, int type, float[] color, float[] settings) {
			super();
			this.id = id;
			this.type = type;
			this.color = color;
			this.settings = settings;
		}
	}
		
	/**
	 * 
	 * Camera abstraction class
	 * 
	 * @author Thomas MILLET
	 *
	 */
	public final static class Camera{
	
		/**
		 * Index of XFOV in settings (perspective)
		 */
		public static final int XFOV = 0;
		
		/**
		 * Index of XMAG in settings (orthographic)
		 */
		public static final int XMAG = 0;
		
		/**
		 * Index of YFOV in settings (perspective)
		 */
		public static final int YFOV = 1;
		
		/**
		 * Index of YMAG in settings (orthographic)
		 */
		public static final int YMAG = 1;
		
		/**
		 * Index of ASPECT_RATIO in settings
		 */
		public static final int ASPECT_RATIO = 2;
		
		/**
		 * Index of ASPECT_RATIO in settings
		 */
		public static final int ZNEAR = 3;
		
		/**
		 * Index of ZFAR in settings
		 */
		public static final int ZFAR = 4;
		
		/**
		 * Perspective camera
		 */
		public static final int PERSPECTIVE = API.PERSPECTIVE;
		
		/**
		 * Orthographic camera
		 */
		public static final int ORTHOGRAPHIC = API.ORTHOGRAPHIC;
		
		/**
		 * The light ID
		 */
		public final int id;
		
		/**
		 * The light type
		 */
		public final int type;
		
		/**
		 * The camera settings, see {@link API}
		 */
		public final float[] settings;
				
		/**
		 * The camera up vectors (vec3)
		 */
		public final float[] up;
		

		/**
		 * @param id The light ID
		 * @param type The light type
		 * @param color The light color
		 * @param settings The light settings
		 */
		public Camera(int id, int type, float[] settings) {
			super();
			this.id = id;
			this.type = type;
			this.settings = settings;
			this.up = new float[]{0f,0f,1f};
		}
	}
		
	/**
	 * 
	 * Material abstraction class
	 * 
	 * @author Thomas MILLET
	 *
	 */
	public final static class Material{
		
		/**
		 * Index in bindings to indicate HANDLE
		 */
		public static final int HANDLE = 0;
		
		/**
		 * Index in bindings to indicate SEMANTIC
		 */
		public static final int SEMANTIC = 1;
		
		/**
		 * Constant material (ambient)
		 */
		public static final int CONSTANT = API.CONSTANT;
		
		/**
		 * Diffuse material and parameter
		 */
		public static final int DIFFUSE = API.DIFFUSE;
		
		/**
		 * Specular material and parameter
		 */
		public static final int SPECULAR = API.SPECULAR;
		
		/**
		 * The material ID
		 */
		public final int id;
		
		/**
		 * The material type
		 */
		public final int type;
		
		/**
		 * Default constructor
		 * 
		 * @param id The material ID
		 * @param type The material type
		 */
		public Material(final int id, final int type){
			super();
			this.id = id;
			this.type = type;
		}
		
		
		/**
		*	Indicates a not set value
		**/
		public final static int NOT_SET = API.UNSPECIFIED;
		
		/**
		 * The emission color in Vec4
		 */
		public float[] emissionColor = null;
		
		/**
		 * The emission bind handle
		 */
		public int[] emissionBinding = null;
		
		/**
		 * The reflective color in Vec4
		 */
		public float[] reflectiveColor = null;
		
		/**
		 * The reflective bind handle
		 */
		public int[] reflectiveBinding = null;
		
		/**
		 * The reflectivity value
		 */
		public float reflectivity = NOT_SET;
		
		/**
		 * The transparent color in Vec4
		 */
		public float[] transparentColor = null;
		
		/**
		 * The transparent bind handle
		 */
		public int[] transparentBinding = null;
		
		/**
		 * The transparency value
		 */
		public float transparency = NOT_SET;
		
		/**
		 * The refraction value
		 */
		public float refraction = NOT_SET;
		
		/**
		 * The ambient color in Vec4
		 */
		public float[] ambientColor = null;
		
		/**
		 * The ambient bind handle
		 */
		public int[] ambientBinding = null;
		
		/**
		 * The diffuse color in Vec4
		 */
		public float[] diffuseColor = null;
		
		/**
		 * The diffuse bind handle
		 */
		public int[] diffuseBinding = null;
		
		/**
		 * The specular color in Vec4
		 */
		public float[] specularColor = null;
		
		/**
		 * The specular bind handle
		 */
		public int[] specularBinding = null;
		
		/**
		 * The shininess value
		 */
		public float shininess = NOT_SET;
	}
	
	/**
	 * 
	 * Node abstraction class
	 * 
	 * @author Thomas MILLET
	 *
	 */
	public final static class Node{
		
		/**
		 * NODE node
		 */
		public static final int NODE = API.NODE_TYPE;
		
		/**
		 * JOINT node
		 */
		public static final int JOINT = API.JOINT_TYPE;
		
		/**
		 * The node ID
		 */
		public final int id;
		
		/**
		 * The node type
		 */
		public final int type;
		
		/**
		 * The associated model matrix
		 */
		public final float[] model = new float[16];
		
		/**
		 * The list of cameras instances
		 */
		public Camera[] cameraInstances = null;
		
		/**
		 * The list of lights instances
		 */
		public Light[] lightInstances = null;
		
		/**
		 * The list of nodes instances
		 */
		public Node[] nodeInstances = null;
		
		/**
		 * The list of geometries
		 */
		public GeometryInstance[] geometryInstances = null;
		
		
		/**
		 * Default constructor
		 * 
		 * @param id The node ID
		 * @param type The node type
		 * @param model The model matrixs
		 */
		public Node(final int id, final int type){
			super();
			this.id = id;
			this.type = type;
			MatrixUtils.setIdentityM(this.model, 0);
		}
		
		/**
		 * Build a new instance of this node and subnodes and prepare it for Matrix computation
		 * 
		 * @return A Node copy instance to be used in scene
		 */
		public Node newInstance(){
			final Node instance = new Node(this.id, this.type);
			System.arraycopy(this.model, 0, instance.model, 0, 16);
			if(this.cameraInstances != null){
				instance.cameraInstances = this.cameraInstances.clone();
			}
			if(this.lightInstances != null){
				instance.lightInstances = this.lightInstances.clone();
			}
			if(this.nodeInstances != null){
				instance.nodeInstances = new Node[this.nodeInstances.length];
				for(int nodeIndex=0; nodeIndex < this.nodeInstances.length; nodeIndex++){
					instance.nodeInstances[nodeIndex] = this.nodeInstances[nodeIndex].newInstance();
				}
			}
			if(this.geometryInstances != null){
				instance.geometryInstances = this.geometryInstances.clone();
			}
			
			return instance;
		}
		
		/**
		 * Geometry instance abstraction class
		 * 
		 * @author Thomas MILLET
		 *
		 */
		public final static class GeometryInstance{
			
			/**
			 * The geometry
			 */
			public final Geometry geometry;
			
			/**
			 * The material instances
			 */
			public MaterialInstance[] materials;
			
			/**
			 * Default constructor
			 * 
			 * @param geometry The geometry
			 * @param materials The list of materials instances
			 */
			public GeometryInstance(final Geometry geometry, final MaterialInstance[] materials){
				super();
				this.geometry = geometry;
				this.materials = materials;
			}
			
			/**
			 * Material instance abstraction class
			 * 
			 * @author Thomas MILLET
			 */
			public final static class MaterialInstance{
				
				/**
				 * Index of INPUT_SEMANTIC in bindings 
				 */
				public final static int INPUT_SEMANTIC = 0;
				
				/**
				 * Index of INPUT_SET in bindings 
				 */
				public final static int INPUT_SET = 1;
				
				/**
				 * Index of MATERIAL_SEMANTIC in bindings 
				 */
				public final static int MATERIAL_SEMANTIC = 2;
				
				/**
				 * The material instance ID/Symbol
				 */
				public final int id;
				
				/**
				 * The bound material
				 */
				public final Material target;
				
				/**
				 * The list of material instance inputs bindings
				 */
				public final int[][]bindings;
				
				/**
				 * Default constructor
				 * 
				 * @param id The material instance ID/Symbol
				 * @param target The target material
				 */
				public MaterialInstance(final int id, final Material target, final int[][]bindings){
					super();
					this.id = id;
					this.target = target;
					this.bindings = bindings;
				}
			}
		}
		
	}
	
	/**
	 * 
	 * Scene abstraction class
	 * 
	 * @author Thomas MILLET
	 *
	 */
	public final static class Scene{

		/**
		 * The scene ID
		 */
		public final int id;
		
		/**
		 * The list of nodes instances ID in the scene
		 */
		public final Node[] nodeInstances;
		
		/**
		 * Default constructor
		 * 
		 * @param id The scene ID
		 * @param nodeInstances The list of nodes instances in the scene
		 */
		public Scene(final int id, final Node[] nodeInstances){
			super();
			this.id = id;
			this.nodeInstances = nodeInstances;
		}
	}
}
