package expcalj.cli;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import com.thebinaryfox.expcalj.Expression;
import com.thebinaryfox.expcalj.ExpressionEnvironment;
import com.thebinaryfox.expcalj.ExpressionException;
import com.thebinaryfox.expcalj.IFunction;

/**
 * A user defined function.
 * 
 * @author The Binary Fox
 */
public class UserFunction implements IFunction {

	static private BigDecimal TRUE = new BigDecimal(1);

	private ExpressionEnvironment env = null;

	private String name = null;
	private String condition = null;
	private String expression = null;
	private String returnexpression = null;
	private String[] parameters = null;

	/**
	 * Create a new user function.
	 * 
	 * @param name
	 *            the name of the function.
	 * @param parameters
	 *            the parameters of the function.
	 * @param expression
	 *            the math expression the function.
	 */
	public UserFunction(String name, String[] parameters, String expression) {
		this.expression = expression;
		this.parameters = parameters;
		this.name = name;
	}

	/**
	 * Create a new user function.
	 * 
	 * @param name
	 *            the name of the function.
	 * @param parameters
	 *            the parameters of the function.
	 * @param expression
	 *            the math expression the function.
	 * @param condition
	 *            the condition expression the function.
	 * @param returnexpression
	 *            the expression to determine the return value if the condition
	 *            is not met
	 */
	public UserFunction(String name, String[] parameters, String expression, String condition, String returnexpression) {
		this.returnexpression = returnexpression;
		this.condition = condition;
		this.expression = expression;
		this.parameters = parameters;
		this.name = name;
	}

	/**
	 * Set the expression environment of this function.
	 * 
	 * @param env
	 *            the environment.
	 */
	public void setEnvironment(ExpressionEnvironment env) {
		this.env = env;
	}

	/**
	 * Get the expression environment of this function.
	 * 
	 * @return the environment.
	 */
	public ExpressionEnvironment getEnvironment() {
		return env;
	}

	/**
	 * Get the name of the function.
	 * 
	 * @return the name of the function.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the conditional expression.
	 * 
	 * @return the conditional expression.
	 */
	public String getConditional() {
		return condition;
	}

	/**
	 * Get the mathematical expression used in this function.
	 * 
	 * @return the expression.
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * Get the mathematical expression used to calculate the return value if the
	 * condition is met.
	 * 
	 * @return the return expression.
	 */
	public String getReturnExpression() {
		return returnexpression;
	}

	/**
	 * Get the parameters this expression takes.
	 * 
	 * @return the parameters.
	 */
	public String[] getParameters() {
		return getParametersSrc().clone();
	}

	protected String[] getParametersSrc() {
		return parameters;
	}

	@Override
	public BigDecimal run(List<BigDecimal> params) {
		if (params.size() != parameters.length)
			throw new ExpressionException(getName() + ": requires exactly " + parameters.length + " parameters!");

		// Get environment.
		ExpressionEnvironment env = getWorkingEnvironment(params);

		// Condition?
		String condition = getConditional();
		if (condition != null) {
			Expression exp = new Expression(condition, env);

			if (exp.calculate().compareTo(TRUE) != 0) {
				exp = new Expression(getExpression(), env);
				return exp.calculate();
			}

			return new Expression(getReturnExpression(), env).calculate();
		}

		return new Expression(getExpression(), env).calculate();
	}

	private ExpressionEnvironment getWorkingEnvironment(List<BigDecimal> params) {
		ExpressionEnvironment env = getEnvironment();
		if (env == null)
			env = ExpressionEnvironment.getDefault();

		env = env.copy();

		Iterator<BigDecimal> parami = params.iterator();
		String[] paramsr = getParametersSrc();
		for (int i = 0; i < paramsr.length; i++) {
			env.setVariable(paramsr[i], parami.next());
		}

		return env;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder(name);
		str.append("(");

		String[] params = getParametersSrc();
		for (int i = 0; i < params.length; i++) {
			if (i != 0)
				str.append(", ");

			str.append(params[i]);
		}

		str.append(")");
		return str.toString();
	}

}
