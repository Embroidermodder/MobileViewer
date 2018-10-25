/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.embroideryio.embroideryio;
 /*
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
//*/

import android.graphics.Matrix;

/**
 *
 * @author Tat
 */
 /*
public class EmbMatrix extends AffineTransform {

    public void mapPoints(float[] p) {
        transform(p, 0, p, 0, 1);
    }

    public void postTranslate(float tx, float ty) {
        this.translate(tx, ty);
    }

    public void postScale(float sx, float sy) {
        this.scale(sx, sy);
    }

    public void postRotate(float theta) {
        this.rotate(theta);
    }

    public void invert(EmbMatrix matrix) {
        try {
            this.invert();
        } catch (NoninvertibleTransformException ex) {
        }
    }

    public void postScale(float sx, float sy, float x, float y) {
        this.translate(x, y);
        this.scale(sx, sy);
        this.translate(-x, -y);
    }

    public void postRotate(float theta, float x, float y) {
        this.translate(x, y);
        this.rotate(theta);
        this.translate(-x, -y);
    }

}
//*/

 class EmbMatrix extends Matrix {
}
