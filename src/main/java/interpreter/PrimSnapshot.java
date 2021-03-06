package interpreter;

import lombok.Data;
import model.Demo;
import model.Edge;
import model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Undirected graph algorithm, so edges need matching reverse edge
// Ignores demo.end() since algorithm generates a minimum spanning tree

@Data
public class PrimSnapshot implements Snapshot {
    /*
        Prim's algorithm:
            tl;dr Like Dijkstra's but priority queue is not path cost but edge cost
            i.e. keep growing the tree by adding the closest neighbor that's not in the tree yet
    */
    List<Edge> tree = new ArrayList<>();
    List<Node> remainingNodes = new ArrayList<>();
    List<Edge> remainingEdges = new ArrayList<>();
    Map<Node, Boolean> isInTree = new HashMap<>();
    Node current; // guaranteed to be in tree
    Boolean canContinue = true;

    public PrimSnapshot(Demo demo) {
        demo.getGraph().getNodes().forEach(node -> {
            // Put the start node into the tree, set current to it;
            // and everything else goes into remainingNodes
            if (node.equals(demo.getStart())) {
                current = node;
                isInTree.put(node, true);
            } else {
                remainingNodes.add(node);
                isInTree.put(node, false);
            }
        });
        remainingEdges.addAll(demo.getGraph().getEdges());
        // ensure all edges have a reversed edge
        for (Edge edge : remainingEdges) {
            if (!remainingEdges.contains(Edge.of(edge.getEnd(), edge.getStart(), edge.getWeight()))) {
                throw new InterpreterException("Prim's Algorithm requires an undirected graph (bidirectional edges)");
            }
        }
    }

    public boolean isOver() {
        return remainingNodes.size() == 0 || remainingEdges.size() == 0 || !canContinue;
    }

    /**
     * Returns the minimum edge from the tree to the node
     * returns null if none exists
     */
    private Edge minEdgeFromTree(Node node) {
        // Search in the remainingEdges that point to n.
        Edge edge = null;
        double minEdgeLength = Double.POSITIVE_INFINITY;
        // Check all remaining edges. If it comes from the tree to the destination node n,
        // take the minimum such edge and return it.
        for (Edge e : remainingEdges) {
            if (isInTree.get(e.getStart()) && e.getEnd().equals(node)) {
                if (e.getWeight() < minEdgeLength) {
                    minEdgeLength = e.getWeight();
                    edge = e;
                }
            }
        }
        return edge;
    }


    /**
     * Take a step in Prim's algorithm, greedily adding the next edge and node to the MST
     */
    public void step() {
        // Find the next edge
        Edge nextEdge = getNextEdge();
        if (nextEdge == null) {
            canContinue = false;
        } else {
            tree.add(nextEdge);
            // also add reverse edge
            tree.add(Edge.of(nextEdge.getEnd(), nextEdge.getStart(), nextEdge.getWeight()));
            remainingEdges.remove(nextEdge);
            Node nextNode = nextEdge.getEnd();
            current = nextNode;
            remainingNodes.remove(nextNode);
            isInTree.put(nextNode, true);
        }
    }

    /**
     * Returns the next edge in the growing MST (mininum spanning tree)
     */
    private Edge getNextEdge() {
        Edge minEdge = null;
        double minLength = Double.POSITIVE_INFINITY;
        // Search in all remaining nodes.
        for (Node node : remainingNodes) {
            Edge minEdgeFromTreeToNode = minEdgeFromTree(node);
            if (minEdgeFromTreeToNode != null) {
                double distFromTree = minEdgeFromTreeToNode.getWeight();
                if (distFromTree < minLength) {
                    minLength = distFromTree;
                    minEdge = minEdgeFromTreeToNode;
                }
            }
        }
        return minEdge;
    }
}