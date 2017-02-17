package edu.rosehulman.gettyscw;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ClientApp extends JFrame {
	// TODO fix model numbers
	private final static int DEFAULT_YEAR = 2000;
	private final static int MIN_YEAR = 1540;
	private final static int MAX_YEAR = 2012;

	static SpinnerNumberModel getModel() {
		return new SpinnerNumberModel(DEFAULT_YEAR, MIN_YEAR, MAX_YEAR, 1);
	}

	static JSpinner.NumberEditor getEditor(JSpinner js) {
		return new JSpinner.NumberEditor(js, "0000");
	}

	public ClientApp() {
		super("Ngrams Client");
		setup();
		this.setSize(1000, 500);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	private void setup() {
		JTabbedPane tabs = new JTabbedPane();
		this.setContentPane(tabs);

		JPanel task1 = new Task1Panel();
		tabs.add("1. Top Words by Year", task1);

		JPanel task2 = new Task2Panel();
		tabs.add("2. Following Word by Year", task2);

		JPanel task3 = new Task3Panel();
		tabs.add("3. Following Word by Range", task3);

		JPanel task4 = new Task4Panel();
		tabs.add("4. Paragraph Analysis", task4);

		JPanel task5 = new Task5Panel();
		tabs.add("5. Paragraph Analysis", task5);
	}

	static Object task2query(String qword, int qyear) throws SQLException {
		// try {
		// Class.forName("org.apache.hive.jdbc.HiveDriver");
		// } catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// System.exit(1);
		// }

		Connection con = DriverManager.getConnection("jdbc:hive2://hadoop-12.csse.rose-hulman.edu:10000/default",
				"root", "ngrams490");//

		System.out.println("Connected");
		Statement stmt = con.createStatement();
		// stmt.setQueryTimeout(30);
		String tableName = "testset";
		ResultSet res;
		String sql;
		/*
		 * sql = "SHOW CURRENT ROLES"; System.out.println("Running: " + sql);
		 * res = stmt.executeQuery(sql); while (res.next()) {
		 * System.out.println(res.getString(1)); }
		 */

		sql = "SHOW TABLES";
		System.out.println("Running: " + sql);
		res = stmt.executeQuery(sql);
		while (res.next()) {
			System.out.println(res.getString(1));
		}

		// select * query
		// Ex: SELECT * FROM testset WHERE year=1988 AND firstword="testA" ORDER
		// BY wordcount DESC LIMIT 2;
		// http://stackoverflow.com/questions/30813945/hive-order-by-query-results-in-error
		sql = "select * from " + tableName + " where year='" + qyear + "' AND firstword='" + qword + "'";// ORDER
																											// BY
																											// wordcount
																											// DESC
																											// LIMIT
																											// 2
		System.out.println("Running: " + sql);
		res = stmt.executeQuery(sql);
		System.out.println("Executed");
		while (res.next()) {
			System.out.println(res.getString(2) + "\t" + res.getInt(4));
		}
		con.close();
		return new Object();

	}

	static Object task3query(String qword, int year1, int year2) {
		return new Object();
	}

	static void task4query(String paragraph, int year) throws SQLException {
		String upper = paragraph.toLowerCase();
		String[] words = upper.split("\\W");
		HashMap<String, Integer> counts = new HashMap<>();
		int count = 0;
		for (String w : words) {
			String trimmed = w.trim();
			if (!trimmed.isEmpty()) {
				count++;
				if (counts.containsKey(trimmed)) {
					counts.put(trimmed, counts.get(trimmed) + 1);
				} else {
					counts.put(trimmed, 1);
				}
				System.out.println(trimmed);
			}
		}
		System.out.println("Counts:");
		for (String s : counts.keySet()) {
			System.out.println(s + " " + counts.get(s));
		}
		Connection con = DriverManager.getConnection("jdbc:hive2://hadoop-12.csse.rose-hulman.edu:10000/default",
				"root", "ngrams490");//

		System.out.println("Connected");
		Statement stmt = con.createStatement();
		// stmt.setQueryTimeout(30);
		String tableName = "task4words";
		String byYearTable = "task4years";
		ResultSet res;
		String sql;

		HashMap<String, Integer> historicalcounts = new HashMap<>();
		sql = "select * from " + byYearTable + " where year='" + year + "'";
		System.out.println("Running: " + sql);
		res = stmt.executeQuery(sql);
		System.out.println("Executed");
		res.next();
		long historicalcount = res.getLong(2);

		for (String s : counts.keySet()) {
			// select * query
			// Ex: SELECT * FROM testset WHERE year=1988 AND firstword="testA"
			// ORDER BY wordcount DESC LIMIT 2;
			// http://stackoverflow.com/questions/30813945/hive-order-by-query-results-in-error
			sql = "select * from " + tableName + " where year='" + year + "' AND gram1='" + s + "'";// ORDER
																									// BY
																									// wordcount
																									// DESC
																									// LIMIT
																									// 2
			System.out.println("Running: " + sql);
			res = stmt.executeQuery(sql);
			System.out.println("Executed");
			if (res.next()) {
				historicalcounts.put(s, res.getInt(3));
				System.out.println(res.getString(1) + "\t" + res.getInt(3));
			} else {
				// Would 20 or 39 be a better value?
				historicalcounts.put(s, 0);
				System.out.println(s + "\t" + 0);
			}
		}

		con.close();
		System.out.println("Word       Your Usage/Historical Usage");

		ArrayList<StringIntPair> wordbadness = new ArrayList<>();
		for (String s : counts.keySet()) {
			float badness;
			if (historicalcounts.get(s) == 0) {
				badness = 10000f;
			} else {
				badness = ((float) counts.get(s) / count) / ((float) historicalcounts.get(s) / historicalcount);
			}
			wordbadness.add(new StringIntPair(s, badness));
		}
		StringIntPair[] wordbadarray = new StringIntPair[wordbadness.size()];
		wordbadness.toArray(wordbadarray);
		Arrays.sort(wordbadarray);
		for (int i = 0; i < wordbadarray.length; i++) {
			StringIntPair p = wordbadarray[i];
			System.out.println(String.format("%1$-10s", p.word) + " " + p.badness);
		}
	}

	static class StringIntPair implements Comparable<StringIntPair> {
		String word;
		float badness;

		StringIntPair(String s, float b) {
			word = s;
			badness = b;
		}

		@Override
		public int compareTo(StringIntPair o) {
			return Float.compare(o.badness, badness);
		}

	}

	class Task1Panel extends JPanel implements ActionListener {
		JPanel main;
		JSpinner year;
		JButton submit;

		Task1Panel() {
			super();
			this.setLayout(new BorderLayout());
			this.main = new JPanel(new GridBagLayout());

			// Line 1 - Year
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			this.main.add(new JLabel("Year"), c);
			this.year = new JSpinner(ClientApp.getModel());
			this.year.setEditor(ClientApp.getEditor(this.year));
			c = (GridBagConstraints) c.clone();
			c.gridx = 2;
			this.main.add(this.year, c);

			c = (GridBagConstraints) c.clone();
			c.gridx = 1;
			c.weightx = 0.5;
			this.main.add(Box.createHorizontalGlue(), c);

			// Line 2 - Submit Button
			c = (GridBagConstraints) c.clone();
			c.fill = GridBagConstraints.BOTH;
			c.gridx = 0;
			c.gridy = 1;
			c.weightx = 0.0;
			c.gridwidth = 3;
			this.submit = new JButton("Submit");
			this.submit.setPreferredSize(new Dimension(300, 25));
			this.main.add(this.submit, c);
			this.submit.addActionListener(this);

			this.add(this.main, BorderLayout.NORTH);
			this.add(Box.createVerticalGlue(), BorderLayout.CENTER);

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int yr = (int) this.year.getValue();
			Object result = task1query(yr);
		}
	}

	class Task2Panel extends JPanel implements ActionListener {
		JTextField word;
		JSpinner year;
		JButton submit;
		JPanel main;

		Task2Panel() {
			super();
			this.setLayout(new BorderLayout());
			this.main = new JPanel(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints();

			// Line 1 - Word
			c.gridx = 0;
			c.gridy = 0;
			this.main.add(new JLabel("Word"), c);

			c = (GridBagConstraints) c.clone();
			c.gridx = 1;
			c.weightx = 0.5;
			this.main.add(Box.createHorizontalGlue(), c);

			c = (GridBagConstraints) c.clone();
			c.gridx = 2;
			c.weightx = 0.0;
			c.fill = GridBagConstraints.BOTH;
			this.word = new JTextField();
			this.main.add(this.word, c);

			// Line 2 - Year
			c = (GridBagConstraints) c.clone();
			c.gridx = 0;
			c.gridy = 1;
			this.main.add(new JLabel("Year"), c);

			c = (GridBagConstraints) c.clone();
			c.gridx = 1;
			c.weightx = 0.5;
			this.main.add(Box.createHorizontalGlue(), c);

			c = (GridBagConstraints) c.clone();
			c.gridx = 2;
			c.weightx = 0.0;
			this.year = new JSpinner(ClientApp.getModel());
			this.year.setEditor(ClientApp.getEditor(this.year));
			this.main.add(this.year, c);

			// Line 3 - Submit button
			c = (GridBagConstraints) c.clone();
			c.fill = GridBagConstraints.BOTH;
			c.gridx = 0;
			c.gridy = 2;
			c.gridwidth = 3;
			c.weightx = 1.0;
			this.submit = new JButton("Submit");
			this.submit.setPreferredSize(new Dimension(300, 25));
			this.main.add(this.submit, c);
			this.submit.addActionListener(this);

			this.add(this.main, BorderLayout.NORTH);
			this.add(Box.createVerticalGlue(), BorderLayout.CENTER);

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int yr = (int) this.year.getValue();
			String wrd = this.word.getText();
			try {
				Object result = task2query(wrd, yr);
			} catch (SQLException exception) {
				JOptionPane.showMessageDialog(null, "Database error!");
				exception.printStackTrace();
			}
		}
	}

	class Task3Panel extends JPanel implements ActionListener, ChangeListener {
		JTextField word;
		JSpinner startYear;
		JSpinner endYear;
		JButton submit;

		Task3Panel() {
			super();
			this.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			// Line 1 - Word
			c.gridx = 0;
			c.gridy = 0;
			this.add(new JLabel("Word"), c);

			c = (GridBagConstraints) c.clone();
			c.gridx = 1;
			c.weightx = 0.5;
			this.add(Box.createHorizontalGlue(), c);

			c = (GridBagConstraints) c.clone();
			c.fill = GridBagConstraints.BOTH;
			c.gridx = 2;
			c.gridy = 0;
			c.weightx = 0.0;
			this.word = new JTextField();
			this.add(this.word, c);

			// Row 2 - Start Year
			c = (GridBagConstraints) c.clone();
			c.gridx = 0;
			c.gridy = 1;
			c.weightx = 0.0;
			this.add(Box.createVerticalGlue(), c);
			this.add(new JLabel("Start Year"), c);

			c = (GridBagConstraints) c.clone();
			c.gridx = 1;
			c.weightx = 0.5;
			this.add(Box.createHorizontalGlue(), c);

			c = (GridBagConstraints) c.clone();
			c.gridx = 2;
			c.weightx = 0.0;
			this.startYear = new JSpinner(ClientApp.getModel());
			this.startYear.setEditor(ClientApp.getEditor(this.startYear));
			this.startYear.addChangeListener(this);
			this.add(this.startYear, c);

			// Row 3 - End Year
			c = (GridBagConstraints) c.clone();
			c.gridx = 0;
			c.gridy = 2;
			c.weightx = 0.0;
			this.add(new JLabel("End Year"), c);

			c = (GridBagConstraints) c.clone();
			c.gridx = 1;
			c.weightx = 0.5;
			this.add(Box.createHorizontalGlue(), c);

			c = (GridBagConstraints) c.clone();
			c.gridx = 2;
			c.weightx = 0.0;
			this.endYear = new JSpinner(ClientApp.getModel());
			this.endYear.setEditor(ClientApp.getEditor(this.endYear));
			this.endYear.addChangeListener(this);
			this.add(this.endYear, c);

			// Row 4 - Submit
			c = (GridBagConstraints) c.clone();
			c.gridx = 0;
			c.gridy = 3;
			c.gridwidth = 3;
			this.submit = new JButton("Submit");
			this.submit.addActionListener(this);
			this.add(this.submit, c);

			// Row 5 - Vertical Filler
			c = (GridBagConstraints) c.clone();
			c.gridx = 0;
			c.gridy = 4;
			c.gridwidth = 3;
			c.weighty = 1.0;
			this.add(Box.createVerticalGlue(), c);

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int styr = (int) this.startYear.getValue();
			int endyr = (int) this.endYear.getValue();
			String wrd = this.word.getText();
			Object result;
			if (styr == endyr) {
				try {
					result = task2query(wrd, styr);

				} catch (SQLException exception) {
					JOptionPane.showMessageDialog(null, "Database error!");
					exception.printStackTrace();
					return;
				}
			} else {
				if (styr > endyr) {
					result = null;
					JOptionPane.showMessageDialog(null, "Start Year must proceed End Year");
					return;
				}
				/*
				 * //TODO start and end year checks if(styr<)
				 */
				result = task3query(wrd, styr, endyr);
			}

		}

		@Override
		public void stateChanged(ChangeEvent e) {
			if (e.getSource() == this.startYear) {
				SpinnerNumberModel num = (SpinnerNumberModel) this.endYear.getModel();
				int min = (int) this.startYear.getValue();
				if ((int) num.getValue() < min) {
					num.setValue(min);
					this.endYear.repaint();
				}
			}
			if (e.getSource() == this.endYear) {
				SpinnerNumberModel num = (SpinnerNumberModel) this.startYear.getModel();
				int max = (int) this.endYear.getValue();
				if ((int) num.getValue() > max) {
					num.setValue(max);
					this.startYear.repaint();
				}
			}

		}
	}

	class Task4Panel extends JPanel implements ActionListener {
		JTextArea word;
		JSpinner year;
		JButton submit;
		JPanel main;

		Task4Panel() {
			super();
			this.setLayout(new BorderLayout());
			this.main = new JPanel(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints();

			// Line 1 - Word
			c.gridx = 0;
			c.gridy = 0;
			this.main.add(new JLabel("Word"), c);

			// c=(GridBagConstraints) c.clone();
			// c.gridx=1;
			// c.weightx=0.5;
			// this.main.add(Box.createHorizontalGlue(),c);

			c = (GridBagConstraints) c.clone();
			c.gridx = 1;
			c.weightx = 0.0;
			c.fill = GridBagConstraints.BOTH;
			this.word = new JTextArea();
			this.main.add(this.word, c);

			// Line 2 - Year
			c = (GridBagConstraints) c.clone();
			c.gridx = 0;
			c.gridy = 1;
			this.main.add(new JLabel("Year"), c);

			c = (GridBagConstraints) c.clone();
			c.gridx = 1;
			c.weightx = 0.5;
			this.main.add(Box.createHorizontalGlue(), c);

			c = (GridBagConstraints) c.clone();
			c.gridx = 2;
			c.weightx = 0.0;
			this.year = new JSpinner(ClientApp.getModel());
			this.year.setEditor(ClientApp.getEditor(this.year));
			this.main.add(this.year, c);

			// Line 3 - Submit button
			c = (GridBagConstraints) c.clone();
			c.fill = GridBagConstraints.BOTH;
			c.gridx = 0;
			c.gridy = 2;
			c.gridwidth = 3;
			c.weightx = 1.0;
			this.submit = new JButton("Submit");
			this.submit.setPreferredSize(new Dimension(300, 25));
			this.main.add(this.submit, c);
			this.submit.addActionListener(this);

			this.add(this.main, BorderLayout.NORTH);
			this.add(Box.createVerticalGlue(), BorderLayout.CENTER);

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int yr = (int) this.year.getValue();
			String wrd = this.word.getText();
			try {
				task4query(wrd, yr);
			} catch (SQLException exception) {
				JOptionPane.showMessageDialog(null, "Database error!");
				exception.printStackTrace();
			}
		}

	}

	class Task5Panel extends JPanel implements ActionListener {
		JTextArea word;
		JSpinner year;
		JButton submit;
		JPanel main;

		Task5Panel() {
			super();
			this.setLayout(new BorderLayout());
			this.main = new JPanel(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints();

			// Line 1 - Word
			c.gridx = 0;
			c.gridy = 0;
			this.main.add(new JLabel("Word"), c);

			// c=(GridBagConstraints) c.clone();
			// c.gridx=1;
			// c.weightx=0.5;
			// this.main.add(Box.createHorizontalGlue(),c);

			c = (GridBagConstraints) c.clone();
			c.gridx = 1;
			c.weightx = 0.0;
			c.fill = GridBagConstraints.BOTH;
			this.word = new JTextArea();
			this.main.add(this.word, c);

			// Line 2 - Year
			c = (GridBagConstraints) c.clone();
			c.gridx = 0;
			c.gridy = 1;
			this.main.add(new JLabel("Year"), c);

			c = (GridBagConstraints) c.clone();
			c.gridx = 1;
			c.weightx = 0.5;
			this.main.add(Box.createHorizontalGlue(), c);

			c = (GridBagConstraints) c.clone();
			c.gridx = 2;
			c.weightx = 0.0;
			this.year = new JSpinner(ClientApp.getModel());
			this.year.setEditor(ClientApp.getEditor(this.year));
			this.main.add(this.year, c);

			// Line 3 - Submit button
			c = (GridBagConstraints) c.clone();
			c.fill = GridBagConstraints.BOTH;
			c.gridx = 0;
			c.gridy = 2;
			c.gridwidth = 3;
			c.weightx = 1.0;
			this.submit = new JButton("Submit");
			this.submit.setPreferredSize(new Dimension(300, 25));
			this.main.add(this.submit, c);
			this.submit.addActionListener(this);

			this.add(this.main, BorderLayout.NORTH);
			this.add(Box.createVerticalGlue(), BorderLayout.CENTER);

		}

		String generate(String qword, int qyear) throws SQLException {
			Connection con = DriverManager.getConnection("jdbc:hive2://hadoop-12.csse.rose-hulman.edu:10000/default",
					"root", "ngrams490");//
			int year = qyear;
			String result = qword + " ";
			String last = qword;

			int length = 0;

			Statement stmt = con.createStatement();
			// stmt.setQueryTimeout(30);

			String tableName = "testset";
			ResultSet res;

			while (length < 8) {
				length++;
				String sql = "select secondword from " + tableName + " where year='" + qyear + "' AND firstword='"
						+ last + "'";
				System.out.println("Running: " + sql);
				res = stmt.executeQuery(sql);
				System.out.println("Executed");
				res.next();
				last = res.getString(1);
				result += last + " ";
				System.out.println(last);
				System.out.println(result);

			}
			con.close();
			return result;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int yr = (int) this.year.getValue();
			String wrd = this.word.getText();
			String res = "Database error!";
			try {
				res = generate(wrd, yr);
			} catch (SQLException exception) {
				exception.printStackTrace();
			}

			JOptionPane.showMessageDialog(null, res);
		}

	}

	public static void main(String[] args) {
		ClientApp app = new ClientApp();

	}

	Object task1query(int yr) {
		return null;
	}
}
