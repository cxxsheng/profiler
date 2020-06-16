package com.cxxsheng.profiler.ui;

import com.cxxsheng.profiler.utils.UiUtils;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;

public abstract class JSearchTextField extends JTextField  {
  private ImageIcon icon;
  private ImageButton closeButton;
  private JSearchTextField instance = this;
  public JSearchTextField(){
    icon = UiUtils.openIconTest("close");

    closeButton = new ImageButton(icon){
      @Override
      public void paint(Graphics g) {
        super.paint(g);
      }

      //容器首选大小
      @Override
      public Dimension getPreferredSize() {
        Dimension dimension = JSearchTextField.super.getPreferredSize();
        dimension.height -= 10;
        dimension.width = dimension.height;
        return dimension;
      }
    };

    this.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    closeButton.addActionListener(e -> JSearchTextField.this.handleClick(instance));

  }

  @Override
  public void addNotify() {
    super.addNotify();
    add(closeButton);//Container中方法将指定组件追加到此容器的尾部
  }


  public abstract void handleClick(JSearchTextField jSearchTextField);

}
