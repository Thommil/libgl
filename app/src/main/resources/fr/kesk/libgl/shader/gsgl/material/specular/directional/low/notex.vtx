/**
* 	Specular directional lighting helper shader in PerVertex 
* 	
*	@author Thomas MILLET
**/

precision mediump float;

//Context
uniform mat4 u_mvpMatrixMat4;      		       
uniform mat4 u_mvMatrixMat4;

//Material
uniform vec4 u_MaterialAmbientVec4;
uniform vec4 u_MaterialDiffuseVec4;
uniform vec4 u_MaterialSpecularVec4;
uniform float u_MaterialShininessFloat;

//Light
uniform vec4 u_LightAmbientColorVec4;
uniform vec3 u_LightDirectionVec3;
uniform vec4 u_LightColorVec4;

//Attributes
attribute vec4 a_PositionVec4;   				 				
attribute vec3 a_NormalVec3;       		
		  
//Varying		  
varying vec4 v_BaseColorVec4;

//Const
const float C_ZERO_FLOAT = 0.0;

void main()
{
	vec3 l_NormalVec3 = normalize(vec3(u_mvMatrixMat4 * vec4(a_NormalVec3, C_ZERO_FLOAT)));
	vec3 l_LightVectorVec3 = normalize(-u_LightDirectionVec3);
	float l_DotNormalLightFloat = dot(l_NormalVec3, l_LightVectorVec3);
 	vec3 l_EyeVec3 = normalize(-vec3(u_mvMatrixMat4 * a_PositionVec4));
	vec3 l_HVec3 = normalize(l_LightVectorVec3 + l_EyeVec3);
	float l_SpecularTermFloat = max(dot(l_HVec3,l_NormalVec3), C_ZERO_FLOAT);
    
	v_BaseColorVec4 = (u_MaterialAmbientVec4 * u_LightAmbientColorVec4) + (u_MaterialSpecularVec4 * u_LightColorVec4 * pow(l_SpecularTermFloat, u_MaterialShininessFloat)) + (u_MaterialDiffuseVec4 * u_LightColorVec4 * max(l_DotNormalLightFloat, C_ZERO_FLOAT));
	gl_Position = u_mvpMatrixMat4 * a_PositionVec4;                      		  
}