package com.parserbox.controller;

import com.parserbox.model.ApplicationProperties;
import com.parserbox.model.FileCacheItem;
import com.parserbox.sevice.UploadServiceImpl;
import com.parserbox.utils.DownloadUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.MultipartProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

@Controller
public class FileUploadController {

    private Log log = LogFactory.getLog(this.getClass());

    @Autowired
    UploadServiceImpl uploadService;

    @Autowired
    MultipartProperties multipartProperties;


    @Autowired
    ApplicationProperties applicationProperties;



    @RequestMapping("/uploadFileRequest")
    public String uploadFileRequest(@RequestParam(value = "fileKey", required = true) String fileKey,
                                    Model model, HttpServletRequest request) {
        model.addAttribute("fileKey", fileKey);
        request.getSession().setAttribute("fileKey", fileKey);

        FileCacheItem c = new FileCacheItem(fileKey, FileCacheItem.status.PENDING);
        uploadService.setFileCacheItem(c);

        return "redirect:/uploadFile.html";
    }


    @RequestMapping("/downloadFileRequest")
    public void downloadFileRequest(@RequestParam(value = "fileKey", required = true) String fileKey,
                                      Model model, HttpServletRequest request, HttpServletResponse response) {
        try {
            String fullPath = applicationProperties.getFileCacheLocation() + File.separatorChar + fileKey;
            File f = new File(fullPath);
            new DownloadUtil().writeFileToHTTPOutputStream(response, f, fileKey);
        } catch (Exception e) {
            log.info(e);
            try {
                OutputStream os = response.getOutputStream();
                StringBuffer b = new StringBuffer();
                b.append("<html>");
                b.append("The file could not be downloaded for the following reason:<br>");
                b.append("<font color='green'>" + e.getMessage() + "</font>");
                b.append("<br>");
                b.append("<br>");
                b.append("Contact support if you continue to see this error.<br>");
                b.append("<button onclick='window.close()'>Close Window</buffon>");
                b.append("</html>");

                os.write(b.toString().getBytes());
                os.flush();
                os.close();
            } catch (Exception ee) {
                log.info(ee);
            }
        }
    }



    @RequestMapping("/uploading")
    public String uploading(Model model) {
        File file = new File(uploadService.getUploadDirectory());
        model.addAttribute("files", file.listFiles());
        return "uploading";
    }

    @RequestMapping(value = "/uploadingPost", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject uploadingPost(@RequestParam("uploadingFiles") MultipartFile uploadedFile,
                                    HttpServletRequest request) throws IOException {

        String fileKey = (String)request.getSession().getAttribute("fileKey");
        File file = null;

         String fileName = uploadedFile.getOriginalFilename();
        file = new File(uploadService.getFileName(fileKey, fileName));
        uploadedFile.transferTo(file);
        uploadService.clearForFileKey(fileKey, file);

        FileCacheItem item = uploadService.getFileCacheItem(fileKey);
        if (item != null) {
            item.setFileStatus(FileCacheItem.status.IMPORTED);
        }
        JSONObject json = new JSONObject();
        json.put("success", true);
        return json;
    }

    @RequestMapping(value = "/getMaxFileSize", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject getMaxFileSize(HttpServletRequest request) throws IOException {
        JSONObject json = new JSONObject();
        String mb = multipartProperties.getMaxFileSize().replaceAll("\\D+","");
        json.put("success", true);
        json.put("maxFileSize", mb);
        return json;
    }
}