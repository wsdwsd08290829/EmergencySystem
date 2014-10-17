package main;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import org.apache.log4j.Logger;

/*
 * summary: get selected Radio: set commandAction on radioBtn => btnGroup->getSelected()->getcommandAction;
 * summary: read image from ImageIO.read(fileObj) to BufferedImage; lable.setIcon(image) to display;
 * summary: draw on image: write to BufferedImage, drawOval, fillRect
 */
public class Emergency {
	final static Logger logger = Logger.getLogger(Emergency.class);
	/*
	 * //demo draw on JLable or Jpanel class DrawableJLable extends JLabel{
	 * public DrawableJLable(String string) { super(string); }
	 * 
	 * @Override public void paintComponent(Graphics g) {
	 * super.paintComponent(g); drawPoint(g, "blue"); } private void
	 * drawPoint(Graphics g, String string) { Graphics2D g2d = (Graphics2D) g;
	 * g2d.setColor(Color.blue); for (int i = 0; i <= 1000; i++) { Dimension
	 * size = getSize(); Insets insets = getInsets();
	 * 
	 * int w = size.width - insets.left - insets.right; int h = size.height -
	 * insets.top - insets.bottom;
	 * 
	 * Random r = new Random(); int x = Math.abs(r.nextInt()) % w; int y =
	 * Math.abs(r.nextInt()) % h; // g2d.setFont(); g2d.drawLine(x, y, x, y); }
	 * } }
	 */
	/* declare window components, for it functionality, refert to initMethods */
	private JFrame frame;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	// GUI components
	private final JPanel panel = new JPanel();
	private final JLabel lblActiveFeactureTypes = new JLabel(
			"Active Feacture Types");
	private final JCheckBox annouceChk = new JCheckBox("Announce System");
	private final JCheckBox studentChk = new JCheckBox("Student");
	private final JCheckBox buildingChk = new JCheckBox("Building");
	private final JPanel panel_1 = new JPanel();
	private final JRadioButton wholeRdo = new JRadioButton("Whole Region");
	private final JRadioButton pointRdo = new JRadioButton("Point Query");
	private final JRadioButton rangeRdo = new JRadioButton("Range Query");
	private final JRadioButton surroundRdo = new JRadioButton(
			"Surround Student");
	private final JRadioButton emergencyRdo = new JRadioButton(
			"Emergency Query");
	private final JLabel lblQueryMethod = new JLabel("Query Method");
	private final JButton submitBtn = new JButton("Submit Query");
	final JLabel cordLabel = new JLabel("X:   Y:");
	TextArea textArea = new TextArea();
	final JLabel imageLabel = new JLabel("New label");
	BufferedImage image;
	// map of 820*580 with potential students, buildings, announcement system to
	// mark on
	String mapPath = "map.jpg";
	/*
	 * datastructures
	 */
	// map active type(table name) to checkbox (boolean)
	Map<String, Boolean> featureMap = new HashMap<String, Boolean>();

	DrawToGraphEngine dge;
	QueryDrawGeoEngine qdge;
	String queryMethod = "";
	// for point, range, emergency query
	Point queryPointCenter;
	final int queryPointRadius = 50;
	// for range query
	ArrayList<Point> pointsList = new ArrayList<Point>();
	boolean rangeSubmitFlag;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Emergency window = new Emergency();
					window.frame.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Emergency() {
		initialize();
		handlersEngine();
	}

	/**
	 * handle user interactions 1. hover one image: display coordinates
	 * 2.different actions to image according the query method choose 3.
	 * different actions to image when submit buttons is clicked according the
	 * query method choose
	 */
	private void handlersEngine() {
		// track x, y coordination of image on hover
		imageLabel.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int mouseX = 0, mouseY = 0;
				mouseX = e.getX();
				mouseY = e.getY();
				cordLabel.setText("X " + mouseX + " Y " + mouseY);
			}
		});
		// action according to query Method
		imageLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				queryPointCenter = new Point(e.getX(), e.getY());
				logger.info("point clicked: " + queryPointCenter);
				// get selected radio button (query method)
				ButtonModel bm = buttonGroup.getSelection();
				queryMethod = bm.getActionCommand();

				// action according to query Method
				if (queryMethod.equals("point")) {

					try {
						image = ImageIO.read(new File(mapPath));
						imageLabel.setIcon(new ImageIcon(image));
						dge = new DrawToGraphEngine(image);
						qdge = new QueryDrawGeoEngine(dge);
					} catch (IOException ex) {
						ex.printStackTrace();
					}

					textArea.setText("point is " + queryPointCenter);
					dge.drawRect(queryPointCenter, 5, 5, Color.red);
					dge.drawCircle(queryPointCenter, 50, Color.red);
				} else if (queryMethod.equals("range")) {
					// pointsList
					imageLabel.setIcon(new ImageIcon(image));
					if (SwingUtilities.isLeftMouseButton(e)) {
						// if submit, redraw old selected lines(remove selected
						// feature from image
						if (rangeSubmitFlag) {
							try {
								image = ImageIO.read(new File(mapPath));
								imageLabel.setIcon(new ImageIcon(image));
								dge = new DrawToGraphEngine(image);
								qdge = new QueryDrawGeoEngine(dge);
							} catch (IOException ex) {
								ex.printStackTrace();
							}
							// if pointList is clear(beacuse other radio is
							// selected, do not redraw
							if (pointsList.size() > 0) {
								for (int i = 0; i < pointsList.size() - 1; i++) {
									Point p1 = pointsList.get(i);
									Point p2 = pointsList.get(i + 1);
									dge.drawLine(p1.x, p1.y, p2.x, p2.y,
											Color.red);
								}
							}
							rangeSubmitFlag = false;
						}
						pointsList.add(new Point(e.getX(), e.getY()));
						if (pointsList.size() >= 2) {
							dge.drawLine(
									pointsList.get(pointsList.size() - 2).x,
									pointsList.get(pointsList.size() - 2).y,
									pointsList.get(pointsList.size() - 1).x,
									pointsList.get(pointsList.size() - 1).y,
									Color.red);
						}
					}
					if (SwingUtilities.isRightMouseButton(e)
							&& pointsList.size() > 2) {
						Point first = pointsList.get(0);
						Point last = pointsList.get(pointsList.size() - 1);
						dge.drawLine(first.x, first.y, last.x, last.y,
								Color.red);
						pointsList.add(first);
					}
				} else if (queryMethod.equals("surround")
						|| queryMethod.equals("emergency")) {
					try {
						image = ImageIO.read(new File(mapPath));
						imageLabel.setIcon(new ImageIcon(image));
						dge = new DrawToGraphEngine(image);
						qdge = new QueryDrawGeoEngine(dge);
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					ArrayList<String> queryLog = qdge
							.queryNearestAnnounce(queryPointCenter);
					displayQueryString(queryLog);
				}
			}

		});
		// when submit query clicked
		submitBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// get parameters(features) for query
				collectChkResults();
				// get selected radio button (query method)
				ButtonModel bm = buttonGroup.getSelection();
				queryMethod = bm.getActionCommand();
				BufferedImage image = null;
				try {

					image = ImageIO.read(new File(mapPath)); // reload image

				} catch (IOException ex) {
					ex.printStackTrace();
				}
				// set Draw engine and query engine class object
				dge = new DrawToGraphEngine(image);
				qdge = new QueryDrawGeoEngine(dge);
				imageLabel.setIcon(new ImageIcon(image));// inorder to draw
				// to it
				// check query method and perform corresponding method
				if (queryMethod.equals("whole")) {
					// reread image for drawing query result
					String sql = "";
					ArrayList<String> sqlLog = new ArrayList<String>();
					ArrayList<String> queryLog = qdge.queryWhole(featureMap);
					displayQueryString(queryLog);
				} else if (queryMethod.equals("point")) {
					if (queryPointCenter == null) {
						textArea.setText("select a point first");
					} else {
						dge.drawRect(queryPointCenter, 5, 5, Color.red);
						dge.drawCircle(queryPointCenter, 50, Color.red);
						ArrayList<String> queryLog = qdge.queryPoint(
								featureMap, queryPointCenter, queryPointRadius);
						displayQueryString(queryLog);
					}
				} else if (queryMethod.equals("range")) {
					for (int i = 0; i < pointsList.size() - 1; i++) {
						Point p1 = pointsList.get(i);
						Point p2 = pointsList.get(i + 1);
						dge.drawLine(p1.x, p1.y, p2.x, p2.y, Color.red);
					}
					ArrayList<String> queryLog = qdge.queryRange(featureMap,
							pointsList);
					displayQueryString(queryLog);
					rangeSubmitFlag = true;
				} else if (queryMethod.equals("surround")) {
					if (queryPointCenter == null) {
						textArea.setText("select a point for nearest announcement first");
					} else {
						ArrayList<String> queryLog = qdge
								.querySurroundAnnounce(queryPointCenter);
						displayQueryString(queryLog);
					}
				} else if (queryMethod.equals("emergency")) {
					if (queryPointCenter == null) {
						textArea.setText("select a point for nearest announcement first");
					} else {
						ArrayList<String> queryLog = qdge
								.queryEmergency(queryPointCenter);
						displayQueryString(queryLog);
					}
				}
			}
		});

		// image reset for each radio button clicked
		Enumeration<AbstractButton> radioEnum = buttonGroup.getElements();
		while (radioEnum.hasMoreElements()) {
			AbstractButton ab = radioEnum.nextElement();
			ab.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					BufferedImage image = null;
					try {
						image = ImageIO.read(new File(mapPath));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					imageLabel.setIcon(new ImageIcon(image));
					// reset queryGeo for next query
					// queryGeo = "";
					// reset pointsList for next range query
					pointsList.clear();
					queryPointCenter = null;
				}
			});
		}
	}

	private void displayQueryString(ArrayList<String> sqlLog) {
		StringBuilder sql = new StringBuilder();
		for (int i = 0; i < sqlLog.size(); i++) {
			sql.append("query" + i + ": " + sqlLog.get(i) + "\n");
		}
		textArea.setText(sql.toString());
	}

	private void collectChkResults() {
		if (annouceChk.isSelected())
			featureMap.put("myannouncementsystems", true);
		else
			featureMap.put("myannouncementsystems", false);
		if (studentChk.isSelected())
			featureMap.put("mystudents", true);
		else
			featureMap.put("mystudents", false);
		if (buildingChk.isSelected())
			featureMap.put("mybuildings", true);
		else
			featureMap.put("mybuildings", false);
	}

	/**
	 * Initialize the contents of the frame by functionality.
	 */
	private void initialize() {
		initFramePanel();
		initFeatureTypes();
		initQueryMethods();
		initImage();
		// others parts
		initSubmitButton();
		initCoordiantionLabel(); // X, Y Label
		initLoggingArea();// textArea
	}

	/**
	 * Logging area for displaying query executed for current operation
	 */
	private void initLoggingArea() {
		textArea.setBounds(24, 610, 820, 58);
		frame.getContentPane().add(textArea);
	}

	/**
	 * display current coordination when mouse hover on image
	 */
	private void initCoordiantionLabel() {
		cordLabel.setBounds(858, 610, 84, 18);
		frame.getContentPane().add(cordLabel);
	}

	/*
	 * init different components in application window
	 */
	/**
	 * set frame, panels panel: for feature type(checkbox) panel_1: for query
	 * method(radio btns)
	 */
	private void initFramePanel() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1081, 723);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		panel.setBorder(new LineBorder(new Color(0, 0, 0), 2, true));
		panel.setBounds(858, 37, 197, 171);
		frame.getContentPane().add(panel);
		panel.setLayout(null);

		panel_1.setBorder(new LineBorder(new Color(0, 0, 0), 2, true));
		panel_1.setBounds(860, 249, 189, 203);
		frame.getContentPane().add(panel_1);
		panel_1.setLayout(null);
	}

	/**
	 * submit button
	 */
	private void initSubmitButton() {
		submitBtn.setBounds(884, 523, 165, 27);
		frame.getContentPane().add(submitBtn);
	}

	/**
	 * Load image from file to contentPane of frame init drawToGraphEngine for
	 * drawing on image
	 */
	private void initImage() {
		try {
			image = ImageIO.read(new File(mapPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		imageLabel.setIcon(new ImageIcon(image));
		dge = new DrawToGraphEngine(image);
		imageLabel.setBounds(14, 13, 830, 590);
		frame.getContentPane().add(imageLabel);
	}

	/**
	 * query method on panel_1 radios for: whole, point, range, surround,
	 * emergency query
	 */
	private void initQueryMethods() {
		lblQueryMethod.setBounds(25, 13, 112, 18);
		panel_1.add(lblQueryMethod);

		wholeRdo.setSelected(true);

		wholeRdo.setBounds(20, 39, 125, 27);
		buttonGroup.add(wholeRdo);
		panel_1.add(wholeRdo);
		wholeRdo.setActionCommand("whole");
		pointRdo.setToolTipText("Select a point in the image; \\n\r\nResult will be related features within range of 50 of the point; \\n\r\nNearest feature of that feature type is yellow, others are green;");

		pointRdo.setBounds(20, 71, 117, 27);
		buttonGroup.add(pointRdo);
		panel_1.add(pointRdo);
		pointRdo.setActionCommand("point");

		rangeRdo.setBounds(20, 103, 117, 27);
		buttonGroup.add(rangeRdo);
		panel_1.add(rangeRdo);
		rangeRdo.setActionCommand("range");

		surroundRdo.setBounds(20, 135, 157, 27);
		buttonGroup.add(surroundRdo);
		panel_1.add(surroundRdo);
		surroundRdo.setActionCommand("surround");

		buttonGroup.add(emergencyRdo);
		emergencyRdo.setBounds(20, 167, 149, 27);
		panel_1.add(emergencyRdo);
		emergencyRdo.setActionCommand("emergency");

	}

	/**
	 * query feature type on panel check boxes for: announce system, student,
	 * building
	 */
	private void initFeatureTypes() {
		lblActiveFeactureTypes.setBounds(14, 13, 169, 18);
		panel.add(lblActiveFeactureTypes);

		annouceChk.setBounds(24, 40, 133, 27);
		panel.add(annouceChk);

		studentChk.setBounds(24, 72, 133, 27);
		panel.add(studentChk);

		buildingChk.setBounds(24, 104, 133, 27);
		panel.add(buildingChk);
	}
	/* end of initializing window components */

}
