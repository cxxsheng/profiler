package com.cxxsheng.profiler.ui.tracearea;

import com.android.tools.adtui.util.SwingUtil;
import com.cxxsheng.profiler.ui.ContentPanel;
import com.cxxsheng.profiler.ui.JIconTextField;
import com.cxxsheng.profiler.ui.TabbedPane;
import com.cxxsheng.profiler.ui.treemodel.profiler.JNode;
import com.cxxsheng.profiler.ui.treemodel.profiler.JRoot;
import com.cxxsheng.profiler.ui.treemodel.profiler.JTrace;
import com.cxxsheng.profiler.utils.UiUtils;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class TraceContentPanel extends ContentPanel {

  private JTree tree;
  private JNode root;
  private DefaultTreeModel treeModel;
  private JIconTextField searchText;

  private List<JNode> boldNodes = new ArrayList();

  public JTree getTree() {
    return tree;
  }

  public TraceContentPanel(TabbedPane panel, JNode jnode) {
    super(panel, jnode);
    root= new JRoot(jnode.getParent().getDescription());
    treeModel = new DefaultTreeModel(root);
    tree = new JTree(treeModel);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
        } else if (SwingUtilities.isRightMouseButton(e)) {
        }
      }
    });
    tree.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          //nodeClickAction(tree.getLastSelectedPathComponent());
        }
      }
    });
    tree.setCellRenderer(new DefaultTreeCellRenderer() {
      @Override
      public Component getTreeCellRendererComponent(JTree tree,
                                                    Object value, boolean selected, boolean expanded,
                                                    boolean isLeaf, int row, boolean focused) {
        Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, focused);
        if (value instanceof JNode) {
          JNode node = (JNode)value;
          setIcon(node.getIcon());
          if (node.isSearchedFather()){
            Font font = getFont();
            Font newFont = new Font(font.getName(),Font.BOLD, font.getSize());
            setFont(newFont);
          }else {
              Font font = getFont();
              Font newFont = new Font(font.getName(), Font.PLAIN,font.getSize());
              setFont(newFont);
          }

          if (node.isSearched()){
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
          }else {
            setBorder(null);
          }
        }
        else
          setIcon(UiUtils.openIcon("package"));
        return c;
      }
    });
    tree.addTreeWillExpandListener(new TreeWillExpandListener() {
      @Override
      public void treeWillExpand(TreeExpansionEvent event) {
      }

      @Override
      public void treeWillCollapse(TreeExpansionEvent event) {
        //if (!treeReloading) {
          //project.removeTreeExpansion(getPathExpansion(event.getPath()));
          //update();
        //}
      }
    });


    JTrace jTrace = (JTrace)jnode;
    for (JNode node : jTrace.getContentNodes()){
      root.add(node);
    };


    setLayout(new BorderLayout());
    JScrollPane treeScrollPane = new JScrollPane(tree);
    treeScrollPane.setMinimumSize(new Dimension(800, 150));
    add(treeScrollPane);


    searchText = new JIconTextField(UiUtils.openIconTest("close")) {
      @Override
      public void handleClick(JIconTextField jSearchTextField) {
        clearAllNodes();
        setSearchTextVisible(false);
      }
    };
    searchText.setVisible(false);

    searchText.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_ENTER){
          String searchString = searchText.getText();
          if (searchString == null)
              return;
          searchString = searchString.trim();
          if (searchString.length() != 0){
            doSearch(searchString);
          }
        }
        else if (e.getKeyChar() == KeyEvent.VK_ESCAPE){
          clearAllNodes();
          setSearchTextVisible(false);
        }
      }
    });


    add(searchText,BorderLayout.NORTH);

  }

  public synchronized void setSearchTextVisible(boolean flag){
    if (searchText != null )
    {
      if (searchText.isVisible() != flag)
      {
        searchText.setVisible(flag);
        SwingUtilities.invokeLater(this::updateUI);
      }
      searchText.grabFocus();
    }
  }

  private void beforeSearch(){
    clearAllNodes();
    searchText.setEnabled(false);
    searchText.setBackground(Color.GRAY);
  }

  private void afterSearch(){
    searchText.setEnabled(true);
    searchText.setBackground(Color.WHITE);
  }

  private void doSearch(@NotNull String searchString){
    beforeSearch();
    getSearchedNodesLoop(root, searchString);
    setSearchedNodePathsFlag();
    afterSearch();
    SwingUtilities.invokeLater(tree::updateUI);
  }


  synchronized private void getSearchedNodesLoop(@NotNull JNode node, @NotNull String searchString){
      if (node.toString().contains(searchString)){
          boldNodes.add(node);
      }
      if (node.hasChildren())
          for (TreeNode child : node.getChildren()){
              if (child instanceof JNode){
                  getSearchedNodesLoop((JNode)child, searchString);
              }
          }
  }

  private void setNodesFlagLoop(@NotNull JNode node, boolean flag){
    node.setSearchedFather(flag);
    if (node.hasParent()){
      setNodesFlagLoop(node.getParent(), flag);
    }
  }

  synchronized private void setSearchedNodePathsFlag() {
      for (JNode node:boldNodes){
        node.setSearchedFlag(true);
        setNodesFlagLoop(node,true);
      }
  }

  private void clearAllNodes(){
      for (JNode node : boldNodes){
        node.setSearchedFlag(false);
        setNodesFlagLoop(node, false);
      }
      boldNodes.clear();
      SwingUtilities.invokeLater(tree::updateUI);

  }

  @Override
  public void loadSettings() {
  }


}
