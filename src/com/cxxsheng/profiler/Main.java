package com.cxxsheng.profiler;

import com.cxxsheng.profiler.settings.ProfilerSettings;
import com.cxxsheng.profiler.ui.MainWindow;
import com.cxxsheng.profiler.utils.NLS;
import javax.swing.*;

public class Main {

  public static void main(String[] args) {
    if (!ProfilerSettings.init())
      return;
    NLS.setLocale(NLS.defaultLocale());
    SwingUtilities.invokeLater(new MainWindow()::init);
  }
}
