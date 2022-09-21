import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        Logger logger = Logger.getAnonymousLogger();
        Properties prop = new Properties();

        try (InputStream input = Files.newInputStream(Paths.get(args[0]))) {
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        String mode = prop.getProperty("mode");
        if (!mode.equalsIgnoreCase("COPY") && !mode.equalsIgnoreCase("MOVE")) {
            logger.severe("Mode is not recognized: " + mode);
            return;
        }

        if (Objects.isNull(prop.getProperty("suffix"))) {
            logger.severe("No suffix is configured");
            return;
        }


        if (Objects.isNull(prop.getProperty("files")) || prop.getProperty("files").isBlank()) {
            logger.warning("No files are configured to be copied/moved");
            return;
        }


        String files = prop.getProperty("files");

        String[] split = files.split(":");

        for (String s : split) {
            try {
                FileInputStream fileInputStream = new FileInputStream(s);
                File file = new File(s);

                if (prop.getProperty("mode").equalsIgnoreCase("MOVE")) {
                    fileInputStream.close();
                    String suffix = suffix(s, prop.getProperty("suffix"));
                    logger.info(changeDotToSlash(s, false) + " => " + suffix);
                    file.delete();
                    File file1 = new File(suffix);
                    file1.createNewFile();
                }

                if (prop.getProperty("mode").equalsIgnoreCase("COPY")) {
                    fileInputStream.close();
                    String suffix = suffix(s, prop.getProperty("suffix"));
                    logger.info(changeDotToSlash(s, false) + " -> " + suffix);
                    File file1 = new File(suffix);
                    file1.createNewFile();
                    FileWriter fileWriter = new FileWriter(file1);
                    FileReader fileReader = new FileReader(file);
                    String readFile = readFile(fileReader);
                    fileWriter.write(readFile);


                    fileReader.close();
                    fileWriter.close();
                }


            } catch (FileNotFoundException e) {
                logger.severe("No such file: " + s);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }

    }

    private static String readFile(FileReader fileReader) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            int read = fileReader.read();


            while (read != -1) {
                stringBuilder.append((char)read);
                read = fileReader.read();
            }
        } catch (IOException e) {
        }

        return stringBuilder.toString();
    }

    static String changeDotToSlash(String withDot, boolean b) {
//        if (b) {
//            String substring = withDot.substring(0, withDot.lastIndexOf('.'));
//            String substring2 = substring.substring(0, substring.lastIndexOf('.'));
//            substring2 = substring.replace('.', '/');
//            return substring2 + withDot.substring(withDot.lastIndexOf('.'));
//        } else {
        String substring = withDot.substring(0, withDot.lastIndexOf('.'));
        substring = substring.replace('.', '/');
        return substring + withDot.substring(withDot.lastIndexOf('.'));
//        }
    }

    static String suffix(String path, String suffix) {
        StringBuilder stringBuilder = new StringBuilder(path.substring(0, path.lastIndexOf('.')));
        stringBuilder.append(suffix);
        stringBuilder.append(path.substring(path.lastIndexOf('.')));
        return stringBuilder.toString();

    }
}
