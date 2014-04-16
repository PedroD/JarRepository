package lumina.kernel.sequences;

import java.util.Iterator;

/**
 * Base class for item sequence generators.
 * <p>
 * Sequence generators are needed to generate new area numbers, new channel
 * number or new IDs.
 * 
 * FIXME: This should be promoted to an allocator service...
 */
public abstract class ItemSequence implements Iterator<Object> {

	/**
	 * The prefix that used to form the items. This will usually be an item name
	 * or a device type.
	 */
	private final String lookupPrefix;

	/**
	 * The sequence start number.
	 */
	private int sequenceStartNumber;

	/**
	 * The current sequence number.
	 */
	private int currentSequenceNumber;

	private static String stripBaseName(final String name) {
		final int openParIndex = name.lastIndexOf('(');
		if (openParIndex > 0) {
			final String noParLast = name.substring(0, openParIndex - 1);
			return noParLast.trim();
		} else {
			return name;
		}
	}

	/**
	 * Creates a sequence with a prefix.
	 * 
	 * @param prefix
	 *            The constant prefix
	 * @param start
	 *            Minimum value for the sequence
	 */
	public ItemSequence(final String prefix, final int start) {
		lookupPrefix = prefix;
		sequenceStartNumber = start;
		currentSequenceNumber = Integer.MIN_VALUE;
	}

	/**
	 * Creates a sequence with no prefix (i.e. a purely numeric sequence)
	 * 
	 * @param start
	 *            Minimum value for the sequence
	 */
	public ItemSequence(final int start) {
		lookupPrefix = null;
		sequenceStartNumber = start;
		currentSequenceNumber = Integer.MIN_VALUE;
	}

	/**
	 * Finds the maximum sequence number used so far.
	 * 
	 * @param prefix
	 *            the prefix to be used, a <code>null</code> value indicates
	 *            that a prefix does not apply
	 * @return the maximum sequence used so far or <tt>-1</tt> if no one is
	 *         used.
	 */
	protected abstract int findMax(final String prefix);

	/**
	 * Creates a new sequence item based of the supplied prefix and sequence
	 * number.
	 * 
	 * @param prefix
	 *            the prefix to be used, a <code>null</code> value indicates
	 *            that a prefix does not apply
	 * @param seqno
	 *            the sequence number
	 * @return a new sequence item that cannot be <code>null</code>
	 */
	protected abstract Object makeNew(final String prefix, final int seqno);

	/**
	 * Creates a default name for an item.
	 * <p>
	 * 
	 * @param baseName
	 *            the base name desired
	 * @param seq
	 *            the sequential number
	 * @return a name with where the new sequential number is enclosed within
	 *         parenthesis
	 */
	protected static String createDefaultName(final String baseName,
			final int seq) {
		final int openParIndex = baseName.lastIndexOf('(');
		if (openParIndex > 0) {
			final int closeParIndex = baseName.lastIndexOf(')');
			/* parenthesis exist and are well-formed */
			if (closeParIndex > openParIndex) {
				if (seq <= 1) {
					return baseName;
				} else {
					return baseName.substring(0, openParIndex - 1) + " (" + seq
							+ ")" + baseName.substring(closeParIndex + 1);
				}
			}
		}

		/* parenthesis do not exist or are not well-formed */
		if (seq <= 1) {
			return baseName;
		} else {
			return baseName + " (" + seq + ")";
		}
	}

	/**
	 * Reevaluates the findMax() function to ensure that the current sequence
	 * number is up to date.
	 * <p>
	 * After calling this function, it is guaranteed that currentSequence >=
	 * findMax(lookupPrefix) + 1
	 */
	public final void ensureMax() {
		final int temp = findMax(lookupPrefix) + 1;
		if (temp > currentSequenceNumber) {
			currentSequenceNumber = temp;
		}
	}

	public static final int idToNumber(final String prefix, final String id) {
		if (id == null || !id.startsWith(prefix)) {
			return -1;
		}
		final String seqPart = id.substring(prefix.length());
		try {
			return Integer.parseInt(seqPart);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public static final String numberToId(final String prefix, final int number) {
		if (number < 0) {
			return "";
		} else {
			return prefix + Integer.toString(number);
		}
	}

	public static int parseNameSequence(final String name, final String prefix) {
		if (name.startsWith(stripBaseName(prefix))) {
			final int openParIndex = name.lastIndexOf('(');
			if (openParIndex > 0) {
				final int closeParIndex = name.lastIndexOf(')');
				if (closeParIndex <= openParIndex) {
					return 1;
				} else {
					final String nameNm = name.substring(openParIndex + 1,
							closeParIndex);
					int num;
					try {
						num = Integer.parseInt(nameNm);
					} catch (NumberFormatException e) {
						num = 1;
					}
					return num;
				}
			} else {
				return 1;
			}
		} else {
			return 0;
		}
	}

	/*
	 * Iterator implementation
	 */

	public final boolean hasNext() {
		return true;
	}

	public final Object next() {
		if (currentSequenceNumber == Integer.MIN_VALUE) {
			currentSequenceNumber = sequenceStartNumber;
			ensureMax();
		}

		final Object nextItem = makeNew(lookupPrefix, currentSequenceNumber);
		currentSequenceNumber++;

		assert nextItem != null : "new item to return cannot be null";
		return nextItem;
	}

	public final void remove() {
		throw new UnsupportedOperationException();
	}
}
