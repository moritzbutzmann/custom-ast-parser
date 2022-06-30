package main.helper;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ClassPathHelper {

    /**
     * Returns a list of all files directly included in the specified Folder. Subfolders are ignored.
     *
     * @param directoryPath - Path of directory to list files
     * @return List of filepaths for files in directory
     */
    public static String[] getRequiredClasspaths(final String directoryPath) {
        // Creating a File object for directory
        File directory = new File(directoryPath);
        List<String> classpaths = new LinkedList<>();
        // List of all files and directories
        File filesList[] = directory.listFiles();
        if (null != filesList) {
            for (File file : filesList) {
                if (file.isFile()) {
                    classpaths.add(directoryPath + "\\" + file.getName());
                }
            }
        }
        return classpaths.toArray(new String[classpaths.size()]);

    }
}
