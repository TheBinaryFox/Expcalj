package expcalj.cli;

import java.math.BigDecimal;

import com.thebinaryfox.expcalj.Expression;
import com.thebinaryfox.expcalj.ExpressionEnvironment;
import com.thebinaryfox.expcalj.ExpressionStep;

/**
 * An extension of the Expression class that reports the steps taken in detail.
 * 
 * @author The Binary Fox
 */
public class DebugExpression extends Expression {

	public DebugExpression(String expression) {
		super(expression);
	}

	public DebugExpression(String expression, ExpressionEnvironment environment) {
		super(expression, environment);
	}

	@Override
	protected ExpressionStep parseExpression() {
		System.out.println("    Parsing...");
		return super.parseExpression();
	}

	@Override
	protected BigDecimal evaluateSteps(ExpressionStep step) {
		System.out.println("    Evaluating...");
		return super.evaluateSteps(step);
	}

	@Override
	protected BigDecimal evaluateStep(ExpressionStep step) {
		System.out.println("        " + step.getLeft().toPlainString() + step.getOperation().toString() + step.getRight().toPlainString());
		return super.evaluateStep(step);
	}

}
