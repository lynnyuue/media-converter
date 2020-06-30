package lynn.media.converter;

import org.apache.commons.cli.*;
import ws.schild.jave.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * media converter main class
 *
 * @author Lynn
 */
public class Main {

    private static final String FORMAT_MP3 = "mp3";
    private static final char SUFFIX_DELIMITER = '.';
    private static final String JAR_SUFFIX = ".jar";

    public static void main(String[] args) throws URISyntaxException, IOException {
        Path basePath = getBasePath();

        CommandLineParser parser = new DefaultParser();
        Options options = getOptions();

        CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Invalid Argument: " + e.getMessage());
            System.exit(-1);
            return;
        }

        String source = commandLine.getOptionValue("source");
        String output = commandLine.getOptionValue("output");

        if (source == null || output == null) {
            System.err.println("source or output should not be null");
            System.exit(-1);
            return;
        }

        String format = commandLine.getOptionValue("format", "");
        if (format.isEmpty()) {
            format = getExt(output);
        }

        if (format.isEmpty()) {
            System.err.println("Invalid output file name");
            System.exit(-1);
            return;
        }

        File sourceFile = basePath.resolve(source).toFile();
        File targetFile = basePath.resolve(output).toFile();

        EncodingAttributes attributes = getAttributes(format);
        EncoderProgressListener listener = new ConvertProgressListener();

        if (sourceFile.isFile()) {
            encode(sourceFile, targetFile, attributes, listener);
        } else if (sourceFile.isDirectory()) {
            Files.createDirectories(targetFile.toPath());
            Files.list(sourceFile.toPath()).forEachOrdered(input -> {
                if(Files.isRegularFile(input)){
                    encode(input.toFile(), targetFile, attributes, listener);
                }
            });
        } else {
            System.err.println("Invalid source file: " + sourceFile);
        }
    }

    private static void encode(File sourceFile, File outputFile, EncodingAttributes attributes,
                               EncoderProgressListener listener) {
        if(outputFile.isDirectory()){
            String format = attributes.getFormat();
            String outputName = getNameWithoutExt(sourceFile.getName()) + format;
            outputFile = outputFile.toPath().resolve(outputName).toFile();
        }

        Encoder encoder = new Encoder();
        try {
            encoder.encode(new MultimediaObject(sourceFile), outputFile, attributes, listener);
            System.out.println("Convert success");
        } catch (EncoderException e) {
            System.err.println("Convert failure: " + e.getMessage());
        }
    }

    private static Path getBasePath() throws URISyntaxException {
        URL url = Main.class.getProtectionDomain().getCodeSource().getLocation();
        Path path = Paths.get(url.toURI());

        if (path.toString().endsWith(JAR_SUFFIX)) {
            return path.getParent();
        }
        return path;
    }

    private static EncodingAttributes getAttributes(String format) {
        if (FORMAT_MP3.equals(format)) {
            return createMP3Attributes();
        }
        System.err.println("Unsupported format: " + format);
        System.exit(-1);
        return null;
    }

    private static EncodingAttributes createMP3Attributes() {
        //Audio Attributes
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("libmp3lame");
        audio.setBitRate(128000);
        audio.setChannels(2);
        audio.setSamplingRate(44100);

        //Encoding attributes
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("mp3");
        attrs.setAudioAttributes(audio);

        return attrs;
    }

    private static String getExt(String file) {
        int index = file.indexOf(SUFFIX_DELIMITER);
        if (index != -1) {
            return file.substring(index + 1);
        }
        return "";
    }

    private static String getNameWithoutExt(String file) {
        String ext = getExt(file);
        if (ext.length() > 0) {
            return file.substring(0, file.length() - ext.length());
        }
        return file;
    }

    private static Options getOptions() {
        Options options = new Options();
        options.addOption("source", true, "source file");
        options.addOption("output", true, "target convert file");
        options.addOption("format", true, "format for output");

        return options;
    }
}
