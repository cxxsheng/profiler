package com.cxxsheng.profiler.ui.treemodel.profiler;

import com.cxxsheng.profiler.utils.UiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class JRoot extends JNode {
  private static final long serialVersionUID = 1069175078833390455L;


  private boolean isSaved = false;

  public boolean isSaved() {
    return isSaved;
  }

  public void setSaved() {
    isSaved = true;
  }

  public Vector<TreeNode> getChildren(){
    return children;
  }

  @Override
  public String getDescription() {
    return "";
  }


  public JRoot(String description){
    this.description = description;
  }

  public synchronized void setDescription(@NotNull String description, @NotNull boolean status){
    this.description = description;
    this.status = status;
  }
  @Override
  public String toString() {
    return  description;
  }

  @Override
  public Icon getIcon() {
    return UiUtils.openIconTest("package");
  }


  @Override
  public boolean checkSameDescription(@NotNull String description) {
    return true;
  }

  @NotNull
  public static JRoot parse(@NotNull Map map){
    String description = (String)map.get("description");
    boolean status = (boolean)map.get("status");
    JRoot root = new JRoot(description);
    root.setStatus(status);
    List<Map> children = (List)map.get("children");

    for (Map deviceMap : children){
      root.add(JDevice.parse(deviceMap));
    }
    root.isSaved = true;
    return root;
  }
}
