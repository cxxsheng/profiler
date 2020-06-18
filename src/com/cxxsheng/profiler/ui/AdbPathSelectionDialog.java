package com.cxxsheng.profiler.ui;

import com.cxxsheng.profiler.core.AdbService;
import com.cxxsheng.profiler.settings.ProfilerSettings;
import com.cxxsheng.profiler.utils.NLS;
import com.cxxsheng.profiler.utils.UiUtils;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;


public class AdbPathSelectionDialog extends JDialog {

  static public void showAdbPathSelectionDialog(Component parent){
    JPanel panel = new JPanel();


    JIconTextField textField = new JIconTextField(UiUtils.openIconTest("open"),20) {
      @Override
      public void handleClick(JIconTextField jSearchTextField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setToolTipText(NLS.str("dialog.title.adbSelect"));
        int ret = fileChooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
          Path path = fileChooser.getSelectedFile().toPath();
          this.setText(path.toString());
        }
      }
    };
    panel.add(textField);
    textField.setText(AdbService.adbPath);
    //fixme
    int result = JOptionPane.showConfirmDialog(parent,panel,NLS.str("dialog.title.adbSelect"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result == 0){
        if (textField.getText()!=null)
          if (textField.getText().length() != 0)
            update(textField.getText());
    }
    System.out.println(result);
  }

  private static void update(@NotNull String path){
    AdbService.adbPath = path;
    ProfilerSettings.ADB_PATH = path;
  }
}
