/**
 *    Copyright [2020] [Ola Aronsson, courtesy of nollettnoll AB]
 *
 *    Licensed under the Creative Commons Attribution 4.0 International (the "License")
 *    you may not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *                https://creativecommons.org/licenses/by/4.0/
 *
 *    The software is provided “as is”, without warranty of any kind, express or
 *    implied, including but not limited to the warranties of merchantability,
 *    fitness for a particular purpose and noninfringement. In no event shall the
 *    authors or copyright holders be liable for any claim, damages or other liability,
 *    whether in an action of contract, tort or otherwise, arising from, out of or
 *    in connection with the software or the use or other dealings in the software.
 */

package com.github.catchitcozucan.core.internal.util.id;

import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.github.catchitcozucan.core.internal.util.MD5Digest;


public class IdGenerator {

	private static final String ONE = "1";
	private static final String NINE = "9";
	private static final String O = "O";
	private static final String NOLLSTR = "0";
	private static final String A_Z_0_9_AND_MASVINGE = "([A-Z0-9]{";
	private static final String MASVINGE_PARENTHESIS = "})";
	private static final String REST_OF_EXPR = "$1-";
	private static IdGenerator INSTANCE; //NOSONAR

	private static int COUNTER; // NOSONAR
	private Object counterLock = new Object();
	private final String seed;
	private static final int MINIMUN_VALUE_LENGTH = 6;
	private static final int MINIMUN_VALUE_LENGTH_MORE_RANDOM = 6;
	private final SecureRandom secureRandom;
	private AtomicInteger id;

	private IdGenerator() {
		seed = MD5Digest.getInstance().digestMessage("" + System.nanoTime());
		secureRandom = new SecureRandom();
		id = new AtomicInteger(0);
	}

	public static synchronized IdGenerator getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new IdGenerator();
		}
		return INSTANCE;
	}

	public Integer getNextId() {
		return id.getAndIncrement();
	}

	public String getId(int length) {
		if (length < MINIMUN_VALUE_LENGTH) {
			throw new IllegalArgumentException(MessageFormat.format("You cannot be serious generating \"IDs\" upon a value-range of {0} characters! Please provide a minimum of {1}", length, MINIMUN_VALUE_LENGTH));
		}

		synchronized (counterLock) {
			COUNTER++;
			String counterString = "" + COUNTER;
			if (counterString.length() > length) {
				throw new IllegalArgumentException(MessageFormat.format("Length {0} will cause an underflow for my counter is already up to {1}", length, COUNTER));
			}
			String result = new StringBuilder(String.format("%" + length + "s", seed.substring(counterString.length(), seed.length()) + COUNTER).replace(' ', '0')).reverse().toString(); // NOSONAR
			return result.substring(0, length);
		}
	}

    // basically from here : https://codereview.stackexchange.com/questions/159421/generate-16-digit-unique-code-like-product-serial
	public String getIdMoreRandom(int length, int groupDashing) {
		if (length < MINIMUN_VALUE_LENGTH_MORE_RANDOM) {
			throw new IllegalArgumentException(MessageFormat.format("You cannot be serious generating \"IDs\" upon a value-range of {0} characters! Please provide a minimum of {1}", length, MINIMUN_VALUE_LENGTH));
		}
		if (length < groupDashing) {
			throw new IllegalArgumentException(MessageFormat.format("You cannot be serious trying to group stuff whereas the number of groups {0} is larger then the total desired key length {1}", length, groupDashing));
		}

		int numberOfDashes = groupDashing;
		String nonFromatted = secureRandom.ints(0, 36).mapToObj(i -> Integer.toString(i, 36)).map(String::toUpperCase).distinct().limit(length).collect(Collectors.joining()); // as '0' and O are VERY hard to distinguish..
		if(groupDashing==0){
			return nonFromatted;
		}
		String formatted;
		formatted = nonFromatted.substring(0, numberOfDashes % length - 1).replaceAll(A_Z_0_9_AND_MASVINGE + (length / numberOfDashes) + MASVINGE_PARENTHESIS, REST_OF_EXPR) + nonFromatted.replaceAll(A_Z_0_9_AND_MASVINGE + (numberOfDashes % length) + MASVINGE_PARENTHESIS, REST_OF_EXPR).substring(numberOfDashes % length - 1);
		String cleaned = formatted.replace(O, NINE).replace(NOLLSTR, ONE);
		if (cleaned.endsWith("-")) {
			return cleaned.substring(0, cleaned.length() - 1);
		} else {
			return cleaned;
		}
	}
}