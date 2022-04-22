package controller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import analyzer.LexicalError;
import analyzer.Lexicon;
import analyzer.SemanticError;
import analyzer.Semantic;
import analyzer.Syntactic;
import analyzer.SyntaticError;
import view.DtmTable;
import view.NumberedBorder;

public class Control {
	public abstract class MyAbstractAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public MyAbstractAction(String text, ImageIcon icon, String desc, KeyStroke keyStroke, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(ACCELERATOR_KEY, keyStroke);
			putValue(MNEMONIC_KEY, mnemonic);
		}
	}

	private MyAbstractAction actionPrint, actionExit, actionUndo, actionRedo, actionCut, actionCopy, actionPaste,
			actionCompile, actionClear, actionAbout, actionSelectAll;

	private UndoManager undoManager;
	private String changed;
	private JTextArea editor;
	private JComboBox<String> database;
	private DefaultListModel<String> dlmTable, message;
	private JLabel lbState;
	private Metadata metadata;

	public Control(Metadata metadata) {
		createActions();
		this.metadata = metadata;
	}

	public JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu mFile = new JMenu("Arquivo");
		mFile.setMnemonic(KeyEvent.VK_A);
		menuBar.add(mFile);
		mFile.add(new JMenuItem(actionPrint));
		mFile.addSeparator();
		mFile.add(new JMenuItem(actionExit));

		JMenu mEdit = new JMenu("Editar");
		mEdit.setMnemonic(KeyEvent.VK_E);
		menuBar.add(mEdit);
		mEdit.add(new JMenuItem(actionUndo));
		mEdit.add(new JMenuItem(actionRedo));
		mEdit.addSeparator();
		mEdit.add(new JMenuItem(actionCut));
		mEdit.add(new JMenuItem(actionCopy));
		mEdit.add(new JMenuItem(actionPaste));
		mEdit.addSeparator();
		mEdit.add(new JMenuItem(actionSelectAll));

		JMenu mProject = new JMenu("Projeto");
		mProject.setMnemonic(KeyEvent.VK_P);
		menuBar.add(mProject);
		mProject.add(new JMenuItem(actionCompile));
		mProject.add(new JMenuItem(actionClear));

		JMenu mHelp = new JMenu("Ajuda");
		mHelp.setMnemonic(KeyEvent.VK_J);
		menuBar.add(mHelp);
		mHelp.add(new JMenuItem(actionAbout));
		return menuBar;
	}

	public JToolBar createToolBar() {
		AbstractAction[] a = { actionPrint, actionUndo, actionRedo, actionCut, actionCopy, actionPaste, actionCompile,
				actionClear, actionAbout };

		JToolBar toolBar = new JToolBar("Barra de ferramentas");
		toolBar.setFloatable(false);
		for (int i = 0; i < a.length; ++i) {
			if (i == 1 || i == 3 || i == 6 || i == 8)
				toolBar.addSeparator();
			JButton b = new JButton(a[i]);
			b.setText("");
			b.setFocusable(false);
			toolBar.add(b);
		}
		return toolBar;
	}

	public JPanel createDatabase() {
		database = new JComboBox<String>();
		database.addItemListener((e) -> {
			if (database.getSelectedIndex() > 0)
				updateTables(database.getSelectedItem().toString());
		});
		updateDatabases();

		JList<String> list = new JList<String>(dlmTable = new DefaultListModel<String>());
		list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		list.setFocusable(false);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(database, BorderLayout.NORTH);
		panel.add(new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
		panel.setPreferredSize(new Dimension(140, 600));
		return panel;
	}

	public JScrollPane createEditor() {
		editor = new JTextArea();
		editor.setBorder(new NumberedBorder());

		final int[] keyEvents = { KeyEvent.VK_A, KeyEvent.VK_X, KeyEvent.VK_C, KeyEvent.VK_V };
		for (int i = 0; i < 4; ++i)
			editor.getInputMap().put(KeyStroke.getKeyStroke(keyEvents[i], InputEvent.CTRL_DOWN_MASK), "none");
		editor.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateStatusBar();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateStatusBar();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {

			}
		});

		undoManager = new UndoManager();
		editor.getDocument().addUndoableEditListener((e) -> {
			undoManager.addEdit(e.getEdit());
		});
		return new JScrollPane(editor, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}

	public JScrollPane createTable() {
		JTable table = new JTable(DtmTable.getInstance());
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		return new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}

	public JScrollPane createMessage() {
		JList<String> lMessage = new JList<String>(message = new DefaultListModel<String>());
		lMessage.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lMessage.setLayoutOrientation(JList.VERTICAL);
		lMessage.setVisibleRowCount(-1);
		lMessage.setFocusable(false);

		JScrollPane spMessage = new JScrollPane(lMessage, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		spMessage.setPreferredSize(new Dimension(0, 66));
		return spMessage;
	}

	public JPanel createStatusBar() {
		JPanel statusBar = new JPanel();
		statusBar.setLayout(new BorderLayout());
		statusBar.setBorder(BorderFactory.createEtchedBorder());
		statusBar.setPreferredSize(new Dimension(0, 20));
		statusBar.add(lbState = new JLabel("Não Modificado"), BorderLayout.WEST);
		changed = editor.getText();
		return statusBar;
	}

	private void updateStatusBar() {
		lbState.setText((!changed.equals(editor.getText())) ? "Modificado" : "Não Modificado");
	}

	private void updateDatabases() {
		database.removeAllItems();
		database.addItem("Nenhum");
		if (metadata.getDatabases() != null)
			metadata.getDatabases().values().forEach(path -> database.addItem(path.getFileName().toString()));
	}

	private void updateTables(String item) {
		dlmTable.removeAllElements();
		try {
			metadata.setDatabase(item);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), e.getClass().getSimpleName(),
					JOptionPane.ERROR_MESSAGE);
		}
		if (metadata.getTables() != null)
			metadata.getTables().values().forEach(t -> dlmTable.addElement(t.getName()));
	}

	private void createActions() {
		actionPrint = new MyAbstractAction("Imprimir...", new ImageIcon(getClass().getResource("/icons/imprimir.png")),
				"Imprimir (Ctrl+P)", KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK),
				(int) KeyEvent.VK_I) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				String msg = new String();
				try {
					boolean printed = editor.print();
					msg = "Impressão " + (printed ? "concluída." : "cancelada.");
				} catch (PrinterException pe) {
					msg = pe.getClass().getSimpleName() + "- Ocorreu um erro de impressão.";
				} catch (SecurityException se) {
					msg = se.getClass().getSimpleName()
							+ "Não foi possivel acessar a impressora por restrições de segurança.";
				} finally {
					message.addElement(msg);
				}
			}
		};

		actionExit = new MyAbstractAction("Sair", null, "Sair (Alt+F4)", null, (int) KeyEvent.VK_S) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};

		actionUndo = new MyAbstractAction("Desfazer", new ImageIcon(getClass().getResource("/icons/desfazer.png")),
				"Desfazer (Ctrl+Z)", KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK),
				(int) KeyEvent.VK_F) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (undoManager.canUndo())
						undoManager.undo();
				} catch (CannotUndoException cue) {
					JOptionPane.showMessageDialog(null, cue.getMessage(), cue.getClass().getSimpleName(),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		actionRedo = new MyAbstractAction("Refazer", new ImageIcon(getClass().getResource("/icons/refazer.png")),
				"Resfazer (Ctrl+Y)", KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK),
				(int) KeyEvent.VK_A) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (undoManager.canRedo())
						undoManager.redo();
				} catch (CannotUndoException cue) {
					JOptionPane.showMessageDialog(null, cue.getMessage(), cue.getClass().getSimpleName(),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		actionCut = new MyAbstractAction("Recortar", new ImageIcon(getClass().getResource("/icons/recortar.png")),
				"Recortar (Ctrl+X)", KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK),
				(int) KeyEvent.VK_R) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				editor.cut();
			}
		};

		actionCopy = new MyAbstractAction("Copiar", new ImageIcon(getClass().getResource("/icons/copiar.png")),
				"Copiar (Ctrl+C)", KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK), (int) KeyEvent.VK_C) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				editor.copy();
			}
		};

		actionPaste = new MyAbstractAction("Colar", new ImageIcon(getClass().getResource("/icons/colar.png")),
				"Colar (Ctrl+V)", KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK), (int) KeyEvent.VK_L) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				editor.paste();
			}
		};

		actionSelectAll = new MyAbstractAction("Selecionar Tudo", null, "Selecionar Tudo (Ctrl+T)",
				KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK), (int) KeyEvent.VK_T) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				editor.selectAll();
			}
		};

		actionCompile = new MyAbstractAction("Compilar", new ImageIcon(getClass().getResource("/icons/compilar.png")),
				"Compilar (F8)", KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), (int) KeyEvent.VK_C) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				long time = System.currentTimeMillis();
				if (!editor.getText().isEmpty()) {
					try {
						message.clear();
						Lexicon lexicon = new Lexicon(editor.getText());
						Syntactic syntactic = new Syntactic();
						Semantic semantic = new Semantic(metadata);
						try {
							syntactic.parse(lexicon, semantic);
							message.addElement("Compilado com sucesso.");
						} catch (LexicalError lex) {
							message.addElement("Erro léxico na linha " + (editor.getLineOfOffset(lex.getPosition()) + 1)
									+ " - '" + lex.getLexeme() + "' " + lex.getMessage());
							editor.setCaretPosition(lex.getPosition());
						} catch (SyntaticError sin) {
							message.addElement("Erro Sintático na linha "
									+ (editor.getLineOfOffset(sin.getPosition()) + 1) + " - " + sin.getMessage()
									+ ", encontrado " + sin.getClasse() + " '" + sin.getLexeme() + "'.");
							editor.setCaretPosition(sin.getPosition());
						} catch (SemanticError sem) {
							message.addElement("Erro Semântico na linha "
									+ (editor.getLineOfOffset(sem.getPosition()) + 1) + " - " + sem.getMessage());
							editor.setCaretPosition(sem.getPosition());
						} catch (IOException ioe) {
							message.addElement(ioe.getClass().getSimpleName() + " - " + ioe.getMessage());
						}
					} catch (BadLocationException ble) {
						JOptionPane.showMessageDialog(null, ble.getMessage(), ble.getClass().getSimpleName(),
								JOptionPane.ERROR_MESSAGE);
					}
					updateDatabases();
				} else {
					final String msg = "Não há código para compilar.";
					if (!message.contains(msg))
						message.addElement(msg);
				}
				time = System.currentTimeMillis() - time;
				message.addElement("time: " + time);
			}
		};

		actionClear = new MyAbstractAction("Limpar", new ImageIcon(getClass().getResource("/icons/limpar.png")),
				"Limpar (F9)", KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), (int) KeyEvent.VK_L) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				editor.setText("");
				changed = "";
				message.removeAllElements();
				editor.requestFocus();
			}
		};

		actionAbout = new MyAbstractAction("Sobre", new ImageIcon(getClass().getResource("/icons/sobre.png")),
				"Sobre (F1)", KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), (int) KeyEvent.VK_B) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane
						.showMessageDialog(
								null, "Gerenciador de Banco de dados." + "\n Por:"
										+ "\n     Carlos Henrique Stapait Junior." + "\n Java 8.",
								"Sobre", JOptionPane.INFORMATION_MESSAGE);
			}
		};
	}
}