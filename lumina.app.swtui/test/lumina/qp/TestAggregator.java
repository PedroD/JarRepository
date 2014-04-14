package lumina.qp;

import junit.framework.TestCase;

public class TestAggregator extends TestCase {

	static class IntegerAggregator extends Aggregator<Integer> {

		@Override
		@SuppressWarnings("unchecked")
		public AggregateFunction<Integer, ?>[] createAggregateFunctions() {
			AggregateFunction<? extends Integer, ?> f = new AbstractSum<Integer>() {
				@Override
				public Number extract(Integer element) {
					return element;
				}
			};

			return (AggregateFunction<Integer, ?>[]) new AggregateFunction[] { f };
		}

		private static final Object EVEN = new Object();
		private static final Object ODD = new Object();

		@Override
		public Object[] getGroups(Integer o) {
			if (o % 2 == 0) {
				return new Object[] { EVEN };
			} else {
				return new Object[] { ODD };
			}
		}
	}

	public void testAggregatorBasic() {
		IterableSequenceSink<AggregateResult> output = new IterableSequenceSink<AggregateResult>();
		Aggregator<Integer> agg = new IntegerAggregator();
		agg.addSink(output);

		agg.insert(0);
		agg.insert(1);
		agg.insert(2);
		agg.insert(3);
		agg.insert(4);
		agg.insert(5);

		// XXX: to be finished
		// check the update propagation
	}
}
