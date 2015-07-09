package com.nomagic.magicdraw.classpath.actions;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.nomagic.magicdraw.classpath.handlers.MoveToMDInstallationMDKScriptsFolderHandler;

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 *
 * Copyright 2012 Jet Propulsion Laboratory/Caltech
 */
public class MoveToMDInstallationMDKScriptsFolderDelegate implements IWorkbenchWindowActionDelegate {

	private ISelection fSelection;
	private IWorkbench fWorkbench;

	@Override
	public void run(IAction action) {
		if (null == fWorkbench)
			Assert.isNotNull(fWorkbench);

		try {
			MoveToMDInstallationMDKScriptsFolderHandler.moveMDKScriptProjects(fSelection);
		} catch (CoreException e) {
			throw new RuntimeException("MoveToDefaultWorkspaceLocationHandler", e);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		fSelection = selection;
	}

	@Override
	public void dispose() {
		fSelection = null;
		fWorkbench = null;
	}

	@Override
	public void init(IWorkbenchWindow window) {
		if (null != window) 
			fWorkbench = window.getWorkbench();
	}

}

