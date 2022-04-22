package analyzer;

public class SyntaticError extends AnalysisError {
	private static final long serialVersionUID = 1L;
	private String lexeme, classe;

	public SyntaticError(String msg, int position, String lexeme, String classe) {
		super(msg, position);
		this.lexeme = lexeme;
		this.classe = classe;
	}

	public String getLexeme() {
		return lexeme;
	}

	public String getClasse() {
		return classe;
	}

	public SyntaticError(String msg) {
		super(msg);
	}
}