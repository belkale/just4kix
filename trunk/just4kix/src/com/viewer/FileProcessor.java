/*
 * Just4Kix - Image Viewer and Comic Book Reader
 *
 * FileProcessor.java
 * Created on 4th May, 2007
 * Author: Naveen Belkale
 */
package com.viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.swing.Timer;

public class FileProcessor {
	public static String WINDOWS_UNRAR = "C:\\Program Files\\WinRAR\\rar";
	public static String UNIX_UNRAR = "unrar";
	private static int BUFFER_COUNT = 3;
	
	private File [] imageFiles;
	private String tmpDir;
	private ImageObj[] bufferedImages;
	private Timer bufferTimer;
	
	/*
	 * Prefetching of images is done in the following way.
	 * a. 3 buffers are maintained where the middle buffer maintains the last requested image
	 * b. The remaining two buffers are supposed to hold the previous and next image
	 * c. A timer is set for 100ms delay once we do a getImage
	 * d. The timer calls preLoadImages() which tries to load the next(previous) image 
	 * e. Note that if we step through the images either in forward or backward direction
	 *    only one image is prefetched each time
	 */
	
	public FileProcessor(File srcFile) throws IOException{
		imageFiles = null;
		tmpDir = System.getProperty("java.io.tmpdir");
		
		bufferedImages = new ImageObj[BUFFER_COUNT];
		if(srcFile == null){
			return;
		}

		boolean tryZip = false;
		if (srcFile.isDirectory()) {
			imageFiles = srcFile.listFiles();
		} else {
			String fileName = srcFile.getName();
			if(fileName.endsWith(".cbz") || fileName.endsWith(".CBZ") 
					|| fileName.endsWith(".zip") || fileName.endsWith(".ZIP")){
				try{
					tryZip = true;
					imageFiles = getZippedFiles(srcFile);
				} catch(IOException e){
					e.printStackTrace();
				}
			} else if(fileName.endsWith(".cbr") || fileName.endsWith(".CBR") 
					|| fileName.endsWith(".rar") || fileName.endsWith(".RAR")) {
				imageFiles = getRaredFiles(srcFile);
			} 
			if((imageFiles == null) || (imageFiles.length == 0)){
				String fileSuffix = fileName.substring(fileName.lastIndexOf('.') + 1);
				boolean supported = (ImageIO.getImageReadersBySuffix(fileSuffix)).hasNext();
				if(supported){
					imageFiles = new File[1];
					imageFiles[0] = srcFile;
				} else if(!tryZip){
					//System.out.println("Trying unzip lastly on " + fileName);
					imageFiles = getZippedFiles(srcFile);
				}
			}
		}
		if((imageFiles.length == 1) && imageFiles[0].isDirectory()){
			imageFiles = imageFiles[0].listFiles();
		}
		if((imageFiles != null) && (imageFiles.length > 1)){
			Collections.sort(Arrays.asList(imageFiles));
		}

		//Mechanism for prefetching of images
		// setup a timer for slideshow
		int delay = 100;
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				preLoadImages();
			}
		};
		bufferTimer = new Timer(delay, taskPerformer);
		bufferTimer.setRepeats(false);
	}
	
	/*
	 * loadObj(int ind) returns null if
	 * a) File with given index does not exist
	 * loadObj(int ind) will return an object with obj.image = null if
	 * b) File is a directory
	 * c) File is not an image
	 */
	private ImageObj loadObj(int ind){
		File file = getFile(ind);
		if(file == null){
			return null;
		}
		/*
		 * We are maintaining ImageObj even for nonImage files and directories to avoid
		 * checking these files twice because of prefetching
		 */
	    ImageObj obj = new ImageObj();
	    obj.index = ind;
	    obj.image = null;
		try {
			//System.out.println("Loading page " + ind);
			obj.image = ImageIO.read(file);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return obj;
	}
	
	/*
	 *  preLoadImages() pre fetches the most likely images that could be requested next
	 */
	private void preLoadImages(){
		//System.out.println("Prefetching images...");
		if(bufferedImages[1] != null){
			int curIndex = bufferedImages[1].index;
			if(bufferedImages[2] == null){
				bufferedImages[2] = loadObj(curIndex + 1);
			}
			if(bufferedImages[0] == null){
				bufferedImages[0] = loadObj(curIndex -1);
			}
		}
	}
	
	public BufferedImage getImage(int ind){
		if(imageFiles == null){
			return null;
		}
		BufferedImage bImage = null;
		if(bufferedImages[1] != null && bufferedImages[1].index == ind){
			return bufferedImages[1].image;
		}
		if(bufferedImages[2] != null && bufferedImages[2].index == ind){
			bufferedImages[0] = bufferedImages[1];
			bufferedImages[1] = bufferedImages[2];
			bImage = bufferedImages[2].image;
			bufferedImages[2] = null;
		} else if(bufferedImages[0] != null && bufferedImages[0].index == ind){
			bufferedImages[2] = bufferedImages[1];
			bufferedImages[1] = bufferedImages[0];
			bImage = bufferedImages[0].image;
			bufferedImages[0] = null;
		}else{
			ImageObj imageObj = loadObj(ind);
			if(imageObj != null){
				bufferedImages[1] = imageObj;
				bufferedImages[2] = null;
				bufferedImages[0] = null;
				bImage = imageObj.image;
			}
		}
		if(!bufferTimer.isRunning()){
			bufferTimer.start();
		}
		return bImage;
	}
	
	private File createExtractDir(File srcFile){
		int randomInt = (int)(Math.random() * (Integer.MAX_VALUE -1 )) + 1;
		int fileNameLen = srcFile.getName().length();
		File newDir = new File(tmpDir + System.getProperty("file.separator") + Integer.toString(randomInt) + 
				"_" + srcFile.getName().substring(0, fileNameLen - 4));
		//System.out.println("Creating Directory:" + newDir.getAbsolutePath());
		newDir.mkdir();
		return newDir;
	}
	
	private File [] getRaredFiles(File srcFile) throws IOException{
		File extractDir = createExtractDir(srcFile);
		String unrarCmd = System.getProperty("unrar.cmd");
		String unrarArg = "e";
		if(unrarCmd == null){
			String os = System.getProperty("os.name");
			//System.out.println("Operating System:" + os);
			if(os.startsWith("Windows")){
				unrarCmd = WINDOWS_UNRAR;
			} else {
				unrarCmd = UNIX_UNRAR;
			}
			
		} else {
			unrarArg = System.getProperty("unrar.arg");
		}
		String [] cmdArray = {unrarCmd, unrarArg, srcFile.getCanonicalPath()};
		
		ProcessBuilder pbuilder = new ProcessBuilder(cmdArray);
		pbuilder.directory(extractDir);
		pbuilder.redirectErrorStream();
		Process process = pbuilder.start();
		byte[] buffer = new byte[1024];
		BufferedInputStream is = new BufferedInputStream(process.getInputStream());
		while(is.read(buffer) != -1){
		}
		
		File[] fileList = extractDir.listFiles();
		setAutoDeletion(extractDir, extractDir.getAbsolutePath());
		return fileList;
	}
	
	private File [] getZippedFiles(File srcFile) throws IOException {
		File extractDir = createExtractDir(srcFile);
		ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(srcFile)));
		
		ZipEntry zentry;
		while((zentry = zin.getNextEntry()) != null){
			//System.out.println("Extracting " + zentry.getName());
			File outFile = new File(extractDir.getAbsolutePath() + 
					System.getProperty("file.separator") + zentry.getName());
			if(zentry.isDirectory()){
				outFile.mkdir();
				continue;
			}
			if(!outFile.getParentFile().exists()){
				outFile.getParentFile().mkdir();
			}
			
			BufferedOutputStream dstBuf = new BufferedOutputStream(new FileOutputStream(outFile));
			byte[] buffer = new byte[1024];
			int count;
			while((count = zin.read(buffer)) != -1){
				dstBuf.write(buffer, 0, count);
			}
			//flushing of the output stream is done before it is closed 
			//in the close() method
			dstBuf.close();
		}
		zin.close();
		
		File[] fileList = extractDir.listFiles();
		setAutoDeletion(extractDir, extractDir.getAbsolutePath());
		return fileList;
	}
		
	private void setAutoDeletion(File file, String mainParent){
		if(!file.getAbsolutePath().startsWith(mainParent)){
			return;
		}
		file.deleteOnExit();
		if(file.isDirectory()){
			for(File entry: file.listFiles()){
				setAutoDeletion(entry, mainParent);
			}
		}
	}
	
	private File getFile(int index){
		if(!hasFile(index)){
			return null;
		}
		return imageFiles[index];
	}
	
	public boolean hasFile(int index){
		if((imageFiles == null) || (index < 0) || (index >= imageFiles.length)){
			return false;
		}
		return true;
	}

	public int getNumFiles(){
		int num = 0;
		if(imageFiles != null){
			num = imageFiles.length;
		}
		return num;
	}
	
	private class ImageObj{
		public int index;
		public BufferedImage image;
	}
}
