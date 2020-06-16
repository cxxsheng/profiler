package com.cxxsheng.profiler.ui;

import com.cxxsheng.profiler.ui.treemodel.profiler.JNode;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;

public abstract class ContentPanel extends JPanel {
  private static final long serialVersionUID = 578807556253338026L;
  protected final TabbedPane tabbedPane;
  protected final JNode node;

  protected ContentPanel(TabbedPane panel, JNode jnode) {
    tabbedPane = panel;
    node = jnode;
  }

  public abstract void loadSettings();

  public TabbedPane getTabbedPane() {
    return tabbedPane;
  }

  public JNode getNode() {
    return node;
  }

  /**
   * Allows to show a tool tip on the tab e.g. for displaying a long path of the
   * selected entry inside the APK file.
   *
   * If <code>null</code> is returned no tool tip will be displayed.
   *
   * @return
   */
  @Nullable
  public String getTabTooltip() {
    return null;
  }

}
