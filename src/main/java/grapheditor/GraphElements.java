package grapheditor;

/**
 * @author Илона
 */

import org.apache.commons.collections15.Factory;

public class GraphElements {

    /**
     * Creates a new instance of GraphElements
     */
    public GraphElements() {
    }

    public static class MyVertex {

        private String name;

        public MyVertex(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        //Извлечь  вершины
        public String getNumberVertex() {
            char[] dst = new char[name.length()];
            name.getChars(0, name.length(), dst, 0);
            StringBuilder str = new StringBuilder();
            for (Character c : dst) {
                str.append(c.toString());
            }
            return str.toString();
        }

        public String toString() {
            return name;
        }
    }

    public static class MyEdge {

        private int weight;
        private String name;
        private int flagP;//Проходили раньше по ребру? 0 - нет, 1 - да.

        public MyEdge(String name) {
            this.name = name;
            flagP = 0;
        }

        public MyEdge(String name, int w) {
            this.name = name;
            this.weight = w;
            flagP = 0;
        }

        public int getWeight() {
            return weight;
        }

        public int getFlagPaint() {
            return flagP;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setFlagPaint(int f) {
            flagP = f;
        }

        public String toString() {
            return name + "   Weight = " + weight;
        }

    }

    // Single factory for creating Vertices...
    public static class MyVertexFactory implements Factory<MyVertex> {

        private static int nodeCount = 0;
        private static MyVertexFactory instance = new MyVertexFactory();

        public MyVertexFactory() {
        }

        public static MyVertexFactory getInstance() {
            return instance;
        }

        public GraphElements.MyVertex create() {
            String name = "" + nodeCount++;

            return new MyVertex(name);
        }

        public static void setNullNodeCount() {
            nodeCount = 0;
        }
    }

    // Singleton factory for creating Edges...
    public static class MyEdgeFactory implements Factory<MyEdge> {

        private static int linkCount = 0;
        private static int defaultWeight = 1;

        private static MyEdgeFactory instance = new MyEdgeFactory();

        public MyEdgeFactory() {
        }

        public static MyEdgeFactory getInstance() {
            return instance;
        }

        public GraphElements.MyEdge create() {
            String name = "Link" + linkCount++;
            MyEdge link = new MyEdge(name);
            link.setWeight(defaultWeight);
            return link;
        }

        public static double getDefaultWeight() {
            return defaultWeight;
        }

        public void setDefaultWeight(int aDefaultWeight) {
            defaultWeight = aDefaultWeight;
        }

        public static void setNullLinkCount() {
            linkCount = 0;
        }

    }

}
