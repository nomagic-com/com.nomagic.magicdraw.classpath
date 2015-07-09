package com.nomagic.magicdraw.classpath;



import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.ui.IPackagesViewPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.MoveProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 *
 * Copyright 2012 Jet Propulsion Laboratory/Caltech
 */
public class MDKProjectPropertyTester extends PropertyTester {
	public static final String PROPERTY_NAMESPACE = "com.nomagic.magicdraw.classpath.project";
	
	public static final String PROPERTY_WORKSPACE_PROJECT 				= "workspaceProject";
	
	public static final String PROPERTY_WORKSPACE_DOCGEN_SCRIPT_LOCATION	= "workspaceDocGenScriptLocation";
	public static final String PROPERTY_MD_DOCGEN_SCRIPT_LOCATION 		= "mdDocGenScriptLocation";
	public static final String PROPERTY_OTHER_DOCGEN_SCRIPT_LOCATION 		= "otherDocGenScriptLocation";
	
	public static final String PROPERTY_WORKSPACE_MDK_SCRIPT_LOCATION 	= "workspaceMDKScriptLocation";
	public static final String PROPERTY_MD_MDK_SCRIPT_LOCATION 			= "mdMDKScriptLocation";
	public static final String PROPERTY_OTHER_MDK_SCRIPT_LOCATION 		= "otherMDKScriptLocation";
	
	public static final String PROPERTY_WORKSPACE_PLUGIN_LOCATION 		= "workspaceMagicDrawPluginLocation";
	public static final String PROPERTY_MD_PLUGIN_LOCATION 				= "mdMagicDrawPluginLocation";
	public static final String PROPERTY_OTHER_PLUGIN_LOCATION 			= "otherMagicDrawPluginLocation";

	public static enum ProjectLocation {
		
		ECLIPSE_PROJECT,
		
		WORKSPACE_DOCGEN_SCRIPT,
		MAGICDRAW_DOCGEN_SCRIPT,
		OTHER_DOCGEN_SCRIPT,

		WORKSPACE_MDK_SCRIPT,
		MAGICDRAW_MDK_SCRIPT,
		OTHER_MDK_SCRIPT,

		WORKSPACE_PLUGIN,
		MAGICDRAW_PLUGIN,
		OTHER_PLUGIN,
		
		UNKNOWN
	}
	
	public static Map<String, ProjectLocation> PropertyName2ProjectLocation;
	
	static {
		PropertyName2ProjectLocation = new HashMap<String, ProjectLocation>();
		
		PropertyName2ProjectLocation.put(PROPERTY_WORKSPACE_PROJECT, 					ProjectLocation.ECLIPSE_PROJECT);
		
		PropertyName2ProjectLocation.put(PROPERTY_WORKSPACE_DOCGEN_SCRIPT_LOCATION,	ProjectLocation.WORKSPACE_DOCGEN_SCRIPT);
		PropertyName2ProjectLocation.put(PROPERTY_MD_DOCGEN_SCRIPT_LOCATION, 			ProjectLocation.MAGICDRAW_DOCGEN_SCRIPT);
		PropertyName2ProjectLocation.put(PROPERTY_OTHER_DOCGEN_SCRIPT_LOCATION, 		ProjectLocation.OTHER_DOCGEN_SCRIPT);
		
		PropertyName2ProjectLocation.put(PROPERTY_WORKSPACE_MDK_SCRIPT_LOCATION,		ProjectLocation.WORKSPACE_MDK_SCRIPT);
		PropertyName2ProjectLocation.put(PROPERTY_MD_MDK_SCRIPT_LOCATION, 			ProjectLocation.MAGICDRAW_MDK_SCRIPT);
		PropertyName2ProjectLocation.put(PROPERTY_OTHER_MDK_SCRIPT_LOCATION, 		ProjectLocation.OTHER_MDK_SCRIPT);
		
		PropertyName2ProjectLocation.put(PROPERTY_WORKSPACE_PLUGIN_LOCATION, 			ProjectLocation.WORKSPACE_PLUGIN);
		PropertyName2ProjectLocation.put(PROPERTY_MD_PLUGIN_LOCATION, 				ProjectLocation.MAGICDRAW_PLUGIN);
		PropertyName2ProjectLocation.put(PROPERTY_OTHER_PLUGIN_LOCATION, 				ProjectLocation.OTHER_PLUGIN);
	}
	
	public static String PACKAGE_EXPLORER_VIEW_ID = "org.eclipse.jdt.ui.PackageExplorer";
	public static String PROJECT_EXPLORER_VIEW_ID = ProjectExplorer.VIEW_ID;
		
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (!(receiver instanceof IProject))
			return false;
		
		IProject receiverProject = (IProject) receiver;	
		if (!receiverProject.isAccessible())
			return false;
		
		ProjectLocation loc = getProjectLocation(receiverProject);
		
		if (PropertyName2ProjectLocation.containsKey(property)) {
			ProjectLocation propertyLoc = PropertyName2ProjectLocation.get(property);
			boolean result = (loc == propertyLoc);
			System.out.println(String.format("test[%s] %s=%b", receiverProject, property, result));
			return result;
		}
		
		return false;
	}

	protected IProject viewerSelectionContainsProject(String viewerId, Object object) {
		TreeViewer viewer = findViewer(viewerId);
		if (viewer != null && object != null) {
			ISelection selection = viewer.getSelection();
			if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
				IStructuredSelection sel = (IStructuredSelection) selection;
				Iterator<?> it = sel.iterator();
				while (it.hasNext()) {
					Object element = it.next();
					if (element instanceof IProject) {
						if (element.equals(object))
							return (IProject) element;
					} else {
						final Object adapted= Platform.getAdapterManager().getAdapter(element, IResource.class);
						if (adapted instanceof IProject) {
							if (adapted.equals(object))
								return (IProject) adapted;
							}
					}
				}
			}
		}
		return null;
	}
	
	public static IProject findProjectInViewers() {
		IProject p = findProjectInViewer(MDKProjectPropertyTester.PACKAGE_EXPLORER_VIEW_ID);
		if (null != p)
			return p;
		p = findProjectInViewer(MDKProjectPropertyTester.PROJECT_EXPLORER_VIEW_ID);
		if (null != p)
			return p;
		return null;
	}
	
	public static IProject findProjectInViewer(String viewId) {
		TreeViewer viewer = findViewer(viewId);
		if (null != viewer) {
			ISelection selection = viewer.getSelection();
			return findProjectInSelection(selection);
		}
		return null;
	}
	
	public static ProjectLocation getProjectLocation(IProject project) {
		if (null == project)
			return null;
		
		IPath currentProjectLocation = project.getLocation();
		if (null == currentProjectLocation)
			throw new IllegalArgumentException("Cannot determine project location");
		IPath eclipseWorkspaceLocation = getEclipseWorkspaceLocation(project);
			
		if (DocGenScriptProjectNatureHelper.hasProjectNature(project)) {
			IPath docGenMagicDrawLocation = DocGenScriptProjectNatureHelper.getDocGenScriptMagicDrawLocation(project);
			if (currentProjectLocation.equals(docGenMagicDrawLocation)) {
				return ProjectLocation.MAGICDRAW_DOCGEN_SCRIPT;
			} else if (currentProjectLocation.equals(eclipseWorkspaceLocation)) {
				return ProjectLocation.WORKSPACE_DOCGEN_SCRIPT;
			} else {
				return ProjectLocation.OTHER_DOCGEN_SCRIPT;
			}
		} else if (MDKScriptProjectNatureHelper.hasProjectNature(project)) {
			IPath scriptMagicDrawLocation = MDKScriptProjectNatureHelper.getScriptMagicDrawLocation(project);
			if (currentProjectLocation.equals(scriptMagicDrawLocation)) {
				return ProjectLocation.MAGICDRAW_MDK_SCRIPT;
			} else if (currentProjectLocation.equals(eclipseWorkspaceLocation)) {
				return ProjectLocation.WORKSPACE_MDK_SCRIPT;
			} else {
				return ProjectLocation.OTHER_MDK_SCRIPT;
			}
		} else if (MagicDrawPluginProjectNatureHelper.hasProjectNature(project)) {
			IPath pluginMagicDrawLocation = MagicDrawPluginProjectNatureHelper.getPluginMagicDrawLocation(project);
			if (currentProjectLocation.equals(pluginMagicDrawLocation)) {
				return ProjectLocation.MAGICDRAW_PLUGIN;
			} else if (currentProjectLocation.equals(eclipseWorkspaceLocation)) {
				return ProjectLocation.WORKSPACE_PLUGIN;
			} else {
				return ProjectLocation.OTHER_PLUGIN;
			}
		} else {
			if (currentProjectLocation.equals(eclipseWorkspaceLocation)) {
				return ProjectLocation.ECLIPSE_PROJECT;
			}
		}
		
		return ProjectLocation.UNKNOWN;
	}
	
	public static IPath getEclipseWorkspaceLocation(final IProject project) {
		IPath locationContext = Platform.getLocation();
		final IPath eclipseWorkspaceLocation = locationContext.addTrailingSeparator().append(project.getName());
		return eclipseWorkspaceLocation;
	}
	
	public static boolean isEclipseProject(IProject project) {
		if (null == project)
			return false;
		ProjectLocation loc = getProjectLocation(project);
		return (loc == ProjectLocation.ECLIPSE_PROJECT);
	}
	
	public static boolean isMDKProject(IProject project) {
		if (null == project)
			return false;
		if (DocGenScriptProjectNatureHelper.hasProjectNature(project))
			return true;
		if (MDKScriptProjectNatureHelper.hasProjectNature(project))
			return true;
		if (MagicDrawPluginProjectNatureHelper.hasProjectNature(project))
			return true;
		return false;
	}
	
	public static IProject findProjectInSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			Iterator<?> it = sel.iterator();
			while (it.hasNext()) {
				Object element = it.next();
				if (element instanceof IProject) {
					return (IProject) element;
				} else {
					final Object adapted= Platform.getAdapterManager().getAdapter(element, IResource.class);
					if (adapted instanceof IProject) {
						return (IProject) adapted;
					}
				}
			}
		}
		return null;
	}
	
	public static List<IProject> findEclipseProjectsInSelection(ISelection selection) throws CoreException {
		final ArrayList<IProject> eclipseProjects = new ArrayList<IProject>();
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			Iterator<?> it = sel.iterator();
			while (it.hasNext()) {
				Object element = it.next();
				if (element instanceof IProject) {
					IProject project = (IProject) element;
					if (isEclipseProject(project))
						eclipseProjects.add(project);
				} else {
					final Object adapted= Platform.getAdapterManager().getAdapter(element, IProject.class);
					if (adapted instanceof IProject) {
						IProject project = (IProject) adapted;
						if (isEclipseProject(project))
							eclipseProjects.add(project);
					}
				}
			}
		}
		return eclipseProjects;
	}
	
	public static List<IProject> findMDKProjectsInSelection(ISelection selection) throws CoreException {
		final ArrayList<IProject> mdkProjects = new ArrayList<IProject>();
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			Iterator<?> it = sel.iterator();
			while (it.hasNext()) {
				Object element = it.next();
				if (element instanceof IProject) {
					IProject project = (IProject) element;
					if (isMDKProject(project))
						mdkProjects.add(project);
				} else {
					final Object adapted= Platform.getAdapterManager().getAdapter(element, IProject.class);
					if (adapted instanceof IProject) {
						IProject project = (IProject) adapted;
						if (isMDKProject(project))
							mdkProjects.add(project);
					}
				}
			}
		}
		return mdkProjects;
	}
	
	public static TreeViewer findViewer(String viewId) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null && viewId != null) {
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			if (window != null) {
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IViewReference viewReference = page.findViewReference(viewId);
					if (viewReference != null) {
						IViewPart view = viewReference.getView(false);
						if (view instanceof IPackagesViewPart) {
							return ((IPackagesViewPart) view).getTreeViewer();
						}
						if (view instanceof CommonNavigator) {
							return ((CommonNavigator) view).getCommonViewer();
						}
					}
				}
			}
		}
		return null;
	}
	
	public static IShellProvider getShellProvider() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			return window;
		}

		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		if (windows.length > 0) {
			return windows[0];
		} 

		return null;
	}
	
	public static void moveProjectsToEclipseWorkspace(List<IProject> projects) {
		IPath locationContext = Platform.getLocation();
		for (IProject project : projects) {
			MDKProjectPropertyTester.moveProject(
					project,
					locationContext,
					"the Eclipse workspace",
					locationContext,
					false);
		}
	}	
	
	public static void moveProject(
			final IProject project, 
			final IPath newProjectLocation,
			final String titleFragment,
			final IPath locationContainer) {	
		moveProject(project, newProjectLocation, titleFragment, locationContainer, true);
	}
	
	/**
	 * @param project The Eclipse project to relocate (into or out of the Eclispe workspace)
	 * @param newProjectLocation The absolute path of the new project location
	 * @param titleFragment Title for the relocation dialog informing the user about what happened.
	 * @param locationContainer The absolute path of an ancestor of the new project location (parent, grand-parent, ...) used for informing the user about the move.
	 * @param validate set it to true for relocating out of the Eclipse workspace; false for relocating into the Eclipse workspace.
	 */
	public static void moveProject(
			final IProject project, 
			final IPath newProjectLocation,
			final String titleFragment,
			final IPath locationContainer,
			final boolean validate) {	
		if (null == project || null == newProjectLocation || null == titleFragment || null == locationContainer)
			throw new IllegalArgumentException("moveProject()");
		
			
		IPath currentProjectLocation = project.getLocation();
		if (currentProjectLocation.equals(newProjectLocation)) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "moveProject(...) -- nilpotent move", null), StatusManager.LOG);
			return;
		}
		
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = ws.getRoot();
		IContainer newProjectLocationContainer = root.getContainerForLocation(newProjectLocation);
		if (newProjectLocationContainer instanceof IProject) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "moveProject(...) -- destination is a project: " + newProjectLocation, null), StatusManager.LOG);
			return;
		}
	
		final Shell shell = getShellProvider().getShell();
		final String title = String.format("Moving '%s' project to %s:\n%s",
				project.getName(),
				titleFragment,
				locationContainer);
		final String info = String.format("Project %s moved to %s:\n%s", 
				project.getName(),
				titleFragment,
				locationContainer);
		
		if (validate) {
			IStatus validation = ws.validateProjectLocation(project, newProjectLocation);
			if (!validation.isOK()) {
				StatusManager.getManager().handle(validation, StatusManager.LOG);
				return;
			}

			URI newProjectLocationURI = ws.getPathVariableManager().resolveURI(newProjectLocation.toFile().toURI());
			try {
				IFileStore fileStore = EFS.getStore(newProjectLocationURI);
				if (fileStore == null)
					throw new IllegalArgumentException("No EFS store for: " + newProjectLocationURI);
				
				IFileInfo fileInfo = fileStore.fetchInfo();
				if (fileInfo == null)
					throw new IllegalArgumentException("No EFS store for: " + newProjectLocationURI);
				
				if (fileInfo.exists()) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Cannot move the project '" + project + "' to: " + newProjectLocationURI + " -- target directory already exists!", null), StatusManager.LOG);
					return;
				}
			} catch (IllegalArgumentException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e), StatusManager.LOG);
				return;
			} catch (CoreException e) {
				StatusManager.getManager().handle(e.getStatus(), StatusManager.LOG);
				return;
			}
		}
		
		final IOperationHistory history = PlatformUI.getWorkbench().getOperationSupport().getOperationHistory();
		final IStatus[] result = new IStatus[]{null};
		IRunnableWithProgress op =  new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				MoveProjectOperation op = new MoveProjectOperation(project, newProjectLocation.toFile().toURI(), title);
				op.setModelProviderIds(null);
				try {
					result[0] = history.execute(op, monitor, WorkspaceUndoUtil.getUIInfoAdapter(shell));
				} catch (ExecutionException e) {
					String message = String.format("Error during project move: %s",  e.getLocalizedMessage());
					result[0] = new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), message, e);
					StatusManager.getManager().handle(result[0], StatusManager.LOG);
				}
			}
		};
	
		try {
			new ProgressMonitorDialog(getShellProvider().getShell()).run(true, true, op);
			if (result[0] != null && result[0].isOK())
				MessageDialog.openInformation(shell, title, info);
		} catch (InterruptedException e) {
			String message = String.format("Error during project move: %s",  e.getLocalizedMessage());
			MessageDialog.openError(getShellProvider().getShell(), title, message);
			Activator.log(IStatus.ERROR, message, e);
		} catch (InvocationTargetException e) {
			// CoreExceptions are collected by the operation, but unexpected runtime
			// exceptions and errors may still occur.
			String message = String.format("Error during project move: %s",  e.getLocalizedMessage());
			MessageDialog.openError(getShellProvider().getShell(), title, message);
			Activator.log(IStatus.ERROR, message, e);
		}
	}

}
