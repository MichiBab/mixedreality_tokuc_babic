/**
 * Diese Datei ist Teil des Vorgabeframeworks für die Veranstaltung "Mixed Reality"
 * <p>
 * Prof. Dr. Philipp Jenke, Hochschule für Angewandte Wissenschaften Hamburg.
 */

package mixedreality.lab.exercise4;

import com.jme3.math.Vector2f;
import mixedreality.base.math.BoundingBox2D;
import ui.Scene2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Locale;

/**
 * Drawing canvas for a 2D polygon simplification using quadric error metrics
 */
public class SimplificationScene extends Scene2D {

  /**
   * This polygon shall be simplified.
   */
  private Polygon polygon;

  /**
   * Implementation of the quadric error metrics in 2D.
   */
  private QuadricErrorMetricsSimplification2D qem;

  /**
   * Selection of the dataset.
   */
  private JComboBox<String> cbPoly;

  public SimplificationScene(int width, int height) {
    super(width, height, new Vector2f(-1, -1), new Vector2f(5, 5));
    lastMousePosition = null;

    this.polygon = new Polygon();
    qem = new QuadricErrorMetricsSimplification2D(polygon);

    setupListeners();
  }

  /**
   * Reset the polygon
   */
  private void reset() {
    polygon.readFromFile((String) cbPoly.getSelectedItem());
    BoundingBox2D bbox2D = polygon.getBBox();
    setRenderArea(bbox2D.getLL().subtract(new Vector2f(0.5f, 0.5f)),
        bbox2D.getUR().add(new Vector2f(0.5f, 0.5f)));
    qem.reset();
    repaint();
  }

  @Override
  public void paint(Graphics g) {
    g.clearRect(0, 0, getWidth(), getHeight());

    Graphics2D g2 = (Graphics2D) g;

    Font oldFont = g2.getFont();
    Font font = new Font("Courier New", oldFont.getStyle(), 9);
    g2.setFont(font);
    drawPolygon(g2, polygon);
  }

  @Override
  public String getTitle() {
    return "Simplification";
  }

  @Override
  public JPanel getUserInterface() {
    JPanel mainPanel = new JPanel();

    Box box = Box.createVerticalBox();
    mainPanel.add(box);

    JButton buttonSimplify = new JButton("Simplify");
    buttonSimplify.addActionListener(e -> {
      qem.simplify();
      repaint();
    });
    box.add(buttonSimplify);

    cbPoly = new JComboBox<>();
    cbPoly.addItem("polygons/simple.polygon");
    cbPoly.addItem("polygons/hamburg.polygon");
    cbPoly.setSelectedIndex(0);
    cbPoly.addActionListener(e -> reset());
    box.add(cbPoly);

    JButton buttonReset = new JButton("Reset");
    buttonReset.addActionListener(e -> {
      reset();
      repaint();
    });
    box.add(buttonReset);

    box.add(Box.createGlue());
    reset();
    return mainPanel;
  }

  /**
   * Setup listeners - used for user interaction.
   */
  public void setupListeners() {
    addMouseMotionListener(new MouseMotionListener() {
      @Override
      public void mouseDragged(MouseEvent e) {
        Vector2f mPos = new Vector2f(e.getX(), e.getY());
        if (lastMousePosition != null) {
          float dx = mPos.x - lastMousePosition.x;
          float dy = mPos.y - lastMousePosition.y;

          repaint();
        }
        lastMousePosition = mPos;
      }

      @Override
      public void mouseMoved(MouseEvent e) {
      }
    });

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        lastMousePosition = null;
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        lastMousePosition = null;
      }
    });
  }

  /**
   * Draw the polygon on the canvas.
   */
  private void drawPolygon(Graphics2D g2, Polygon polygon) {
    for (int i = 0; i < polygon.getNumEdges(); i++) {
      drawEdge(g2, polygon.getEdge(i));
    }
  }

  /**
   * Draw an edge on the canvas.
   */
  private void drawEdge(Graphics2D g2, PolygonEdge edge) {
    PolygonVertex start = edge.getStartVertex();
    PolygonVertex end = edge.getEndVertex();
    drawVertex(g2, start);

    drawLine(g2, start.getPosition(), end.getPosition(), Color.BLACK);
  }

  /**
   * Draw a vertex on the canvas.
   */
  private void drawVertex(Graphics2D g2, PolygonVertex v) {
    drawPoint(g2, v.getPosition(), Color.BLUE);
    // displayQEM(g2, v.getPosition(), simplification2D.getQEM4Vertex(v));
    displayPos(g2, v.getPosition());
  }

  /**
   * Display position coordinates.
   */
  private void displayPos(Graphics2D g2, Vector2f p) {
    String txt = String.format(Locale.US, "(%.1f,%.1f)", p.x, p.y);
    p = world2Pixel(p);
    g2.drawString(txt, (int) p.x - 60, (int) p.y);
  }
}
