package lumina.ui.views;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * Base class for views.
 * <p>
 * Manages the lazy creation of clipboard object for the view and the saving of
 * the {@link IMemento} object.
 */
public abstract class AbstractViewPart extends ViewPart {

	/**
	 * Memento object used to set and get the preferences of the view. Used for
	 * querying preferences when creating the controls. Can be <code>null</code>
	 * if the preference store is deleted.
	 */
	private IMemento viewPreferenceMemento;

	/**
	 * Initializes the view part.
	 * 
	 * @param site
	 *            view site
	 * @param memento
	 *            the memento with view preferences
	 * @throws PartInitException
	 *             for compatibility
	 */
	@Override
	public void init(final IViewSite site, final IMemento memento)
			throws PartInitException {
		super.init(site, memento);
		viewPreferenceMemento = memento;
	}

	/**
	 * Gets the memento object used to initialize the view.
	 * 
	 * @return the memento object used to initialize the view.
	 */
	protected IMemento getPreferenceMemento() {
		return viewPreferenceMemento;
	}
}
