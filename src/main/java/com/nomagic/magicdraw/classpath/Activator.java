package com.nomagic.magicdraw.classpath;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.nomagic.magicdraw.classpath"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	private MDKProjectSelectionListener mdkProjectSelectionListener;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		mdkProjectSelectionListener = new MDKProjectSelectionListener();
		mdkProjectSelectionListener.hookOnViewer(MDKProjectPropertyTester.PACKAGE_EXPLORER_VIEW_ID);
		mdkProjectSelectionListener.hookOnViewer(MDKProjectPropertyTester.PROJECT_EXPLORER_VIEW_ID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (mdkProjectSelectionListener != null) {
			mdkProjectSelectionListener.dispose();
		}
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	public static void log(int status, String message, Throwable t) {
		log(new Status(status, getDefault().getBundle().getSymbolicName(), message, t));
	}
}
