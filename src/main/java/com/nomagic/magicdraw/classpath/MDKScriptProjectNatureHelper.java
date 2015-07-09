package com.nomagic.magicdraw.classpath;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 *
 * Copyright 2012 Jet Propulsion Laboratory/Caltech
 */
public class MDKScriptProjectNatureHelper {

	public static boolean hasProjectNature(IProject project) {
		if (null == project)
			throw new IllegalArgumentException("MDKScriptProjectNatureHelper.hasProjectNature()");
		
		try {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();

			for (int i = 0; i < natures.length; ++i) {
				if (MDKScriptProjectNature.NATURE_ID.equals(natures[i]))
					return true;
			}
			return false;
			
		} catch (CoreException e) {
			throw new IllegalArgumentException("MDKScriptProjectNatureHelper.hasProjectNature()", e);
		}
	}

	/**
	 * Toggles sample nature on a project
	 * 
	 * @param project
	 *            to have sample nature added or removed
	 * @throws CoreException 
	 */
	public static void toggleNature(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();

		for (int i = 0; i < natures.length; ++i) {
			if (MDKScriptProjectNature.NATURE_ID.equals(natures[i])) {
				// Remove the nature
				String[] newNatures = new String[natures.length - 1];
				System.arraycopy(natures, 0, newNatures, 0, i);
				System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
				description.setNatureIds(newNatures);
				project.setDescription(description, null);
				return;
			}
		}

		// Add the nature
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = MDKScriptProjectNature.NATURE_ID;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);
	}

	public static void addNature(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();

		for (int i = 0; i < natures.length; ++i) {
			if (MDKScriptProjectNature.NATURE_ID.equals(natures[i])) {
				return;
			}
		}

		// Add the nature
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = MDKScriptProjectNature.NATURE_ID;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);
	}
		
	public static void removeNature(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();

		for (int i = 0; i < natures.length; ++i) {
			if (MDKScriptProjectNature.NATURE_ID.equals(natures[i])) {
				// Remove the nature
				String[] newNatures = new String[natures.length - 1];
				System.arraycopy(natures, 0, newNatures, 0, i);
				System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
				description.setNatureIds(newNatures);
				project.setDescription(description, null);
				return;
			}
		}
	}
	
	public static IPath getScriptMagicDrawLocation(final IProject mdkPluginsProject) {
		IPath mdInstallRoot = MDVariableInitializer.getMDInstallRootPath();
		if (mdInstallRoot.isEmpty())
			return null;
		final IPath mdkScriptProjectLocation = mdInstallRoot.addTrailingSeparator().append("mdk.scripts").addTrailingSeparator().append(mdkPluginsProject.getName());
		return mdkScriptProjectLocation;
	}
	
	public static void moveMDKScriptsProjects(List<IProject> mdkScriptsProjects) {
		for (IProject mdkScriptsProject : mdkScriptsProjects) {
			moveMDKScriptsProject(mdkScriptsProject);
		}
	}
	
	/**
	 * There should be a simpler way to do this...
	 * 
	 * @param mdkScriptsProject the JPL MagicDraw Script-natured project to move to MD's install.root/mdk.scripts folder
	 */
	public static void moveMDKScriptsProject(final IProject mdkScriptsProject) {
		IPath mdInstallRoot = MDVariableInitializer.getMDInstallRootPath();
		if (mdInstallRoot.isEmpty())
			return;
		IPath newMDKScriptProjectLocation = getScriptMagicDrawLocation(mdkScriptsProject);
		if (null == newMDKScriptProjectLocation)
			return;
		MDKProjectPropertyTester.moveProject(
				mdkScriptsProject, 
				newMDKScriptProjectLocation,
				"the ./mdk.scripts folder in MD's install.root",
				mdInstallRoot);
	}
}
