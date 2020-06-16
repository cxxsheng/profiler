package com.cxxsheng.profiler.ui.treemodel.profiler;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public  abstract class JNode extends DefaultMutableTreeNode {
  static protected final int JNODE_UNKOWN_TYPE = 0;
  static protected final int JNODE_JDEVICE_TYPE = 1;
  static protected final int JNODE_JPROCESS_TYPE = 2;
  static protected final int JNODE_JTRACE_TYPE = 3;
  static protected final int JNODE_JTHREAD_TYPE = 4;
  static protected final int JNODE_JMETHOD_TYPE = 5;
  static protected final int JNODE_JROOT_TYPE = 0x10;


  transient protected boolean status = false;
  transient private JNode mParent;
  private static final long serialVersionUID = -6426997873508032309L;
  transient protected String description;

  transient private boolean isSearched = false;
  transient private boolean isSearchedFather = false;
  private static Logger LOG = LoggerFactory.getLogger(JNode.class);


  public Vector<TreeNode> getChildren(){
    return children;
  }

  public void setSearchedFather(boolean searchedFather) {
    isSearchedFather = searchedFather;
  }

  public boolean isSearchedFather(){
    return isSearchedFather;
  }

  public JNode getChildByDescription(@NotNull String description){
    if (children == null)
      return null;
    for (TreeNode node : getChildren()){
      if (node instanceof JNode)
        if (((JNode)node).checkSameDescription(description))
          return (JNode)node;
    }
    return null;
  }

  @Override
  public void add(@NotNull MutableTreeNode newChild) {
    super.add(newChild);
    if (newChild instanceof JNode)
      ((JNode)newChild).mParent = this;
    else
      LOG.warn("unkown child " + newChild.toString());
  }

  public JNode getParent(){
    return mParent;
  }


  public boolean hasParent(){
    if (getParent() != null)
      return true;
    return false;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return getDescription();
  }


  public synchronized void setStatus(@NotNull boolean status){
    this.status  = status;
  }

  public boolean isSearched() {
    return isSearched;
  }

  public void setSearchedFlag(boolean flag){
    isSearched = flag;
  }

  public boolean getStatus(){
    return status;
  }
  public abstract Icon getIcon();

  public boolean hasChildren(){

    return (children != null) && !children.isEmpty();
  }



  public Map getMap(){

    Map map = new HashMap();
    map.put("description", description);
    map.put("status",status);
    //if (this instanceof JDevice)
    //  map.put("type", JNODE_JDEVICE_TYPE);
    //else if (this instanceof JProcess)
    //  map.put("type", JNODE_JPROCESS_TYPE);
    //else if (this instanceof JTrace)
    //  map.put("type", JNODE_JTRACE_TYPE);
    //else if (this instanceof JTrace.JThread)
    //  map.put("type", JNODE_JTHREAD_TYPE);
    //else if (this instanceof JTrace.JMethod)
    //  map.put("type", JNODE_JMETHOD_TYPE);
    //else if (this instanceof JRoot)
    //  map.put("type", JNODE_JROOT_TYPE);
    //else
    //  map.put("type", JNODE_UNKOWN_TYPE);

    List list =  new ArrayList();
    if (hasChildren())
    {
      for (TreeNode  node : getChildren()){
        if (node instanceof JNode){
          list.add(((JNode)node).getMap());
        }
      }
    }
    map.put("children", list);
    return map;
  }

  public boolean checkSameDescription(@NotNull String description) {
    return this.description.equals(description);
  }







}
