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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;

import com.nomagic.magicdraw.classpath.preferences.PreferenceConstants;

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 *
 * Copyright 2012 Jet Propulsion Laboratory/Caltech
 */
public class MDClasspathContainerPage extends WizardPage implements IClasspathContainerPage, IClasspathContainerPageExtension {

	public static String PAGE_NAME = "MDK MD Classpath Container";
	public static String PAGE_TITLE = "Java Classpath Container for MDK MD extensions";
	public static String PAGE_DESC = "Java Classpath Container for MDK MD extensions";
	public static String MD_DIRECTORY_LABEL = "MagicDraw Installation Directory";
	public static String BROWSE_LABEL = "Browse...";
	public static String SELECT_LABEL = "Select jars...";
	public static String DIR_SELECT_LABEL = "Select the MagicDraw installation directory";

	public MDClasspathContainerPage() {
		super(PAGE_NAME, PAGE_TITLE, null);
		setDescription(PAGE_DESC);
		setPageComplete(true);
	}

	private IPath fMDInstallRootPath = MDVariableInitializer.getMDInstallRootPath();
	private Text fMDInstallRootCombo;
	private Button fMDInstallRootBrowseButton;

	private Button fMDClasspathSelectionButton;
	private ListViewer fMDClasspathEntryViewer;
	private List<IPath> fMDClasspathEntries = new ArrayList<IPath>();

	public IPath setMDInstallRootPath(String mdInstallRoot) {
		fMDInstallRootPath = new Path(mdInstallRoot);
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		try {
			JavaCore.setClasspathVariable(MDVariableInitializer.MD_CLASSPATH_VARIABLE, fMDInstallRootPath, new NullProgressMonitor());
			store.setValue(PreferenceConstants.MAGICDRAW_INSTALL_ROOT_PATH, mdInstallRoot);
			((IPersistentPreferenceStore) store).save();
		} catch (JavaModelException e) {
			Activator.log(IStatus.ERROR, "MDClasspathContainer (set)", e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			Activator.log(IStatus.ERROR, "MDClasspathContainer (save preference)", e);
		}
		return fMDInstallRootPath;
	}

	@Override
	public void setWizard(IWizard newWizard) {
		super.setWizard(newWizard);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		composite.setFont(parent.getFont());

		createDirGroup(composite);

		setControl(composite);    
	}

	private void createDirGroup(Composite parent) {
		Composite dirSelectionGroup = new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns = 1;
		dirSelectionGroup.setLayout(layout);
		dirSelectionGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL| GridData.VERTICAL_ALIGN_FILL));

		new Label(dirSelectionGroup, SWT.NONE).setText(MD_DIRECTORY_LABEL);

		fMDInstallRootCombo = new Text(dirSelectionGroup, SWT.SINGLE | SWT.BORDER);
		fMDInstallRootCombo.setEditable(false);
		fMDInstallRootCombo.setText( fMDInstallRootPath.toString() );
		{
			GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.minimumWidth = 400;
			gd.horizontalAlignment = GridData.FILL;
			fMDInstallRootCombo.setLayoutData(gd);
		}

		fMDInstallRootBrowseButton= new Button(dirSelectionGroup, SWT.PUSH);
		fMDInstallRootBrowseButton.setText( BROWSE_LABEL ); 
		fMDInstallRootBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		fMDInstallRootBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleDirBrowseButtonPressed();
			}
		}); 

		fMDClasspathSelectionButton= new Button(dirSelectionGroup, SWT.PUSH);
		fMDClasspathSelectionButton.setText( SELECT_LABEL ); 
		fMDClasspathSelectionButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		fMDClasspathSelectionButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleSelectButtonPressed();
			}
		}); 

		fMDClasspathEntryViewer = new ListViewer(dirSelectionGroup, SWT.SINGLE | SWT.BORDER);
		fMDClasspathEntryViewer.setContentProvider(new ContentProvider());
		fMDClasspathEntryViewer.setInput(fMDClasspathEntries);
		{
			GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.grabExcessVerticalSpace = true;
			gd.minimumHeight = 300;
			gd.horizontalAlignment = GridData.FILL;
			gd.verticalAlignment = GridData.FILL;
			fMDClasspathEntryViewer.getList().setLayoutData(gd);
		}

		setControl(dirSelectionGroup);
	}

	private class ContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return fMDClasspathEntries.toArray();
		}

		public void dispose() { }

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }
	}

	protected void handleDirBrowseButtonPressed() {
		DirectoryDialog dialog = new DirectoryDialog(getContainer().getShell(), SWT.SAVE);
		dialog.setMessage("Select the MagicDraw installation root folder");
		dialog.setFilterPath(fMDInstallRootCombo.getText());
		String dir = dialog.open();
		if (dir != null) {
			fMDInstallRootCombo.setText(dir);    
			setMDInstallRootPath(dir);
		}            
	}

	protected void handleSelectButtonPressed() {
		final IPath fRootPath = MDVariableInitializer.getMDInstallRootPath().addTrailingSeparator();
		final File fRoot = fRootPath.toFile();
		final List<File> initialSelection = new ArrayList<File>();
		final List<String> checkedPrefixes = new ArrayList<String>();
		final List<File> checkedSummary = new ArrayList<File>();
		for (IPath mdLibPath : fMDClasspathEntries) {
			File mdLib = mdLibPath.toFile();
			initialSelection.add(mdLib);
		}
		CheckedTreeSelectionDialog dialog = new CheckedTreeSelectionDialog(
				getContainer().getShell(), 
				new FileLabelProvider(), 
				new FileContentProvider()) {

			@Override
			public void create() {
				super.create();
				CheckboxTreeViewer treeViewer= getTreeViewer();
				for (File f : initialSelection) {
					IPath fp = fRootPath.append(f.getPath());
					File af = fp.toFile();
					treeViewer.reveal(af);
					treeViewer.setChecked(af, true);
				}
				treeViewer.collapseAll();
			}

			@Override
			protected void computeResult() {
				checkedSummary.clear();
				CheckboxTreeViewer treeViewer= getTreeViewer();
				for (Object checkedElement : treeViewer.getCheckedElements()) {
					if (treeViewer.getGrayed(checkedElement))
						continue;

					if (checkedElement instanceof File) {
						File checkedFile = (File) checkedElement;

						String checkedFilepath = checkedFile.getAbsolutePath();
						boolean isCheckedDirectory = checkedFile.isDirectory();
						if (isCheckedDirectory && !checkedFilepath.endsWith(File.separator))
							checkedFilepath += File.separator;

						boolean addToSummary = true;
						for (String checkedPrefix : checkedPrefixes) {
							if (checkedPrefix.equals(checkedFilepath) || (!isCheckedDirectory && checkedFilepath.startsWith(checkedPrefix)))
								addToSummary = false;
						}

						if (addToSummary) {
							checkedSummary.add(checkedFile);
							if (checkedFile.isDirectory())
								checkedPrefixes.add(checkedFilepath);
						}
					}
				}
			}
		};
		dialog.setTitle("MagicDraw Library Classpath Selection");
		dialog.setMessage("Select the libraries from the MagicDraw installation root:");
		dialog.setInput(fRoot);
		dialog.setContainerMode(true);

		dialog.open();
		int code = dialog.getReturnCode();
		if (Dialog.OK == code) {
			String root = MDVariableInitializer.getMDInstallRootPath().toString();
			int rootPrefix = root.length() + 1;
			fMDClasspathEntries.clear();
			for (File file : checkedSummary) {
				String path = file.getAbsolutePath();
				String lib = path.substring(rootPrefix);
				IPath mdLibpath = new Path(lib);
				fMDClasspathEntries.add(mdLibpath);
			}
			fMDClasspathEntryViewer.setInput(fMDClasspathEntries);
		}

	}

	private static class FileLabelProvider extends LabelProvider {

		@Override
		public String getText(Object element) {
			if (element instanceof File)
				return ((File) element).getName();

			return element.toString();
		}
	}

	private static final Object[] EMPTY= new Object[0];


	private static Object[] fileChildren(File f) {
		if (f.isFile())
			return EMPTY;

		ArrayList<File> children = new ArrayList<File>();
		File[] fd1s = f.listFiles();
		if (null != fd1s)
			for (File fd1 : fd1s) {
				boolean ok = false;
				if (fd1.isFile()) {
					if (fd1.getName().endsWith(".jar")) 
					{ ok = true; }
				} else {
					assert (fd1.isDirectory());
					File[] fd2s = fd1.listFiles();
					if (null != fd2s)
						for (File fd2 : fd2s) {
							if (ok) break;
							if (fd2.isFile()) {
								if (fd2.getName().endsWith(".jar")) 
								{ ok = true; break; }
							} else {
								assert (fd2.isDirectory());
								File[] fd3s = fd2.listFiles();
								if (null != fd3s)
									for (File fd3 : fd3s) {
										if (fd3.isFile() && fd3.getName().endsWith(".jar"))
										{ ok = true; break; }
									}
							}
						}
				}
				if (ok)
					children.add(fd1);
			}
		return children.toArray();
	}

	private static class FileContentProvider implements ITreeContentProvider {



		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof File) {
				return fileChildren((File) parentElement);
			}
			return EMPTY;
		}

		public Object getParent(Object element) {
			if (element instanceof File) {
				return ((File) element).getParentFile();
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		public Object[] getElements(Object element) {
			return getChildren(element);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	private static Comparator<IPath> IPATH_COMPARATOR = new Comparator<IPath>() {
		@Override
		public int compare(IPath p1, IPath p2) {
			return p1.toString().compareTo(p2.toString());
		}        	
	};

	public void setSelection(IClasspathEntry containerEntry) {
		if(containerEntry != null) {
			IPath path = containerEntry.getPath();
			String containerID = path.segment(0);
			if (MDContainer.ID.toString().equals(containerID)) {
				String mdPath = path.removeFirstSegments(1).toString();
				String[] mdPathParts = mdPath.split("[" + MDContainer.MD_LIB_SEPARATOR + "]");
				fMDInstallRootPath = MDVariableInitializer.getMDInstallRootPath();
				fMDClasspathEntries.clear();
				for (int i=1; i<mdPathParts.length; i++) {
					IPath mdLib = new Path(mdPathParts[i]).removeTrailingSeparator();
					fMDClasspathEntries.add(mdLib);
				}
				Collections.sort(fMDClasspathEntries, IPATH_COMPARATOR);
			}
		}        
	} 

	public IClasspathEntry getSelection() {
		IPath path = MDContainer.ID;
		for (IPath classpathEntry : fMDClasspathEntries) {
			path = path.append(MDContainer.MD_LIB_SEPARATOR + classpathEntry.toString());
		}
		IClasspathEntry mdClasspath = JavaCore.newContainerEntry(path, null, null, true);
		return mdClasspath;
	}

	@Override
	public boolean finish() {
		File installRoot = MDVariableInitializer.getMDInstallRootPath().toFile();
		if (!(installRoot.exists() && installRoot.isDirectory()))
			return false;

		File libDir = new File(installRoot.getAbsolutePath() + File.separator + "lib");
		if (!(libDir.exists() && libDir.isDirectory()))
			return false;

		File mdAPI = new File(libDir.getAbsolutePath() + File.separator + "md_api.jar");
		if (!(mdAPI.exists() && mdAPI.isFile() && mdAPI.canRead()))
			return false;

		return true;
	}

	@Override
	public void initialize(IJavaProject project, IClasspathEntry[] currentEntries) {
	}
}
