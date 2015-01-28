package com.thebinaryfox.expcalj;

import java.math.BigDecimal;

/**
 * An object representing a step in the evaluation.
 * 
 * @author The Binary Fox
 */
public class ExpressionStep {

	private ExpressionStep next;
	private ExpressionStep previous;
	private BigDecimal left;
	private BigDecimal right;
	private IOperation op;
	private int op_order = 0;

	public ExpressionStep(BigDecimal left, IOperation op, BigDecimal right) {
		this.left = left;
		this.right = right;
		this.op = op;

		// Get order.
		OperationOrder oo = op.getClass().getAnnotation(OperationOrder.class);
		if (oo != null) {
			op_order = oo.value();
		}
	}

	public int getOrder() {
		return op_order;
	}

	public boolean hasNext() {
		return next != null;
	}

	public ExpressionStep next() {
		return next;
	}

	public boolean hasPrevious() {
		return previous != null;
	}

	public ExpressionStep previous() {
		return previous;
	}

	public ExpressionStep add(ExpressionStep step) {
		setNext(step);
		step.setPrevious(this);
		return step;
	}

	public void setNext(ExpressionStep step) {
		next = step;
	}

	public void setPrevious(ExpressionStep step) {
		previous = step;
	}

	public void updateLeft(BigDecimal left) {
		this.left = left;
	}

	public void updateRight(BigDecimal right) {
		this.right = right;
	}

	public BigDecimal getLeft() {
		return left;
	}

	public BigDecimal getRight() {
		return right;
	}

	public IOperation getOperation() {
		return op;
	}

	@Override
	public String toString() {
		return getLeft().toPlainString() + getOperation().toString() + getRight().toPlainString();
	}

	public String toStringExpression() {
		ExpressionStep step = this;
		while (step.hasPrevious()) {
			step = step.previous();
		}

		StringBuilder sb = new StringBuilder(step.getLeft().toPlainString());
		sb.append(step.getOperation().toString());

		while (step.hasNext()) {
			step = step.next();

			sb.append(step.getLeft().toPlainString());
			sb.append(step.getOperation().toString());
		}

		sb.append(step.getRight().toPlainString());
		return sb.toString();
	}

}
