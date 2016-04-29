package syntax.antlr;

/**
 * Created by avyatkin on 04/04/16.
 */
public class Lexeme {
    private String type;

    public String getText() {
        return text;
    }

    private String text;

    public int getSize() {
        return text.length();
    }

    public String getType() {
        return type;
    }


    public Lexeme(String type, String text) {
        this.type = type;
        this.text = text;
    }

    @Override
    public String toString() {
        return "{" +
                "type='" + type + '\'' +
                ", text='" + text + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lexeme lexeme = (Lexeme) o;

        if (text != null ? !text.equals(lexeme.text) : lexeme.text != null) return false;
        if (type != null ? !type.equals(lexeme.type) : lexeme.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (type != null ? type.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }
}
