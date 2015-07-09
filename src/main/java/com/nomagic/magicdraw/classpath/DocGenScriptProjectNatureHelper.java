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
public class DocGenScriptProjectNatureHelper {

	public static boolean hasProjectNature(IProject project) {
		if (null == project)
			throw new IllegalArgumentException("MDKDocGenScriptProjectNatureHelper.hasProjectNature()");
		
		try {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();

			for (int i = 0; i < natures.length; ++i) {
				if (DocGenScriptProjectNature.NATURE_ID.equals(natures[i]))
					return true;
			}
			
			return false;
			
		} catch (CoreException e) {
			throw new IllegalArgumentException("MDKDocGenScriptProjectNatureHelper.hasProjectNature()", e);
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
			if (DocGenScriptProjectNature.NATURE_ID.equals(natures[i])) {
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
		newNatures[natures.length] = DocGenScriptProjectNature.NATURE_ID;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);
	}

	public static void addNature(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();

		for (int i = 0; i < natures.length; ++i) {
			if (DocGenScriptProjectNature.NATURE_ID.equals(natures[i])) {
				return;
			}
		}

		// Add the nature
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = DocGenScriptProjectNature.NATURE_ID;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);
	}
	
	public static void removeNature(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();

		for (int i = 0; i < natures.length; ++i) {
			if (DocGenScriptProjectNature.NATURE_ID.equals(natures[i])) {
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
	
	public static IPath getDocGenScriptMagicDrawLocation(final IProject mdkDocGenScriptProject) {
		IPath mdInstallRoot = MDVariableInitializer.getMDInstallRootPath();
		if (mdInstallRoot.isEmpty())
			return null;
		final IPath mdkDocGenScriptProjectLocation = mdInstallRoot.addTrailingSeparator().append("DocGenUserScripts").addTrailingSeparator().append(mdkDocGenScriptProject.getName());
		return mdkDocGenScriptProjectLocation;
	}
	
	public static void moveMDKDocGenScriptsProjects(List<IProject> mdkDocGenScriptProjects) {
		for (IProject mdkDocGenScriptProject : mdkDocGenScriptProjects) {
			moveMDKDocGenScriptProject(mdkDocGenScriptProject);
		}
	}
	
	/**
	 * There should be a simpler way to do this...
	 * 
	 * @param mdkDocGenScriptProject the JPL MagicDraw DocGenScript-natured project to move to MD's install.root/DocGenUserScripts folder
	 */
	public static void moveMDKDocGenScriptProject(final IProject mdkDocGenScriptProject) {
		IPath mdInstallRoot = MDVariableInitializer.getMDInstallRootPath();
		if (mdInstallRoot.isEmpty())
			return;
		IPath newMDKDocGenScriptProjectLocation = getDocGenScriptMagicDrawLocation(mdkDocGenScriptProject);
		if (null == newMDKDocGenScriptProjectLocation)
			return;
		MDKProjectPropertyTester.moveProject(
				mdkDocGenScriptProject, 
				newMDKDocGenScriptProjectLocation,
				"the ./DocGenUserScripts folder in MD's install.root",
				mdInstallRoot);
	}
}
