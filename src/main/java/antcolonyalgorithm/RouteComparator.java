package antcolonyalgorithm;

import java.util.Comparator;

/**
 *
 * @author Илона
 */
public class RouteComparator implements Comparator<Ant> {

    @Override
    public int compare(Ant ant1, Ant ant2) {
        if (ant1.getRoute() == ant2.getRoute()) {
            return 0;
        }
        if (ant1.getRouteLenght() > ant2.getRouteLenght()) {
            return 1;
        } else {
            return -1;
        }
    }
}
