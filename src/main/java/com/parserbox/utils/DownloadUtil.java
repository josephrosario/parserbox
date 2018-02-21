package com.parserbox.utils;


        import org.apache.commons.lang3.StringUtils;

        import java.io.File;
        import java.io.FileInputStream;
        import java.io.OutputStream;
        import java.net.URLEncoder;

        import javax.servlet.http.HttpServletResponse;


public class DownloadUtil {

    public DownloadUtil() {
    }

    public String getContentType(String filename) throws Exception {
        String ctype = "";
        filename = StringUtils.trimToEmpty(filename).toLowerCase();
        if (filename.endsWith("txt")) {
            ctype = "text/plain";
        } else if (filename.endsWith("xml")) {
            ctype = "text/plain";
        } else if (filename.endsWith("htm")|| filename.endsWith("html")) {
            ctype = "text/html";
        } else if (filename.endsWith("doc") || filename.endsWith("docx")) {
            ctype = "application/msword";
        } else if (filename.endsWith("xls") || filename.endsWith("xlsx")) {
            ctype = "application/vnd.ms-excel";
        } else if (filename.endsWith("pdf")) {
            ctype = "application/pdf";
        } else {
            ctype = "application/octet-stream";
        }
        return ctype;
    }

    public void writeFileToHTTPOutputStream(HttpServletResponse response, File file, String filename) throws Exception {
        writeFileToHTTPOutputStream(response, file, filename, null);
    }
    public void writeFileToHTTPOutputStream(HttpServletResponse response, File file, String filename, String disposition) throws Exception {
        if (StringUtils.isBlank(disposition)) disposition = "attachment";
        response.setHeader("Content-Disposition", disposition + "; filename=" + URLEncoder.encode(filename,"UTF-8").replace("+", "%20"));
        response.setContentType(getContentType(filename));
        FileInputStream fs = new FileInputStream(file);
        OutputStream os = response.getOutputStream();
        int k;
        while ((k = fs.read()) != -1) {
            os.write(k);
        }
        fs.close();
        os.flush();
        os.close();
    }

    public void writeFileToHTTPOutputStream(HttpServletResponse response, byte[] file, String filename, String disposition) throws Exception {
        if (StringUtils.isBlank(disposition)) disposition = "attachment";
        response.setHeader("Content-Disposition", disposition + "; filename=" + URLEncoder.encode(filename,"UTF-8").replace("+", "%20"));
        response.setContentType(getContentType(filename));
        OutputStream os = response.getOutputStream();
        os.write(file);
        os.flush();
        os.close();
    }
}
