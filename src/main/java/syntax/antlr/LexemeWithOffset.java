package syntax.antlr;

/**
 * Created by avyatkin on 27/04/16.
 */
class LexemeWithOffset {
    public Lexeme getLexeme() {
        return lexeme;
    }

    public int getOffset() {
        return offset;
    }

    public LexemeWithOffset shift(int off) {
        offset += off;
        return this;
    }

    public LexemeWithOffset(Lexeme lexeme, int offset) {
        this.lexeme = lexeme;
        this.offset = offset;
    }

    Lexeme lexeme;
    int offset;
}