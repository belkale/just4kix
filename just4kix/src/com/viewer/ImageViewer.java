/*
 * Just4Kix - Image Viewer and Comic Book Reader
 *
 * ImageViewer.java
 * Created on 4th May, 2007
 * Author: Naveen Belkale
 */
package com.viewer;

import static com.viewer.EventManager.APPDIR;
import static com.viewer.EventManager.PAGE_NO;
import static com.viewer.EventManager.RECENT_FILE;
import static com.viewer.EventManager.UNRAR_ARG;
import static com.viewer.EventManager.UNRAR_CMD;
import static com.viewer.EventManager.VIEW_SIZE;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

public class ImageViewer {

	private ImageComponent imgComp;

	private Properties appProperties;

	
	private final String appDir = System.getProperty("user.home")
			+ System.getProperty("file.separator") + ".just4kix";

	private EventManager eventManager;

	public ImageViewer() {

		// load settings
		loadProperties();

		JFrame mainFrame = new JFrame("Just4Kix : Image Viewer");
		mainFrame.setUndecorated(true);
		mainFrame.setResizable(false);

		GraphicsDevice gdevice = GraphicsEnvironment
				.getLocalGraphicsEnvironment().getDefaultScreenDevice();

		Rectangle maxRect = gdevice.getDefaultConfiguration().getBounds();
		imgComp = new ImageComponent(null, new Dimension(maxRect.width - 5,
				maxRect.height - 5));
		imgComp.setFocusable(true);
		String viewStr = appProperties.getProperty(VIEW_SIZE);
		if (viewStr != null) {
			imgComp.setCurState(Integer.parseInt(viewStr));
		}
		
		eventManager = new EventManager(this, mainFrame);
		imgComp.addKeyListener(eventManager);
		mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowStateListener(eventManager);
		
		JScrollPane scrollPane = new JScrollPane(imgComp);
		mainFrame.add(scrollPane);

		if (gdevice.isFullScreenSupported()) {
			gdevice.setFullScreenWindow(mainFrame);
		}

		mainFrame.pack();
		mainFrame.setVisible(true);
	}

	

	public ImageComponent getImageComponent() {
		return imgComp;
	}

	public Properties getAppProperties() {
		return appProperties;
	}

	
	public void onExit() {
		saveProperties();
		System.exit(0);
	}

	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ImageViewer imgViewer = new ImageViewer();
			}
		});
	}

	private void loadProperties() {
		/*
		 * Properties defaultProps = new Properties(); try { FileInputStream in =
		 * new FileInputStream("defaultconf.xml"); defaultProps.loadFromXML(in);
		 * in.close(); } catch (Exception e) {
		 * System.out.println(e.getMessage()); }
		 */

		appProperties = new Properties();

		File dirFile = new File(appDir);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}

		final String appConfFile = appDir
				+ System.getProperty("file.separator") + "conf.xml";
		try {
			FileInputStream in = new FileInputStream(appConfFile);
			appProperties.loadFromXML(in);
			in.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		appProperties.setProperty(APPDIR, appDir);
		
		String unrarCmd = appProperties.getProperty(UNRAR_CMD);
		if(unrarCmd != null){
			System.setProperty("unrar.cmd", unrarCmd);
			String unrarArg = appProperties.getProperty(UNRAR_ARG);
				if(unrarArg != null){
				System.setProperty("unrar.arg", unrarArg);
			}
		}
	}

	private void saveProperties() {
		final String appConfFile = appDir
				+ System.getProperty("file.separator") + "conf.xml";
		String lastFile = imgComp.getMainFile();
		if (lastFile != null) {
			appProperties.setProperty(RECENT_FILE, lastFile);
			appProperties.setProperty(PAGE_NO, Integer.toString(imgComp
					.getCurIndex()));
		}
		try {
			FileOutputStream os = new FileOutputStream(appConfFile);
			appProperties.storeToXML(os, "Just4Kix Image Viewer Properties");
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
