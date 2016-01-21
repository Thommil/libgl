/**
* 	Diffuse spot lighting helper shader in PerVertex 
* 	
*	@author Thomas MILLET
**/

precision mediump float;
 
//Material
uniform sampler2D u_TextureIndexSampler2D;
 
//Varying		         		          		
varying vec4 v_DiffuseBaseColorVec4; 
varying vec4 v_BaseColorVec4;
varying vec2 v_TexCoordinateVec2;

void main()                    		
{        
	gl_FragColor = texture2D(u_TextureIndexSampler2D, v_TexCoordinateVec2) * (v_BaseColorVec4 + v_DiffuseBaseColorVec4);
}	
	