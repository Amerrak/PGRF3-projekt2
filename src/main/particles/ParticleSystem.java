package main.particles;

import lwjglutils.OGLBuffers;

public class ParticleSystem {

    public static OGLBuffers createParticles() {

        float[] velocity = new float[100000];
        int[] ib = new int[velocity.length/4];
        int index = 0;
        float cycleTime = 6.0f;

        for(int i = 0; i < velocity.length/4;i++)
        {
            velocity[index++] = (float) (Math.random());
            velocity[index++] = (float) (Math.random());
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
