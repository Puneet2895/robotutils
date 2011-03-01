/*
 * This file is part of the TimeFinder project.
 *  Visit http://www.timefinder.de for more information.
 *  Copyright (c) 2009 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package robotutils.planning;

import java.util.Iterator;
import java.util.Set;

/**
 * Based on the Kuhn and Munkres algorithm described in the book of John Clark
 * translated into German ("Graphentheorie. Grundlagen und Anwendungen").
 * Another working description you can find in:
 * "Graph Theory with Applications" from J.A. Bondy and U.S.R. Murty.
 * <p/>
 * Elsewhere in the literature KuhnMunkres it is called hungarian *algorithm*.
 * Normally the algorithm calculates a perfect matching for a
 * maximal weighted, bipartite graph.
 * The hungarian *method* only calculates a perfect matching for a 
 * maximal bipartite graph (graph without weights).
 * <p/>
 * Peter slightly modified it to
 * 1. calulate the minimal weighted, bipartite graph (instead maximal).
 * 2. handle n*m matrices and infinity entries
 *
 * It has been modified to follow with the other robotutils data structures and
 * algorithms, and all external dependencies have been refactored or removed.
 *
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 * @author pkv
 */
public class KuhnMunkresAlgorithm {
//
//    private float floatMax;
//    private int NO_X;
//    private int NO_Y;
//    private float[] xLabeling;
//    private float[] yLabeling;
//    private BipartiteWeightedGraph graph;
//    private BipartiteWeightedGraph eqSubGraph;
//    private BipartiteWeightedGraph matching;
//    private Set<Integer> setS;
//    private Set<Integer> setT;
//    private Set<Integer> setN;
//
//    /**
//     * We can use this as placeholder for the matching graph,
//     * which uses BipartiteGraph instead a more lightweight version of this
//     * e.g. BipartiteUnweightedGraph
//     */
//    private final static float CONNECT = 1f;
//
//    /**
//     * Takes in a matrix of non-negative costs where the row indices represent
//     * possible <b>executors</b> of the task and the column indices represent
//     * possible <b>tasks</b>.  The returned matrix contains a 1 for entries
//     * corresponding to an assignment of executors to tasks.
//     *
//     * Each executor will be  assigned exactly one task.
//     *
//     * @param costMatrix a non-negative matrix of costs.
//     * @return an indicator matrix of assignments from executors to tasks.
//     */
//    public static int[][] computeAssignments(float[][] costMatrix) {
//
//        NO_X = costMatrix[0].length; // COLUMNS
//        NO_Y = costMatrix.length;    // ROWS
//
//        assert NO_X > 0 : "Matrix must have at least one column.";
//        assert NO_Y > 0 : "Matrix must have at least one row.";
//
//        // TODO performance: use a static big number instead of determining it?
//        floatMax = AssignmentHelper.getFloatMax(Math.max(costMatrix.length, costMatrix[0].length));
//        graph = new BipartiteWeightedGraph(NO_X, NO_Y);
//
//        for (int x = 0; x < NO_X; x++) {
//            for (int y = 0; y < NO_Y; y++) {
//                if (costMatrix[y][x] < floatMax) {
//                    // this algo calculates maximal weighted bipartite graph
//                    // we need the minimal one
//
//                    //Float.MAX_VALUE does not work because for large numbers it is 'x + 1 = x'
//                    graph.setEdge(x, y, floatMax - costMatrix[y][x]);
//                } else {
//                    graph.setEdge(x, y, 0);
//                }
//            }
//        }
//        if (NO_Y < NO_X) {
//            graph.addYNodes(NO_X - NO_Y);
//            for (int x = 0; x < NO_X; x++) {
//                for (int y = NO_Y; y < NO_X; y++) {
//                    graph.setEdge(x, y, 0);
//                }
//            }
//            NO_Y = NO_X;
//        }
//
//        xLabeling = new float[NO_X];
//        yLabeling = new float[NO_Y];
//
//        // calulate initial labeling
//        for (int x = 0; x < NO_X; x++) {
//            float max = -1;
//            for (int y : graph.getX(x)) {
//                float tmp = graph.getEdge(x, y);
//                if (max < tmp) {
//                    max = tmp;
//                }
//            }
//            assert max > -1;
//            xLabeling[x] = max;
//        }
//
//        // the edges of eqSubGraph depends on the labeling
//        eqSubGraph = new BipartiteWeightedGraph(NO_X, NO_Y);
//        // calculate eqSubGraph from labeling
//        recalculateEqSubGraph();
//
//        // S is a subset of X (columns)
//        setS = new FastSet<Integer>(NO_X);
//        // T is a subset of Y (rows)
//        setT = new FastSet<Integer>(NO_Y);
//
//        //TODO use a lighter graph (without weight) for matching
//        // we can choose any matching M
//        //TODO PERFORMANCE use a fast heuristic to get a better starting matching
//        matching = new BipartiteWeightedGraph(NO_X, NO_Y);
//        setN = new FastSet<Integer>(NO_Y);
//
//        // Main loop
//        while (true) {
//
//            // find the next unassigned x node of matching
//            // if no such node exists then we have either an optimal matching
//            // (all x nodes are M-saturated) or not all x nodes can be valid assigned
//            int u = -1;
//            for (int x = 0; x < NO_X; x++) {
//                if (graph.getX(x).size() > 0) {
//                    if (matching.getX(x).size() == 0) {
//                        u = x;
//                        break;
//                    }
//                    assert matching.getX(x).size() < 2;
//                }
//            }
//
//            // TODO PERFORMANCE introduce a set of unassigned nodes
//            // and test if it is faster, because we have to change this set
//            // in the last augmenting-tree section, too
//
//            // no such unassigned node exists
//            if (u < 0) {
//                break;
//            }
//
//            // Now calculate the augmenting tree via breadth first search
//            setS.clear();
//            setS.add(u);
//            setT.clear();
//            int y;
//
//            while (true) {
//                // calculate the set of neighbors (setN) of setS from all
//                // existent neighbors: eqSubGraph.getX(i)
//                // recalculate neighbors - DUPLICATE CODE see below
//                setN.clear();
//                for (int tmpX : setS) {
//                    setN.addAll(eqSubGraph.getX(tmpX));
//                }
//
//                if (setT.equals(setN)) {
//                    // calculate new slack => new labeling l => new eqSubGraph
//                    // this process forces that !T.equals(N)
//                    recalculateLabeling();
//                    recalculateEqSubGraph();
//
//                    // recalculate neighbors - DUPLICATE CODE see above
//                    setN.clear();
//                    for (int tmpX : setS) {
//                        setN.addAll(eqSubGraph.getX(tmpX));
//                    }
//                }
//
//                // find y from N(S) \ T (N(S) will be evaluated on eqSubGraph!)
//                // T contains already visited y nodes.
//                y = -1;
//                for (int tmpY : setN) {
//                    if (!setT.contains(tmpY)) {
//                        y = tmpY;
//                        break;
//                    }
//                }
//
//                assert y >= 0 : "There has to be an y in N(S)\\T on G_l";
//                assert matching.getY(y).size() < 2;
//
//                if (matching.getY(y).size() == 0) {
//                    // found an unassigned node -> go calculating augmenting path
//                    break;
//                }
//
//                // otherwise extend alternating path and hope for an
//                // unmatched node so that sometimes we get an augmenting path
//                int matchedX = matching.getY(y).iterator().next();
//                setS.add(matchedX);
//                setT.add(y);
//            }
//
//            // Search for the augmenting path via breadth first search.
//            // In the last loop we do nearly the same but the equality graph
//            // can change while the search. This is the reason for this separate
//            // search. Now we construct the path.
//
//            // The following tree contains only the y nodes!
//            // The corresponding x node can simply be retrieved from the matching.
//            SimpleTree augmentatingTree = new SimpleTree();
//            Set<Integer> yNeighbors = eqSubGraph.getX(u);
//            augmentatingTree.setRoots(yNeighbors);
//            int tmpX;
//
//            // TODO do we need setS or setT here?
//            while (!yNeighbors.contains(y)) {
//                for (int tmpY : yNeighbors) {
//                    if (matching.getY(tmpY).size() > 0) {
//                        tmpX = matching.getY(tmpY).iterator().next();
//                        augmentatingTree.addBranches(tmpY, eqSubGraph.getX(tmpX));
//                    }
//                }
//
//                augmentatingTree.higher();
//                yNeighbors = augmentatingTree.getNeighbors();
//            }
//
//            // ... and swap the matching along the this path
//            // => extended matching (one more edge).
//            boolean ret;
//            int currentX, currentY;
//            int lastY = y;
//            Iterator<Integer> augmentingIter = augmentatingTree.getToRootIterator(y);
//
//            while (augmentingIter.hasNext()) {
//                currentY = augmentingIter.next();
//                Set<Integer> matchedX = matching.getY(currentY);
//                assert matchedX.size() == 1;
//                currentX = matchedX.iterator().next();
//
//                // add edge to matching
//                matching.setEdge(currentX, lastY, CONNECT);
//
//                // remove edge from matching
//                ret = matching.removeEdge(currentX, currentY);
//                assert ret : "currentY=" + currentY + " currentX=" + currentX + " M=" + matching;
//
//                //assert matching.getY(currentY).size() == 1 : "Matching: " + matching;
//                //assert matching.getY(lastX).size() == 1 : "Matching: " + matching;
//
//                lastY = currentY;
//            }
//
//            matching.setEdge(u, lastY, CONNECT);
//            assert eqSubGraph.getY(lastY).contains(u);
//        }
//
//        // transform the matching graph into the necessary assignment (matrix)
//        // at least we should calculate one row-assignment for EVERY column
//        int assignment[][] = new int[NO_X][];
//        for (int x = 0; x < NO_X; x++) {
//            for (int y = 0; y < NO_Y; y++) {
//                if (matching.getY(y).contains(x) &&
//                        graph.getEdge(x, y) > 0) {
//                    assignment[x] = new int[]{y, x};
//                    break;
//                }
//            }
//        }
//        return assignment;
//    }
//
//    /**
//     * This method recalculates the equality subgraph from the current labeling.
//     */
//    private void recalculateEqSubGraph() {
//        eqSubGraph.clear();
//        for (int x = 0; x < NO_X; x++) {
//            for (int y : graph.getX(x)) {
//                float tmp = graph.getEdge(x, y);
//                if (xLabeling[x] + yLabeling[y] == tmp) {
//                    eqSubGraph.setEdge(x, y, tmp);
//                }
//            }
//        }
//    }
//
//    private void recalculateLabeling() {
//        // slack = min(l(x) + l(y) - weight(xy))
//        // for all x from setS
//        // but y must not in setT
//        float slackVar = floatMax;
//        float tmp;
//        for (int x : setS) {
//            for (int y : graph.getX(x)) {
//                if (!setT.contains(y)) {
//                    tmp = xLabeling[x] + yLabeling[y] - graph.getEdge(x, y);
//                    if (tmp < slackVar) {
//                        slackVar = tmp;
//                    }
//                }
//            }
//        }
//
//        assert slackVar < Float.MAX_VALUE;
//        // l(x) = l(x) - slack, for all nodes from setS
//        // l(y) = l(y) + slack, for all nodes from setS
//        for (int x : setS) {
//            xLabeling[x] = xLabeling[x] - slackVar;
//            assert xLabeling[x] > -1;
//        }
//
//        for (int y : setT) {
//            yLabeling[y] = yLabeling[y] + slackVar;
//            assert yLabeling[y] > -1;
//        }
//    }
}