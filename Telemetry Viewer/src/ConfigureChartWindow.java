import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

@SuppressWarnings("serial")
public class ConfigureChartWindow extends JDialog {
	
	/**
	 * A dialog box where the user can configure an existing chart's settings.
	 *  
	 * @param parentWindow    The JFrame that this dialog box should be tied to (and centered over.)
	 * @param chart           The chart to configure.
	 */
	public ConfigureChartWindow(JFrame parentWindow, PositionedChart chart) {
		
		setTitle("Configure Chart");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JPanel windowContents = new JPanel();
		JScrollPane jScrollPane = new JScrollPane(windowContents);

		windowContents.setBorder(new EmptyBorder(10, 10, 10, 10));
		windowContents.setLayout(new BoxLayout(windowContents, BoxLayout.Y_AXIS));

		JPanel doneButtonPanel = new JPanel();
		JButton doneButton = new JButton("Done");
		doneButton.addActionListener(event -> dispose());
		doneButtonPanel.setLayout(new GridLayout(1, 3, 10, 10));
		doneButtonPanel.add(new JLabel(""));
		doneButtonPanel.add(new JLabel(""));
		doneButtonPanel.add(doneButton);
		
		// show the control widgets
		for(JPanel widget : chart.getWidgets()) {
			windowContents.add(widget != null ? widget : Box.createVerticalStrut(10));
			windowContents.add(Box.createVerticalStrut(10));
		}
		
		// leave some room, then show the done button
		windowContents.add(Box.createVerticalStrut(40));
		windowContents.add(doneButtonPanel);

		jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane.setViewportBorder(new LineBorder(Color.RED));
		jScrollPane.getViewport().add(windowContents, null);
		add(jScrollPane, BorderLayout.CENTER);
		// size and position the window
		//setResizable(false);
		pack();
		setSize((int) (getWidth() * 1.3), getHeight());
		setLocationRelativeTo(parentWindow);
		
		//setModal(true);
		setVisible(true);
		
	}

}
