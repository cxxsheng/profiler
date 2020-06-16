package com.cxxsheng.profiler;

import com.cxxsheng.profiler.ui.MainWindow;
import com.cxxsheng.profiler.utils.NLS;
import javax.swing.*;

public class Main {

  public static void main(String[] args) {
    NLS.setLocale(NLS.defaultLocale());
    SwingUtilities.invokeLater(new MainWindow()::init);
  }
}
