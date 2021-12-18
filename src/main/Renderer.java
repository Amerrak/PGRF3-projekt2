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
    private int locProjectionParticles, locViewParticles, locAccelerationParticles, locTimeParticles, locCycleTimeParticles, locCubeModeParticles, locRowCountParticles, locColumnCountParticles;

    private int shaderProgramParticlesPointSprite;
    private int locProjectionParticlesPointSprite, locViewParticlesPointSprite, locAccelerationParticlesPointSprite, locTimeParticlesPointSprite, locCycleTimeParticlesPointSprite;
    private int locRowCountParticlesPointSprite, locColumnCountParticlesPointSprite, locSolid, locPointSize;

    private boolean perspectiveProjection = true;
    private boolean mousePressed;
    private boolean cubeMode = true;

    private OGLBuffers buffersParticles, buffersParticles2, buffersWithLessParticles;

    private OGLTexture2D fireTexture, cosmicTexture, particleStarTexture, testTexture, smokeTexture, particleAtlasTexture;
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
        locCubeModeParticles = glGetUniformLocation(shaderProgramParticles, "cubeMode");
        locRowCountParticles = glGetUniformLocation(shaderProgramParticles, "rowCount");
        locColumnCountParticles = glGetUniformLocation(shaderProgramParticles, "columnCount");

        locProjectionParticlesPointSprite = glGetUniformLocation(shaderProgramParticlesPointSprite, "projection");
        locViewParticlesPointSprite = glGetUniformLocation(shaderProgramParticlesPointSprite, "view");
        locAccelerationParticlesPointSprite = glGetUniformLocation(shaderProgramParticlesPointSprite, "acceleration");
        locTimeParticlesPointSprite = glGetUniformLocation(shaderProgramParticlesPointSprite, "time");
        locCycleTimeParticlesPointSprite = glGetUniformLocation(shaderProgramParticlesPointSprite, "cycleTime");
        locRowCountParticlesPointSprite = glGetUniformLocation(shaderProgramParticlesPointSprite, "rowCount");
        locColumnCountParticlesPointSprite = glGetUniformLocation(shaderProgramParticlesPointSprite, "columnCount");
        locSolid = glGetUniformLocation(shaderProgramParticlesPointSprite, "solid");
        locPointSize = glGetUniformLocation(shaderProgramParticlesPointSprite, "pointSize");

        buffersParticles = ParticleSystem.createParticles(1, 1, 25000);
        buffersParticles2 = ParticleSystem.createParticles(0,0, 25000);
        buffersWithLessParticles = ParticleSystem.createParticles(0,0, 50);

        camera = new Camera()
                .withPosition(new Vec3D(-3, 3, 3))
                .withAzimuth(-1 / 4f * Math.PI)
                .withZenith(-1.3 / 5f * Math.PI);
        camera = camera.backward(10);
        textRenderer = new OGLTextRenderer(width, height);

        try {
            fireTexture = new OGLTexture2D("textures/fire.png");
            cosmicTexture = new OGLTexture2D("textures/cosmic.png");
            particleStarTexture = new OGLTexture2D("textures/particleStar.png");
            testTexture = new OGLTexture2D("textures/test.png");
            smokeTexture = new OGLTexture2D("textures/smoke.png");
            particleAtlasTexture = new OGLTexture2D("textures/particleAtlas.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void display() {
        glEnable(GL_DEPTH_TEST);

        glClearColor(0f, 0.5f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, width, height);

        renderFromParticles();
        renderFromParticlesPointSprite();

        textRenderer.addStr2D(width - 250, height - 3, "H for help. E for Geometry change:" + (cubeMode ? "Cubes" : "Quads"));
    }

    private void renderFromParticles() {
        glUseProgram(shaderProgramParticles);

        glDisable(GL_VERTEX_PROGRAM_POINT_SIZE);
        glDisable(GL_POINT_SPRITE);

        glUniformMatrix4fv(locViewParticles, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjectionParticles, false, projection.floatArray());

        glUniform1f(locTimeParticles, TimeManager.getTime());
        glUniform1f(locAccelerationParticles, -1.8f);
        glUniform1f(locCycleTimeParticles, 6.0f);
        glUniform1i(locCubeModeParticles, cubeMode ? GL_TRUE : GL_FALSE);

        glUniform1i(locRowCountParticles, 8);
        glUniform1i(locColumnCountParticles, 8);
        fireTexture.bind(shaderProgramParticles, "textureSampler", 0);

        if(cubeMode){
            buffersWithLessParticles.draw(GL_POINTS, shaderProgramParticles);
        }else {
            buffersParticles2.draw(GL_POINTS, shaderProgramParticles);
        }

    }

    private void renderFromParticlesPointSprite() {
        glUseProgram(shaderProgramParticlesPointSprite);
        glUniform1i(locPointSize, 30);

        glEnable(GL_VERTEX_PROGRAM_POINT_SIZE);
        glEnable(GL_POINT_SPRITE);

        glUniformMatrix4fv(locViewParticlesPointSprite, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjectionParticlesPointSprite, false, projection.floatArray());

        glUniform1f(locTimeParticlesPointSprite, TimeManager.getTime());
        glUniform1f(locAccelerationParticlesPointSprite, -1.8f);
        glUniform1f(locCycleTimeParticlesPointSprite, 6.0f);

        glUniform1i(locRowCountParticlesPointSprite, 8);
        glUniform1i(locColumnCountParticlesPointSprite, 8);
        glUniform1i(locSolid, 1);
        fireTexture.bind(shaderProgramParticlesPointSprite, "textureSampler", 0);
        buffersParticles.draw(GL_POINTS, shaderProgramParticlesPointSprite);

        glUniform1i(locRowCountParticlesPointSprite, 4);
        glUniform1i(locColumnCountParticlesPointSprite, 4);
        glUniform1i(locSolid, 2);
        cosmicTexture.bind(shaderProgramParticlesPointSprite, "textureSampler", 0);
        buffersParticles.draw(GL_POINTS, shaderProgramParticlesPointSprite);

        glUniform1i(locRowCountParticlesPointSprite, 1);
        glUniform1i(locColumnCountParticlesPointSprite, 1);
        glUniform1i(locSolid, 3);
        particleStarTexture.bind(shaderProgramParticlesPointSprite, "textureSampler", 0);
        buffersWithLessParticles.draw(GL_POINTS, shaderProgramParticlesPointSprite);

        glUniform1i(locRowCountParticlesPointSprite, 1);
        glUniform1i(locColumnCountParticlesPointSprite, 1);
        glUniform1i(locSolid, 4);
        testTexture.bind(shaderProgramParticlesPointSprite, "textureSampler", 0);
        buffersWithLessParticles.draw(GL_POINTS, shaderProgramParticlesPointSprite);

        glUniform1i(locPointSize, 350);
        glUniform1i(locRowCountParticlesPointSprite, 8);
        glUniform1i(locColumnCountParticlesPointSprite, 8);
        glUniform1i(locSolid, 5);
        smokeTexture.bind(shaderProgramParticlesPointSprite, "textureSampler", 0);
        buffersWithLessParticles.draw(GL_POINTS, shaderProgramParticlesPointSprite);

        glUniform1i(locPointSize, 15);
        glUniform1i(locRowCountParticlesPointSprite, 4);
        glUniform1i(locColumnCountParticlesPointSprite, 4);
        glUniform1i(locSolid, 6);
        particleAtlasTexture.bind(shaderProgramParticlesPointSprite, "textureSampler", 0);
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
                    case GLFW_KEY_E -> cubeMode = !cubeMode;
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
                                    "\n E - Change GS behavior - Cubes / Quads" +
                                    "\n W,A,S,D,R,F - Movement - forward, left, backward, right, up, down",
                            "Help",
                            JOptionPane.INFORMATION_MESSAGE);

                }
            }
        }
    };
}
