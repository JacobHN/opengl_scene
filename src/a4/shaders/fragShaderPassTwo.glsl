#version 430

in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingHalfVector;

//shadow
in vec4 shadow_coord;

//fog
in vec3 vertEyeSpacePos;

//reflection
in vec3 vNormal;
in vec3 vVertPos;

//bump
in vec3 originalVertex;

//3dTexture
in vec3 originalPosition;

//texture
in vec2 tc;
out vec4 color;

struct PositionalLight{
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    vec3 position;
};

struct Material{
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float shininess;
};


uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform mat4 texRot_matrix;

uniform mat4 shadow_MVP;

uniform int onlyGlobalAmbient;
uniform int bump;
uniform int reflective;
uniform int tex3DBool;

uniform float flipNormal;
uniform float alpha;

layout (binding=0) uniform sampler2D samp;

layout (binding=2) uniform sampler2D normalMap;
layout (binding=3) uniform samplerCube t;
layout (binding=4) uniform sampler3D s;

layout (binding=5) uniform sampler2DShadow shadowTex;


float lookup(float x, float y){
    float t = textureProj(shadowTex, shadow_coord + vec4(x * 0.001 * shadow_coord.w,
                                                        y * 0.001 * shadow_coord.w,
                                                        -0.01, 0.0));
    return t;
}

void main(void){



    //fog
    vec4 fogColor = vec4(0.7, 0.8, 0.9, 1.0);
    float fogStart = 15.0;
    float fogEnd = 30.0;

    float dist = length(vertEyeSpacePos.xyz);
    float fogFactor = clamp(((fogEnd-dist)/(fogEnd-fogStart)), 0.0, 1.0);


    //material
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    //light
    vec4 lightColor;
    vec4 shadowColor;
    vec4 lightShadowColor;

    float shadowFactor=0.0;
    vec3 L = normalize(varyingLightDir);
    vec3 N = normalize(varyingNormal);
    vec3 V = normalize(-varyingVertPos);

    //bump
    if(bump == 1){
        float a = 0.25;
        float b = 75.0;
        float x = originalVertex.x;
        float y = originalVertex.y;
        float z = originalVertex.z;
        N.x = varyingNormal.x + a*sin(b*x);
        N.y = varyingNormal.y + a*sin(b*y);
        N.z = varyingNormal.z + a*sin(b*z);
        N = normalize(N);
    }

    float cosTheta = dot(L,N);
    vec3 H = normalize(varyingHalfVector);
    float cosPhi = dot(H,N);

    //shadow
    float swidth = 5.5;
    vec2 o = mod(floor(gl_FragCoord.xy), 2.0) * swidth;
    shadowFactor += lookup(-1.5*swidth + o.x,  1.5*swidth - o.y);
    shadowFactor += lookup(-1.5*swidth + o.x, -0.5*swidth - o.y);
    shadowFactor += lookup( 0.5*swidth + o.x,  1.5*swidth - o.y);
    shadowFactor += lookup( 0.5*swidth + o.x, -0.5*swidth - o.y);
    shadowFactor =  textureProj(shadowTex, shadow_coord) * .70 + (shadowFactor/ 4.0) * .30;
    //shadowFactor =  textureProj(shadowTex, shadow_coord);

    if(onlyGlobalAmbient == 0){

        ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz;
        diffuse = light.diffuse.xyz * material.diffuse.xyz * max(cosTheta,0.0);
        specular = light.specular.xyz * material.specular.xyz * pow(max(cosPhi,0.0), material.shininess* 3.0);

        lightColor = vec4((ambient + diffuse + specular),1.0);
        shadowColor = globalAmbient * material.ambient + light.ambient * material.ambient;
        lightShadowColor = vec4((shadowColor.xyz + shadowFactor*(lightColor.xyz)), 1.0);

    }else {
        ambient = ((globalAmbient * material.ambient)).xyz;
        diffuse = vec3(0,0,0);
        specular = vec3(0,0,0);

        lightColor = vec4((ambient), 1.0);
        shadowColor = globalAmbient * material.ambient;
        lightShadowColor = vec4((shadowColor.xyz), 1.0);

    }
    vec4 textureColor;

    textureColor = texture(samp, tc);

    if(tex3DBool == 1){
        textureColor = texture(s, originalPosition/2.0+0.5);
    }
    vec4 fullColor = 0.5 * textureColor + 0.5 * lightShadowColor;

    //reflection
    if(reflective == 1){
        vec3 r = -reflect(normalize(-vVertPos), normalize(vNormal));
        fullColor = 0.5 * texture(t, r) + 0.5 * lightShadowColor;
    }

    fullColor = vec4(fullColor.xyz, alpha);

    color = mix(fogColor, fullColor, fogFactor);

}
