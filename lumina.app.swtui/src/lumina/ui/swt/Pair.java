package lumina.ui.swt;

/**
 * An hash-compatible pair class.
 * 
 * @param <A>
 *            the type of the first component of the pair
 * @param <B>
 *            the type of the second component of the pair
 */
public class Pair<A, B> {

	/**
	 * The fist component.
	 */
	private final A first;

	/**
	 * The second component.
	 */
	private final B second;

	/**
	 * Creates a pair from the given components.
	 * 
	 * @param first
	 *            the first component.
	 * @param second
	 *            the second component.
	 */
	public Pair(A first, B second) {
		super();
		this.first = first;
		this.second = second;
	}

	@Override
	public int hashCode() {
		int hashFirst = first != null ? first.hashCode() : 0;
		int hashSecond = second != null ? second.hashCode() : 0;

		return (hashFirst + hashSecond) * hashSecond + hashFirst;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object other) {
		if (other instanceof Pair<?, ?>) {
			final Pair<A, B> otherPair = (Pair<A, B>) other;
			return ((this.first == otherPair.first || (this.first != null
					&& otherPair.first != null && this.first
						.equals(otherPair.first))) && (this.second == otherPair.second || (this.second != null
					&& otherPair.second != null && this.second
						.equals(otherPair.second))));
		}

		return false;
	}

	@Override
	public String toString() {
		return "(" + first.toString() + ", " + second.toString() + ")";
	}

	public A getFirst() {
		return first;
	}

	public B getSecond() {
		return second;
	}
}
