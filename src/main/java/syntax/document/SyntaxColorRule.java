package syntax.document;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by avyatkin on 02/04/16.
 */
public class SyntaxColorRule {
    public SyntaxColorRule(Color color, String ... applicableTypes) {
        this.color = color;
        this.applicableTypes = new HashSet<>(Arrays.asList(applicableTypes));
    }

    Color color;
    HashSet<String> applicableTypes;

    public Optional<Color> getColor(String tokenType) {
        if (applicableTypes.contains(tokenType))
            return Optional.of(color);
        else
            return Optional.empty();
    }
}
