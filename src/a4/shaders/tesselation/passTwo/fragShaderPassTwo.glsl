#version 430

in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingHalfVector;

in vec4 shadow_coord;


//fog
in vec3 vertEyeSpacePos;

in vec2 tes_out;
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

uniform mat4 shadow_MVP;

uniform int onlyGlobalAmbient;
uniform int reflective;


layout (binding = 0) uniform sampler2D tex_color;
layout (binding = 1) uniform sampler2D tex_height;
layout (binding = 2) uniform sampler2D tex_normal;
layout (binding=5) uniform sampler2DShadow shadowTex;

float lookup(float x, float y){
    float t = textureProj(shadowTex, shadow_coord + vec4(x * 0.001 * shadow_coord.w,
                                                        y * 0.001 * shadow_coord.w,
                                                        -0.01, 0.0));
    return t;
}

vec3 calcNewNormal()
{
    vec3 normal = vec3(0,1,0);
    vec3 tangent = vec3(1,0,0);
    vec3 bitangent = cross(tangent, normal);
    mat3 tbn = mat3(tangent, bitangent, normal);
    vec3 retrievedNormal = texture(tex_normal,tes_out).xyz;
    retrievedNormal = retrievedNormal * 2.0 - 1.0;
    vec3 newNormal = tbn * retrievedNormal;
    newNormal = normalize(newNormal);
    return newNormal;
}

void main(void){

    //fog
    vec4 fogColor = vec4(0.7, 0.8, 0.9, 1.0);
    float fogStart = 15.0;
    float fogEnd = 30.0;

    float dist = length(vertEyeSpacePos.xyz);
    float fogFactor = clamp(((fogEnd-dist)/(fogEnd-fogStart)), 0.0, 1.0);


    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    vec4 lightColor;
    vec4 shadowColor;
    vec4 lightShadowColor;

    float shadowFactor=0.0;
    vec3 L = normalize(varyingLightDir);
    vec3 N = calcNewNormal();
    vec3 V = normalize(-varyingVertPos);

    float cosTheta = dot(L,N);
    vec3 H = normalize(varyingHalfVector);
    float cosPhi = dot(H,N);


    float swidth = 5.5;
    vec2 o = mod(floor(gl_FragCoord.xy), 2.0) * swidth;
    shadowFactor += lookup(-1.5*swidth + o.x,  1.5*swidth - o.y);
    shadowFactor += lookup(-1.5*swidth + o.x, -0.5*swidth - o.y);
    shadowFactor += lookup( 0.5*swidth + o.x,  1.5*swidth - o.y);
    shadowFactor += lookup( 0.5*swidth + o.x, -0.5*swidth - o.y);
    shadowFactor =  textureProj(shadowTex, shadow_coord) * .70 + (shadowFactor/ 4.0) * .30;


    if(onlyGlobalAmbient == 0){

        ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz;
        diffuse = light.diffuse.xyz * material.diffuse.xyz * max(cosTheta,0.0);
        specular = light.specular.xyz * material.specular.xyz * pow(max(cosPhi,0.0), material.shininess* 3.0);

        lightColor = vec4((ambient + diffuse + specular),1.0);
        shadowColor = globalAmbient * material.ambient + light.ambient * material.ambient;
        lightShadowColor = vec4((shadowColor.xyz + shadowFactor*(lightColor.xyz)), 1.0);

    }
    else {

        ambient = ((globalAmbient * material.ambient)).xyz;
        diffuse = vec3(0,0,0);
        specular = vec3(0,0,0);

        lightColor = vec4((ambient), 1.0);
        shadowColor = globalAmbient * material.ambient;
        lightShadowColor = vec4((shadowColor.xyz), 1.0);


    }



    vec4 textureColor = texture(tex_color, tes_out);

    //fog
    vec4 fullColor = 0.5 * textureColor + 0.5 * lightShadowColor;

    color = mix(fogColor, fullColor, fogFactor);

}
