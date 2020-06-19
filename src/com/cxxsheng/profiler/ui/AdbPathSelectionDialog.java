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


  public static final int MODIFIED_SUCCESSFULLY = 0;
  public static final int USER_CANCELED = -1;
  public static final int RETRY_NEEDED = 1;

  static public int showAdbPathSelectionDialog(Component parent){
    JPanel panel = new JPanel();


    JIconTextField textField = new JIconTextField(UiUtils.openIconTest("open"),20) {
      @Override
      public void handleClick(JIconTextField jSearchTextField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setToolTipText(NLS.str("dialog.title.adbSelect"));
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
          Path path = fileChooser.getSelectedFile().toPath();
          this.setText(path.toString());
        }
      }
    };
    panel.add(textField);
    textField.setText(AdbService.adbPath);

    int result = JOptionPane.showConfirmDialog(parent,panel,NLS.str("dialog.title.adbSelect"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result == 0){
        if (textField.getText()!=null)
          if (textField.getText().length() != 0)
             if(update(textField.getText()))
               return MODIFIED_SUCCESSFULLY;
    }else if (result == 2)
      return USER_CANCELED;
    return RETRY_NEEDED;
  }

  private static boolean update(@NotNull String path){
    path = path.trim();
    if (path.equals(ProfilerSettings.ADB_PATH))
      return false;
    AdbService.adbPath = path;
    ProfilerSettings.ADB_PATH = path;
    return true;
  }
}
