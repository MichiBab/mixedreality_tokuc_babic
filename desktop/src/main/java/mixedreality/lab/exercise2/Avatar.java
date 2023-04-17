/**
 * Diese Datei ist Teil des Vorgabeframeworks für die Veranstaltung "Mixed Reality"
 * <p>
 * Prof. Dr. Philipp Jenke, Hochschule für Angewandte Wissenschaften Hamburg.
 */

package mixedreality.lab.exercise2;

import com.jme3.math.Matrix3f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import math.Vectors;

/**
 * Information about the avatar in the game.
 */
public class Avatar {
    /**
     * Current position of the avatar
     */
    protected Vector2f pos;

    /**
     * Current rotation angle of the avatar (radian measure)
     */
    protected float rotationAngle;

    /**
     * The avatar must be rotated that its orientation matches this vector.
     */
    protected Vector2f targetPos;

    /**
     * Change rate of the orientation per time step.
     */
    public static final float ROTATION_VELOCITY = 0.1f;

    /**
     * Change rate of the position per time step.
     */
    public static final float MOVE_VELOCITY = 0.01f;

    public Avatar() {
        pos = new Vector2f(0, 0);
        rotationAngle = 0;
        targetPos = null;
    }

    public Vector2f getPos() {
        // Pos = translation part of pose matrix
        return Vectors.xy(makePose().mult(Vector3f.UNIT_Z));
    }

    public Vector2f getOrientation() {
        // Orientation of first colum of rotation part of pose matrix.
        return Vectors.xy(makePose().mult(Vector3f.UNIT_X));
    }

    public void setTargetPos(Vector2f o) {
        this.targetPos = o;
    }

    // ++++++++++++++++ YOUR TASKS START HERE +++++++++++++++++++++++++++++++++

    /**
     * Generate a 3x3 homogenious transformation matrix which contains the
     * current rotation and p
     */
    protected Matrix3f makePose() {
        Matrix3f pose = new Matrix3f();
        float cos_theta = (float) Math.cos(rotationAngle);
        float sin_theta = (float) Math.sin(rotationAngle);
        pose.setColumn(0, new Vector3f(cos_theta, -sin_theta, 0));
        pose.setColumn(1, new Vector3f(sin_theta, cos_theta, 0));
        pose.setColumn(2, new Vector3f(pos.x, pos.y, 1));
        return pose;
    }

    public static double normalizeRad(double angle) {
        return ((angle + Math.PI) % (2 * Math.PI)) - Math.PI;
    }

    public static double calculateAngleDifference(double from, double to) {
        double diff = (to - from + 360) % 360;
        if (diff > 180) {
            diff = 360 - diff;
        }

        return diff;
    }

    public static double calculateWhichDirection(double from, double to) {
        boolean done = false;
        int left_counter = 0;
        int from_i = (int) from;
        int to_i = (int) to;

        while (!done) {
            if (to_i == from_i) {
                break;
            }
            if (to_i == 0) {
                to_i = 360;
            } else {
                to_i -= 1;
            }
            left_counter += 1;
        }

        if (left_counter < 180) {
            return -1;
        }
        return 1;

    }

    /**
     * Move the avatar along the current orientation.
     */
    public void moveToTargetPos() {
        // print current position and orientation
        System.out.println("pos: " + pos);
        System.out.println("orientation: " + getOrientation());
        // First: rotate to target position
        if (targetPos == null) {
            return;
        }
        float diff_x = targetPos.x - pos.x;
        float diff_y = targetPos.y - pos.y;
        double theta = Math.atan2(diff_x, diff_y);

        // convert theta to degrees [0; 360]
        double theta_deg = Math.toDegrees(theta) - 90;
        if (theta_deg < 0) {
            theta_deg += 360;
        }
        double rotation_deg = Math.toDegrees(rotationAngle);
        if (rotation_deg < 0) {
            rotation_deg += 360;
        }

        double diff = calculateAngleDifference(rotation_deg, theta_deg);

        System.out.println(
                "wanted: " + theta_deg + " current: " + rotation_deg + " diff: " + diff);

        double pose_distance = Math.sqrt(Math.pow(diff_x, 2) + Math.pow(diff_y, 2));

        if (pose_distance < 2.0 * MOVE_VELOCITY) {
            System.out.println("on goal!");
            return;
        }

        if (Math.abs(diff) > 10) {
            double step_size = Math.min(Math.toDegrees(ROTATION_VELOCITY), Math.abs(diff));
            rotationAngle += Math.toRadians(step_size * -calculateWhichDirection(rotation_deg, theta_deg));
        }
        // calculate pose distance
        pos.x += MOVE_VELOCITY * diff_x / pose_distance;
        pos.y += MOVE_VELOCITY * diff_y / pose_distance;

    }

}
