/**
* 	Diffuse directional lighting helper shader in PerVertex 
* 	
*	@author Thomas MILLET
**/

precision mediump float;

//Context
uniform mat4 u_mvpMatrixMat4;      		       
uniform mat4 u_mvMatrixMat4;

//Material
uniform vec4 u_MaterialAmbientVec4;

//Light
uniform vec4 u_LightAmbientColorVec4;
uniform vec3 u_LightDirectionVec3;
uniform vec4 u_LightColorVec4;

//Attributes
attribute vec4 a_PositionVec4;   				 				
attribute vec3 a_NormalVec3;   
attribute vec2 a_TexCoordinateVec2;    		
		  
//Varying		  
varying vec4 v_DiffuseBaseColorVec4; 
varying vec4 v_BaseColorVec4;
varying vec2 v_TexCoordinateVec2;

//Const
const float C_ZERO_FLOAT = 0.0;

void main()
{
	vec3 l_NormalVec3 = normalize(vec3(u_mvMatrixMat4 * vec4(a_NormalVec3, C_ZERO_FLOAT)));
	float l_DotNormalLightFloat = dot(l_NormalVec3, normalize(-u_LightDirectionVec3));
	
	v_BaseColorVec4 = u_MaterialAmbientVec4 * u_LightAmbientColorVec4;
	v_DiffuseBaseColorVec4 = u_LightColorVec4 * max(l_DotNormalLightFloat, C_ZERO_FLOAT);
	v_TexCoordinateVec2 = a_TexCoordinateVec2;
	gl_Position = u_mvpMatrixMat4 * a_PositionVec4;                       		  
}