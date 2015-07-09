/**
 * Copyright 2011, by the California Institute of Technology. 
 * ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged. 
 * Any commercial use must be negotiated with the Office of Technology Transfer at the California Institute of Technology.
 *
 * This software may be subject to U.S. export control laws. 
 * By accepting this software, the user agrees to comply with all applicable U.S. export laws and regulations.
 * User has the responsibility to obtain export licenses, or other export authority as may be required before exporting 
 * such information to foreign countries or providing access to foreign persons.
 * 
 * @author nicolas.f.rouquette@jpl.nasa.gov
 * @see https://mdk-env.jpl.nasa.gov/jira/browse/DMME-445
 */
package com.nomagic.magicdraw.classpath;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 *
 * Copyright 2012 Jet Propulsion Laboratory/Caltech
 */
public class MDContainer implements IClasspathContainer {
	public final static Path ID = new Path("com.nomagic.magicdraw.CLASSPATH_LIB_CONTAINER");
	public final static String DESC = "MagicDraw Classpath container";
	private final List<IPath> mdLibs = new ArrayList<IPath>();
	private IPath completePath;
	private final List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
	private IClasspathEntry[] classpathEntries = null;
	private IPath mdInstallRootPath = null;
	
	public static String MD_LIB_SEPARATOR = ",";
	
	public MDContainer(IPath path, IJavaProject project) {
		completePath = path;
		assert (MDContainer.ID.toString().equals(path.segment(0)));
		initializeIfNeeded();
	}
	
	protected void initializeIfNeeded() {
		IPath path = MDVariableInitializer.getMDInstallRootPath().addTrailingSeparator();
		if (mdInstallRootPath == null || !path.equals(mdInstallRootPath)) {
			mdInstallRootPath = path;
			initialize();
		}
	}
	
	protected void initialize() {
		mdLibs.clear();
		String mdPath = completePath.removeFirstSegments(1).toString();
		String[] mdPathParts = mdPath.split("[" + MD_LIB_SEPARATOR + "]");
		for (String mdPathPart : mdPathParts) {
			if (mdPathPart.endsWith(".jar/")) {
				IPath mdLib = new Path(mdPathPart).removeTrailingSeparator();
				mdLibs.add(mdLib);
			} else if (mdPathPart.endsWith(".jar")) {
				IPath mdLib = new Path(mdPathPart);
				mdLibs.add(mdLib);
			} else if (mdPathPart.length() > 0) {
				IPath mdPart = mdInstallRootPath.append(mdPathPart);
				File mdPartFile = mdPart.toFile();
				if (mdPartFile.exists() && mdPartFile.isDirectory()) {
					IPath mdLib = new Path(mdPathPart).addTrailingSeparator();
					mdLibs.add(mdLib);
				}
			}
		}
		
		entries.clear();
		for (int i=0; i<mdLibs.size(); i++) {
			IPath mdLib = mdLibs.get(i);
			IPath mdLibPath = mdInstallRootPath.append(mdLib);
			File mdLibFile = mdLibPath.toFile();
			if (!mdLibFile.exists() || !mdLibFile.canRead())
				continue;

			if ("jar".equals(mdLib.getFileExtension()) && mdLibFile.isFile()) {
				IClasspathEntry cpEntry = JavaCore.newLibraryEntry(mdLibPath, null, null);
				entries.add(cpEntry);
			} else if (mdLibFile.isDirectory()) {
				FilenameFilter jarFilter = new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith(".jar");
					}
				};

				File[] jars = mdLibFile.listFiles(jarFilter);
				for (File jar : jars) {
					IPath jarPath = new Path(jar.getAbsolutePath());
					IClasspathEntry cpEntry = JavaCore.newLibraryEntry(jarPath, null, null);
					entries.add(cpEntry);
				}
			}
		}
		
		classpathEntries = new IClasspathEntry[entries.size()];
		entries.toArray(classpathEntries);
	}

	public IPath getPath() {
		return completePath;
	}

	public int getKind() {
		return IClasspathContainer.K_APPLICATION;
	} 

	public String getDescription() {
		return DESC;
	}

	@Override
	public IClasspathEntry[] getClasspathEntries() {
		initializeIfNeeded();
		return classpathEntries;
	}
}
