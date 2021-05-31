/**
 * Wood noise implementation. creates and returns 3d wood noise texture. For educational use only.
 * Implementation obtained from:
 * Gordon, V. Scott; Clevenger, John. Computer Graphics Programming in OpenGL with JAVA Second Edition
 * (p. 498). Mercury Learning and Information. Kindle Edition.
 *
 */
package a4;

import java.io.*;
import java.lang.Math;
import java.nio.*;
import java.util.*;
import java.awt.Color;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.common.nio.Buffers;
import org.joml.*;

public class WoodNoiseMapping {

    GL4 gl;
    private int noiseTexture;
    private int noiseHeight= 300;
    private int noiseWidth = 300;
    private int noiseDepth = 300;
    private double[][][] noise = new double[noiseHeight][noiseWidth][noiseDepth];
    private java.util.Random random = new java.util.Random();

    WoodNoiseMapping(){
        generateNoise();
        noiseTexture = buildNoiseTexture();
    }

    // 3D Texture section
    private void fillDataArray(byte data[]) {
        double xyPeriod = 10.0;
        double turbPower = 0.05;
        double turbSize =  20.0;

        for (int i=0; i<noiseWidth; i++) {
            for (int j=0; j<noiseHeight; j++) {
                for (int k=0; k<noiseDepth; k++) {
                    double xValue = (i - (double)noiseWidth/2.0) / (double)noiseWidth;
                    double yValue = (j - (double)noiseHeight/2.0) / (double)noiseHeight;
                    double distValue = Math.sqrt(xValue * xValue + yValue * yValue)
                                + turbPower * turbulence(i, j, k, turbSize) / 256.0;
                    double sineValue = 128.0 * Math.abs(Math.sin(2.0 * xyPeriod * distValue * Math.PI));

                    Color c = new Color((int)(60+(int)sineValue), (int)(10+(int)sineValue), 0);

                    data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+0] = (byte) c.getRed();
                    data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+1] = (byte) c.getGreen();
                    data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+2] = (byte) c.getBlue();
                    data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+3] = (byte) 255;
                }
            }
        }
    }

    private int buildNoiseTexture() {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        byte[] data = new byte[noiseHeight*noiseWidth*noiseDepth*4];

        fillDataArray(data);

        ByteBuffer bb = Buffers.newDirectByteBuffer(data);

        int[] textureIDs = new int[1];
        gl.glGenTextures(1, textureIDs, 0);
        int textureID = textureIDs[0];

        gl.glBindTexture(GL_TEXTURE_3D, textureID);

        gl.glTexStorage3D(GL_TEXTURE_3D, 1, GL_RGBA8, noiseWidth, noiseHeight, noiseDepth);
        gl.glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0,
                noiseWidth, noiseHeight, noiseDepth, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, bb);

        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        return textureID;
    }

    void generateNoise() {
        for (int x=0; x<noiseHeight; x++) {
            for (int y=0; y<noiseWidth; y++) {
                for (int z=0; z<noiseDepth; z++) {
                    noise[x][y][z] = random.nextDouble();
                }
            }
        }
    }

    double smoothNoise(double x1, double y1, double z1) {
        //get fractional part of x, y, and z
        double fractX = x1 - (int) x1;
        double fractY = y1 - (int) y1;
        double fractZ = z1 - (int) z1;

        //neighbor values
        int x2 = ((int)x1 + noiseWidth + 1) % noiseWidth;
        int y2 = ((int)y1 + noiseHeight+ 1) % noiseHeight;
        int z2 = ((int)z1 + noiseDepth + 1) % noiseDepth;

        //smooth the noise by interpolating
        double value = 0.0;
        value += (1-fractX) * (1-fractY) * (1-fractZ) * noise[(int)x1][(int)y1][(int)z1];
        value += (1-fractX) * fractY     * (1-fractZ) * noise[(int)x1][(int)y2][(int)z1];
        value += fractX     * (1-fractY) * (1-fractZ) * noise[(int)x2][(int)y1][(int)z1];
        value += fractX     * fractY     * (1-fractZ) * noise[(int)x2][(int)y2][(int)z1];

        value += (1-fractX) * (1-fractY) * fractZ     * noise[(int)x1][(int)y1][(int)z2];
        value += (1-fractX) * fractY     * fractZ     * noise[(int)x1][(int)y2][(int)z2];
        value += fractX     * (1-fractY) * fractZ     * noise[(int)x2][(int)y1][(int)z2];
        value += fractX     * fractY     * fractZ     * noise[(int)x2][(int)y2][(int)z2];

        return value;
    }

    private double turbulence(double x, double y, double z, double size) {
        double value = 0.0, initialSize = size;
        while(size >= 0.9) {
            value = value + smoothNoise(x/size, y/size, z/size) * size;
            size = size / 2.0;
        }
        value = 128.0 * value / initialSize;
        return value;
    }


    public int getWoodTexture(){return noiseTexture;};
}
