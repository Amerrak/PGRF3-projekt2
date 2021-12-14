uniform mat4 projection;
uniform mat4 view;
uniform float acceleration;
uniform float time;
uniform float cycleTime;

in vec4 velocity;

void main(void)
{
    vec4 position;
    float t = time;

    if(time > velocity.w)
    {
        t = time - velocity.w;
    }
    t = mod(t, cycleTime);
    position = vec4(velocity.x * t, velocity.y * t + 0.5 * acceleration * t * t, velocity.z * t, 1.0);

    gl_Position = position;
}