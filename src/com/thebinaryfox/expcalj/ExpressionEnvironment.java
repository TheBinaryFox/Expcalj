package com.thebinaryfox.expcalj;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import com.thebinaryfox.expcalj.operations.*;
import com.thebinaryfox.expcalj.functions.*;
import com.thebinaryfox.expcalj.variables.*;

/**
 * A class representing an object that is used as the environment for the
 * Expcalj expression calculator.
 * 
 * @author The Binary Fox
 */
public class ExpressionEnvironment {

	// Default environment.
	static private ExpressionEnvironment default_environment;

	/**
	 * Get the default expression environment. This is used in Expression
	 * objects that do not have an environment passed to them.
	 * 
	 * @return the default expression environment.
	 */
	static public ExpressionEnvironment getDefault() {
		if (default_environment == null) {
			default_environment = new ExpressionEnvironment();
			default_environment.useDefault();
		}

		return default_environment;
	}

	// Defaults
	static private HashMap<String, IOperation> default_operations;
	static private HashMap<String, IVariable> default_variables;
	static private HashMap<String, IFunction> default_functions;
	static private MathContext default_context = MathContext.DECIMAL128;
	static {
		default_operations = new HashMap<String, IOperation>();
		default_variables = new HashMap<String, IVariable>();
		default_functions = new HashMap<String, IFunction>();

		setDefaultOperation("+", new OpAddition());
		setDefaultOperation("-", new OpSubtraction());
		setDefaultOperation("*", new OpMultiplication());
		setDefaultOperation("/", new OpDivision());
		setDefaultOperation("%", new OpRemainder());

		setDefaultFunction("abs", new FuncAbsolute());
		setDefaultFunction("neg", new FuncNegative());
		setDefaultFunction("floor", new FuncFloor());
		setDefaultFunction("ceil", new FuncCeil());
		setDefaultFunction("round", new FuncRound());

		setDefaultVariable("pi", new VarPi());
	}

	/**
	 * Set a default operation.
	 * 
	 * @param operator
	 *            the operator.
	 * @param operation
	 *            the operation.
	 */
	static public void setDefaultOperation(String operator, IOperation operation) {
		if (operation == null)
			default_operations.remove(operator);
		else
			default_operations.put(operator, operation);
	}

	/**
	 * Set a default variable.
	 * 
	 * @param name
	 *            the operator.
	 * @param variable
	 *            the operation.
	 */
	static public void setDefaultVariable(String name, IVariable variable) {
		if (variable == null)
			default_variables.remove(name);
		else
			default_variables.put(name, variable);
	}

	/**
	 * Set a default function.
	 * 
	 * @param name
	 *            the name of the function.
	 * @param function
	 *            the function.
	 */
	static public void setDefaultFunction(String name, IFunction function) {
		if (name == null)
			default_functions.remove(name);
		else
			default_functions.put(name, function);
	}

	/**
	 * Set the default math context.
	 * 
	 * @param context
	 *            the math context.
	 */
	static public void setDefaultMathContext(MathContext context) {
		if (context == null)
			context = MathContext.DECIMAL128;

		default_context = context;
	}

	// Calculator
	private HashMap<String, IOperation> operations;
	private HashMap<String, IVariable> variables;
	private HashMap<String, IFunction> functions;
	private MathContext context;

	private boolean opt_mulv = false;
	private boolean opt_mulb = false;

	/**
	 * Create a new calculator environment.
	 */
	public ExpressionEnvironment() {
		operations = new HashMap<String, IOperation>();
		variables = new HashMap<String, IVariable>();
		functions = new HashMap<String, IFunction>();
		context = MathContext.UNLIMITED;
	}

	/**
	 * Set the default operators, variables, and functions.
	 */
	public void useDefault() {
		operations.putAll(default_operations);
		variables.putAll(default_variables);
		functions.putAll(default_functions);
		context = default_context;
	}

	/**
	 * Check if bracket multiplication is enabled. Example: 3(5+2)
	 * 
	 * @return true if it is enabled.
	 */
	public boolean isBracketMultiplyEnabled() {
		return opt_mulb;
	}

	/**
	 * Check if variable multiplication is enabled. Example: 2f
	 * 
	 * @return true if it is enabled.
	 */
	public boolean isVariableMultiplyEnabled() {
		return opt_mulv;
	}

	/**
	 * Set if bracket multiplication is enabled.
	 * 
	 * @param enabled
	 *            whether it is enabled or not.
	 */
	public void setBracketMultiplyEnabled(boolean enabled) {
		opt_mulb = enabled;
	}

	/**
	 * Set if variable multiplication is enabled.
	 * 
	 * @param enabled
	 *            whether it is enabled or not.
	 */
	public void setVariableMultiplyEnabled(boolean enabled) {
		opt_mulv = enabled;
	}

	/**
	 * Check to see if the calculator environment has a variable.
	 * 
	 * @param name
	 *            the name of the variable.
	 * @return true if the variable is defined.
	 */
	public boolean hasVariable(String name) {
		return variables.containsKey(name);
	}

	/**
	 * Get the value of a variable in the calculator environment.
	 * 
	 * @param name
	 *            the name of the variable.
	 * @return the variable.
	 */
	public IVariable getVariable(String name) {
		return variables.get(name);
	}

	/**
	 * Get a set containing all the variable names.
	 * 
	 * @return the variable names.
	 */
	public Set<String> getVariables() {
		return Collections.unmodifiableSet(variables.keySet());
	}

	/**
	 * Set a variable.
	 * 
	 * @param name
	 *            the name of the variable.
	 * @param value
	 *            the value of the variable.
	 */
	public void setVariable(String name, IVariable value) {
		if (name == null)
			throw new IllegalArgumentException("The variable name cannot be null!");

		name = name.trim();
		if (name.isEmpty())
			throw new IllegalArgumentException("The variable name cannot be empty!");

		// Validate not starting with number or special character.
		char f = name.charAt(0);
		if ((f >= '0' && f <= '9') || f == '.')
			throw new IllegalArgumentException("The variable name may not start with a numeric character!");

		if (name.contains("(") || name.contains(")"))
			throw new IllegalArgumentException("The variable name may not contain \"(\" or \")\"");

		// Set
		if (value == null)
			variables.remove(name);
		else
			variables.put(name, value);
	}

	/**
	 * Set a variable.
	 * 
	 * @param name
	 *            the name of the variable.
	 * @param value
	 *            the value of the variable.
	 */
	public void setVariable(String name, BigDecimal value) {
		// TODO disallow certain characters
		setVariable(name, new VarStatic(value));
	}

	/**
	 * Check to see if the calculator environment supports an operation.
	 * 
	 * @param name
	 *            the operator for the operation.
	 * @return true if the operation is supported.
	 */
	public boolean hasOperation(String operator) {
		return operations.containsKey(operator);
	}

	/**
	 * Get an operation by its operator.
	 * 
	 * @param operator
	 *            the operator.
	 * @return the operation.
	 */
	public IOperation getOperation(String operator) {
		return operations.get(operator);
	}

	/**
	 * Get a set containing all the supported operators.
	 * 
	 * @return the operators.
	 */
	public Set<String> getOperations() {
		return Collections.unmodifiableSet(operations.keySet());
	}

	/**
	 * Set an operation.
	 * 
	 * @param operator
	 *            the operator.
	 * @param operation
	 *            the operation.
	 */
	public void setOperation(String operator, IOperation operation) {
		if (operator == null)
			throw new IllegalArgumentException("The operator cannot be null!");

		operator = operator.trim();
		if (operator.isEmpty())
			throw new IllegalArgumentException("The variable operator cannot be empty!");

		char[] chrs = operator.toCharArray();
		for (int i = 0; i < chrs.length; i++) {
			char f = chrs[i];
			if ((f >= '0' && f <= '9') || f == '.')
				throw new IllegalArgumentException("The operator contain a numeric character!");

			if (Character.isWhitespace(f))
				throw new IllegalArgumentException("The operator may not contain whitespace!");
		}

		// Set
		if (operation == null)
			operations.remove(operator);
		else
			operations.put(operator, operation);
	}

	/**
	 * Check to see if the calculator has a function declared.
	 * 
	 * @param name
	 *            the name of the function.
	 * @return true if the function is declared.
	 */
	public boolean hasFunctionn(String name) {
		return functions.containsKey(name);
	}

	/**
	 * Get a function by its name.
	 * 
	 * @param name
	 *            the name of the function.
	 * @return the function, or null.
	 */
	public IFunction getFunction(String name) {
		return functions.get(name);
	}

	/**
	 * Get a set containing all the declared functions.
	 * 
	 * @return the functions.
	 */
	public Set<String> getFunctions() {
		return Collections.unmodifiableSet(functions.keySet());
	}

	/**
	 * Set a function.
	 * 
	 * @param name
	 *            the function name.
	 * @param function
	 *            the function object.
	 */
	public void setFunction(String name, IFunction function) {
		// Validate
		if (name == null)
			throw new IllegalArgumentException("The variable name cannot be null!");

		name = name.trim();
		if (name.isEmpty())
			throw new IllegalArgumentException("The variable name cannot be empty!");

		char f = name.charAt(0);
		if ((f >= '0' && f <= '9') || f == '.')
			throw new IllegalArgumentException("The function name may not start with a numeric character!");

		// Set
		if (function == null)
			functions.remove(name);
		else
			functions.put(name, function);
	}

	/**
	 * Get the math context used in operations.
	 * 
	 * @return the math context.
	 */
	public MathContext getMathContext() {
		return context;
	}

	/**
	 * Set the math context used in operations.
	 * 
	 * @param context
	 *            the math context.
	 */
	public void setMathContext(MathContext context) {
		if (context == null)
			throw new IllegalArgumentException("The math context cannot be null!");

		this.context = context;
	}

	/**
	 * Copy the environment.
	 * 
	 * @return the environment that was copied.
	 */
	@SuppressWarnings("unchecked")
	public ExpressionEnvironment copy() {
		ExpressionEnvironment env = new ExpressionEnvironment();
		env.opt_mulb = opt_mulb;
		env.opt_mulv = opt_mulv;
		env.operations = (HashMap<String, IOperation>) operations.clone();
		env.functions = (HashMap<String, IFunction>) functions.clone();
		env.variables = (HashMap<String, IVariable>) variables.clone();

		return env;
	}

}
