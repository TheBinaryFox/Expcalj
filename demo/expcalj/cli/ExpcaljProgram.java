package expcalj.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import com.thebinaryfox.expcalj.Expression;
import com.thebinaryfox.expcalj.ExpressionEnvironment;
import com.thebinaryfox.expcalj.ExpressionException;
import com.thebinaryfox.expcalj.IFunction;
import com.thebinaryfox.expcalj.IOperation;
import com.thebinaryfox.expcalj.IVariable;
import com.thebinaryfox.expcalj.OperationOrder;
import com.thebinaryfox.expcalj.operations.OpExponent;
import com.thebinaryfox.expcalj.variables.VarStatic;

/**
 * The test program.
 * 
 * @author The Binary Fox
 */
public class ExpcaljProgram {

	static private HashMap<String, BigDecimal> local_vars;
	static private HashMap<String, UserFunction> local_funcs;

	static private ExpressionEnvironment env;
	static private Exception lasterror;
	static private String format;
	static private String ilstring;
	static private VarAns ans;
	static private boolean bsodmode;
	static private boolean running;
	static private boolean ansi;
	static private boolean nnl;

	static private String color(String code) {
		if (ansi)
			return "\033[" + code + "m";
		else
			return "";
	}

	static public void main(String[] args) {
		// Args
		ansi = false;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("--color")) {
				ansi = true;
			}
		}

		// Create the environment.
		env = new ExpressionEnvironment();
		ans = new VarAns();

		local_vars = new HashMap<String, BigDecimal>();
		local_funcs = new HashMap<String, UserFunction>();

		// Add to the environment.
		env.useDefault();

		env.setOperation("==", new TestOpEquals());
		env.setOperation("!=", new TestOpNotEquals());
		env.setOperation(">", new TestOpGreaterThan());
		env.setOperation("<", new TestOpLessThan());
		env.setOperation("^", new OpExponent());

		env.setVariable("ans", ans);

		implementBigDecimalMath();

		// PS1
		ilstring = "Calc";

		// Run
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String line = null;

		format = "plain";

		running = true;
		while (running) {
			try {
				drawInputLine();

				line = reader.readLine();
				handle(line);
			} catch (Exception ex) {
				if (bsodmode)
					bsod(ex);

				drawErrorLine(ex);
				if (!(ex instanceof ExpcaljException))
					lasterror = ex;
			} catch (StackOverflowError ex) {
				ExpressionException eex = new ExpressionException("Stack overflow!", ex);
				if (bsodmode)
					bsod(eex);

				drawErrorLine(eex);
				lasterror = eex;
			} finally {
				if (nnl) {
					nnl = false;
					continue;
				}

				System.out.println();
			}
		}
	}

	static private void drawInputLine() {
		System.out.print(color("37") + ilstring + "$ " + color("0"));
		System.out.flush();
	}

	static private void drawErrorLine(Exception ex) {
		if (ex instanceof ExpressionException || ex instanceof ExpcaljException) {
			System.out.println(color("31") + ex.getMessage() + color("0"));
		} else {
			String name = ex.getClass().getSimpleName();
			String msg = ex.getMessage();

			if (msg == null) {
				msg = "?";
			}

			System.out.println(color("1;31") + name + ": " + color("0;31") + msg + color("0"));
		}
	}

	static private void drawErrorDetails(Exception ex) {
		String name = ex.getClass().getSimpleName();
		String msg = ex.getMessage();

		if (msg == null) {
			msg = "?";
		}

		System.out.println(color("1;31") + name + ": " + color("0;31") + msg + color("0"));
		System.out.println(color("31") + "Stack trace:");
		ex.printStackTrace(System.out);
		System.out.print(color("0"));
	}

	static private void handleCommand(String line) {
		String command;
		String arguments;

		// Split
		int index = line.indexOf(' ');
		if (index == -1) {
			command = line;
			arguments = "";
		} else {
			command = line.substring(0, index).trim();
			arguments = line.substring(index + 1).trim();
		}

		command = command.toLowerCase();

		// Handle
		switch (command) {
		case "(":
			bsodmode = true;
			break;

		case "about":
			commandAbout(arguments);
			break;

		case "?":
		case "help":
			commandHelp(arguments);
			break;

		case "trace":
			commandTrace(arguments);
			break;

		case "stat":
			commandStat(arguments);
			break;

		case "context":
			commandContext(arguments);
			break;

		case "save":
			save(arguments);
			break;

		case "load":
			load(arguments);
			break;

		case "ps1":
			ilstring = arguments;
			break;

		case "format":
			format = arguments.toLowerCase();
			if (!(format.equals("plain") || format.equals("scientific") || format.equals("separated")))
				throw new ExpcaljException("format: unknown number formatting type.");

			break;

		case "benchmark":
			commandBenchmark(arguments);
			break;

		case "=":
		case "set":
		case "define":
			commandDefine(arguments, false);
			break;

		case "-":
		case "unset":
		case "undefine":
			commandUndefine(arguments, false);
			break;

		case "env":
			commandEnv(arguments);
			break;

		case "q":
		case "quit":
			if (!arguments.isEmpty()) {
				throw new ExpcaljException("quit: requires no arguments.");
			}

			running = false;
			break;
		default:
			throw new ExpcaljException("expcalj: unknown command \"" + command + "\"");
		}
	}

	static private void handle(String line) {
		line = line.trim();
		if (line.isEmpty()) {
			if (ansi)
				System.out.print("\033[1A");

			nnl = true;
			return;
		}

		if (line.startsWith(":")) {
			handleCommand(line.substring(1));
			return;
		}

		// Expression
		Expression ex = new Expression(line, env);
		ex.evaluate();

		ans.set(ex.getValue());

		// Format
		switch (format) {
		case "separated":
			String ps = bigDecimalTrim(ex.getValue());
			String dp = null;

			int decidx = ps.indexOf('.');
			if (decidx != -1) {
				dp = ps.substring(decidx + 1);
				ps = ps.substring(0, decidx);
			}

			int offset = ps.length() % 3;
			StringBuilder sb = new StringBuilder();

			int point = offset;
			if (offset > 0) {
				sb.append(ps.substring(0, offset));
			}

			for (; point < ps.length(); point += 3) {
				int npoint = point + 3;
				if (npoint > ps.length())
					npoint = ps.length();

				if (point != 0)
					sb.append(' ');
				sb.append(ps.substring(point, npoint));
			}

			if (dp != null) {
				sb.append('.');
				sb.append(dp);
			}

			System.out.println(color("32") + sb.toString() + color("0"));
			break;

		case "scientific":
			System.out.println(color("32") + ex.getValue().toEngineeringString() + color("0"));
			break;

		case "plain":
		default:
			System.out.println(color("32") + bigDecimalTrim(ex.getValue()) + color("0"));
			break;
		}
	}

	//
	//
	//

	static private void commandStat(String arguments) {
		if (arguments.isEmpty()) {
			throw new ExpcaljException("stat: requires two arguments.");
		}

		String what = null;
		int index = arguments.indexOf(' ');
		if (index == -1)
			throw new ExpcaljException("stat: requires two arguments.");

		what = arguments.substring(0, index).trim().toLowerCase();
		arguments = arguments.substring(index + 1).trim();

		switch (what) {
		case "f":
		case "func":
		case "function":
			IFunction function = env.getFunction(arguments);
			if (function == null)
				throw new ExpcaljException("stat: undefined function.");

			String fname = function.toString();
			if (fname.contains("@")) {
				fname = arguments;
			}

			if (!fname.endsWith(")"))
				fname = fname + "()";

			if (function instanceof UserFunction) {
				UserFunction uf = (UserFunction) function;

				System.out.println(color("34") + "function " + color("0") + fname + color("34") + " {" + color("0"));

				String cond = uf.getConditional();
				if (cond != null) {
					System.out.println("    " + color("34") + "if (" + color("0") + uf.getConditional() + color("34") + ")" + color("0"));
					System.out.println("        " + color("34") + "return " + color("0") + uf.getReturnExpression());
				}

				System.out.println("    " + color("34") + "return " + color("0") + uf.getExpression());
				System.out.println(color("34") + "}" + color("0"));
			} else {
				System.out.println(color("34") + "function " + color("0") + fname + color("34") + " {" + color("0"));
				System.out.println("    Java: " + function.getClass().getCanonicalName());
				System.out.println(color("34") + "}" + color("0"));
			}

			break;

		case "o":
		case "op":
		case "operation":
		case "operator":
			IOperation operator = env.getOperation(arguments);
			if (operator == null)
				throw new ExpcaljException("stat: undefined operator.");

			String oname = operator.toString();
			if (oname.contains("@")) {
				oname = arguments;
			}

			String ocode = "Java: " + operator.getClass().getCanonicalName();
			int oorder = 0;

			OperationOrder oorderobj = operator.getClass().getAnnotation(OperationOrder.class);
			if (oorderobj != null)
				oorder = oorderobj.value();

			if (oorder >= 0)
				System.out.println(color("34") + "@Order(" + color("0") + oorder + color("34") + ")" + color("0"));
			System.out.println(color("34") + "operator " + color("0") + oname + color("34") + " {" + color("0"));
			System.out.println("    " + ocode);
			System.out.println(color("34") + "}" + color("0"));
			break;

		case "v":
		case "var":
		case "variable":
			IVariable variable = env.getVariable(arguments);
			if (variable == null)
				throw new ExpcaljException("stat: undefined variable.");

			String vname = variable.toString();
			if (vname.contains("@")) {
				vname = arguments;
			}

			String vcode;
			if (variable instanceof VarStatic) {
				vcode = color("34") + "return " + color("0") + variable.value().toPlainString();
			} else {
				vcode = "Java: " + variable.getClass().getCanonicalName();
			}

			System.out.println(color("34") + "variable " + color("0") + vname + color("34") + " {" + color("0"));
			System.out.println("    " + vcode);
			System.out.println(color("34") + "}" + color("0"));
			break;

		default:
			throw new ExpcaljException("stat: can only stat \"function\", \"operator\", or \"variable\"");
		}
	}

	static private void commandHelp(String arguments) {
		int page = 1;
		int max = 4;

		if (!arguments.isEmpty()) {
			try {
				page = Integer.parseInt(arguments);
			} catch (Exception ex) {
				throw new ExpcaljException("help: invalid page.", ex);
			}

			if (page < 1 || page > max)
				throw new ExpcaljException("help: invalid page.");
		}

		System.out.println(color("1;33") + "Expcalj Help (" + page + " of " + max + ")" + color("0"));
		switch (page) {
		case 1:
			System.out.println(color("43;30") + ":about    " + color("0;33") + " - Info about Expcalj." + color("0"));
			System.out.println(color("43;30") + ":format   " + color("0;33") + " - Change the format of the output number." + color("0"));
			System.out.println(color("43;30") + ":help     " + color("0;33") + " - Expcalj calculator command reference." + color("0"));
			System.out.println(color("43;30") + ":quit     " + color("0;33") + " - Exit the Expcalj calculator." + color("0"));
			break;
		case 2:
			System.out.println(color("43;30") + ":undefine " + color("0;33") + " - Undefine a variable or function." + color("0"));
			System.out.println(color("43;30") + ":define   " + color("0;33") + " - Define a variable or function." + color("0"));
			System.out.println(color("43;30") + ":stat     " + color("0;33") + " - View statistics about a variable, function, or operator." + color("0"));
			System.out.println(color("43;30") + ":env      " + color("0;33") + " - List the variables and functions defined." + color("0"));
			break;
		case 3:
			System.out.println(color("43;30") + ":context  " + color("0;33") + " - Change the MathContext in the environment." + color("0"));
			System.out.println(color("43;30") + ":load     " + color("0;33") + " - Load an Expcalj CLI state file." + color("0"));
			System.out.println(color("43;30") + ":save     " + color("0;33") + " - Save an Expcalj CLI state file." + color("0"));
			break;
		case 4:
			System.out.println(color("43;30") + ":benchmark" + color("0;33") + " - Benchmark an expression evaluation." + color("0"));
			System.out.println(color("43;30") + ":trace    " + color("0;33") + " - Get the stack trace of the last error." + color("0"));
			break;
		}
	}

	static private void commandContext(String arguments) {
		if (arguments.isEmpty()) {
			throw new ExpcaljException("context: requires a context.");
		}

		arguments = arguments.toLowerCase();
		
		// 32 bit
		if (arguments.equals("32 bit") || arguments.equals("32-bit") || arguments.equals("32bit")) {
			env.setMathContext(MathContext.DECIMAL32);
			System.out.println(color("33") + "Changed context to " + color("0") + "32 bit" + color("33") + "." + color("0"));
			return;
		}
		
		// 64 bit
		if (arguments.equals("64 bit") || arguments.equals("64-bit") || arguments.equals("64bit")) {
			env.setMathContext(MathContext.DECIMAL64);
			System.out.println(color("33") + "Changed context to " + color("0") + "64 bit" + color("33") + "." + color("0"));
			return;
		}
		
		// 128 bit
		if (arguments.equals("128 bit") || arguments.equals("128-bit") || arguments.equals("128bit")) {
			env.setMathContext(MathContext.DECIMAL128);
			System.out.println(color("33") + "Changed context to " + color("0") + "128 bit" + color("33") + "." + color("0"));
			return;
		}
		
		// Unlimited
		if (arguments.equals("unlimited") || arguments.equals("*")) {
			env.setMathContext(MathContext.UNLIMITED);
			System.out.println(color("33") + "Changed context to " + color("0") + "unlimited" + color("33") + "." + color("0"));
			return;
		}

		// Custom
		// TODO
		
		// Unknown
		throw new ExpcaljException("context: unknown math context.");
	}

	static private void commandAbout(String arguments) {
		if (!arguments.isEmpty()) {
			throw new ExpcaljException("about: requires no arguments.");
		}

		System.out.println(color("1;33") + "Expcalj Expression Calculator" + color("0"));
		System.out.println(color("33") + "Copyright (C) 2015, The Binary Fox." + color("0"));
	}

	static private void commandBenchmark(String arguments) {
		if (arguments.isEmpty()) {
			throw new ExpcaljException("benchmark: requires arguments.");
		}

		try {
			Expression e = new Expression(arguments, env);
			e.evaluate();
		} catch (Exception ex) {
			lasterror = ex;
			throw new ExpcaljException("benchmark: could not evaluate expression.");
		}

		int tests = 5;
		int runs = 100000;
		boolean slow = false;

		// Warm up.
		long time = System.currentTimeMillis();
		Expression wexp = new Expression(arguments, env);
		for (int i = 0; i < runs; i++) {
			wexp.evaluate();
		}

		long now = System.currentTimeMillis();
		if (now - time > 1000) {
			slow = true;
			System.out.print(color("33") + "This is taking a while... ");
		}

		// Test
		long[] times = new long[tests];
		for (int i = 0; i < tests; i++) {
			time = System.currentTimeMillis();
			Expression exp = new Expression(arguments, env);
			for (int j = 0; j < runs; j++) {
				exp.evaluate();
			}
			now = System.currentTimeMillis();
			times[i] = now - time;

			if (slow)
				System.out.print("*");
		}

		if (slow)
			System.out.println(color("0"));

		// Calculate
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		double avg = 0;

		for (int i = 0; i < tests; i++) {
			long c = times[i];
			if (c < min)
				min = c;
			if (c > max)
				max = c;
			avg += c;
		}

		avg = Math.floor((avg / tests) * 100) / 100;

		// Report
		System.out.println(color("33") + "Results of " + color("0") + runs + color("33") + " evaluations for " + color("0") + tests + color("33") + " tests:" + color("0"));
		System.out.println(color("33") + "    Min: " + color("0") + min + color("33") + " ms" + color("0"));
		System.out.println(color("33") + "    Max: " + color("0") + max + color("33") + " ms" + color("0"));
		System.out.println(color("33") + "    Avg: " + color("0") + avg + color("33") + " ms" + color("0"));
	}

	static private void commandEnv(String arguments) {
		String[] vars = env.getVariables().toArray(new String[0]);
		String[] fncs = env.getFunctions().toArray(new String[0]);

		int entries = vars.length + fncs.length;
		int pagesize = 9;
		int max = (int) Math.ceil((double) entries / pagesize);
		int page = 1;

		if (!arguments.isEmpty()) {
			try {
				page = Integer.parseInt(arguments);
			} catch (Exception ex) {
				throw new ExpcaljException("env: invalid page.", ex);
			}

			if (page < 1 || page > max)
				throw new ExpcaljException("env: invalid page.");
		}

		// Create env array.
		String[] arr = new String[vars.length + fncs.length];
		System.arraycopy(vars, 0, arr, 0, vars.length);
		for (int i = 0; i < fncs.length; i++) {
			arr[vars.length + i] = fncs[i] + "()";
		}

		Arrays.sort(arr);

		// List
		System.out.println(color("1;33") + "Expcalj Environment (" + page + " of " + max + ")" + color("0"));
		for (int i = (page - 1) * pagesize; i < arr.length && i < page * pagesize; i++) {
			String name = arr[i];

			String type = "?   ";
			String ext = null;
			String typecolor = null;
			String namecolor = null;
			if (name.endsWith("()")) {
				name = name.substring(0, name.length() - 2);
				IFunction ifn = env.getFunction(name);
				if (ifn instanceof UserFunction) {
					type = "User";
					typecolor = color("42;30");
					namecolor = color("32");
				} else {
					type = "Java";
					typecolor = color("43;30");
					namecolor = color("33");
				}

				ext = "()";
			} else {
				IVariable var = env.getVariable(name);
				if (var instanceof VarStatic) {
					type = "User";
					typecolor = color("42;30");
					namecolor = color("32");
				} else {
					type = "Java";
					typecolor = color("43;30");
					namecolor = color("33");
				}

				ext = "";
			}

			int len = 73 - name.length() - ext.length();
			StringBuilder pad = new StringBuilder();
			while (pad.length() < len)
				pad.append(" ");

			System.out.println(namecolor + name + color("2") + ext + color("0") + pad.toString() + " " + typecolor + " " + type + " " + color("0"));
		}
	}

	static private void commandTrace(String arguments) {
		if (!arguments.isEmpty()) {
			throw new ExpcaljException("trace: requires no arguments.");
		}

		if (lasterror == null)
			throw new ExpcaljException("trace: no error recorded");

		drawErrorDetails(lasterror);
	}

	static private void commandDefine(String arguments, boolean quiet) {
		String name = null;
		String value = null;
		String condition = null;
		String returnexp = null;
		LinkedList<String> params = null;

		StringBuilder buffer = new StringBuilder();
		boolean onvalue = false;
		boolean onparam = false;
		boolean wfequal = false;

		boolean func = false;

		char[] chrs = arguments.toCharArray();
		for (int i = 0; i < chrs.length; i++) {
			char chr = chrs[i];

			if (onvalue) {
				if (buffer.length() == 0)
					if (Character.isWhitespace(chr))
						continue;

				// Conditional
				if ((buffer.length() == 0 && !onparam) || onparam && (condition == null || returnexp == null)) {
					if (func) {
						if (chr == '[') {
							if (onparam)
								throw new ExpcaljException("Unexpected \"[\" in function condition.");

							onparam = true;
							continue;
						}

						if (chr == ']') {
							if (!onparam)
								throw new ExpcaljException("Unexpected \"]\" in function condition.");

							if (condition == null)
								condition = buffer.toString();
							else
								returnexp = buffer.toString();

							buffer.setLength(0);
							onparam = false;
							continue;
						}
					}

					buffer.append(chr);
					continue;
				}

				buffer.append(chr);
			} else {
				if (Character.isWhitespace(chr))
					continue;

				if (chr == '=') {
					if (name == null && buffer.length() > 0) {
						name = buffer.toString();
						buffer.setLength(0);
					}

					onvalue = true;
					wfequal = false;
					continue;
				} else if (wfequal)
					throw new ExpcaljException("define: unexpected \"" + chr + "\" in definition.");

				if (chr == '(') {
					if (onparam)
						throw new ExpcaljException("define: unexpected \"(\" in function definition.");

					func = true;
					params = new LinkedList<String>();
					name = buffer.toString();
					buffer.setLength(0);
					onparam = true;
					continue;
				}

				if (chr == ')') {
					if (!onparam)
						throw new ExpcaljException("define: unexpected \")\" in function definition.");

					String param = buffer.toString();
					if (param.isEmpty()) {
						if (!params.isEmpty())
							throw new ExpcaljException("define: no parameter name specified.");
					} else {
						params.add(param);
					}

					buffer.setLength(0);
					onparam = false;
					wfequal = true;
					continue;
				}

				if (chr == ',') {
					if (!onparam)
						throw new ExpcaljException("define: unexpected \",\" in definition.");

					String param = buffer.toString();
					if (param.isEmpty())
						throw new ExpcaljException("define: no parameter name specified.");

					buffer.setLength(0);
					params.add(param);
					continue;
				}

				buffer.append(chr);
			}
		}

		value = buffer.toString().trim();
		// Check
		if (name == null || name.isEmpty())
			throw new ExpcaljException("define: missing variable/function name.");

		if (value == null || value.isEmpty())
			throw new ExpcaljException("define: missing variable/function value.");

		char[] namechrs = name.toCharArray();
		for (int i = 0; i < namechrs.length; i++) {
			char namechr = namechrs[i];

			if (i == 0 && ((namechr >= '0' && namechr <= '9') || namechr == '.'))
				throw new ExpcaljException("define: variable/function name cannot start with a number.");

			if (!Character.isAlphabetic(namechr) && !((namechr >= '0' && namechr <= '9') || namechr == '.'))
				throw new ExpcaljException("define: variable/function name must be alpha numeric.");
		}

		// Define
		if (func) {
			defineFunction(name, params, condition, returnexp, value, quiet);
		} else {
			defineVariable(name, value, quiet);
		}
	}

	static private void commandUndefine(String arguments, boolean quiet) {
		if (arguments.isEmpty())
			throw new ExpcaljException("undefine: requires argument.");

		arguments = arguments.trim();

		if (arguments.endsWith("()")) {
			String name = arguments.substring(0, arguments.length() - 2);
			name = name.trim();

			IFunction fn = env.getFunction(name);
			if (fn == null)
				throw new ExpcaljException("undefine: function is already undefined!");
			if (!(fn instanceof UserFunction))
				throw new ExpcaljException("undefine: cannot remove Java-implemented function!");

			local_funcs.remove(name);
			env.setFunction(name, null);

			System.out.println(color("33") + "Successfully removed function.");
		} else {
			String name = arguments.trim();

			IVariable var = env.getVariable(name);
			if (var == null)
				throw new ExpcaljException("undefine: variable is already undefined!");
			if (!(var instanceof VarStatic))
				throw new ExpcaljException("undefine: cannot remove Java-implemented variable!");

			local_vars.remove(name);
			env.setVariable(name, (IVariable) null);

			System.out.println(color("33") + "Successfully removed variable.");

		}
	}

	static private void defineFunction(String name, List<String> params, String condition, String returnexp, String value, boolean quiet) {
		// Validate
		if (condition != null && returnexp == null)
			throw new ExpcaljException("define: missing return expression with condition.");

		// Validate params
		ExpressionEnvironment validenv = new ExpressionEnvironment();
		try {
			BigDecimal v = new BigDecimal(0);
			Iterator<String> parami = params.iterator();
			while (parami.hasNext())
				validenv.setVariable(parami.next(), v);

		} catch (IllegalArgumentException ex) {
			throw new ExpcaljException(ex.getMessage().replace("variable", "parameter"));
		}

		// Overwrite?
		IFunction ofunc = env.getFunction(name);
		if (ofunc != null && !(ofunc instanceof UserFunction))
			throw new ExpressionException("define: unable to overwrite Java-implemented function.");

		// Create
		UserFunction func = new UserFunction(name, params.toArray(new String[0]), value, condition, returnexp);

		env.setFunction(name, func);
		local_funcs.put(name, func);

		// Message
		if (!quiet) {
			StringBuilder paramstr = new StringBuilder();
			for (int i = 0; i < params.size(); i++) {
				if (i != 0) {
					paramstr.append(color("32"));
					paramstr.append(", ");
				}

				paramstr.append(color("0"));
				paramstr.append(params.get(i));
			}
			System.out.println(color("33") + "Defined \"" + color("32") + name + "(" + paramstr.toString() + color("32") + ")" + color("33") + "\" as \"" + color("32") + value + color("33") + "\""
					+ (condition == null ? "" : " with conditon") + "." + color("0"));
		}
	}

	static private void defineVariable(String name, String value, boolean quiet) {
		// Evaluate value.
		Expression exp = new Expression(value, env);
		BigDecimal dvalue = null;

		try {
			dvalue = exp.calculate();
		} catch (Exception ex) {
			throw new ExpressionException("define: unable to evaluate variable value.", ex);
		}

		// Overwrite?
		IVariable var = env.getVariable(name);
		if (var != null && !(var instanceof VarStatic))
			throw new ExpressionException("define: unable to overwrite Java-implemented variable.");

		// Set
		env.setVariable(name, dvalue);
		local_vars.put(name, dvalue);

		// Message
		if (!quiet) {
			System.out.println(color("33") + "Defined \"" + color("32") + name + color("33") + "\" as " + color("32") + dvalue.toPlainString() + color("33") + "." + color("0"));
		}
	}

	static private String str_force_length_a(String str, int len) {
		if (str.length() > len) {
			return str.substring(0, len - 3) + "...";
		}

		StringBuilder sb = new StringBuilder(str);
		while (sb.length() < len) {
			sb.append(" ");
		}

		return sb.toString();
	}

	static private String str_force_length_c(String str, int len) {
		if (str.length() > len) {
			return str.substring(0, len - 3) + "...";
		}

		StringBuilder sb = new StringBuilder(str);
		StringBuilder pb = new StringBuilder();
		int l = sb.length();
		while (l++ < len) {
			if (l % 2 == 1) {
				sb.append(' ');
			} else {
				pb.append(' ');
			}
		}

		return pb.toString() + sb.toString();
	}

	static private void bsod(Exception ex) {
		final int WIDTH = 80;

		String error = ex == null ? "ERROR" : ex.getClass().getSimpleName();
		String message = "Unknown error.";
		int lines = 3;

		// Print title
		System.out.println(color("47;34") + str_force_length_c(error, WIDTH) + color("0"));
		System.out.println(color("44;37") + str_force_length_c("Expcalj has encountered an unexpected error and has shut down to protect data.", WIDTH) + color("0"));
		System.out.println(color("44;37") + str_force_length_a("", WIDTH) + color("0"));

		// Print stack trace.
		if (ex != null) {
			if (ex.getMessage() != null)
				message = ex.getMessage();

			if (message.length() > 0)
				message = Character.toUpperCase(message.charAt(0)) + message.substring(1);

			System.out.println(color("1;44;37") + str_force_length_a(" " + message, WIDTH) + color("0"));
			lines++;

			StackTraceElement[] stack = ex.getStackTrace();
			for (int i = 0; i < stack.length; i++) {
				StackTraceElement ste = stack[i];

				lines++;
				System.out.println(color("44;37") + str_force_length_a(" " + ste.getClassName() + "." + ste.getMethodName() + "()", WIDTH - 8) + str_force_length_a(" : " + ste.getLineNumber(), 8)
						+ color("0"));
			}
		}

		// Print remaining lines.
		while (lines++ < 23) {
			System.out.println(color("44;37") + str_force_length_a("", WIDTH) + color("0"));
		}

		System.exit(0xC0000004);
	}

	static private void save(String name) {
		if (name.isEmpty()) {
			throw new ExpcaljException("save: requires a file name.");
		}

		if (!name.toLowerCase().endsWith(".ecj")) {
			name = name + ".ecj";
		}

		try {
			// A bit redundant below, I know. TODO
			if (name.startsWith(".") || name.startsWith("../") || name.startsWith("/") || name.startsWith("\\") || (name.length() > 1 && name.charAt(1) == ':') || name.contains(":"))
				throw new ExpcaljException("Only allowed to save in current or sub-directories!");

			File file = new File(name);
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));

			// Functions
			bw.write("# Expcalj state file.");
			bw.newLine();
			bw.write("# Functions");
			bw.newLine();
			Iterator<Entry<String, UserFunction>> funcs = local_funcs.entrySet().iterator();
			while (funcs.hasNext()) {
				Entry<String, UserFunction> pair = funcs.next();
				UserFunction func = pair.getValue();

				bw.write(func.getName());
				bw.write("(");

				String[] params = func.getParameters();
				for (int i = 0; i < params.length; i++) {
					if (i != 0)
						bw.write(",");
					bw.write(params[i]);
				}

				bw.write(") = ");

				String cond = func.getConditional();
				if (cond != null) {
					bw.write("[");
					bw.write(cond);
					bw.write("][");
					bw.write(func.getReturnExpression());
					bw.write("]");
				}

				bw.write(" ");
				bw.write(func.getExpression());
				bw.newLine();
			}

			// Vars
			bw.newLine();
			bw.write("# Variables");
			bw.newLine();
			Iterator<Entry<String, BigDecimal>> varss = local_vars.entrySet().iterator();
			while (varss.hasNext()) {
				Entry<String, BigDecimal> pair = varss.next();

				bw.write(pair.getKey());
				bw.write(" = ");
				bw.write(pair.getValue().toPlainString());
				bw.newLine();
			}

			// Finish
			bw.newLine();
			bw.close();

			System.out.println(color("33") + "Saved." + color("0"));
		} catch (IOException ex) {
			throw new RuntimeException("Failed to save!", ex);
		}
	}

	static private void load(String name) {
		if (name.isEmpty()) {
			if (new File("default.ecj").exists())
				name = "default.ecj";
			else
				throw new ExpcaljException("load: requires a file name.");
		}

		if (!name.toLowerCase().endsWith(".ecj") && !name.endsWith("/")) {
			name = name + ".ecj";
		}

		try {
			// A bit redundant below, I know. TODO
			if (name.startsWith(".") || name.startsWith("../") || name.startsWith("/") || name.startsWith("\\") || (name.length() > 1 && name.charAt(1) == ':') || name.contains(":"))
				throw new ExpcaljException("Only allowed to load in current or sub-directories!");

			File file = new File(name);
			if (file.isDirectory() && name.endsWith("/")) {
				File file2 = new File(file, "default.ecj");
				if (file2.exists())
					file = file2;
			}

			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;

			int linenum = 0;
			while ((line = br.readLine()) != null) {
				linenum++;
				line = line.trim();

				// Comment
				if (line.isEmpty() || line.startsWith("#"))
					continue;

				try {
					// Command
					if (line.startsWith(":")) {
						drawInputLine();
						System.out.println(line);
						handle(line);
						continue;
					}

					// Define
					commandDefine(line, true);
				} catch (Exception ex) {
					System.out.println(color("1;31") + "Error on line " + linenum + ": " + color("0;31") + ex.getMessage() + color("0"));
				}
			}

			br.close();
			System.out.println(color("1;33") + "Loaded." + color("0"));
		} catch (IOException ex) {
			throw new RuntimeException("Failed to load!", ex);
		}
	}

	static private String bigDecimalTrim(BigDecimal bd) {
		String str = bd.toPlainString();
		while (str.endsWith("0"))
			str = str.substring(0, str.length() - 1);

		return str;
	}

	static private final String[] BDM_Functions = new String[] { "com.thebinaryfox.expcalj.functions.FuncSquareRoot" };

	static private void implementBigDecimalMath() {
		// Functions
		for (int i = 0; i < BDM_Functions.length; i++) {
			try {
				Class<?> clas = Thread.currentThread().getContextClassLoader().loadClass(BDM_Functions[i]);
				if (IFunction.class.isAssignableFrom(clas)) {
					IFunction func = (IFunction) clas.newInstance();
					String name = func.toString();
					if (name.endsWith("()"))
						name = name.substring(0, name.length() - 2);

					env.setFunction(name, func);
				}
			} catch (Exception ex) {
				// FAIL
			}
		}
	}

}
