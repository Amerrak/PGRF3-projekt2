package main;

import lwjglutils.*;
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
    private float time;

    private final List<Integer> POLYGON_MODES = Arrays.asList(GL_FILL, GL_LINE, GL_POINT);

    private int polygonMode;
    private int debugMode;

    private int shaderProgramViewer, shaderProgramLight;
    private int locView, locProjection, locSolid, locLightPosition, locEyePosition, locLightVP, locTime, locTextureMode, locAmbientEnabled;
    private int locDiffuseEnabled, locSpecularEnabled, locQuadraticAttenuation, locSpotDirection, locSpotCutOff, locRotation, locReflectorDisabled, locDebugMode;
    private int locViewLight, locProjectionLight, locSolidLight, locTimeLight, locRotationLight;

    private boolean ambientEnabled = true;
    private boolean diffuseEnabled = true;
    private boolean specularEnabled = true;
    private boolean flatPlane = true;
    private boolean perspectiveProjection = true;
    private boolean textureMode = true;
    private boolean animationEnabled = true;
    private boolean showCylindricalObject2;
    private boolean showSphericalObject2;
    private boolean reflectorDisabled;
    private boolean mousePressed;

    private OGLBuffers buffers;
    private OGLRenderTarget renderTarget;
    private OGLTexture2D defaultTexture;
    private OGLTexture2D donutTexture;
    private OGLTexture2D waterTexture;
    private OGLTexture.Viewer viewer;
    private Camera camera, cameraLight;

    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        shaderProgramViewer = ShaderUtils.loadProgram("/start");
        shaderProgramLight = ShaderUtils.loadProgram("/light");

        locView = glGetUniformLocation(shaderProgramViewer, "view");
        locProjection = glGetUniformLocation(shaderProgramViewer, "projection");
        locSolid = glGetUniformLocation(shaderProgramViewer, "solid");
        locLightPosition = glGetUniformLocation(shaderProgramViewer, "lightPosition");
        locEyePosition = glGetUniformLocation(shaderProgramViewer, "eyePosition");
        locLightVP = glGetUniformLocation(shaderProgramViewer, "lightVP");
        locTime = glGetUniformLocation(shaderProgramViewer, "time");
        locTextureMode = glGetUniformLocation(shaderProgramViewer, "textureMode");
        locAmbientEnabled = glGetUniformLocation(shaderProgramViewer, "ambientEnabled");
        locDiffuseEnabled = glGetUniformLocation(shaderProgramViewer, "diffuseEnabled");
        locSpecularEnabled = glGetUniformLocation(shaderProgramViewer, "specularEnabled");
        locQuadraticAttenuation = glGetUniformLocation(shaderProgramViewer, "quadraticAttenuation");
        locSpotDirection = glGetUniformLocation(shaderProgramViewer, "spotDirection");
        locSpotCutOff = glGetUniformLocation(shaderProgramViewer, "spotCutOff");
        locRotation = glGetUniformLocation(shaderProgramViewer, "rotation");
        locReflectorDisabled = glGetUniformLocation(shaderProgramViewer, "reflectorDisabled");
        locDebugMode = glGetUniformLocation(shaderProgramViewer, "debugMode");

        locViewLight = glGetUniformLocation(shaderProgramLight, "view");
        locProjectionLight = glGetUniformLocation(shaderProgramLight, "projection");
        locSolidLight = glGetUniformLocation(shaderProgramLight, "solid");
        locTimeLight = glGetUniformLocation(shaderProgramLight, "time");
        locRotationLight = glGetUniformLocation(shaderProgramLight, "rotation");

        buffers = GridFactory.createGrid(200, 200);
        renderTarget = new OGLRenderTarget(1024, 1024);
        viewer = new OGLTexture2D.Viewer();

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
            defaultTexture = new OGLTexture2D("textures/default.jpg");
            waterTexture = new OGLTexture2D("textures/water.jpg");
            donutTexture = new OGLTexture2D("textures/donut.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void display() {
        glEnable(GL_DEPTH_TEST);

        if (animationEnabled) {
            time += 0.01f;
        }

        glPolygonMode(GL_FRONT_AND_BACK, POLYGON_MODES.get(polygonMode % 3));
        renderFromLight();
        renderFromViewer();

        viewer.view(renderTarget.getColorTexture(), -1.0, -1.0, 0.7);
        viewer.view(renderTarget.getDepthTexture(), -1.0, -0.3, 0.7);
        textRenderer.addStr2D(width - 150, height - 3, "H for help, DebugMode = " + debugMode % 6);
    }

    private void renderFromLight() {
        glUseProgram(shaderProgramLight);
        renderTarget.bind();
        glClearColor(0.5f, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUniformMatrix4fv(locViewLight, false, cameraLight.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjectionLight, false, projection.floatArray());
        glUniform1f(locTimeLight, time);

        drawObjects(locSolidLight, locRotationLight, shaderProgramLight, false);
    }

    private void renderFromViewer() {
        glUseProgram(shaderProgramViewer);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glViewport(0, 0, width, height);

        glClearColor(0f, 0.5f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUniformMatrix4fv(locView, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjection, false, projection.floatArray());
        glUniformMatrix4fv(locLightVP, false, cameraLight.getViewMatrix().mul(projection).floatArray());
        glUniform1i(locTextureMode, textureMode ? GL_TRUE : GL_FALSE);
        glUniform1i(locAmbientEnabled, ambientEnabled ? GL_TRUE : GL_FALSE);
        glUniform1i(locDiffuseEnabled, diffuseEnabled ? GL_TRUE : GL_FALSE);
        glUniform1i(locSpecularEnabled, specularEnabled ? GL_TRUE : GL_FALSE);
        glUniform1i(locReflectorDisabled, reflectorDisabled ? GL_TRUE : GL_FALSE);
        glUniform1i(locDebugMode, debugMode % 6);

        glUniform1f(locQuadraticAttenuation, 0.01f);
        glUniform1f(locSpotCutOff, 0.95f);
        glUniform3f(locSpotDirection, (float) cameraLight.getViewVector().getX(), (float) cameraLight.getViewVector().getY(), (float) cameraLight.getViewVector().getZ());

        glUniform3fv(locLightPosition, ToFloatArray.convert(cameraLight.getPosition()));
        glUniform3fv(locEyePosition, ToFloatArray.convert(camera.getEye()));
        glUniform1f(locTime, time);

        renderTarget.getDepthTexture().bind(shaderProgramViewer, "depthTexture", 1);
        defaultTexture.bind(shaderProgramViewer, "default", 0);

        drawObjects(locSolid, locRotation, shaderProgramViewer, true);
    }

    private void drawObjects(int solidLoc, int rotationLoc, int shaderProgram, boolean bindTexture) {
        if (!flatPlane) {
            if (bindTexture) {
                waterTexture.bind(shaderProgramViewer, "water", 0);
            }
            glUniform1i(solidLoc, 2);
        } else {
            glUniform1i(solidLoc, 1);
        }
        buffers.draw(GL_TRIANGLES, shaderProgram);
        if (bindTexture) {
            donutTexture.bind(shaderProgramViewer, "donut", 0);
        }
        glUniformMatrix4fv(rotationLoc, false, new Mat4RotY(time).floatArray());
        glUniform1i(solidLoc, 3);
        buffers.draw(GL_TRIANGLES, shaderProgram);
        glUniformMatrix4fv(rotationLoc, false, new Mat4Identity().floatArray());
        if (bindTexture) {
            defaultTexture.bind(shaderProgramViewer, "default", 0);
        }

        glUniform1i(solidLoc, 4);
        buffers.draw(GL_TRIANGLES, shaderProgram);

        if (showSphericalObject2) {
            glUniform1i(solidLoc, 5);
            buffers.draw(GL_TRIANGLES, shaderProgram);

        }

        if (showCylindricalObject2) {
            glUniform1i(solidLoc, 6);
            buffers.draw(GL_TRIANGLES, shaderProgram);
        }
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
                    case GLFW_KEY_E -> polygonMode++;
                    case GLFW_KEY_Q -> textureMode = !textureMode;
                    case GLFW_KEY_G -> animationEnabled = !animationEnabled;
                    case GLFW_KEY_1 -> ambientEnabled = !ambientEnabled;
                    case GLFW_KEY_2 -> diffuseEnabled = !diffuseEnabled;
                    case GLFW_KEY_3 -> specularEnabled = !specularEnabled;
                    case GLFW_KEY_4 -> flatPlane = !flatPlane;
                    case GLFW_KEY_5 -> showCylindricalObject2 = !showCylindricalObject2;
                    case GLFW_KEY_6 -> showSphericalObject2 = !showSphericalObject2;
                    case GLFW_KEY_V -> cameraLight = cameraLight.addAzimuth(0.1);
                    case GLFW_KEY_C -> cameraLight = cameraLight.addAzimuth(-0.1);

                    case GLFW_KEY_I -> cameraLight = cameraLight.forward(0.1);
                    case GLFW_KEY_K -> cameraLight = cameraLight.backward(0.1);
                    case GLFW_KEY_J -> cameraLight = cameraLight.left(0.1);
                    case GLFW_KEY_L -> cameraLight = cameraLight.right(0.1);
                    case GLFW_KEY_T -> reflectorDisabled = !reflectorDisabled;
                    case GLFW_KEY_B -> debugMode++;
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
