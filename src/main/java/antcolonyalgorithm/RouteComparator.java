package antcolonyalgorithm;

import java.util.Comparator;

/**
 * @author Илона
 */
public class RouteComparator implements Comparator<Ant> {

    @Override
    public int compare(Ant ant1, Ant ant2) {
        if (ant1.getRouteLength() == ant2.getRouteLength()) {
            return 0;
        }
        if (ant1.getRouteLength() > ant2.getRouteLength()) {
            return 1;
        } else {
            return -1;
        }
    }
}
