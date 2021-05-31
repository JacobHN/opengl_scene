/**
 * creates a 3d virtual world using objects created in blender and objects created explicitly with vertexs that
 * can be traversed through using the wasdeq and up down left right arrow keys. Adds light and shadow to each object
 * that can cast a shadow on each other. Uses tesselation, bump mapping, fog and transparency, 3D textures, environment mapping.
 * @author Jacob Hua
 * @version 1.0
 * @since 2021-04-15
 *
 */
package a4;

import a4.objects.CubeHead;
import a4.objects.SkyBox;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.texture.*;
import org.joml.*;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.*;
import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import com.jogamp.opengl.*;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES2.*;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_COMPARE_FUNC;
import static com.jogamp.opengl.GL2GL3.*;
import static com.jogamp.opengl.GL3ES3.GL_PATCHES;
import static com.jogamp.opengl.GL3ES3.GL_PATCH_VERTICES;


public class Starter extends JFrame implements GLEventListener, KeyListener, MouseListener, MouseWheelListener, MouseInputListener {

    private GLCanvas myCanvas;

    private int renderingProgram;
    private int axesRenderingProgram;
    private int shadowRenderingProgram;
    private int skyBoxRenderingProgram;
    private int flatRenderingProgram;
    private int tesselationRenderingProgram;
    private int tesselationShadowRendingProgram;


    private int vao[] = new int[1];
    private int vbo[] = new int[50];
    private float cameraX, cameraY, cameraZ;

    //world matrices
    private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
    private Matrix4fStack mvStack = new Matrix4fStack(8);
    private Matrix4f pMat = new Matrix4f();
    private Matrix4f vMat = new Matrix4f();
    private Matrix4f mMat = new Matrix4f();

    private Matrix4f invTrMat = new Matrix4f();

    private Matrix4f texRotMat = new Matrix4f();
    private Matrix4f origTexRotMat = new Matrix4f();


    private int mvLoc;
    private int projLoc;
    private int nLoc;
    private int sLoc;
    private float aspect;

    private int mvSkyLoc;
    private int projSkyLoc;

    private int mvFlatLoc;
    private int projFlatLoc;

    private int texRotLoc;

    private int alphaLoc;
    private int flipLoc;

    //texture
    private int mushroomTexture;
    private int floorTexture;
    private int whiteTexture;
    private int treeTexture;
    private int skyTexture;
    private int skyBoxTexture;
    WoodNoiseMapping woodNoise;
    private int woodTexture;
    private int logTexture;
    private int axeTexture;
    private int axeHandleTexture;
    private int ghostTexture;

    //tesselation textures
    private int tessTexture;
    private int tessNormalMap;
    private int tessHeightMap;

    //model
    private ImportedModel mushroom;
    private CubeHead cubeHead;
    private ImportedModel grid;
    private ImportedModel tree;
    private SkyBox skyBox;

    private ImportedModel log;
    private ImportedModel axe;
    private ImportedModel axeHandle;
    private ImportedModel ghost;


    //time variables
    private long elapsedTime;
    private long startTime;
    private long currentTime;
    private float tf;

    //camera variables
    Camera camera;

    //light variables
    private float amt = 0.0f;
    private int globalAmbLoc, ambLoc, diffLoc,
            specLoc, posLoc, mambLoc, mdiffLoc, mspecLoc, mshiLoc;
    private Vector3f currentLightPos = new Vector3f();
    private float[] lightPos = new float[3];

    float[] globalAmbient = { 0.6f, 0.6f, 0.6f, 1.0f };
    float[] lightAmbient = { 0.1f, 0.1f, 0.1f, 1.0f };
    float[] lightDiffuse = { 1.0f, 1.0f, 1.0f, 1.0f };
    float[] lightSpecular = { 1.0f, 1.0f, 1.0f, 1.0f };

    private Vector3f initialLightLoc = new Vector3f(5.0f, 3.0f, 2.0f);


    //material variables
    float[] matAmb = Utils.silverAmbient();
    float[] matDif = Utils.silverDiffuse();
    float[] matSpe = Utils.silverSpecular();
    float matShi = Utils.silverShininess();

    //shadow variables
    private int scSizeX, scSizeY;
    private int [] shadowTex = new int[1];
    private int [] shadowBuffer = new int[1];
    private Matrix4f lightVmat = new Matrix4f();
    private Matrix4f lightPmat = new Matrix4f();
    private Matrix4f shadowMVP1 = new Matrix4f();
    private Matrix4f shadowMVP2 = new Matrix4f();
    private Matrix4f b = new Matrix4f();

    Vector3f origin = new Vector3f(0,0,0);
    Vector3f up = new Vector3f(0,1,0);

    private Matrix4fStack shadowMVPStack1 = new Matrix4fStack(8);
    private Matrix4fStack shadowMVPStack2 = new Matrix4fStack(8);

    //boolean variables
    boolean axisFlag = true;
    int globalAmbSwitch = 0;
    int reflectionSwitch = 0;
    int bumpSwitch = 0;
    int tex3DSwitch = 0;

    //light movement variables
    int originalDraggedPositionX;
    int originalDraggedPositionY;


    //object locations
    Vector3f tree1Loc = new Vector3f(.8f, 0.0f, 0.0f);
    Vector3f tree2Loc = new Vector3f(2.5f, 0.0f,0.8f);
    Vector3f tree3Loc = new Vector3f(-1.5f, 0.0f, -0.8f);
    Vector3f tree4Loc = new Vector3f(-2.0f, 0.0f, 2.0f);
    Vector3f mushroom1Loc = new Vector3f(0.0f, 0.0f, 0.0f);
    Vector3f mushroom1Scale = new Vector3f(0.05f, 0.05f, 0.05f);
    Vector3f mushroom2Loc = new Vector3f(0.2f, 0.0f, 0.3f);
    Vector3f mushroom2Scale = new Vector3f(0.05f, 0.05f, 0.05f);
    Vector3f floorLoc = new Vector3f(0.0f, 0.0f, 0.0f);
    Vector3f floorScale = new Vector3f(20.0f, 20.0f, 20.0f);
    Vector3f ghostLoc = new Vector3f(1.5f, 1.5f, 1.5f);
    Vector3f ghostScale = new Vector3f(0.25f, 0.25f,0.25f);
    Vector3f logLoc = new Vector3f(-2.0f,0.0f,2.0f);
    Vector3f logScale = new Vector3f(0.4f, 0.4f, 0.4f);
    Vector3f axeLoc = new Vector3f(0.0f,1.0f,0.0f);
    Vector3f axeScale = new Vector3f(0.6f, 0.6f, 0.6f);
    Vector3f axeHandleLoc = new Vector3f(-2.0f,1.0f,2.3f);;
    Vector3f axeHandleScale = new Vector3f(0.6f, 0.6f, 0.6f);




    /**
     * constructor
     */
    public Starter() {
        setTitle("assignment 4");
        setSize(1000,1000);
        myCanvas = new GLCanvas();
        myCanvas.addGLEventListener(this);
        myCanvas.addKeyListener(this);
        myCanvas.addMouseListener(this);
        myCanvas.addMouseWheelListener(this);
        myCanvas.addMouseMotionListener(this);
        this.add(myCanvas);
        this.setVisible(true);
        Animator animator = new Animator(myCanvas);
        animator.start();
    }

    /**
     * initializes program
     * @param glAutoDrawable
     */
    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        //pass 2 render
        renderingProgram = Utils.createShaderProgram("a4/shaders/vertShaderPassTwo.glsl", "a4/shaders/fragShaderPassTwo.glsl");
        //axes render
        axesRenderingProgram = Utils.createShaderProgram("a4/shaders/axesVertShader.glsl", "a4/shaders/axesFragShader.glsl");
        //pass 1 render
        shadowRenderingProgram = Utils.createShaderProgram("a4/shaders/vertShaderPassOne.glsl", "a4/shaders/fragShaderPassOne.glsl");
        //skybox and lightbox render
        skyBoxRenderingProgram = Utils.createShaderProgram("a4/shaders/vertCShader.glsl", "a4/shaders/fragCShader.glsl");
        //no light rendering
        flatRenderingProgram = Utils.createShaderProgram("a4/shaders/vertShader.glsl","a4/shaders/fragShader.glsl");

        //tesselation render
        tesselationRenderingProgram = Utils.createShaderProgram(
                "a4/shaders/tesselation/passTwo/vertShaderPassTwo.glsl",
                "a4/shaders/tesselation/passTwo/tessCShader.glsl",
                "a4/shaders/tesselation/passTwo/tessEShader.glsl",
                "a4/shaders/tesselation/passTwo/fragShaderPassTwo.glsl"
        );
        //tesselation shadow render
        tesselationShadowRendingProgram = Utils.createShaderProgram(
                "a4/shaders/tesselation/passOne/vertShaderPassOne.glsl",
                "a4/shaders/tesselation/passOne/tessCShaderPassOne.glsl",
                "a4/shaders/tesselation/passOne/tessEShaderPassOne.glsl",
                "a4/shaders/tesselation/passOne/fragShaderPassOne.glsl"
        );

        startTime = System.currentTimeMillis();

        cameraX = 0.0f; cameraY = 4.0f; cameraZ = 12.0f;
        setupVertices();
        setupShadowBuffers();
        //camera position and angle variables
        camera = new Camera(cameraX, cameraY, cameraZ);

        //load textures
        mushroomTexture = Utils.loadTexture("a4/objects/mushroom_color.png");
        floorTexture = Utils.loadTexture("a4/objects/grass.jpg");
        whiteTexture = Utils.loadTexture("a4/objects/white.png");
        treeTexture = Utils.loadTexture("a4/objects/tree_texture2.png");
        skyTexture = Utils.loadTexture("a4/objects/cloudSkyBox.jpg");
        skyBoxTexture = Utils.loadCubeMap("a4/objects/cubeMap");
        gl.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
        ghostTexture = Utils.loadTexture("a4/objects/ghost_texture.png");
        axeTexture = Utils.loadTexture("a4/objects/axe_texture.png");

        //tess textures
        tessTexture = Utils.loadTexture("a4/objects/grass.png");
        tessHeightMap = Utils.loadTexture("a4/objects/grassHeight.jpg");
        tessNormalMap = Utils.loadTexture("a4/objects/grassNormal.jpg");


        //load 3d texture
        woodNoise = new WoodNoiseMapping();
        woodTexture = woodNoise.getWoodTexture();
        texRotMat.rotateY((float)Math.toRadians(2.5f));
        texRotMat.rotateX((float)Math.toRadians(2.5f));
        texRotMat.rotateZ((float)Math.toRadians(92.5f));
        origTexRotMat = texRotMat;

        b.set(
                0.5f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.5f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f);


        //print out system aOpenGL, JOGL and JAVA
        System.out.println("OpenGL version: " +  gl.glGetString(GL_VERSION));
        System.out.println("JOGL version: " + Package.getPackage("com.jogamp.opengl").getImplementationVersion());
        System.out.println("Java version: " + System.getProperty("java.version"));
    }



    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {}

    /**
     * displays objects onto screen at clock speed
     * @param glAutoDrawable
     */
    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glClear(GL_DEPTH_BUFFER_BIT);
        gl.glClear(GL_COLOR_BUFFER_BIT);


        currentLightPos.set(initialLightLoc);
        lightVmat.identity().setLookAt(currentLightPos, origin, up);
        lightPmat.identity().setPerspective((float) Math.toRadians(90.0f), aspect, 0.1f, 1000.0f);

        gl.glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer[0]);
        gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowTex[0], 0);

        gl.glDrawBuffer(GL_NONE);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(GL_POLYGON_OFFSET_FILL);
        gl.glPolygonOffset(3.0f, 5.0f);

        passOne();

        gl.glDisable(GL_POLYGON_OFFSET_FILL);

        gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl.glActiveTexture(GL_TEXTURE5);
        gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);

        gl.glDrawBuffer(GL_FRONT);

        passTwo();

    }

    /**
     * first render of the scene to create shadow (only creates verts for shadow position)
     */
    private void passOne(){
        GL4 gl = (GL4) GLContext.getCurrentGL();

        gl.glUseProgram(shadowRenderingProgram);
        shadowMVPStack1.identity();
        mMat.identity();

        shadowMVPStack1.pushMatrix();
        shadowMVPStack1.mul(lightPmat);
        shadowMVPStack1.mul(lightVmat);
        shadowMVPStack1.mul(mMat);

        //mushroom shadow
        shadowMVPStack1.pushMatrix();
        shadowMVPStack1.translate(mushroom1Loc);
        shadowMVPStack1.pushMatrix();//mushroom scale
        shadowMVPStack1.scale(mushroom1Scale);
        sLoc = gl.glGetUniformLocation(shadowRenderingProgram, "shadowMVP");
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVPStack1.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glClear(GL_DEPTH_BUFFER_BIT);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CCW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LEQUAL);

        gl.glDrawArrays(GL_TRIANGLES, 0, mushroom.getNumVertices());

        shadowMVPStack1.popMatrix();
        shadowMVPStack1.popMatrix();

        //mushroom 2 shadow
        shadowMVPStack1.pushMatrix();
        shadowMVPStack1.translate(mushroom2Loc);
        shadowMVPStack1.pushMatrix();//mushroom scale
        shadowMVPStack1.scale(mushroom1Scale);
        sLoc = gl.glGetUniformLocation(shadowRenderingProgram, "shadowMVP");
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVPStack1.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CCW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LEQUAL);

        gl.glDrawArrays(GL_TRIANGLES, 0, mushroom.getNumVertices());

        shadowMVPStack1.popMatrix();
        shadowMVPStack1.popMatrix();

        //tree 1 shadow
        shadowMVPStack1.pushMatrix();
        shadowMVPStack1.translate(tree1Loc);
        shadowMVPStack1.pushMatrix();//mushroom scale
        shadowMVPStack1.scale(0.5f, 0.5f, 0.5f);
        sLoc = gl.glGetUniformLocation(shadowRenderingProgram, "shadowMVP");
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVPStack1.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CCW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LEQUAL);

        gl.glDrawArrays(GL_TRIANGLES, 0, tree.getNumVertices());

        shadowMVPStack1.popMatrix();
        shadowMVPStack1.popMatrix();

        //tree 2 shadow
        shadowMVPStack1.pushMatrix();
        shadowMVPStack1.translate(tree2Loc);
        shadowMVPStack1.pushMatrix();//mushroom scale
        shadowMVPStack1.scale(0.5f, 0.5f, 0.5f);
        sLoc = gl.glGetUniformLocation(shadowRenderingProgram, "shadowMVP");
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVPStack1.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CCW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LEQUAL);

        gl.glDrawArrays(GL_TRIANGLES, 0, tree.getNumVertices());

        shadowMVPStack1.popMatrix();
        shadowMVPStack1.popMatrix();

        //tree 3 shadow
        shadowMVPStack1.pushMatrix();
        shadowMVPStack1.translate(tree3Loc);
        shadowMVPStack1.pushMatrix();//mushroom scale
        shadowMVPStack1.scale(0.5f, 0.5f, 0.5f);
        sLoc = gl.glGetUniformLocation(shadowRenderingProgram, "shadowMVP");
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVPStack1.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CCW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LEQUAL);

        gl.glDrawArrays(GL_TRIANGLES, 0, tree.getNumVertices());

        shadowMVPStack1.popMatrix();
        shadowMVPStack1.popMatrix();

        //log shadow
        shadowMVPStack1.pushMatrix();//log translate
        shadowMVPStack1.translate(logLoc);
        shadowMVPStack1.pushMatrix();//log scale
        shadowMVPStack1.scale(logScale);
        sLoc = gl.glGetUniformLocation(shadowRenderingProgram, "shadowMVP");
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVPStack1.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CCW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LEQUAL);

        gl.glDrawArrays(GL_TRIANGLES, 0, log.getNumVertices());

        shadowMVPStack1.popMatrix();//log translate
        shadowMVPStack1.popMatrix();//log scale

        //axe shadow
        shadowMVPStack1.pushMatrix();//axe translation
        shadowMVPStack1.translate(axeHandleLoc);
        shadowMVPStack1.pushMatrix();//axe rotate
        shadowMVPStack1.rotate((float)Math.toRadians(230),1f, 0f, 0f);
        shadowMVPStack1.pushMatrix();//axe scale
        shadowMVPStack1.scale(axeHandleScale);

        sLoc = gl.glGetUniformLocation(shadowRenderingProgram, "shadowMVP");
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVPStack1.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER,vbo[20]);
        gl.glVertexAttribPointer(0,3,GL_FLOAT,false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CCW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LEQUAL);

        gl.glDrawArrays(GL_TRIANGLES, 0, axeHandle.getNumVertices());

        //axe head model
        shadowMVPStack1.pushMatrix();
        shadowMVPStack1.translate(axeLoc);

        sLoc = gl.glGetUniformLocation(shadowRenderingProgram, "shadowMVP");
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVPStack1.get(vals));


        gl.glBindBuffer(GL_ARRAY_BUFFER,vbo[17]);
        gl.glVertexAttribPointer(0,3,GL_FLOAT,false, 0, 0);
        gl.glEnableVertexAttribArray(0);


        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CCW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LEQUAL);

        gl.glDrawArrays(GL_TRIANGLES, 0, axe.getNumVertices());

        shadowMVPStack1.popMatrix();//axe head translate

        shadowMVPStack1.popMatrix();//axe translate
        shadowMVPStack1.popMatrix();//axe rotate
        shadowMVPStack1.popMatrix();//axe scale


        //tesselation shadow
        gl.glUseProgram(tesselationShadowRendingProgram);

        shadowMVPStack1.pushMatrix();//tesselation
        shadowMVPStack1.translate(floorLoc);
        shadowMVPStack1.pushMatrix();//scale
        shadowMVPStack1.scale(floorScale);


        sLoc = gl.glGetUniformLocation(tesselationShadowRendingProgram, "shadowMVP");
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVPStack1.get(vals));

        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, tessTexture);
        gl.glActiveTexture(GL_TEXTURE1);
        gl.glBindTexture(GL_TEXTURE_2D, tessHeightMap);
        gl.glActiveTexture(GL_TEXTURE2);
        gl.glBindTexture(GL_TEXTURE_2D, tessNormalMap);

        //gl.glClear(GL_DEPTH_BUFFER_BIT);
        //gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LEQUAL);

        gl.glPatchParameteri(GL_PATCH_VERTICES, 4);
        gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        gl.glDrawArraysInstanced(GL_PATCHES, 0, 4, 64*64);

        gl.glFrontFace(GL_CCW);

        shadowMVPStack1.popMatrix();//tesselation
        shadowMVPStack1.popMatrix();//scale

        shadowMVPStack1.popMatrix();



    }

    /**
     * creates scene with shadow (second render of the scene)
     */
    private void passTwo(){
        GL4 gl = (GL4) GLContext.getCurrentGL();


        elapsedTime = System.currentTimeMillis() - startTime;
        tf = (float)elapsedTime/1000;

        gl.glUseProgram(renderingProgram);

        //3d texture switch
        int tex3DBool = gl.glGetUniformLocation(renderingProgram, "tex3DBool");
        gl.glProgramUniform1i(renderingProgram, tex3DBool, tex3DSwitch);


        //reflection switch
        int reflectionBool = gl.glGetUniformLocation(renderingProgram, "reflective");
        gl.glProgramUniform1i(renderingProgram, reflectionBool, reflectionSwitch);
        gl.glActiveTexture(GL_TEXTURE3);
        gl.glBindTexture(GL_TEXTURE_CUBE_MAP, skyBoxTexture);

        //bump switch
        int bumpBool = gl.glGetUniformLocation(renderingProgram, "bump");
        gl.glProgramUniform1i(renderingProgram, bumpBool, globalAmbSwitch);

        //global ambient switch
        int globalAmbBool = gl.glGetUniformLocation(renderingProgram, "onlyGlobalAmbient");
        gl.glProgramUniform1i(renderingProgram, globalAmbBool, globalAmbSwitch);

        globalAmbBool = gl.glGetUniformLocation(tesselationRenderingProgram, "onlyGlobalAmbient");
        gl.glProgramUniform1i(tesselationRenderingProgram, globalAmbBool, globalAmbSwitch);


        mvLoc = gl.glGetUniformLocation(renderingProgram, "mv_matrix");
        projLoc = gl.glGetUniformLocation(renderingProgram, "proj_matrix");
        nLoc = gl.glGetUniformLocation(renderingProgram, "norm_matrix");
        sLoc = gl.glGetUniformLocation(renderingProgram, "shadowMVP");
        texRotLoc = gl.glGetUniformLocation(renderingProgram, "texRot_matrix");
        alphaLoc = gl.glGetUniformLocation(renderingProgram, "alpha");
        flipLoc = gl.glGetUniformLocation(renderingProgram, "flipNormal");

        gl.glProgramUniform1f(renderingProgram, alphaLoc, 1.0f);
        gl.glProgramUniform1f(renderingProgram, flipLoc, 1.0f);


        aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
        pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
        gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));

        //rotation matrix
        camera.updateCameraRotation();
        //position matrix
        camera.updateCameraPosition();

        vMat = camera.vMatrix();
        mMat.identity();

        mvStack.pushMatrix();//pushes camera onto matrix
        mvStack.identity();
        mvStack.mul(vMat);
        mvStack.mul(mMat);

        mvStack.invert(invTrMat);
        invTrMat.transpose(invTrMat);

        gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
        gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
        gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));


        //objects not affected by lighting
        gl.glUseProgram(skyBoxRenderingProgram);
        mvSkyLoc = gl.glGetUniformLocation(skyBoxRenderingProgram, "mv_matrix");
        projSkyLoc = gl.glGetUniformLocation(skyBoxRenderingProgram, "proj_matrix");

        //skybox
        mvStack.pushMatrix();//
        mvStack.setTranslation(camera.getCameraPosition().m03(),camera.getCameraPosition().m13(),camera.getCameraPosition().m23());

        gl.glUniformMatrix4fv(mvSkyLoc, 1, false, mvStack.get(vals));
        gl.glUniformMatrix4fv(projSkyLoc, 1, false, pMat.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER,vbo[12]);
        gl.glVertexAttribPointer(0,3,GL_FLOAT,false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
        gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);

        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_CUBE_MAP, skyBoxTexture);

        gl.glEnable(GL_CULL_FACE);
        gl.glEnable(GL_CCW);
        gl.glDisable(GL_DEPTH_TEST);
        gl.glDrawArrays(GL_TRIANGLES, 0, 36);
        gl.glEnable(GL_DEPTH_TEST);

        mvStack.popMatrix();

        //gl.glUseProgram(renderingProgram);

        gl.glUseProgram(flatRenderingProgram);
        mvFlatLoc = gl.glGetUniformLocation(flatRenderingProgram, "mv_matrix");
        projFlatLoc = gl.glGetUniformLocation(flatRenderingProgram, "proj_matrix");

        //light cube
        mvStack.pushMatrix();//light cube translation
        mvStack.translate(currentLightPos);
        mvStack.pushMatrix();//light cube scale
        mvStack.scale(0.1f, 0.1f, 0.1f);
        gl.glUniformMatrix4fv(mvFlatLoc, 1, false, mvStack.get(vals));
        gl.glUniformMatrix4fv(projFlatLoc, 1, false, pMat.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER,vbo[9]);
        gl.glVertexAttribPointer(0,3,GL_FLOAT,false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
        gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);

        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, whiteTexture);

        gl.glEnable(GL_CULL_FACE);
        gl.glEnable(GL_CCW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(GL_LEQUAL);

        gl.glDrawArrays(GL_TRIANGLES, 0, 36);

        mvStack.popMatrix();
        mvStack.popMatrix();

        gl.glUseProgram(renderingProgram);

        //install lighting into stack
        currentLightPos.mulPosition(vMat);
        lightPos[0]=currentLightPos.x();
        lightPos[1]=currentLightPos.y();
        lightPos[2]=currentLightPos.z();
        installLight(renderingProgram, mvStack);

        //shadow creation
        shadowMVPStack2.identity();
        shadowMVPStack2.mul(b);
        shadowMVPStack2.mul(lightPmat);
        shadowMVPStack2.mul(lightVmat);
        shadowMVPStack2.mul(mMat);

        changeToGoldMaterial();
        //creates mushroom
        mvStack.pushMatrix();//mushroom translation
        mvStack.translate(mushroom1Loc);
        mvStack.pushMatrix();//mushroom scale
        mvStack.scale(mushroom1Scale);

        shadowMVPStack2.pushMatrix();//mushroom translation
        shadowMVPStack2.translate(mushroom1Loc);
        shadowMVPStack2.pushMatrix();//mushroom scale
        shadowMVPStack2.scale(mushroom1Scale);

        mvStack.invert(invTrMat);
        invTrMat.transpose(invTrMat);

        gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
        gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVPStack2.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER,vbo[0]);
        gl.glVertexAttribPointer(0,3,GL_FLOAT,false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
        gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(2);

        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, mushroomTexture);


        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(GL_LEQUAL);
        gl.glEnable(GL_CULL_FACE);

        gl.glDrawArrays(GL_TRIANGLES, 0, mushroom.getNumVertices());

        mvStack.popMatrix(); //mushroom scale
        mvStack.popMatrix(); //mushroom translation

        shadowMVPStack2.popMatrix();
        shadowMVPStack2.popMatrix();

        //creates mushroom 2
        mvStack.pushMatrix();//mushroom translation
        mvStack.translate(mushroom2Loc);
        mvStack.pushMatrix();//mushroom scale
        mvStack.scale(mushroom2Scale);

        shadowMVPStack2.pushMatrix();//mushroom translation
        shadowMVPStack2.translate(mushroom2Loc);
        shadowMVPStack2.pushMatrix();//mushroom scale
        shadowMVPStack2.scale(mushroom2Scale);

        mvStack.invert(invTrMat);
        invTrMat.transpose(invTrMat);

        gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
        gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVPStack2.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER,vbo[0]);
        gl.glVertexAttribPointer(0,3,GL_FLOAT,false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
        gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(2);

        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, mushroomTexture);


        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(GL_LEQUAL);
        gl.glEnable(GL_CULL_FACE);

        gl.glDrawArrays(GL_TRIANGLES, 0, mushroom.getNumVertices());

        mvStack.popMatrix(); //mushroom scale
        mvStack.popMatrix(); //mushroom translation

        shadowMVPStack2.popMatrix();
        shadowMVPStack2.popMatrix();

        bumpSwitch = 1;
        gl.glProgramUniform1i(renderingProgram, bumpBool, bumpSwitch);
        changeToSilverMaterial();
        //tree model
        mvStack.pushMatrix();//tree translation
        mvStack.translate(tree1Loc);
        mvStack.pushMatrix();//mushroom scale
        mvStack.scale(0.5f, 0.5f, 0.5f);

        shadowMVPStack2.pushMatrix();//tree translation
        shadowMVPStack2.translate(tree1Loc);
        shadowMVPStack2.pushMatrix();//tree scale
        shadowMVPStack2.scale(0.5f, 0.5f, 0.5f);

        mvStack.invert(invTrMat);
        invTrMat.transpose(invTrMat);

        gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
        gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVPStack2.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER,vbo[6]);
        gl.glVertexAttribPointer(0,3,GL_FLOAT,false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
        gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
        gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(2);

        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, treeTexture);


        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(GL_LEQUAL);
        gl.glEnable(GL_CULL_FACE);

        gl.glDrawArrays(GL_TRIANGLES, 0, tree.getNumVertices());

        mvStack.popMatrix(); //tree scale
        mvStack.popMatrix(); //tree translation

        shadowMVPStack2.popMatrix();
        shadowMVPStack2.popMatrix();

        bumpSwitch = 0;
        gl.glProgramUniform1i(renderingProgram, bumpBool, bumpSwitch);

        bumpSwitch = 1;
        gl.glProgramUniform1i(renderingProgram, bumpBool, bumpSwitch);
        //tree model 2
        mvStack.pushMatrix();//tree translation
        mvStack.translate(tree2Loc);
        mvStack.pushMatrix();//mushroom scale
        mvStack.scale(0.5f, 0.5f, 0.5f);

        shadowMVPStack2.pushMatrix();//tree translation
        shadowMVPStack2.translate(tree2Loc);
        shadowMVPStack2.pushMatrix();//tree scale
        shadowMVPStack2.scale(0.5f, 0.5f, 0.5f);

        mvStack.invert(invTrMat);
        invTrMat.transpose(invTrMat);

        gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
        gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVPStack2.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER,vbo[6]);
        gl.glVertexAttribPointer(0,3,GL_FLOAT,false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
        gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
        gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(2);

        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, treeTexture);

        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(GL_LEQUAL);
        gl.glEnable(GL_CULL_FACE);

        gl.glDrawArrays(GL_TRIANGLES, 0, tree.getNumVertices());

        mvStack.popMatrix(); //tree scale
        mvStack.popMatrix(); //tree translation

        shadowMVPStack2.popMatrix();
        shadowMVPStack2.popMatrix();

        bumpSwitch = 0;
        gl.glProgramUniform1i(renderingProgram, bumpBool, bumpSwitch);

        bumpSwitch = 1;
        gl.glProgramUniform1i(renderingProgram, bumpBool, bumpSwitch);
        //tree model 3
        mvStack.pushMatrix();//tree translation
        mvStack.translate(tree3Loc);
        mvStack.pushMatrix();//tree scale
        mvStack.scale(0.5f, 0.5f, 0.5f);

        shadowMVPStack2.pushMatrix();//tree translation
        shadowMVPStack2.translate(tree3Loc);
        shadowMVPStack2.pushMatrix();//tree scale
        shadowMVPStack2.scale(0.5f, 0.5f, 0.5f);

        mvStack.invert(invTrMat);
        invTrMat.transpose(invTrMat);

        gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
        gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVPStack2.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER,vbo[6]);
        gl.glVertexAttribPointer(0,3,GL_FLOAT,false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
        gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
        gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(2);

        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, treeTexture);


        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(GL_LEQUAL);
        gl.glEnable(GL_CULL_FACE);

        gl.glDrawArrays(GL_TRIANGLES, 0, tree.getNumVertices());

        mvStack.popMatrix(); //tree scale
        mvStack.popMatrix(); //tree translation

        shadowMVPStack2.popMatrix();
        shadowMVPStack2.popMatrix();

        bumpSwitch = 0;
        gl.glProgramUniform1i(renderingProgram, bumpBool, bumpSwitch);


        tex3DSwitch = 1;
        gl.glProgramUniform1i(renderingProgram, tex3DBool, tex3DSwitch);

        //log model
        mvStack.pushMatrix();//log translation
        mvStack.translate(logLoc);
        mvStack.pushMatrix();//log scale
        mvStack.scale(logScale);

        shadowMVPStack2.pushMatrix();//log translation
        shadowMVPStack2.translate(logLoc);
        shadowMVPStack2.pushMatrix();//log scale
        shadowMVPStack2.scale(logScale);

        mvStack.invert(invTrMat);
        invTrMat.transpose(invTrMat);

        gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
        gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVPStack2.get(vals));
        gl.glUniformMatrix4fv(texRotLoc, 1, false, texRotMat.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER,vbo[14]);
        gl.glVertexAttribPointer(0,3,GL_FLOAT,false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[16]);
        gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(2);

        gl.glActiveTexture(GL_TEXTURE4);
        gl.glBindTexture(GL_TEXTURE_3D, woodTexture);

        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(GL_LEQUAL);
        gl.glEnable(GL_CULL_FACE);

        gl.glDrawArrays(GL_TRIANGLES, 0, log.getNumVertices());

        mvStack.popMatrix(); //tree scale
        mvStack.popMatrix(); //tree translation

        shadowMVPStack2.popMatrix();
        shadowMVPStack2.popMatrix();

        tex3DSwitch = 0;
        gl.glProgramUniform1i(renderingProgram, tex3DBool, tex3DSwitch);

        //axe and handle model
        mvStack.pushMatrix();//axe translation
        mvStack.translate(axeHandleLoc);
        mvStack.pushMatrix();//axe rotate
        mvStack.rotate((float)Math.toRadians(230),1f, 0f, 0f);
        mvStack.pushMatrix();//axe scale
        mvStack.scale(axeHandleScale);

        shadowMVPStack2.pushMatrix();//axe translation
        shadowMVPStack2.translate(axeHandleLoc);
        shadowMVPStack2.pushMatrix();//axe rotate
        shadowMVPStack2.rotate((float)Math.toRadians(230),1f, 0f, 0f);
        shadowMVPStack2.pushMatrix();//axe scale
        shadowMVPStack2.scale(axeHandleScale);

        tex3DSwitch = 1;
        gl.glProgramUniform1i(renderingProgram, tex3DBool, tex3DSwitch);

        mvStack.invert(invTrMat);
        invTrMat.transpose(invTrMat);

        gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
        gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVPStack2.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER,vbo[20]);
        gl.glVertexAttribPointer(0,3,GL_FLOAT,false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[21]);
        gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[22]);
        gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(2);

        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, axeHandleTexture);


        //gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(GL_LEQUAL);
        gl.glEnable(GL_CULL_FACE);

        gl.glDrawArrays(GL_TRIANGLES, 0, axeHandle.getNumVertices());

        tex3DSwitch = 0;
        gl.glProgramUniform1i(renderingProgram, tex3DBool, tex3DSwitch);

        //reflection
        reflectionSwitch = 1;
        gl.glProgramUniform1i(renderingProgram, reflectionBool, reflectionSwitch);

        //axe model
        mvStack.pushMatrix();
        mvStack.translate(axeLoc);
        shadowMVPStack2.pushMatrix();
        shadowMVPStack2.translate(axeLoc);

        mvStack.invert(invTrMat);
        invTrMat.transpose(invTrMat);

        gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
        gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVPStack2.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER,vbo[17]);
        gl.glVertexAttribPointer(0,3,GL_FLOAT,false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[18]);
        gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[19]);
        gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(2);

        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, axeTexture);


        //gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(GL_LEQUAL);
        gl.glEnable(GL_CULL_FACE);

        gl.glDrawArrays(GL_TRIANGLES, 0, axe.getNumVertices());


        reflectionSwitch = 0;
        gl.glProgramUniform1i(renderingProgram, reflectionBool, reflectionSwitch);


        mvStack.popMatrix();//axe translate
        shadowMVPStack2.popMatrix();//axe translate


        mvStack.popMatrix();//axe translate
        mvStack.popMatrix();//axe rotate
        mvStack.popMatrix();//axe scale

        shadowMVPStack2.popMatrix();//axe translate
        shadowMVPStack2.popMatrix();//axe rotate
        shadowMVPStack2.popMatrix();//axe scale


        //ghost model
        mvStack.pushMatrix();//ghost translation
        mvStack.translate(ghostLoc);
        mvStack.pushMatrix();//ghost scale
        mvStack.scale(ghostScale);

        shadowMVPStack2.pushMatrix();//ghost translation
        shadowMVPStack2.translate(ghostLoc);
        shadowMVPStack2.pushMatrix();//ghost scale
        shadowMVPStack2.scale(ghostScale);

        mvStack.invert(invTrMat);
        invTrMat.transpose(invTrMat);

        gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
        gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVPStack2.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER,vbo[23]);
        gl.glVertexAttribPointer(0,3,GL_FLOAT,false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[24]);
        gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[25]);
        gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(2);

        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, ghostTexture);


        //transparency
        gl.glEnable(GL_BLEND);
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        gl.glBlendEquation(GL_FUNC_ADD);

        gl.glEnable(GL_CULL_FACE);
        gl.glProgramUniform1f(renderingProgram, alphaLoc, 0.3f);
        gl.glProgramUniform1f(renderingProgram, flipLoc, -1.0f);
        gl.glDrawArrays(GL_TRIANGLES, 0, ghost.getNumVertices());


        gl.glCullFace(GL_BACK);
        gl.glProgramUniform1f(renderingProgram, alphaLoc, 0.7f);
        gl.glProgramUniform1f(renderingProgram, flipLoc, 1.0f);
        gl.glDrawArrays(GL_TRIANGLES, 0,ghost.getNumVertices());

        gl.glDisable(GL_BLEND);

        gl.glProgramUniform1f(renderingProgram, alphaLoc, 1.0f);
        gl.glProgramUniform1f(renderingProgram, flipLoc, 1.0f);


        mvStack.popMatrix(); //ghost scale
        mvStack.popMatrix(); //ghost translation

        shadowMVPStack2.popMatrix();
        shadowMVPStack2.popMatrix();


        //tesselation model
        installLight(tesselationRenderingProgram, mvStack);
        gl.glUseProgram(tesselationRenderingProgram);

        mvLoc = gl.glGetUniformLocation(tesselationRenderingProgram, "mv_matrix");
        projLoc = gl.glGetUniformLocation(tesselationRenderingProgram, "proj_matrix");
        nLoc = gl.glGetUniformLocation(tesselationRenderingProgram, "norm_matrix");
        sLoc = gl.glGetUniformLocation(tesselationRenderingProgram, "shadowMVP");

        mvStack.pushMatrix();//tesselation
        mvStack.translate(floorLoc);
        mvStack.pushMatrix();//scale
        mvStack.scale(floorScale);

        shadowMVPStack2.pushMatrix();//translation
        shadowMVPStack2.translate(floorLoc);
        shadowMVPStack2.pushMatrix();//scale
        shadowMVPStack2.scale(floorScale);


        mvStack.invert(invTrMat);
        invTrMat.transpose(invTrMat);

        gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
        gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
        gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVPStack2.get(vals));

        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, tessTexture);
        gl.glActiveTexture(GL_TEXTURE1);
        gl.glBindTexture(GL_TEXTURE_2D, tessHeightMap);
        gl.glActiveTexture(GL_TEXTURE2);
        gl.glBindTexture(GL_TEXTURE_2D, tessNormalMap);

        //gl.glClear(GL_DEPTH_BUFFER_BIT);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);

        gl.glPatchParameteri(GL_PATCH_VERTICES, 4);
        gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        gl.glDrawArraysInstanced(GL_PATCHES, 0, 4, 64*64);


        mvStack.popMatrix();//tesselation
        mvStack.popMatrix();//scale

        shadowMVPStack2.popMatrix();
        shadowMVPStack2.popMatrix();

        gl.glFrontFace(GL_CCW);


        //creates axis if spacebar is pushed
        if(axisFlag) {
            mvStack.pushMatrix();
            gl.glUseProgram(axesRenderingProgram);
            mvLoc = gl.glGetUniformLocation(axesRenderingProgram, "mv_matrix");
            projLoc = gl.glGetUniformLocation(axesRenderingProgram, "proj_matrix");

            gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
            gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));


            gl.glDrawArrays(GL_LINES, 0, 6);
            mvStack.popMatrix();
        }

        mvStack.popMatrix();//pop camera


        currentTime = System.currentTimeMillis() - startTime;

    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
        pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);

        setupShadowBuffers();
    }

    /**
     * install lights into a certain rendering program
     * @param renderingProgram rendering program used for lighting
     * @param vMat view matrix (camera location)
     */
    private void installLight(int renderingProgram, Matrix4f vMat){
        GL4 gl = (GL4) GLContext.getCurrentGL();

        globalAmbLoc = gl.glGetUniformLocation(renderingProgram, "globalAmbient");
        ambLoc = gl.glGetUniformLocation(renderingProgram, "light.ambient");
        diffLoc = gl.glGetUniformLocation(renderingProgram, "light.diffuse");
        specLoc = gl.glGetUniformLocation(renderingProgram, "light.specular");
        posLoc = gl.glGetUniformLocation(renderingProgram, "light.position");
        mambLoc = gl.glGetUniformLocation(renderingProgram, "material.ambient");
        mdiffLoc = gl.glGetUniformLocation(renderingProgram, "material.diffuse");
        mspecLoc = gl.glGetUniformLocation(renderingProgram, "material.specular");
        mshiLoc = gl.glGetUniformLocation(renderingProgram, "material.shininess");

        gl.glProgramUniform4fv(renderingProgram, globalAmbLoc, 1, globalAmbient, 0);
        gl.glProgramUniform4fv(renderingProgram, ambLoc, 1, lightAmbient, 0);
        gl.glProgramUniform4fv(renderingProgram, diffLoc, 1, lightDiffuse, 0);
        gl.glProgramUniform4fv(renderingProgram, specLoc, 1, lightSpecular, 0);
        gl.glProgramUniform3fv(renderingProgram, posLoc, 1, lightPos, 0);
        gl.glProgramUniform4fv(renderingProgram, mambLoc, 1, matAmb, 0);
        gl.glProgramUniform4fv(renderingProgram, mdiffLoc, 1, matDif, 0);
        gl.glProgramUniform4fv(renderingProgram, mspecLoc, 1, matSpe, 0);
        gl.glProgramUniform1f(renderingProgram, mshiLoc, matShi);

    }

    /**
     * set up shadows buffer to create shadows
     */
    private void setupShadowBuffers(){
        GL4 gl = (GL4) GLContext.getCurrentGL();
        scSizeX = myCanvas.getWidth();
        scSizeY = myCanvas.getHeight();

        gl.glGenFramebuffers(1, shadowBuffer, 0);

        gl.glGenTextures(1, shadowTex, 0);
        gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
                scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }

    /**
     * sets up the vertices and texture coordinates and binds them to vbos
     */
    private void setupVertices(){
        GL4 gl = (GL4) GLContext.getCurrentGL();

        cubeHead = new CubeHead();
        mushroom = new ImportedModel("objects/mushroom.obj");
        grid = new ImportedModel("objects/grid.obj");
        tree = new ImportedModel("objects/tree2.obj");
        skyBox = new SkyBox();

        axe = new ImportedModel("objects/axe.obj");
        axeHandle = new ImportedModel("objects/axe_handle.obj");
        log = new ImportedModel("objects/log.obj");
        ghost = new ImportedModel("objects/ghost.obj");




        gl.glGenVertexArrays(vao.length, vao, 0);
        gl.glBindVertexArray(vao[0]);
        gl.glGenBuffers(vbo.length, vbo, 0);


        //binds mushroom model
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        FloatBuffer mushVertBuf = Buffers.newDirectFloatBuffer(mushroom.getPValues());
        gl.glBufferData(GL_ARRAY_BUFFER, mushVertBuf.limit()*4, mushVertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        FloatBuffer mushTextBuf = Buffers.newDirectFloatBuffer(mushroom.getTValues());
        gl.glBufferData(GL_ARRAY_BUFFER, mushTextBuf.limit()*4, mushTextBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
        FloatBuffer mushNormBuf = Buffers.newDirectFloatBuffer(mushroom.getNValues());
        gl.glBufferData(GL_ARRAY_BUFFER, mushNormBuf.limit()*4, mushNormBuf, GL_STATIC_DRAW);

        //binds grid model
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
        FloatBuffer floorVertBuf = Buffers.newDirectFloatBuffer(grid.getPValues());
        gl.glBufferData(GL_ARRAY_BUFFER, floorVertBuf.limit()*4, floorVertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
        FloatBuffer floorTextBuf = Buffers.newDirectFloatBuffer(grid.getTValues());
        gl.glBufferData(GL_ARRAY_BUFFER, floorTextBuf.limit()*4, floorTextBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
        FloatBuffer floorNormBuf = Buffers.newDirectFloatBuffer(grid.getNValues());
        gl.glBufferData(GL_ARRAY_BUFFER, floorNormBuf.limit()*4, floorNormBuf, GL_STATIC_DRAW);

        //bind tree model
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
        FloatBuffer treeVertBuf = Buffers.newDirectFloatBuffer(tree.getPValues());
        gl.glBufferData(GL_ARRAY_BUFFER, treeVertBuf.limit()*4, treeVertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
        FloatBuffer treeTextBuf = Buffers.newDirectFloatBuffer(tree.getTValues());
        gl.glBufferData(GL_ARRAY_BUFFER, treeTextBuf.limit()*4, treeTextBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
        FloatBuffer treeNormBuf = Buffers.newDirectFloatBuffer(tree.getNValues());
        gl.glBufferData(GL_ARRAY_BUFFER, treeNormBuf.limit()*4, treeNormBuf, GL_STATIC_DRAW);

        //binds lightCube model
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
        FloatBuffer lightBoxVertBuf = Buffers.newDirectFloatBuffer(cubeHead.getVertices());
        gl.glBufferData(GL_ARRAY_BUFFER, lightBoxVertBuf.limit()*4, lightBoxVertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
        FloatBuffer lightBoxTextBuf = Buffers.newDirectFloatBuffer(cubeHead.getTextureCoordinates());
        gl.glBufferData(GL_ARRAY_BUFFER, lightBoxTextBuf.limit()*4, lightBoxTextBuf, GL_STATIC_DRAW);

        //binds skyCube model
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
        FloatBuffer skyBoxVertBuf = Buffers.newDirectFloatBuffer(skyBox.getVertices());
        gl.glBufferData(GL_ARRAY_BUFFER, skyBoxVertBuf.limit()*4, skyBoxVertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
        FloatBuffer skyBoxTextBuf = Buffers.newDirectFloatBuffer(skyBox.getTextureCoordinates());
        gl.glBufferData(GL_ARRAY_BUFFER, skyBoxTextBuf.limit()*4, skyBoxTextBuf, GL_STATIC_DRAW);

        //bind log model
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
        FloatBuffer logVertBuf = Buffers.newDirectFloatBuffer(log.getPValues());
        gl.glBufferData(GL_ARRAY_BUFFER, logVertBuf.limit()*4, logVertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[15]);
        FloatBuffer logTextBuf = Buffers.newDirectFloatBuffer(log.getTValues());
        gl.glBufferData(GL_ARRAY_BUFFER, logTextBuf.limit()*4, logTextBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[16]);
        FloatBuffer logNormBuf = Buffers.newDirectFloatBuffer(log.getNValues());
        gl.glBufferData(GL_ARRAY_BUFFER, logNormBuf.limit()*4, logNormBuf, GL_STATIC_DRAW);

        //bind axe model
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[17]);
        FloatBuffer axeVertBuf = Buffers.newDirectFloatBuffer(axe.getPValues());
        gl.glBufferData(GL_ARRAY_BUFFER, axeVertBuf.limit()*4, axeVertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[18]);
        FloatBuffer axeTextBuf = Buffers.newDirectFloatBuffer(axe.getTValues());
        gl.glBufferData(GL_ARRAY_BUFFER, axeTextBuf.limit()*4, axeTextBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[19]);
        FloatBuffer axeNormBuf = Buffers.newDirectFloatBuffer(axe.getNValues());
        gl.glBufferData(GL_ARRAY_BUFFER, axeNormBuf.limit()*4, axeNormBuf, GL_STATIC_DRAW);

        //bind axe handel model
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[20]);
        FloatBuffer axeHandleVertBuf = Buffers.newDirectFloatBuffer(axeHandle.getPValues());
        gl.glBufferData(GL_ARRAY_BUFFER, axeHandleVertBuf.limit()*4, axeHandleVertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[21]);
        FloatBuffer axeHandleTextBuf = Buffers.newDirectFloatBuffer(axeHandle.getTValues());
        gl.glBufferData(GL_ARRAY_BUFFER, axeHandleTextBuf.limit()*4, axeHandleTextBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[22]);
        FloatBuffer axeHandleNormBuf = Buffers.newDirectFloatBuffer(axeHandle.getNValues());
        gl.glBufferData(GL_ARRAY_BUFFER, axeHandleNormBuf.limit()*4, axeHandleNormBuf, GL_STATIC_DRAW);

        //bind ghost model
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[23]);
        FloatBuffer ghostVertBuf = Buffers.newDirectFloatBuffer(ghost.getPValues());
        gl.glBufferData(GL_ARRAY_BUFFER, ghostVertBuf.limit()*4, ghostVertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[24]);
        FloatBuffer ghostTextBuf = Buffers.newDirectFloatBuffer(ghost.getTValues());
        gl.glBufferData(GL_ARRAY_BUFFER, ghostTextBuf.limit()*4, ghostTextBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[25]);
        FloatBuffer ghostNormBuf = Buffers.newDirectFloatBuffer(ghost.getNValues());
        gl.glBufferData(GL_ARRAY_BUFFER, ghostNormBuf.limit()*4, ghostNormBuf, GL_STATIC_DRAW);



    }

    /**
     * change material to gold
     */
    private void changeToGoldMaterial(){
        GL4 gl = (GL4) GLContext.getCurrentGL();

        matAmb = Utils.goldAmbient();
        matDif = Utils.goldDiffuse();
        matSpe = Utils.goldSpecular();
        matShi = Utils.goldShininess();

        mambLoc = gl.glGetUniformLocation(renderingProgram, "material.ambient");
        mdiffLoc = gl.glGetUniformLocation(renderingProgram, "material.diffuse");
        mspecLoc = gl.glGetUniformLocation(renderingProgram, "material.specular");
        mshiLoc = gl.glGetUniformLocation(renderingProgram, "material.shininess");

        gl.glProgramUniform4fv(renderingProgram, mambLoc, 1, matAmb, 0);
        gl.glProgramUniform4fv(renderingProgram, mdiffLoc, 1, matDif, 0);
        gl.glProgramUniform4fv(renderingProgram, mspecLoc, 1, matSpe, 0);
        gl.glProgramUniform1f(renderingProgram, mshiLoc, matShi);
    }

    /**
     * change material to silver
     */
    private void changeToSilverMaterial(){
        GL4 gl = (GL4) GLContext.getCurrentGL();

        matAmb = Utils.silverAmbient();
        matDif = Utils.silverDiffuse();
        matSpe = Utils.silverSpecular();
        matShi = Utils.silverShininess();

        mambLoc = gl.glGetUniformLocation(renderingProgram, "material.ambient");
        mdiffLoc = gl.glGetUniformLocation(renderingProgram, "material.diffuse");
        mspecLoc = gl.glGetUniformLocation(renderingProgram, "material.specular");
        mshiLoc = gl.glGetUniformLocation(renderingProgram, "material.shininess");

        gl.glProgramUniform4fv(renderingProgram, mambLoc, 1, matAmb, 0);
        gl.glProgramUniform4fv(renderingProgram, mdiffLoc, 1, matDif, 0);
        gl.glProgramUniform4fv(renderingProgram, mspecLoc, 1, matSpe, 0);
        gl.glProgramUniform1f(renderingProgram, mshiLoc, matShi);
    }


    /**
     * change material to a flat black rubber color (used for matt materials)
     */
    private void changeToMatMaterial(){
        GL4 gl = (GL4) GLContext.getCurrentGL();

        float[] amb = {.2f,.2f,.2f ,1.0f};
        float[] dif = {.01f, .01f, .01f, 1.0f};
        float[] spec = {.4f, .4f, .4f, 1.0f};

        matAmb = amb;
        matDif = dif;
        matSpe = spec;
        matShi = .078125f;

        mambLoc = gl.glGetUniformLocation(renderingProgram, "material.ambient");
        mdiffLoc = gl.glGetUniformLocation(renderingProgram, "material.diffuse");
        mspecLoc = gl.glGetUniformLocation(renderingProgram, "material.specular");
        mshiLoc = gl.glGetUniformLocation(renderingProgram, "material.shininess");

        gl.glProgramUniform4fv(renderingProgram, mambLoc, 1, matAmb, 0);
        gl.glProgramUniform4fv(renderingProgram, mdiffLoc, 1, matDif, 0);
        gl.glProgramUniform4fv(renderingProgram, mspecLoc, 1, matSpe, 0);
        gl.glProgramUniform1f(renderingProgram, mshiLoc, matShi);
    }


    /**
     * calls constructor
     * @param args arguements
     */
    public static void main(String[] args){
       new Starter();
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {}

    /**
     *  key pushed
     * @param keyEvent key pushed
     */
    @Override
    public void keyPressed(KeyEvent keyEvent) {
        switch(keyEvent.getKeyCode()){
            //key push a, move camera left
            case KeyEvent.VK_A:
                camera.moveLeft();
                System.out.println("press a");
                break;
            //key push s, move camera back
            case KeyEvent.VK_S:
                camera.moveBack();
                System.out.println("press s");
                break;
            //key push d, move camera right
            case KeyEvent.VK_D:
                camera.moveRight();
                System.out.println("press d");
                break;
            //key push w, move camera forward
            case KeyEvent.VK_W:
                camera.moveForward();
                System.out.println("press w");
                break;
            // key push q, moves up
            case KeyEvent.VK_Q:
                camera.moveUp();
                System.out.println("press w");
                break;
            // key push e moves down
            case KeyEvent.VK_E:
                camera.moveDown();
                System.out.println("press w");
                break;
            // key push left look left
            case KeyEvent.VK_LEFT:
                camera.lookLeft();
                break;
            // key push down look down
            case KeyEvent.VK_DOWN:
                camera.lookDown();
                break;
            // key push right look right
            case KeyEvent.VK_RIGHT:
                camera.lookRight();
                break;
            // key push up, look up
            case KeyEvent.VK_UP:
                camera.lookUp();
                break;
            case KeyEvent.VK_SPACE:
                axisFlag = (axisFlag) ? false : true;
                break;
            case KeyEvent.VK_G:
                globalAmbSwitch = (globalAmbSwitch == 0) ? 1 : 0;
                System.out.println(globalAmbSwitch);
                break;
        }
    }

    /**
     * scrolling changes the z axis of the light
     * @param e mouse input
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        //scroll down
        if(e.getWheelRotation() == 1){
            initialLightLoc.add(new Vector3f(0.0f, 0.0f, 0.5f));
            System.out.println("working");
            System.out.println(initialLightLoc.toString());
        //scroll up
        }else if (e.getWheelRotation() == -1){
            initialLightLoc.sub(new Vector3f(0.0f, 0.0f, 0.5f));
        }
    }

    /**
     * gets original mouse position
     * @param e mouse clicked
     */
    @Override
    public void mousePressed(MouseEvent e) {
        originalDraggedPositionX = e.getX();
        originalDraggedPositionY = e.getY();
    }

    /**
     * light movement in the x and y location
     * @param e mouse dragged location
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        int draggedAmountX;
        int draggedAmountY;

        draggedAmountX = -(originalDraggedPositionX - e.getX());
        draggedAmountY = originalDraggedPositionY - e.getY();

        originalDraggedPositionX = e.getX();
        originalDraggedPositionY = e.getY();
        initialLightLoc.add(new Vector3f((draggedAmountX* 0.1f), (draggedAmountY * 0.1f), 0.0f));
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseMoved(MouseEvent e) {}
}
