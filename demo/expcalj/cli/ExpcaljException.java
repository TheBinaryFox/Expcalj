package expcalj.cli;

/**
 * An exception thrown when an error occurred in the calculator CLI.
 * 
 * @author The Binary Fox
 */
public class ExpcaljException extends RuntimeException {

	private static final long serialVersionUID = 4918780084433014262L;

	public ExpcaljException(String str) {
		super(str);
	}

	public ExpcaljException(String str, Throwable t) {
		super(str, t);
	}

}
