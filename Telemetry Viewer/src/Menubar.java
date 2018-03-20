import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class Menubar extends JPanel {

	
	public JMenuBar menubar;
	JMenu fileMenu;
	JMenu helpMenu;
	JMenu viewMenu;
	
	JMenuItem open;
	JMenu save;
	JMenuItem savelayout;
	JMenuItem savecsvlog;
	
	JMenuItem reset;
	
	JMenuItem gridsize;
	
	JMenuItem help;
	
	
	public Menubar() {
		menubar = new JMenuBar();
		fileMenu = new JMenu("File");
		viewMenu = new JMenu("View");
		helpMenu = new JMenu("Help");
		
		menubar.add(fileMenu);
		menubar.add(viewMenu);
		menubar.add(helpMenu);
		
		
		save = new JMenu("Save");
		savelayout = new JMenuItem("Layout");
		savecsvlog = new JMenuItem("CSV Log");
		save.add(savelayout);
		save.add(savecsvlog);
		
		open = new JMenuItem("Open Layout");
		reset = new JMenuItem("Reset");
		
		fileMenu.add(save);
		fileMenu.add(open);
		fileMenu.add(reset);
		
		
		gridsize = new JMenuItem("Grid size");
		viewMenu.add(gridsize);
		
		help = new JMenuItem("Help");
		helpMenu.add(help);
		
		open.addActionListener(event -> {
			JFileChooser inputFile = new JFileChooser();
			if(inputFile.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				String filePath = inputFile.getSelectedFile().getAbsolutePath();
				Controller.openLayout(filePath);
			}
		});
		
		
		savelayout.setEnabled(false);
		savelayout.addActionListener(event -> {
			JFileChooser saveFile = new JFileChooser();
			if(saveFile.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				String filePath = saveFile.getSelectedFile().getAbsolutePath();
				if(!filePath.endsWith(".txt"))
					filePath += ".txt";
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
	
	
	
}
