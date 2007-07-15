/*
 * Just4Kix - Image Viewer and Comic Book Reader
 *
 * ImageComponent.java
 * Created on 4th May, 2007
 * Author: Naveen Belkale
 */
package com.viewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.Timer;

public class ImageComponent extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2259993453997399820L;

	public static final int FIT_WIDTH = 0;

	public static final int FIT_HEIGHT = 1;

	public static final int FIT_IMAGE = 2;

	public static final int ACTUAL_SIZE = 3;

	public static final int CUSTOM_ZOOM = 4;

	private Dimension curDim, newDim;

	private FileProcessor imageFiles;

	private int curIndex, curState;

	private BufferedImage curImage;

	private double scale;

	private String fileName;

	private int rotate;

	private Timer slideshowTimer;

	public ImageComponent(File mainFile, Dimension maxDim) {
		setOpaque(true);
		setBackground(Color.BLACK);

		curState = FIT_IMAGE;
		scale = 1.0;
		rotate = 0;
		forKix = false;
		curDim = maxDim;
		newDim = new Dimension(maxDim);
		setImageFiles(mainFile);

		// setup a timer for slideshow
		int delay = 5000;
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				runShow();
			}
		};
		slideshowTimer = new Timer(delay, taskPerformer);
		slideshowTimer.setRepeats(true);
	}

	@Override
	public void paintComponent(Graphics g) {
		if (curImage == null) {
			return;
		}
		Graphics2D g2d = (Graphics2D) g;
		AffineTransform at = new AffineTransform();
		// the order of affine transform is in the reverse. Hence first comes
		// last

		// Third Transformation
		// center the image
		double tx = 0, ty = 0;
		if (newDim.width + 4 < curDim.width) {
			tx = (curDim.width - newDim.width) / 2.0;
		}
		if (newDim.height + 4 < curDim.height) {
			ty = (curDim.height - newDim.height) / 2.0;
		}
		at.translate(tx, ty);

		// Second Transformation
		if (rotate % 2 == 0) {
			at.quadrantRotate(rotate, newDim.width / 2, newDim.height / 2);
		} else {
			at.translate(newDim.width / 2, newDim.height / 2);
			at.quadrantRotate(rotate);
			at.translate(-newDim.height / 2, -newDim.width / 2);
		}

		// First Transformation
		at.scale(scale, scale);

		if (isOpaque()) {
			g2d.setColor(getBackground());
			g2d.fillRect(0, 0, getWidth(), getHeight());
		}

		if (forKix) {
			draw4Kix(g2d);
		}
		g2d.drawImage(curImage, at, null);
	}

	private void determineScale() {
		if (curImage == null) {
			return;
		}
		int imgX = curImage.getWidth();
		int imgY = curImage.getHeight();
		int X = curDim.width;
		int Y = curDim.height;

		if (rotate % 2 != 0) {
			int tmp = imgX;
			imgX = imgY;
			imgY = tmp;
		}

		switch (curState) {
		case FIT_IMAGE:
			double scaleX,
			scaleY;
			scaleX = X / (double) imgX;
			scaleY = Y / (double) imgY;
			scale = scaleX <= scaleY ? scaleX : scaleY;
			break;
		case FIT_HEIGHT:
			scale = Y / (double) imgY;
			break;
		case FIT_WIDTH:
			scale = X / (double) imgX;
			break;
		case ACTUAL_SIZE:
			scale = 1.0;
			break;
		}
		/*
		 * System.out.println("Available:" + getWidth() + "x" + getHeight() + "
		 * Image:" + imgX + "x" + imgY +" Scaled:" + X + "x" + Y);
		 */
		newDim.width = (int) (scale * imgX);
		newDim.height = (int) (scale * imgY);
		return;
	}

	public int getCurState() {
		return curState;
	}

	public void setCurState(int curState) {
		this.curState = curState;
		if (curImage == null) {
			return;
		}
		refreshImage();
	}

	public int getCurIndex() {
		return curIndex;
	}

	public int getNumPages() {
		return imageFiles.getNumFiles();
	}

	@Override
	public Dimension getPreferredSize() {
		return newDim;
	}

	public void setImageFiles(File mainFile) {
		try {
			imageFiles = new FileProcessor(mainFile);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage() + " on "
					+ mainFile.getAbsolutePath());
			e.printStackTrace();
		}

		if (mainFile != null) {
			fileName = mainFile.getAbsolutePath();
		}
		this.curIndex = 0;
		changeImage(true);
	}

	public String getMainFile() {
		return fileName;
	}

	public void whatNext() {
		// System.out.println("View Rect:" + viewRect + " Visible Image:" +
		// getVisibleRect());
		Rectangle viewRect = getVisibleRect();
		int Y = getHeight();

		if (viewRect.y + viewRect.height >= Y) {
			showNextImage();
			return;
		}

		int newY = viewRect.y - 20 + curDim.height;
		if (newY <= Y) {
			viewRect.y = newY;
			scrollRectToVisible(viewRect);
			return;
		}
		viewRect.y = Y - curDim.height;
		scrollRectToVisible(viewRect);
		return;
	}

	public void whatPrev() {
		Rectangle viewRect = getVisibleRect();
		if (viewRect.y <= 0) {
			showPreviousImage();
			return;
		}

		if (viewRect.y <= viewRect.height - 20) {
			viewRect.y = 0;
			scrollRectToVisible(viewRect);
			return;
		}
		viewRect.y = viewRect.y - viewRect.height + 20;
		scrollRectToVisible(viewRect);
		return;
	}

	public void zoomInImage() {
		if (curImage == null) {
			return;
		}
		curState = CUSTOM_ZOOM;
		scale += 0.05;
		refreshImage();
	}

	public void zoomOutImage() {
		if (curImage == null) {
			return;
		}
		curState = CUSTOM_ZOOM;
		scale -= 0.05;
		refreshImage();
	}

	public void rotateRight() {
		if (curImage == null) {
			return;
		}
		rotate++;
		refreshImage();
	}

	public void rotateLeft() {
		if (curImage == null) {
			return;
		}
		rotate--;
		refreshImage();
	}

	public void startSlideshow(int secs) {
		if (slideshowTimer.isRunning() || (secs <= 0)) {
			return;
		}
		slideshowTimer.setInitialDelay(secs * 1000);
		slideshowTimer.setDelay(secs * 1000);
		slideshowTimer.restart();
	}

	public void stopSlideshow() {
		slideshowTimer.stop();
	}

	private void runShow() {
		// System.out.println("Calling method ruhShow()");
		if (!imageFiles.hasFile(curIndex + 1)) {
			slideshowTimer.stop();
			return;
		}
		whatNext();
	}

	public void showImage(int index) {
		if ((imageFiles == null) || !imageFiles.hasFile(index)) {
			return;
		}
		curIndex = index;
		changeImage(true);
	}

	public void showNextImage() {
		if ((imageFiles == null) || !imageFiles.hasFile(curIndex + 1)) {
			return;
		}
		curIndex++;
		changeImage(true);
	}

	public void showPreviousImage() {
		if ((imageFiles == null) || !imageFiles.hasFile(curIndex - 1)) {
			return;
		}
		curIndex--;
		changeImage(false);
	}

	/* Reads the image file with index curIndex. If the file is not an image or
	 * is a directory,
	 * reads the previous or next image based on the argument next */
	private void changeImage(boolean next) {
		//System.out.println("changeImage() -> curIndex:" + curIndex);
		curImage = imageFiles.getImage(curIndex);
		if(curImage == null){
			if(next) {
				showNextImage();
			} else {
				showPreviousImage();
			}
			return;
		}
		rotate = 0;
		refreshImage();
		scrollRectToVisible(new Rectangle(curDim));
	}

	private void refreshImage() {
		determineScale();
		revalidate();
		repaint();
	}

	// Just For Kicks!!
	private boolean forKix;

	public void just4Kix() {
		forKix = true;
		refreshImage();
		int delay = 5000;
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				forKix = false;
				refreshImage();
			}
		};
		Timer timer = new Timer(delay, taskPerformer);
		timer.setRepeats(false);
		timer.start();
	}

	private void draw4Kix(Graphics2D g2) {

		FontRenderContext frc = g2.getFontRenderContext();
		Font f = new Font("Helvetica", 1, curDim.width / 6);
		String s = new String("Just4Kix");
		TextLayout tl = new TextLayout(s, f, frc);
		Shape outline = tl.getOutline(null);
		Rectangle r = outline.getBounds();

		AffineTransform saveXform = g2.getTransform();
		Color sColor = g2.getColor();

		Rectangle viewRect = getVisibleRect();
		g2.translate(viewRect.x + (viewRect.width - r.width) / 2, viewRect.y
				+ (viewRect.height - r.height) / 2);
		g2.setColor(Color.blue);
		g2.draw(outline);
		g2.setClip(outline);

		g2.setTransform(saveXform);
		g2.setColor(sColor);
	}
}
