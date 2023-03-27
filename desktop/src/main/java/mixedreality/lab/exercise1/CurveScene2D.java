/**
 * Diese Datei ist Teil des Vorgabeframeworks für die Veranstaltung "Mixed Reality"
 * <p>
 * Prof. Dr. Philipp Jenke, Hochschule für Angewandte Wissenschaften Hamburg.
 */

package mixedreality.lab.exercise1;

import com.jme3.math.Vector2f;
import mixedreality.base.math.ConvexHull2D;
import mixedreality.base.math.Curve;
import ui.Scene2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * 2D scene to display a 2D curve and some of its properties.
 */
public class CurveScene2D extends Scene2D {

  /**
   * Constant used to control the look-and-feel.
   */
  protected static final int CURVE_RESOLUTION = 50;
  protected static final Color CONTROL_POINT_COLOR = new Color(200, 160, 60);
  protected static final Color CURVE_COLOR = new Color(80, 160, 180);
  protected static final Color CURRENT_POINT_COLOR = new Color(14, 167, 181);// new Color(238,186,48);
  protected static final Color CONVEX_HULL_COLOR = new Color(220, 220, 220);
  protected static final float TANGENT_LENGTH = 0.5f;
  protected static final float SELECT_ACCURACY = 10.0f;

  /**
   * Current parameter value
   */
  private float t;

  /**
   * Curve to be drawn
   */
  private Curve curve;

  /**
   * Display settings: convex hull
   */
  boolean showConvexHull = true;

  /**
   * Display settings: control points
   */
  boolean showControlPoints = true;

  /**
   * Display settings: control polygon
   */
  boolean showControlPolygon = true;

  /**
   * Display settings: curve
   */
  boolean showCurve = true;

  public CurveScene2D(int width, int height, Curve curve) {
    super(width, height);
    this.curve = curve;
    this.t = 0.5f;

    setupListeners();
  }

  @Override
  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    drawCurve(g2);
  }

  @Override
  public String getTitle() {
    return "Curves";
  }

  @Override
  public JPanel getUserInterface() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    // Control t
    panel.add(new JLabel("t:"));
    JSlider sliderT = new JSlider();
    sliderT.addChangeListener(e -> {
      t = sliderT.getValue() / 100.0f;
      repaint();
    });
    panel.add(sliderT);

    // Curve
    JCheckBox checkBoxCurve = new JCheckBox("Show curve");
    checkBoxCurve.setSelected(showCurve);
    checkBoxCurve.addActionListener(e -> {
      showCurve = checkBoxCurve.isSelected();
      repaint();
    });
    panel.add(checkBoxCurve);

    // Convex hull
    JCheckBox checkBoxConvexHull = new JCheckBox("Show convex hull");
    checkBoxConvexHull.setSelected(showConvexHull);
    checkBoxConvexHull.addActionListener(e -> {
      showConvexHull = checkBoxConvexHull.isSelected();
      repaint();
    });
    panel.add(checkBoxConvexHull);

    // Control points
    JCheckBox checkBoxControlPoints = new JCheckBox("Showm control points");
    checkBoxControlPoints.setSelected(showControlPoints);
    checkBoxControlPoints.addActionListener(e -> {
      showControlPoints = checkBoxControlPoints.isSelected();
      repaint();
    });
    panel.add(checkBoxControlPoints);

    // Control polygon
    JCheckBox checkBoxControlPolygon = new JCheckBox("Show control polygon");
    checkBoxControlPolygon.setSelected(showControlPolygon);
    checkBoxControlPolygon.addActionListener(e -> {
      showControlPolygon = checkBoxControlPolygon.isSelected();
      repaint();
    });
    panel.add(checkBoxControlPolygon);

    return panel;
  }

  /**
   * Setup listeners - used for user interaction.
   */
  public void setupListeners() {
    addMouseMotionListener(new MouseMotionListener() {
      @Override
      public void mouseDragged(MouseEvent e) {
        int cpIndex = getClosestControlPointIndex(e.getX(), e.getY());
        if (cpIndex >= 0) {
          Vector2f worldPos = pixel2World(new Vector2f(e.getX(), e.getY()));
        }
      }

      @Override
      public void mouseMoved(MouseEvent e) {

      }
    });

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        int cpIndex = getClosestControlPointIndex(e.getX(), e.getY());
        // One could handle a picked control point here ...
      }
    });
  }

  /**
   * Return the control point position for drawing in world coordinates. Special
   * case:
   * tangents in Hermite curve.
   */
  private Vector2f getControlPointPositionForDraw(int index) {
    Vector2f cp = curve.getControlPoint(index);
    return cp;
  }

  /**
   * Return the index of the closest control point to the given coordinates.
   */
  protected int getClosestControlPointIndex(int x, int y) {
    Vector2f point = new Vector2f(x, y);
    int minIndex = -1;
    double minDistance = -1;
    for (int i = 0; i <= curve.getDegree(); i++) {
      Vector2f cp = getControlPointPositionForDraw(i);
      Vector2f cpPixel = world2Pixel(cp);
      double dist = cpPixel.subtract(point).length();
      if ((minDistance < 0 || dist < minDistance) && (dist < SELECT_ACCURACY)) {
        minDistance = dist;
        minIndex = i;
      }
    }
    return minIndex;
  }

  /**
   * Draw the curve and its properties on the canvas.
   */
  private void drawCurve(Graphics gc) {
    gc.setColor(Color.WHITE);
    gc.clearRect(0, 0, getWidth(), getHeight());

    // Draw convex hull
    if (showConvexHull) {
      Vector2f[] controllPoints = new Vector2f[curve.getDegree() + 1];
      for (int i = 0; i <= curve.getDegree(); i++) {
        controllPoints[i] = curve.getControlPoint(i);
      }
      Vector2f[] chPoints = ConvexHull2D.convex_hull(controllPoints);
      int[] xPoints = new int[chPoints.length];
      int[] yPoints = new int[chPoints.length];
      for (int i = 0; i < chPoints.length; i++) {
        Vector2f pPixel = world2Pixel(chPoints[i]);
        xPoints[i] = (int) pPixel.x;
        yPoints[i] = (int) pPixel.y;
      }
      gc.setColor(CONVEX_HULL_COLOR);
      gc.fillPolygon(xPoints, yPoints, xPoints.length);
    }

    // Draw control points
    if (showControlPoints) {
      for (int i = 0; i <= curve.getDegree(); i++) {
        drawPoint(gc, getControlPointPositionForDraw(i), CONTROL_POINT_COLOR);
      }
    }

    // Control polygon
    if (showControlPolygon) {
      for (int i = 0; i < curve.getDegree(); i++) {
        Vector2f a = world2Pixel(
            curve.getControlPoint(i));
        Vector2f b = world2Pixel(curve.getControlPoint(i + 1));
        gc.setColor(CONTROL_POINT_COLOR);
        gc.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
      }
    }

    // Draw curve
    if (showCurve) {
      Vector2f lastCurvePoint = null;
      gc.setColor(CURVE_COLOR);
      for (int i = 0; i < CURVE_RESOLUTION; i++) {
        float t = i / (CURVE_RESOLUTION - 1.0f);
        Vector2f p_t = curve.eval(t);
        Vector2f p = world2Pixel(p_t);
        if (lastCurvePoint != null) {
          gc.drawLine((int) lastCurvePoint.x, (int) lastCurvePoint.y, (int) p.x, (int) p.y);
        }
        lastCurvePoint = p;
      }
    }

    // Draw currentPoint + tangent
    boolean showCurrentPoint = true;
    if (showCurrentPoint) {
      Vector2f currentPoint = curve.eval(t);
      drawPoint(gc, currentPoint, CURRENT_POINT_COLOR);

      // Draw tangent
      Vector2f tangent = curve.evalDerivative(t).normalize().mult(TANGENT_LENGTH);
      Vector2f a = world2Pixel(currentPoint);
      Vector2f b = world2Pixel(currentPoint.add(tangent));
      gc.setColor(CURRENT_POINT_COLOR);
      gc.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
    }
  }
}
