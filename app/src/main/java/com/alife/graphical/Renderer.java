package com.alife.graphical;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import androidx.appcompat.app.AppCompatActivity;

import com.alife.graphical.utils.ResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Renderer implements GLSurfaceView.Renderer {

    private AppCompatActivity mActivity;

    // Store our model data in a float buffer
    private FloatBuffer mTriangleVertices;

    // Shader
    Shader shader;
    // Shader handles
    private int mPositionHandle;
    private int mColorHandle;

    // Matrices
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    public Renderer(AppCompatActivity root) {
        mActivity = root;

        final float[] triangle1VerticesData = {
                // x, y, z, r, g, b, a
                -0.5f, -0.25f,  0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                 0.5f, -0.25f,  0.0f, 0.0f, 0.0f, 1.0f, 1.0f,
                 0.0f,  0.55f,  0.0f, 0.0f, 1.0f, 0.0f, 1.0f
        };

        // Initialize the buffer
        mTriangleVertices = ByteBuffer.allocateDirect(triangle1VerticesData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(triangle1VerticesData).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set background clear color to orange
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Position the eye behind the origin
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 1.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our up vector. This is where out head would be pointing
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        /*
        * Set the view matrix. This matrix can be said to represent the camera position.
        * NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and view matrix.
        * In OpenGL 2, we can keep track of these matrices separately if we choose.
        */
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        // Getting shader sources
        String vsSource = ResourceReader.ReadRawFileContent(mActivity, "main_v");
        String fsSource = ResourceReader.ReadRawFileContent(mActivity, "main_f");

        // Creating shader objects
        shader = new Shader(vsSource, fsSource);

        // Bind attributes
        GLES20.glBindAttribLocation(shader.getRendererID(), 0, "a_Position");
        GLES20.glBindAttribLocation(shader.getRendererID(), 1, "a_Color");

        // Set program handles. These will later be used to pass in values to the shader program
        mPositionHandle = GLES20.glGetAttribLocation(shader.getRendererID(), "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(shader.getRendererID(), "a_Color");

        // Tell OpenGL to use this program when rendering.
        shader.activate();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same while the width will vary as per aspect ratio.
        final float ratio = (float)width / height;
        final float left = -ratio;
        final float right = ratio * 1;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear screen
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        // Do a complete rotation every 10 seconds
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int)time);

        // Draw the triangle facing straight on
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);

        drawTriangle(mTriangleVertices);
    }

    private void drawTriangle(final FloatBuffer triangleBuffer) {
        // Pass the position data
        triangleBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 28, triangleBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass the color data
        triangleBuffer.position(3);
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 28, triangleBuffer);
        GLES20.glEnableVertexAttribArray(mColorHandle);

        // Calculate combined world matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        // Load matrix to uniform variable
        shader.setUniformMatrix4x4("u_MVPMatrix", mMVPMatrix);

        // OpenGL draw call
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }
}
