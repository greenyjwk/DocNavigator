// Java imports
package com.company;

import java.awt.Container;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.Desktop;
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
    ArrayList<String> retrievedDocuments = new ArrayList<String>();
    ButtonGroup returnedDocumentsOptions = new ButtonGroup();
    File fileToView = new File("test");

    WindowListener exitListener = null;
    // GUI

    // JTextFields
    JTextField location, searchBox;

    // TextAreas
    JTextArea resultArea;

    // Buttons
    JButton browse, search, view, clear;

    // Panels
    JPanel browsePanel, searchPanel, viewPanel, retrievedDocumentsPanel;

    // Scrollpane
    JScrollPane scrollPane, resultAreaScrollPane;

    // Filechooser

    JFileChooser fileChooser;

    // selected folder
    File selectedFolder;

    private boolean validate = false;
    private boolean revalidate = true;
    private boolean repaint = true;


    /**
     * Construct a docNavigator
     */
    public docsNavigator() {

        // GUI settings
        setSize(750, 300);
        setLocation(300, 400);
        setTitle("DocsNavigator");
        setMinimumSize(new Dimension(750, 300));

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

        retrievedDocumentsPanel = new JPanel();
        retrievedDocumentsPanel.setLayout(new GridLayout(0, 1));

        scrollPane = new JScrollPane(retrievedDocumentsPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        browse = new JButton("Browse");
        browse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {

                    String folder = getFolder(true);
                    pi = new PositionalIndex(folder);
                    String folderBoxText = "Your selected directory is: " + folder;
                    location.setText(folderBoxText);

                } catch (Exception eb) {
                    eb.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Something went wrong! Please try again");
                }

                JOptionPane.showMessageDialog(null, "Positional Index successfully created");
            }
        });

        search = new JButton("Search");
        search.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                retrievedDocumentsPanel.removeAll();
                retrievedDocumentsPanel.repaint();
                retrievedDocumentsPanel.revalidate();
                String[] searchTerm = searchBox.getText().split(" ");
                searchResults = pi.phraseQuery(searchTerm);
                int counter = 0;
                // For test
                // System.out.println("\nDoc ID search Result");
                for (Doc doc : searchResults) {
                    System.out.println(doc.docId);
                    int id = doc.docId;
                    // String result = "" + id;
                    retrievedDocuments.add(pi.fileNames.get(id));
                    // retrievedDocuments.add(result);
                    counter++;
                }

                for (int j = 0; j < counter; j++) {
                    JRadioButton radio = new JRadioButton(retrievedDocuments.get(j));
                    radio.setActionCommand(retrievedDocuments.get(j));
                    returnedDocumentsOptions.add(radio);
                    retrievedDocumentsPanel.add(radio);
                }

                if (validate) {
                    retrievedDocumentsPanel.validate();
                }
                if (revalidate) {
                    retrievedDocumentsPanel.revalidate();
                }
                if (repaint) {
                    retrievedDocumentsPanel.repaint();
                }

                if (retrievedDocuments.size() == 0){
                    JOptionPane.showMessageDialog(null, "No documents were found. Please try another query");
                }

                // Reset the search result for the next search
                searchResults = new ArrayList<Doc>();
                retrievedDocuments = new ArrayList<String>();
            }
        });

        view = new JButton("View Document");
        view.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                try{
                    String folder = getFolder(false);
                    String fileLocation = folder + "/" + returnedDocumentsOptions.getSelection().getActionCommand();
                    //File f = new File(fileLocation);
                    //fileToView = new File(fileLocation);

                    JFileChooser fc = new JFileChooser();
                    fc.setVisible(false);
                    Path path = Paths.get(fileLocation);
                    File file = path.toFile();
                    //fc.setSelectedFile(file);
                    
                   // if (f.exists()) 
                    {
                        if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
                        else System.out.println("File does not exists!");
                    }
                } catch(Exception ert) {
                    ert.printStackTrace();
                }
            }
        });

        clear = new JButton("Clear");
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchBox.setText("");
                retrievedDocumentsPanel.removeAll();
                retrievedDocumentsPanel.repaint();
                retrievedDocumentsPanel.revalidate();
            }
        });

        // JScrollPane scrollPane = new JScrollPane(viewPanel,
        // ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        // ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

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
        searchPanel.add(view);
        searchPanel.add(clear);
        // searchPanel.add(resultArea);
        // retrievedDocumentsPanel.add(view);

        // viewPanel = new JPanel();
        // viewPanel.setLayout(new FlowLayout());
        // viewPanel.add(resultArea);
        // viewPanel.add(view);

        // container.add(browsePanel);
        container.add(searchPanel);
        container.add(retrievedDocumentsPanel);
        // container.add(viewPanel);

        setVisible(true);
        setResizable(false);
    }// end of constructor


    /**
     * Access to the current directory path to retrieve files
     * @param showDialogValid valid value that determines to show diaglog
     * @return path string
     */
    public String getFolder(boolean showDialogValid) {
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new java.io.File(".")); // start at application current directory
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnVal;
        if(showDialogValid) returnVal = fileChooser.showSaveDialog(this);
        else returnVal = 1;

        if (returnVal == JFileChooser.APPROVE_OPTION) selectedFolder = fileChooser.getSelectedFile();
        String current = selectedFolder.getAbsolutePath();
        int index = current.lastIndexOf('/');
        String finalFolder = current.substring(0, index);

        return finalFolder;
    }

    public static void main(String[] args) {
        docsNavigator search = new docsNavigator();
    }// end of main

}// end of docsNavigator