package com.cxxsheng.profiler.ui;

import com.cxxsheng.profiler.settings.ProfilerSettings;
import com.cxxsheng.profiler.utils.LangLocale;
import com.cxxsheng.profiler.utils.NLS;
import com.cxxsheng.profiler.utils.UiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class ProfilerSettingsWindow extends JDialog {


    private static final Logger LOG = LoggerFactory.getLogger(ProfilerSettingsWindow.class);

//    private final transient MainWindow mainWindow;
//    private final transient ProfilerSettings settings;
//    private final transient String startSettings;
//    private final transient LangLocale prevLang;


    public ProfilerSettingsWindow (MainWindow mainWindow, ProfilerSettings settings) {
//        this.mainWindow = mainWindow;
//        this.settings = settings;
//        this.startSettings = JadxSettingsAdapter.makeString(settings);
//        this.prevLang = settings.getLangLocale();
//
//        initUI();
//
        setTitle(NLS.str("preferences.title"));
        setSize(400, 550);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        pack();
//        UiUtils.setWindowIcons(this);
        setLocationRelativeTo(null);
    }
}
