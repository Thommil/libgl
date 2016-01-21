/**
* 	Simplest shader with ambient light support (texture) 
* 	
*	@author Thomas MILLET
**/

precision mediump float;

//Context
uniform mat4 u_mvpMatrixMat4;      		       

//Material
uniform vec4 u_MaterialAmbientVec4;

//Light
uniform vec4 u_LightAmbientColorVec4;

//Attributes
attribute vec4 a_PositionVec4;   				 				
attribute vec2 a_TexCoordinateVec2;	  
		  
//Varying		  
varying vec4 v_BaseAmbientColorVec4;
varying vec2 v_TexCoordinateVec2; 

void main()                                                 	
{
    v_TexCoordinateVec2 = a_TexCoordinateVec2;
	v_BaseAmbientColorVec4 = u_MaterialAmbientVec4 * u_LightAmbientColorVec4;
	gl_Position = u_mvpMatrixMat4 * a_PositionVec4;                       		  
}