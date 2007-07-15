/*
 * Just4Kix - Image Viewer and Comic Book Reader
 *
 * EventManager.java
 * Created on 4th May, 2007
 * Author: Naveen Belkale
 */
package com.viewer;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

public class EventManager implements KeyListener, ActionListener, WindowStateListener{
	public static final String OPEN_FILE = "Open File/Directory";
	public static final String QUIT = "Quit";
	public static final String NEXT = "Next";
	public static final String PREV = "Prev";
	public static final String NEXT_PAGE = "Next Page";
	public static final String PREV_PAGE = "Prev Page";
	public static final String GO_TO = "Go To";
	public static final String CONTINUE = "Continue";
	public static final String PAGE_NO = "Page Number";
	public static final String VIEW_SIZE = "View Size";
	public static final String FIT_IMAGE = "Fit Image";
	public static final String FIT_HEIGHT = "Fit Height";
	public static final String FIT_WIDTH = "Fit Width";
	public static final String ACTUAL_SIZE = "Actual Size";
	public static final String CUSTOM_ZOOM = "Zoomed image";
	public static final String ZOOM_IN = "Zoom In";
	public static final String ZOOM_OUT = "Zoom Out";
	public static final String ROTATE_LEFT = "Rotate Anitclockwise";
	public static final String ROTATE_RIGHT = "Rotate Clockwise";
	public static final String SLIDESHOW = "Start Slideshow";
	public static final String HELP = "Help";
	
	public static final String SETTINGS = "Settings";
	public static final String APPDIR = "Just4Kix Home";
	public static final String SLIDESHOW_DELAY = "Slideshow Delay";
	public static final String UNRAR_CMD = "Unrar Command";
	public static final String UNRAR_ARG = "Unrar Argument";
	
	public static final String IMAGE_HOME = "Images Home";
	public static final String RECENT_FILE = "Recent File";
	
	private static final int MAX_RECENT = 7;
	//Used button mnemonics and shortcuts: o, space, backspace, pageup, pagedown, 
	// u, d, l, r, g, c, v, m, t, q, s, h
	
	private ImageViewer viewer;
	private ImageComponent imgComp;
	private Properties appProperties;
	private JFileChooser fc;
	
	//Settings dialog buttons
	private JDialog settingsDialog;
	private JButton imgHomeButton, saveButton, cancelButton;
	private JTextField imgHomeText;
	private JSpinner slideshowSpinner;
	private JTextField unrarCmdText, unrarArgText;
	private JRadioButtonMenuItem customZoom;
	private JMenuItem[] recentFiles;
	private List <String> recentFilesList;
	private JFrame mainFrame;
	
	private int slideshowDelay; //slideshow delay in seconds
	private boolean onSlideshow; 
	
	public EventManager(ImageViewer viewer, JFrame mainFrame){
		this.viewer = viewer;
		this.mainFrame = mainFrame;
		
		imgComp = viewer.getImageComponent();
		appProperties = viewer.getAppProperties();
		
		slideshowDelay = 5;
		String ssdelayStr = appProperties.getProperty(SLIDESHOW_DELAY);
		if(ssdelayStr != null){
			slideshowDelay = Integer.parseInt(ssdelayStr);
		}
		onSlideshow = false;
		
		String imageHome = appProperties.getProperty(IMAGE_HOME);
		fc = new JFileChooser(imageHome);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setMultiSelectionEnabled(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", "jpg", "jpeg", 
				"gif", "bmp", "png", "cbr", "cbz", "rar", "zip", "wbmp");
		fc.setFileFilter(filter);
		
		
		recentFilesList = new ArrayList<String>(MAX_RECENT);
		for(int i=0; i < MAX_RECENT; i++){
			String rf = appProperties.getProperty(RECENT_FILE + " " + i);
			if(rf != null){
				recentFilesList.add(rf);
			}
		}
		addPopupMenu();
		
	}
	private void addPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();

		JMenuItem openFile = new JMenuItem(OPEN_FILE, KeyEvent.VK_O);
		openFile.addActionListener(this);
		popupMenu.add(openFile);

		popupMenu.addSeparator();

		JMenuItem next = new JMenuItem(NEXT, KeyEvent.VK_SPACE);
		next.addActionListener(this);
		popupMenu.add(next);

		JMenuItem prev = new JMenuItem(PREV, KeyEvent.VK_BACK_SPACE);
		prev.addActionListener(this);
		popupMenu.add(prev);

		popupMenu.addSeparator();

		JMenuItem nextPage = new JMenuItem(NEXT_PAGE, KeyEvent.VK_PAGE_DOWN);
		nextPage.addActionListener(this);
		popupMenu.add(nextPage);

		JMenuItem prevPage = new JMenuItem(PREV_PAGE, KeyEvent.VK_PAGE_UP);
		prevPage.addActionListener(this);
		popupMenu.add(prevPage);

		JMenuItem goTo = new JMenuItem(GO_TO, KeyEvent.VK_G);
		goTo.addActionListener(this);
		popupMenu.add(goTo);

		JMenuItem cont = new JMenuItem(CONTINUE, KeyEvent.VK_C);
		cont.addActionListener(this);
		popupMenu.add(cont);

		popupMenu.addSeparator();

		JMenu viewSize = new JMenu(VIEW_SIZE);
		viewSize.setMnemonic(KeyEvent.VK_V);
		popupMenu.add(viewSize);

		ButtonGroup sizeGroup = new ButtonGroup();
		JRadioButtonMenuItem fitImage = new JRadioButtonMenuItem(FIT_IMAGE);
		if (imgComp.getCurState() == ImageComponent.FIT_IMAGE) {
			fitImage.setSelected(true);
		}
		fitImage.addActionListener(this);
		sizeGroup.add(fitImage);
		viewSize.add(fitImage);

		JRadioButtonMenuItem fitWidth = new JRadioButtonMenuItem(FIT_WIDTH);
		if (imgComp.getCurState() == ImageComponent.FIT_WIDTH) {
			fitWidth.setSelected(true);
		}
		fitWidth.addActionListener(this);
		sizeGroup.add(fitWidth);
		viewSize.add(fitWidth);

		JRadioButtonMenuItem fitHeight = new JRadioButtonMenuItem(FIT_HEIGHT);
		if (imgComp.getCurState() == ImageComponent.FIT_HEIGHT) {
			fitHeight.setSelected(true);
		}
		fitHeight.addActionListener(this);
		sizeGroup.add(fitHeight);
		viewSize.add(fitHeight);

		JRadioButtonMenuItem actualSize = new JRadioButtonMenuItem(ACTUAL_SIZE);
		if (imgComp.getCurState() == ImageComponent.ACTUAL_SIZE) {
			actualSize.setSelected(true);
		}
		actualSize.addActionListener(this);
		sizeGroup.add(actualSize);
		viewSize.add(actualSize);

		customZoom = new JRadioButtonMenuItem(CUSTOM_ZOOM);
		if (imgComp.getCurState() == ImageComponent.CUSTOM_ZOOM) {
			customZoom.setSelected(true);
		}
		customZoom.addActionListener(this);
		sizeGroup.add(customZoom);
		viewSize.add(customZoom);

		popupMenu.addSeparator();

		JMenuItem zoomIn = new JMenuItem(ZOOM_IN, KeyEvent.VK_D);
		zoomIn.addActionListener(this);
		popupMenu.add(zoomIn);

		JMenuItem zoomOut = new JMenuItem(ZOOM_OUT, KeyEvent.VK_U);
		zoomOut.addActionListener(this);
		popupMenu.add(zoomOut);

		JMenuItem rotateLeft = new JMenuItem(ROTATE_LEFT, KeyEvent.VK_L);
		rotateLeft.addActionListener(this);
		popupMenu.add(rotateLeft);

		JMenuItem rotateRight = new JMenuItem(ROTATE_RIGHT, KeyEvent.VK_R);
		rotateRight.addActionListener(this);
		popupMenu.add(rotateRight);

		JMenuItem slideshow = new JMenuItem(SLIDESHOW, KeyEvent.VK_S);
		slideshow.addActionListener(this);
		popupMenu.add(slideshow);

		popupMenu.addSeparator();

		JMenuItem settings = new JMenuItem(SETTINGS);
		settings.addActionListener(this);
		popupMenu.add(settings);

		popupMenu.addSeparator();

		JMenuItem recentLabel = new JMenu("Recent Files");
		popupMenu.add(recentLabel);
		recentFiles = new JMenuItem[MAX_RECENT];
		for(int i = 0; i < MAX_RECENT; i++){
			recentFiles[i] = new JMenuItem("Not Set");
			recentFiles[i].addActionListener(this);
			recentFiles[i].setVisible(false);
			recentLabel.add(recentFiles[i]);
		}
		updateRecentFilesMenu();
		
		popupMenu.addSeparator();
		JMenuItem help = new JMenuItem(HELP, KeyEvent.VK_H);
		help.addActionListener(this);
		popupMenu.add(help);
		
		JMenuItem quit = new JMenuItem(QUIT, KeyEvent.VK_Q);
		quit.addActionListener(this);
		popupMenu.add(quit);

		MouseListener popupListener = new PopupListener(popupMenu);
		imgComp.addMouseListener(popupListener);
	}
	private void zoomIn() {
		customZoom.setSelected(true);
		imgComp.zoomInImage();
	}

	private void zoomOut() {
		customZoom.setSelected(true);
		imgComp.zoomOutImage();
	}

	private void updateRecentFilesMenu(){
		if(recentFilesList.size() > 0){
			for(int i=0; i < recentFilesList.size(); i++){
				String str = recentFilesList.get(i);
				if (str.length() > 50) {
					recentFiles[i].setText(".."	+ str.substring(str.length() - 40));
				} else {
					recentFiles[i].setText(str);
				}
				//System.out.println("Updated item " + i + " with " + str);
				recentFiles[i].setVisible(true);
			}
		}
	}
	
	private JDialog getSettingsDialog(){
		//has to specify mainFrame in JDialog constructor. Otherwise 
		// dialog is not shown in Windows OS 
		JDialog dialog = new JDialog(mainFrame, "Settings");
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(5, 5, 5, 5);
		c.weightx = 0.5;
		c.weighty = 0.5;
		
		JLabel slideshowLabel = new JLabel("Slideshow Delay ( in Seconds )");
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		dialogPanel.add(slideshowLabel, c);
		String slideshowStr = appProperties.getProperty(SLIDESHOW_DELAY);
		if(slideshowStr == null){
			slideshowStr = "5";
		}
		int secs = Integer.parseInt(slideshowStr);
		SpinnerModel model = new SpinnerNumberModel(secs, 0, 1000, 1);
		slideshowSpinner = new JSpinner(model);
		c.gridx = 3;
		c.gridy = 0;
		c.gridwidth = 1;
		dialogPanel.add(slideshowSpinner, c);
		
		//Image Home Directory
		JLabel alabel = new JLabel("Images Home");
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		dialogPanel.add(alabel, c);
		String homeStr = appProperties.getProperty(IMAGE_HOME);
		if(homeStr == null){
			homeStr = System.getProperty("user.home");
		}
		imgHomeText = new JTextField(homeStr, 20);
		imgHomeText.setEditable(false);
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 2;
		dialogPanel.add(imgHomeText, c);
		imgHomeButton = new JButton("Set");
		imgHomeButton.addActionListener(this);
		c.gridx = 3;
		c.gridy = 1;
		c.gridwidth = 1;
		dialogPanel.add(imgHomeButton, c);
		
		c.gridwidth = 1;
		alabel = new JLabel("Unrar Command");
		c.gridx = 0;
		c.gridy = 2;
		dialogPanel.add(alabel, c);
		String unrarCmdStr = appProperties.getProperty(UNRAR_CMD);
		if(unrarCmdStr == null){
			String os = System.getProperty("os.name");
			if(os.startsWith("Windows")){
				unrarCmdStr = FileProcessor.WINDOWS_UNRAR;
			} else {
				unrarCmdStr = FileProcessor.UNIX_UNRAR;
			}
		}
		unrarCmdText = new JTextField(unrarCmdStr, 15);
		c.gridx = 1;
		c.gridy = 2;
		dialogPanel.add(unrarCmdText, c);
		alabel = new JLabel("Arg", SwingConstants.RIGHT);
		c.gridx = 2;
		c.gridy = 2;
		dialogPanel.add(alabel, c);
		String unrarArgStr = appProperties.getProperty(UNRAR_ARG);
		if(unrarArgStr == null){
			unrarArgStr = "e";
		}
		unrarArgText = new JTextField(unrarArgStr);
		c.gridx = 3;
		c.gridy = 2;
		dialogPanel.add(unrarArgText, c);
		
		FlowLayout layout = new FlowLayout();
		layout.setHgap(50);
		JPanel scPanel = new JPanel(layout);
		saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		scPanel.add(saveButton, c);
		cancelButton = new JButton("Cancel");
		Dimension dim = cancelButton.getPreferredSize();
		saveButton.setPreferredSize(dim);
		cancelButton.addActionListener(this);
		scPanel.add(cancelButton, c);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 4;
		dialogPanel.add(scPanel, c);
		
		dialog.add(dialogPanel);
		dialog.pack();
		
		Dimension d = dialog.getPreferredSize();
		Rectangle rect = mainFrame.getBounds();
		dialog.setLocation(rect.x + (rect.width - d.width)/2, 
				rect.y + (rect.height - d.height)/2);
		
		dialog.setVisible(true);
		//System.out.println(dialog.getBounds());
		return dialog;
	}
	private void checkNUpdateRecent(String filePath){
		int i = 0;
		int len = recentFilesList.size();
		for(i = 0; i< len; i++){
			if(recentFilesList.get(i).equals(filePath)){
				break;
			}
		}
		if(i < len){
			recentFilesList.remove(i);
		} else if((i == len) && (len == MAX_RECENT)){
			recentFilesList.remove(len - 1);
		}
		recentFilesList.add(0, filePath);
		updateRecentFilesMenu();
	}
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		switch (keyCode) {
		case KeyEvent.VK_PAGE_DOWN:
			imgComp.showNextImage();
			break;
		case KeyEvent.VK_PAGE_UP:
			imgComp.showPreviousImage();
			break;
		case KeyEvent.VK_SPACE:
			imgComp.whatNext();
			break;
		case KeyEvent.VK_BACK_SPACE:
			imgComp.whatPrev();
			break;
		case KeyEvent.VK_O:
			int returnVal = fc.showOpenDialog(imgComp);
			if(returnVal == JFileChooser.APPROVE_OPTION){
				File file = fc.getSelectedFile();
				imgComp.setImageFiles(file);
				checkNUpdateRecent(file.getAbsolutePath());
			}
			break;
		case KeyEvent.VK_D:
			zoomIn();
			break;
		case KeyEvent.VK_U:
			zoomOut();
			break;
		case KeyEvent.VK_L:
			imgComp.rotateLeft();
			break;
		case KeyEvent.VK_R:
			imgComp.rotateRight();
			break;
		case KeyEvent.VK_G:
			String pageStr = JOptionPane.showInputDialog("Enter Page of " + imgComp.getNumPages() 
					+ " Pages", new Integer(1));
			if(pageStr != null){
				try{
					int pageNum = Integer.parseInt(pageStr);
					imgComp.showImage(pageNum - 1);
				} catch(NumberFormatException exp){
					exp.printStackTrace();
				}
			}
			break;
		case KeyEvent.VK_H:
			JOptionPane.showMessageDialog(mainFrame, getHelpString());
			break;
		case KeyEvent.VK_Q:
			for(int i = 0; i < recentFilesList.size(); i++){
				appProperties.setProperty(RECENT_FILE + " " + i, recentFilesList.get(i));
			}
			viewer.onExit();
			break;
		}
	}

	private String getHelpString(){
		String help = null;
		 try {
			 ClassLoader cl = this.getClass().getClassLoader();
			 BufferedInputStream is = new BufferedInputStream(cl.getResourceAsStream("doc/help.but"));
			 StringBuilder inputStr = new StringBuilder(1024);
			 byte[] buffer = new byte[1024];
			 int len;
			 while((len = is.read(buffer, 0, 1024)) != -1){
				 inputStr.append(new String(buffer, 0, len));
			 }
			 help = inputStr.toString();
			 is.close();
		 } catch (IOException e) {
			e.printStackTrace();
		}
		return help;
	}
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	public void keyTyped(KeyEvent e) {
		//System.out.println("keyPressed event called");
		if(onSlideshow){
			imgComp.stopSlideshow();
			onSlideshow = false;
		}
		char input = e.getKeyChar();
		if(input == '@'){
			imgComp.just4Kix();
		}
	}

	public void actionPerformed(ActionEvent e) {
		String actionInfo = e.getActionCommand();
		//System.out.println("actionPerformed called");
		if(onSlideshow){
			imgComp.stopSlideshow();
			onSlideshow = false;
		}
		if(actionInfo.equals(OPEN_FILE)){
			int returnVal = fc.showOpenDialog(imgComp);
			if(returnVal == JFileChooser.APPROVE_OPTION){
				File file = fc.getSelectedFile();
				imgComp.setImageFiles(file);
				checkNUpdateRecent(file.getAbsolutePath());
			}
		} else if(actionInfo.equals(NEXT)){
			imgComp.whatNext();
		} else if(actionInfo.equals(PREV)){
			imgComp.whatPrev();
		} else if(actionInfo.equals(NEXT_PAGE)){
			imgComp.showNextImage();
		} else if(actionInfo.equals(PREV_PAGE)){
			imgComp.showPreviousImage();
		} else if(actionInfo.equals(FIT_IMAGE)){
			imgComp.setCurState(ImageComponent.FIT_IMAGE);
			appProperties.setProperty(VIEW_SIZE, Integer.toString(ImageComponent.FIT_IMAGE));
		} else if(actionInfo.equals(FIT_WIDTH)){
			imgComp.setCurState(ImageComponent.FIT_WIDTH);
			appProperties.setProperty(VIEW_SIZE, Integer.toString(ImageComponent.FIT_WIDTH));
		} else if(actionInfo.equals(FIT_HEIGHT)){
			imgComp.setCurState(ImageComponent.FIT_HEIGHT);
			appProperties.setProperty(VIEW_SIZE, Integer.toString(ImageComponent.FIT_HEIGHT));
		} else if(actionInfo.equals(ACTUAL_SIZE)){
			imgComp.setCurState(ImageComponent.ACTUAL_SIZE);
			appProperties.setProperty(VIEW_SIZE, Integer.toString(ImageComponent.ACTUAL_SIZE));
		} else if(actionInfo.equals(CUSTOM_ZOOM)){
			imgComp.setCurState(ImageComponent.CUSTOM_ZOOM);
		} else if(actionInfo.equals(CONTINUE)){
			String pageStr = appProperties.getProperty(PAGE_NO);
			String recentFile = appProperties.getProperty(RECENT_FILE);
			if((pageStr != null) && (recentFile != null)){
				int pageNum = Integer.parseInt(pageStr);
				imgComp.setImageFiles(new File(recentFile));
				imgComp.showImage(pageNum);
				checkNUpdateRecent(recentFile);
			}
		} else if(actionInfo.equals(GO_TO)){
			String pageStr = JOptionPane.showInputDialog("Enter Page of " + imgComp.getNumPages() 
					+ " Pages", new Integer(1));
			if(pageStr != null){
				try{
					int pageNum = Integer.parseInt(pageStr);
					imgComp.showImage(pageNum - 1);
				} catch(NumberFormatException exp){
					exp.printStackTrace();
				}
			}
			
		} else if(actionInfo.equals(QUIT)){
			for(int i = 0; i < recentFilesList.size(); i++){
				appProperties.setProperty(RECENT_FILE + " " + i, recentFilesList.get(i));
			}
			viewer.onExit();
		} else if(actionInfo.equals(ZOOM_IN)){
			zoomIn();
		} else if(actionInfo.equals(ZOOM_OUT)){
			zoomOut();
		} else if(actionInfo.equals(ROTATE_LEFT)){
			imgComp.rotateLeft();
		} else if(actionInfo.equals(ROTATE_RIGHT)){
			imgComp.rotateRight();
		} else if(actionInfo.equals(SLIDESHOW)){
			onSlideshow = true;
			imgComp.startSlideshow(slideshowDelay);
		} else if(actionInfo.equals(HELP)){
			JOptionPane.showMessageDialog(mainFrame, getHelpString());
		} else if(actionInfo.equals(SETTINGS)){
			settingsDialog = getSettingsDialog();
		} else {
			//Hacks checking the button mnemonic as I cudnt find a better way
			AbstractButton source = (AbstractButton)e.getSource();
			if(source.equals(imgHomeButton)){
				//System.out.println("Setting home directory");
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(imgComp);
				if(returnVal == JFileChooser.APPROVE_OPTION){
					imgHomeText.setText(fc.getSelectedFile().getAbsolutePath());
				}
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				
			} else if(source.equals(cancelButton)){
				settingsDialog.dispose();
			} else if(source.equals(saveButton)){
				String imgHomeStr = imgHomeText.getText();
				fc.setCurrentDirectory(new File(imgHomeStr));
				appProperties.setProperty(IMAGE_HOME, imgHomeStr);
				
				int ssdSecs = (Integer)slideshowSpinner.getValue();
				if(ssdSecs > 0){
					slideshowDelay = ssdSecs;
					appProperties.setProperty(SLIDESHOW_DELAY, Integer.toString(ssdSecs));
				}
				
				String unrarCmd = unrarCmdText.getText();
				if(unrarCmd != null){
					System.setProperty("unrar.cmd", unrarCmd);
					appProperties.setProperty(UNRAR_CMD, unrarCmd);
					String unrarArg = unrarArgText.getText();
					if(unrarArg != null){
						System.setProperty("unrar.arg", unrarArg);
						appProperties.setProperty(UNRAR_ARG, unrarArg);
					}
				}
				settingsDialog.dispose();
			} else {
				for(int i = 0; i< recentFilesList.size(); i++){
					JMenuItem recentItem = recentFiles[i];
					if(source == recentItem){ //Mnemonic for recent file is C
						//System.out.println("Opening recent file:" + actionInfo);
						String recentStr = recentFilesList.get(i);
						if(recentStr != null){
							imgComp.setImageFiles(new File(recentStr));
							checkNUpdateRecent(recentStr);
						}
					}
				}
			}
		}
	}
	
	public void windowStateChanged(WindowEvent e) {
		if(e.getNewState() == WindowEvent.WINDOW_CLOSING){
			for(int i = 0; i < recentFilesList.size(); i++){
				appProperties.setProperty(RECENT_FILE + " " + i, recentFilesList.get(i));
			}
			viewer.onExit();
		}
	}
	class PopupListener extends MouseAdapter {
		JPopupMenu popup;

		PopupListener(JPopupMenu popupMenu) {
			popup = popupMenu;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

}
