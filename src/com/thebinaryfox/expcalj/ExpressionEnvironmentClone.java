package com.thebinaryfox.expcalj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A class that "clones" another expression environment by using data from the
 * other environment while maintaining its own data maps.
 * 
 * @author The Binary Fox
 */
public class ExpressionEnvironmentClone extends ExpressionEnvironment {

	// Source environment.
	private ExpressionEnvironment src;

	/**
	 * Create a new cloned calculator environment.
	 * 
	 * @param env
	 *            the source environment.
	 */
	public ExpressionEnvironmentClone(ExpressionEnvironment env) {
		super();
		src = env;

		setMathContext(env.getMathContext());
		setVariableMultiplyEnabled(env.isVariableMultiplyEnabled());
		setBracketMultiplyEnabled(env.isBracketMultiplyEnabled());
	}

	@Override
	public boolean hasVariable(String name) {
		if (variables.containsKey(name)) {
			IVariable var = variables.get(name);
			if (var == null)
				return false;

			return true;
		}

		return src.hasVariable(name);
	}

	@Override
	public IVariable getVariable(String name) {
		if (variables.containsKey(name)) {
			return variables.get(name);
		}

		return src.getVariable(name);
	}

	@Override
	public Set<String> getVariables() {
		ArrayList<String> vas = new ArrayList<String>();
		vas.addAll(src.getVariables());

		Iterator<String> vai = variables.keySet().iterator();
		while (vai.hasNext()) {
			String k = vai.next();
			Object v = variables.get(k);

			if (v == null) {
				vas.remove(k);
			} else {
				if (!vas.contains(k))
					vas.add(k);
			}
		}

		return Collections.unmodifiableSet(new HashSet<String>(vas));
	}

	@Override
	public void setVariable(String name, IVariable value) {
		if (name != null)
			name = name.trim();

		validateVariable(name);

		// Set
		if (value == null) {
			if (src.hasVariable(name))
				variables.put(name, null);
			else if (variables.containsKey(name))
				variables.remove(name);
		} else
			variables.put(name, value);
	}

	@Override
	public boolean hasOperation(String operator) {
		if (operations.containsKey(operator)) {
			IOperation op = operations.get(operator);
			if (op == null)
				return false;

			return true;
		}

		return src.hasOperation(operator);
	}

	@Override
	public IOperation getOperation(String operator) {
		if (operations.containsKey(operator)) {
			return operations.get(operator);
		}

		return src.getOperation(operator);
	}

	@Override
	public Set<String> getOperations() {
		ArrayList<String> ops = new ArrayList<String>();
		ops.addAll(src.getOperations());

		Iterator<String> opi = operations.keySet().iterator();
		while (opi.hasNext()) {
			String k = opi.next();
			Object v = operations.get(k);

			if (v == null) {
				ops.remove(k);
			} else {
				if (!ops.contains(k))
					ops.add(k);
			}
		}

		return Collections.unmodifiableSet(new HashSet<String>(ops));
	}

	@Override
	public void setOperation(String operator, IOperation operation) {
		if (operator != null)
			operator = operator.trim();

		validateOperator(operator);

		// Set
		if (operation == null) {
			if (src.hasOperation(operator))
				operations.put(operator, null);
			else if (operations.containsKey(operator))
				operations.remove(operator);
		} else
			operations.put(operator, operation);
	}

	@Override
	public boolean hasFunction(String name) {
		if (functions.containsKey(name)) {
			IFunction func = functions.get(name);
			if (func == null)
				return false;

			return true;
		}

		return src.hasFunction(name);
	}

	@Override
	public IFunction getFunction(String name) {
		if (functions.containsKey(name))
			return functions.get(name);

		return src.getFunction(name);
	}

	@Override
	public Set<String> getFunctions() {
		ArrayList<String> fns = new ArrayList<String>();
		fns.addAll(src.getFunctions());

		Iterator<String> fni = functions.keySet().iterator();
		while (fni.hasNext()) {
			String k = fni.next();
			Object v = functions.get(k);

			if (v == null) {
				fns.remove(k);
			} else {
				if (!fns.contains(k))
					fns.add(k);
			}
		}

		return Collections.unmodifiableSet(new HashSet<String>(fns));
	}

	@Override
	public void setFunction(String name, IFunction function) {
		if (name != null)
			name = name.trim();

		validateFunction(name);

		// Set
		if (function == null)
			if (src.hasFunction(name))
				functions.put(name, null);
			else if (functions.containsKey(name))
				functions.remove(name);
			else
				functions.put(name, function);
	}

	/**
	 * Do a deep clone of the environment, including the operation, function,
	 * and variable maps. This will take considerably longer than a non-clone
	 * environment due to having to copy everything up to the original source
	 * environment.
	 * 
	 * @return the cloned environment.
	 */
	@Override
	public ExpressionEnvironment copyDeep() {
		ExpressionEnvironment env = new ExpressionEnvironment();
		env.opt_mulb = opt_mulb;
		env.opt_mulv = opt_mulv;
		env.context = context;

		env.operations = new HashMap<String, IOperation>();
		Iterator<String> operations = getOperations().iterator();
		while (operations.hasNext()) {
			String k = operations.next();
			IOperation v = getOperation(k);

			if (v != null)
				env.operations.put(k, v);
		}

		env.functions = new HashMap<String, IFunction>();
		Iterator<String> functions = getOperations().iterator();
		while (functions.hasNext()) {
			String k = functions.next();
			IFunction v = getFunction(k);

			if (v != null)
				env.functions.put(k, v);
		}

		env.variables = new HashMap<String, IVariable>();
		Iterator<String> variables = getOperations().iterator();
		while (variables.hasNext()) {
			String k = variables.next();
			IVariable v = getVariable(k);

			if (v != null)
				env.variables.put(k, v);
		}

		return env;
	}

}
