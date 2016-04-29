package iterators;

import gui.state.ColoredString;
import org.apache.commons.collections4.list.TreeList;
import org.junit.Before;
import org.junit.Test;
import syntax.EditorUtil;
import syntax.antlr.iterators.ColoredStringNavigator;
import static org.junit.Assert.assertEquals;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static syntax.EditorUtil.map;

/**
 * Created by avyatkin on 28/04/16.
 */
public class ColoredStringNavigatorTest {
    TreeList<List<ColoredString>> linesList = new TreeList<>(
            Arrays.asList(
                coloredStrings("first", "line"),
                coloredStrings("second", "line"),
                coloredStrings("third", "line")));

    ColoredStringNavigator navigator;

    @Before
    public void init() {
        navigator = new ColoredStringNavigator(linesList);
    }

    private List<ColoredString> coloredStrings(String ... strings) {
        return map(Arrays.asList(strings), str -> new ColoredString(str, Color.black, 0, "nothing"));
    }

    @Test
    public void shouldNavigateForwardCorrectly() {
        List<String> resultList = new ArrayList<>();
        while (navigator.hasNext()) resultList.add(navigator.next().getContent());
        assertEquals("[first, line, second, line, third, line]", resultList.toString());
    }

    @Test
    public void shouldNavigateBackwardCorrectly() {
        List<String> resultList = new ArrayList<>();
        while (navigator.hasNext()) navigator.next();
        while (navigator.hasPrevious()) resultList.add(navigator.previous().getContent());

        assertEquals("[line, third, line, second, line, first]", resultList.toString());
    }

    @Test
    public void shouldWorkAfterMultipleTraversals() {
        List<String> resultList = new ArrayList<>();
        while (navigator.hasNext()) navigator.next();
        while (navigator.hasPrevious()) navigator.previous();
        while (navigator.hasNext()) resultList.add(navigator.next().getContent());
        assertEquals("[first, line, second, line, third, line]", resultList.toString());
    }

    @Test
    public void navigateCorrectlyForEdge() {
        navigator.beforePoint(new Point("third line".length(),2));
        assertEquals(navigator.next().getContent(), "line");
    }

    @Test
    public void navigateCorrectlyForBeginning() {
        navigator.beforePoint(new Point(0,0));
        assertEquals(navigator.next().getContent(), "first");
    }

    @Test
    public void navigateCorrectlyForMiddle() {
        navigator.beforePoint(new Point("second".length()-1, 1));
        assertEquals(navigator.next().getContent(), "second");
        navigator.beforePoint(new Point(0, 1));
        assertEquals(navigator.next().getContent(), "second");
        navigator.beforePoint(new Point("second".length(), 1));
        assertEquals(navigator.next().getContent(), "line");
        navigator.beforePoint(new Point("second line".length(), 1));
        assertEquals(navigator.next().getContent(), "line");
    }

    @Test
    public void shouldNavigateCorrectlyInSingleton() {
        navigator = new ColoredStringNavigator(new TreeList<>(Arrays.asList(coloredStrings(""))));
        navigator.beforePoint(new Point(0,0));
        assertEquals(navigator.next().getContent(), "");
    }

    @Test
    public void shouldNavigateCorrectlyInEmpty() {
        navigator = new ColoredStringNavigator(new TreeList<>(Arrays.asList()));
        assertEquals(navigator.hasNext(), false);
    }
}
