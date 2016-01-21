/**
* 	Diffuse directional lighting helper shader in PerPixel 
*
*	@author Thomas MILLET
**/

precision mediump float;
 
//Varying		         		          		
varying vec3 v_NormalVec3;
varying vec4 v_BaseAmbientColorVec4;  
varying vec4 v_BaseColorVec4;
varying vec3 v_LightVectorVec3;

//Const
const float C_ZERO_FLOAT = 0.0;

void main()                    		
{     
	float l_DotNormalLightFloat = dot(v_NormalVec3, v_LightVectorVec3);

	gl_FragColor = v_BaseAmbientColorVec4 + (v_BaseColorVec4 * max(l_DotNormalLightFloat, C_ZERO_FLOAT));
}	
	