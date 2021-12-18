package main.particles;

import lwjglutils.OGLBuffers;

public class ParticleSystem {

    public static OGLBuffers createParticles(float x, float y, int count) {

        float[] velocity = new float[count * 4];
        int[] ib = new int[velocity.length/4];
        int index = 0;
        float cycleTime = 6.0f;

        for(int i = 0; i < velocity.length/4;i++)
        {
            velocity[index++] = x + (float) (Math.random());
            velocity[index++] = y + (float) (Math.random());
            velocity[index++] = (float) (Math.random());
            velocity[index++] =  (float) (cycleTime*Math.random());
            ib[i] = i;
        }

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("velocity", 4)
        };
        return new OGLBuffers(velocity, attributes, ib);
    }
}
