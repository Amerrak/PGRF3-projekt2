#version 150

in float t[];

layout (points) in;
layout (triangle_strip, max_vertices = 24) out;

uniform mat4 projection;
uniform mat4 view;
uniform int cubeMode;

out vec3 finalColor;
out vec2 textureCoord;
out float lifeLength;

const float size = 0.1;
const vec3 lightDirection = normalize(vec3(0.4, -1.0, 0.8));

void createVertex(vec3 offset, vec3 faceNormal){
    vec4 actualOffset = vec4(offset * size, 0.0);
    vec4 worldPosition = gl_in[0].gl_Position + actualOffset;
    gl_Position = projection * view * worldPosition;
    float brightness = max(dot(-lightDirection, faceNormal), 0.3);
    finalColor = vec3(1.0,1.0,1.0) * brightness;
    lifeLength = t[0];
    EmitVertex();
}

void createVertexWithTexture(vec3 offset, vec3 faceNormal, vec2 text){
    vec4 actualOffset = vec4(offset * size, 0.0);
    vec4 worldPosition = gl_in[0].gl_Position + actualOffset;
    gl_Position = projection * view * worldPosition;
    float brightness = max(dot(-lightDirection, faceNormal), 0.3);
    finalColor = vec3(1.0,1.0,1.0) * brightness;
    textureCoord = text;
    lifeLength = t[0];
    EmitVertex();
}

void main(void)
{
    if(cubeMode == 1){
        vec3 faceNormal = vec3(0.0, 0.0, 1.0);
        createVertex(vec3(-1.0, 1.0, 1.0), faceNormal);
        createVertex(vec3(-1.0, -1.0, 1.0), faceNormal);
        createVertex(vec3(1.0, 1.0, 1.0), faceNormal);
        createVertex(vec3(1.0, -1.0, 1.0), faceNormal);

        EndPrimitive();

        faceNormal = vec3(1.0, 0.0, 0.0);
        createVertex(vec3(1.0, 1.0, 1.0), faceNormal);
        createVertex(vec3(1.0, -1.0, 1.0), faceNormal);
        createVertex(vec3(1.0, 1.0, -1.0), faceNormal);
        createVertex(vec3(1.0, -1.0, -1.0), faceNormal);

        EndPrimitive();

        faceNormal = vec3(0.0, 0.0, -1.0);
        createVertex(vec3(1.0, 1.0, -1.0), faceNormal);
        createVertex(vec3(1.0, -1.0, -1.0), faceNormal);
        createVertex(vec3(-1.0, 1.0, -1.0), faceNormal);
        createVertex(vec3(-1.0, -1.0, -1.0), faceNormal);

        EndPrimitive();

        faceNormal = vec3(-1.0, 0.0, 0.0);
        createVertex(vec3(-1.0, 1.0, -1.0), faceNormal);
        createVertex(vec3(-1.0, -1.0, -1.0), faceNormal);
        createVertex(vec3(-1.0, 1.0, 1.0), faceNormal);
        createVertex(vec3(-1.0, -1.0, 1.0), faceNormal);

        EndPrimitive();

        faceNormal = vec3(0.0, 1.0, 0.0);
        createVertex(vec3(1.0, 1.0, 1.0), faceNormal);
        createVertex(vec3(1.0, 1.0, -1.0), faceNormal);
        createVertex(vec3(-1.0, 1.0, 1.0), faceNormal);
        createVertex(vec3(-1.0, 1.0, -1.0), faceNormal);

        EndPrimitive();

        faceNormal = vec3(0.0, -1.0, 0.0);
        createVertex(vec3(-1.0, -1.0, 1.0), faceNormal);
        createVertex(vec3(-1.0, -1.0, -1.0), faceNormal);
        createVertex(vec3(1.0, -1.0, 1.0), faceNormal);
        createVertex(vec3(1.0, -1.0, -1.0), faceNormal);

        EndPrimitive();
    }else {

        vec3 faceNormal = vec3(0.0, 1.0, 0.0);
        createVertexWithTexture(vec3(1.0, 1.0, 1.0), faceNormal, vec2(0.0,0.0));
        createVertexWithTexture(vec3(1.0, 1.0, -1.0), faceNormal, vec2(0.0,1.0));
        createVertexWithTexture(vec3(-1.0, 1.0, 1.0), faceNormal, vec2(1.0,0.0));
        createVertexWithTexture(vec3(-1.0, 1.0, -1.0), faceNormal, vec2(1.0,1.0));

        EndPrimitive();
    }
}