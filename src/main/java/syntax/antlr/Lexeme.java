package syntax.antlr;

/**
 * Created by avyatkin on 04/04/16.
 */
public class Lexeme {
    int offset;
    int distanceToNextToken;
    int revision;
    String type;
    int size;

    public int getOffset() {
        return offset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lexeme lexeme = (Lexeme) o;

        if (distanceToNextToken != lexeme.distanceToNextToken) return false;
        if (offset != lexeme.offset) return false;
        if (revision != lexeme.revision) return false;
        if (size != lexeme.size) return false;
        if (!type.equals(lexeme.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = offset;
        result = 31 * result + distanceToNextToken;
        result = 31 * result + revision;
        result = 31 * result + type.hashCode();
        result = 31 * result + size;
        return result;
    }

    public int getDistanceToNextToken() {
        return distanceToNextToken;
    }

    public int getStopIndex() {
        return offset + size - 1;
    }

    public int getRevision() {
        return revision;
    }

    public int getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public Lexeme(int offset, int distanceToNextToken, int size, String type) {
        this.offset = offset;
        this.distanceToNextToken = distanceToNextToken;
        this.revision = 0;
        this.size = size;
        this.type = type;
    }

    @Override
    public String toString() {
        return "{" +
                "offset=" + offset +
                ", distanceToNextToken=" + distanceToNextToken +
                ", revision=" + revision +
                ", type='" + type + '\'' +
                ", size=" + size +
                '}';
    }
}
