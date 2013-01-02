package  edu.sc.seis.seisFile;

/**
 * Simple class for storing the version derived from the gradle build.gradle file.
 * 
 */ 
public class BuildVersion {

    private static final String version = "1.5.2_localmod";
    private static final String name = "seisFile";
    private static final String group = "edu.sc.seis";
    private static final String date = "Fri Jul 20 17:18:31 EDT 2012";

    /** returns the version of the project from the gradle build.gradle file. */
    public static String getVersion() {
        return version;
    }
    /** returns the name of the project from the gradle build.gradle file. */
    public static String getName() {
        return name;
    }
    /** returns the group of the project from the gradle build.gradle file. */
    public static String getGroup() {
        return group;
    }
    /** returns the date this file was generated, usually the last date that the project was modified. */
    public static String getDate() {
        return date;
    }
    public static String getDetailedVersion() {
        return getGroup()+":"+getName()+":"+getVersion()+" "+getDate();
    }
}
