package main;

import lwjglutils.*;
import main.particles.ParticleSystem;
import main.utils.TimeManager;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import transforms.*;

import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * @author PGRF FIM UHK
 * @version 2.0
 * @since 2019-09-02
 */
public class Renderer extends AbstractRenderer {

    private final Mat4 ORTHOGONAL_PROJECTION = new Mat4OrthoRH(5, 5, 0.1, 25);
    private final Mat4 PERSPECTIVE_PROJECTION = new Mat4PerspRH(Math.PI / 3, 600 / 800f, 1.0, 20.0);

    private Mat4 projection = PERSPECTIVE_PROJECTION;

    private double oldMx, oldMy;

    private int shaderProgramParticles;

    private int locProjectionParticles, locViewParticles, locAccelerationParticles, locTimeParticles, locCycleTimeParticles;

    private boolean perspectiveProjection = true;
    private boolean mousePressed;

    private OGLBuffers buffersParticles;

    private OGLTexture2D particleStarTexture;
    private Camera camera, cameraLight;

    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        shaderProgramParticles = ShaderUtils.loadProgram("/particles");

        locProjectionParticles = glGetUniformLocation(shaderProgramParticles, "projection");
        locViewParticles = glGetUniformLocation(shaderProgramParticles, "view");
        locAccelerationParticles = glGetUniformLocation(shaderProgramParticles, "acceleration");
        locTimeParticles = glGetUniformLocation(shaderProgramParticles, "time");
        locCycleTimeParticles = glGetUniformLocation(shaderProgramParticles, "cycleTime");

        buffersParticles = ParticleSystem.createParticles();

        camera = new Camera()
                .withPosition(new Vec3D(-3, 3, 3))
                .withAzimuth(-1 / 4f * Math.PI)
                .withZenith(-1.3 / 5f * Math.PI);

        textRenderer = new OGLTextRenderer(width, height);

        cameraLight = new Camera()
                .withPosition(new Vec3D(-3, 3, 3))
                .withAzimuth(-1 / 4f * Math.PI)
                .withZenith(-1.3 / 5f * Math.PI);

        try {
            particleStarTexture = new OGLTexture2D("textures/fire.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void display() {
        glEnable(GL_DEPTH_TEST);

        renderFromParticles();

        textRenderer.addStr2D(width - 150, height - 3, "H for help");
    }

    private void renderFromParticles() {
        glUseProgram(shaderProgramParticles);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glViewport(0, 0, width, height);

        glClearColor(0f, 0.5f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUniformMatrix4fv(locViewParticles, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjectionParticles, false, projection.floatArray());

        glUniform1f(locTimeParticles, TimeManager.getTime());
        glUniform1f(locAccelerationParticles, -1.8f);
        glUniform1f(locCycleTimeParticles, 6.0f);

        particleStarTexture.bind(shaderProgramParticles, "particleStar", 0);
        buffersParticles.draw(GL_POINTS, shaderProgramParticles);
    }

    @Override
    public GLFWCursorPosCallback getCursorCallback() {
        return cursorPosCallback;
    }

    @Override
    public GLFWMouseButtonCallback getMouseCallback() {
        return mouseButtonCallback;
    }

    @Override
    public GLFWKeyCallback getKeyCallback() {
        return keyCallback;
    }

    private final GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
            if (mousePressed) {
                camera = camera.addAzimuth(Math.PI * (oldMx - x) / LwjglWindow.WIDTH);
                camera = camera.addZenith(Math.PI * (oldMy - y) / LwjglWindow.HEIGHT);
                oldMx = x;
                oldMy = y;
            }
        }
    };

    private final GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
        @Override
        public void invoke(long window, int button, int action, int mods) {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                double[] xPos = new double[1];
                double[] yPos = new double[1];
                glfwGetCursorPos(window, xPos, yPos);
                oldMx = xPos[0];
                oldMy = yPos[0];
                mousePressed = (action == GLFW_PRESS);
            }
        }
    };

    private final GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                switch (key) {
                    case GLFW_KEY_A -> camera = camera.left(0.1);
                    case GLFW_KEY_D -> camera = camera.right(0.1);
                    case GLFW_KEY_W -> camera = camera.forward(0.1);
                    case GLFW_KEY_S -> camera = camera.backward(0.1);
                    case GLFW_KEY_R -> camera = camera.up(0.1);
                    case GLFW_KEY_F -> camera = camera.down(0.1);
                    case GLFW_KEY_P -> {
                        perspectiveProjection = !perspectiveProjection;
                        if (perspectiveProjection) {
                            projection = PERSPECTIVE_PROJECTION;
                        } else {
                            projection = ORTHOGONAL_PROJECTION;
                        }
                    }
                    case GLFW_KEY_V -> cameraLight = cameraLight.addAzimuth(0.1);
                    case GLFW_KEY_C -> cameraLight = cameraLight.addAzimuth(-0.1);

                    case GLFW_KEY_I -> cameraLight = cameraLight.forward(0.1);
                    case GLFW_KEY_K -> cameraLight = cameraLight.backward(0.1);
                    case GLFW_KEY_J -> cameraLight = cameraLight.left(0.1);
                    case GLFW_KEY_L -> cameraLight = cameraLight.right(0.1);
                    case GLFW_KEY_H -> JOptionPane.showMessageDialog(null,
                            " H - Help " +
                                    "\n LeftClick and drag - Changing observer's view" +
                                    "\n W,A,S,D,R,F - Movement - forward, left, backward, right, up, down" +
                                    "\n E - Change polygon mode: "
                                    + "\n                   ["
                                    + "\n                     0: Standard (Fill)"
                                    + "\n                     1: Lines, "
                                    + "\n                     2: Points "
                                    + "\n                   ]" +
                                    "\n Q - Textures [on / off]" +
                                    "\n G - Animations [on / off]" +
                                    "\n 1 - Ambient lighting [on / off]" +
                                    "\n 2 - Diffuse lighting [on / off]" +
                                    "\n 3 - Specular lighting [on / off]" +
                                    "\n 4 - Switch shape of plane [1 / 2]" +
                                    "\n 5 - Show next cylindrical object [on / off]" +
                                    "\n 6 - Show next spherical object [on / off]" +
                                    "\n I,J,K,L,V,C- Movement of light source - forward, left, backward, turn right, turn left" +
                                    "\n T - Reflector lighting [on / off]" +
                                    "\n B - Change debug mode: "
                                    + "\n                   ["
                                    + "\n                     0: Standard, "
                                    + "\n                     1: Position, "
                                    + "\n                     2: Depth buffer "
                                    + "\n                     3: Normal, "
                                    + "\n                     4: Texture coordinate "
                                    + "\n                     5: Distance from light source "
                                    + "\n                   ]",
                            "Help",
                            JOptionPane.INFORMATION_MESSAGE);

                }
            }
        }
    };
}
