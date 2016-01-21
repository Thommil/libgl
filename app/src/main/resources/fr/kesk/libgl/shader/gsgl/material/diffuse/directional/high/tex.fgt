/**
* 	Diffuse directional lighting helper shader in PerPixel 
*
*	@author Thomas MILLET
**/

precision mediump float;
 
//Material
uniform vec4 u_MaterialAmbientVec4;
uniform sampler2D u_TextureIndexSampler2D;

//Light
uniform vec4 u_LightColorVec4;
		  
//Varying		         		          		
varying vec3 v_NormalVec3;  
varying vec2 v_TexCoordinateVec2;
varying vec4 v_BaseAmbientColorVec4;  
varying vec3 v_LightVectorVec3;

//Const
const float C_ZERO_FLOAT = 0.0;

void main()                    		
{	
	float l_DotNormalLightFloat = dot(v_NormalVec3, v_LightVectorVec3);
	
	gl_FragColor = texture2D(u_TextureIndexSampler2D, v_TexCoordinateVec2) * (v_BaseAmbientColorVec4 + (u_LightColorVec4 * max(l_DotNormalLightFloat, C_ZERO_FLOAT)));
}	
	