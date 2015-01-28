package com.thebinaryfox.expcalj;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that specifies the order in which operations are evaluated.
 * 
 * @author The Binary Fox
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationOrder {

	/**
	 * The order, starting from 0 (being the lowest priority)
	 * 
	 * @return the operation order
	 */
	public int value();

}
