package de.je.util.javautil.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * {@link SwingUtility} contains some Swing helpers
 */
public class SwingUtility {

   /**
    * Enables or disables all children of a given {@link Container}.
    * 
    * @param c
    *           The container
    * @param bEnable
    *           true to enable, false to disable
    */
   public static void enableAllChildren(Container c, boolean bEnable) {
      Component[] children = c.getComponents();

      for (int i = 0; i < children.length; ++i)
         children[i].setEnabled(bEnable);
   }

   /**
    * Creates a menu item with items and subitems (recursive structure). The created menu item is returned.
    * 
    * @param m
    *           The {@link ExtendedMenuItem} as basis for the {@link JComponent}
    * @param a
    *           An {@link ActionListener} to register with the menu items
    * @param imageFolder
    *           An optional folder for images for each menu item or null if none. Each icon file is expected to have the
    *           name of the menu item.
    * @return The created {@link JComponent} menu component
    */
   public static JComponent getMenuItem(ExtendedMenuItem m, ActionListener a, String imageFolder) {
      if (m.subItems != null) {
         JMenu ret = new JMenu(m.name);

         for (int i = 0; i < m.subItems.length; ++i)
            if (m.subItems[i] == null)
               ret.addSeparator();

            else
               ret.add(getMenuItem(m.subItems[i], a, imageFolder));

         if (m.mnemonic != 0)
            ret.setMnemonic(m.mnemonic);
         if (m.iconName != null)
            ret.setIcon(new ImageIcon((imageFolder != null ? imageFolder : "") + m.iconName));
         if (a != null)
            ret.addActionListener(a);

         return ret;
      }

      JMenuItem ret = new JMenuItem(m.name);

      if (m.key != null)
         ret.setAccelerator(m.key);
      if (m.mnemonic != 0)
         ret.setMnemonic(m.mnemonic);
      if (m.iconName != null)
         ret.setIcon(new ImageIcon((imageFolder != null ? imageFolder : "") + m.iconName));
      if (a != null)
         ret.addActionListener(a);

      return ret;
   }

   /**
    * Returns a {@link GregorianCalendar} instance based on the given {@link Date}
    * 
    * @param d
    *           The {@link Date} to convert.
    * @return a {@link GregorianCalendar} instance based on the given {@link Date}
    */
   public static GregorianCalendar gregCalFromDate(Date d) {
      GregorianCalendar g = new GregorianCalendar();

      g.setTime(d);

      return g;
   }
}

/**
 * {@link ExtendedMenuItem} is a class for menu items with extended properties
 */
class ExtendedMenuItem {

   /**
    * Creates a new {@link ExtendedMenuItem}.
    * 
    * @param name
    *           The name of the menu item
    * @param mnemonic
    *           The mnemonic of the menu item
    * @param iconName
    *           The name of the icon file
    * @param key
    *           The key of the menu item
    * @param subItems
    *           The sub items of the menu item
    */
   public ExtendedMenuItem(String name, int mnemonic, String iconName, KeyStroke key, ExtendedMenuItem[] subItems) {
      this.name = name;
      this.mnemonic = mnemonic;
      this.iconName = iconName;
      this.key = key;
      this.subItems = subItems;
   }

   /**
    * The name of the menu item
    */
   public String name;
   /**
    * The mnemonic of the menu item
    */
   public int mnemonic;
   /**
    * The name of the icon file
    */
   public String iconName;
   /**
    * The sub items of the menu item
    */
   public KeyStroke key;
   /**
    * The key of the menu item
    */
   public ExtendedMenuItem[] subItems;
}

/**
 * {@link GridBagHandler} is a class for easier working with the {@link GridBagLayout}.
 */
class GridBagHandler {

   /**
    * Creates a new {@link GridBagHandler}.
    * 
    * @param container
    *           The container
    */
   public GridBagHandler(Container container) {
      m_Layouted = container;

      m_Layouted.setLayout(m_Layout);
   }

   /**
    * Convenient setting of constraints
    * 
    * @param c
    * @param x
    * @param y
    */
   public void setConstraints(Component c, int x, int y) {
      setConstraints(c, x, y, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,
         0);
   }

   /**
    * Convenient setting of constraints
    * 
    * @param c
    * @param x
    * @param y
    * @param width
    */
   public void setConstraints(Component c, int x, int y, int width) {
      setConstraints(c, x, y, width, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
         new Insets(0, 0, 0, 0), 0, 0);
   }

   /**
    * Convenient setting of constraints
    * 
    * @param c
    * @param x
    * @param y
    * @param width
    * @param anchor
    */
   public void setConstraints(Component c, int x, int y, int width, int anchor) {
      setConstraints(c, x, y, width, 1, 0, 0, anchor, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
   }

   /**
    * Convenient setting of constraints
    * 
    * @param c
    * @param x
    * @param y
    * @param width
    * @param anchor
    * @param i
    */
   public void setConstraints(Component c, int x, int y, int width, int anchor, Insets i) {
      setConstraints(c, x, y, width, 1, 0, 0, anchor, GridBagConstraints.NONE, i, 0, 0);
   }

   /**
    * Convenient setting of constraints
    * 
    * @param c
    * @param x
    * @param y
    * @param width
    * @param height
    * @param wx
    * @param wy
    * @param anchor
    * @param fill
    * @param i
    * @param ipadx
    * @param ipady
    */
   public void setConstraints(Component c, int x, int y, int width, int height, double wx, double wy, int anchor,
      int fill, Insets i, int ipadx, int ipady) {
      GridBagConstraints cbc = new GridBagConstraints(x, y, width, height, wx, wy, anchor, fill, i, ipadx, ipady);

      m_Layout.setConstraints(c, cbc);

      m_Layouted.add(c);

      if (y > m_MaxY)
         m_MaxY = y;

      if (x + width > m_MaxWidth)
         m_MaxWidth = x + width;
   }

   /**
    * Adds an invisible element to the container, which is used for correctly positioning every other element in the
    * layout.
    */
   public void AddPositioner() {
      setConstraints(new JLabel(), 0, m_MaxY + 1, m_MaxWidth, 1, 1, 1, GridBagConstraints.NORTH,
         GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
   }

   /*
    * +++++++++++++ Attributes +++++++++++++
    */

   private GridBagLayout m_Layout = new GridBagLayout();
   private Container m_Layouted = null;
   private int m_MaxY = 0;
   private int m_MaxWidth = 0;
}
