package gov.nasa.jpl.kservices.util;

import gov.nasa.jpl.ae.event.TimeVaryingMap;
import gov.nasa.jpl.ae.event.TimeVaryingMap.Interpolation;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.Scanner;


public class UrlUtil {

    public static <T> TimeVaryingMap<T> getTimeline( String name, String url,
                                                     String format, // "json" or "csv"
                                                     Class<T> cls,
                                                     Interpolation interpolation ) {
        Path path = null;
        if ( name == null || name.isEmpty() ) {
            path = downloadFromURL(url, format);
        } else {
            path = downloadFromURL( url, name, format );
        }
        if ( path == null || !path.toFile().exists() ) {
            System.err.println( "Failed to get timeline.  File does not exist: " + path );
            return null;
        }
        String pathString = path.toString();
        TimeVaryingMap<T> tvm = null;
        if ( pathString.toLowerCase().endsWith( ".json" ) ) {
            File f = null;
            try {
                f = convertJSONtoCSV( path );
            } catch ( Throwable t ) {
                t.printStackTrace();
                return null;
            }
            tvm = new TimeVaryingMap<T>( name, f.getAbsolutePath(), (T)null,
                                         cls, interpolation );
        } else {
            tvm = new TimeVaryingMap<T>( name, pathString, (T)null,
                                         cls, interpolation );
        }
        return tvm;
    }

    //public static String dataPath = "";
    public static String dataPath = "../baeModels/europa/data/resources/jfiles";
    public static boolean setDataPath(String s) {
        dataPath = s;
        return true;
    }

    /**
     * @param url of json file to be downloaded from Raven
     * @return path of file downloaded
     * https://stackoverflow.com/questions/921262/how-to-download-and-save-a-file-from-internet-using-java
     */
    public static Path downloadFromURL(String url, String format) {
        String name = null;
        // meant to match the file name between the last '/' character and '?' in URL
        Pattern p = Pattern.compile( "(?<=name=).*?(?=\\.|&)" ); // matches what is contained in URL between name= and . (if extension) or &
        Matcher m = p.matcher( url );
        if ( m.find() ) {
            name = m.group( 0 );
        } else {
//            throw new IllegalArgumentException(
//                    "file name not found from URL! " + url );
        }
        return downloadFromURL( url, name, format );
    }

     public static Path downloadFromURL(String url, String name, String format) {
         File dpdp = new File( UrlUtil.dataPath );
         if ( !dpdp.exists() ) {
             dpdp.mkdir();
         }

         boolean isCSV = format != null && format.toLowerCase().trim().equals( "csv" );
         String fileName = name + (isCSV ? ".csv" : ".json");

        Path targetPath = new File(dataPath + File.separator + fileName ).toPath();
        try {
            URL fileSite = new URL(url);
            InputStream in = fileSite.openStream();
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("DOWNLOADED FILE PATH: :" + targetPath.toString());
        } catch (Throwable e) {
            System.err.println( "Failed to access URL: " + url  );
            System.err.println( e.getLocalizedMessage()  );
            //e.printStackTrace();
        }

        return targetPath;
    }

    /**
     * @param url of file to be downloaded
     * @return String of url's contents, UTF-8 encoding
     * https://stackoverflow.com/questions/4328711/read-url-to-string-in-few-lines-of-java-code
     */
    public static String URLToFileString(String url) {

        String urlFileString = "";
        try {
            URL fileSite = new URL(url);
            Scanner scanner = new Scanner(fileSite.openStream(), "UTF-8");
            scanner.useDelimiter("\\A");
            urlFileString = scanner.hasNext() ? scanner.next() : "";
            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return urlFileString;
    }

    /**
     *
     * @param jsonPath path of .json file to be converted (in Raven's simple Data Value, Data Timestamp 2-col format)
     * @return csv File (note gov.nasa.jpl.mbee.util.FileUtils.fromCSVFile takes File)
     */
    public static File convertJSONtoCSV(Path jsonPath) {

        try {

            // meant to match the .extension of file name
            File csvFile = new File(jsonPath.toString().replaceAll("\\.json$", ".csv"));
            System.out.println("FILE TO BE WRITTEN TO:" + csvFile.getPath());

            FileOutputStream fos = new FileOutputStream(csvFile);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            Pattern valP = Pattern.compile( "\"Data Value\"\\s:\\s(.+?)(}|\\s,)" );
            Pattern timeP = Pattern.compile( "\"Data Timestamp\"\\s:\\s\"([^\"]+?)\"(}|\\s,)" );
            Stream<String> jsonStream = Files.lines(jsonPath);

            jsonStream.forEach(line -> {

                String timestamp, value;
                Matcher valM = valP.matcher(line);
                Matcher timeM = timeP.matcher(line);

                if(valM.find() && timeM.find()) {
                    timestamp = timeM.group(1);
                    value = valM.group(1);
                    try {
                        bw.write(String.format("%s,%s", timestamp, value));
                        bw.newLine();
                    } catch(IOException e) { e.printStackTrace(); }
                } else if (line.contains("Data Value") || line.contains("Data Timestamp")){
                    System.out.println(String.format("failed to get timestamp and data value for line:\n %s", line));
                    //Debug.outln(String.format("failed to get timestamp and data value for line:\n %s", line));
                }
            });

            bw.close();
            return csvFile;

        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Cannot convert specified json file to csv! " + jsonPath);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter path to write files:");
        String p = scanner.nextLine();
        if (p == null || p.isEmpty() ) {
            p = ".";
        }
        setDataPath( p );

        System.out.println("Enter URL for timeline data:");
        String url = scanner.nextLine();

        // read data into json file and convert to csv file
        Path thepath = downloadFromURL(url.trim(), "json");
        convertJSONtoCSV(thepath);

        System.out.println(URLToFileString(url));
   }

}
