/**
s 
* 	
*	@author Thomas MILLET
**/
 
precision mediump float;
 
//Material
uniform sampler2D u_TextureIndexSampler2D;

//Varying		
varying vec4 v_BaseAmbientColorVec4;  
varying vec2 v_TexCoordinateVec2; 

void main()                    		
{
	gl_FragColor = v_BaseAmbientColorVec4 * texture2D(u_TextureIndexSampler2D, v_TexCoordinateVec2);
}	
	