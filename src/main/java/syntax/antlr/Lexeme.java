package syntax.antlr;

/**
 * Created by avyatkin on 04/04/16.
 */
public class Lexeme {
    private int offset;
    private int distanceToNextToken;
    private int revision;
    private String type;

    public String getText() {
        return text;
    }

    private String text;
    private int size;

    public int getOffset() {
        return offset;
    }

    public Lexeme shift(int off) {
        offset += off;
        return this;
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

    public Lexeme(int offset, int distanceToNextToken, int size, String type, String text) {
        this.offset = offset;
        this.distanceToNextToken = distanceToNextToken;
        this.revision = 0;
        this.size = size;
        this.type = type;
        this.text = text;
    }

    @Override
    public String toString() {
        return "{" +
                "offset=" + offset +
                ", distanceToNextToken=" + distanceToNextToken +
                ", revision=" + revision +
                ", type='" + type + '\'' +
                ", text='" + text + '\'' +
                ", size=" + size +
                '}';
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
        if (text != null ? !text.equals(lexeme.text) : lexeme.text != null) return false;
        if (type != null ? !type.equals(lexeme.type) : lexeme.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = offset;
        result = 31 * result + distanceToNextToken;
        result = 31 * result + revision;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + size;
        return result;
    }
}
