package com.cxxsheng.profiler.settings;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ProfilerSettings {
    private static final Logger LOG = LoggerFactory.getLogger(ProfilerSettings.class);


    private static String BASE_DIR;
    private static String OS;

    private static final String VERSION = "0.0.1";
    private static final String DIR_NAME = "Pr0fi13r";
    private static final String CONFIG_NAME = "config.json";
    private static File Config;

    public static String ADB_PATH = "";
    public static int WINDOW_HEIGHT = 250;
    public static int WINDOW_WIDTH = 400;

    public static boolean init(){
        OS = (System.getProperty("os.name")).toUpperCase();
        if (OS.contains("WIN"))
          BASE_DIR = System.getenv("AppData");
        else
          BASE_DIR = System.getProperty("user.home");

        if (BASE_DIR == null)
        {
          LOG.error("BASE_DIR is null");
          return false;
        }

        if (OS.contains("WIN"))
          BASE_DIR += ("/"+DIR_NAME);
        else
        {
          BASE_DIR +=  "/Library/Application Support";
          BASE_DIR += ("/"+DIR_NAME);
        }

        File base_dir = new File(BASE_DIR);
        if (!base_dir.exists())
          if(!base_dir.mkdirs()){
            LOG.error("cannot create directory: "+ BASE_DIR);
            return false;
          }

         Config = new File(BASE_DIR + "/" + CONFIG_NAME);
         if (Config.exists())
           return parse_config_file();
         LOG.info("first start up!");
         return true;
    }

    private static boolean parse_config_file(){
      try {
        byte[] bs = Files.readAllBytes(Config.toPath());
        Map configMap = (Map) JSONObject.parse(bs);
        ADB_PATH = (String)configMap.get("ADB_PATH");
        WINDOW_WIDTH =  (int)(configMap.get("WINDOW_WIDTH"));
        WINDOW_HEIGHT = (int)configMap.get("WINDOW_HEIGHT");
      }

      catch (IOException e) {
        LOG.error(e.getMessage());
        return false;
      }
      return true;
    }


    public static boolean save_config_file(){
      try {
        if (!Config.exists())
          if(!Config.createNewFile()) {
            LOG.error("createNewFile failed!");
            return false;
          }

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("ADB_PATH",ADB_PATH);
        configMap.put("WINDOW_WIDTH", WINDOW_WIDTH);
        configMap.put("WINDOW_HEIGHT", WINDOW_HEIGHT);
        byte[] bs =  JSONObject.toJSONBytes(configMap);
        FileOutputStream fos = new FileOutputStream(Config);
        fos.write(bs);
        fos.close();
        return true;

      }
      catch (IOException e) {
        LOG.error(e.getMessage());
        return false;
      }
    }


  public static String getVersion() {
      return VERSION;
  }
}
