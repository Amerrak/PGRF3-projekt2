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

    private int shaderProgramParticlesPointSprite;
    private int locProjectionParticlesPointSprite, locViewParticlesPointSprite, locAccelerationParticlesPointSprite, locTimeParticlesPointSprite, locCycleTimeParticlesPointSprite;

    private boolean perspectiveProjection = true;
    private boolean mousePressed;

    private OGLBuffers buffersParticles;

    private OGLTexture2D particleStarTexture;
    private Camera camera;

    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        shaderProgramParticles = ShaderUtils.loadProgram("/particles");
        shaderProgramParticlesPointSprite = ShaderUtils.loadProgram("/particlesPointSprite");

        locProjectionParticles = glGetUniformLocation(shaderProgramParticles, "projection");
        locViewParticles = glGetUniformLocation(shaderProgramParticles, "view");
        locAccelerationParticles = glGetUniformLocation(shaderProgramParticles, "acceleration");
        locTimeParticles = glGetUniformLocation(shaderProgramParticles, "time");
        locCycleTimeParticles = glGetUniformLocation(shaderProgramParticles, "cycleTime");

        locProjectionParticlesPointSprite = glGetUniformLocation(shaderProgramParticles, "projection");
        locViewParticlesPointSprite = glGetUniformLocation(shaderProgramParticles, "view");
        locAccelerationParticlesPointSprite = glGetUniformLocation(shaderProgramParticles, "acceleration");
        locTimeParticlesPointSprite = glGetUniformLocation(shaderProgramParticles, "time");
        locCycleTimeParticlesPointSprite = glGetUniformLocation(shaderProgramParticles, "cycleTime");

        buffersParticles = ParticleSystem.createParticles();

        camera = new Camera()
                .withPosition(new Vec3D(-3, 3, 3))
                .withAzimuth(-1 / 4f * Math.PI)
                .withZenith(-1.3 / 5f * Math.PI);

        textRenderer = new OGLTextRenderer(width, height);

        try {
            particleStarTexture = new OGLTexture2D("textures/fire.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void display() {
        glEnable(GL_DEPTH_TEST);

        renderFromParticlesPointSprite();

        textRenderer.addStr2D(width - 150, height - 3, "H for help");
    }

    private void renderFromParticles() {
        glUseProgram(shaderProgramParticles);

        glDisable(GL_VERTEX_PROGRAM_POINT_SIZE);
        glDisable(GL_POINT_SPRITE);

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

    private void renderFromParticlesPointSprite() {
        glUseProgram(shaderProgramParticlesPointSprite);

        glEnable(GL_VERTEX_PROGRAM_POINT_SIZE);
        glEnable(GL_POINT_SPRITE);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glViewport(0, 0, width, height);

        glClearColor(0f, 0.5f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUniformMatrix4fv(locViewParticlesPointSprite, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjectionParticlesPointSprite, false, projection.floatArray());

        glUniform1f(locTimeParticlesPointSprite, TimeManager.getTime());
        glUniform1f(locAccelerationParticlesPointSprite, -1.8f);
        glUniform1f(locCycleTimeParticlesPointSprite, 6.0f);

        particleStarTexture.bind(shaderProgramParticlesPointSprite, "particleStar", 0);
        buffersParticles.draw(GL_POINTS, shaderProgramParticlesPointSprite);
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

                    case GLFW_KEY_H -> JOptionPane.showMessageDialog(null,
                            " H - Help " +
                                    "\n LeftClick and drag - Changing observer's view" +
                                    "\n P - Change projection - Perspective / Orthogonal" +
                                    "\n W,A,S,D,R,F - Movement - forward, left, backward, right, up, down",
                            "Help",
                            JOptionPane.INFORMATION_MESSAGE);

                }
            }
        }
    };
}
