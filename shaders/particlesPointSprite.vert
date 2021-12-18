#version 150

uniform mat4 projection;
uniform mat4 view;
uniform float acceleration;
uniform float time;
uniform float cycleTime;

in vec4 velocity;

out float lifeLength;

void main(void)
{
    vec4 position;
    float t = time;
    gl_PointSize = 50;

    if(time > velocity.w)
    {
        t = time - velocity.w;
    }
    t = mod(t, cycleTime);
    position = vec4(velocity.x, velocity.y, velocity.z * t, 1.0);

    gl_Position = projection * view * position ;
    lifeLength = t;
}