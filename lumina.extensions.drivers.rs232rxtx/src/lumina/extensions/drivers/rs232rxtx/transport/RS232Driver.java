package lumina.extensions.drivers.rs232rxtx.transport;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import lumina.api.properties.AbstractProperty;
import lumina.api.properties.IProperty;
import lumina.api.properties.IPropertySet;
import lumina.api.properties.ImmutablePropertySet;
import lumina.api.properties.PropertySet;
import lumina.extensions.base.properties.ChoiceProperty;
import lumina.extensions.base.properties.types.NumberPropertyType;
import lumina.extensions.base.properties.types.RealPropertyType;
import lumina.extensions.base.properties.types.TextPropertyType;
import lumina.kernel.Logger;
import lumina.network.transport.api.AbstractTransportDriver;

import org.osgi.service.log.LogService;

import codebase.Filenames;
import codebase.streams.TimeoutOutputStream;

// TODO: Auto-generated Javadoc
/**
 * A fully asynchronous RS232 communication driver.
 * <p>
 * This device driver is designed to work with <a
 * href="http://www.rxtx.org/">RXTX</a> which is an open source implementation
 * of SUN's <a href=
 * "http://download.oracle.com/docs/cd/E17802_01/products/products/javacomm/reference/api/"
 * >COMM API</a>.
 * <p>
 * <b>Thread safety</b>. The driver has been designed with thread safety in
 * mind. It is very important to shield the RXTX event threads from other
 * threads. RXTX code is not reentrant. The RXTX driver will hang if any RXTX
 * method is called from an execution thread coming from RXTX itself. All events
 * are received in the {@link #serialEventListener} handler and data messages
 * are inserted into a queue and and a monitor thread is used to pull messages
 * out of this queue and call {@link #notifyMessageReceived(byte[])}. The driver
 * if fully asynchronous. Data can be read and written simultaneously.
 * <p>
 * <b>Cable connect/removal detection mechanism</b>. The driver is capable of
 * detecting cable connect and disconnect events. When connected with a full
 * cable, a DSR signal is sent periodically. Each time we receive a DSR signal,
 * we keep a timestamp. The timer {@link #cableConnectMonitor} check
 * periodically for a timeout condition and calls
 * {@link #notifyCableDisconnected()}. When the first DSR signal is received we
 * send a {@link #notifyCableConnected()}.
 * <p>
 * <b>Port removal detection mechanism</b>. For virtual COM ports and USB-o-COM
 * ports ports, the port can be removed. There is no elegant way to detect that
 * the port has been removed. Port removal will result in several I/O errors,
 * after a predefined number of I/O errors, the drives closes (i.e. shuts down).
 * The close operation checks if the port still exists. If the port does not
 * exist -but existed when {@link #open()} was called-, the
 * {@link #notifyPortVanished()} method is called.
 * <p>
 * <b>I/O error retry and auto-shutdown mechanism</b>. This is a protection
 * mechanism against serious interface connection errors. The driver keeps an
 * error counter that will be used to shut down the driver in case a number of
 * errors occur in a sequence. This counter is reset as soon as sent is
 * successful. This logic is encapsulated in the {@link RS232Driver.IOMonitor}
 * class. <b>Important note:</b> The driver does not perform an auto-connect
 * after the shutdown in order to prevent the possible cyclic start-stop
 * behavior that may arise.
 * <p>
 * A message may be delivered garbled in situations (1) the driver is not
 * physically capable of detecting that an error as occurred; (2) an I/O error
 * as occurred and the driver tried to recover but was not completely
 * successful.
 * <p>
 * <b>Hardware compliance</b>. So far we have been able to successfully test the
 * driver with the following hardware:
 * <ul>
 * <li>Simple 3-pin cable</li>
 * <li>Full 9-pin cable</li>
 * <li>Several USB-to-RS232 interfaces
 * </ul>
 * <p>
 * <b>Using the driver.</b> The drive should be used as follows:
 * <ol>
 * <li>Open/Close.</li> After constructing the RS232Driver make sure call
 * {@link #open()}. Shutdown is issued by calling {@link #close()}.
 * <li>Sending data.</li> Data can be sent by writing data to an output stream
 * obtained by {@link #getOutputStream()}.
 * <li>Reading data.</li> Data can be received by reading data from an input
 * stream obtained by {@link #getInputStream()} or by registering an
 * {@link MessageEventListener} through
 * {@link #addMessageListener(MessageEventListener)}.
 * </ol>
 * <p>
 * Implementation notes: So far, this driver has been tested only with RXTX.
 * There are other drivers that may prove to be useful if we choose to: 1.
 * SeriaIO (not free) 2. WinJCom
 * (http://www.engidea.com/blog/informatica/winjcom/winjcom.html)
 */
public class RS232Driver extends AbstractTransportDriver {

	/**
	 * This class encapsulates the behavior of the driver when an I/O error is
	 * found. For the moment all it does is an emergency close after a number of
	 * IO errors.
	 */
	private final class IOMonitor {
		/**
		 * Limit of message send/receive retries before signaling a an interface
		 * problem.
		 */
		private static final int MAGIC_IO_ERROR_LIMIT = 50;

		/**
		 * Tracks the number of retry attempts after detecting an IO error. This
		 * counter is used in the mechanism that retries sending the messages
		 */
		private int ioErrorRetryCount;

		/**
		 * Open successful.
		 */
		void openSuccessfull() {
			ioErrorRetryCount = 0;
		}

		/**
		 * Receive failed.
		 */
		void receiveFailed() {
			ioErrorRetryCount += 1;

			if (shouldClose()) {
				RS232Driver.this.emergencyCloseInterface();
			}
		}

		/**
		 * Send failed.
		 */
		@SuppressWarnings("unused")
		void sendFailed() {
			ioErrorRetryCount += 1;

			if (shouldClose()) {
				emergencyCloseInterface();
			}
		}

		/**
		 * Send successful.
		 */
		@SuppressWarnings("unused")
		void sendSuccessfull() {
			ioErrorRetryCount = 0;
		}

		/**
		 * Should close.
		 * 
		 * @return true, if successful
		 */
		boolean shouldClose() {
			return ioErrorRetryCount > MAGIC_IO_ERROR_LIMIT;
		}
	}

	/**
	 * This thread consumes the messages arriving from the network and performs
	 * the appropriate notifications. It serves to avoid deadlocks arising from
	 * running the {@link #notifyMessageReceived(byte[])} from the drivers
	 * thread.
	 * <p>
	 * Note that if there are no listeners registered there is no need to peek
	 * up messages. Maybe the client of this the driver will call
	 * {@link #receive()}.
	 */
	private final class MessageArrivalNotifier extends Thread {

		/**
		 * Instantiates a new message arrival notifier.
		 */
		MessageArrivalNotifier() {
			super(MESSAGE_ARRIVAL_THREAD_NAME);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			try {
				while (true) {
					/*
					 * We really need two queues in order to handle the messages
					 * coming from the network. If we try to use only one queue
					 * the take/poll method won't block the thread because the
					 * queue will not be empty.
					 */
					final int message = networkArrivedMessagesQueue.take();
					notifyMessageReceived(message);
				}
			} catch (InterruptedException ex) {
				/*
				 * If the thread is stopped its not a problem: we just ignore it
				 * since we are not locking any resources.
				 */
			}
		}
	}

	/**
	 * The parameters of the RS232 Port.
	 * <p>
	 * This class isolates the parameters from the implementation in the
	 * <tt>gnu.io</tt> package.
	 */
	private static final class SerialPortSettings {
		/**
		 * Valid Baud Rates for RS232.
		 */
		public static final Integer[] VALID_BAUD_RATES = new Integer[] { 110,
				300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 28800, 38400,
				56000, 57600, 115200, 128000, 153600, 230400, 256000, 460800,
				921600 };

		/**
		 * Valid Data Bits for RS232.
		 */
		public static final Integer[] VALID_DATA_BITS = new Integer[] { 5, 6,
				7, 8 };

		/**
		 * Valid Parity options for RS232.
		 */
		public static final String[] VALID_PARITIES = new String[] { "NONE",
				"EVEN", "ODD", "MARK", "SPACE" };

		/**
		 * Valid Stop Bits for RS232.
		 */
		public static final Float[] VALID_STOP_BITS = new Float[] { 1f, 1.5f,
				2f };

		/**
		 * Predefined values for no control flow handshake.
		 */
		public static final int FLOWCONTROL_NONE = SerialPort.FLOWCONTROL_NONE;

		/**
		 * Predefined values for 'RTSCTS IN' control flow handshake.
		 */
		public static final int FLOWCONTROL_RTSCTS_IN = SerialPort.FLOWCONTROL_RTSCTS_IN;

		/**
		 * Predefined values for 'RTSCTS OUT' control flow handshake.
		 */
		public static final int FLOWCONTROL_RTSCTS_OUT = SerialPort.FLOWCONTROL_RTSCTS_OUT;

		/**
		 * Predefined values for 'XON/XOFF IN' control flow handshake.
		 */
		public static final int FLOWCONTROL_XONXOFF_IN = SerialPort.FLOWCONTROL_XONXOFF_IN;

		/**
		 * Predefined values for 'XON/XOFF OUT' control flow handshake.
		 */
		public static final int FLOWCONTROL_XONXOFF_OUT = SerialPort.FLOWCONTROL_XONXOFF_OUT;

		/*
		 * Holders of the serial port parameters
		 */
		/**
		 * The flow control.
		 */
		public static int flowControl = FLOWCONTROL_NONE;

		/**
		 * The dtr.
		 */
		public static boolean dtr = false;

		/**
		 * The rts.
		 */
		public static boolean rts = false;

		/**
		 * The baud rate.
		 */
		public static int baudRate = 9600;
		/**
		 * The data bits.
		 */
		public static int dataBits = SerialPort.DATABITS_8;

		/**
		 * The stop bits.
		 */
		public static int stopBits = SerialPort.STOPBITS_1;

		/**
		 * The parity.
		 */
		public static int parity = SerialPort.PARITY_NONE;

		/**
		 * Creates a default serial port parameters object.
		 */
		private SerialPortSettings() {
		}
	}

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

	/**
	 * Debug flag used to control whether the RS232 signals should be sent to
	 * the log for debugging purposes or not.
	 */
	private static final boolean LOG_SERIAL_SIGNALS = false;

	/**
	 * Port default timeout in milliseconds for open, read and write operations.
	 */
	private static final int TIMEOUT_MILLIS = 500;

	/**
	 * The cable connect timeout interval used to determine that the cable has
	 * been disconnected.
	 */
	private static final int CABLE_CONNECT_TIMEOUT_MILLIS = 2000;

	/**
	 * Name of the cable connect monitor timer thread.
	 */
	private static final String CABLE_MONITOR_TIMER = "RS232 Cable Connect Monitor"; // NON-NLS-1

	/**
	 * Name of the message arrival thread.
	 */
	private static final String MESSAGE_ARRIVAL_THREAD_NAME = "RS232 Message Arrival Notifier"; // NON-NLS-1

	/**
	 * Size of the receive queue in messages.
	 */
	private static final int RECEIVE_QUEUE_MESSAGES = 8 * 1024;

	/**
	 * The default application name.
	 */
	private static final String DEFAULT_APPLICATION_NAME = "RS232 Driver: No applicaiton name!";

	/**
	 * Text used to find out that the USB port has been removed.
	 */
	private static final String IO_EXCEPTION_MAGIC_TEXT = "No error in nativeavailable";

	/**
	 * Gets the port identifier of a given port name.
	 * 
	 * @param portName
	 *            the port name
	 * @return the port identifier or <code>null</code> if the port does not
	 *         exist.
	 */
	private static CommPortIdentifier getPortId(final String portName) {
		try {
			@SuppressWarnings("rawtypes")
			final Enumeration portList;
			portList = CommPortIdentifier.getPortIdentifiers();

			if (portList != null) {
				/*
				 * Search for the port with the appropriate name
				 */
				while (portList.hasMoreElements()) {
					final CommPortIdentifier portId = (CommPortIdentifier) portList
							.nextElement();
					if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
						if (portId.getName().equals(portName)) {
							return portId;
						}
					}
				}
			}
		} catch (Throwable t) {

			/*
			 * If something happens assume not found at once
			 */
			return null;
		}
		return null;
	}

	/**
	 * Checks if the serial port is still there.
	 * 
	 * @param serialPort
	 *            the serial port object to be tested.
	 * @return <code>true</code> if a port with the name of the given object was
	 *         found; returns <code>false</code> if the port was not found.
	 */
	private static boolean portStillExists(final SerialPort serialPort) {
		if (serialPort != null) {
			/*
			 * Get the clean port name
			 */
			final String serialPortNameWithoutPath = Filenames
					.getBaseName(serialPort.getName());

			return portStillExists(serialPortNameWithoutPath);
		}
		return false;
	}

	/**
	 * Checks if a the serial port with the given name is still there.
	 * <p>
	 * Lists the ports and checks if the current port identifier is still in the
	 * list.
	 * 
	 * @param portName
	 *            the serial port name
	 * @return <code>true</code> if a port with the given name exists; returns
	 *         <code>false</code> if the port was not found.
	 */
	private static boolean portStillExists(final String portName) {
		try {
			@SuppressWarnings("rawtypes")
			final Enumeration portList = CommPortIdentifier
					.getPortIdentifiers();
			if (portList != null) {
				/*
				 * Search for the port with the appropriate name
				 */
				while (portList.hasMoreElements()) {
					final CommPortIdentifier portId = (CommPortIdentifier) portList
							.nextElement();

					/*
					 * Compare by name, don't trust the ids, who knows what may
					 * have happened.
					 */
					if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
						if (portId.getName().equals(portName)) {
							return true;
						}
					}
				}
			}
		} catch (Throwable t) {
			/*
			 * If something happens assume false at once
			 */
			return false;
		}
		return false;
	}

	/**
	 * The device/port name.
	 */
	private String serialPortName;

	/**
	 * The name of the application using the serial port.
	 */
	private String applicationName;

	/**
	 * The timeout for open, message send and receive operations in
	 * milliseconds.
	 */
	private final int driverTimeout;

	/**
	 * The stream used to send messages into the network.
	 */
	private TimeoutOutputStream timeoutOutputStream;

	/**
	 * The stream used to get data from the network.
	 */
	private InputStream rxtxInputStream;

	/**
	 * The serial port object. Always guard accesses to this variable using
	 * {@link #serialPortLock} lock. Set to <code>null</code> if the driver is
	 * closed.
	 */
	private SerialPort serialPort;

	/**
	 * Lock that prevents multiple threads from accessing the serial port
	 * object. {@link #serialPort}. This is needed because de RXTX library is
	 * not reentrant.
	 */
	private final Object serialPortLock = new Object();

	// TODO: refactor to include appropriate logging

	/**
	 * Queue that holds the messages arriving from the network. This queue is
	 * used to decouple the RXTX thread from the remaining code.
	 */
	private final LinkedBlockingQueue<Byte> networkArrivedMessagesQueue = new LinkedBlockingQueue<Byte>(
			RECEIVE_QUEUE_MESSAGES);

	/**
	 * Time stamp of the last physical connection notification. Used to
	 * determine if the last time a DSR notification was seen was too long ago.
	 */
	private volatile long lastConnectNotificationTimeStamp;

	/**
	 * Lock used when accessing the last notification time stamp.
	 */
	private final Object lastConnectNotificationLock = new Object();

	/**
	 * The timer that regularly checks if the cable is connected.
	 */
	private Timer cableConnectMonitor;

	/**
	 * Flag used to track if a notification for cable connected has been sent.
	 * Used to avoid duplicate notifications.
	 */
	private boolean notifiedCableConnected = false;

	/**
	 * Flag used to track if a notification for cable disconnected has been
	 * sent. Used to avoid duplicate notifications.
	 */
	private boolean notifiedCableDisconnected = false;

	/**
	 * Flag that tracks if the communication port is open.
	 */
	private boolean portIsOpen = false;

	/**
	 * Listener of serial port events. This object processes all data and events
	 * coming from the serial port.
	 */
	private final SerialPortEventListener serialEventListener = new SerialPortEventListener() {
		public void serialEvent(final SerialPortEvent event) {
			switch (event.getEventType()) {
			case SerialPortEvent.BI:
				Logger.getInstance().log(LogService.LOG_INFO,
						"SerialPortEvent - Break interrupt"); //$NON-NLS-1$
				break;
			case SerialPortEvent.OE:
				Logger.getInstance().log(LogService.LOG_INFO,
						"SerialPortEvent - Overrun error"); //$NON-NLS-1$
				break;
			case SerialPortEvent.PE:
				Logger.getInstance().log(LogService.LOG_INFO,
						"SerialPortEvent - Parity error"); //$NON-NLS-1$
				break;
			case SerialPortEvent.CD:
				Logger.getInstance().log(LogService.LOG_INFO,
						"SerialPortEvent - Carrier detect"); //$NON-NLS-1$
				break;
			case SerialPortEvent.CTS:
				Logger.getInstance().log(LogService.LOG_INFO,
						"SerialPortEvent - Clear to send"); //$NON-NLS-1$
				break;
			case SerialPortEvent.DSR:
				/*
				 * This event is received whenever we have the full - cable ( 9
				 * pin ) connected .
				 * 
				 * We only activate the connection monitor once we see at least
				 * on DSR signal , we don 't want this feature if the cable can
				 * 't honor the DSR bit.
				 */
				synchronized (lastConnectNotificationLock) {
					lastConnectNotificationTimeStamp = System
							.currentTimeMillis();
					enableCableConnectionMonitor();
					if (!notifiedCableConnected) {
						notifyCableConnected();

						notifiedCableConnected = true;
						notifiedCableDisconnected = false;
					}
				}
				break;
			case SerialPortEvent.RI:
				Logger.getInstance().log(LogService.LOG_INFO,
						"SerialPortEvent - Ring indicator"); //$NON-NLS-1$
				break;
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
				Logger.getInstance().log(LogService.LOG_INFO,
						"SerialPortEvent - Output buffer is empty"); //$NON-NLS-1$
				break;
			case SerialPortEvent.FE:
				Logger.getInstance().log(LogService.LOG_INFO,
						"SerialPortEvent - Framing error"); //$NON-NLS-1$

				/*
				 * A framing error consists of synchronization error caused by
				 * different speeds of the receiver and of the transmitter .
				 * 
				 * Whenever a framing error occurs we clean anything we have
				 * read from the buffer so far . We have observed that this
				 * option results in less offending messages being sent to the
				 * protocol layer.
				 */
				ioMonitor.receiveFailed();

				break;
			case SerialPortEvent.DATA_AVAILABLE:
				if (!ioMonitor.shouldClose()) {
					/*
					 * data has been received from the serial port
					 */
					try {
						final int available = rxtxInputStream.available();

						/*
						 * if there are no bytes available we still honor the
						 * event.
						 */
						if (available >= 0) {
							/*
							 * read data
							 */
							final byte[] readBuffer = new byte[available];
							final int numBytes = rxtxInputStream
									.read(readBuffer);

							for (int i = 0; i < numBytes; i++) {
								/*
								 * put the message in the queue to be consumed
								 */
								networkArrivedMessagesQueue.put(readBuffer[i]);
							}
						}
					} catch (IOException e) {
						/*
						 * We don't call notifyError() here. It will be called
						 * by the ioRetryMonitor after a certain amount of
						 * failed messages.
						 */
						ioMonitor.receiveFailed();
						Logger.getInstance().log(LogService.LOG_INFO,
								"Reading data from network: " + e.toString()); // NON-NLS-1

						if (e.toString().contains(IO_EXCEPTION_MAGIC_TEXT)) {
							RS232Driver.this.emergencyCloseInterface();
						}
					} catch (InterruptedException e) {
						ioMonitor.receiveFailed();
						Logger.getInstance().log(LogService.LOG_INFO,
								"Reading data from network: " + e.toString()); // NON-NLS-1
					}
				}
				break;
			default:
				break;
			}
		}
	};

	/**
	 * The instance of {@link MessageArrivalNotifier} that is lazily
	 * instantiated by {@link #openPort()}.
	 */
	private MessageArrivalNotifier messageArrivalNotifier;

	/**
	 * The {@link IOMonitor} instance.
	 */
	private final IOMonitor ioMonitor = new IOMonitor();

	/** The port configurations. */
	private final IPropertySet portConfigurations;

	/** The port name property. */
	private IProperty portNameProperty = new AbstractProperty(
			new TextPropertyType()) {

		@Override
		public String getDescription() {
			return "Name of the serial port";
		}

		@Override
		public String getName() {
			return PORT_NAME_PROP_NAME;
		}

		@Override
		public Object getValue() {
			return serialPortName;
		}

		@Override
		protected void helperSetValue(Object name) {
			serialPortName = (String) name;
		}
	};

	/** The databits property. */
	private IProperty databitsProperty = new ChoiceProperty(
			new NumberPropertyType()) {

		@Override
		public Object[] getChoices() {
			return SerialPortSettings.VALID_DATA_BITS;
		}

		@Override
		public String getDescription() {
			return "Data bits of the serial port";
		}

		@Override
		public String getName() {
			return DATABITS_PROP_NAME;
		}

		@Override
		public Object getValue() {
			// TODO: Backwards conversion to match the set value.
			return SerialPortSettings.dataBits;
		}

		@Override
		protected void helperSetValue(Object value) {
			Integer db = (Integer) value;
			switch (db) {
			case 5:
				SerialPortSettings.dataBits = SerialPort.DATABITS_5;
				break;
			case 6:
				SerialPortSettings.dataBits = SerialPort.DATABITS_6;
				break;
			case 7:
				SerialPortSettings.dataBits = SerialPort.DATABITS_7;
				break;
			case 8:
			default:
				SerialPortSettings.dataBits = SerialPort.DATABITS_8;
			}
		}
	};

	/** The baudrate property. */
	private IProperty baudrateProperty = new ChoiceProperty(
			new NumberPropertyType()) {

		@Override
		public Object[] getChoices() {
			return SerialPortSettings.VALID_BAUD_RATES;
		}

		@Override
		public String getDescription() {
			return "Baud rate of the serial port";
		}

		@Override
		public String getName() {
			return BAUDRATE_PROP_NAME;
		}

		@Override
		public Object getValue() {
			return SerialPortSettings.baudRate;
		}

		@Override
		protected void helperSetValue(Object value) {
			SerialPortSettings.baudRate = (Integer) value;
		}
	};

	/** The parity property. */
	private IProperty parityProperty = new ChoiceProperty(
			new TextPropertyType()) {

		@Override
		public Object[] getChoices() {
			return SerialPortSettings.VALID_PARITIES;
		}

		@Override
		public String getDescription() {
			return "Parity of the serial port";
		}

		@Override
		public String getName() {
			return PARITY_PROP_NAME;
		}

		@Override
		public Object getValue() {
			// TODO: Backwards conversion to match the set value. (must return
			// strings)
			return SerialPortSettings.parity;
		}

		@Override
		protected void helperSetValue(Object value) {
			String par = (String) value;
			if (par.equals("EVEN"))
				SerialPortSettings.parity = SerialPort.PARITY_EVEN;
			else if (par.equals("NONE"))
				SerialPortSettings.parity = SerialPort.PARITY_NONE;
			else if (par.equals("ODD"))
				SerialPortSettings.parity = SerialPort.PARITY_ODD;
			else if (par.equals("MARK"))
				SerialPortSettings.parity = SerialPort.PARITY_MARK;
			else
				SerialPortSettings.parity = SerialPort.PARITY_SPACE;
		}
	};

	private IProperty stopbitsProperty = new ChoiceProperty(
			new RealPropertyType()) {

		@Override
		public Object[] getChoices() {
			return SerialPortSettings.VALID_STOP_BITS;
		}

		@Override
		public String getDescription() {
			return "Stop bits of the serial port";
		}

		@Override
		public String getName() {
			return STOPBITS_PROP_NAME;
		}

		@Override
		public Object getValue() {
			// TODO: Backwards conversion to match the set value.
			if (SerialPortSettings.stopBits == SerialPort.STOPBITS_1)
				return 1.0;
			else if (SerialPortSettings.stopBits == SerialPort.STOPBITS_1_5)
				return 1.5;
			else
				return 2.0;
		}

		@Override
		protected void helperSetValue(Object v) {
			if (((Double) v).equals(2.0))
				SerialPortSettings.stopBits = SerialPort.STOPBITS_2;
			else if (((Double) v).equals(1.0))
				SerialPortSettings.stopBits = SerialPort.STOPBITS_1;
			else
				SerialPortSettings.stopBits = SerialPort.STOPBITS_1_5;
		}
	};

	/**
	 * Instantiates a new RS232 driver with timeout and port parameters.
	 * <p>
	 * Note that {@link #setPort(String)} must be called before the driver can
	 * be used.
	 */
	public RS232Driver() {
		applicationName = DEFAULT_APPLICATION_NAME;
		driverTimeout = TIMEOUT_MILLIS;
		IPropertySet set = new PropertySet();
		/*
		 * Set the properties in the configuration set.
		 */
		set.addProperty(portNameProperty);
		set.addProperty(databitsProperty);
		set.addProperty(baudrateProperty);
		set.addProperty(parityProperty);
		set.addProperty(stopbitsProperty);
		portConfigurations = new ImmutablePropertySet(set);
	}

	/**
	 * Method that closes the currently open communication connection.
	 * <p>
	 * This method also verifies if the connection being closed corresponds to a
	 * port that may have vanished. If so it performs the appropriate
	 * notification.
	 * 
	 * @return <code>true</code> if the close operation was successful;
	 *         <code>false</code> otherwise.
	 */
	@Override
	protected synchronized boolean closeDriver() {
		portIsOpen = false;
		if (messageArrivalNotifier != null) {
			messageArrivalNotifier.interrupt();
			messageArrivalNotifier = null;
		}

		return internalCleanupClose();
	}

	/**
	 * Disables the cable connection monitor.
	 */
	private void disableCableConnectionMonitor() {
		if (cableConnectMonitor != null) {
			cableConnectMonitor.cancel();
			cableConnectMonitor = null;
		}
	}

	/**
	 * Closes the network driver in the case of a fatal error.
	 * <p>
	 * Even if the interface is not closed, we don't care. We did our best.
	 */
	private synchronized void emergencyCloseInterface() {
		notifyIOError();

		internalCleanupClose();

		String portName = "";
		if (serialPort != null) {
			portName = serialPort.getName();
		}

		if (portName != null) {
			Logger.getInstance().log(
					LogService.LOG_INFO,
					"RS232Driver: Emergency close of the RS232 port "
							+ portName);
		} else {
			Logger.getInstance()
					.log(LogService.LOG_INFO,
							"RS232Driver: Emergency close of the network RS232. Port name could not be determined.");
		}
	}

	/**
	 * Enables the cable connection monitor.
	 * <p>
	 * If the monitor is already start, does nothing. The cable connect monitor
	 * is only activated upon the reception of a DSR signal.
	 */
	private void enableCableConnectionMonitor() {
		if (cableConnectMonitor == null) {
			cableConnectMonitor = new Timer(CABLE_MONITOR_TIMER);
			scheduleCableConnectMonitorTask();
		}
	}

	/**
	 * Obtains the that this port will publish when open.
	 * 
	 * @return the current application name, or <code>null</code> if not set
	 */
	public synchronized String getApplicationName() {
		return applicationName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lumina.extensions.transport.rs232.AbstractTransportDriver#
	 * getDriverOutputStream()
	 */
	@Override
	protected OutputStream getDriverOutputStream() {
		try {
			return this.serialPort.getOutputStream();
		} catch (IOException e) {
			setError(
					"Could not obtain an output stream to port "
							+ serialPortName,
					"Please check that the port is not being used by other process and it still exists.");
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * lumina.extensions.transport.rs232.AbstractTransportDriver#getDriverTimeout
	 * ()
	 */
	@Override
	protected int getDriverTimeout() {
		return driverTimeout;
	}

	/**
	 * Gets the list of port names.
	 * 
	 * @return an array of strings with the names of the serial ports or
	 *         <code>null</code> if the names cannot be obtained.
	 */
	@Override
	public String[] getExistingPortNames() {
		@SuppressWarnings("rawtypes")
		final Enumeration portList;
		try {
			portList = CommPortIdentifier.getPortIdentifiers();
		} catch (Throwable t) {
			/*
			 * There is some driver problem here!
			 */
			return null;
		}

		if (portList == null) {
			return null;
		}

		final List<String> portNames = new ArrayList<String>();
		while (portList.hasMoreElements()) {
			final CommPortIdentifier port = (CommPortIdentifier) portList
					.nextElement();
			if (port.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				portNames.add(port.getName());
			}
		}

		return portNames.toArray(new String[0]);
	}

	/**
	 * Obtains the current port name.
	 * 
	 * @return the port name, or <code>null</code> if not set
	 */
	public String getPortName() {
		if (portConfigurations == null)
			return serialPortName;
		IProperty portNameProp = portConfigurations
				.findPropertyByName(PORT_NAME_PROP_NAME);
		final String portName = (String) portNameProp.getValue();
		return portName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * lumina.extensions.transport.rs232.AbstractTransportDriver#getPropertySet
	 * ()
	 */
	@Override
	public IPropertySet getPropertySet() {
		return portConfigurations;
	}

	/**
	 * Closes the serial port.
	 * <p>
	 * Besides closing the serial port it checks if the close event is due to a
	 * sudden removal of the USB port. If the USB port has been, calls
	 * {@link #notifyPortVanished()}. The best place to check for the port
	 * presence is the close operation, since it was last checked during the
	 * open operation.
	 * <p>
	 * Assumes exclusive access to the {@link #serialPort} object and thread
	 * safety.
	 * 
	 * @return true, if successful
	 */
	private boolean internalCleanupClose() {
		disableCableConnectionMonitor();
		clearError();

		/*
		 * /!\ WARNING: calling these methods from the method from the RXTX
		 * thread will hang the the RXTX layer.
		 * 
		 * Moreover, the serialPort.close() method will block if another
		 * operation on the driver is pending, for example a send operation.
		 */

		if (serialPort != null) {
			boolean closeSuccessfull = true;
			try {
				timeoutOutputStream.close();
				closeSuccessfull = timeoutOutputStream.isClosed();

			} catch (IOException e) {
				closeSuccessfull = false;
			}
			serialPort.removeEventListener();

			if (closeSuccessfull) {
				serialPort.close();
				System.err.println("Port CLOSED:" + getPortName() + ">");
			} else {
				setError(
						Messages.getString("RS232Driver.portNotClosed.problem"), //$NON-NLS-1$
						Messages.getString("RS232Driver.portNotClosed.resolution")); //$NON-NLS-1$

				notifyIOError();

				System.err.println("Port *NOT* CLOSED:" + getPortName() + ">");
			}

			final SerialPort portToTest = serialPort;

			if (!portStillExists(portToTest)) {
				notifyPortVanished();
			}

			return closeSuccessfull;
		}

		/*
		 * We closed everything we could, so we assume we really closed.
		 */
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * lumina.network.physical.AbstractCommunicationDriver#isDriverPresent()
	 */
	/**
	 * Checks if is driver lib present.
	 * 
	 * @return true, if is driver lib present
	 */
	private boolean isDriverLibPresent() {
		try {
			/*
			 * this can be the first time we interact with the driver
			 */
			CommPortIdentifier.getPortIdentifiers();
			return true;
		} catch (Throwable t) {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lumina.network.physical.CommunicationDriver#receive()
	 * 
	 * NOTE: After registering a network event listener the messages will be
	 * immediately delivered to the listeners and therefore unavailable to the
	 * {@link #receive()} method, which, henceforth will always return
	 * <code>null</code>.
	 */
	// public synchronized byte[] receive() {
	// try {
	// if (outstandingReceivedMessages == null) {
	// return null;
	// }
	//
	// return new byte[] { outstandingReceivedMessages.poll(driverTimeout,
	// TimeUnit.MILLISECONDS).byteValue() };
	// } catch (InterruptedException e) {
	// return null;
	// }
	// }

	/**
	 * Checks if the communication port is still open.
	 * 
	 * @return <code>true</code> if the communication port has been successfully
	 *         open and has not been close; returns <code>false</code> if the
	 *         port is closed or an error has occurred during open.
	 */
	public synchronized boolean isPortOpen() {
		return portIsOpen;
	}

	/**
	 * Checks if this port with the specified port name exists.
	 * 
	 * @return <code>true</code> if an RS232 port with the name of this port
	 *         exists; returns <code>false</code> otherwise.
	 */
	@Override
	public boolean isPortPresent() {
		return portStillExists(getPortName());
	}

	/**
	 * Method that opens a communication connection.
	 * 
	 * @return true, if successful
	 */
	protected synchronized boolean openDriver() {
		portIsOpen = false;

		if (!isDriverLibPresent()) {
			/*
			 * Oops trouble contacting the driver (?)
			 * 
			 * We go against the general rule of letting errors be propagated.
			 * Lets still try to recover here.
			 */
			setError(Messages.getString("RS232Driver.driverNotFound.problem"), //$NON-NLS-1$
					Messages.getString("RS232Driver.driverNotFound.resolution")); //$NON-NLS-1$

			notifyIOError();
			return false;
		}

		if (getPortName() == null) {
			throw new IllegalStateException("open() called but port not set");
		}

		if (applicationName == null) {
			throw new IllegalStateException(
					"open() called but application name not set");
		}

		final CommPortIdentifier portId = getPortId(getPortName());

		if (portId != null) {
			/*
			 * Guarantee exclusive access to the serial port object
			 */
			synchronized (serialPortLock) {
				/*
				 * initialize serial port
				 */
				try {
					serialPort = (SerialPort) portId.open(applicationName,
							driverTimeout); //$NON-NLS-1$
					ioMonitor.openSuccessfull();
				} catch (final PortInUseException e) {
					setError(
							Messages.getString("RS232Driver.portInUse.problem", //$NON-NLS-1$
									portId.getName()),
							Messages.getString(
									"RS232Driver.portInUse.resolution", portId.getName())); //$NON-NLS-1$

					notifyIOError();

					Logger.getInstance().log(LogService.LOG_INFO,
							"Serial port '" + serialPortName + "' is in use.",
							e);
					return false;
				}

				try {
					/*
					 * Don't wrap the input stream. Data arrival is event
					 * driven. We only read after the driver tells us to.
					 */
					rxtxInputStream = serialPort.getInputStream();

					/*
					 * get the input stream and wrap it to get timeout
					 * exceptions
					 */
					timeoutOutputStream = new TimeoutOutputStream(
							serialPort.getOutputStream(), driverTimeout,
							TimeUnit.MILLISECONDS);

					timeoutOutputStream.open();
				} catch (IOException e) {
					setError(
							Messages.getString("RS232Driver.IOError.problem"), //$NON-NLS-1$
							Messages.getString("RS232Driver.IOError.resolution")); //$NON-NLS-1$

					notifyIOError();

					Logger.getInstance().log(LogService.LOG_ERROR,
							"Get Input Stream: " + e.toString());
					return false;
				}

				/*
				 * Register this object to receive the serial port events
				 */
				try {
					serialPort.addEventListener(serialEventListener);
				} catch (final TooManyListenersException e) {
					setError(
							Messages.getString(
									"RS232Driver.couldNotSetCommunicationParameter.problem", //$NON-NLS-1$
									portId.getName()),
							Messages.getString(
									"RS232Driver.couldNotSetCommunicationParameter.resolution", //$NON-NLS-1$
									portId.getName()));

					Logger.getInstance().log(Logger.LOG_INFO,
							"Adding the Event Listner: " + e.toString());
					return false;
				}

				/*
				 * Activate the DATA_AVAILABLE notifier
				 */
				serialPort.notifyOnDataAvailable(true);

				/*
				 * Activate other notifications
				 */
				serialPort.notifyOnBreakInterrupt(true);
				serialPort.notifyOnCarrierDetect(true);
				serialPort.notifyOnCTS(true);
				serialPort.notifyOnDSR(true);
				// TODO
				serialPort.notifyOnFramingError(false);
				serialPort.notifyOnOverrunError(true);
				serialPort.notifyOnParityError(true);
				serialPort.notifyOnRingIndicator(true);

				if (LOG_SERIAL_SIGNALS) {
					Logger.getInstance().log(Logger.LOG_INFO,
							"CD: " + serialPort.isCD());
					Logger.getInstance().log(Logger.LOG_INFO,
							"CTS: " + serialPort.isCTS());
					Logger.getInstance().log(Logger.LOG_INFO,
							"DSR: " + serialPort.isDSR());
					Logger.getInstance().log(Logger.LOG_INFO,
							"DTR: " + serialPort.isDTR());
					Logger.getInstance().log(Logger.LOG_INFO,
							"RI: " + serialPort.isRI());
					Logger.getInstance().log(Logger.LOG_INFO,
							"RTS: " + serialPort.isRTS());
				}
				try {
					serialPort.setSerialPortParams(SerialPortSettings.baudRate,
							SerialPortSettings.dataBits,
							SerialPortSettings.stopBits,
							SerialPortSettings.parity);

					serialPort.setFlowControlMode(serialPort
							.getFlowControlMode());
					serialPort.setDTR(SerialPortSettings.dtr);
					serialPort.setRTS(SerialPortSettings.rts);

					/*
					 * Setting DTR to true and then to false resets the PCNode.
					 * 
					 * TODO: Move this code out of here into a resetPcNode
					 * method when we have a PCNode interface driver.
					 */
					serialPort.setDTR(true);

					/*
					 * DTR must be false in order for the 9- and 3- pin cable to
					 * work properly
					 */
					serialPort.setDTR(false);
				} catch (UnsupportedCommOperationException e) {
					setError(
							Messages.getString(
									"RS232Driver.couldNotSetCommunicationParameter.problem", //$NON-NLS-1$
									portId.getName()),
							Messages.getString(
									"RS232Driver.couldNotSetCommunicationParameter.resolution", //$NON-NLS-1$
									portId.getName()));
					notifyIOError();

					return false;
				}

			}

			portIsOpen = true;

			if (messageArrivalNotifier == null) {
				messageArrivalNotifier = new MessageArrivalNotifier();
				messageArrivalNotifier.start();
			}
		}

		if (portId == null) {
			setError(
					Messages.getString(
							"RS232Driver.portNotFound.problem", getPortName()), //$NON-NLS-1$
					Messages.getString(
							"RS232Driver.portNotFound.resolution", getPortName())); //$NON-NLS-1$
			notifyIOError();
			return false;
		}

		return true;
	}

	/**
	 * Task used to monitor the cable connection.
	 * <p>
	 * After an interval defined by CABLE_CONNECT_TIMEOUT_MILLIS without
	 * receiving a {@link SerialPortEvent#DSR} event, this task calls
	 * {@link #notifyCableDisconnected()}.
	 */
	private void scheduleCableConnectMonitorTask() {
		final TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				synchronized (lastConnectNotificationLock) {
					try {
						final long t = System.currentTimeMillis();
						if (t - lastConnectNotificationTimeStamp > CABLE_CONNECT_TIMEOUT_MILLIS) {
							if (!notifiedCableDisconnected) {
								notifyCableDisconnected();

								notifiedCableDisconnected = true;
								notifiedCableConnected = false;
							}
						}
					} catch (Exception e) {
						Logger.getInstance().log(Logger.LOG_INFO,
								"Error in cable monitor check", e); //$NON-NLS-1$
					}
				}
			}
		};

		cableConnectMonitor.scheduleAtFixedRate(timerTask, 0,
				CABLE_CONNECT_TIMEOUT_MILLIS);
	}

	/**
	 * Sets the application name that this port will publish when open
	 * <p>
	 * Can only be called if the driver is not open.
	 * 
	 * @param appName
	 *            the new application name. Cannot be <code>null</code>.
	 */
	public synchronized void setApplicationName(String appName) {
		if (isOpen()) {
			throw new IllegalStateException(
					"setApplicationName() cannot be called while the driver is connected"); // $NON-NLS-1$
		} else {
			applicationName = appName;
		}
	}

	/**
	 * Sets the port name.
	 * <p>
	 * Can only be called if the driver is not open.
	 * 
	 * @param port
	 *            the new port name.
	 */
	public synchronized void setPort(final String port) {
		if (isOpen()) {
			throw new IllegalStateException(
					"setPort() cannot be called while the driver is connected"); // $NON-NLS-1$
		} else {
			serialPortName = port;
		}
	}
}
