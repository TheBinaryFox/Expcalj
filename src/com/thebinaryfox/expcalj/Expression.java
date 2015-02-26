package com.thebinaryfox.expcalj;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * An object representing a mathematical expression.
 * 
 * @author The Binary Fox
 */
public class Expression {

	/**
	 * A container for the result of the seek methods.
	 * 
	 * @author The Binary Fox
	 */
	static protected class ParseSeek {
		String string;
		int end;
	}

	/**
	 * A simple operation that just returns a value.
	 * 
	 * @author The Binary Fox
	 */
	static protected class OpReturn implements IOperation {

		static private OpReturn instance = new OpReturn();

		static public OpReturn instance() {
			return instance;
		}

		@Override
		public BigDecimal calculate(BigDecimal left, BigDecimal right, ExpressionEnvironment env) {
			return right == null ? left : right;
		}

		@Override
		public String toString() {
			return "=";
		}

	}

	private String expression;
	private ExpressionEnvironment environment;
	private BigDecimal eval_value;

	/**
	 * Create a new expression object.
	 * 
	 * @param expression
	 *            the expression.
	 */
	public Expression(String expression) {
		this.expression = expression;
	}

	/**
	 * Create a new expression object with an environment.
	 * 
	 * @param expression
	 *            the expression.
	 * @param environment
	 *            the expression environment.
	 */
	public Expression(String expression, ExpressionEnvironment environment) {
		this.expression = expression;
		this.environment = environment;
	}

	/**
	 * Set the expression environment.
	 * 
	 * @param environment
	 *            the environment.
	 */
	public void setEnvironment(ExpressionEnvironment environment) {
		this.environment = environment;
	}

	/**
	 * Get the expression environment.
	 * 
	 * @return the environment.
	 */
	public ExpressionEnvironment getEnvironment() {
		if (environment == null)
			environment = ExpressionEnvironment.getDefault();

		return environment;
	}

	/**
	 * Get the expression as a string.
	 * 
	 * @return the expression.
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * Get the value of the expression, if calculated.
	 * 
	 * @return the value, or null if there is no successfully calculated
	 *         expression.
	 */
	public BigDecimal getValue() {
		return eval_value;
	}

	/**
	 * Evaluate the expression, and return the calculated value.
	 * 
	 * @param env
	 *            the calculator environment.
	 * 
	 * @return the result of the calculations.
	 * @throws ExpressionException
	 */
	public BigDecimal calculate() throws ExpressionException {
		evaluate();
		return getValue();
	}

	/**
	 * Evaluate the expression.
	 * 
	 * @param env
	 *            the calculator environment.
	 * 
	 * @throws ExpressionException
	 */
	public void evaluate() throws ExpressionException {
		ExpressionStep first = null;

		// Split into steps.
		first = parseExpression();

		// Evaluate steps and set the eval_value.
		eval_value = evaluateSteps(first, getEnvironment());
	}

	/**
	 * Evaluate the step chain by order.
	 * 
	 * @param first
	 *            the expected first step in the chain.
	 * @param env
	 *            the expression environment.
	 * 
	 * @return the evaluated chain.
	 */
	protected BigDecimal evaluateSteps(ExpressionStep first, ExpressionEnvironment env) {
		int order = first.getOrder();

		// Determine highest order.
		ExpressionStep here = first.next();
		if (here != null) {
			while (here.hasNext()) {
				int o = here.getOrder();
				if (o > order) {
					order = o;
				}

				here = here.next();
			}

			int mo = here.getOrder();
			if (mo > order) {
				order = mo;
			}
		}

		// Start evaluating with descending orders.
		BigDecimal result = null;
		while (order >= 0) {
			here = first;
			int o;

			while (true) {
				o = here.getOrder();
				if (o == order) {
					result = evaluateStep(here, env);
				}

				if (!here.hasNext())
					break;

				here = here.next();
			}

			order--;
		}

		// Finish
		return result;
	}

	/**
	 * Evaluate a single expression and modify the chain accordingly.
	 * 
	 * @param step
	 *            the step to evaluate.
	 * @param env
	 *            the expression environment.
	 */
	protected BigDecimal evaluateStep(ExpressionStep step, ExpressionEnvironment env) {
		BigDecimal val = step.getOperation().calculate(step.getLeft(), step.getRight(), env);

		boolean changes = false;
		if (step.hasPrevious()) {
			ExpressionStep previous = step.previous();
			previous.updateRight(val);
			previous.setNext(step.next());
			changes = true;
		}

		if (step.hasNext()) {
			ExpressionStep next = step.next();
			next.updateLeft(val);
			next.setPrevious(step.previous());
			changes = true;
		}

		if (!changes) {
			step.updateRight(val);
		}

		return val;
	}

	/**
	 * Parse an expression string and create an ExpressionStep chain.
	 * 
	 * @return the step chain.
	 */
	protected ExpressionStep parseExpression() {
		ExpressionEnvironment env = getEnvironment();
		ExpressionStep chain = null;
		IOperation oper = null;
		BigDecimal right = null;
		BigDecimal left = null;

		// Parse
		ExpressionStep last = null;
		char[] chars = getExpression().toCharArray();

		// Find initial left value.
		ParseSeek seek = seekValue(chars, 0);
		left = parseValue(seek.string);

		// Loop for operator and right value.
		while (seek.end < chars.length) {
			// Operator
			seek = seekOperator(chars, seek.end);
			if (seek.end >= chars.length)
				throw new ExpressionException("Missing right-hand side of operator.");

			oper = env.getOperation(seek.string);
			if (oper == null)
				throw new ExpressionException("Unknown operator \"" + seek.string + "\".");

			// Value
			seek = seekValue(chars, seek.end);
			right = parseValue(seek.string);

			// Math!
			if (chain == null)
				chain = last = new ExpressionStep(left, oper, right);
			else
				last = last.add(new ExpressionStep(left, oper, right));

			left = right;
			right = null;
		}

		// No expression, just value.
		if (chain == null) {
			return new ExpressionStep(left, OpReturn.instance, left);
		}

		// Return
		return chain;
	}

	/**
	 * Seek for a value.
	 * 
	 * @param arr
	 *            the char array.
	 * @param start
	 *            the start position of the seek.
	 * @return the seeked value.
	 */
	protected ParseSeek seekValue(char[] arr, int start) {
		ParseSeek seek = new ParseSeek();
		StringBuilder sb = new StringBuilder();

		boolean ignore_whitespace = true;
		int depth = 0;
		int i = start;
		for (; i < arr.length; i++) {
			char c = arr[i];

			// Bracket
			if (c == '(') {
				sb.append(c);
				depth++;
				continue;
			}

			if (c == ')') {
				sb.append(c);
				depth--;
				continue;
			}

			if (depth > 0) {
				sb.append(c);
				continue;
			}

			// Non-bracket.
			if (ignore_whitespace && Character.isWhitespace(c)) {
				continue;
			}

			ignore_whitespace = false;

			// Check if negative.
			if (c == '-') {
				if (sb.length() == 0) {
					sb.append(c);
					continue;
				} else {
					// TODO
				}
			}

			// Check if valid value symbol.
			if (isValueSymbol(c)) {
				sb.append(c);
			} else {
				break;
			}
		}

		if (depth > 0)
			throw new ExpressionException("Unmatched '(' bracket.");

		if (depth < 0)
			throw new ExpressionException("Unmatched ')' bracket.");

		seek.string = sb.toString();
		seek.end = i;
		return seek;
	}

	/**
	 * Seek for an operator.
	 * 
	 * @param arr
	 *            the char array.
	 * @param start
	 *            the start position of the seek.
	 * @return the seeked operator.
	 */
	protected ParseSeek seekOperator(char[] arr, int start) {
		ParseSeek seek = new ParseSeek();
		StringBuilder sb = new StringBuilder();

		boolean ignore_whitespace = true;
		boolean ignoring_end_ws = false;
		int i = start;
		for (; i < arr.length; i++) {
			char c = arr[i];
			if (ignore_whitespace && Character.isWhitespace(c)) {
				continue;
			}

			ignore_whitespace = false;

			// Check if valid value symbol.
			if (isValueSymbol(c)) {
				break;
			} else {
				if (Character.isWhitespace(c)) {
					ignoring_end_ws = true;
					continue;
				}

				if (ignoring_end_ws) {
					if (c == '-')
						break;

					throw new ExpressionException("Invalid whitespace inside operator.");
				}

				sb.append(c);
			}
		}

		seek.string = sb.toString();
		seek.end = i;
		return seek;
	}

	/**
	 * Determine if a value is an integer.
	 * 
	 * @param value
	 *            the value.
	 * @return true if the value is an integer or other.
	 */
	protected boolean isInteger(String value) {
		char[] chars = value.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];

			if (!((c >= '0' && c <= '9') || c == '.'))
				return false;
		}

		return true;
	}

	/**
	 * Determine if a value is a bracketed expression.
	 * 
	 * @param value
	 *            the value.
	 * @return true if the value is a bracketed expression.
	 */
	protected boolean isBrackets(String value) {
		return value.startsWith("(") && value.endsWith(")");
	}

	/**
	 * Determine if a value is a function.
	 * 
	 * @param value
	 *            the value.
	 * @return true if the value is a function.
	 */
	protected boolean isFunction(String value) {
		if (value.endsWith(")")) {
			return value.indexOf('(') > 0;
		}

		return false;
	}

	/**
	 * Parse a bracket, function, variable, or integer value.
	 * 
	 * @param value
	 *            the value.
	 * @return the parsed result.
	 */
	protected BigDecimal parseValue(String value) {
		// Negative?
		if (value.startsWith("-")) {
			return parseValue(value.substring(1)).negate();
		}

		// Expression
		if (isBrackets(value)) {
			return parseBrackets(value);
		}

		// Function
		if (isFunction(value)) {
			return parseFunction(value);
		}

		// Integer
		if (isInteger(value)) {
			return parseInteger(value);
		}

		// Variable
		return parseVariable(value);
	}

	/**
	 * Parse a variable.
	 * 
	 * @param variable
	 *            the variable name.
	 * @return the value of the variable.
	 */
	protected BigDecimal parseVariable(String variable) {
		// Is option multiply variable?
		ExpressionEnvironment env = getEnvironment();
		if (env.isVariableMultiplyEnabled()) {
			char fc = variable.charAt(0);
			if ((fc >= '0' && fc <= '9') || fc == '-' || fc == '.') {
				char[] fn = variable.toCharArray();
				int i = 0;
				for (; i < fn.length; i++) {
					char c = fn[i];
					if ((c < '0' || c > '9') && c != '-' && c != '.') {
						break;
					}
				}

				BigDecimal by = new BigDecimal(variable.substring(0, i));
				BigDecimal mul = parseVariable(variable.substring(i));

				return mul.multiply(by, env.getMathContext());
			}
		}

		// Just variable.
		if (variable.charAt(0) == '-') {
			variable = variable.substring(1);
			IVariable var = env.getVariable(variable);
			if (var == null)
				throw new ExpressionException("Undefined variable \"" + variable + "\".");

			return var.value().negate();
		} else {
			IVariable var = env.getVariable(variable);
			if (var == null)
				throw new ExpressionException("Undefined variable \"" + variable + "\".");

			return var.value();
		}
	}

	/**
	 * Parse a bracketed expression.
	 * 
	 * @param value
	 *            the expression.
	 * @return the result.
	 */
	protected BigDecimal parseBrackets(String value) {
		return new Expression(value.substring(1, value.length() - 1), getEnvironment()).calculate();
	}

	/**
	 * Parse a function.
	 * 
	 * @param value
	 *            the entire function string, including parameters.
	 * @return the parsed function.
	 */
	protected BigDecimal parseFunction(String value) {
		int lbindex = value.indexOf('(');
		if (lbindex == -1)
			throw new ExpressionException("Not a function!");

		String functionname = value.substring(0, lbindex);
		String paramstr = value.substring(lbindex + 1, value.length() - 1);

		ExpressionEnvironment env = getEnvironment();

		// Is it just multiplying brackets?
		if (getEnvironment().isBracketMultiplyEnabled()) {
			char[] fn = functionname.toCharArray();
			boolean dfn = true;
			for (int i = 0; i < fn.length; i++) {
				char fc = fn[i];
				if ((fc < '0' || fc > '9') && fc != '-' && fc != '.') {
					dfn = false;
					break;
				}
			}

			if (dfn) {
				BigDecimal mul = new BigDecimal(functionname);
				BigDecimal by = new Expression(paramstr, env).calculate();
				return mul.multiply(by, env.getMathContext());
			}
		}

		// Get function.
		IFunction function = env.getFunction(functionname);
		if (function == null)
			throw new ExpressionException("Undeclared function \"" + functionname + "\".");

		// Get parameters.
		LinkedList<BigDecimal> params = new LinkedList<BigDecimal>();
		parseParameters(paramstr, params);

		// Do function.
		try {
			return function.run(params, env);
		} catch (ExpressionException ex) {
			throw new ExpressionException(functionname + ": " + ex.getMessage(), ex);
		} catch (Exception ex) {
			throw new ExpressionException(ex.getClass().getSimpleName() + ": " + ex.getMessage(), ex);
		}
	}

	/**
	 * Parse a decimal integer.
	 * 
	 * @param integer
	 *            the integer to parse.
	 * @return the parsed BigDecimal.
	 */
	protected BigDecimal parseInteger(String integer) {
		try {
			return new BigDecimal(integer);
		} catch (NumberFormatException ex) {
			throw new ExpressionException("Invalid integer value provided.", ex);
		}
	}

	/**
	 * Parse function parameters.
	 * 
	 * @param paramstr
	 *            the parameter string.
	 * @param params
	 *            the list to add the parameter values to.
	 */
	protected void parseParameters(String paramstr, LinkedList<BigDecimal> params) {
		ExpressionEnvironment env = getEnvironment();

		char[] chrs = paramstr.toCharArray();
		if (chrs.length == 0)
			return;

		if (chrs[chrs.length - 1] == ',') {
			throw new ExpressionException("Trailing \",\" in function parameters.");
		}

		int last = 0;
		int i = 0;

		int depth = 0;
		for (; i < chrs.length; i++) {
			char c = chrs[i];

			// Bracket nesting.
			if (c == '(') {
				depth++;
				continue;
			}

			if (c == ')') {
				depth--;
				continue;
			}

			if (depth > 0)
				continue;

			// Next parameter.
			if (c == ',') {
				params.add(new Expression(paramstr.substring(last, i), env).calculate());
				last = i + 1;
			}
		}

		if (last < chrs.length) {
			params.add(new Expression(paramstr.substring(last), env).calculate());
		}
	}

	/**
	 * Check to see if the character is a valid character for values.
	 * 
	 * @param c
	 *            the character to check.
	 * @return true if the character is a valid character for values.
	 */
	protected boolean isValueSymbol(char c) {
		return (c >= '0' && c <= '9') || c == '.' || c == '_' || c == '(' || c == ')' || Character.isAlphabetic(c);
	}

}
