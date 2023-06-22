/**
 * Diese Datei ist Teil des Vorgabeframeworks für die Veranstaltung "Mixed Reality"
 * <p>
 * Prof. Dr. Philipp Jenke, Hochschule für Angewandte Wissenschaften Hamburg.
 */

package mixedreality.lab.exercise7;

import com.jme3.math.Vector3f;
import mixedreality.base.mesh.TriangleMesh;
import mixedreality.base.mesh.TriangleMeshTools;
import mixedreality.lab.exercise7.functions.ImplicitFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class creates a triangle mesh for a given implicit function using the
 * Marching Cubes algorithm.
 */
public class MarchingCubes {

    /**
     * Corner points of a unit cube
     */
    private final Vector3f[] corners = {
            new Vector3f(-0.5f, -0.5f, -0.5f),
            new Vector3f(0.5f, -0.5f, -0.5f),
            new Vector3f(0.5f, -0.5f, 0.5f),
            new Vector3f(-0.5f, -0.5f, 0.5f),
            new Vector3f(-0.5f, 0.5f, -0.5f),
            new Vector3f(0.5f, 0.5f, -0.5f),
            new Vector3f(0.5f, 0.5f, 0.5f),
            new Vector3f(-0.5f, 0.5f, 0.5f)
    };

    public MarchingCubes() {
    }

    /**
     * Create a mesh for a single cube, given an 8-bit index.
     */
    public Optional<TriangleMesh> getMesh(Index8Bit index, float[] values, float isovalue) {
        TriangleMesh mesh = new TriangleMesh();
        // Lookup table
        int[] list = lookup(index);

        if (index.toInt() == 0 || index.toInt() == 255) {
            return Optional.empty(); // Return empty mesh if no intersection
        }

        /*
         * System.out.println(index);
         * for (int i = 0; i < list.length; i += 1) {
         * if (i == 0) {
         * System.out.print(list[i]);
         * continue;
         * }
         * System.out.print(", " + list[i]);
         * }
         * System.out.println();
         */
        for (int i = 0; i < list.length - 2; i += 1) {
            if (list[i] == -1 || list[i + 1] == -1 || list[i + 2] == -1) {
                continue;
            }
            // Valid triangle
            int cornerIndex1 = list[i];
            int cornerIndex2 = list[i + 1];
            int cornerIndex3 = list[i + 2];

            // Calculate interpolated points for each edge of the triangle
            Vector3f interpolatedPoint1 = getEdgePoint(cornerIndex1, values, isovalue);
            Vector3f interpolatedPoint2 = getEdgePoint(cornerIndex2, values, isovalue);
            Vector3f interpolatedPoint3 = getEdgePoint(cornerIndex3, values, isovalue);

            // Add interpolated points to the `TriangleMesh`
            int vertexIndex1 = mesh.addVertex(interpolatedPoint1);
            int vertexIndex2 = mesh.addVertex(interpolatedPoint2);
            int vertexIndex3 = mesh.addVertex(interpolatedPoint3);

            mesh.addTriangle(vertexIndex1, vertexIndex2, vertexIndex3);
        }

        return Optional.of(mesh);
    }

    private int[] lookup(Index8Bit index) {
        int[] list = new int[15];
        for (int i = 0; i < 15; i++) {
            list[i] = faces[index.toInt() + i];
        }
        return list;
    }

    /**
     * Return the required point on the edge provided by the edgeIndex.
     */
    protected Vector3f getEdgePoint(int edgeIndex, float[] values, float isoValue) {
        if (edgeIndex == 0) {
            return interpolate(0, 1, values, isoValue);
        } else if (edgeIndex == 1) {
            return interpolate(1, 2, values, isoValue);
        } else if (edgeIndex == 2) {
            return interpolate(2, 3, values, isoValue);
        } else if (edgeIndex == 3) {
            return interpolate(3, 0, values, isoValue);
        } else if (edgeIndex == 4) {
            return interpolate(4, 5, values, isoValue);
        } else if (edgeIndex == 5) {
            return interpolate(5, 6, values, isoValue);
        } else if (edgeIndex == 6) {
            return interpolate(6, 7, values, isoValue);
        } else if (edgeIndex == 7) {
            return interpolate(7, 4, values, isoValue);
        } else if (edgeIndex == 8) {
            return interpolate(0, 4, values, isoValue);
        } else if (edgeIndex == 9) {
            return interpolate(1, 5, values, isoValue);
        } else if (edgeIndex == 10) {
            return interpolate(3, 7, values, isoValue);
        } else {
            return interpolate(2, 6, values, isoValue);
        }
    }

    /**
     * Generate a mesh for the complete domain: split domain into a structured
     * grid, tesselate all grid cells and unit all created triangles to a single
     * mesh.
     */

    public TriangleMesh makeMesh(ImplicitFunction f, float isovalue, Vector3f ll, Vector3f ur, int resX, int resY,
            int resZ) {
        TriangleMesh mesh = new TriangleMesh();

        float sizeX = (ur.x - ll.x) / resX;
        float sizeY = (ur.y - ll.y) / resY;
        float sizeZ = (ur.z - ll.z) / resZ;

        int test_cnt = 0;

        for (int i = 0; i < resX; i++) {
            for (int j = 0; j < resY; j++) {
                for (int k = 0; k < resZ; k++) {
                    // Calculate the lower left point of the current subcube
                    Vector3f subcubeLowerLeft = new Vector3f(
                            ll.x + i * sizeX,
                            ll.y + j * sizeY,
                            ll.z + k * sizeZ);

                    // Calculate the 8 corner points of the current subcube
                    Vector3f[] corner_points = new Vector3f[] {
                            subcubeLowerLeft,
                            new Vector3f(subcubeLowerLeft.x + sizeX, subcubeLowerLeft.y, subcubeLowerLeft.z),
                            new Vector3f(subcubeLowerLeft.x + sizeX, subcubeLowerLeft.y + sizeY, subcubeLowerLeft.z),
                            new Vector3f(subcubeLowerLeft.x, subcubeLowerLeft.y + sizeY, subcubeLowerLeft.z),
                            new Vector3f(subcubeLowerLeft.x, subcubeLowerLeft.y, subcubeLowerLeft.z + sizeZ),
                            new Vector3f(subcubeLowerLeft.x + sizeX, subcubeLowerLeft.y, subcubeLowerLeft.z + sizeZ),
                            new Vector3f(subcubeLowerLeft.x + sizeX, subcubeLowerLeft.y + sizeY,
                                    subcubeLowerLeft.z + sizeZ),
                            new Vector3f(subcubeLowerLeft.x, subcubeLowerLeft.y + sizeY, subcubeLowerLeft.z + sizeZ)
                    };

                    // Compute function values for the eight corners
                    float[] values = new float[] {
                            f.eval(corner_points[0]),
                            f.eval(corner_points[1]),
                            f.eval(corner_points[2]),
                            f.eval(corner_points[3]),
                            f.eval(corner_points[4]),
                            f.eval(corner_points[5]),
                            f.eval(corner_points[6]),
                            f.eval(corner_points[7])
                    };

                    // Create the index and convert it to an Index8Bit object
                    int indexValue = getIndexValue(values,
                            isovalue);
                    Index8Bit index = new Index8Bit();
                    index.fromInt(indexValue);

                    // Generate the mesh for the current cube and add it to the main mesh
                    Optional<TriangleMesh> cubeMesh = getMesh(index,
                            values, isovalue);
                    if (cubeMesh.isPresent()) {
                        test_cnt++;

                        TriangleMesh current = cubeMesh.get();
                        current.computeTriangleNormals();
                        // If this prints Invalid triangle - cannot compute normal. then something went
                        // probably wrong with the mesh generation

                        TriangleMeshTools.scale(mesh, sizeX);
                        TriangleMeshTools.translate(current, corner_points[0]);
                        TriangleMeshTools.unite(mesh, current);
                    }
                }
            }
        }
        System.out.println("Number of triangles: " + mesh.getNumberOfTriangles());

        return mesh;
    }

    private int getIndexValue(float[] values, float isovalue) {
        int indexValue = 0;
        if (values[0] > isovalue)
            indexValue += 1;
        if (values[1] > isovalue)
            indexValue += 2;
        if (values[2] > isovalue)
            indexValue += 4;
        if (values[3] > isovalue)
            indexValue += 8;
        if (values[4] > isovalue)
            indexValue += 16;
        if (values[5] > isovalue)
            indexValue += 32;
        if (values[6] > isovalue)
            indexValue += 64;
        if (values[7] > isovalue)
            indexValue += 128;
        return indexValue;
    }

    /**
     * Interpolate a position between p and q.
     */
    private Vector3f interpolate(int aIdx, int bIdx, float[] values, float isoValue) {
        float lambda = values == null ? 0.5f : (isoValue - values[aIdx]) / (values[bIdx] - values[aIdx]);
        Vector3f xP = corners[aIdx].mult(1.0f - lambda);
        Vector3f xQ = corners[bIdx].mult(lambda);
        return xP.add(xQ);
    }

    /**
     * This is the lookup-table. For each configuration (8bit-index) it returns
     * the set of edge-triples which are intersected by the iso-surface. A
     * triangle must be created for each edge-triple.
     */
    public static final int[] faces = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 8, 3, -1, -1, -1,
            -1,
            -1, -1, -1, -1, -1, -1, -1, -1, 0, 1, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 8, 3, 9, 8, 1,
            -1, -1,
            -1, -1, -1, -1, -1, -1, -1, 1, 2, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 8, 3, 1, 2, 11, -1,
            -1,
            -1, -1, -1, -1, -1, -1, -1, 9, 2, 11, 0, 2, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, 2, 8, 3, 2, 11, 8, 11, 9,
            8,
            -1, -1, -1, -1, -1, -1, 3, 10, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 10, 2, 8, 10, 0, -1,
            -1, -1,
            -1, -1, -1, -1, -1, -1, 1, 9, 0, 2, 3, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 10, 2, 1, 9, 10, 9, 8, 10,
            -1,
            -1, -1, -1, -1, -1, 3, 11, 1, 10, 11, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 11, 1, 0, 8, 11, 8, 10, 11,
            -1,
            -1, -1, -1, -1, -1, 3, 9, 0, 3, 10, 9, 10, 11, 9, -1, -1, -1, -1, -1, -1, 9, 8, 11, 11, 8, 10, -1, -1, -1,
            -1, -1,
            -1, -1, -1, -1, 4, 7, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 4, 3, 0, 7, 3, 4, -1, -1, -1, -1,
            -1, -1,
            -1, -1, -1, 0, 1, 9, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, 4, 1, 9, 4, 7, 1, 7, 3, 1, -1, -1, -1, -1,
            -1,
            -1, 1, 2, 11, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, 3, 4, 7, 3, 0, 4, 1, 2, 11, -1, -1, -1, -1, -1,
            -1, 9,
            2, 11, 9, 0, 2, 8, 4, 7, -1, -1, -1, -1, -1, -1, 2, 11, 9, 2, 9, 7, 2, 7, 3, 7, 9, 4, -1, -1, -1, 8, 4, 7,
            3, 10,
            2, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, 4, 7, 10, 2, 4, 2, 0, 4, -1, -1, -1, -1, -1, -1, 9, 0, 1, 8, 4,
            7, 2,
            3, 10, -1, -1, -1, -1, -1, -1, 4, 7, 10, 9, 4, 10, 9, 10, 2, 9, 2, 1, -1, -1, -1, 3, 11, 1, 3, 10, 11, 7, 8,
            4,
            -1, -1, -1, -1, -1, -1, 1, 10, 11, 1, 4, 10, 1, 0, 4, 7, 10, 4, -1, -1, -1, 4, 7, 8, 9, 0, 10, 9, 10, 11,
            10, 0,
            3, -1, -1, -1, 4, 7, 10, 4, 10, 9, 9, 10, 11, -1, -1, -1, -1, -1, -1, 9, 5, 4, -1, -1, -1, -1, -1, -1, -1,
            -1, -1,
            -1, -1, -1, 9, 5, 4, 0, 8, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 5, 4, 1, 5, 0, -1, -1, -1, -1, -1, -1,
            -1,
            -1, -1, 8, 5, 4, 8, 3, 5, 3, 1, 5, -1, -1, -1, -1, -1, -1, 1, 2, 11, 9, 5, 4, -1, -1, -1, -1, -1, -1, -1,
            -1, -1,
            3, 0, 8, 1, 2, 11, 4, 9, 5, -1, -1, -1, -1, -1, -1, 5, 2, 11, 5, 4, 2, 4, 0, 2, -1, -1, -1, -1, -1, -1, 2,
            11, 5,
            3, 2, 5, 3, 5, 4, 3, 4, 8, -1, -1, -1, 9, 5, 4, 2, 3, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 10, 2, 0,
            8, 10,
            4, 9, 5, -1, -1, -1, -1, -1, -1, 0, 5, 4, 0, 1, 5, 2, 3, 10, -1, -1, -1, -1, -1, -1, 2, 1, 5, 2, 5, 8, 2, 8,
            10,
            4, 8, 5, -1, -1, -1, 11, 3, 10, 11, 1, 3, 9, 5, 4, -1, -1, -1, -1, -1, -1, 4, 9, 5, 0, 8, 1, 8, 11, 1, 8,
            10, 11,
            -1, -1, -1, 5, 4, 0, 5, 0, 10, 5, 10, 11, 10, 0, 3, -1, -1, -1, 5, 4, 8, 5, 8, 11, 11, 8, 10, -1, -1, -1,
            -1, -1,
            -1, 9, 7, 8, 5, 7, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, 9, 3, 0, 9, 5, 3, 5, 7, 3, -1, -1, -1, -1, -1, -1,
            0, 7,
            8, 0, 1, 7, 1, 5, 7, -1, -1, -1, -1, -1, -1, 1, 5, 3, 3, 5, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, 9, 7, 8,
            9, 5,
            7, 11, 1, 2, -1, -1, -1, -1, -1, -1, 11, 1, 2, 9, 5, 0, 5, 3, 0, 5, 7, 3, -1, -1, -1, 8, 0, 2, 8, 2, 5, 8,
            5, 7,
            11, 5, 2, -1, -1, -1, 2, 11, 5, 2, 5, 3, 3, 5, 7, -1, -1, -1, -1, -1, -1, 7, 9, 5, 7, 8, 9, 3, 10, 2, -1,
            -1, -1,
            -1, -1, -1, 9, 5, 7, 9, 7, 2, 9, 2, 0, 2, 7, 10, -1, -1, -1, 2, 3, 10, 0, 1, 8, 1, 7, 8, 1, 5, 7, -1, -1,
            -1, 10,
            2, 1, 10, 1, 7, 7, 1, 5, -1, -1, -1, -1, -1, -1, 9, 5, 8, 8, 5, 7, 11, 1, 3, 11, 3, 10, -1, -1, -1, 5, 7, 0,
            5, 0,
            9, 7, 10, 0, 1, 0, 11, 10, 11, 0, 10, 11, 0, 10, 0, 3, 11, 5, 0, 8, 0, 7, 5, 7, 0, 10, 11, 5, 7, 10, 5, -1,
            -1,
            -1, -1, -1, -1, -1, -1, -1, 11, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 8, 3, 5, 11, 6, -1,
            -1,
            -1, -1, -1, -1, -1, -1, -1, 9, 0, 1, 5, 11, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 8, 3, 1, 9, 8, 5, 11,
            6, -1,
            -1, -1, -1, -1, -1, 1, 6, 5, 2, 6, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 6, 5, 1, 2, 6, 3, 0, 8, -1, -1,
            -1,
            -1, -1, -1, 9, 6, 5, 9, 0, 6, 0, 2, 6, -1, -1, -1, -1, -1, -1, 5, 9, 8, 5, 8, 2, 5, 2, 6, 3, 2, 8, -1, -1,
            -1, 2,
            3, 10, 11, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, 0, 8, 10, 2, 0, 11, 6, 5, -1, -1, -1, -1, -1, -1,
            0, 1,
            9, 2, 3, 10, 5, 11, 6, -1, -1, -1, -1, -1, -1, 5, 11, 6, 1, 9, 2, 9, 10, 2, 9, 8, 10, -1, -1, -1, 6, 3, 10,
            6, 5,
            3, 5, 1, 3, -1, -1, -1, -1, -1, -1, 0, 8, 10, 0, 10, 5, 0, 5, 1, 5, 10, 6, -1, -1, -1, 3, 10, 6, 0, 3, 6, 0,
            6, 5,
            0, 5, 9, -1, -1, -1, 6, 5, 9, 6, 9, 10, 10, 9, 8, -1, -1, -1, -1, -1, -1, 5, 11, 6, 4, 7, 8, -1, -1, -1, -1,
            -1,
            -1, -1, -1, -1, 4, 3, 0, 4, 7, 3, 6, 5, 11, -1, -1, -1, -1, -1, -1, 1, 9, 0, 5, 11, 6, 8, 4, 7, -1, -1, -1,
            -1,
            -1, -1, 11, 6, 5, 1, 9, 7, 1, 7, 3, 7, 9, 4, -1, -1, -1, 6, 1, 2, 6, 5, 1, 4, 7, 8, -1, -1, -1, -1, -1, -1,
            1, 2,
            5, 5, 2, 6, 3, 0, 4, 3, 4, 7, -1, -1, -1, 8, 4, 7, 9, 0, 5, 0, 6, 5, 0, 2, 6, -1, -1, -1, 7, 3, 9, 7, 9, 4,
            3, 2,
            9, 5, 9, 6, 2, 6, 9, 3, 10, 2, 7, 8, 4, 11, 6, 5, -1, -1, -1, -1, -1, -1, 5, 11, 6, 4, 7, 2, 4, 2, 0, 2, 7,
            10,
            -1, -1, -1, 0, 1, 9, 4, 7, 8, 2, 3, 10, 5, 11, 6, -1, -1, -1, 9, 2, 1, 9, 10, 2, 9, 4, 10, 7, 10, 4, 5, 11,
            6, 8,
            4, 7, 3, 10, 5, 3, 5, 1, 5, 10, 6, -1, -1, -1, 5, 1, 10, 5, 10, 6, 1, 0, 10, 7, 10, 4, 0, 4, 10, 0, 5, 9, 0,
            6, 5,
            0, 3, 6, 10, 6, 3, 8, 4, 7, 6, 5, 9, 6, 9, 10, 4, 7, 9, 7, 10, 9, -1, -1, -1, 11, 4, 9, 6, 4, 11, -1, -1,
            -1, -1,
            -1, -1, -1, -1, -1, 4, 11, 6, 4, 9, 11, 0, 8, 3, -1, -1, -1, -1, -1, -1, 11, 0, 1, 11, 6, 0, 6, 4, 0, -1,
            -1, -1,
            -1, -1, -1, 8, 3, 1, 8, 1, 6, 8, 6, 4, 6, 1, 11, -1, -1, -1, 1, 4, 9, 1, 2, 4, 2, 6, 4, -1, -1, -1, -1, -1,
            -1, 3,
            0, 8, 1, 2, 9, 2, 4, 9, 2, 6, 4, -1, -1, -1, 0, 2, 4, 4, 2, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, 8, 3, 2,
            8, 2,
            4, 4, 2, 6, -1, -1, -1, -1, -1, -1, 11, 4, 9, 11, 6, 4, 10, 2, 3, -1, -1, -1, -1, -1, -1, 0, 8, 2, 2, 8, 10,
            4, 9,
            11, 4, 11, 6, -1, -1, -1, 3, 10, 2, 0, 1, 6, 0, 6, 4, 6, 1, 11, -1, -1, -1, 6, 4, 1, 6, 1, 11, 4, 8, 1, 2,
            1, 10,
            8, 10, 1, 9, 6, 4, 9, 3, 6, 9, 1, 3, 10, 6, 3, -1, -1, -1, 8, 10, 1, 8, 1, 0, 10, 6, 1, 9, 1, 4, 6, 4, 1, 3,
            10,
            6, 3, 6, 0, 0, 6, 4, -1, -1, -1, -1, -1, -1, 6, 4, 8, 10, 6, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, 7, 11,
            6, 7,
            8, 11, 8, 9, 11, -1, -1, -1, -1, -1, -1, 0, 7, 3, 0, 11, 7, 0, 9, 11, 6, 7, 11, -1, -1, -1, 11, 6, 7, 1, 11,
            7, 1,
            7, 8, 1, 8, 0, -1, -1, -1, 11, 6, 7, 11, 7, 1, 1, 7, 3, -1, -1, -1, -1, -1, -1, 1, 2, 6, 1, 6, 8, 1, 8, 9,
            8, 6,
            7, -1, -1, -1, 2, 6, 9, 2, 9, 1, 6, 7, 9, 0, 9, 3, 7, 3, 9, 7, 8, 0, 7, 0, 6, 6, 0, 2, -1, -1, -1, -1, -1,
            -1, 7,
            3, 2, 6, 7, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, 2, 3, 10, 11, 6, 8, 11, 8, 9, 8, 6, 7, -1, -1, -1, 2, 0,
            7, 2,
            7, 10, 0, 9, 7, 6, 7, 11, 9, 11, 7, 1, 8, 0, 1, 7, 8, 1, 11, 7, 6, 7, 11, 2, 3, 10, 10, 2, 1, 10, 1, 7, 11,
            6, 1,
            6, 7, 1, -1, -1, -1, 8, 9, 6, 8, 6, 7, 9, 1, 6, 10, 6, 3, 1, 3, 6, 0, 9, 1, 10, 6, 7, -1, -1, -1, -1, -1,
            -1, -1,
            -1, -1, 7, 8, 0, 7, 0, 6, 3, 10, 0, 10, 6, 0, -1, -1, -1, 7, 10, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1,
            -1, 7, 6, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 3, 0, 8, 10, 7, 6, -1, -1, -1, -1, -1, -1, -1,
            -1,
            -1, 0, 1, 9, 10, 7, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, 8, 1, 9, 8, 3, 1, 10, 7, 6, -1, -1, -1, -1, -1,
            -1, 11,
            1, 2, 6, 10, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 2, 11, 3, 0, 8, 6, 10, 7, -1, -1, -1, -1, -1, -1, 2,
            9, 0,
            2, 11, 9, 6, 10, 7, -1, -1, -1, -1, -1, -1, 6, 10, 7, 2, 11, 3, 11, 8, 3, 11, 9, 8, -1, -1, -1, 7, 2, 3, 6,
            2, 7,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, 7, 0, 8, 7, 6, 0, 6, 2, 0, -1, -1, -1, -1, -1, -1, 2, 7, 6, 2, 3, 7, 0,
            1, 9,
            -1, -1, -1, -1, -1, -1, 1, 6, 2, 1, 8, 6, 1, 9, 8, 8, 7, 6, -1, -1, -1, 11, 7, 6, 11, 1, 7, 1, 3, 7, -1, -1,
            -1,
            -1, -1, -1, 11, 7, 6, 1, 7, 11, 1, 8, 7, 1, 0, 8, -1, -1, -1, 0, 3, 7, 0, 7, 11, 0, 11, 9, 6, 11, 7, -1, -1,
            -1,
            7, 6, 11, 7, 11, 8, 8, 11, 9, -1, -1, -1, -1, -1, -1, 6, 8, 4, 10, 8, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            3, 6,
            10, 3, 0, 6, 0, 4, 6, -1, -1, -1, -1, -1, -1, 8, 6, 10, 8, 4, 6, 9, 0, 1, -1, -1, -1, -1, -1, -1, 9, 4, 6,
            9, 6,
            3, 9, 3, 1, 10, 3, 6, -1, -1, -1, 6, 8, 4, 6, 10, 8, 2, 11, 1, -1, -1, -1, -1, -1, -1, 1, 2, 11, 3, 0, 10,
            0, 6,
            10, 0, 4, 6, -1, -1, -1, 4, 10, 8, 4, 6, 10, 0, 2, 9, 2, 11, 9, -1, -1, -1, 11, 9, 3, 11, 3, 2, 9, 4, 3, 10,
            3, 6,
            4, 6, 3, 8, 2, 3, 8, 4, 2, 4, 6, 2, -1, -1, -1, -1, -1, -1, 0, 4, 2, 4, 6, 2, -1, -1, -1, -1, -1, -1, -1,
            -1, -1,
            1, 9, 0, 2, 3, 4, 2, 4, 6, 4, 3, 8, -1, -1, -1, 1, 9, 4, 1, 4, 2, 2, 4, 6, -1, -1, -1, -1, -1, -1, 8, 1, 3,
            8, 6,
            1, 8, 4, 6, 6, 11, 1, -1, -1, -1, 11, 1, 0, 11, 0, 6, 6, 0, 4, -1, -1, -1, -1, -1, -1, 4, 6, 3, 4, 3, 8, 6,
            11, 3,
            0, 3, 9, 11, 9, 3, 11, 9, 4, 6, 11, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, 4, 9, 5, 7, 6, 10, -1, -1, -1,
            -1, -1,
            -1, -1, -1, -1, 0, 8, 3, 4, 9, 5, 10, 7, 6, -1, -1, -1, -1, -1, -1, 5, 0, 1, 5, 4, 0, 7, 6, 10, -1, -1, -1,
            -1,
            -1, -1, 10, 7, 6, 8, 3, 4, 3, 5, 4, 3, 1, 5, -1, -1, -1, 9, 5, 4, 11, 1, 2, 7, 6, 10, -1, -1, -1, -1, -1,
            -1, 6,
            10, 7, 1, 2, 11, 0, 8, 3, 4, 9, 5, -1, -1, -1, 7, 6, 10, 5, 4, 11, 4, 2, 11, 4, 0, 2, -1, -1, -1, 3, 4, 8,
            3, 5,
            4, 3, 2, 5, 11, 5, 2, 10, 7, 6, 7, 2, 3, 7, 6, 2, 5, 4, 9, -1, -1, -1, -1, -1, -1, 9, 5, 4, 0, 8, 6, 0, 6,
            2, 6,
            8, 7, -1, -1, -1, 3, 6, 2, 3, 7, 6, 1, 5, 0, 5, 4, 0, -1, -1, -1, 6, 2, 8, 6, 8, 7, 2, 1, 8, 4, 8, 5, 1, 5,
            8, 9,
            5, 4, 11, 1, 6, 1, 7, 6, 1, 3, 7, -1, -1, -1, 1, 6, 11, 1, 7, 6, 1, 0, 7, 8, 7, 0, 9, 5, 4, 4, 0, 11, 4, 11,
            5, 0,
            3, 11, 6, 11, 7, 3, 7, 11, 7, 6, 11, 7, 11, 8, 5, 4, 11, 4, 8, 11, -1, -1, -1, 6, 9, 5, 6, 10, 9, 10, 8, 9,
            -1,
            -1, -1, -1, -1, -1, 3, 6, 10, 0, 6, 3, 0, 5, 6, 0, 9, 5, -1, -1, -1, 0, 10, 8, 0, 5, 10, 0, 1, 5, 5, 6, 10,
            -1,
            -1, -1, 6, 10, 3, 6, 3, 5, 5, 3, 1, -1, -1, -1, -1, -1, -1, 1, 2, 11, 9, 5, 10, 9, 10, 8, 10, 5, 6, -1, -1,
            -1, 0,
            10, 3, 0, 6, 10, 0, 9, 6, 5, 6, 9, 1, 2, 11, 10, 8, 5, 10, 5, 6, 8, 0, 5, 11, 5, 2, 0, 2, 5, 6, 10, 3, 6, 3,
            5, 2,
            11, 3, 11, 5, 3, -1, -1, -1, 5, 8, 9, 5, 2, 8, 5, 6, 2, 3, 8, 2, -1, -1, -1, 9, 5, 6, 9, 6, 0, 0, 6, 2, -1,
            -1,
            -1, -1, -1, -1, 1, 5, 8, 1, 8, 0, 5, 6, 8, 3, 8, 2, 6, 2, 8, 1, 5, 6, 2, 1, 6, -1, -1, -1, -1, -1, -1, -1,
            -1, -1,
            1, 3, 6, 1, 6, 11, 3, 8, 6, 5, 6, 9, 8, 9, 6, 11, 1, 0, 11, 0, 6, 9, 5, 0, 5, 6, 0, -1, -1, -1, 0, 3, 8, 5,
            6, 11,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, 11, 5, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, 5, 11, 7,
            5, 10,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, 5, 11, 10, 7, 5, 8, 3, 0, -1, -1, -1, -1, -1, -1, 5, 10, 7, 5, 11,
            10, 1,
            9, 0, -1, -1, -1, -1, -1, -1, 11, 7, 5, 11, 10, 7, 9, 8, 1, 8, 3, 1, -1, -1, -1, 10, 1, 2, 10, 7, 1, 7, 5,
            1, -1,
            -1, -1, -1, -1, -1, 0, 8, 3, 1, 2, 7, 1, 7, 5, 7, 2, 10, -1, -1, -1, 9, 7, 5, 9, 2, 7, 9, 0, 2, 2, 10, 7,
            -1, -1,
            -1, 7, 5, 2, 7, 2, 10, 5, 9, 2, 3, 2, 8, 9, 8, 2, 2, 5, 11, 2, 3, 5, 3, 7, 5, -1, -1, -1, -1, -1, -1, 8, 2,
            0, 8,
            5, 2, 8, 7, 5, 11, 2, 5, -1, -1, -1, 9, 0, 1, 5, 11, 3, 5, 3, 7, 3, 11, 2, -1, -1, -1, 9, 8, 2, 9, 2, 1, 8,
            7, 2,
            11, 2, 5, 7, 5, 2, 1, 3, 5, 3, 7, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 8, 7, 0, 7, 1, 1, 7, 5, -1, -1,
            -1,
            -1, -1, -1, 9, 0, 3, 9, 3, 5, 5, 3, 7, -1, -1, -1, -1, -1, -1, 9, 8, 7, 5, 9, 7, -1, -1, -1, -1, -1, -1, -1,
            -1,
            -1, 5, 8, 4, 5, 11, 8, 11, 10, 8, -1, -1, -1, -1, -1, -1, 5, 0, 4, 5, 10, 0, 5, 11, 10, 10, 3, 0, -1, -1,
            -1, 0,
            1, 9, 8, 4, 11, 8, 11, 10, 11, 4, 5, -1, -1, -1, 11, 10, 4, 11, 4, 5, 10, 3, 4, 9, 4, 1, 3, 1, 4, 2, 5, 1,
            2, 8,
            5, 2, 10, 8, 4, 5, 8, -1, -1, -1, 0, 4, 10, 0, 10, 3, 4, 5, 10, 2, 10, 1, 5, 1, 10, 0, 2, 5, 0, 5, 9, 2, 10,
            5, 4,
            5, 8, 10, 8, 5, 9, 4, 5, 2, 10, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, 2, 5, 11, 3, 5, 2, 3, 4, 5, 3, 8, 4,
            -1,
            -1, -1, 5, 11, 2, 5, 2, 4, 4, 2, 0, -1, -1, -1, -1, -1, -1, 3, 11, 2, 3, 5, 11, 3, 8, 5, 4, 5, 8, 0, 1, 9,
            5, 11,
            2, 5, 2, 4, 1, 9, 2, 9, 4, 2, -1, -1, -1, 8, 4, 5, 8, 5, 3, 3, 5, 1, -1, -1, -1, -1, -1, -1, 0, 4, 5, 1, 0,
            5, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, 8, 4, 5, 8, 5, 3, 9, 0, 5, 0, 3, 5, -1, -1, -1, 9, 4, 5, -1, -1, -1, -1, -1,
            -1,
            -1, -1, -1, -1, -1, -1, 4, 10, 7, 4, 9, 10, 9, 11, 10, -1, -1, -1, -1, -1, -1, 0, 8, 3, 4, 9, 7, 9, 10, 7,
            9, 11,
            10, -1, -1, -1, 1, 11, 10, 1, 10, 4, 1, 4, 0, 7, 4, 10, -1, -1, -1, 3, 1, 4, 3, 4, 8, 1, 11, 4, 7, 4, 10,
            11, 10,
            4, 4, 10, 7, 9, 10, 4, 9, 2, 10, 9, 1, 2, -1, -1, -1, 9, 7, 4, 9, 10, 7, 9, 1, 10, 2, 10, 1, 0, 8, 3, 10, 7,
            4,
            10, 4, 2, 2, 4, 0, -1, -1, -1, -1, -1, -1, 10, 7, 4, 10, 4, 2, 8, 3, 4, 3, 2, 4, -1, -1, -1, 2, 9, 11, 2, 7,
            9, 2,
            3, 7, 7, 4, 9, -1, -1, -1, 9, 11, 7, 9, 7, 4, 11, 2, 7, 8, 7, 0, 2, 0, 7, 3, 7, 11, 3, 11, 2, 7, 4, 11, 1,
            11, 0,
            4, 0, 11, 1, 11, 2, 8, 7, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, 4, 9, 1, 4, 1, 7, 7, 1, 3, -1, -1, -1, -1,
            -1,
            -1, 4, 9, 1, 4, 1, 7, 0, 8, 1, 8, 7, 1, -1, -1, -1, 4, 0, 3, 7, 4, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, 4,
            8, 7,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 9, 11, 8, 11, 10, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, 3,
            0, 9,
            3, 9, 10, 10, 9, 11, -1, -1, -1, -1, -1, -1, 0, 1, 11, 0, 11, 8, 8, 11, 10, -1, -1, -1, -1, -1, -1, 3, 1,
            11, 10,
            3, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 2, 10, 1, 10, 9, 9, 10, 8, -1, -1, -1, -1, -1, -1, 3, 0, 9, 3,
            9,
            10, 1, 2, 9, 2, 10, 9, -1, -1, -1, 0, 2, 10, 8, 0, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, 3, 2, 10, -1, -1,
            -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, 2, 3, 8, 2, 8, 11, 11, 8, 9, -1, -1, -1, -1, -1, -1, 9, 11, 2, 0, 9, 2,
            -1,
            -1, -1, -1, -1, -1, -1, -1, -1, 2, 3, 8, 2, 8, 11, 0, 1, 8, 1, 11, 8, -1, -1, -1, 1, 11, 2, -1, -1, -1, -1,
            -1,
            -1, -1, -1, -1, -1, -1, -1, 1, 3, 8, 9, 1, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 9, 1, -1, -1, -1, -1,
            -1, -1,
            -1, -1, -1, -1, -1, -1, 0, 3, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1,
            -1, -1, -1, -1, -1, -1, -1 };

}
