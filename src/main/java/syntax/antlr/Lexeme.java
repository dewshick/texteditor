package syntax.antlr;

import java.util.Optional;

/**
 * Created by avyatkin on 04/04/16.
 */
public class Lexeme {
    private int offset;
    private String type;
    private String text;

    public String getText() {
        return text;
    }

    public int getOffset() {
        return offset;
    }

    public Lexeme shift(int off) {
        offset += off;
        return this;
    }

    public int getSize() {
        return text.length();
    }

    public String getType() {
        return type;
    }

    public Lexeme(int offset, String type, String text) {
        this.offset = offset;
        this.type = type;
        this.text = text;
    }

    @Override
    public String toString() {
        return "{" +
                "offset=" + offset +
                ", type='" + type + '\'' +
                ", text='" + text + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lexeme lexeme = (Lexeme) o;

        if (offset != lexeme.offset) return false;
        if (text != null ? !text.equals(lexeme.text) : lexeme.text != null) return false;
        if (type != null ? !type.equals(lexeme.type) : lexeme.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = offset;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }
}
