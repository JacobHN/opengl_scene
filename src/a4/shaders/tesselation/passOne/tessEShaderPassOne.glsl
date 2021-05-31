#version 430

layout (quads, equal_spacing,ccw) in;

out vec3 varyingNormal;
out vec3 varyingLightDir;
out vec3 varyingVertPos;
out vec3 varyingHalfVector;

out vec4 shadow_coord;

out vec2 tc;

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


uniform mat4 shadowMVP;

in vec2 tcs_out[];
out vec2 tes_out;

layout (binding = 0) uniform sampler2D tex_color;
layout (binding = 1) uniform sampler2D tex_height;
layout (binding = 2) uniform sampler2D tex_normal;
layout (binding = 5) uniform sampler2DShadow shadowTex;

void main() {

    vec2 tc = vec2(tcs_out[0].x + (gl_TessCoord.x)/64.0,
    tcs_out[0].y + (1.0-gl_TessCoord.y)/64.0);

    vec4 tessellatedPoint = vec4(gl_in[0].gl_Position.x + gl_TessCoord.x/64.0, 0.0,
    gl_in[0].gl_Position.z + gl_TessCoord.y/64.0, 1.0);

    tessellatedPoint.y += (texture(tex_height, tc).r) / 60.0;


    gl_Position = shadowMVP * tessellatedPoint;
    tes_out = tc;
}
