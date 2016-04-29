package syntax;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by avyatkin on 28/04/16.
 */
public class EditorUtil {
    public static <T, U> List<U> map(List<T> input, Function<T,U> mapper) {
        return input.stream().map(mapper).collect(Collectors.toList());
    }

    public static <T> String join(List<T> input, Function<T, String> mapper, String infix) {
        return String.join(infix, map(input, mapper));
    }
}
