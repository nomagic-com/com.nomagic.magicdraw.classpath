package com.nomagic.magicdraw.classpath;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathVariableInitializer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;

import com.nomagic.magicdraw.classpath.preferences.PreferenceConstants;

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 *
 * Copyright 2012 Jet Propulsion Laboratory/Caltech
 */
public class MDVariableInitializer extends ClasspathVariableInitializer {
	
	public static String MD_CLASSPATH_VARIABLE = "MD"; //$NON-NLS-1$

	@Override
	public void initialize(String variable) {
		resetMDClasspathVariable();
	}

	public static IPath getMDInstallRootPath() {
		IPath md = JavaCore.getClasspathVariable(MD_CLASSPATH_VARIABLE);
		if (null != md)
			return md;
	
		return new Path("/");
	}

	public static void resetMDClasspathVariable() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String mdInstallRoot = "";
		if (store.contains(PreferenceConstants.MAGICDRAW_INSTALL_ROOT_PATH)) {
			mdInstallRoot = store.getString(PreferenceConstants.MAGICDRAW_INSTALL_ROOT_PATH);
		}
		IPath mdInstallRootPath = new Path(mdInstallRoot);
		try {
			JavaCore.setClasspathVariable(MD_CLASSPATH_VARIABLE, mdInstallRootPath, new NullProgressMonitor());
		} catch (JavaModelException e) {
			Activator.log(IStatus.ERROR, "resetMDClasspathVariable", e);
		}
	}
}
