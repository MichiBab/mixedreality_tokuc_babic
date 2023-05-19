/**
 * Diese Datei ist Teil des Vorgabeframeworks für die Veranstaltung "Mixed Reality"
 * <p>
 * Prof. Dr. Philipp Jenke, Hochschule für Angewandte Wissenschaften Hamburg.
 */

package mixedreality.lab.exercise4;

import com.jme3.math.Matrix3f;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import misc.Logger;

import java.util.*;

/**
 * Implementation of the quadric error metric by Garland and Heckbert.
 */
public class QuadricErrorMetricsSimplification2D {

  /**
   * Processed polygon.
   */
  private Polygon polygon;

  /**
   * List of QEMs for the points.
   */
  protected Map<PolygonVertex, Matrix3f> pointQems;

  /**
   * Collapse information for the edge
   */
  protected Map<PolygonEdge, EdgeCollapse> edgesQems;

  /**
   * Contains the edges ordered bei increasing collapse error.
   */
  protected PriorityQueue<PolygonEdge> queue;

  public QuadricErrorMetricsSimplification2D(Polygon polygon) {
    pointQems = new HashMap<>();
    edgesQems = new HashMap<>();
    queue = new PriorityQueue<PolygonEdge>((e1, e2) -> (getError(e1) < getError(e2)) ? -1 : 1);
    this.polygon = polygon;
    reset();
  }

  protected void reset() {
    pointQems.clear();
    edgesQems.clear();
    queue.clear();

    // Compute QEM for each vertex.
    for (int i = 0; i < polygon.getNumPoints(); i++) {
      PolygonVertex vertex = polygon.getPoint(i);
      pointQems.put(vertex, computePointQem(vertex));
    }
    for (int edgeIndex = 0; edgeIndex < polygon.getNumEdges(); edgeIndex++) {
      PolygonEdge edge = polygon.getEdge(edgeIndex);
      edgesQems.put(edge, computeEdgeCollapseResult(edge));
      queue.add(edge);
    }

    // Debugging: distance matrices
    // for (int i = 0; i < polygon.getNumPoints(); i++) {
    // PolygonVertex vertex = polygon.getPoint(i);
    // PolygonEdge edge = vertex.getIncomingEdge();
    // Matrix3f Q = computeDistanceMatrix(edge);
    // System.out.println("Distance matrix for " + edge);
    // System.out.println(Q);
    // }

    // Debugging: vertex QEMs
    // for (int i = 0; i < polygon.getNumPoints(); i++) {
    // PolygonVertex vertex = polygon.getPoint(i);
    // Matrix3f Q = computePointQem(vertex);
    // System.out.println("QEM for vertex " + vertex);
    // System.out.println(Q);
    // }

    // Debugging: edge QEMs
    // for (int i = 0; i < polygon.getNumPoints(); i++) {
    // PolygonEdge edge = polygon.getEdge(i);
    // EdgeCollapse edgeCollapse = computeEdgeCollapseResult(edge);
    // System.out.println("QEM for edge " + edge);
    // System.out.println(edgeCollapse);
    // }

  }

  /**
   * Computes the initial QEM for an edge.
   */
  protected Matrix3f computeDistanceMatrix(PolygonEdge edge) {
    // Calculate the distancematrix between edge.start and edge.end
    PolygonVertex start = edge.getStartVertex();
    PolygonVertex end = edge.getEndVertex();
    Vector2f startVec = start.getPosition();
    Vector2f endVec = end.getPosition();

    Vector2f edgeVec = endVec.subtract(startVec);
    edgeVec = new Vector2f(edgeVec.y, -1 * edgeVec.x).normalize();
    // Z achse stellt abstand von der kante zum ursprung dar, um die richtung zu
    // erhalten
    Vector3f edgeVec3 = new Vector3f(edgeVec.x, edgeVec.y, -1 * edgeVec.dot(startVec));
    return dyadic(edgeVec3, edgeVec3);
  }

  protected Matrix3f computePointQem(PolygonVertex v) {

    // Die QEM für einen Punkt ist die Summe der QEMs aller Kanten, die an dem Punkt
    // anliegen
    Matrix3f Q = new Matrix3f(0, 0, 0, 0, 0, 00, 0, 0, 0);
    for (PolygonEdge edge : getIncidentEdges(v)) {
      Q = add(Q, computeDistanceMatrix(edge));
    }
    return Q;
  }

  /**
   * Compute the result if the edge is collaped - this is used in the priority
   * queue to select the next edge to
   * collapse.
   */
  protected EdgeCollapse computeEdgeCollapseResult(PolygonEdge edge) {
    PolygonVertex start = edge.getStartVertex();
    PolygonVertex end = edge.getEndVertex();

    // Ableitungsmatrix der Fehlerquadrik
    Matrix3f errorQuadric = add(computePointQem(start), computePointQem(end));
    // Invertierte Ableitungsmatrix
    Matrix3f errorQuadricEdgeDerivativeInverted = errorQuadric.clone().setRow(2, new Vector3f(0, 0, 1)).invert();

    Vector3f newPosition;
    if (!errorQuadricEdgeDerivativeInverted.equals(Matrix3f.ZERO)) {
      // Die invertierte Ableitungsmatrix repräsentiert die partiellen Ableitungen der
      // Fehlerquadrik nach den Koordinaten der Kante. Durch die Multiplikation mit
      // der invertierten Ableitungsmatrix erhalten wir die optimale z-Koordinate für
      // die Kollabierung der Kante
      newPosition = errorQuadricEdgeDerivativeInverted.mult(new Vector3f(0, 0, 1));
    } else {
      newPosition = convert2to3(start.getPosition().add(end.getPosition()).divide(2));
    }
    // Error bestimmt durch das Skalarprodukt von newPosition und errorQuadric
    // eingesetzt mit der newPosition
    float error = newPosition.dot(errorQuadric.mult(newPosition));
    return new EdgeCollapse(error, errorQuadric, convert3to2(newPosition));
  }

  protected PolygonVertex collapse(PolygonEdge edge, Vector2f newPos) {
    if (!(edge instanceof PolygonEdge)) {
      throw new IllegalArgumentException();
    }
    return polygon.collapse(edge, newPos);
  }

  protected List<PolygonEdge> getIncidentEdges(PolygonVertex v) {
    if (!(v instanceof PolygonVertex)) {
      throw new IllegalArgumentException();
    }
    PolygonVertex vertex = (PolygonVertex) v;
    return Arrays.asList(vertex.getIncomingEdge(), vertex.getOutgoingEdge());
  }

  public Matrix3f getQEM4Vertex(PolygonVertex v) {
    return pointQems.get(v);
  }

  /**
   * Apply one simplification step.
   */
  public void simplify() {
    if (polygon.getNumEdges() == 0) {
      Logger.getInstance().error("Cannot collapse with less than 2 points.");
      return;
    }

    PolygonEdge queueEdge = queue.poll();
    PolygonVertex p = collapse(queueEdge, edgesQems.get(queueEdge).newPos);
    pointQems.put(p, edgesQems.get(queueEdge).Q);

    // Update incident edges
    List<PolygonEdge> incidentEdges = getIncidentEdges(p);
    for (PolygonEdge edge : incidentEdges) {
      edgesQems.put(edge, computeEdgeCollapseResult(edge));
    }
  }

  /**
   * Convert from 3d -> 2d.
   */
  public static Vector2f convert3to2(Vector3f p) {
    return new Vector2f(p.x, p.y);
  }

  /**
   * Convert from 2d -> 3d (homogenious).
   */
  public static Vector3f convert2to3(Vector2f p) {
    return new Vector3f(p.x, p.y, 1);
  }

  private double transferFunction(double x) {
    return Math.pow(1 - x, 8);
  }

  /**
   * Returns the error for the specified edge.
   */
  protected double getError(PolygonEdge edge) {
    return edgesQems.get(edge).error;
  }

  /**
   * Compute and return the sum of two matrices.
   */
  protected Matrix3f add(Matrix3f A, Matrix3f B) {
    Matrix3f result = new Matrix3f();
    for (int rowIndex = 0; rowIndex < 3; rowIndex++) {
      for (int colIndex = 0; colIndex < 3; colIndex++) {
        result.set(rowIndex, colIndex, A.get(rowIndex, colIndex) + B.get(rowIndex, colIndex));
      }
    }
    return result;
  }

  /**
   * Compute the dyadic between the two vectors
   */
  protected Matrix3f dyadic(Vector3f v, Vector3f w) {
    Matrix3f result = new Matrix3f();
    for (int rowIndex = 0; rowIndex < 3; rowIndex++) {
      for (int colIndex = 0; colIndex < 3; colIndex++) {
        result.set(rowIndex, colIndex, v.get(rowIndex) * w.get(colIndex));
      }
    }
    return result;
  }

  public static void testFunctionality() {
    Polygon polygon = new Polygon();

    QuadricErrorMetricsSimplification2D simplification = new QuadricErrorMetricsSimplification2D(polygon);
    // punkte: a = (1,1) b = (4,1) c = (4,3) d = (3,4) e = (1,3)
    // kanten: ab, bc, cd, de, ea
    // Erstelle ein neues Polygon
    // Erstelle die Punkte
    polygon.addPoint(new Vector2f(1, 1));
    polygon.addPoint(new Vector2f(4, 1));
    polygon.addPoint(new Vector2f(4, 3));
    polygon.addPoint(new Vector2f(3, 4));
    polygon.addPoint(new Vector2f(1, 3));
    // Erstelle die Kanten
    polygon.addEdge(0, 1);
    polygon.addEdge(1, 2);
    polygon.addEdge(2, 3);
    polygon.addEdge(3, 4);
    polygon.addEdge(4, 0);
    // Teste Abstandsmatrix zwischen b und c
    PolygonEdge edge = polygon.getEdge(1);
    Matrix3f Q = simplification.computeDistanceMatrix(edge);
    System.out.println("Abstandsmatrix b und c:");
    System.out.println(Q);
    assert (Q.equals(new Matrix3f(1, 0, -4, 0, 0, 0, -4, 0, 16)));

    // Teste Fehlerquadrik Vertex Qv_c
    Matrix3f QEM_c = simplification.computePointQem(polygon.getPoint(2));
    Matrix3f erg = new Matrix3f((float) 1.5, (float) 0.5, (float) -7.5, (float) 0.5, (float) 0.5, (float) -3.5,
        (float) -7.5, (float) -3.5, (float) 40.5);
    System.out.println("QEM_c:");
    System.out.println(QEM_c);
    assert (QEM_c.equals(erg));

    // Teste Collapse
    PolygonEdge edge2 = polygon.getEdge(2);
    EdgeCollapse QEM_bc = simplification.computeEdgeCollapseResult(edge2);
    erg = new Matrix3f((float) 2.2, (float) 0.6, (float) -10, (float) 0.6, (float) 1.8, (float) -9, (float) -10,
        (float) -9, (float) 70);
    assert (QEM_bc.Q.equals(erg));
    System.out.println("Neuer Punkt zwischen d und c:");
    System.out.println(QEM_bc.Q);
  }
}
