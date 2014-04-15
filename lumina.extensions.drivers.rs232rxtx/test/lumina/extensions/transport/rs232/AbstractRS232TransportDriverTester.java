package lumina.extensions.transport.rs232;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;
import lumina.api.properties.IPropertySet;
import lumina.network.transport.api.ITransportDriver;
import lumina.network.transport.api.ITransportDriver.MessageEventListener;
import codebase.streams.TimeoutInputStream;
import codebase.streams.TimeoutOutputStream;

/**
 * Tests the transport layer.
 * <p>
 * <b>Requires com0com, read below:</b> Setup <tt>com0com</tt> to use a pair of
 * virtual ports where writing on one port will offer the data for reading in
 * the other port. And reading on one port will try to read from the other port.
 */
public abstract class AbstractRS232TransportDriverTester extends TestCase {

	/**
	 * Constant property name.
	 */
	public static final String PORT_NAME_PROP_NAME = "Port Name";

	/**
	 * Constant property name.
	 */
	public static final String BAUDRATE_PROP_NAME = "Baud Rate";

	/**
	 * Constant property name.
	 */
	public static final String DATABITS_PROP_NAME = "Data Bits";

	/**
	 * Constant property name.
	 */
	public static final String PARITY_PROP_NAME = "Parity";

	/**
	 * Constant property name.
	 */
	public static final String STOPBITS_PROP_NAME = "Stop Bits";

	private static final String UTF8 = "UTF-8";

	/**
	 * Default baudrate.
	 */
	private static final int DEFAULT_BAUDRATE = 9600;
	/**
	 * Default baudrate.
	 */
	private static final int DEFAULT_DATABITS = 8;
	/**
	 * Default baudrate.
	 */
	private static final Double DEFAULT_STOPBITS = 1.0;
	/**
	 * Default baudrate.
	 */
	private static final String DEFAULT_PARITY = "NONE";

	/**
	 * Sender port.
	 */
	private static final String COM_RECEIVE_PORT = "COM98";

	/**
	 * Receiver port.
	 */
	private static final String COM_SEND_PORT = "COM99";

	/**
	 * This port is used to confine the {@link #testSendTimeout()} test.
	 */
	private static final String SPECIAL_COM_PORT = "COM97";

	/**
	 * A test message.
	 */
	private static final String SIMPLE_MESSAGE = "THIS IS A TEST MESSAGE.";

	/**
	 * Returns the RS232 transport driver instance to test.
	 * 
	 * @return the RS232 transport driver to test.
	 */
	protected abstract ITransportDriver getRS232Driver();

	private void setConfigs(ITransportDriver driver, String comPort,
			int baudRate, int dataBits, Double stopBits, String parity) {
		IPropertySet configs = driver.getPropertySet();
		configs.findPropertyByName(PORT_NAME_PROP_NAME).setValue(comPort);
		configs.findPropertyByName(BAUDRATE_PROP_NAME).setValue(baudRate);
		configs.findPropertyByName(DATABITS_PROP_NAME).setValue(dataBits);
		configs.findPropertyByName(STOPBITS_PROP_NAME).setValue(stopBits);
		configs.findPropertyByName(PARITY_PROP_NAME).setValue(parity);
	}

	/**
	 * Tests correct operation of open() when the port is present.
	 * 
	 * @throws IOException
	 */
	public void testSimpleOpen() throws IOException {
		ITransportDriver driver = getRS232Driver();
		setConfigs(driver, COM_RECEIVE_PORT, DEFAULT_BAUDRATE,
				DEFAULT_DATABITS, DEFAULT_STOPBITS, DEFAULT_PARITY);
		assertTrue(driver.isPortPresent());
		driver.open();
		assertTrue(driver.isOpen());
		assertTrue(driver.close());
		assertFalse(driver.isOpen());
	}

	/**
	 * Tests correct operation of multiple open() and close() when the port is
	 * present.
	 * 
	 * @throws IOException
	 */
	// public void testMultipleOpen() throws IOException {
	// TransportDriver driver = getRS232Driver();
	// driver.setConfigurations(getConfigs(COM_CLIENT_PORT, DEFAULT_BAUDRATE));
	// assertTrue(driver.isPresent());
	// for (int i = 0; i < 5; i++) {
	// driver.open();
	// assertTrue(driver.isOpen());
	// assertTrue(driver.close());
	// }
	// assertFalse(driver.isOpen());
	// }

	// /**
	// * Tests correct operation of repeated open() and close() when the port is
	// * present.
	// *
	// * @throws IOException
	// */
	// public void testMultipleClose() throws IOException {
	// TransportDriver driver = getRS232Driver();
	// driver.setConfigurations(getConfigs(COM_CLIENT_PORT, DEFAULT_BAUDRATE));
	// assertTrue(driver.isPresent());
	// for (int i = 0; i < 5; i++) {
	// driver.open();
	// assertTrue(driver.isOpen());
	// }
	// for (int i = 0; i < 5; i++) {
	// assertTrue(driver.close());
	// }
	// assertFalse(driver.isOpen());
	// }

	/**
	 * Tests the basic send and receive operation.
	 * <p>
	 * Creates a pair of instances and sends a simple message from one end and
	 * checks that the message reaches the other end.
	 */
	public void testBasicSendReceive() throws IOException, InterruptedException {
		ITransportDriver sendDriver = getRS232Driver();
		setConfigs(sendDriver, COM_SEND_PORT, DEFAULT_BAUDRATE,
				DEFAULT_DATABITS, DEFAULT_STOPBITS, DEFAULT_PARITY);
		ITransportDriver receiveDriver = getRS232Driver();
		setConfigs(receiveDriver, COM_RECEIVE_PORT, DEFAULT_BAUDRATE,
				DEFAULT_DATABITS, DEFAULT_STOPBITS, DEFAULT_PARITY);
		receiveDriver.open();
		sendDriver.open();
		assertTrue(receiveDriver.isOpen());
		assertTrue(sendDriver.isOpen());
		// allow the threads of the receive driver to run
		sendDriver.getOutputStream().write(SIMPLE_MESSAGE.getBytes(UTF8));
		// receive
		byte[] message = new byte[SIMPLE_MESSAGE.length()];
		int bytesRead = receiveDriver.getInputStream().read(message);
		assertEquals(SIMPLE_MESSAGE, new String(message, UTF8));
		assertEquals(bytesRead, SIMPLE_MESSAGE.getBytes(UTF8).length);
		sendDriver.close();
		receiveDriver.close();
	}

	/**
	 * Tests the correct operation of open() when the port is not present.
	 * 
	 * @throws IOException
	 *             When the driver fails to open a port for communication.
	 */
	public void testSimpleOpenInexisting() throws IOException {
		ITransportDriver driver = getRS232Driver();
		setConfigs(driver, "INEXISTENT PORT NAME", DEFAULT_BAUDRATE,
				DEFAULT_DATABITS, DEFAULT_STOPBITS, DEFAULT_PARITY);
		assertFalse(driver.isPortPresent());
		driver.open();
		assertFalse(driver.isOpen());
		assertTrue(driver.close()); // does nothing
	}

	// /**
	// * Tests correct operation of re-opening after close.
	// *
	// * @throws IOException
	// */
	// public void testSimpleOpenReopen() throws IOException {
	// TransportDriver driver = getRS232Driver();
	// driver.setConfigurations(getConfigs(COM_SERVER_PORT, DEFAULT_BAUDRATE));
	// assertTrue(driver.isPresent());
	//
	// driver.open();
	// assertTrue(driver.isOpen());
	// assertTrue(driver.close());
	//
	// driver.open();
	// assertTrue(driver.isOpen());
	// assertTrue(driver.close());
	// }

	/**
	 * Tests that the driver detects that that the port is in use.
	 */
	public void testPortAlreadyInUse() throws IOException {
		ITransportDriver driver1 = getRS232Driver();
		setConfigs(driver1, COM_RECEIVE_PORT, DEFAULT_BAUDRATE,
				DEFAULT_DATABITS, DEFAULT_STOPBITS, DEFAULT_PARITY);
		ITransportDriver driver2 = getRS232Driver();
		setConfigs(driver2, COM_RECEIVE_PORT, DEFAULT_BAUDRATE,
				DEFAULT_DATABITS, DEFAULT_STOPBITS, DEFAULT_PARITY);
		driver1.open();
		driver2.open();
		assertFalse(driver2.isOpen());
		assertTrue(driver2.getErrorMessage() != null);
		assertTrue(driver2.hasError());

		driver1.close();
		driver2.close(); // does nothing
	}

	/**
	 * Tests sending and receiving a very small message.
	 * <p>
	 * Sending a empty message is the same as not sending anything
	 */
	public void test1ByteSendReceive() throws IOException, InterruptedException {
		ITransportDriver sendDriver = getRS232Driver();
		setConfigs(sendDriver, COM_SEND_PORT, DEFAULT_BAUDRATE,
				DEFAULT_DATABITS, DEFAULT_STOPBITS, DEFAULT_PARITY);
		ITransportDriver receiveDriver = getRS232Driver();
		setConfigs(receiveDriver, COM_RECEIVE_PORT, DEFAULT_BAUDRATE,
				DEFAULT_DATABITS, DEFAULT_STOPBITS, DEFAULT_PARITY);
		receiveDriver.open();
		sendDriver.open();
		assertTrue(receiveDriver.isOpen());
		assertTrue(sendDriver.isOpen());

		InputStream receiverInputStream = receiveDriver.getInputStream();

		// send
		sendDriver.getOutputStream().write(12);

		// sendDriver.getOutputStream().write(1);

		// allow the threads of the receive driver to run
		Thread.yield();
		Thread.sleep(50);

		// receive
		assertTrue(receiverInputStream.read() == 12);

		sendDriver.close();
		receiveDriver.close();
	}

	/**
	 * Tests sending and receiving an empty message.
	 * <p>
	 * Sending a empty message is the same as not sending anything
	 */
	public void testEmptySendReceive() throws IOException, InterruptedException {
		ITransportDriver sendDriver = getRS232Driver();
		setConfigs(sendDriver, COM_SEND_PORT, DEFAULT_BAUDRATE,
				DEFAULT_DATABITS, DEFAULT_STOPBITS, DEFAULT_PARITY);
		ITransportDriver receiveDriver = getRS232Driver();
		setConfigs(receiveDriver, COM_RECEIVE_PORT, DEFAULT_BAUDRATE,
				DEFAULT_DATABITS, DEFAULT_STOPBITS, DEFAULT_PARITY);
		sendDriver.open();
		receiveDriver.open();
		assertTrue(receiveDriver.isOpen());

		// send
		sendDriver.getOutputStream().write(new byte[] {});

		// allow the threads of the receive driver to run
		Thread.yield();
		Thread.sleep(50);

		// receive
		TimeoutInputStream ti = new codebase.streams.TimeoutInputStream(
				receiveDriver.getInputStream());
		boolean pass = false;
		try {
			ti.read();
		} catch (IOException e) {
			pass = true;
		}
		assertTrue(pass);

		ti.close();
		sendDriver.close();
		receiveDriver.close();
	}

	/**
	 * Tests that the messages are delivered in sequence.
	 * <p>
	 * Sends a sequence of small messages, waits until they are delivered and
	 * then checks that they arrived.
	 */
	public void testMessageSequence() throws IOException, InterruptedException {
		ITransportDriver sendDriver = getRS232Driver();
		setConfigs(sendDriver, COM_SEND_PORT, DEFAULT_BAUDRATE,
				DEFAULT_DATABITS, DEFAULT_STOPBITS, DEFAULT_PARITY);
		ITransportDriver receiveDriver = getRS232Driver();
		setConfigs(receiveDriver, COM_RECEIVE_PORT, DEFAULT_BAUDRATE,
				DEFAULT_DATABITS, DEFAULT_STOPBITS, DEFAULT_PARITY);
		sendDriver.open();
		receiveDriver.open();
		assertTrue(receiveDriver.isOpen());

		// send
		// without the Thread method calls below the bytes would be concatenated
		// and only one message would be delivered

		sendDriver.getOutputStream().write("@".getBytes(UTF8));
		Thread.yield();
		Thread.sleep(500);

		sendDriver.getOutputStream().write("@RS001".getBytes(UTF8));
		Thread.yield();
		Thread.sleep(500);

		sendDriver.getOutputStream().write("M3".getBytes(UTF8));
		Thread.yield();
		Thread.sleep(500);

		// receive
		// assertEquals("@", new String(receiveDriver.receive(), UTF8));
		// assertEquals("RS001", new String(receiveDriver.receive(), UTF8));
		// assertEquals("M3", new String(receiveDriver.receive(), UTF8));
		// assertNull(receiveDriver.receive());

		sendDriver.close();
		receiveDriver.close();
	}

	/**
	 * Tests that the input and output streams are operational.
	 */
	public void testIOStreamMessageSequence() throws IOException,
			InterruptedException {
		ITransportDriver sendDriver = getRS232Driver();
		setConfigs(sendDriver, COM_SEND_PORT, DEFAULT_BAUDRATE,
				DEFAULT_DATABITS, DEFAULT_STOPBITS, DEFAULT_PARITY);
		ITransportDriver receiveDriver = getRS232Driver();
		setConfigs(receiveDriver, COM_RECEIVE_PORT, DEFAULT_BAUDRATE,
				DEFAULT_DATABITS, DEFAULT_STOPBITS, DEFAULT_PARITY);
		sendDriver.open();
		receiveDriver.open();
		assertTrue(receiveDriver.isOpen());

		OutputStream os = sendDriver.getOutputStream();
		InputStream is = receiveDriver.getInputStream();

		// Basic os.write -> is.read sequence
		os.write("M1M2M3M4".getBytes(UTF8));
		byte[] read1 = new byte[6];
		assertEquals(6, is.read(read1));
		assertEquals("M1M2M3", new String(read1, UTF8));

		os.write("M5".getBytes(UTF8));
		byte[] read2 = new byte[4];
		assertEquals(4, is.read(read2));
		assertEquals("M4M5", new String(read2, UTF8));

		// driver.send -> is.read sequence
		os.write("M7".getBytes(UTF8));
		Thread.yield();
		Thread.sleep(500);

		// assertNull(is.read());
		byte[] read3 = new byte[2];
		assertEquals(2, is.read(read3));
		assertEquals("M7", new String(read3, UTF8));
		sendDriver.close();
		receiveDriver.close();
	}

	/**
	 * Tests that an exception is thrown while sending messages if the sender
	 * driver is closed before its time.
	 */
	public void testSendWithConnectionClosed() throws IOException,
			InterruptedException {
		ITransportDriver sendDriver = getRS232Driver();
		setConfigs(sendDriver, COM_SEND_PORT, DEFAULT_BAUDRATE,
				DEFAULT_DATABITS, DEFAULT_STOPBITS, DEFAULT_PARITY);
		sendDriver.open();
		assertTrue(sendDriver.isOpen());

		// send
		TimeoutOutputStream to = new TimeoutOutputStream(
				sendDriver.getOutputStream());
		to.open();

		boolean pass = false;

		// closing the base driver does not avoid writing
		sendDriver.close();

		// close the timeout stream
		to.close();

		try {
			to.write(SIMPLE_MESSAGE.getBytes(UTF8));
		} catch (IOException e) {
			// Stream closed error
			pass = true;
		}

		// extra close, should be harmless
		to.close();

		assertTrue(pass);
		assertTrue(sendDriver.close());
	}

	private boolean receiveCalled = false;

	/**
	 * Tests that a notification is thrown when new messages arrive and in error
	 * situations.
	 */
	public void testRS232DriverMessageNotifications() throws IOException,
			InterruptedException {
		ITransportDriver sendDriver = getRS232Driver();
		setConfigs(sendDriver, COM_SEND_PORT, DEFAULT_BAUDRATE,
				DEFAULT_DATABITS, DEFAULT_STOPBITS, DEFAULT_PARITY);
		ITransportDriver receiveDriver = getRS232Driver();
		setConfigs(receiveDriver, COM_RECEIVE_PORT, DEFAULT_BAUDRATE,
				DEFAULT_DATABITS, DEFAULT_STOPBITS, DEFAULT_PARITY);
		sendDriver.open();
		receiveDriver.open();
		assertTrue(receiveDriver.isOpen());

		OutputStream os = sendDriver.getOutputStream();

		final String testMessage = "M1M2M3";
		ITransportDriver.MessageEventListener listener = new MessageEventListener() {
			private int i = 0;
			private byte[] m = new byte[testMessage.length()];

			@Override
			public void receive(int message) {
				m[i++] = (byte) message;
				if (i < m.length)
					return;
				try {
					assertEquals("M1M2M3", new String(m, UTF8));
					receiveCalled = true;
				} catch (UnsupportedEncodingException e) {
				}
			}

			@Override
			public void error() {
			}
		};

		receiveDriver.addMessageListener(listener);

		// Basic os.write -> is.read sequence
		os.write(testMessage.getBytes(UTF8));
		Thread.sleep(100);
		assertTrue(receiveCalled);

		sendDriver.close();
		receiveDriver.close();
	}

	/**
	 * Tests that the messages sending returns timeout if the driver fails to
	 * send.
	 * <p>
	 * <b>WARNING:</b> This test may crash further tests since it cannot close
	 * the COM port after using it, so it will leave it open and locked,
	 * avoiding future tests to use it. This test checks that
	 * {@link #SPECIAL_COM_PORT} exists and only then it runs.
	 */
	public void testSendTimeout() throws IOException, InterruptedException {
		// TODO: set 4000 when property sets are ready!!

		final int timeoutConst = 200;
		ITransportDriver sendDriver = getRS232Driver();
		setConfigs(sendDriver, SPECIAL_COM_PORT, DEFAULT_BAUDRATE,
				DEFAULT_DATABITS, DEFAULT_STOPBITS, DEFAULT_PARITY);
		if (!sendDriver.isPortPresent())
			return;

		sendDriver.open();
		assertTrue(sendDriver.isOpen());

		// send
		long millisSendSimple = System.currentTimeMillis();

		sendDriver.getOutputStream().write(SIMPLE_MESSAGE.getBytes(UTF8));
		Thread.yield();
		Thread.sleep(500);

		assertTrue(System.currentTimeMillis() - millisSendSimple >= timeoutConst);

		/*
		 * Here we should test the following: assertFalse(sendDriver.close());
		 * 
		 * But since Timeout Output Stream only times out if its decorated
		 * stream write operation blocks, then we can no longer check if the
		 * driver is able to close or not because (i) if the decorated stream
		 * write() can block, the driver will not be able to close. (ii) if the
		 * decorated stream write cannot block, then the driver shall close
		 * correctly.
		 */
	}

	private boolean testPortOpen = false;
	private boolean testPortClose = false;

	/**
	 * Tests that a notification is thrown when the connection's status changes.
	 */
	public void testRS232DriverCommunicationNotifications() throws IOException,
			InterruptedException {
		ITransportDriver driver = getRS232Driver();
		setConfigs(driver, COM_RECEIVE_PORT, DEFAULT_BAUDRATE,
				DEFAULT_DATABITS, DEFAULT_STOPBITS, DEFAULT_PARITY);
		ITransportDriver.ConnectionStatusListener listener = new ITransportDriver.ConnectionStatusListener() {

			@Override
			public void cableConnected() {
			}

			@Override
			public void cableDisconnected() {
			}

			@Override
			public void portVanished() {
			}

			@Override
			public void portOpen() {
				testPortOpen = true;
			}

			@Override
			public void portClosed() {
				testPortClose = true;
			}
		};
		driver.addConnectionStatusListener(listener);

		driver.open();
		assertTrue(driver.isOpen());
		driver.close();
		assertFalse(driver.isOpen());
		assertTrue(testPortClose && testPortOpen);
	}
}
