package syntax.brackets;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by avyatkin on 02/04/16.
 */
public class BracketHighlighting {
    @Override
    public String toString() {
        return "{" + "workingBraces=" + workingBraces + ", brokenBraces=" + brokenBraces + '}';
    }

    public BracketHighlighting(List<Integer> workingBraces, List<Integer> brokenBraces) {
        if (workingBraces == null)
            workingBraces = new ArrayList<>(0);
        if (brokenBraces == null)
            brokenBraces = new ArrayList<>(0);

        this.workingBraces = workingBraces;
        this.brokenBraces = brokenBraces;
    }

    public List<Integer> getWorkingBraces() { return workingBraces; }
    public List<Integer> getBrokenBraces() { return brokenBraces; }
    List<Integer> workingBraces;
    List<Integer> brokenBraces;
}
