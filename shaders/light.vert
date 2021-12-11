#version 150
in vec2 inPosition;

uniform mat4 view;
uniform mat4 projection;

uniform int solid;
uniform float time;
uniform mat4 rotation;

const float PI = 3.14159;

vec3 getSphere(vec2 pos) {
    float az = pos.x * PI;
    float ze = pos.y * PI / 2;
    float r = (2 + sin(time)) / 3;

    float x = r * cos(az + time) * cos(ze);
    float y = 2 * r * sin(az + time) * cos(ze);
    float z = 0.5 * r * sin(ze);

    return vec3(x, y, z);
}

vec3 getPlane(vec2 pos) {
    return vec3(pos * 3.0, -1.0);
}

vec3 getPlane2(vec2 pos) {
    pos = 3*pos;
    float z = (sin(pos.x * pos.x * sin(time) + pos.y * pos.y * sin(time))/3) - 1;
    return vec3(pos, z);
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

vec3 getCylinder(vec2 pos) {
    float az = pos.x * PI;
    float r = 1;

    float x = r * cos(az)/2 + sin(time)*3;
    float y = r * sin(az)/2;
    float z = pos.y;

    return vec3(x, y, z);
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

vec3 getCylindricalObject2(vec2 pos) {
    float az = pos.x * PI;
    float ze = pos.y * PI;

    float x = cos((2*az)+4);
    float y = 4 + (2 * sin(az*3));
    float z = ze/2;

    return vec3(x, y, z);

}

void main() {
    vec2 position = inPosition * 2 - 1;

    vec3 pos3;
    if (solid == 1) {
        pos3 = getPlane(position);
    } else if (solid == 2){
        pos3 = getPlane2(position);
    } else if (solid == 3) {
        pos3 = getDonut(position);
    } else if (solid == 4){
        pos3 = getCylinder(position);
    } else if (solid == 5){
        pos3 = getSphericalObject2(position);
    } else if (solid == 6){
        pos3 = getCylindricalObject2(position);
    }

    gl_Position = projection * view * rotation * vec4(pos3, 1.0);
}
