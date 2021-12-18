#version 150

in vec4 velocity;

uniform mat4 projection;
uniform mat4 view;
uniform float acceleration;
uniform float time;
uniform float cycleTime;
uniform int solid;
uniform int pointSize;

out float lifeLength;

void main(void)
{
    vec4 position;
    float t = time;
    gl_PointSize = pointSize;

    if(time > velocity.w)
    {
        t = time - velocity.w;
    }
    t = mod(t, cycleTime);

    if(solid == 1){
        position = vec4(velocity.x, velocity.y, velocity.z * t, 1.0);
    }else if (solid == 2){
        position = vec4(2 + velocity.x * t, 2 + velocity.y * t + 0.5 * acceleration * t * t, velocity.z * t, 1.0);
    }else if (solid == 3){
        position = vec4(velocity.x - 5,velocity.y - 2, velocity.z * t, 1.0);
    }else if (solid == 4){
        position = vec4(velocity.x - 6,velocity.y - 2, velocity.z * t, 1.0);
    }else if (solid == 5){
        position = vec4(velocity.x + 3,velocity.y + 5, velocity.z * t, 1.0);
    }else if (solid == 6){
        position = vec4((velocity.x - 1.5) * t - 1,(velocity.y - 1.5) * t - 5, (velocity.z - 0.5) * t, 1.0);
    }

    gl_Position = projection * view * position ;
    lifeLength = t;
}