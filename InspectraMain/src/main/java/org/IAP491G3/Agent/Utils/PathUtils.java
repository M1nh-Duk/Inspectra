package org.IAP491G3.Agent.Utils;

import java.io.*;

import static org.IAP491G3.Agent.Loader.Contraints.DUMP_DIR;


public class PathUtils {
    public static String getCurrentJarPath() throws Exception {
        return new File(PathUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
    }
    public static String getCurrentDirectory(){
        try{
            return new File(getCurrentJarPath()).getParent();
        }catch (Exception e){
            return new File(".").getAbsolutePath();
        }
    }


    public static void addTextToFile(File f, String content, boolean append){
        try{
            if(!(new File(f.getParent()).exists())){
                new File(f.getParent()).mkdirs();
            }
            if(!f.exists()){
                f.createNewFile();
            }
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f.getAbsolutePath(), append)));
            out.println(content);
            out.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void appendTextToFile(File f, String content){
        addTextToFile(f, content, true);
    }


    public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file, append);
    }

    public static void createDumpFolder() {
        File dumpDir = new File(PathUtils.getCurrentDirectory(), "dump");
        if (!dumpDir.exists()) {
            boolean created = dumpDir.mkdirs();
            if (created) {
                StringUtils.println("Dump folder created at: " + dumpDir.getAbsolutePath());
            } else {
                StringUtils.printAndLogErr(new IllegalAccessException("Failed to create dump folder at: " + dumpDir.getAbsolutePath()));
            }
        }
        DUMP_DIR = dumpDir.getAbsolutePath();
    }
}