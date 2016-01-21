/**
* 	Specular directional lighting helper shader in PerPixel 
* 	
*	@author Thomas MILLET
**/

precision mediump float;
 		  
//Material
uniform vec4 u_MaterialSpecularVec4;
uniform float u_MaterialShininessFloat;		  
	
//Light
uniform vec4 u_LightColorVec4;	
		  
//Varying		         		          		
varying vec3 v_PositionVec3;
varying vec3 v_NormalVec3;
varying vec4 v_BaseAmbientColorVec4;  
varying vec4 v_BaseColorVec4;
varying vec3 v_LightVectorVec3; 

//Const
const float C_ZERO_FLOAT = 0.0;

void main()                    		
{     
	float l_DotNormalLightFloat = dot(v_NormalVec3, v_LightVectorVec3);
	vec3 l_EyeVec3 = normalize(-v_PositionVec3);
	vec3 l_HVec3 = normalize(v_LightVectorVec3 + l_EyeVec3);
	float l_SpecularTermFloat = max(dot(l_HVec3, v_NormalVec3), C_ZERO_FLOAT);
	vec4 l_SpecularBaseColorVec4 = u_MaterialSpecularVec4 * u_LightColorVec4 * pow(l_SpecularTermFloat, u_MaterialShininessFloat);

	gl_FragColor = v_BaseAmbientColorVec4 + (v_BaseColorVec4 * max(l_DotNormalLightFloat, C_ZERO_FLOAT)) + l_SpecularBaseColorVec4;
}
	