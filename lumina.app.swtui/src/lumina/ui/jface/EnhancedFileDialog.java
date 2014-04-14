package lumina.ui.jface;

import java.io.File;

import lumina.Preferences;
import lumina.ui.swt.SimpleDialogs;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * A file dialog that remembers the last directory.
 * <p>
 * This class adds the following functionality to the SWT FileDialog:
 * <ol>
 * <li>Remember last selected directory</li>
 * <li>"Overwrite File" prompt if the user selects a file that already exists
 * (save dialogs)</li>
 * </ol>
 * Both these features are turned on by default; they can be turned off by
 * setting the corresponding properties to <code>false</code>. The persistent
 * setting (last selected directory) is keyed off the dialog's title by default.
 * To use a different key call setKey() with a non null value. The setting is
 * kept in the preferences, so it persists between restarts of the application.
 */
public class EnhancedFileDialog extends FileDialog {

	/**
	 * The Constant OVERWRITE_FILE_TITLE.
	 */
	private static final String OVERWRITE_FILE_TITLE = Messages
			.getString("EnhancedFileDialog.Overwrite.title"); //$NON-NLS-N$

	/**
	 * The Constant OVERWRITE_FILE_MESSAGE_KEY.
	 */
	private static final String OVERWRITE_FILE_MESSAGE_KEY = "EnhancedFileDialog.Overwrite.message"; //$NON-NLS-N$

	/**
	 * Preference key.
	 */
	private static final String PREF_LAST_DIR = "last_dir"; //$NON-NLS-N$

	/**
	 * The remember location.
	 */
	protected boolean rememberLocation = true;

	/**
	 * The prompt overwrite.
	 */
	protected boolean promptOverwrite = true;

	/**
	 * The key.
	 */
	protected String key;

	/**
	 * Set to <code>true</code> if a open location was explicitly set by
	 * calling. {@link #setFilterPath(String)}.
	 */
	private boolean userSetLocation;

	/**
	 * Instantiates a new enhanced file dialog.
	 * 
	 * @param parent
	 *            the parent
	 * @param style
	 *            the style
	 */
	public EnhancedFileDialog(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Instantiates a new enhanced file dialog.
	 * 
	 * @param parent
	 *            the parent
	 */
	public EnhancedFileDialog(Shell parent) {
		super(parent);
	}

	/**
	 * Gets the remember location flag.
	 * 
	 * @return the remember location flag value
	 */
	public final boolean getRememberLocation() {
		return rememberLocation;
	}

	/**
	 * Sets the remember location flag.
	 * 
	 * @param value
	 *            the new remember location
	 */
	public final void setRememberLocation(boolean value) {
		this.rememberLocation = value;
	}

	/**
	 * Gets the prompt on overwrite.
	 * 
	 * @return the prompt overwrite value
	 */
	public final boolean getPromptOverwrite() {
		return promptOverwrite;
	}

	/**
	 * Sets the prompt on overwrite flag.
	 * 
	 * @param value
	 *            the new prompt overwrite: should be <code>true</code> if the
	 *            user should be prompted on overwrite; <code>false</code>
	 *            otherwise.
	 */
	public final void setPromptOverwrite(boolean value) {
		this.promptOverwrite = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Dialog#checkSubclass()
	 */
	@Override
	protected void checkSubclass() {
		// FileDialog calls this to enforce a strange subclassing policy.
		// Fortunately the method is not final, so we neutralize it by providing
		// an empty implementation
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.FileDialog#setFilterPath(java.lang.String)
	 */
	@Override
	public final void setFilterPath(String string) {
		super.setFilterPath(string);
		userSetLocation = true;
	}

	/**
	 * Opens the dialog box asking the user for the due confirmations.
	 * 
	 * @return a string describing the absolute path of the first selected file,
	 *         or null if the dialog was cancelled or an error occurred
	 * @see org.eclipse.swt.widgets.FileDialog#open()
	 */
	@Override
	public final String open() {
		// get the preferences node that will have the settings for this dialog
		final IEclipsePreferences prefs;
		if (key != null) {
			prefs = Preferences.getNode(key.toLowerCase());
		} else {
			prefs = Preferences.getNode(this.getText().toLowerCase());
		}

		/*
		 * If rememberLocation is true and the user hasn't set an explicit path,
		 * try to use the last used path
		 */
		final boolean shoudUseLastUsedPath = rememberLocation
				&& !userSetLocation;
		if (shoudUseLastUsedPath) {
			final String lastDir = prefs.get(PREF_LAST_DIR, null);
			if (lastDir != null) {
				final File path = new File(lastDir);
				if (path.exists() && path.isDirectory()) {
					this.setFilterPath(lastDir);
				}
			}
		}

		String result = super.open();

		/*
		 * If promptOverwrite is true and this is a save dialog, then check if
		 * the selected file exists; if so, show the overwrite dialog*
		 */
		final boolean shouldShowOverwriteDialog = promptOverwrite
				&& result != null && (this.getStyle() & SWT.SAVE) != 0;

		if (shouldShowOverwriteDialog) {
			final File path = new File(result);
			if (path.exists()) {
				if (!SimpleDialogs.showQuestion(OVERWRITE_FILE_TITLE, Messages
						.getString(OVERWRITE_FILE_MESSAGE_KEY,
								new Object[] { path.getName() }), true)) {
					result = null;
				}
			}
		}

		// Save the location
		if (rememberLocation && result != null) {
			final File path = new File(result);
			prefs.put(PREF_LAST_DIR, path.getParent());
		}

		return result;
	}
}
