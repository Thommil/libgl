/**
* 	Specular point lighting helper shader in PerPixel 
* 	
*	@author Thomas MILLET
**/

precision mediump float;
 
//Material
uniform vec4 u_MaterialSpecularVec4;
uniform float u_MaterialShininessFloat;
uniform sampler2D u_TextureIndexSampler2D;

//Light
uniform vec4 u_LightColorVec4;
uniform vec3 u_LightPositionVec3;
uniform vec3 u_LightAttenuationVec3;
		  
//Varying	
varying vec3 v_PositionVec3;	         		          		
varying vec3 v_NormalVec3;  
varying vec2 v_TexCoordinateVec2;
varying vec4 v_BaseAmbientColorVec4;  

//Const
const float C_ZERO_FLOAT = 0.0;

void main()                    		
{        
	vec3 l_LightRawVectorVec3 = u_LightPositionVec3 - v_PositionVec3;
	vec3 l_LightVectorVec3 = normalize(l_LightRawVectorVec3);
	float l_DistanceFloat = length(l_LightRawVectorVec3);
	float l_DiffuseFloat = u_LightAttenuationVec3.x + (l_DistanceFloat * u_LightAttenuationVec3.y) + (l_DistanceFloat * l_DistanceFloat * u_LightAttenuationVec3.z); 
	float l_DotNormalLightFloat = dot(v_NormalVec3, l_LightVectorVec3);
	vec3 l_EyeVec3 = normalize(-v_PositionVec3);
	vec3 l_HVec3 = normalize(l_LightVectorVec3 + l_EyeVec3);
	float l_SpecularTermFloat = max(dot(l_HVec3, v_NormalVec3), C_ZERO_FLOAT);
	vec4 l_SpecularBaseColorVec4 = u_MaterialSpecularVec4 * u_LightColorVec4 * pow(l_SpecularTermFloat, u_MaterialShininessFloat);

	gl_FragColor = texture2D(u_TextureIndexSampler2D, v_TexCoordinateVec2) * (v_BaseAmbientColorVec4 + (l_DiffuseFloat * (u_LightColorVec4 * max(l_DotNormalLightFloat, C_ZERO_FLOAT))) + l_SpecularBaseColorVec4);

}
	