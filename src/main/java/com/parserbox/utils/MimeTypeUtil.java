package com.parserbox.utils;


import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil2;

public class MimeTypeUtil {

    //as of Sep 2009 mime-util (ver. 2.1.2) detects office 2007 fiels as ZIP when MagicMimeMimeDetector is used
    //but ExtensionMimeDetector detects them correctly
    //so we will use 2 detectors with extension detector as a preferred detector
    private static MimeUtil2 extensionDetector = new MimeUtil2();
    private static MimeUtil2 contentDetector = new MimeUtil2();

    public static String DEFAULT_BINARY_CONTENT_TYPE = "application/octet-stream";

    static {
        extensionDetector.registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
        contentDetector.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
    }

    /**
     * Detects file's conent type by filename (extension) or by file content, if the content type cannot be detected by extension
     * If content type cannot be resovlved at all, the "application/octet-stream" is returned
     * Never returns null, even for null or empty files
     * @param filename the file name
     * @param content the file content
     * @return file content type
     */
    public static String detectContentType(String filename, byte[] content) {
        MimeType type = MimeUtil2.getMostSpecificMimeType(extensionDetector.getMimeTypes(filename));
        if (type == MimeUtil2.UNKNOWN_MIME_TYPE) {
            type = MimeUtil2.getMostSpecificMimeType(contentDetector.getMimeTypes(content));
        }
        if (type == MimeUtil2.UNKNOWN_MIME_TYPE) {
            return DEFAULT_BINARY_CONTENT_TYPE;
        }
        return type.getMediaType() + ((type.getSubType() != null) ? ("/" + type.getSubType()) : "");
    }
}
