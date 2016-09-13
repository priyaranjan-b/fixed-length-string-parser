package com.parser.util;

/**
 * Custom exception class for {@link StringParserUtil} class
 * @author prbehera <br>
 *         $Id: MQParseException.java,v 1.1.2.2 2015-02-24 15:03:57 prbehera Exp $
 * @version 1.$Revision: 1.1.2.2 $
 */
public class ParserException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID=1L;

	/**
	 * Creates a new {@link ParserException}.
	 * @param message message
	 */
	public ParserException(String message) {
		super(message);
	}

	/**
	 * Creates a new {@link ParserException}.
	 * @param cause cause
	 */
	public ParserException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new {@link ParserException}.
	 * @param message message
	 * @param cause cause
	 */
	public ParserException(String message,Throwable cause) {
		super(message,cause);
	}

}
