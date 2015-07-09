package com.nomagic.magicdraw.classpath.preferences;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.nomagic.magicdraw.classpath.Activator;
import com.nomagic.magicdraw.classpath.MDVariableInitializer;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 * 
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 *
 * Copyright 2012 Jet Propulsion Laboratory/Caltech
 */
public class MDPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	private DirectoryFieldEditor fMDInstallRootDirEditor = null;
	private boolean hasApplied = false;
	
	public MDPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("MagicDraw Preference Page");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		fMDInstallRootDirEditor = new DirectoryFieldEditor(
				PreferenceConstants.MAGICDRAW_INSTALL_ROOT_PATH, 
				"MagicDraw Installation Root Directory:", 
				getFieldEditorParent());
		addField(fMDInstallRootDirEditor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	@Override
	public boolean performOk() {
		boolean retVal = super.performOk() && hasApplied;
		return retVal;
	}

	@Override
	public void performApply() {
		try {
			fMDInstallRootDirEditor.store();
			IPath mdInstallRoot = new Path(fMDInstallRootDirEditor.getStringValue());
			JavaCore.setClasspathVariable(MDVariableInitializer.MD_CLASSPATH_VARIABLE, mdInstallRoot, new NullProgressMonitor());
			IPreferenceStore store = this.getPreferenceStore();
			((IPersistentPreferenceStore) store).save();
			hasApplied = true;
		} catch (IOException e) {
			Activator.log(IStatus.ERROR, "MDPreferencePage (save)", e);
		} catch (JavaModelException e) {
			Activator.log(IStatus.ERROR, "MDPreferencePage (save)", e);
		}
	}
}