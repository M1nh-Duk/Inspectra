package org.IAP491G3.Agent.Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.IAP491G3.Agent.AgentCore.Result;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;



public class LogUtils {
    public static File log_file = null;
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static File getLogFile() {
        File logDir = new File(PathUtils.getCurrentDirectory(), "logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        return new File(logDir, "inspectra.log");
    }

    public static void logit(String content) {
        if (log_file == null) {
            log_file = getLogFile();
        }
        String eventTime = getEventTime();
        PathUtils.appendTextToFile(log_file, eventTime + "  " + content);
    }

    public static String getEventTime() {
        Timestamp createTime = new Timestamp(System.currentTimeMillis());
        return  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(createTime);
    }
    public static void logToJSON(Result result) {
        // Define path to the logs directory
        String logsDirPath = "alert"; // Relative to the current working directory
        File alertDir = new File(PathUtils.getCurrentDirectory(), "alert");

        // Create the logs directory if it does not exist
        if (!alertDir.exists()) {
            alertDir.mkdir(); // Create the directory
        }

        // Create the path to the JSON file
        File jsonFile = new File(alertDir, "alert.json");

        // Append mode for logging
        try (Writer writer = new FileWriter(jsonFile, true)) { // Append mode
            String jsonString = gson.toJson(result); // Serialize the Result object
            writer.write(jsonString + "\n");
        } catch (IOException e) {
            StringUtils.printAndLogErr(e);
        }
    }

}