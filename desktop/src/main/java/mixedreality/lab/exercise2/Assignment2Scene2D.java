/**
 * Diese Datei ist Teil des Vorgabeframeworks für die Veranstaltung "Mixed Reality"
 * <p>
 * Prof. Dr. Philipp Jenke, Hochschule für Angewandte Wissenschaften Hamburg.
 */

package mixedreality.lab.exercise2;

import com.jme3.math.Matrix3f;
import com.jme3.math.Vector2f;
import sprites.AnimatedSprite;
import sprites.Constants;
import sprites.SpriteAnimationImporter;
import ui.Scene2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Timer;
import java.util.TimerTask;

import static math.Matrices.transform;
import static sprites.Constants.WALK_ANIMATION_IDS;

/**
 * Base scene for assigment 2
 */
public class Assignment2Scene2D extends Scene2D implements MouseListener {
    /**
     * Representation of the animated sprite used for the avatar
     */
    private AnimatedSprite avatarSprite;

    // private final Sprite staticSprite;

    /**
     * This class controls the behavior of the avatar
     */
    protected Avatar avatar;

    /**
     * The current position of the mouse cursor in scene coordinates.
     */
    private Vector2f mousePosInScene;

    private final Vector2f arrowPos;

    public Assignment2Scene2D(int width, int height) {
        super(width, height, new Vector2f(-1, -1), new Vector2f(1, 1));
        avatar = new Avatar();
        this.mousePosInScene = new Vector2f(-1, -1);

        // Avatar
        loadAvatarSprite();
        avatarSprite.setAnimationId(Constants.WalkAnimations.WALK_E);

        // Arrow
        // this.staticSprite = new Sprite("sprites/staticSprite.png", 50, 50);
        this.arrowPos = new Vector2f(-0.5f, -0.5f);

        // This is the game loop
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    updateGame();
                    renderGame();
                });
            }
        }, 0, 100);
        addMouseListener(this);
    }

    /**
     * This method is called once before rendering and is used to update the game
     * state.
     */
    private void updateGame() {
        // Move avatar into current orientation
        avatar.moveToTargetPos();
        System.out.println("in2DScene: " + avatar.getPos());
    }

    /**
     * This method is called one per render frame.
     */
    private void renderGame() {
        avatarSprite.setAnimationId(computeAnimationForOrientation());
        repaint();
    }

    @Override
    public void paint(Graphics g) {

        // Draw target
        if (mousePosInScene != null) {
            drawPoint(g, mousePosInScene, Color.BLACK);
        }

        // Show orientation
        drawLine(g, avatar.getPos(),
                avatar.getPos().add(avatar.getOrientation().mult(0.2f)), Color.LIGHT_GRAY);

        // Draw avatar sprite
        avatarSprite.draw(g, world2Pixel(avatar.getPos()));
        System.out.println("AvataregtPos2DScne" + avatar.getPos());

        // Draw arrow
        Matrix3f pose = getArrowPose(avatar, arrowPos);
        Vector2f start = new Vector2f(0, 0);
        Vector2f end = new Vector2f(0.2f, 0);
        Vector2f s = transform(pose, start);
        Vector2f e = transform(pose, end);
        drawPoint(g, s, Color.BLUE);
        drawLine(g, s, e, Color.BLUE);
    }

    @Override
    public String getTitle() {
        return "Assignment 2 - Game";
    }

    /**
     * Pre-load of sprite animations, generate animated sprite object.
     */
    private void loadAvatarSprite() {
        SpriteAnimationImporter.ImportParams[] importParams = {
                new SpriteAnimationImporter.ImportParams(
                        "sprites/character_sprites.png",
                        WALK_ANIMATION_IDS[Constants.WalkAnimations.WALK_S.ordinal()],
                        64, 64,
                        new SpriteAnimationImporter.Idx(0, 10),
                        SpriteAnimationImporter.Orientation.HORIZONTAL,
                        9, false),
                new SpriteAnimationImporter.ImportParams(
                        "sprites/character_sprites.png",
                        WALK_ANIMATION_IDS[Constants.WalkAnimations.WALK_W.ordinal()],
                        64, 64,
                        new SpriteAnimationImporter.Idx(0, 9),
                        SpriteAnimationImporter.Orientation.HORIZONTAL,
                        9, false),
                new SpriteAnimationImporter.ImportParams(
                        "sprites/character_sprites.png",
                        WALK_ANIMATION_IDS[Constants.WalkAnimations.WALK_N.ordinal()],
                        64, 64,
                        new SpriteAnimationImporter.Idx(0, 8),
                        SpriteAnimationImporter.Orientation.HORIZONTAL,
                        9, false),
                new SpriteAnimationImporter.ImportParams(
                        "sprites/character_sprites.png",
                        WALK_ANIMATION_IDS[Constants.WalkAnimations.WALK_E.ordinal()],
                        64, 64,
                        new SpriteAnimationImporter.Idx(0, 11),
                        SpriteAnimationImporter.Orientation.HORIZONTAL,
                        9, false)
        };
        avatarSprite = SpriteAnimationImporter.importAnimatedSprite(importParams);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        mousePosInScene = pixel2World(new Vector2f(e.getX(), e.getY()));
        avatar.setTargetPos(mousePosInScene);
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    // +++++++++ YOUR TASKS START HERE +++++++++++++++++++++++++++++++++++++++++

    /**
     * Compute the walking animation constant for the current avatar rotation.
     */
    protected Constants.WalkAnimations computeAnimationForOrientation() {
        // rad to degree
        Vector2f orientation = avatar.getOrientation();
        float angle = (float) Math.toDegrees(orientation.getAngle());
        // round to 0-360
        // 90 degrees is north, 0 degrees is east
        angle = (angle + 360) % 360;
        System.out.println(angle);

        // if angle is between 45 and 135, return north. If it is between 135 and 225,
        // return west, etc.
        if (angle >= 45 && angle < 135) {
            return Constants.WalkAnimations.WALK_N;
        } else if (angle >= 135 && angle < 225) {
            return Constants.WalkAnimations.WALK_W;
        } else if (angle >= 225 && angle < 315) {
            return Constants.WalkAnimations.WALK_S;
        } else {
            return Constants.WalkAnimations.WALK_E;
        }
        /*
         * // if angle is between 67.5 and 112.5, return north. If it is between 112.5
         * and
         * // 157.5, return north_west
         * // etc.
         * if (angle >= 67.5 && angle < 112.5) {
         * return Constants.WalkAnimations.WALK_N;
         * } else if (angle >= 112.5 && angle < 157.5) {
         * return Constants.WalkAnimations.WALK_NW;
         * } else if (angle >= 157.5 && angle < 202.5) {
         * return Constants.WalkAnimations.WALK_W;
         * } else if (angle >= 202.5 && angle < 247.5) {
         * return Constants.WalkAnimations.WALK_SW;
         * } else if (angle >= 247.5 && angle < 292.5) {
         * return Constants.WalkAnimations.WALK_S;
         * } else if (angle >= 292.5 && angle < 337.5) {
         * return Constants.WalkAnimations.WALK_SE;
         * } else if (angle >= 337.5 || angle < 22.5) {
         * return Constants.WalkAnimations.WALK_E;
         * } else if (angle >= 22.5 && angle < 67.5) {
         * return Constants.WalkAnimations.WALK_NE;
         * } else {
         * return Constants.WalkAnimations.WALK_E;
         * }
         */
    }

    /**
     * Return a 2D rotation matrix for the static sprite. The sprite's orientation
     * should follow the avatar.
     */
    protected Matrix3f getArrowPose(Avatar avatar, Vector2f spritePos) {
        float x_pos = spritePos.x;
        float y_pos = spritePos.y;
        float x_target = avatar.getPos().x;
        float y_target = avatar.getPos().y;
        Vector2f direction_vector = new Vector2f(x_target - x_pos, y_target - y_pos);
        double angle = Math.atan2(direction_vector.y, direction_vector.x);
        // add half a pie to get the correct rotation
        angle += 1.5 * Math.PI;
        double cos_angle = Math.cos(angle);
        double sin_angle = Math.sin(angle);

        Matrix3f rotationMatrix = new Matrix3f(
                (float) cos_angle, (float) sin_angle, x_pos,
                (float) -sin_angle, (float) cos_angle, y_pos,
                0, 0, 1);
        return rotationMatrix;
    }
}