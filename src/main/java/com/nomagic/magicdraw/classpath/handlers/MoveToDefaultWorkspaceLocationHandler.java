package com.nomagic.magicdraw.classpath.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.nomagic.magicdraw.classpath.MDKProjectPropertyTester;
import com.nomagic.magicdraw.classpath.MDKProjectPropertyTester.ProjectLocation;

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 *
 * Copyright 2012 Jet Propulsion Laboratory/Caltech
 */
public class MoveToDefaultWorkspaceLocationHandler extends AbstractHandler implements IElementUpdater {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
	}

	public static void moveToDefaultWorkspace(ISelection selection) throws CoreException {
		List<IProject> projects = MDKProjectPropertyTester.findMDKProjectsInSelection(selection);
		List<IProject> docGenScriptsToMove = new ArrayList<IProject>();
		List<IProject> mdkScripsToMove = new ArrayList<IProject>();
		List<IProject> pluginsToMove = new ArrayList<IProject>();
		for (IProject project : projects) {
			ProjectLocation loc = MDKProjectPropertyTester.getProjectLocation(project);
			switch (loc) {
			case MAGICDRAW_DOCGEN_SCRIPT:
			case OTHER_DOCGEN_SCRIPT:
				docGenScriptsToMove.add(project);
				break;
			case MAGICDRAW_MDK_SCRIPT:
			case OTHER_MDK_SCRIPT:
				mdkScripsToMove.add(project);
				break;
			case MAGICDRAW_PLUGIN:
			case OTHER_PLUGIN:
				pluginsToMove.add(project);
				break;
			default:
				break;
			}
		}
		MDKProjectPropertyTester.moveProjectsToEclipseWorkspace(docGenScriptsToMove);
		MDKProjectPropertyTester.moveProjectsToEclipseWorkspace(mdkScripsToMove);
		MDKProjectPropertyTester.moveProjectsToEclipseWorkspace(pluginsToMove);
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		try {
			moveToDefaultWorkspace(selection);
			return null;
		} catch (CoreException e) {
			throw new ExecutionException("MoveToDefaultWorkspaceLocationHandler", e);
		}
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IProject project = MDKProjectPropertyTester.findProjectInViewers();
		ProjectLocation loc = MDKProjectPropertyTester.getProjectLocation(project);
		if (null == loc)
			return;
	}
}