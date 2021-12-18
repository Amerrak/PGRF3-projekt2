#version 150

in vec4 velocity;

uniform mat4 projection;
uniform mat4 view;
uniform float acceleration;
uniform float time;
uniform float cycleTime;

out float t;

void main(void)
{
    vec4 position;
    t = time;

    if(time > velocity.w)
    {
        t = time - velocity.w;
    }
    t = mod(t, cycleTime);
    position = vec4(velocity.x, velocity.y, velocity.z * t, 1.0);

    gl_Position = position;
}