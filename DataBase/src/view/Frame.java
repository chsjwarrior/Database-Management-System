package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import controller.Control;
import controller.Metadata;

public class Frame extends JFrame {
	private static final long serialVersionUID = 1L;

	public Frame(Control control) {
		super();

		setJMenuBar(control.createMenuBar());

		getContentPane().add(control.createToolBar(), BorderLayout.NORTH);
		getContentPane().add(control.createDatabase(), BorderLayout.WEST);

		JPanel pnCenter = new JPanel(new BorderLayout());

		JSplitPane spEditor = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, control.createEditor(),
				control.createTable());
		spEditor.setDividerLocation(250);
		spEditor.setResizeWeight(1.0);
		spEditor.setOneTouchExpandable(true);

		pnCenter.add(spEditor, BorderLayout.CENTER);
		pnCenter.add(control.createMessage(), BorderLayout.SOUTH);

		getContentPane().add(pnCenter, BorderLayout.CENTER);
		getContentPane().add(control.createStatusBar(), BorderLayout.SOUTH);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setTitle("Banco de Dados");
		setMinimumSize(new Dimension(600, 300));
		setSize(800, 600);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), e.getClass().getSimpleName(),
					JOptionPane.ERROR_MESSAGE);
		}
		UIManager.put("TextArea.font", new Font("Verdana", Font.PLAIN, 12));
		UIManager.put("TextArea.selectionBackground", new Color(0, 170, 255));

		Metadata metadata = null;
		try {
			metadata = new Metadata();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE,
					null);
			System.exit(0);
		}
		final Control control = new Control(metadata);
		final Frame frame = new Frame(control);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.setVisible(true);
			}
		});
	}
}