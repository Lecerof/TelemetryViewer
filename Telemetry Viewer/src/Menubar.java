import java.awt.CheckboxMenuItem;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;

@SuppressWarnings("serial")
public class Menubar extends JPanel implements PropertyChangeListener {

	
	public JMenuBar menubar;
	JMenu fileMenu;
	JMenu helpMenu;
	JMenu viewMenu;
	JMenu settingMenu;
	
	JMenu openRecent;
	JMenu save;
	JMenuItem open;
	JMenuItem savelayout;
	JMenuItem savecsvlog;
	JMenuItem openFile;
	JMenuItem openHistory1;
	JMenuItem openHistory2;
	JMenuItem openHistory3;
	JMenuItem openHistory4;
		
	
	JMenuItem reset;
	
	static JCheckBoxMenuItem grid;
	
	JMenuItem help;
	
	JMenu defaultChartSize;
	JSlider defaultChartX;
	JSlider defaultChartY;
	
	public Menubar() {
		menubar = new JMenuBar();
		fileMenu = new JMenu("File");
		viewMenu = new JMenu("View");
		helpMenu = new JMenu("Help");
		settingMenu = new JMenu("Settings");
		
		menubar.add(fileMenu);
		menubar.add(viewMenu);
		menubar.add(helpMenu);
		menubar.add(settingMenu);
		
		
		save = new JMenu("Save");
		savelayout = new JMenuItem("Layout");
		savecsvlog = new JMenuItem("CSV Log");
		save.add(savelayout);
		save.add(savecsvlog);
		
		open = new JMenuItem("Open");
		openRecent = new JMenu("Open recent");
		openFile = new JMenuItem("Open Layout from file");
		
		
		reset = new JMenuItem("Reset");
		
		fileMenu.add(save);
		fileMenu.add(open);
		fileMenu.add(openRecent);
		fileMenu.add(reset);
		openHistory1 = new JMenuItem("");
		openHistory2 = new JMenuItem("");
		openHistory3 = new JMenuItem("");
		openHistory4 = new JMenuItem("");
		
		openHistory1.addActionListener(event -> Controller.openLayout(Controller.fileHistory[0].getPath()));
		openHistory2.addActionListener(event -> Controller.openLayout(Controller.fileHistory[1].getPath()));
		openHistory3.addActionListener(event -> Controller.openLayout(Controller.fileHistory[2].getPath()));
		openHistory4.addActionListener(event -> Controller.openLayout(Controller.fileHistory[3].getPath()));
		
		
		Controller.pcs.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String event = evt.getPropertyName();
				if (event.startsWith("updateHistory")) {
					switch (Integer.parseInt(event.substring(event.length()-1,event.length()))) {
						case 0:
							if (evt.getNewValue() == null) {
								openRecent.remove(openHistory1);
								break;
							}
							else {
								openHistory1.setText(((File)evt.getNewValue()).getName());
								openRecent.add(openHistory1);
							}
							break;
						case 1:
							if (evt.getNewValue() == null) {
								openRecent.remove(openHistory2);	
							}
							else {
								openHistory2.setText(((File)evt.getNewValue()).getName());
								openRecent.add(openHistory2);
							}
							break;
						case 2:
							if (evt.getNewValue() == null) {
								openRecent.remove(openHistory3);	
							}
							else {
								openHistory3.setText(((File)evt.getNewValue()).getName());
								openRecent.add(openHistory3);
							}
							break;
						case 3:
							if (evt.getNewValue() == null) {
								openRecent.remove(openHistory4);	
							}
							else {
								openHistory4.setText(((File)evt.getNewValue()).getName());
								openRecent.add(openHistory4);
							}
							break;
					}
				
			}
			}
		});
		
		Controller.updateHistyoryList();

		
		
		grid = new JCheckBoxMenuItem("Show grid", true);
		viewMenu.add(grid);

		
		help = new JMenuItem("Help");
		helpMenu.add(help);
		
		defaultChartSize = new JMenu("Default chartsize");
		settingMenu.add(defaultChartSize);
		JPanel sliderPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		int min = 1; int max = 17; int initX = 7; int initY = 4;
		OpenGLChartsRegion.setDefaultChartX(initX);
		OpenGLChartsRegion.setDefaultChartY(initY);
		
		defaultChartX = new JSlider(min,max,initX);
		defaultChartX.createStandardLabels(1,1);
		defaultChartX.setMinorTickSpacing(1);
		defaultChartX.setMajorTickSpacing(4);
		defaultChartX.setFont(new Font("Serif", Font.PLAIN, 10));
		defaultChartX.setPaintLabels(true);
		defaultChartX.setPaintTicks(true);
		defaultChartX.setPreferredSize(new Dimension(100,40));
		defaultChartX.setSnapToTicks(true);
		JLabel sliderLabelX = new JLabel("  X: ", JLabel.LEFT);
        sliderLabelX.setAlignmentX(Component.LEFT_ALIGNMENT);
        sliderPanel.add(sliderLabelX);
        c.gridwidth = GridBagConstraints.REMAINDER;
        sliderPanel.add(defaultChartX,c);
        
		defaultChartY = new JSlider(min,max,initY);
		defaultChartY.createStandardLabels(1,1);
		defaultChartY.setMinorTickSpacing(1);
		defaultChartY.setMajorTickSpacing(4);
		defaultChartY.setFont(new Font("Serif", Font.PLAIN, 10));
		defaultChartY.setPaintLabels(true);
		defaultChartY.setPaintTicks(true);
		defaultChartY.setPreferredSize(new Dimension(100,40));
		defaultChartY.setSnapToTicks(true);
		JLabel sliderLabelY = new JLabel("  Y: ", JLabel.LEFT);
        sliderLabelY.setAlignmentX(Component.LEFT_ALIGNMENT);
        sliderPanel.add(sliderLabelY);
        sliderPanel.add(defaultChartY);
        
        defaultChartSize.add(sliderPanel);
		
        
        defaultChartX.addChangeListener(event -> {
        		OpenGLChartsRegion.setDefaultChartX(defaultChartX.getValue());
        });
        defaultChartY.addChangeListener(event -> {
    			OpenGLChartsRegion.setDefaultChartY(defaultChartY.getValue());
        });
		
		open.addActionListener(event -> {
			JFileChooser inputFile = new JFileChooser();
			if(inputFile.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				String filePath = inputFile.getSelectedFile().getAbsolutePath();
				Controller.openLayout(filePath);
			}
		});
		
		
		//savelayout.setEnabled(false);
		savelayout.addActionListener(event -> {
			JFileChooser saveFile = new JFileChooser();
			if(saveFile.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				String filePath = saveFile.getSelectedFile().getAbsolutePath();
				
				
			    
				if(!filePath.endsWith(".txt")) {
					filePath += ".txt";
				}
				
				Controller.saveLayout(filePath);
			}
		});
		
		
		savecsvlog.addActionListener(event -> {
			JFileChooser saveFile = new JFileChooser();
			if(saveFile.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				String filePath = saveFile.getSelectedFile().getAbsolutePath();
				if(!filePath.endsWith(".csv"))
					filePath += ".csv";
				Controller.exportCsvLogFile(filePath);
			}

		});
		
		
		reset.addActionListener(event -> Controller.removeAllCharts());
		
		defaultChartSize.addActionListener(event -> {
			
		});
		
		
		help.addActionListener(event -> {
			
			//JFrame parentWindow = (JFrame) SwingUtilities.windowForComponent(ControlsRegion.this);
			String helpText = "<html><b>Telemetry Viewer v0.4 (2017-07-21)</b><br>" +
			                  "A fast and easy tool for visualizing data received over a UART.<br><br>" +
			                  "Step 1: Use the controls at the lower-right corner of the main window to connect to a serial port.<br>" +
			                  "Step 2: A \"Data Structure\" window will pop up, use it to specify how your data is laid out, then click \"Done\"<br>" +
			                  "Step 3: Click-and-drag in the tiles region to place a chart.<br>" +
			                  "Step 4: A \"New Chart\" window will pop up, use it to specify the type of chart and its settings.<br>" +
			                  "Repeat steps 3 and 4 to create more charts.<br><br>" +
			                  "Use your scroll wheel to rewind or fast forward.<br>" +
			                  "Use your scroll wheel while holding down Ctrl to zoom in or out.<br>" +
			                  "Use your scroll wheel while holding down Shift to adjust display scaling.<br><br>" +
			                  "Click the x icon at the top-right corner of any chart to remove it.<br>" +
			                  "Click the gear icon at the top-right corner of any chart to change its settings.<br><br>" +
			                  "Click the \"Open Layout\" button to open a layout file.<br>" +
			                  "Click the \"Save Layout\" button to save your current configuration (port settings, data structure, and chart settings) to a file.<br>" +
			                  "Click the \"Export CSV Log\" button to save all of your acquired samples to a CSV file.<br>" +
			                  "Click the \"Reset\" button to remove all charts.<br><br>" +
			                  "This software is free and open source.<br>" +
			                  "Author: Farrell Farahbod</html>";
			JLabel helpLabel = new JLabel(helpText);
			JButton websiteButton = new JButton("<html><a href=\"http://www.farrellf.com/TelemetryViewer/\">http://www.farrellf.com/TelemetryViewer/</a></html>");
			websiteButton.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					try { Desktop.getDesktop().browse(new URI("http://www.farrellf.com/TelemetryViewer/")); } catch(Exception ex) {}
				}
			});
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.add(helpLabel);
			panel.add(websiteButton);
			panel.add(new JLabel(" "));
			JOptionPane.showMessageDialog(null, panel, "Help", JOptionPane.PLAIN_MESSAGE);

		});
		
		
		


		
		
	}
	


	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		System.out.println("hej");
		System.out.println(evt.getPropertyName());
		
	}
	
	
	
}

