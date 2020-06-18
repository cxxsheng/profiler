package com.cxxsheng.profiler.ui;

import com.cxxsheng.profiler.utils.UiUtils;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import java.awt.*;

public abstract class JIconTextField extends JTextField {

  private ImageIcon icon;
  private ImageButton closeButton;
  private JIconTextField instance = this;

  public JIconTextField(@NotNull ImageIcon icon, int columns){
    super(columns);
    this.icon = icon;
    initUI();
  }

  public JIconTextField(@NotNull ImageIcon icon){
    this(icon, 0);
  }

  @Override
  public void addNotify() {
    super.addNotify();
    add(closeButton);//Container中方法将指定组件追加到此容器的尾部
  }


  private void initUI(){
    closeButton = new ImageButton(icon){
      @Override
      public void paint(Graphics g) {
        super.paint(g);
      }

      //容器首选大小
      @Override
      public Dimension getPreferredSize() {
        Dimension dimension = JIconTextField.super.getPreferredSize();
        dimension.height -= 10;
        dimension.width = dimension.height;
        return dimension;
      }
    };

    this.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    closeButton.addActionListener(e -> JIconTextField.this.handleClick(instance));

  }
  public abstract void handleClick(JIconTextField jSearchTextField);

}
