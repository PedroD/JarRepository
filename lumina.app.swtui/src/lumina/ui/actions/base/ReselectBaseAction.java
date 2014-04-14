package lumina.ui.actions.base;

import lumina.ui.jface.SelectionUtils;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * Base class for an action that can change the currently selected items.
 * <p>
 * This action keeps tack of the {@link ISelectionProvider}.
 * <p>
 * This class offers the {@link #reselect(ISelection)} method that when called
 * will cause all other actions to update.
 * <p>
 * The effect of this is to allow other actions, like for example 'paste' to
 * update their availability. If this action is not executed CTRL-C followed by
 * CTRL-V will not work.
 * 
 * @author Paulo Carreira
 */
public abstract class ReselectBaseAction extends Action {

	/**
	 * Holds the selection provider or null.
	 */
	private ISelectionProvider selectionProvider;

	/**
	 * Creates a new action with no assigned selection provider.
	 * <p>
	 * The selection provider must be given by calling
	 * {@link #setSelectionProvider(ISelectionProvider)}.
	 */
	protected ReselectBaseAction() {
		super();
	}

	/**
	 * Creates a new action that knows the given selection provider.
	 * 
	 * @param provider
	 *            the selection provider object
	 */
	protected ReselectBaseAction(final ISelectionProvider provider) {
		super();

		selectionProvider = provider;
	}

	/**
	 * Creates a new action that knows the given selection provider.
	 * 
	 * @param provider
	 *            the selection provider object
	 * @param text
	 *            the text of the action
	 */
	protected ReselectBaseAction(final ISelectionProvider provider,
			final String text) {
		super(text);

		selectionProvider = provider;
	}

	/**
	 * Sets the selection provider.
	 * 
	 * @param provider
	 *            the selection provider.
	 * @throws IllegalStateException
	 *             if the provider has been set
	 */
	protected final void setSelectionProvider(final ISelectionProvider provider) {
		if (!hasSelectionProvider()) {
			selectionProvider = provider;
		} else {
			throw new IllegalStateException();
		}
	}

	/**
	 * Checks whether the selection provider has been assigned.
	 * 
	 * @return <code>true</code> if the selection provider is not
	 *         <code>null</code>; returns <code>false</code> othersise.
	 */
	protected final boolean hasSelectionProvider() {
		return selectionProvider != null;
	}

	/**
	 * Reselects the items selected in the selection provider.
	 * <p>
	 * 
	 * @param selection
	 *            the selection to be set
	 */
	protected final void reselect(Object[] selection) {
		SelectionUtils.doSelectItems(selection, selectionProvider);
	}

	/**
	 * Reselects the items selected in the selection provider.
	 * <p>
	 * 
	 * @param selection
	 *            the selection to be set
	 */
	protected final void reselect(ISelection selection) {
		SelectionUtils.doSelectItems(selection, selectionProvider);
	}
}
