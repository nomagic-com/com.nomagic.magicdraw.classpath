package com.nomagic.magicdraw.classpath.handlers;

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

import com.nomagic.magicdraw.classpath.DocGenScriptProjectNatureHelper;
import com.nomagic.magicdraw.classpath.MDKProjectPropertyTester;
import com.nomagic.magicdraw.classpath.MDKProjectPropertyTester.ProjectLocation;

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 *
 * Copyright 2012 Jet Propulsion Laboratory/Caltech
 */
public class AddDocGenScriptNatureActionHandler extends AbstractHandler implements IElementUpdater {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		try {
			List<IProject> eclipseProjects = MDKProjectPropertyTester.findEclipseProjectsInSelection(selection);
			for (IProject eclipseProject : eclipseProjects) {
				ProjectLocation loc = MDKProjectPropertyTester.getProjectLocation(eclipseProject);
				switch (loc) {
				case ECLIPSE_PROJECT:
					DocGenScriptProjectNatureHelper.addNature(eclipseProject);
					break;
				default:
					break;
				}
			}
			return null;
		} catch (CoreException e) {
			throw new ExecutionException("AddDocGenScriptNatureActionHandler", e);
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
