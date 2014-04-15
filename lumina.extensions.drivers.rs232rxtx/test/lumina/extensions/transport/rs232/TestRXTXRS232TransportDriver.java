package lumina.extensions.transport.rs232;

import lumina.network.transport.api.ITransportDriver;
import lumina.extensions.drivers.rs232rxtx.transport.RS232Driver;

/**
 * Tests the transport layer.
 * <p>
 * <b>Requires com0com, read below:</b> Setup <tt>com0com</tt> to use a pair of
 * virtual ports where writing on one port will offer the data for reading in
 * the other port. And reading on one port will try to read from the other port.
 */
public class TestRXTXRS232TransportDriver extends
		AbstractRS232TransportDriverTester {

	@Override
	protected ITransportDriver getRS232Driver() {
		return new RS232Driver();
	}
}
