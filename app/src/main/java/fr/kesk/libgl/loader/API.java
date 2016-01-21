package fr.kesk.libgl.loader;

/**
 * Interface to be shared between minifier and loader.<br/>
 * 
 * @author Thomas MILLET
 *
 */
public interface API {
	
	/**
	 * Version
	 */
	public static final float VERSION = 1.0f;
	
	/************************************************************************************************
	 * COMMON		
	 ************************************************************************************************
	
	/**
	 * Indicates a null or empty entry
	 */
	public static final int EMPTY = 0x00000000;
	
	/**
	 * Indicates a false value
	 */
	public static final int FALSE = 0x00000000;
	
	/**
	 * Default value for unspecified entry
	 */
	public static final int UNSPECIFIED = 0xffffffff;
	
	/**
	 * Default value for error entry
	 */
	public static final int ERROR = 0xffffffff;
	
	/**
	 *  VAR_TYPE
	 */
	
	/**
	 * Indicates a type FLOAT
	 */
	public static final int FLOAT = 0x0000000a;
	
	/**
	 * Indicates a type SHORT
	 */
	public static final int SHORT = 0x0000000b;
	
	/**
	 * Indicates a type BOOLEAN
	 */
	public static final int BOOLEAN = 0x0000000c;
	
	/**
	 * Indicates a type INTEGER
	 */
	public static final int INTEGER = 0x0000000d;
	
	/**
	 * Indicates a type STRING
	 */
	public static final int STRING = 0x0000000e;
		
	/**
	 * Parameter of type float[2]
	 */
	public static final int FLOAT2 = 0x0000000f;
	
	/**
	 * Parameter of type float[3]
	 */
	public static final int FLOAT3 = 0x00000010;
	
	/**
	 * Parameter of type float[4]
	 */
	public static final int FLOAT4 = 0x00000011;
	
	/**
	 * Parameter of type surface
	 */
	public static final int SURFACE = 0x00000012;
	
	/**
	 * Parameter of type sampler
	 */
	public static final int SAMPLER = 0x00000013;
	
	/**
	 *  ATTRIBUTES
	 */
	
	/**
	 * ID attribute
	 */
	public static final int ID = 0x0000001a;
	
	/**
	 * TYPE attribute
	 */
	public static final int TYPE = 0x0000001b;
		
	
	/************************************************************************************************
	 * HEADER - Header	
	 ************************************************************************************************
	 
	  [HEADER][$VERSION]
	  [$GEOMETRY_COUNT][$LIGHT_COUNT][$CAMERA_COUNT][$IMAGERY_COUNT][$MATERIAL_COUNT][$SCENE_COUNT]
	  
	 ************************************************************************************************/
	public static final int HEADER = 0x00000032;
	
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
	public static final int GEOMETRY = 0x00000064;
	
	/************************************************************************************************
	 * LIGHT	- Describes the light sources in the scene	
	 ************************************************************************************************
	 
	  [LIGHT][$ID][$TYPE]
		  case AMBIENT : [$COLOR_RGB]
		  case DIRECTIONAL : [$COLOR_RGB]
		  case POINT : [$COLOR_RGB][$KC][$KL][$KQ]
		  case SPOT : [$COLOR_RGB][$KC][$KL][$KQ][$FALLOFF_ANGLE][$FALLOFF_EXP]
	  ...
	  [LIGHT][$ID][$TYPE]
	  
	 ************************************************************************************************/
	public static final int LIGHT = 0x000000C8;
	
	/************************************************************************************************
	 * CAMERA	- Describes the optics in the scene	
	 ************************************************************************************************
	 
	  [CAMERA][$ID][$CAMERA_TYPE]
		  case PERSPECTIVE : [$XFOV][$YFOV][$ASPECT_RATIO][$ZNEAR][$ZFAR][$UP_X][$UP_Y][$UP_Z]
		  case ORTHOGRAPHIC : [$XMAG][$YMAG][$ASPECT_RATIO][$ZNEAR][$ZFAR][$UP_X][$UP_Y][$UP_Z]
	  ...
	  [CAMERA][$ID]
	  
	 ************************************************************************************************/
	public static final int CAMERA = 0x0000012C;
	
	/************************************************************************************************
	 * IMAGERY	- Describes the images in the scene	
	 ************************************************************************************************
	 
	  [IMAGE][$ID][$FORMAT][$INTERNAL_FORMAT][$TYPE][$WIDTH][$HEIGHT][$COMPRESS][$MAG][$MIN][$PATH]
	  ...
	  [IMAGE][$ID][$FORMAT][$INTERNAL_FORMAT][$TYPE][$WIDTH][$HEIGHT][$COMPRESS][$MAG][$MIN][$PATH]
	  
	 ************************************************************************************************/
	public static final int IMAGERY = 0x00000190;
	
	
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
	public static final int MATERIAL = 0x000001f4;
	
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
	public static final int NODE = 0x00000258;
	
	/************************************************************************************************
	 * SCENE - Describes the scene elements	
	 ************************************************************************************************
	  
	  [SCENE][$ID][$NODE_COUNT]
	  [NODE_ID]
	  ...		
	  [SCENE][$ID][$NODE_COUNT]
	 ************************************************************************************************/
	public static final int SCENE = 0x000002bc;
	
	/**
	 *  PRIMITIVE_TYPE
	 */
	
	/**
	 * Provides the information needed for a mesh to bind vertex attributes
	 * together and then organize those vertices into individual lines.
	 */
	public static final int LINES = 0x00000001;
	
	/**
	 * Provides the information needed to bind vertex attributes together 
	 * and then organize those vertices into connected line-strips.
	 */
	public static final int LINESTRIPS = 0x00000003;
	
	/**
	 * Provides the information needed for a mesh to bind vertex attributes
	 * together and then organize those vertices into individual polygons.
	 */
	public static final int POLYGONS = 0x00000002;
	
	/**
	 * Provides the information needed for a mesh to bind vertex attributes
	 * together and then organize those vertices into individual polygons.
	 */
	public static final int POLYLIST = 0x00000004;
	
	/**
	 * Provides the information needed to for a mesh to bind vertex attributes
	 * together and then organize those vertices into individual triangles.
	 */
	public static final int TRIANGLES = 0x00000004;
	
	/**
	 * Provides the information needed for a mesh to bind vertex attributes
	 * together and then organize those vertices into connected triangles.
	 */
	public static final int TRIFANS = 0x00000006;
	
	/**
	 * Provides the information needed for a mesh to bind vertex attributes
	 * together and then organize those vertices into connected triangles.
	 */
	public static final int TRISTRIPS = 0x00000005;
	
	/**
	 *  INPUT SEMANTIC
	 */
	
	/**
	 * Geometric binormal (bitangent) vector
	 */
	public static final int BINORMAL = 0x000004B0;
	
	/**
	 * Color coordinate vector
	 */
	public static final int COLOR = 0x000004B1;
	
	/**
	 * Continuity constraint at the control vertex (CV)
	 */
	public static final int CONTINUITY = 0x000004B2;
	
	/**
	 * Raster or MIP-level input
	 */
	public static final int IMAGE = 0x000004B3;
	
	/**
	 * Sampler input
	 */
	public static final int INPUT = 0x000004B4;
	
	/**
	 * Tangent vector for preceding control point
	 */
	public static final int IN_TANGENT = 0x000004B5;
	
	/**
	 * Sampler interpolation type
	 */
	public static final int INTERPOLATION = 0x000004B6;
	
	/**
	 * Inverse of local-to-world matrix
	 */
	public static final int INV_BIND_MATRIX = 0x000004B7;
	
	/**
	 * Skin influence identifier
	 */
	public static final int JOINT = 0x000004B8;
	
	/**
	 * Number of piece-wise linear approximation steps to use
	 * for the spline segment that follows this CV
	 */
	public static final int LINEAR_STEPS = 0x000004B9;
	
	/**
	 * Morph targets for mesh morphing
	 */
	public static final int MORPH_TARGET = 0x000004BA;
	
	/**
	 * Weights for mesh morphing
	 */
	public static final int MORPH_WEIGHT = 0x000004BB;
	
	/**
	 * Normal vector
	 */
	public static final int NORMAL = 0x000004BC;
	
	/**
	 * Sampler output
	 */
	public static final int OUTPUT = 0x000004BD;
	
	/**
	 * Tangent vector for succeeding control point
	 */
	public static final int OUT_TANGENT = 0x000004BE;
	
	/**
	 * Geometric coordinate vector
	 */
	public static final int POSITION = 0x000004BF;
	
	/**
	 * Geometric tangent vector
	 */
	public static final int TANGENT = 0x000004C0;
	
	/**
	 * Texture binormal (bitangent) vector
	 */
	public static final int TEXBINORMAL = 0x000004C1;
	
	/**
	 * Texture coordinate vector
	 */
	public static final int TEXCOORD = 0x000004C2;
	
	/**
	 * Texture tangent vector
	 */
	public static final int TEXTANGENT = 0x000004C3;
	
	/**
	 * Generic parameter vector
	 */
	public static final int UV = 0x000004C4;
	
	/**
	 * Mesh vertex
	 */
	public static final int VERTEX = 0x000004C5;
	
	/**
	 * Skin influence weighting value
	 */
	public static final int WEIGHT = 0x000004C6;
	
	/**
	 *  LIGHT_TYPE
	 */
	
	/**
	 * Ambient light source
	 */
	public static final int AMBIENT = 0x000007D0;
	
	/**
	 * Directional light source
	 */
	public static final int DIRECTIONAL = 0x000007D1;
	
	/**
	 * Point light source
	 */
	public static final int POINT = 0x000007D2;
	
	/**
	 * Spot light source
	 */
	public static final int SPOT = 0x000007D3;
	
	/**
	 *  MATERIAL_TYPE and parameters
	 */
	
	/**
	 * Constant material (ambient)
	 */
	public static final int CONSTANT = 0x00001388;
	
	/**
	 * Diffuse material and parameter
	 */
	public static final int DIFFUSE = 0x00001389;
	
	/**
	 * Specular material and parameter
	 */
	public static final int SPECULAR = 0x0000138a;
	
		
	/**
	 *  CAMERA_TYPE
	 */
	
	/**
	 * Perspective camera
	 */
	public static final int PERSPECTIVE = 0x00000BB8;
	
	/**
	 * Orthographic camera
	 */
	public static final int ORTHOGRAPHIC = 0x00000BB9;
	
	/**
	 *  IMAGE_FORMAT
	 */
	
	/**
	 * Bitmap image format
	 */
	public static final int BMP = 0x00000FA0;
	
	/**
	 * JPG image format
	 */
	public static final int JPG = 0x00000FA1;
	
	/**
	 * PNG image format
	 */
	public static final int PNG = 0x00000FA2;
	
	/**
	 * GIF image format
	 */
	public static final int GIF = 0x00000FA3;
	
	/**
	 *  IMAGE INTERNAL_FORMAT
	 */
	
	/**
	 * Image internal format based on GL_ALPHA
	 */
	public static final int ALPHA = 0x1906;
	
	/**
	 * Image internal format based on GL_LUMINANCE
	 */
	public static final int LUMINANCE = 0x1909;
	
	/**
	 * Image internal format based on GL_LUMINANCE_ALPHA
	 */
	public static final int LUMINANCE_ALPHA = 0x190a;
	
	/**
	 * Image internal format based on GL_RGB
	 */
	public static final int RGB = 0x1907;
	
	/**
	 * Image internal format based on GL_RGBA
	 */
	public static final int RGBA = 0x1908;
	
	/**
	 *  IMAGE TYPE
	 */
	
	/**
	 * Image internal format based on GL_UNSIGNED_BYTE
	 */
	public static final int UNSIGNED_BYTE = 0x1401;
	
	/**
	 * Image internal format based on GL_UNSIGNED_SHORT_5_6_5
	 */
	public static final int UNSIGNED_SHORT_5_6_5 = 0x8363;
	
	/**
	 * Image internal format based on GL_UNSIGNED_SHORT_4_4_4_4
	 */
	public static final int UNSIGNED_SHORT_4_4_4_4 = 0x8033;
	
	/**
	 * Image internal format based on GL_UNSIGNED_SHORT_5_5_5_1
	 */
	public static final int UNSIGNED_SHORT_5_5_5_1 = 0x8034;
	
	/**
	 * IMAGE MIPMAP 
	 */
	
	/**
	 * NO MIMAP MIN/MAG NEAREST
	 */
	public static final int NEAREST = 0x2600;
	
	/**
	 * NO MIMAP MIN/MAG LINEAR
	 */
	public static final int LINEAR = 0x2601;
	
	/**
	 * MIMAP MIN NEAREST
	 */
	public static final int NEAREST_MIPMAP_NEAREST = 0x2700;
	
	/**
	 * MIMAP MIN NEAREST/LINEAR
	 */
	public static final int NEAREST_MIPMAP_LINEAR = 0x2702;
	
	/**
	 * MIMAP MIN LINEAR/NEAREST
	 */
	public static final int LINEAR_MIPMAP_NEAREST = 0x2701;
	
	/**
	 * MIMAP MIN LINEAR/LINEAR
	 */
	public static final int LINEAR_MIPMAP_LINEAR = 0x2703;
		
	/**
	 * NODE_TYPE
	 */
	
	/**
	 * NODE node
	 */
	public static final int NODE_TYPE = 0x1770;
	
	/**
	 * JOINT node
	 */
	public static final int JOINT_TYPE = 0x1771;
	
	/**
	 * TRANSFORMATIONS
	 */
	
	/**
	 * Lookat tranformation
	 */
	public static final int LOOKAT = 0x1772;
	
	/**
	 * Matrix tranformation
	 */
	public static final int MATRIX = 0x1773;
	
	/**
	 * Rotate tranformation
	 */
	public static final int ROTATE = 0x1774;
	
	/**
	 * Scale tranformation
	 */
	public static final int SCALE = 0x1775;
	
	/**
	 * Skew tranformation
	 */
	public static final int SKEW = 0x1776;
	
	/**
	 * Translate tranformation
	 */
	public static final int TRANSLATE = 0x1777;
		
}
