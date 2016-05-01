package syntax.brackets;

import java.awt.*;
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

    public BracketHighlighting(List<Point> workingBraces, List<Point> brokenBraces) {
        if (workingBraces == null)
            workingBraces = new ArrayList<>(0);
        if (brokenBraces == null)
            brokenBraces = new ArrayList<>(0);

        this.workingBraces = workingBraces;
        this.brokenBraces = brokenBraces;
    }

    public List<Point> getWorkingBraces() { return workingBraces; }
    public List<Point> getBrokenBraces() { return brokenBraces; }
    List<Point> workingBraces;
    List<Point> brokenBraces;

    public static BracketHighlighting emptyHighlighting() {
        return new BracketHighlighting(new ArrayList<>(), new ArrayList<>());
    }
}
