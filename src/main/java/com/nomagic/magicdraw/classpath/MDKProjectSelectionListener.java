package com.nomagic.magicdraw.classpath;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.services.IEvaluationService;

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 *
 * Copyright 2012 Jet Propulsion Laboratory/Caltech
 */
public class MDKProjectSelectionListener implements ISelectionChangedListener {

	private Viewer viewer;

	public void dispose() {
		if (viewer != null) {
			viewer.removeSelectionChangedListener(this);
		}
	}

	public void hookOnViewer(final String viewerId) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (viewerId != null && workbench != null && workbench.getDisplay() != null) {
			Display display = workbench.getDisplay();
			Thread displayThread = display.getThread();
			if (workbench.isStarting() || !Thread.currentThread().equals(displayThread)) {
				// while workbench is starting defer hooking until later
				UIJob job = new UIJob(display, "viewer hooker") {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						hookOnViewer(viewerId);
						return Status.OK_STATUS;
					}
				};
				job.schedule(250);
			} else if (viewerId != null) {
				Viewer viewer = MDKProjectPropertyTester.findViewer(viewerId);
				if (viewer != null) {
					if (this.viewer != null) {
						this.viewer.removeSelectionChangedListener(this);
					}
					requestRefresh();
					viewer.addSelectionChangedListener(this);
					this.viewer = viewer;
				}
			}
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		requestRefresh();
	}

	protected void requestRefresh() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IEvaluationService evaluationService = (IEvaluationService) window.getService(IEvaluationService.class);
		if (evaluationService != null) {
			evaluationService.requestEvaluation(MDKProjectPropertyTester.PROPERTY_NAMESPACE + "." + MDKProjectPropertyTester.PROPERTY_WORKSPACE_PLUGIN_LOCATION);
			evaluationService.requestEvaluation(MDKProjectPropertyTester.PROPERTY_NAMESPACE + "." + MDKProjectPropertyTester.PROPERTY_MD_PLUGIN_LOCATION);
			evaluationService.requestEvaluation(MDKProjectPropertyTester.PROPERTY_NAMESPACE + "." + MDKProjectPropertyTester.PROPERTY_OTHER_PLUGIN_LOCATION);
			evaluationService.requestEvaluation(MDKProjectPropertyTester.PROPERTY_NAMESPACE + "." + MDKProjectPropertyTester.PROPERTY_WORKSPACE_MDK_SCRIPT_LOCATION);
			evaluationService.requestEvaluation(MDKProjectPropertyTester.PROPERTY_NAMESPACE + "." + MDKProjectPropertyTester.PROPERTY_MD_MDK_SCRIPT_LOCATION);
			evaluationService.requestEvaluation(MDKProjectPropertyTester.PROPERTY_NAMESPACE + "." + MDKProjectPropertyTester.PROPERTY_OTHER_MDK_SCRIPT_LOCATION);
			evaluationService.requestEvaluation(MDKProjectPropertyTester.PROPERTY_NAMESPACE + "." + MDKProjectPropertyTester.PROPERTY_WORKSPACE_PROJECT);
		}
		ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
		if (commandService != null) {
			commandService.refreshElements("com.nomagic.magicdraw.classpath.MDKProject.moveToMDInstallationScriptsFolder", null);
			commandService.refreshElements("com.nomagic.magicdraw.classpath.MDKProject.moveToMDInstallationPluginsFolder", null);
			commandService.refreshElements("com.nomagic.magicdraw.classpath.MDKProject.moveToDefaultWorkspaceLocation", null);
		}
	}

}
