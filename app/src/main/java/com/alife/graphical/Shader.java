package com.alife.graphical;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

public class Shader {
    private int mId;

    public Shader(String vsSource, String fsSource) {
        mId = GLES20.glCreateProgram();
        attachShader(vsSource, GLES20.GL_VERTEX_SHADER);
        attachShader(fsSource, GLES20.GL_FRAGMENT_SHADER);
        linkProgram();
    }

    private void attachShader(String source, int type) {
        int shader = GLES20.glCreateShader(type);

        if (shader == 0) {
            throw new RuntimeException("Failed to create shader object!");
        }

        // Load shader source into the shader
        GLES20.glShaderSource(shader, source);

        // Compile the shader
        GLES20.glCompileShader(shader);

        // Get the compilation status
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader
        if (compileStatus[0] == 0) {
            String errorLog = GLES20.glGetShaderInfoLog(shader);

            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Failed to compile shader!\n" + errorLog);
        }

        GLES20.glAttachShader(mId, shader);
        GLES20.glDeleteShader(shader);
    }

    private void linkProgram() {
        // Link shader program
        GLES20.glLinkProgram(mId);

        // Check linkage errors
        final int[] linkageStatus = new int[1];
        GLES20.glGetProgramiv(mId, GLES20.GL_LINK_STATUS, linkageStatus, 0);

        // If linkage failed, delete program and throw error
        if (linkageStatus[0] == 0) {
            GLES20.glDeleteProgram(mId);
            throw new RuntimeException("Failed to link shader program!");
        }
    }

    public void activate() {
        GLES20.glUseProgram(mId);
    }

    public int getRendererID() {
        return mId;
    }

    public int getUniformLocation(String varName) {
        return GLES20.glGetUniformLocation(mId, varName);
    }
    public int getAttribLocation(String attribName) {
        return GLES20.glGetAttribLocation(mId, attribName);
    }

    public void setUniformMatrix4x4(String var, float[] matrix) {
        GLES20.glUniformMatrix4fv(getUniformLocation(var), 1, false, matrix, 0);
    }
    public void setUniformMatrix4x4(String var, FloatBuffer matrix) {
        GLES20.glUniformMatrix4fv(getUniformLocation(var), 1, false, matrix);
    }

    public void dispose() {
        GLES20.glDeleteProgram(mId);
    }
}
