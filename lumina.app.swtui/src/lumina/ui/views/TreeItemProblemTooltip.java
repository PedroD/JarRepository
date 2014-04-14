package lumina.ui.views;

import lumina.base.model.ModelItem;
import lumina.base.model.Project;
import lumina.base.model.Queries;
import lumina.base.model.validators.ValidationProblem;
import lumina.base.model.validators.ValidatorManager;

/**
 * Displays a tooltip with the problems of the node.
 */
public class TreeItemProblemTooltip extends AbstractTreeItemTooltip {

	/**
	 * Returns the problem text.
	 * 
	 * @param problem
	 *            validation problem to get text from
	 * @return text problem
	 */
	private static String getProblemText(final ValidationProblem problem) {
		final StringBuffer sb = new StringBuffer();
		if (problem.getDescription() != null) {
			sb.append(problem.getDescription());
		}

		if (problem.getProblemDetails() != null) {
			if (sb.length() != 0) {
				sb.append("\n");
			}

			sb.append(problem.getProblemDetails());
		}

		if (problem.getResolution() != null) {
			if (sb.length() != 0) {
				sb.append("\n");
			}

			sb.append(problem.getResolution());
		}

		return sb.toString();
	}

	/**
	 * Obtains the tooltip text of an item.
	 * 
	 * @param data
	 *            the tree item data
	 * @return a string with the tooltip text to be displayed or null if the
	 *         tooltip should not be shown.
	 */
	@Override
	protected String getTooltipText(final Object data) {
		if (data != null && data instanceof ModelItem) {
			final ModelItem item = (ModelItem) data;
			final Project project = Queries.getAncestorProject(item);
			final ValidatorManager vm = project.getValidatorManager();

			if (vm.hasProblems(item)) {
				ValidationProblem problem = vm.getMostImportantProblemFor(item);

				return getProblemText(problem);
			}
		}

		return null;
	}
}
