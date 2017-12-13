/**
 *
 * {@link ShowMetaData}.java
 *
 * @author Jens Ebert
 *
 * @date 08.01.2009
 *
 */
package com.github.jmeta.tools.datablockviewer.api.services;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link DataBlockViewerMainDialog} is a dialog that is able to show all meta data in an audio file the user may
 * choose.
 */
public class DataBlockViewerMainDialog extends JFrame {

   /**
    * Initializes and displays the window.
    */
   public void init() {

      setLayout(new GridBagLayout());
      JScrollPane scrollPane = new JScrollPane(m_metaDataTree);
      add(m_metaDataLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.BOTH,
         new Insets(2, 2, 2, 2), 0, 0));
      add(scrollPane, new GridBagConstraints(0, 1, 7, 1, 0, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH,
         new Insets(2, 2, 2, 2), 0, 0));
      add(m_fileLabel, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.BOTH,
         new Insets(2, 2, 2, 2), 0, 0));
      add(m_fileName, new GridBagConstraints(1, 2, 5, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.BOTH,
         new Insets(2, 2, 2, 2), 0, 0));
      add(m_fileChooserButton, new GridBagConstraints(6, 2, 1, 1, 0, 0, GridBagConstraints.NORTH,
         GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
      add(m_separator, new GridBagConstraints(0, 3, 7, 1, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.BOTH,
         new Insets(10, 2, 10, 2), 0, 0));
      add(m_readMetaDataButton, new GridBagConstraints(0, 4, 3, 1, 0, 0, GridBagConstraints.NORTH,
         GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
      add(m_clearButton, new GridBagConstraints(4, 4, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.BOTH,
         new Insets(2, 2, 2, 2), 0, 0));
      add(m_closeButton, new GridBagConstraints(5, 4, 2, 1, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.BOTH,
         new Insets(2, 2, 2, 2), 0, 0));

      m_closeButton.addActionListener(m_closeAction);
      m_clearButton.addActionListener(m_clearAction);
      m_readMetaDataButton.addActionListener(m_readAction);
      m_fileChooserButton.addActionListener(m_chooseFileAction);

      m_chooser.setCurrentDirectory(new File(MUSIC_PATH));

      setDefaultCloseOperation(EXIT_ON_CLOSE);
      setTitle("Datablock viewer");
      setVisible(true);
      setSize(new Dimension(400, 300));
      setLocation(400, 300);

      // Refresh main window to show all controls directly
      validate();
   }

   private final Action m_closeAction = new AbstractAction() {

      private static final long serialVersionUID = 1L;

      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent arg0) {

         Reject.ifNull(arg0, "arg0");

         System.exit(0);
      }
   };

   private final Action m_clearAction = new AbstractAction() {

      private static final long serialVersionUID = 1L;

      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent arg0) {

         Reject.ifNull(arg0, "arg0");

         m_metaDataTree.removeAll();
         m_metaDataTree.setModel(null);
         m_metaDataTree.repaint();
         m_fileName.setText("");
      }
   };

   private final Action m_chooseFileAction = new AbstractAction() {

      private static final long serialVersionUID = 1L;

      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent arg0) {

         Reject.ifNull(arg0, "arg0");

         if (m_chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File chosenFile = m_chooser.getSelectedFile();

            m_fileName.setText(chosenFile.getAbsolutePath());
         }
      }
   };

   private final Action m_readAction = new AbstractAction() {

      private static final long serialVersionUID = 1L;

      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent arg0) {

         Reject.ifNull(arg0, "arg0");

         File fileToRead = new File(m_fileName.getText());

         if (!fileToRead.exists()) {
            JOptionPane.showMessageDialog(null, "The file : " + fileToRead.getAbsolutePath() + " does not exist.",
               "File does not exist", JOptionPane.OK_OPTION);
            return;
         }

         Iterator<Container> allAttributes = null;

         try {
            allAttributes = m_reader.getAllContainersFromFile(fileToRead);

            fillTree(allAttributes, fileToRead.getName());
         } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
               "Exception during reading the file : " + fileToRead.getAbsolutePath() + ": " + e,
               "Problem while reading", JOptionPane.OK_OPTION);
         }
      }
   };

   /**
    * Fills the attribute tree with data.
    *
    * @param topLevelContainers
    *           The attributes to fill.
    * @param fileName
    *           The file name.
    */
   private void fillTree(Iterator<Container> topLevelContainers, String fileName) {

      DefaultMutableTreeNode root = new DefaultMutableTreeNode("All Meta Data in " + fileName);

      fillInContainer(topLevelContainers, root);

      m_metaDataTree.setModel(new DefaultTreeModel(root));
   }

   private void fillInContainer(Iterator<Container> containers, DefaultMutableTreeNode parent) {

      while (containers.hasNext()) {
         Container nextOne = containers.next();

         DefaultMutableTreeNode tagNode = new DefaultMutableTreeNode(nextOne.getId());

         parent.add(tagNode);

         fillInHeaders(nextOne, nextOne.getHeaders(), tagNode);

         Payload payload = nextOne.getPayload();
         DefaultMutableTreeNode payloadNode = new DefaultMutableTreeNode(payload.getId());

         parent.add(payloadNode);

         fillInContainer(payload.getContainerIterator(), payloadNode);

         fillInFields(payloadNode, payload.getFields());

         fillInHeaders(nextOne, nextOne.getFooters(), tagNode);
      }
   }

   private void fillInFields(DefaultMutableTreeNode parent, List<Field<?>> fields) {

      for (int i = 0; i < fields.size(); ++i) {
         Field<?> field = fields.get(i);

         DefaultMutableTreeNode fieldNode = new DefaultMutableTreeNode(field.getId());

         parent.add(fieldNode);

         try {
            fieldNode.add(new DefaultMutableTreeNode(field.getStringRepresentation()));
         } catch (BinaryValueConversionException e) {
         }
      }
   }

   private void fillInHeaders(Container container, List<Header> headers, DefaultMutableTreeNode parent) {

      for (int i = 0; i < headers.size(); ++i) {
         Header header = headers.get(i);

         DefaultMutableTreeNode headerNode = new DefaultMutableTreeNode(header.getId());

         parent.add(headerNode);

         fillInFields(headerNode, header.getFields());
      }
   }

   private static final String MUSIC_PATH = "C:\\";

   private final DataBlockReader m_reader = new DataBlockReader();

   private final JFileChooser m_chooser = new JFileChooser();

   private final JLabel m_metaDataLabel = new JLabel("DataBlock structure:");

   private final JLabel m_fileLabel = new JLabel("File:");

   private final JTextField m_fileName = new JTextField();

   private final JButton m_fileChooserButton = new JButton("..");

   private final JTree m_metaDataTree = new JTree(new Object[] { "< No file selected >" });

   private final JButton m_readMetaDataButton = new JButton("Read DataBlocks");

   private final JButton m_clearButton = new JButton("Clear");

   private final JButton m_closeButton = new JButton("Close");

   private final JSeparator m_separator = new JSeparator();

   private static final long serialVersionUID = 1L;
}
