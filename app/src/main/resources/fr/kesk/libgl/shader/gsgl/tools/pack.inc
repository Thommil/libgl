/**
* 	Set of functions to pack/unpack colors to float 
* 	
*	@author Thomas MILLET
**/

//Constants
const float C_ONE_FLOAT = 1.0;
const float C_ZERO_FLOAT = 0.0;
const float C_255_FLOAT = 255.0;
const float C_256_FLOAT = 256.0;
const float C_65025_FLOAT = 65025.0;
const float C_65536_FLOAT = 65536.0;
const float C_65793_FLOAT = 65793.0;
const float C_16581375_FLOAT = 16581375.0;
const float C_16777215_FLOAT = 16777215.0;

//24 Bits COLOR

/**
*	Convert FLOAT to VEC3
*/
vec3 FloatToVec3(float unit){
    const vec3 scale = vec3(C_ONE_FLOAT, C_256_FLOAT, C_65536_FLOAT);
    const float normal = C_256_FLOAT / C_255_FLOAT;
    vec3 color = unit * scale;
    color = fract(color);
    color *= normal;
    color.rg -= color.gb / C_256_FLOAT;
    return color;
}
 
/**
*	Convert VEC3 to FLOAT
*/
float Vec3ToFloat(vec3 color) {
    const vec3 scale = vec3(C_65536_FLOAT, C_256_FLOAT, C_ONE_FLOAT) / C_65793_FLOAT;
    return dot(color, scale);
}

//32 Bits COLOR

/**
*	Convert FLOAT to VEC4
*/
vec4 FloatToVec4(float unit) {
    const vec4 factor = vec4(C_ONE_FLOAT, C_255_FLOAT, C_65025_FLOAT, C_16581375_FLOAT);
    const float mask = C_ONE_FLOAT / C_256_FLOAT;
    vec4 color = unit * factor;
    color.gba = fract(color.gba);
    color.rgb -= color.gba * mask;
    return color;
}
 
/**
*	Convert VEC4 to FLOAT
*/ 
float Vec4ToFloat(vec4 color) {
    const vec4 factor = C_ONE_FLOAT / vec4(C_ONE_FLOAT, C_255_FLOAT, C_65025_FLOAT, C_16581375_FLOAT);
    return dot(color, factor);
}