/**
* 	Simplest shader with ambient light support (no texture) 
* 	
*	@author Thomas MILLET
**/
 
precision mediump float;
 
//Material
uniform vec4 u_MaterialAmbientVec4;

//Light
uniform vec4 u_LightAmbientColorVec4;
		  
void main()                    		
{
	gl_FragColor = u_MaterialAmbientVec4 * u_LightAmbientColorVec4;
}	
	