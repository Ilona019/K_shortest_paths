package main;

import grapheditor.GenerationMatrix;

import java.util.LinkedList;

public class ConvertRouteToString {

    public String routeToString(GenerationMatrix m, LinkedList<Integer> route) {
        StringBuilder str = new StringBuilder();
        for (Integer integer : route) {
            str.append(m.getVertexOfIndex(integer).getNumberVertex()).append("->");
        }
        return str.toString();
    }
}
