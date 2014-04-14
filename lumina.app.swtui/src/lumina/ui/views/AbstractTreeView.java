package lumina.ui.views;

import lumina.base.model.Device;
import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.base.model.Project;
import lumina.base.model.ProjectModel;
import lumina.base.model.PropertyChangeNames;
import lumina.base.model.Queries;
import lumina.base.model.validators.ValidatorManager;
import lumina.qp.AggregateResult;
import lumina.qp.Sink;
import lumina.qp.Source;
import lumina.qp.TableSink;
import lumina.ui.actions.DeselectAllActionHandler;
import lumina.ui.actions.SelectAllActionHandler;
import lumina.ui.actions.retarget.DeselectAllRetargetAction;
import lumina.ui.actions.retarget.RenameRetargetAction;
import lumina.ui.actions.retarget.SelectAllRetargetAction;
import lumina.ui.jface.SelectionUtils;
import lumina.ui.jface.TreeViewUtils;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;

/**
 * Abstract tree view.
 * <p>
 * Provides abstract class for the tree view
 * <p>
 * <b>Note:</b> call open()/close() when the project is (re)loaded. This is necessary to
 * guarantee that the resources are freed. otherwise the table sink will grow very large.
 */
public abstract class AbstractTreeView extends
        AbstractViewPart implements ISelectionListener, DetailsProviderView {

    /**
     * The tag that corresponds to the status of the info toggle to be saved.
     */
    protected static final String TAG_TOGGLE_INFO_ENABLED = "toggleInfoEnabled";

    /**
     * The default with of the leftmost column.
     */
    private static final int DEFAULT_LEFTMOST_COLUMN_WIDTH = 180;

    /**
     * View content provider.
     */
    protected final IContentProvider viewContentProvider;
    /**
     * View label provider.
     */
    protected AbstractTreeViewLabelProvider viewLabelProvider;

    /**
     * Tree treeViewer.
     */
    private TreeViewer treeViewer;

    /**
     * The tooltip listener.
     */
    private TreeItemProblemTooltip tooltipListener;

    /**
     * The item edit listener.
     */
    private TreeItemInlineEditListener inlineEditListerner;

    /**
     * The info toggle button that appears in the upper right corner of the view.
     */
    private CommandContributionItem infoToggleButton;

    /**
     * Context menu.
     */
    private Menu contextMenu;

    /**
     * Context menu manager.
     */
    private MenuManager menuMgr;

    /**
     * The default column for the tree.
     */
    private TreeColumn modelTreeColumn;

    /**
     * The remaining columns that hold the extra information.
     */
    private TreeColumn[] infoPropertyColumns;

    /**
     * A reference to the item processor sink where changes to model items should be
     * reported.
     */
    private Sink<ModelItem> modelItemProcessor;

    /**
     * Constructs a tree view based on the supplied content provider and label provider.
     * 
     * @param contentProvider the content provider.
     * @param labelProvider the label provider.
     */
    public AbstractTreeView(final IContentProvider contentProvider,
                            final AbstractTreeViewLabelProvider labelProvider) {
        viewContentProvider = contentProvider;
        viewLabelProvider = labelProvider;

        modelItemProcessor = getItemSink();
        if (modelItemProcessor != null) {
            final Source<AggregateResult> queryResult = getAggregateResultSource();
            final TableSink<AggregateResult> resultTable = getResultTable();
            queryResult.addSink(resultTable);
            viewLabelProvider.setQueryResult(resultTable);
        } else {
            viewLabelProvider.setQueryResult(null);
        }
    }

    private ModelItem[] getAddedItems(final PropertyChangeEvent event) {
        assert PropertyChangeNames.isNodeAdd(event.getProperty());

        if (event.getNewValue() instanceof ModelItem[]) {
            return (ModelItem[]) event.getNewValue();
        } else if (event.getNewValue() instanceof ModelItem) {
            return new ModelItem[] { (ModelItem) event.getNewValue() };
        } else {
            return new ModelItem[] {};
        }
    }

    private ModelItem[] getRemovedItems(final PropertyChangeEvent event) {
        assert PropertyChangeNames.isNodeRemove(event.getProperty());

        if (event.getOldValue() instanceof ModelItem[]) {
            return (ModelItem[]) event.getOldValue();
        } else if (event.getOldValue() instanceof ModelItem) {
            return new ModelItem[] { (ModelItem) event.getOldValue() };
        } else {
            return new ModelItem[] {};
        }
    }

    /**
     * Creates the actions for the navigation view's toolbar.
     */
    private void createActionsInternal() {
        /*
         * Undo + redo
         */
        final IAction undoAction = new UndoActionHandler(getSite(),
                IOperationHistory.GLOBAL_UNDO_CONTEXT);

        final IAction redoAction = new RedoActionHandler(getSite(),
                IOperationHistory.GLOBAL_UNDO_CONTEXT);

        getViewSite().getActionBars()
                .setGlobalActionHandler(ActionFactory.UNDO.getId(), undoAction);
        getViewSite().getActionBars()
                .setGlobalActionHandler(ActionFactory.REDO.getId(), redoAction);

        /*
         * Copy
         */
        // final IAction copyAction = new CopyItemsToClipboardAction(treeViewer,
        // ClipboardUtils.getClipboard(), getWorkbenchWindow());
        //
        // getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(),
        // copyAction);
        /*
         * Cut
         */
        // final IAction cutAction = new CutItemsToClipboardAction(treeViewer,
        // ClipboardUtils.getClipboard(), getWorkbenchWindow());
        //
        // getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.CUT.getId(),
        // cutAction);
        /*
         * Paste
         */
        //
        // getHandlerService().activateHandler("org.eclipse.ui.edit.paste",
        // new PasteItemsFromClipboardHandler());
        //
        // final IAction pasteAction = new
        // PasteItemsFromClipboardAction(ClipboardUtils
        // .getClipboard(), getWorkbenchWindow());
        //
        // getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.PASTE.getId(),
        // pasteAction);
        /*
         * Delete item
         */
        final IAction deleteHandler = getDeleteHandler();

        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.DELETE.getId(),
                deleteHandler);

        /*
         * Hooks the delete key
         */
        treeViewer.getTree().addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.character == SWT.DEL) {
                    if (deleteHandler != null) {
                        deleteHandler.run();
                    }
                }
            }

            public void keyReleased(KeyEvent e) {
            }
        });

        /*
         * Select and deselect
         */
        final SelectAllActionHandler selectAllItemHandler = new SelectAllActionHandler(treeViewer,
                getWorkbenchWindow());

        final DeselectAllActionHandler deselectAllItemHandler = new DeselectAllActionHandler(
                selectAllItemHandler, getWorkbenchWindow());

        getViewSite().getActionBars().setGlobalActionHandler(SelectAllRetargetAction.ID,
                selectAllItemHandler);

        getViewSite().getActionBars().setGlobalActionHandler(DeselectAllRetargetAction.ID,
                deselectAllItemHandler);

        /*
         * Rename
         */
        final IAction renameActionHandler = new RenameTreeItemActionHandler(treeViewer,
                getWorkbenchWindow());

        getViewSite().getActionBars().setGlobalActionHandler(RenameRetargetAction.ID,
                renameActionHandler);

        /*
         * Hook click + click
         */
        setInlineEditListener(new TreeItemInlineEditListener(getWorkbenchWindow()));

        /*
         * Hook the double-click listener
         */
        getTreeViewer().getTree().addListener(SWT.MouseDoubleClick, new Listener() {
            public void handleEvent(final Event event) {
                if (event.type == SWT.MouseDoubleClick) {
                    final ISelection selection = treeViewer.getSelection();
                    handleDoubleClick(selection);
                }
            }
        });
    }

    /**
     * Create the contribution item for the info toggle button.
     * 
     * @return a {@link CommandContributionItem} object; or <code>null</code> if the view
     *         chooses not to implement the info button.
     */
    protected abstract CommandContributionItem createInfoToggleContributionItem();

    /**
     * Passing the focus request to the treeViewer's control.
     */
    @Override
    public void setFocus() {
        if (treeViewer != null && treeViewer.getControl() != null
                && !treeViewer.getControl().isDisposed()) {
            treeViewer.getControl().setFocus();
        }

        /*
         * We have to create this object in here and take it out of the
         * plugin.xml file because of an Eclipse bug where the button would be
         * rendered twice.
         */
        if (infoToggleButton == null) {
            infoToggleButton = createInfoToggleContributionItem();

            if (infoToggleButton != null) {
                final IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();
                manager.add(infoToggleButton);
                manager.update(true);
            }
        }
    }

    /**
     * Creates the actions of the view.
     * <p>
     * Hooks the handlers for actions for such as Undo, Redo, Cut Copy, Select All and
     * Deselect all. The Delete action handler must be hooked in {@link #createActions()}.
     */
    private void createActions() {
        createActionsInternal();
        createViewActions();
    }

    /**
     * Creates the view's context menu.
     * <p>
     * This method makes the popup menu available as an URI in the form
     * <tt>popup:context.menu.id</tt> where <tt>context.menu.id</tt> is the value returned
     * by {@link #getContextMenuId()}. Other plugins may add actions to a treeview's menu
     * by registering commands in this URI in their <tt>plugin.xml</tt> file.
     * <p>
     * Descending classes overriding this method must not forget to call
     * <tt>super.createContextMenu()</tt>
     */
    protected void createContextMenu() {
        final String id = getContextMenuId();
        menuMgr = new MenuManager(id);

        // Create menu.
        final Tree tree = treeViewer.getTree();
        contextMenu = menuMgr.createContextMenu(tree);

        // Every context menu must have this option (see issue #234)
        menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        tree.setMenu(contextMenu);

        // Register the menu extension.
        getSite().registerContextMenu(id, menuMgr, treeViewer);
    }

    /**
     * Recursively refreshes a tree item.
     * <p>
     * Recursive refresh is needed when changing a property of one item impacts the
     * property on the children.
     * 
     * @param item tree item
     */
    protected void refreshItemWithChildren(final Object item) {
        refreshTreeItem(item);

        /*
         * update the children if they exist
         */
        final IContentProvider provider = treeViewer.getContentProvider();
        if (provider instanceof IStructuredContentProvider) {
            final IStructuredContentProvider structuredProvider = (IStructuredContentProvider) provider;
            final Object[] children = structuredProvider.getElements(item);
            if (children != null) {
                for (Object child : children) {
                    refreshTreeItem(child);
                }
            }
        }
    }

    /**
     * Refreshes a tree item.
     * 
     * @param item tree item
     */
    protected void refreshTreeItem(final Object item) {
        treeViewer.update(item, null);
        /*
         * we have removed the treeViewer.refresh(item, true) instruction. Not
         * to be so aggressive with refreshing makes the tree more responsive
         * without flickering
         */
    }

    /**
     * Restores the current tree selection from the preference memento.
     * 
     * @param memento the memento to read the selection from
     * @param viewer the treeViewer where the state is to be restored
     * @return the selection restored into the tree treeViewer
     */
    protected static TreeSelection restoreTreeSelection(final IMemento memento,
                                                        final TreeViewer viewer) {

        viewer.getTree().setVisible(false);
        viewer.expandAll();
        viewer.collapseAll();
        TreeViewUtils.restoreState(viewer, memento);
        viewer.getTree().setVisible(true);

        /*
         * set the saved selection
         */
        final TreeSelection selection = TreeViewUtils.restoreTreeSelection(viewer, memento);
        return selection;
    }

    /**
     * Sets and propagates the current selection.
     * <p>
     * Only sets if the selection is not empty.
     * 
     * @param selection a tree selection
     */
    protected final void setSelection(final TreeSelection selection) {
        if (!selection.isEmpty()) {
            treeViewer.setSelection(selection, true);
            final ISelectionProvider provider = getSite().getSelectionProvider();
            final Object[] selectedObjects = selection.toArray();

            SelectionUtils.doSelectItems(selectedObjects, provider);
        }
    }

    private TableSink<AggregateResult> getResultTable() {
        TableSink<AggregateResult> tableSink = new TableSink<AggregateResult>() {
            private void refresh(final AggregateResult elem) {
                final Object item = elem.getGroupId();
                if (item instanceof ModelItem) {
                    AbstractTreeView.this.refreshTreeItem(item);
                }
            }

            @Override
            protected Object getKey(final AggregateResult element) {
                return element.getGroupId();
            }

            @Override
            protected void handleInsert(final Object key, final AggregateResult out) {
                refresh(out);
            }

            @Override
            public void handleDelete(final Object key, final AggregateResult out) {
                refresh(out);
            }

            @Override
            public void handleUpdate(final Object key, final AggregateResult out) {
                refresh(out);
            }
        };

        return tableSink;
    }

    /**
     * Gets a sink where the changes to {@link ModelItem}s are to be reported.
     * <p>
     * The sink returned should be invariant. Implementations should always return the
     * same object.
     * <p>
     * The default implementation return <code>null</code>.
     * 
     * @return a sink of <code>null</code> if not available.
     */
    protected Sink<ModelItem> getItemSink() {
        return null;
    }

    /**
     * Gets a source of aggregate results to update the view.
     * <p>
     * The source returned should be invariant. Implementations should always return the
     * same object.
     * <p>
     * The default implementation return <code>null</code>.
     * 
     * @return a source or <code>null</code> if not available.
     */
    protected Source<AggregateResult> getAggregateResultSource() {
        return null;
    }

    /**
     * @return the handler for the Delete retarget action.
     */
    protected abstract IAction getDeleteHandler();

    /**
     * Gets the context menu id.
     * <p>
     * Descending classes must supply their context menu id, preferably in the form
     * "#lumina.ui.views.myview.contextMenu"
     * 
     * @return the context menu id for the view
     */
    protected abstract String getContextMenuId();

    /**
     * Creates the specific action of the view.
     */
    protected abstract void createViewActions();

    /**
     * Creates the Drag'n'Drop support for the view.
     */
    protected abstract void createDNDSupport();

    /**
     * Checks if an item showing in the tree should be updated recursively.
     * <p>
     * This is needed because updates to some items may not need to recursively refresh
     * the children.
     * 
     * @param object tree item
     * @return true if item must update recursively, false otherwise
     */
    protected abstract boolean mustUpdateRecursively(final Object object);

    /**
     * Creates the columns that are display when the info mode is turned on.
     * 
     * @param tree the tree (the treeViewer) where the columns are to be created.
     * @return array with tree column
     */
    public abstract TreeColumn[] createColumns(final Tree tree);

    /**
     * Handle a property change event that has not been captured by the global property
     * change event.
     * <p>
     * The default implementation does nothing.
     * <p>
     * Descending classes may override this method to react to property change events.
     * 
     * @param event the property change event, never <code>null</code>
     */
    protected void handlePropertyChange(final PropertyChangeEvent event) {
    }

    /**
     * Handles the double-click on an item of the tree.
     * <p>
     * This method is a default empty implementation. Descending classes should averride
     * it.
     * 
     * @param selection the selected item on double-click.
     */
    protected void handleDoubleClick(final ISelection selection) {
    }

    /**
     * Creates the treeViewer and initialize it.
     * 
     * @param parent component
     */
    @Override
    public void createPartControl(final Composite parent) {
        treeViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

        treeViewer.setContentProvider(viewContentProvider);
        treeViewer.setLabelProvider(viewLabelProvider);

        setTooltipListener(new TreeItemProblemTooltip());

        createActions();
        createContextMenu();
        createDNDSupport();

        /*
         * Register the selection provider
         * 
         * The views are registered as selection listeners in
         * ApplicationWorkbenchWindowAdvisor
         */
        getSite().setSelectionProvider(treeViewer);

        /* register the property change listeners of the model */
        ProjectModel.getInstance().addPropertyChangeListener(new IPropertyChangeListener() {
            /**
             * Reacts to generic property change events.
             * <p>
             * Depending on the type of event this method (1) refreshes the appropriate
             * tree nodes and (2) notifies the item processor in order to update the
             * real-time views.
             * 
             * @param event property change event
             * @see #handlePropertyChange(PropertyChangeEvent)
             */
            public final void propertyChange(final PropertyChangeEvent event) {
                if (treeViewer == null || treeViewer.getTree().isDisposed()) {
                    return;
                }

                final Object subject = event.getSource();
                final String changedProperty = event.getProperty();

                if (PropertyChangeNames.isMetadataChange(changedProperty)) {
                    assert ModelUtils.isModelItem(subject);

                    if (modelItemProcessor != null) {
                        final ModelItem item = (ModelItem) subject;
                        modelItemProcessor.update(item);
                    }

                    if (mustUpdateRecursively(subject)) {
                        refreshItemWithChildren(subject);
                    } else {
                        refreshTreeItem(subject);
                    }
                } else if (isInfoMode() && PropertyChangeNames.isStatusChange(changedProperty)) {
                    /*
                     * Update the item properties
                     */
                    assert ModelUtils.isModelItem(subject);

                    if (modelItemProcessor != null) {
                        final ModelItem item = (ModelItem) subject;
                        modelItemProcessor.update(item);
                    }

                    refreshTreeItem(subject);
                } else if (PropertyChangeNames.isNodeAdd(changedProperty)) {
                    assert ModelUtils.isModelItem(subject);
                    treeViewer.refresh(subject, false);
                    treeViewer.expandToLevel(subject, 1);

                    if (modelItemProcessor != null) {
                        for (ModelItem i : getAddedItems(event)) {
                            modelItemProcessor.insert(i);
                        }
                    }
                } else if (PropertyChangeNames.isNodeRemove(changedProperty)) {
                    if (subject instanceof Project) {
                        treeViewer.refresh(subject, false);
                        treeViewer.expandToLevel(subject, 1);
                    } else {
                        final Object[] affectedNodes = (Object[]) subject;
                        for (Object node : affectedNodes) {
                            assert ModelUtils.isModelItem(node);

                            if (modelItemProcessor != null) {
                                for (ModelItem i : getRemovedItems(event)) {
                                    modelItemProcessor.delete(i);
                                }
                            }

                            treeViewer.refresh(node, false);
                            treeViewer.expandToLevel(node, 1);
                        }
                    }
                } else if (PropertyChangeNames.isProjectLoading(changedProperty)) {
                    /*
                     * the input object doesn't matter, it just has to be != null
                     */
                    treeViewer.setInput(new Object());

                    if (subject instanceof Project) {
                        final Project project = (Project) subject;
                        final ValidatorManager manager = project.getValidatorManager();

                        if (manager != null) {
                            /*
                             * Add the label provider to the list of validation event
                             * listeners in order to update the overlays when a
                             * validation event occurs. The label/icon may change
                             * whenever a validation event occurs.
                             */
                            manager.addValidationEventListener(viewLabelProvider);
                        }

                        final Device[] devices = Queries.getAllDevices(project);
                        if (modelItemProcessor != null) {
                            for (Device d : devices) {
                                modelItemProcessor.insert(d);
                            }
                        }
                    }
                } else if (PropertyChangeNames.isProjectLoaded(changedProperty)) {
                    final boolean firstProjectLoaded = event.getOldValue() == Project.UNASSIGNED_PROJECT;
                    if (firstProjectLoaded) {
                        final IMemento memento = getPreferenceMemento();
                        final TreeSelection selection = restoreTreeSelection(memento, treeViewer);
                        /*
                         * propagate the selection loaded to the other views
                         */
                        setSelection(selection);
                    }
                }

                /*
                 * give the opportunity to the descending view to react to property
                 * changes as well
                 */
                handlePropertyChange(event);
            }


        });

        /*
         * set the saved info mode
         */
        if (getPreferenceMemento() != null) {
            final String state = getPreferenceMemento().getString(TAG_TOGGLE_INFO_ENABLED);
            if (state != null) {
                setInfoMode(Boolean.parseBoolean(state));
            }
        }
    }

    /**
     * Terminates.
     * <p>
     * Disposes all tree view dependent objects and removes all the listeners.
     */
    public void dispose() {
        if (contextMenu != null) {
            contextMenu.dispose();
        }

        if (menuMgr != null) {
            menuMgr.dispose();
        }

        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);

        super.dispose();
    }

    /**
     * Returns the view's workbench window.
     * 
     * @return the view's workbench window
     */
    protected IWorkbenchWindow getWorkbenchWindow() {
        return getSite().getWorkbenchWindow();
    }

    /**
     * Returns the selected tree item.
     * 
     * @return selected tree item
     */
    protected final ISelection getSelected() {
        return treeViewer.getSelection();
    }

    /**
     * Returns the tree view.
     * 
     * @return tree view
     */
    public final TreeViewer getTreeViewer() {
        return treeViewer;
    }

    /**
     * Turns the information mode <tt>on</tt> or <tt>off</tt.
     * <p>
     * When this property is changed, a property change event is propagated.
     * 
     * @param turnedOn indicated whether the info mode should be turned on or off.
     */
    public final void setInfoMode(boolean turnedOn) {
        if (turnedOn) {
            if (!isInfoMode()) {
                final Tree tree = treeViewer.getTree();
                tree.setHeaderVisible(true);

                modelTreeColumn = new TreeColumn(tree, SWT.NONE);
                modelTreeColumn.setWidth(DEFAULT_LEFTMOST_COLUMN_WIDTH);
                modelTreeColumn.setText("");

                infoPropertyColumns = createColumns(tree);

                firePropertyChange(0);
            }
        } else {
            if (isInfoMode()) {
                modelTreeColumn.dispose();

                if (infoPropertyColumns != null) {
                    for (TreeColumn c : infoPropertyColumns) {
                        c.dispose();
                    }
                }

                final Tree tree = treeViewer.getTree();
                tree.setHeaderVisible(false);

                firePropertyChange(0);
            }
        }

        treeViewer.refresh();
    }

    /**
     * Checks if view is in info mode.
     * 
     * @return <code>true</code> if the info mode is turned on; <code>false</code>
     *         otherwise.
     */
    public boolean isInfoMode() {
        final Tree tree = treeViewer.getTree();
        return tree.getHeaderVisible();
    }

    /**
     * Saves the current tree view state.
     * 
     * @param memento the memento to save
     */
    @Override
    public void saveState(final IMemento memento) {
        super.saveState(memento);
        final Boolean isEnabled = isInfoMode();
        memento.putString(TAG_TOGGLE_INFO_ENABLED, isEnabled.toString());

        final ISelection selection = treeViewer.getSelection();
        if (selection instanceof TreeSelection) {
            TreeViewUtils.saveTreeSelection((TreeSelection) selection, memento);
        }

        TreeViewUtils.saveState(treeViewer, memento);
    }

    /**
     * Trigger for selection change.
     * 
     * @param part workbench part
     * @param selection selected item
     */
    public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
        if (part != this) {
            /*
             * We cannot set the selection if already set. Otherwise, the
             * selectionChanged method would be called without stopping
             * resulting on a stack overflow.
             * 
             * /!\ The treeViewer returns a TreeSelection; however
             * treeSelection.equals(s) is only capable of comparing with
             * selections of the same type. This is why we do not write
             * currentSelection.equals(selection).
             */
            final ISelection currentSelection = treeViewer.getSelection();
            final boolean alreadySelected = selection.equals(currentSelection);
            if (!alreadySelected) {
                treeViewer.setSelection(selection);
            }
        }
    }

    /**
     * Reacts to generic property change events.
     * <p>
     * Depending on the type of event this method (1) refreshes the appropriate tree nodes
     * and (2) notifies the item processor in order to update the real-time views.
     * 
     * @param event property change event
     * @see #handlePropertyChange(PropertyChangeEvent)
     */
    public final void propertyChange(final PropertyChangeEvent event) {
        if (treeViewer == null || treeViewer.getTree().isDisposed()) {
            return;
        }

        final Object subject = event.getSource();
        final String changedProperty = event.getProperty();

        if (PropertyChangeNames.isMetadataChange(changedProperty)) {
            assert ModelUtils.isModelItem(subject);

            if (modelItemProcessor != null) {
                final ModelItem item = (ModelItem) subject;
                modelItemProcessor.update(item);
            }

            if (mustUpdateRecursively(subject)) {
                refreshItemWithChildren(subject);
            } else {
                refreshTreeItem(subject);
            }
        } else if (isInfoMode() && PropertyChangeNames.isStatusChange(changedProperty)) {
            /*
             * Update the item properties
             */
            assert ModelUtils.isModelItem(subject);

            if (modelItemProcessor != null) {
                final ModelItem item = (ModelItem) subject;
                modelItemProcessor.update(item);
            }

            refreshTreeItem(subject);
        } else if (PropertyChangeNames.isNodeAdd(changedProperty)) {
            assert ModelUtils.isModelItem(subject);
            treeViewer.refresh(subject, false);
            treeViewer.expandToLevel(subject, 1);

            if (modelItemProcessor != null) {
                for (ModelItem i : getAddedItems(event)) {
                    modelItemProcessor.insert(i);
                }
            }
        } else if (PropertyChangeNames.isNodeRemove(changedProperty)) {
            if (subject instanceof Project) {
                treeViewer.refresh(subject, false);
                treeViewer.expandToLevel(subject, 1);
            } else {
                final Object[] affectedNodes = (Object[]) subject;
                for (Object node : affectedNodes) {
                    assert ModelUtils.isModelItem(node);

                    if (modelItemProcessor != null) {
                        for (ModelItem i : getRemovedItems(event)) {
                            modelItemProcessor.delete(i);
                        }
                    }

                    treeViewer.refresh(node, false);
                    treeViewer.expandToLevel(node, 1);
                }
            }
        } else if (PropertyChangeNames.isProjectLoading(changedProperty)) {
            /*
             * the input object doesn't matter, it just has to be != null
             */
            treeViewer.setInput(new Object());

            if (subject instanceof Project) {
                final Project project = (Project) subject;
                final ValidatorManager manager = project.getValidatorManager();

                if (manager != null) {
                    /*
                     * Add the label provider to the list of validation event
                     * listeners in order to update the overlays when a
                     * validation event occurs. The label/icon may change
                     * whenever a validation event occurs.
                     */
                    manager.addValidationEventListener(viewLabelProvider);
                }

                final Device[] devices = Queries.getAllDevices(project);
                if (modelItemProcessor != null) {
                    for (Device d : devices) {
                        modelItemProcessor.insert(d);
                    }
                }
            }
        } else if (PropertyChangeNames.isProjectLoaded(changedProperty)) {
            final boolean firstProjectLoaded = event.getOldValue() == Project.UNASSIGNED_PROJECT;
            if (firstProjectLoaded) {
                final IMemento memento = getPreferenceMemento();
                final TreeSelection selection = restoreTreeSelection(memento, treeViewer);
                /*
                 * propagate the selection loaded to the other views
                 */
                setSelection(selection);
            }
        }

        /*
         * give the opportunity to the descending view to react to property
         * changes as well
         */
        handlePropertyChange(event);
    }

    /**
     * Removes inline edit listener.
     */
    public final void removeInlineEditListener() {
        if (inlineEditListerner != null) {
            inlineEditListerner.remove(treeViewer.getTree());
            inlineEditListerner = null;
        }
    }

    /**
     * Removes tooltip listener.
     */
    public final void removeTooltipListener() {
        if (tooltipListener != null) {
            tooltipListener.unregister(treeViewer.getTree());
            tooltipListener = null;
        }
    }

    /**
     * Sets inline edit listener for this tree.
     * <p>
     * Defines the edit listener for inline edition.
     * 
     * @param listener tree item inline edit listener
     */
    protected final void setInlineEditListener(final TreeItemInlineEditListener listener) {
        removeInlineEditListener();
        listener.register(treeViewer.getTree());
        inlineEditListerner = listener;
    }

    /**
     * Sets the tooltop listener.
     * <p>
     * Defines the listeter for the problem tooltip.
     * 
     * @param listener tree item problem tooltip
     */
    public final void setTooltipListener(final TreeItemProblemTooltip listener) {
        removeTooltipListener();
        listener.register(treeViewer.getTree());
        tooltipListener = listener;
    }
}
