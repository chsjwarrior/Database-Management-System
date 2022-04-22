package analyzer;

public class LexicalError extends AnalysisError {
	private static final long serialVersionUID = 1L;
	private String lexeme;

	public LexicalError(String msg, int position, String lexeme) {
		super(msg, position);
		this.lexeme = lexeme;
	}

	public String getLexeme() {
		return lexeme;
	}

	public LexicalError(String msg) {
		super(msg);
	}
}