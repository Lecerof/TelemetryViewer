import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;

public class Main {
	
	static int globalScale;
	
	public static void main(String[] args) {
		
		System.out.println(System.getProperty("user.dir"));

		globalScale = 1;
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = env.getDefaultScreenDevice();
	    try {
	    	java.lang.reflect.Field field = device.getClass().getDeclaredField("scale");
	        if (field != null) {
	            field.setAccessible(true);
	            Object scale = field.get(device);
	            if (scale instanceof Integer && ((Integer) scale).intValue() == 2) {
	                globalScale = 2;
	            }
	        }
	    } catch (Exception ignore) {}
				
		try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception e){}
		
		
		
		JFrame window = new JFrame("Telemetry Viewer v0.4");
		ControlsRegion controlsRegion = new ControlsRegion();
		OpenGLChartsRegion chartsRegion = new OpenGLChartsRegion(controlsRegion);
		Menubar menuBar = new Menubar();
		
		
		
		window.setLayout(new BorderLayout());
		window.add(menuBar.menubar, BorderLayout.NORTH);
		window.add(chartsRegion, BorderLayout.CENTER);
		window.add(controlsRegion, BorderLayout.SOUTH);
		
		window.setSize( (int) (GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width * 0.6), 
						(int) (GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height * 0.6));
		window.setLocationRelativeTo(null);
		
		window.setMinimumSize(window.getPreferredSize());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		
		/*LogitechSmoothScrolling mouse = new LogitechSmoothScrolling();
		
		window.addWindowFocusListener(new WindowFocusListener() {
			@Override public void windowGainedFocus(WindowEvent we) {
				mouse.enableSmoothScrolling();
			}
			@Override public void windowLostFocus(WindowEvent we) { }
		});
		
		
		
		
		
		*/
	}

}
