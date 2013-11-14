/*
 * fm.krui.kruifm.FlipAnimation - FlipAnimation.java
 *
 * (C) 2013 - Tony Andrys
 * http://www.tonyandrys.com
 *
 * Created: 11/14/2013
 *
 * ---
 *
 * This file is part of KRUI.FM.
 *
 * KRUI.FM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or(at your option) any later version.
 *
 * KRUI.FM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with KRUI.FM.  If not, see <http://www.gnu.org/licenses/>.
 */

package fm.krui.kruifm;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * fm.krui.kruifm - FlipAnimation
 *
 * I didn't write this! This class is referenced on various blogs but no one seems to know the original author...
 * If you know the author, please let me know.
 */

public class FlipAnimation extends Animation {

    private Camera camera;

    private View fromView;
    private View toView;

    private float centerX;
    private float centerY;

    private boolean forward = true;

    /**
     * Creates a 3D flip animation between two views.
     *
     * @param fromView First view in the transition.
     * @param toView Second view in the transition.
     */
    public FlipAnimation(View fromView, View toView)
    {
        this.fromView = fromView;
        this.toView = toView;

        setDuration(700);
        setFillAfter(false);
        setInterpolator(new AccelerateDecelerateInterpolator());
    }

    public void reverse()
    {
        forward = false;
        View switchView = toView;
        toView = fromView;
        fromView = switchView;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight)
    {
        super.initialize(width, height, parentWidth, parentHeight);
        centerX = width/2;
        centerY = height/2;
        camera = new Camera();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t)
    {
        // Angle around the y-axis of the rotation at the given time
        // calculated both in radians and degrees.
        final double radians = Math.PI * interpolatedTime;
        float degrees = (float) (180.0 * radians / Math.PI);

        // Once we reach the midpoint in the animation, we need to hide the
        // source view and show the destination view. We also need to change
        // the angle by 180 degrees so that the destination does not come in
        // flipped around
        if (interpolatedTime >= 0.5f)
        {
            degrees -= 180.f;
            fromView.setVisibility(View.GONE);
            toView.setVisibility(View.VISIBLE);
        }

        if (forward)
            degrees = -degrees; //determines direction of rotation when flip begins

        final Matrix matrix = t.getMatrix();
        camera.save();
        camera.rotateY(degrees);
        camera.getMatrix(matrix);
        camera.restore();
        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
    }

}