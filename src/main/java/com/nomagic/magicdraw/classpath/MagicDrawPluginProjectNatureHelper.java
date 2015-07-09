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
public class MagicDrawPluginProjectNatureHelper {

	public static boolean hasProjectNature(IProject project) {
		if (null == project)
			throw new IllegalArgumentException("MDKPluginProjectNatureHelper.hasProjectNature()");
		
		try {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();

			for (int i = 0; i < natures.length; ++i) {
				if (MagicDrawPluginProjectNature.NATURE_ID.equals(natures[i]))
					return true;
			}
			
			return false;
			
		} catch (CoreException e) {
			throw new IllegalArgumentException("MDKPluginProjectNatureHelper.hasProjectNature()", e);
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
			if (MagicDrawPluginProjectNature.NATURE_ID.equals(natures[i])) {
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
		newNatures[natures.length] = MagicDrawPluginProjectNature.NATURE_ID;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);
	}

	public static void addNature(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();

		for (int i = 0; i < natures.length; ++i) {
			if (MagicDrawPluginProjectNature.NATURE_ID.equals(natures[i])) {
				return;
			}
		}

		// Add the nature
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = MagicDrawPluginProjectNature.NATURE_ID;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);
	}
	
	public static void removeNature(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();

		for (int i = 0; i < natures.length; ++i) {
			if (MagicDrawPluginProjectNature.NATURE_ID.equals(natures[i])) {
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
	
	public static IPath getPluginMagicDrawLocation(final IProject mdkPluginsProject) {
		IPath mdInstallRoot = MDVariableInitializer.getMDInstallRootPath();
		if (mdInstallRoot.isEmpty())
			return null;
		final IPath mdkPluginProjectLocation = mdInstallRoot.addTrailingSeparator().append("plugins").addTrailingSeparator().append(mdkPluginsProject.getName());
		return mdkPluginProjectLocation;
	}
	
	public static void moveMDKPluginsProjects(List<IProject> mdkPluginsProjects) {
		for (IProject mdkPluginsProject : mdkPluginsProjects) {
			moveMDKPluginsProject(mdkPluginsProject);
		}
	}
	
	/**
	 * There should be a simpler way to do this...
	 * 
	 * @param mdkPluginsProject the JPL MagicDraw Plugin-natured project to move to MD's install.root/mdk.scripts folder
	 */
	public static void moveMDKPluginsProject(final IProject mdkPluginsProject) {
		IPath mdInstallRoot = MDVariableInitializer.getMDInstallRootPath();
		if (mdInstallRoot.isEmpty())
			return;
		IPath newMDKPluginProjectLocation = getPluginMagicDrawLocation(mdkPluginsProject);
		if (null == newMDKPluginProjectLocation)
			return;
		MDKProjectPropertyTester.moveProject(
				mdkPluginsProject, 
				newMDKPluginProjectLocation,
				"the ./plugins folder in MD's install.root",
				mdInstallRoot);
	}

}
