package com.cxxsheng.profiler.ui;

import javax.swing.*;
import java.awt.*;

public class ImageButton extends JButton {

  public ImageButton(ImageIcon icon){
    setSize(icon.getImage().getWidth(null),
            icon.getImage().getHeight(null));
    setIcon(icon);
    setMargin(new Insets(0, 0, 0, 0));
    setIconTextGap(0);
    setBorderPainted(false);
    setBorder(null);
    setText(null);
    setFocusPainted(false);
    setContentAreaFilled(false);
  }
}