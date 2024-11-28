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


    /**
     * Opens a {@link FileOutputStream} for the specified file, checking and
     * creating the parent directory if it does not exist.
     * <p>
     * At the end of the method either the stream will be successfully opened,
     * or an exception will have been thrown.
     * <p>
     * The parent directory will be created if it does not exist.
     * The file will be created if it does not exist.
     * An exception is thrown if the file object exists but is a directory.
     * An exception is thrown if the file exists but cannot be written to.
     * An exception is thrown if the parent directory cannot be created.
     *
     * @param file  the file to open for output, must not be {@code null}
     * @param append if {@code true}, then bytes will be added to the
     * end of the file rather than overwriting
     * @return a new {@link FileOutputStream} for the specified file
     * @throws IOException if the file object is a directory
     * @throws IOException if the file cannot be written to
     * @throws IOException if a parent directory needs creating but that fails
     * @since 2.1
     */
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