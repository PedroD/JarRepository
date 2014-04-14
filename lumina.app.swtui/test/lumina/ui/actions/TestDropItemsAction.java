package lumina.ui.actions;

import junit.framework.TestCase;
import lumina.base.model.Area;
import lumina.base.model.Floor;
import lumina.base.model.ModelItem;
import lumina.base.model.ProjectModel;
import lumina.network.sandbox.TesterNullDevice;

/**
 * Test the methods related to allowing drag and drop.
 */
public class TestDropItemsAction extends TestCase {

	/**
	 * Tests that paste can happen when it should.
	 */
	public void testAcceptsPaste() {
		assertTrue(DropItemsAction.acceptsPaste(
				new ModelItem[] { new TesterNullDevice() }, new Area("", "")));

		assertTrue(DropItemsAction.acceptsPaste(
				new ModelItem[] { new TesterNullDevice() },
				new TesterNullDevice()));
	}

	/**
	 * Tests that paste cannot happen in the wrong places.
	 */
	public void testAcceptsPasteSafe() {
		assertFalse(DropItemsAction.acceptsPaste(
				new ModelItem[] { new TesterNullDevice() }, new Floor("", "")));
		assertFalse(DropItemsAction.acceptsPaste(
				new ModelItem[] { new TesterNullDevice() }, ProjectModel
						.getInstance().getProject()));
	}

	/**
	 * Tests that paste can happen when it should.
	 */
	public void testCanPastOrDropSafe() {
		assertTrue(DropItemsAction.canPasteOrDrop(
				new ModelItem[] { new TesterNullDevice() }, new Area("", "")));

		assertTrue(DropItemsAction.canPasteOrDrop(
				new ModelItem[] { new TesterNullDevice() },
				new TesterNullDevice()));
	}

	/**
	 * Tests that paste cannot happen in the wrong places.
	 */
	public void testCanPasteOrDrop() {
		assertFalse(DropItemsAction.canPasteOrDrop(
				new ModelItem[] { new TesterNullDevice() }, new Floor("", "")));
		assertFalse(DropItemsAction.canPasteOrDrop(
				new ModelItem[] { new TesterNullDevice() }, ProjectModel
						.getInstance().getProject()));
	}
}
