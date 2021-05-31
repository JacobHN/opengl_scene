#version 430


layout (location=0) in vec3 pos;

uniform mat4 shadowMVP;

void main(void){
    gl_Position =  shadowMVP * vec4(pos,1.0);
}