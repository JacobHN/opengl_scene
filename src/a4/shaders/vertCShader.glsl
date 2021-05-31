#version 430


layout (location=0) in vec3 pos;
layout (location=1) in vec2 texCoord;
out vec3 tc;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;

layout (binding=0) uniform samplerCube samp;

void main(void){

    tc = pos;
    gl_Position = proj_matrix * mv_matrix * vec4(pos,1.0);
}