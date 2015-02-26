package expcalj.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A class that is designed to read standard terminal input, which supports ANSI
 * escape sequences.
 * 
 * @author The Binary Fox
 */
public class STDINReader {

	static private STDINReader reader;

	/**
	 * Get the STDIN reader.
	 * 
	 * @return
	 */
	static public STDINReader get() {
		if (reader == null) {
			reader = new STDINReader();
			reader.open();
		}

		return reader;
	}

	private STDINReader() {
	}

	private Runnable onInterrupt;
	private boolean raw_enabled;
	private BufferedReader cooked_reader;

	/**
	 * Open the STDIN reader. This doesn't really open anything, but rather just
	 * sets the TTY options.
	 */
	protected void open() {
		boolean r = raw();
		boolean r2 = cook();

		if (!r || !r2) {
			try {
				cooked_reader = new BufferedReader(new InputStreamReader(System.in));
			} catch (Exception ex2) {
			}
		}
	}

	/**
	 * Close the STDIN reader.
	 */
	public void close() {
		cook();
	}

	protected boolean raw() {
		try {
			if (!raw_enabled) {
				Process proc = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", "stty -echo raw </dev/tty" });
				if (proc.waitFor() == 0) {
					raw_enabled = true;
					return true;
				}

				return false;
			}

			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	protected boolean cook() {
		try {
			if (raw_enabled) {
				Process proc = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", "stty echo cooked </dev/tty" });
				if (proc.waitFor() == 0) {
					raw_enabled = false;
					return true;
				}

				return false;
			}

			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Check to see if raw reading and ANSI is supported.
	 * 
	 * @return true or false.
	 */
	public boolean isSupportedANSI() {
		return raw_enabled;
	}

	static private final int CHAR_ESC = 27;
	static private final int CHAR_BSP = 127;
	static private final int CHAR_RET = '\r';
	static private final int CHAR_NLN = '\n';

	protected char[] buffer;
	protected int pointer;
	protected int bufused;

	protected String[] history = new String[10];
	protected char[] history_temp;
	protected int history_pointer;
	protected int history_bufused;

	/**
	 * Clear the input line.
	 */
	protected void drawClear() {
		for (int i = pointer; i > 0; i--) {
			System.out.print('\b');
		}

		for (int i = bufused; i > 0; i--) {
			System.out.print(' ');
		}

		for (int i = bufused; i > 0; i--) {
			System.out.print('\b');
		}
	}

	/**
	 * Draw the input line.
	 */
	protected void drawInput() {
		for (int i = 0; i < bufused; i++) {
			System.out.print(buffer[i]);
		}
	}

	/**
	 * Allocate the buffer.
	 * 
	 * @param size
	 */
	protected void bufferAlloc(int size) {
		buffer = new char[size];
		pointer = 0;
		bufused = 0;
	}

	/**
	 * Change the buffer capacity.
	 * 
	 * @param size
	 */
	protected void bufferCapacity(int size) {
		if (buffer == null)
			buffer = new char[size];

		char[] nbuffer = new char[size];
		System.arraycopy(buffer, 0, nbuffer, 0, buffer.length);
		buffer = nbuffer;
	}

	/**
	 * Insert a character to the buffer and console.
	 * 
	 * @param c
	 *            the character.
	 */
	protected void bufferInsert(char c) {
		// Insert
		if (bufused == buffer.length) {
			bufferCapacity(buffer.length * 2);
		}

		// Append
		if (pointer == bufused) {
			buffer[pointer] = c;
			pointer++;
			bufused++;
			System.out.print(c);
			return;
		}

		System.arraycopy(buffer, pointer, buffer, pointer + 1, bufused - pointer);
		buffer[pointer] = c;
		pointer++;
		bufused++;

		// Insert - Print
		int i = pointer - 1;
		for (; i < bufused; i++) {
			System.out.print(buffer[i]);
		}
		for (; i > pointer; i--) {
			System.out.print('\b');
		}
	}

	/**
	 * Handle a backspace to the buffer and console.
	 */
	protected void bufferBackspace() {
		if (pointer == 0)
			return;

		if (pointer == bufused) {
			pointer--;
			bufused--;
			System.out.print(" \b\b \b");
			return;
		}

		// Remove from middle.
		System.arraycopy(buffer, pointer, buffer, pointer - 1, bufused - pointer);
		pointer--;
		bufused--;

		// Remove - Print
		int i = pointer;
		System.out.print('\b');
		for (; i <= bufused; i++) {
			if (i == bufused)
				System.out.print(' ');
			else
				System.out.print(buffer[i]);
		}
		for (; i > pointer; i--) {
			System.out.print('\b');
		}
	}

	/**
	 * Read an ANSI sequence.
	 * 
	 * @throws IOException
	 */
	protected void handleANSI() throws IOException {
		int step = 0;
		for (int i = 1; i > 0; i--) {
			step++;
			char read = (char) System.in.read();

			if (step == 1) {
				switch (read) {
				case '[':
					i++;
					break;
				}

				continue;
			}

			if (step == 2) {
				switch (read) {
				case 'A':
					if (history_pointer + 1 >= history.length || history[history_pointer + 1] == null)
						return;

					if (history_pointer == -1) {
						history_bufused = bufused;
					}

					history_pointer++;
					drawClear();
					buffer = history[history_pointer].toCharArray();
					bufused = pointer = buffer.length;
					drawInput();
					break;
				case 'B':
					if (history_pointer < 0)
						return;

					history_pointer--;
					if (history_pointer == -1) {
						drawClear();
						buffer = history_temp;
						bufused = pointer = history_bufused;
						drawInput();
					} else {
						drawClear();
						buffer = history[history_pointer].toCharArray();
						bufused = pointer = buffer.length;
						drawInput();
					}
					break;
				case 'C':
					if (pointer >= bufused)
						break;

					pointer++;
					System.out.print((char) CHAR_ESC + "[C");
					break;
				case 'D':
					if (pointer < 1)
						break;

					pointer--;
					System.out.print((char) CHAR_ESC + "[D");
					break;
				}
				continue;
			}
		}
	}

	/**
	 * Set the CTRL+C interrupt handler.
	 * 
	 * @param r
	 *            the interrupt handler runnable.
	 */
	public void setInterruptHandler(Runnable r) {
		onInterrupt = r;
	}

	/**
	 * Read a line.
	 * 
	 * @return the line read.
	 */
	public String readLine() throws IOException {
		if (cooked_reader != null) {
			return cooked_reader.readLine();
		}

		// Read raw.
		history_temp = buffer;
		history_pointer = -1;

		raw();
		bufferAlloc(1024);
		while (true) {
			char read = (char) System.in.read();

			// Interrupt
			if (read == 3) {
				if (onInterrupt != null) {
					onInterrupt.run();
					cook();
					return new String(buffer, 0, bufused);
				}

				close();
				System.exit(0);
			}

			// Escape
			if (read == CHAR_ESC) {
				handleANSI();
				continue;
			}

			// Backspace
			if (read == CHAR_BSP) {
				bufferBackspace();
				continue;
			}

			// New line
			if (read == CHAR_RET || read == CHAR_NLN) {
				break;
			}

			// Insert
			bufferInsert(read);
		}

		cook();
		System.out.println();
		String str = new String(buffer, 0, bufused);

		// History
		System.arraycopy(history, 0, history, 1, history.length - 1);
		history[0] = str;

		// Return
		return str;
	}

}
