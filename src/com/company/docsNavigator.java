// Java imports
package com.company;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.regex.Pattern;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import java.io.File;
import java.io.IOException;

// class docsNavigator starts
public class docsNavigator extends JFrame {

    PositionalIndex pi;
    ArrayList<Doc> searchResults;
    ButtonGroup returnedDocumentsOptions = new ButtonGroup();

    WindowListener exitListener = null;
    // GUI

    // JTextFields
    JTextField location, searchBox;

    // TextAreas
    JTextArea resultArea;

    // Buttons
    JButton browse, search, view, clear;

    // Panels
    JPanel browsePanel, searchPanel, viewPanel;

    // Scrollpane
    JScrollPane scrollPane, resultAreaScrollPane;

    // Filechooser

    JFileChooser fileChooser;

    // selected folder
    File selectedFolder;

    // constructor
    public docsNavigator() {

        // GUI settings
        setSize(750, 280);
        setLocation(300, 50);
        setTitle("DocsNavigator");
        setMinimumSize(new Dimension(750, 280));

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        exitListener = new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showOptionDialog(
                        null, "Are you sure you want to close the application?",
                        "Exit Confirmation", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (confirm == 0)
                    System.exit(0);
            }
        };
        addWindowListener(exitListener);

        // GUI layout
        // main container
        Container container = getContentPane();
        container.setLayout(new GridLayout(0, 1));

        location = new JTextField(50);
        searchBox = new JTextField(30);

        resultArea = new JTextArea(10, 40);
        JScrollPane resultAreaScrollPane = new JScrollPane(resultArea);

        browse = new JButton("Browse");
        browse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String folder = getFolder();
                pi = new PositionalIndex(folder);
                String folderBoxText = "Your selected directory is: " + folder;
                location.setText(folderBoxText);
            }
        });

        search = new JButton("Search");
        search.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String[] searchTerm = searchBox.getText().split(" ");
                searchResults = pi.phraseQuery(searchTerm);

                // For test
                System.out.println("\nDoc ID search Result");
                for (Doc doc : searchResults)
                    System.out.println(doc.docId);
                // For test

                // Reset the search result for the next search
                searchResults = new ArrayList<>();
            }
        });

        view = new JButton("View Document");
        view.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(searchResults);
            }
        });

        clear = new JButton("Clear");
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchBox.setText("");
                resultArea.setText("");
            }
        });

        JScrollPane scrollPane = new JScrollPane(viewPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // browsePanel = new JPanel();
        // browsePanel.setLayout(new FlowLayout());
        // browsePanel.add(location);
        // browsePanel.add(browse);

        searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout());
        searchPanel.add(location);
        searchPanel.add(browse);
        searchPanel.add(searchBox);
        searchPanel.add(search);
        searchPanel.add(clear);
        searchPanel.add(resultArea);
        searchPanel.add(view);

        // viewPanel = new JPanel();
        // viewPanel.setLayout(new FlowLayout());
        // viewPanel.add(resultArea);
        // viewPanel.add(view);

      //  container.add(browsePanel);
        container.add(searchPanel);
        //container.add(viewPanel);

        setVisible(true);
        setResizable(false);
    }// end of constructor

    public String getFolder() {
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new java.io.File(".")); // start at application current directory
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            selectedFolder = fileChooser.getSelectedFile();
        }

        String current = selectedFolder.getAbsolutePath();
        int index = current.lastIndexOf('/');
        String finalFolder = current.substring(0, index);

        return finalFolder;
    }

    public static void main(String[] args) {
        docsNavigator search = new docsNavigator();
    }// end of main

}// end of docsNavigator