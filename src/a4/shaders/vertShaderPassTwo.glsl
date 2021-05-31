#version 430


layout (location=0) in vec3 pos;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertNormal;

//light
out vec3 varyingNormal;
out vec3 varyingLightDir;
out vec3 varyingVertPos;
out vec3 varyingHalfVector;

//shadows
out vec4 shadow_coord;

//texture
out vec2 tc;

//fog
out vec3 vertEyeSpacePos;

//reflection
out vec3 vNormal;
out vec3 vVertPos;

//bump
out vec3 originalVertex;

//3d texture
out vec3 originalPosition;

//light
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
uniform mat4 shadowMVP;
uniform mat4 texRot_matrix;

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

void main(void){
    //bump
    originalVertex = pos;


    //transparency
    if(flipNormal < 0){
        varyingNormal = -varyingNormal;
    }

    varyingVertPos = (mv_matrix * vec4(pos, 1.0)).xyz;
    varyingLightDir = light.position - varyingVertPos;
    varyingNormal = (norm_matrix * vec4(vertNormal,1.0)).xyz;

    varyingHalfVector = normalize(normalize(varyingLightDir)
                        + normalize(-varyingVertPos)).xyz;

    //shadow
    shadow_coord = shadowMVP * vec4(pos,1.0);

    //fog
    vertEyeSpacePos = (mv_matrix * vec4(pos,1.0)).xyz;

    //reflection
    vVertPos = (mv_matrix * vec4(pos,1.0)).xyz;
    vNormal = (norm_matrix * vec4(vertNormal,1.0)).xyz;

    //3d texture
    if(tex3DBool == 1){
        originalPosition = vec3(texRot_matrix * vec4(pos, 1.0f)).xyz;
    }



    gl_Position = proj_matrix * mv_matrix * vec4(pos,1.0);
    tc = texCoord;
}