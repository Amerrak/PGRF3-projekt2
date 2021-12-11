#version 150
in vec2 inPosition;

uniform mat4 view;
uniform mat4 projection;
uniform mat4 lightVP;

uniform int solid;
uniform vec3 lightPosition;
uniform vec3 eyePosition;
uniform float time;
uniform mat4 rotation;

out vec2 texCoord;
out vec3 normal;
out vec3 light;
out vec3 viewDirection;
out vec4 depthTextureCoord;
out float dist;
out vec4 outPosition;

const float PI = 3.14159;

vec3 getPlane(vec2 pos) {
    return vec3(pos * 3.0, -1.0);
}

vec3 getPlaneNormal(vec2 pos) {
    vec3 u = getPlane(pos + vec2(0.001, 0)) - getPlane(pos - vec2(0.001, 0));
    vec3 v = getPlane(pos + vec2(0, 0.001)) - getPlane(pos - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getPlane2(vec2 pos) {
    pos = 3*pos;
    float z = (sin(pos.x * pos.x * sin(time) + pos.y * pos.y * sin(time))/3) - 1;
    return vec3(pos, z);
}

vec3 getPlane2Normal(vec2 pos) {
    vec3 u = getPlane2(pos + vec2(0.001, 0)) - getPlane2(pos - vec2(0.001, 0));
    vec3 v = getPlane2(pos + vec2(0, 0.001)) - getPlane2(pos - vec2(0, 0.001));
    return cross(u, v);
}

float getZ(vec2 pos) {
    return sin(pos.x * 5);
}

vec3 getCylinder(vec2 pos) {
    float az = pos.x * PI;
    float r = 1;

    float x = r * cos(az)/2 + sin(time)*3;
    float y = r * sin(az)/2;
    float z = pos.y;

    return vec3(x, y, z);
}

vec3 getCylinderNormal(vec2 pos) {
    vec3 u = getCylinder(pos + vec2(0.001, 0)) - getCylinder(pos - vec2(0.001, 0));
    vec3 v = getCylinder(pos + vec2(0, 0.001)) - getCylinder(pos - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getSphericalObject2(vec2 pos) {
    float az = pos.x * PI;
    float ze = pos.y * PI;
    float r = 1;

    float x = (r * cos(az) * cos(ze)) - 4;
    float y = r * sin(2 * az) * (2*cos(3 * ze));
    float z = r * sin(ze);

    return vec3(x, y, z);

}

vec3 getSphericalObject2Normal(vec2 pos) {
    vec3 u = getSphericalObject2(pos + vec2(0.001, 0)) - getSphericalObject2(pos - vec2(0.001, 0));
    vec3 v = getSphericalObject2(pos + vec2(0, 0.001)) - getSphericalObject2(pos - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getCylindricalObject2(vec2 pos) {
    float az = pos.x * PI;
    float ze = pos.y * PI;

    float x = cos((2*az)+4);
    float y = 4 + (2 * sin(az*3));
    float z = ze/2;

    return vec3(x, y, z);

}

vec3 getCylindricalObject2Normal(vec2 pos) {
    vec3 u = getCylindricalObject2(pos + vec2(0.001, 0)) - getCylindricalObject2(pos - vec2(0.001, 0));
    vec3 v = getCylindricalObject2(pos + vec2(0, 0.001)) - getCylindricalObject2(pos - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getDonut(vec2 pos) {
    float az = pos.x * PI;
    float ze = pos.y * PI;
    float r = (2 + sin(3*time))/10;

    float x = r * cos(az) * (3 + 1.4 * cos(ze));
    float y = r * sin(az) * (3 + 1.4 * cos(ze)) + sin(time/2)*3;
    float z = r * 1.4 * sin(ze);

    return vec3(x, y, z);

}

vec3 getDonutNormal(vec2 pos) {
    vec3 u = getDonut(pos + vec2(0.001, 0)) - getDonut(pos - vec2(0.001, 0));
    vec3 v = getDonut(pos + vec2(0, 0.001)) - getDonut(pos - vec2(0, 0.001));
    return cross(u, v);
}


void main() {
    vec2 position = inPosition * 2 - 1;
    texCoord = inPosition;

    vec3 pos3;
    if (solid == 1) {
        pos3 = getPlane(position);
        normal = getPlaneNormal(position);
    } else if (solid == 2){
        pos3 = getPlane2(position);
        normal = getPlane2Normal(position);
    } else if (solid == 3) {
        pos3 = getDonut(position);
        normal = getDonutNormal(position);
    } else if (solid == 4){
        pos3 = getCylinder(position);
        normal = getCylinderNormal(position);
    } else if (solid == 5){
        pos3 = getSphericalObject2(position);
        normal = getSphericalObject2Normal(position);
    } else if (solid == 6){
        pos3 = getCylindricalObject2(position);
        normal = getCylindricalObject2Normal(position);
    }

    gl_Position = projection * view * rotation * vec4(pos3, 1.0);
    outPosition = rotation * vec4(pos3, 1.0);
    light = lightPosition - pos3;
    dist = length(light);
    viewDirection = eyePosition - pos3;

    depthTextureCoord = lightVP * vec4(pos3, 1.0);
    depthTextureCoord.xyz = depthTextureCoord.xyz / depthTextureCoord.w;
    depthTextureCoord.xyz = (depthTextureCoord.xyz + 1) / 2;
} 
