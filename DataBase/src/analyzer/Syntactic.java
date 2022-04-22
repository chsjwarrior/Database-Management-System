package analyzer;

import java.io.IOException;
import java.util.Stack;

public class Syntactic implements Constants {
	private Stack<Integer> stack = new Stack<Integer>();
	private Token currentToken;
	private Token previousToken;
	private Lexicon scanner;
	private Semantic semanticAnalyser;

	private static final boolean isTerminal(int x) {
		return x < FIRST_NON_TERMINAL;
	}

	private static final boolean isNonTerminal(int x) {
		return x >= FIRST_NON_TERMINAL && x < FIRST_SEMANTIC_ACTION;
	}

	private boolean step() throws LexicalError, SyntaticError, SemanticError, IOException {
		if (currentToken == null) {
			int pos = 0;
			if (previousToken != null)
				pos = previousToken.getPosition() + previousToken.getLexeme().length();

			currentToken = new Token(DOLLAR, "$", pos);
		}

		int x = ((Integer) stack.pop()).intValue();
		int a = currentToken.getId();

		if (x == EPSILON) {
			return false;
		} else if (isTerminal(x)) {
			if (x == a) {
				if (stack.empty())
					return true;
				else {
					previousToken = currentToken;
					currentToken = scanner.nextToken();
					return false;
				}
			} else {
				throw new SyntaticError(PARSER_ERROR[x], currentToken.getPosition(), currentToken.getLexeme(),
						currentToken.getClasse());
			}
		} else if (isNonTerminal(x)) {
			if (pushProduction(x, a))
				return false;
			else
				throw new SyntaticError(PARSER_ERROR[x], currentToken.getPosition(), currentToken.getLexeme(),
						currentToken.getClasse());
		} else // isSemanticAction(x)
		{
			semanticAnalyser.executeAction(x - FIRST_SEMANTIC_ACTION, previousToken);
			return false;
		}
	}

	private boolean pushProduction(int topStack, int tokenInput) {
		int p = PARSER_TABLE[topStack - FIRST_NON_TERMINAL][tokenInput - 1];
		if (p >= 0) {
			int[] production = PRODUCTIONS[p];
			// empilha a produção em ordem reversa
			for (int i = production.length - 1; i >= 0; --i) 
				stack.push((int) production[i]);
			
			return true;
		} else
			return false;
	}

	public void parse(Lexicon scanner, Semantic semanticAnalyser)
			throws LexicalError, SyntaticError, SemanticError, IOException {
		this.scanner = scanner;
		this.semanticAnalyser = semanticAnalyser;

		stack.clear();
		stack.push((int) DOLLAR);
		stack.push((int) START_SYMBOL);

		currentToken = scanner.nextToken();

		while (!step())
			;
	}
}