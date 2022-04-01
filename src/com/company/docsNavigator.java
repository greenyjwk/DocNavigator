package com.company;
// Java imports
import java.awt.Container;
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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import java.io.File;

// class docsNavigator starts
public class docsNavigator extends JFrame {

    PositionalIndex pi;
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

    private boolean validate = false;
    private boolean revalidate = true;
    private boolean repaint = true;

    /**
     * Construct a docNavigator object
     */
    public docsNavigator() {

        // GUI settings
        setSize(750, 200);
        setLocation(350, 275);
        setTitle("DocsNavigator");
        setMinimumSize(new Dimension(750, 200));

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
        retrievedDocumentsPanel.setLayout(new GridLayout(0, 2));
        //retrievedDocumentsPanel.setPreferredSize(new Dimension(500, 150));
        

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
                if(searchBox.getText().equals("") ){
                    JOptionPane.showMessageDialog(null, "Please write your query");
                    return;
                }
                String[] searchTerm = searchBox.getText().split(" ");
                searchResults = pi.phraseQuery(searchTerm);
                int counter = 0;
                for (Doc doc : searchResults) {
                    System.out.println("Doc Id is : " + doc.docId);
                    int id = doc.docId;
                    retrievedDocuments.add(pi.fileNames.get(id));
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
                    JFileChooser fc = new JFileChooser();
                    fc.setVisible(false);
                    Path path = Paths.get(fileLocation);
                    File file = path.toFile();

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

        searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout());
        searchPanel.add(location);
        searchPanel.add(browse);
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

}// end - docsNavigator
