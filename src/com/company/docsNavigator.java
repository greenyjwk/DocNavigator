package com.company;
// Java imports
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import javax.swing.*;
import java.io.File;

// class docsNavigator starts
@SuppressWarnings("unchecked")
public class docsNavigator extends JFrame {
    InvertedIndex pi;
    ArrayList<Doc> searchResults;
    ArrayList<String> retrievedDocuments = new ArrayList<String>();
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
    JPanel browsePanel, searchPanel, viewPanel, retrievedDocumentsPanel;
    // Scrollpane
    JScrollPane scrollPane, resultAreaScrollPane;
    // Filechooser
    JFileChooser fileChooser;
    // selected folder
    File selectedFolder;
    // JLabels
    JLabel info = new JLabel("Welcome to DocsNavigator. With this program you can search through files on your computer based on file contents");
    JLabel info2 = new JLabel("1. Please click the Browse button to select the folder where you want to search");
    JLabel info3 = new JLabel("2. Please write your query in the field below then click the Search button to show the results");
    JLabel info4 = new JLabel("Queries can have from any number of words. The program will return all documents that have those words");
    JLabel info5 = new JLabel("After results are returned, click the small circle in front of any document and click View Document to view it");
    
    private boolean validate = false;
    private boolean revalidate = true;
    private boolean repaint = true;
    float time;

    /**
     * Construct a docNavigator object
     */
    public docsNavigator() {

        // GUI settings
        setSize(800, 370);
        setLocation(350, 275);
        setTitle("DocsNavigator");
        setMinimumSize(new Dimension(800, 370));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Window adapter for existing the GUI
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
        retrievedDocumentsPanel.setLayout(new GridLayout(0, 2));
        scrollPane = new JScrollPane(retrievedDocumentsPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Browse Button
        // - let users select directory from the file system
        // - generates the positional index
        // - notify the user whether the positional indexes are successfully generated
        browse = new JButton("Browse");
        browse.setToolTipText("Navigate to the folder where your files are. Make sure to actually navigate to the exact folder");
        browse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String folder = getFolder(true);

                    long startTime = System.currentTimeMillis();
                    pi = new InvertedIndex(folder);
                    // get the end time
                    long endTime = System.currentTimeMillis();
                    time = ((endTime - startTime) / 1000);
                    double totalTime = (time / 60);
                    System.out.println(
                            "\nTime to build Inverted Index in seconds: " + ((endTime - startTime) / 1000)
                                    + " seconds");
                    System.out.println("Time to build Inverted Index in minutes: "
                            + (Math.round(totalTime * 100.0) / 100.0) + " minutes \n");


                    System.out.println("\n\n");

                    String folderBoxText = "Your selected directory is: " + folder;
                    location.setText(folderBoxText);
                    JOptionPane.showMessageDialog(null, "Inverted Index successfully created");
                } catch (Exception eb) {
                    // eb.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Inverted Index not created! Please try selecting your directory again");
                }
            }
        });

        // Search Button
        // - let users use the phrase query terms to find the relevant documents
        search = new JButton("Search");
        search.setToolTipText("Write your query to the field on the left and click this button to show search");
        search.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                retrievedDocumentsPanel.removeAll();
                retrievedDocumentsPanel.repaint();
                retrievedDocumentsPanel.revalidate();
                // String regex = "\\s+";
                String query = searchBox.getText();
                query = query.replaceAll("\\s", "");
                if (query.equals("")) {
                    JOptionPane.showMessageDialog(null, "Please write your query");
                    return;
                }

                // retrieving the documents
                String[] searchTerm = searchBox.getText().split(" ");

                //search time measurement
                long searchStartTime = System.nanoTime();
                searchResults = pi.Search(searchTerm);
                long searchEndTime = System.nanoTime();

                int counter = 0;
                for (Doc doc : searchResults) {
                    int id = doc.docId;
                    retrievedDocuments.add(pi.fileNames.get(id));
                    counter++;
                }

                // generate the radio buttons
                for (int j = 0; j < counter; j++) {
                    JRadioButton radio = new JRadioButton(retrievedDocuments.get(j));
                    radio.setActionCommand(retrievedDocuments.get(j));
                    returnedDocumentsOptions.add(radio);
                    retrievedDocumentsPanel.add(radio);
                }
                // repainting the GUI
                if (validate) retrievedDocumentsPanel.validate();
                if (revalidate) retrievedDocumentsPanel.revalidate();
                if (repaint) retrievedDocumentsPanel.repaint();
                if (retrievedDocuments.size() == 0)
                    JOptionPane.showMessageDialog(null, "No documents were found. Please try another query");

                // Reset the search result for the next search
                searchResults = new ArrayList<Doc>();
                retrievedDocuments = new ArrayList<String>();
            }
        });


        // View Button
        // - let users view the returned documents
        view = new JButton("View Document");
        view.setToolTipText("After selecting any document in the document pane below, click this button to view the returned documents");
        view.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ButtonModel buttonModel = returnedDocumentsOptions.getSelection();
                boolean status;
                if (buttonModel == null) status = false;
                else status = true;

                if (status == false) JOptionPane.showMessageDialog(null, "Please select a document");
                else {
                    try {
                        String folder = getFolder(false);
                        String fileLocation = folder + "/" + returnedDocumentsOptions.getSelection().getActionCommand();
                        JFileChooser fc = new JFileChooser();
                        fc.setVisible(false);
                        Path path = Paths.get(fileLocation);
                        File file = path.toFile();
                        {
                            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
                            else System.out.println("File does not exists!");
                        }
                    } catch (Exception ert) {
                        JOptionPane.showMessageDialog(null, "Please select a document");
                        ert.printStackTrace();
                    }
                }
            }
        });

        // Clear Button
        // - clear all the fields except directory field
        clear = new JButton("Clear");
        clear.setToolTipText("Click this button to clear all fields except the folder location");
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                returnedDocumentsOptions.clearSelection();
                searchBox.setText("");
                retrievedDocumentsPanel.removeAll();
                retrievedDocumentsPanel.repaint();
                retrievedDocumentsPanel.revalidate();
            }
        });

        searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout());
        searchPanel.add(info);
        searchPanel.add(info2);
        searchPanel.add(location);
        searchPanel.add(browse);
        searchPanel.add(info3);
        searchPanel.add(info4);
        searchPanel.add(info5);
        searchPanel.add(searchBox);
        searchPanel.add(search);
        searchPanel.add(view);
        searchPanel.add(clear);
        container.add(searchPanel);
        container.add(scrollPane);
        setVisible(true);
        setResizable(false);
    }// end of constructor


    /**
     * Access to the current directory path to retrieve files
     *
     * @param showDialogValid valid value that determines to show diaglog
     * @return path string
     */
    public String getFolder(boolean showDialogValid) {
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new java.io.File(".")); // start at application current directory
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnVal;
        if (showDialogValid) returnVal = fileChooser.showSaveDialog(this);
        else returnVal = 1;
        String finalFolder = "";
        String temp = "";
        try {
            if (returnVal == JFileChooser.APPROVE_OPTION) selectedFolder = fileChooser.getSelectedFile();
            String current = selectedFolder.getAbsolutePath();
            int index = current.lastIndexOf('/');
            finalFolder = current.substring(0, index);
            temp = finalFolder;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error while getting path");
        }
        return temp;
    }

    public static void main(String[] args) {
        docsNavigator search = new docsNavigator();
    }// end of main
}// end - docsNavigator
