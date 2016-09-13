package com.parser.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field level annotation to use {@link StringParserUtil}.
 * @author priyaranjan-b
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MQField {

	/** position of the field in the class. */
	int position();

	/** length of the field , to be specified if not composite. */
	int length() default 0;
	
	/** specify if the field is composite */
	boolean isComposite() default false;

	/** specify the max size if the field is composite */
	int totalCollectionLength() default 0;
}
