import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;

/**
 * Manages the grid region and all charts on the screen.
 * 
 * Users can click-and-drag in this region to create new charts or interact with existing charts.
 */
@SuppressWarnings("serial")
public class OpenGLChartsRegion extends JPanel {
	
	Animator animator;
	int canvasWidth;
	int canvasHeight;
	static int defaultChartX;
	static int defaultChartY;
	
	// grid size
	int columnCount;
	int rowCount;
	int tileWidth;
	int tileHeight;
	int tilesYoffset;
	
	// grid locations for the opposite corners of where a new chart will be placed
	int startX;
	int startY;
	int endX;
	int endY;
	int pixelScaleFactor = Main.globalScale;
	
	// time and zoom settings
	boolean liveView;
	int nonLiveViewSamplesCount;
	double zoomLevel;
	
	// mouse pointer's current location (pixels, origin at bottom-left)
	int mouseX;
	int mouseY;
	PositionedChart chartToRemoveOnClick;
	PositionedChart chartToConfigureOnClick;
	PositionedChart chartMouseIsOver;
	
	boolean serialPortConnected;
	
	JFrame parentWindow;
	
	public OpenGLChartsRegion(ControlsRegion controlsRegion) {
		
		super();
		tileWidth   = 50;
		tileHeight  = 50;
		
		defaultChartX = 4;
		defaultChartY = 4;
		
		startX  = -1;
		startY  = -1;
		endX    = -1;
		endY    = -1;
		
		liveView = true;
		nonLiveViewSamplesCount = 0;
		zoomLevel = 1;
		
		mouseX = -1;
		mouseY = -1;
		chartToRemoveOnClick = null;
		
		serialPortConnected = false;
		
		parentWindow = (JFrame) SwingUtilities.windowForComponent(this);
		OpenGLChartsRegion hello = this;
		GLCanvas glCanvas = new GLCanvas(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
		glCanvas.addGLEventListener(new GLEventListener() {
			
			@Override public void init(GLAutoDrawable drawable) {
				
				GL2 gl = drawable.getGL().getGL2();
				
				gl.glEnable(GL2.GL_BLEND);
				gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
				gl.glEnable(GL2.GL_POINT_SMOOTH);
			    gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_FASTEST);
				gl.glEnable(GL2.GL_LINE_SMOOTH);
			    gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_FASTEST);
//				gl.glEnable(GL2.GL_POLYGON_SMOOTH);
//			    gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_FASTEST);
			    
				gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
			    
				gl.glLineWidth(Theme.lineWidth);
				gl.glPointSize(Theme.pointSize);
				
				gl.setSwapInterval(1);
				
			}
						
			@Override public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
				
				GL2 gl = drawable.getGL().getGL2();
				
				gl.glMatrixMode(GL2.GL_PROJECTION);
				gl.glLoadIdentity();
				gl.glOrtho(0, width, 0, height, -100000, 100000);
				
				canvasWidth = width;
				canvasHeight = height;
				
			}

			@Override public void display(GLAutoDrawable drawable) {
				
				
				
				
				
				
				
				// prepare OpenGL
				GL2 gl = drawable.getGL().getGL2();
				
				gl.glMatrixMode(GL2.GL_MODELVIEW);
				gl.glLoadIdentity();
				
				gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
				gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
				
				gl.glLineWidth(Theme.lineWidth);
				gl.glPointSize(Theme.pointSize);
				
				// if there are no charts and no serial port connection, tell the user to connect or open a layout
				if(!serialPortConnected && Controller.getCharts().size() == 0) {
					
					// draw the background
					gl.glBegin(GL2.GL_QUADS);
					gl.glColor4fv(Theme.neutralColor, 0);
						gl.glVertex2f(0,           0);
						gl.glVertex2f(0,           canvasHeight);
						gl.glVertex2f(canvasWidth, canvasHeight);
						gl.glVertex2f(canvasWidth, 0);
					gl.glEnd();
					
					// draw the text
					String message = "Start by connecting to a serial port or opening a layout file.";
					float messageWidth = FontUtils.xAxisTextWidth(message);
					float messageHeight = FontUtils.xAxisTextHeight;
					float xMessageLeft = (canvasWidth / 2.0f) - (messageWidth / 2.0f);
					float yMessageBottom = (canvasHeight / 2.0f) - (messageHeight / 2.0f);
					FontUtils.setOffsets(0, 0);
					FontUtils.drawXaxisText(message, (int) xMessageLeft, (int) yMessageBottom);
					FontUtils.drawQueuedText(gl, canvasWidth, canvasHeight);
					
					
					return;
					
				}
				
				// a serial port connection exists, or charts exist, so draw the tiles and any charts

				// draw a neutral background
				gl.glBegin(GL2.GL_QUADS);
				gl.glColor4fv(Theme.neutralColor, 0);
					gl.glVertex2f(0,           0);
					gl.glVertex2f(0,           canvasHeight);
					gl.glVertex2f(canvasWidth, canvasHeight);
					gl.glVertex2f(canvasWidth, 0);
				gl.glEnd();
				columnCount = pixelScaleFactor*getWidth()/tileWidth;
				rowCount    =  pixelScaleFactor*getHeight()/tileHeight;
				tilesYoffset = canvasHeight - (tileHeight * rowCount);
				// Draw a grid over the background
				if (Menubar.grid.getState()) {
				drawDashedGrid(gl, canvasWidth, canvasHeight, tileWidth, 
						tileHeight, new Point(0, tilesYoffset), 10, 5 );
				drawFullGrid(gl, canvasWidth, canvasHeight, tileWidth *defaultChartX , tileHeight*defaultChartY, 
						new Point(0, tilesYoffset));
				}
				
				
				
				// if there are no charts, tell the user how to add one
				List<PositionedChart> charts = Controller.getCharts();
				if(charts.size() == 0) {
				
					liveView = true;
					
					// draw the text
					String message = "Add a chart by clicking once, or by clicking-and-dragging across an empty area";
					float messageWidth = FontUtils.xAxisTextWidth(message);
					float messageHeight = FontUtils.xAxisTextHeight;
					float xMessageLeft = (canvasWidth / 2.0f) - (messageWidth / 2.0f);
					float xMessageRight = xMessageLeft + messageWidth;
					float yMessageBottom = (canvasHeight / 2.0f) - (messageHeight / 2.0f);
					float yMessageTop = yMessageBottom + messageHeight;
					
					gl.glBegin(GL2.GL_QUADS);
					gl.glColor4fv(Theme.transparentNeutralColor, 0);
						gl.glVertex2f(xMessageLeft  - Theme.legendTextPadding, yMessageBottom - Theme.legendTextPadding);
						gl.glVertex2f(xMessageLeft  - Theme.legendTextPadding, yMessageTop    + Theme.legendTextPadding);
						gl.glVertex2f(xMessageRight + Theme.legendTextPadding, yMessageTop    + Theme.legendTextPadding);
						gl.glVertex2f(xMessageRight + Theme.legendTextPadding, yMessageBottom - Theme.legendTextPadding);
					gl.glEnd();
					
					FontUtils.setOffsets(0, 0);
					FontUtils.drawXaxisText(message, (int) xMessageLeft, (int) yMessageBottom);
					FontUtils.drawQueuedText(gl, canvasWidth, canvasHeight);

					
				}
				
				// draw a bounding box where the user is actively clicking-and-dragging to place a new chart
				gl.glBegin(GL2.GL_QUADS);
				gl.glColor4fv(Theme.tileSelectedColor, 0);
					int x1 = startX < endX ? startX * tileWidth : endX * tileWidth;
					int y1 = startY < endY ? startY * tileHeight   : endY * tileHeight;
					int x2 = x1 + (Math.abs(endX - startX) + 1) * tileWidth;
					int y2 = y1 + (Math.abs(endY - startY) + 1) * tileHeight;
					y1 = canvasHeight - y1;
					y2 = canvasHeight - y2;
					gl.glVertex2f(x1, y1);
					gl.glVertex2f(x1, y2);
					gl.glVertex2f(x2, y2);
					gl.glVertex2f(x2, y1);
				gl.glEnd();		
				
				// draw the charts
				//
				// the modelview matrix is translated so the origin will be at the bottom-left for each chart.
				// the scissor test is used to clip rendering to the region allocated for each chart.
				// if charts will be using off-screen framebuffers, they need to disable the scissor test when (and only when) drawing off-screen.
				// after the chart is drawn with OpenGL, any text queued for rendering will be drawn on top.
				PositionedChart chartToClose = null;
				PositionedChart chartToConfigure = null;
				for(PositionedChart chart : charts) {
					
					// draw the tile
					int width = tileWidth * (chart.bottomRightX - chart.topLeftX + 1);
					int height = tileHeight * (chart.bottomRightY - chart.topLeftY + 1);
					int xOffset = chart.topLeftX * tileWidth;
					int yOffset = canvasHeight - (chart.topLeftY * tileHeight) - height;
					drawTile(gl, xOffset, yOffset, width, height);
					
					// draw the chart
					xOffset += Theme.tilePadding;
					yOffset += Theme.tilePadding;
					width  -= 2 * Theme.tilePadding;
					height -= 2 * Theme.tilePadding;
					
					gl.glEnable(GL2.GL_SCISSOR_TEST);
					gl.glScissor(xOffset, yOffset, width, height);
					gl.glPushMatrix();
					gl.glTranslatef(xOffset, yOffset, 0);
					
					FontUtils.setOffsets(xOffset, yOffset);
					int lastSampleNumber = liveView ? Controller.getSamplesCount() - 1 : nonLiveViewSamplesCount;
					chart.drawChart(gl, width, height, lastSampleNumber, zoomLevel);
					FontUtils.drawQueuedText(gl, canvasWidth, canvasHeight);
					
					gl.glPopMatrix();
					gl.glDisable(GL2.GL_SCISSOR_TEST);
					
					// draw the chart configure and close buttons
					width += (int) Theme.tileShadowOffset;
					boolean mouseOverCloseButton = drawChartCloseButton(gl, xOffset, yOffset, width, height);
					if(mouseOverCloseButton)
						chartToClose = chart;
					boolean mouseOverConfigureButton = drawChartSettingsButton(gl, xOffset, yOffset, width, height);
					if(mouseOverConfigureButton)
						chartToConfigure = chart;
					if (mouseOverChart(xOffset, yOffset, width, height))
						chartMouseIsOver = chart;
					
				}
				
				chartToRemoveOnClick = chartToClose;
				chartToConfigureOnClick = chartToConfigure;
				
			}
			
			@Override public void dispose(GLAutoDrawable drawable) {
				
			}
			
		});
		
		setLayout(new BorderLayout());
		add(glCanvas, BorderLayout.CENTER);
	
		animator = new Animator(glCanvas);
		animator.setUpdateFPSFrames(1, null);
		animator.start();
		
		glCanvas.addMouseListener(new MouseListener() {
			
			// the mouse was pressed, attempting to start a new chart region, or to configure/remove an existing chart
			@Override public void mousePressed(MouseEvent me) {
				
				if(!serialPortConnected && Controller.getCharts().size() == 0)
					return;
				
				if(chartToRemoveOnClick != null) {
					Controller.removeChart(chartToRemoveOnClick);
					return;
				}
				
				if(chartToConfigureOnClick != null) {
					new ConfigureChartWindow(parentWindow, chartToConfigureOnClick);
					return;
				}
				
				int proposedStartX = me.getX() * columnCount / getWidth();
				
				int proposedStartY = me.getY() * rowCount / getHeight();
				
				if(proposedStartX < columnCount && proposedStartY < rowCount && Controller.gridRegionAvailable(proposedStartX, proposedStartY, proposedStartX, proposedStartY)) {
					startX = endX = proposedStartX;
					startY = endY = proposedStartY;
					return;
					
				}
				if (me.getButton() == 3) {
					JPopupMenu pmenu = new JPopupMenu("Menu");
					JMenuItem options = new JMenuItem("Options");
					JMenuItem resize = new JMenuItem("Resize");
					JMenuItem move = new JMenuItem("Move");
					JMenuItem close = new JMenuItem("Close");
					pmenu.add(options);
					pmenu.add(resize);
					pmenu.add(move);
					pmenu.addSeparator();
					pmenu.add(close);
					//pmenu.setLightWeightPopupEnabled(false);
					
					options.addActionListener(e -> new ConfigureChartWindow(parentWindow, chartMouseIsOver));
					close.addActionListener(e -> Controller.removeChart(chartMouseIsOver));
					
					pmenu.show(hello, me.getX(), me.getY());
				}
				
			}
			
			// the mouse was released, attempting to create a new chart
			@Override public void mouseReleased(MouseEvent me) {
				
				if(!serialPortConnected && Controller.getCharts().size() == 0)
					return;

				if(endX == -1 || endY == -1)
					return;
			
				int proposedEndX = me.getX() * columnCount / getWidth();
				int proposedEndY = me.getY() * rowCount / getHeight();
				
				if(proposedEndX < columnCount && proposedEndY < rowCount && Controller.gridRegionAvailable(startX, startY, proposedEndX, proposedEndY)) {
					endX = proposedEndX;
					endY = proposedEndY;
				}
				
				
				int x1, y1, x2, y2;
				if ( (startX == endX) || (startY == endY) ) {
					x1 = startX;
					y1 = startY;
					x2 = (startX == endX) ? endX + defaultChartX-1 : endX;
					y2 = (startY == endY) ? endY + defaultChartY-1 : endY;
					if (!Controller.gridRegionAvailable(x1, y1, x2, y2)) {
						startX = startY = -1;
						endX   = endY   = -1;
						return;
					}
				} else {
					x1 = startX;
					y1 = startY;
					x2 = endX;
					y2 = endY;
				}
				
				startX = startY = -1;
				endX   = endY   = -1;
				
				new AddChartWindow(parentWindow, x1, y1, x2, y2);
				
			}

			// the mouse left the canvas, no longer need to show the chart close icon
			@Override public void mouseExited (MouseEvent me) {
				
				mouseX = -1;
				mouseY = -1;
				
			}
			
			@Override public void mouseClicked(MouseEvent me) { }
			
			@Override public void mouseEntered(MouseEvent me) { }
			
		});

		
		
		glCanvas.addMouseMotionListener(new MouseMotionListener() {
			
			// the mouse was dragged while attempting to create a new chart
			@Override public void mouseDragged(MouseEvent me) {
				
				if(!serialPortConnected && Controller.getCharts().size() == 0)
					return;
				
				if(endX == -1 || endY == -1)
					return;
				
				int proposedEndX = me.getX() * columnCount / getWidth();
				int proposedEndY = me.getY() * rowCount / getHeight();
				
				if(proposedEndX < columnCount && proposedEndY < rowCount && Controller.gridRegionAvailable(startX, startY, proposedEndX, proposedEndY)) {
					endX = proposedEndX;
					endY = proposedEndY;
				}
				
			}
			
			// log the mouse position so a chart close icon can be drawn
			@Override public void mouseMoved(MouseEvent me) {
				
				if(!serialPortConnected && Controller.getCharts().size() == 0)
					return;
				
				mouseX = me.getX();
				mouseY = glCanvas.getHeight() - me.getY();
				
			}
			
		});
		
		glCanvas.addMouseWheelListener(new MouseWheelListener() {
			
			// the mouse wheel was scrolled
			@Override public void mouseWheelMoved(MouseWheelEvent mwe) {

				double scrollAmount = mwe.getPreciseWheelRotation();
				double samplesPerScroll = Controller.getSampleRate() / 4;
				double zoomPerScroll = 0.1;
				float  displayScalingPerScroll = 0.1f;
				
				if(Controller.getCharts().size() == 0 && mwe.isShiftDown() == false)
					return;
				
				if(scrollAmount == 0)
					return;
				
				if(mwe.isControlDown() == false && mwe.isShiftDown() == false) {
					
					// no modifiers held down, so we're timeshifting
					if(liveView == true) {
						liveView = false;
						nonLiveViewSamplesCount = (Controller.getSamplesCount() - 1);
					}
					
					double delta = scrollAmount * samplesPerScroll * zoomLevel;
					if(delta < -0.5 || delta > 0.5)
						delta = Math.round(delta);
					else if(delta < 0)
						delta = -1;
					else if(delta >= 0)
						delta = 1;
					nonLiveViewSamplesCount += delta;
					
					if(nonLiveViewSamplesCount >= Controller.getSamplesCount() - 1)
						liveView = true;
				
				} else if(mwe.isControlDown() == true) {
					
					// ctrl is down, so we're zooming
					zoomLevel *= 1 + (scrollAmount * zoomPerScroll);
					
					if(zoomLevel > 1)
						zoomLevel = 1;
					else if(zoomLevel < 0)
						zoomLevel = Double.MIN_VALUE;
					
				} else if(mwe.isShiftDown() == true) {
					
					// shift is down, so we're setting the display scaling factor
					float newFactor = Controller.getDisplayScalingFactor() * (1 - ((float)scrollAmount * displayScalingPerScroll));
					Controller.setDisplayScalingFactor(newFactor);
					
				}
				
			}
			
		});
		
		// update the column and row counts when they change
		Controller.addGridChangedListener(new GridChangedListener() {
			@Override public void gridChanged(int columns, int rows) {
				columnCount = columns;
				rowCount = rows;
			}
		});
		
		// track if a serial port is connected
		Controller.addSerialPortListener(new SerialPortListener() {
			@Override public void connectionOpened(int sampleRate, Packet packet, String portName, int baudRate) {
				serialPortConnected = true;
			}
			
			@Override public void connectionLost() {
				serialPortConnected = false;
			}
			
			@Override public void connectionClosed() {
				serialPortConnected = false;
			}
		});
		
	}
	
	/**
	 * Draws a tile, the tile's drop-shadow, and a margin around the tile.
	 * 
	 * @param gl         The OpenGL context.
	 * @param xOffset    Lower-left x location.
	 * @param yOffset    Lower-left y location.
	 * @param width      Total region width, including the tile, drop-shadow and margin.
	 * @param height     Total region height, including the tile, drop-shadow and margin.
	 */
	private void drawTile(GL2 gl, int xOffset, int yOffset, int width, int height) {
		
		// draw the background (margin)
		gl.glBegin(GL2.GL_QUADS);
		gl.glColor4fv(Theme.neutralColor, 0);
			gl.glVertex2f(xOffset,         yOffset);
			gl.glVertex2f(xOffset,         yOffset + height);
			gl.glVertex2f(xOffset + width, yOffset + height);
			gl.glVertex2f(xOffset + width, yOffset);
		gl.glEnd();
		
		// draw the tile's drop-shadow
		gl.glBegin(GL2.GL_QUADS);
		gl.glColor4fv(Theme.tileShadowColor, 0);
			gl.glVertex2f(xOffset         + Theme.tilePadding + Theme.tileShadowOffset, yOffset          + Theme.tilePadding - Theme.tileShadowOffset);
			gl.glVertex2f(xOffset         + Theme.tilePadding + Theme.tileShadowOffset, yOffset + height - Theme.tilePadding - Theme.tileShadowOffset);
			gl.glVertex2f(xOffset + width - Theme.tilePadding + Theme.tileShadowOffset, yOffset + height - Theme.tilePadding - Theme.tileShadowOffset);
			gl.glVertex2f(xOffset + width - Theme.tilePadding + Theme.tileShadowOffset, yOffset          + Theme.tilePadding - Theme.tileShadowOffset);
		gl.glEnd();

		// draw the tile
		gl.glBegin(GL2.GL_QUADS);
		gl.glColor4fv(Theme.tileColor, 0);
			gl.glVertex2f(xOffset         + Theme.tilePadding, yOffset          + Theme.tilePadding);
			gl.glVertex2f(xOffset         + Theme.tilePadding, yOffset + height - Theme.tilePadding);
			gl.glVertex2f(xOffset + width - Theme.tilePadding, yOffset + height - Theme.tilePadding);
			gl.glVertex2f(xOffset + width - Theme.tilePadding, yOffset          + Theme.tilePadding);
		gl.glEnd();
		
	}
	
	/**
	 * Draws an "X" close chart button for the user to click on if the mouse is over this chart.
	 * 
	 * @param gl         The OpenGL context.
	 * @param xOffset    Chart region lower-left x location.
	 * @param yOffset    Chart region lower-left y location.
	 * @param width      Chart region width.
	 * @param height     Chart region height.
	 * @return           True if the mouse cursor is over this button, false if not.
	 */
	private boolean drawChartCloseButton(GL2 gl, int xOffset, int yOffset, int width, int height) {
		
		// only draw if necessary
		if(!mouseOverChart(xOffset, yOffset, width, height))
			return false;
		
		float buttonWidth = 15f * pixelScaleFactor;
		float inset = buttonWidth * 0.2f;
		float buttonXleft = xOffset + width - buttonWidth;
		float buttonXright = xOffset + width;
		float buttonYtop = yOffset + height;
		float buttonYbottom = yOffset + height - buttonWidth;
		boolean mouseOverButton = mouseOverButton(buttonXleft, buttonXright, buttonYbottom, buttonYtop);
		float[] white = new float[] {1, 1, 1, 1};
		float[] black = new float[] {0, 0, 0, 1};
		
		// draw button background
		gl.glBegin(GL2.GL_QUADS);
			gl.glColor4fv(mouseOverButton ? black : white, 0);
			gl.glVertex2f(buttonXleft, buttonYbottom);
			gl.glVertex2f(buttonXleft, buttonYtop);
			gl.glVertex2f(buttonXright, buttonYtop);
			gl.glVertex2f(buttonXright, buttonYbottom);
		gl.glEnd();
		
		// draw button outline
		gl.glBegin(GL2.GL_LINE_LOOP);
			gl.glColor4fv(mouseOverButton ? white : black, 0);
			gl.glVertex2f(buttonXleft, buttonYbottom);
			gl.glVertex2f(buttonXleft, buttonYtop);
			gl.glVertex2f(buttonXright, buttonYtop);
			gl.glVertex2f(buttonXright, buttonYbottom);
		gl.glEnd();
		
		// draw the "X"
		gl.glBegin(GL2.GL_LINES);
			gl.glColor4fv(mouseOverButton ? white : black, 0);
			gl.glVertex2f(buttonXleft  + inset, buttonYtop    - inset);
			gl.glVertex2f(buttonXright - inset, buttonYbottom + inset);
			gl.glVertex2f(buttonXleft  + inset, buttonYbottom + inset);
			gl.glVertex2f(buttonXright - inset, buttonYtop    - inset);
		gl.glEnd();
		
		return mouseOverButton;
		
	}
	
	/**
	 * Draws a chart settings button (gear icon) for the user to click on if the mouse is over this chart.
	 * 
	 * @param gl         The OpenGL context.
	 * @param xOffset    Chart region lower-left x location.
	 * @param yOffset    Chart region lower-left y location.
	 * @param width      Chart region width.
	 * @param height     Chart region height.
	 * @return           True if the mouse cursor is over this button, false if not.
	 */
	private boolean drawChartSettingsButton(GL2 gl, int xOffset, int yOffset, int width, int height) {
		
		// only draw if necessary
		if(!mouseOverChart(xOffset, yOffset, width, height))
			return false;
		float buttonWidth = 15f * pixelScaleFactor;
		float offset = buttonWidth + 1;
		float buttonXleft = xOffset + width - buttonWidth - offset;
		float buttonXright = xOffset + width - offset;
		float buttonYtop = yOffset + height;
		float buttonYbottom = yOffset + height - buttonWidth;
		boolean mouseOverButton = mouseOverButton(buttonXleft, buttonXright, buttonYbottom, buttonYtop);
		float[] white = new float[] {1, 1, 1, 1};
		float[] black = new float[] {0, 0, 0, 1};
		
		int teethCount = 7;
		int vertexCount = teethCount * 4;
		float gearCenterX = buttonXright - (buttonWidth / 2);
		float gearCenterY = buttonYtop - (buttonWidth / 2);
		float outerRadius = buttonWidth * 0.35f;
		float innerRadius = buttonWidth * 0.25f;
		float holeRadius  = buttonWidth * 0.10f;
		
		// draw button background
		gl.glBegin(GL2.GL_QUADS);
			gl.glColor4fv(mouseOverButton ? black : white, 0);
			gl.glVertex2f(buttonXleft, buttonYbottom);
			gl.glVertex2f(buttonXleft, buttonYtop);
			gl.glVertex2f(buttonXright, buttonYtop);
			gl.glVertex2f(buttonXright, buttonYbottom);
		gl.glEnd();
		
		// draw button outline
		gl.glBegin(GL2.GL_LINE_LOOP);
			gl.glColor4fv(mouseOverButton ? white : black, 0);
			gl.glVertex2f(buttonXleft, buttonYbottom);
			gl.glVertex2f(buttonXleft, buttonYtop);
			gl.glVertex2f(buttonXright, buttonYtop);
			gl.glVertex2f(buttonXright, buttonYbottom);
		gl.glEnd();
		
		// draw the gear teeth
		gl.glBegin(GL2.GL_LINE_LOOP);
			gl.glColor4fv(mouseOverButton ? white : black, 0);
			for(int vertex = 0; vertex < vertexCount; vertex++) {
				float x = gearCenterX + (float) Math.cos((double) vertex / (double)vertexCount * 2 * Math.PI) * (vertex % 4 < 2 ? outerRadius : innerRadius);
				float y = gearCenterY + (float) Math.sin((double) vertex / (double)vertexCount * 2 * Math.PI) * (vertex % 4 < 2 ? outerRadius : innerRadius);
				gl.glVertex2f(x, y);
			}
		gl.glEnd();
		
		// draw the hole
		gl.glBegin(GL2.GL_LINE_LOOP);
		gl.glColor4fv(mouseOverButton ? white : black, 0);
		for(int vertex = 0; vertex < vertexCount; vertex++) {
			float x = gearCenterX + (float) Math.cos((double) vertex / (double)vertexCount * 2 * Math.PI) * holeRadius;
			float y = gearCenterY + (float) Math.sin((double) vertex / (double)vertexCount * 2 * Math.PI) * holeRadius;
			gl.glVertex2f(x, y);
		}
		gl.glEnd();
		
		return mouseOverButton;
		
	}
	
	private boolean mouseOverChart(int xOffset, int yOffset, int width, int height) {
		return 	mouseX >= xOffset/pixelScaleFactor && 
				mouseX <= xOffset/pixelScaleFactor + width/pixelScaleFactor && 
				mouseY >= yOffset/pixelScaleFactor && 
				mouseY <= yOffset/pixelScaleFactor + height/pixelScaleFactor;
	}
	private boolean mouseOverButton(float buttonXleft, float buttonXright, float buttonYbottom, float buttonYtop) {
		return 	mouseX >= buttonXleft/pixelScaleFactor && 
				mouseX <= buttonXright/pixelScaleFactor && 
				mouseY+2*pixelScaleFactor >= buttonYbottom/pixelScaleFactor && 
				mouseY+2*pixelScaleFactor <= buttonYtop/pixelScaleFactor;
	}
	
	public static void setDefaultChartX(int a) {
		defaultChartX = a;
	}
	public static void setDefaultChartY(int a) {
		defaultChartY = a;
	}
	
	public void drawFullGrid(GL2 gl, int width, int height, int xSpacing, int ySpacing, 
			Point start) {
		
		// Check indexing
		int numberOfVerticalLines = width / defaultChartX;
		int numberOfHorizontalLines = height / defaultChartY;
		float colorRed = 0.00f;
		float colorGreen = 0.00f;
		float colorBlue = 0.00f;
		
		for (int i = 0; i <= numberOfVerticalLines; i ++) {
			drawFullVerticalLine(gl, new Point(i*xSpacing + start.x, start.y), height, colorRed, colorGreen, colorBlue);
		}
		for (int i = 0; i <= numberOfHorizontalLines; i ++) {
			drawFullHorizontalLine(gl, new Point(start.x, height - i*ySpacing), width, colorRed, colorGreen, colorBlue);
		}
		
	}
	
	
	
	public void drawDashedGrid(GL2 gl, int width, int height, int xSpacing, int ySpacing, 
													Point start, int gap, int lineLength) {
		int numberOfVerticalLines = width / xSpacing;
		int numberOfHorizontalLines = height / ySpacing;
		float colorRed = 0.02f;
		float colorGreen = 0.02f;
		float colorBlue = 0.02f;
		
		for (int i = 0; i <= numberOfVerticalLines; i ++) {
			drawDashedVerticalLine(gl, new Point(i*xSpacing + start.x, start.y), height, gap, lineLength, colorRed, colorGreen, colorBlue);
		}
		for (int i = 0; i <= numberOfHorizontalLines; i ++) {
			drawDashedHorizontalLine(gl, new Point(start.x, height - i*ySpacing), width, gap, lineLength, colorRed, colorGreen, colorBlue);
		}
				
	}
	
	public void drawDashedVerticalLine(GL2 gl, Point start, int length, 
												int gap, int linelength, float colorRed,
												float colorGreen, float colorBlue) {
		int numberOfDashedLines = length / (linelength + gap);
		for (int i = 0; i <= numberOfDashedLines; i ++) {
			Point starttmp = new Point(start.x, start.y + i*(linelength + gap));
			drawFullVerticalLine(gl, starttmp, linelength, colorRed, colorGreen, colorBlue);
		}	
	}
	
	public void drawDashedHorizontalLine(GL2 gl, Point start, int length, 
											int gap, int linelength, float colorRed,
											float colorGreen, float colorBlue) {
		int numberOfDashedLines = length / (linelength + gap); 
		for (int i = 0; i <= numberOfDashedLines; i ++) {
		Point starttmp = new Point(start.x + i*(linelength + gap), start.y );
		drawFullHorizontalLine(gl, starttmp, linelength, colorRed, colorGreen, colorBlue);
		}	
	}
	
	public void drawFullVerticalLine(GL2 gl, Point start, int length,
			float colorRed, float colorGreen, float colorBlue) {
		gl.glBegin(GL2.GL_LINES);
		gl.glColor3f(0.0f, 0.0f, 0.0f);
		gl.glVertex2f(start.x, start.y);
		gl.glVertex2f(start.x, start.y + length);
		gl.glEnd();
	}
	public void drawFullHorizontalLine(GL2 gl, Point start, int length,
					float colorRed, float colorGreen, float colorBlue) {
		gl.glBegin(GL2.GL_LINES);
		gl.glColor3f(0.0f, 0.0f, 0.0f);
		gl.glVertex2f(start.x, start.y+1);
		gl.glVertex2f(start.x + length, start.y+1);
		gl.glEnd();
	}
	
	
	
}