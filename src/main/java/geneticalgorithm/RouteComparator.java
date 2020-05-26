
package geneticalgorithm;
import java.util.Comparator;

/**
 *
 * @author Илона
 */
public class RouteComparator implements Comparator<Individual> {
    
    @Override
    public int compare(Individual ind1, Individual ind2) {
        if (ind1.getLengthRoute() == ind2.getLengthRoute()) {
            return 0;
        }
        if (ind1.getLengthRoute() > ind2.getLengthRoute()) {
            return 1;
        } else {
            return -1;
        }
    }
}  
