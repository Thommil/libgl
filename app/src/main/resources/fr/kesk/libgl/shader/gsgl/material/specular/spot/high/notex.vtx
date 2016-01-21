/**
* 	Specular spot lighting helper shader in PerPixel 
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

//Light
uniform vec4 u_LightAmbientColorVec4;
uniform vec4 u_LightColorVec4;
uniform vec3 u_LightDirectionVec3;

//Attributes
attribute vec4 a_PositionVec4;   				 				
attribute vec3 a_NormalVec3;       			  
		  
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
	v_PositionVec3 = vec3(u_mvMatrixMat4 * a_PositionVec4);
	v_NormalVec3 = normalize(vec3(u_mvMatrixMat4 * vec4(a_NormalVec3, C_ZERO_FLOAT)));
	v_BaseAmbientColorVec4 = u_MaterialAmbientVec4 * u_LightAmbientColorVec4;
	v_BaseColorVec4 = u_LightColorVec4 * u_MaterialDiffuseVec4;
	v_LightVectorVec3 = normalize(-u_LightDirectionVec3);
	gl_Position = u_mvpMatrixMat4 * a_PositionVec4;                       		  
}