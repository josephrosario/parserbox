package com.parserbox.controller;

import com.parserbox.model.*;
import com.parserbox.utils.FormulaProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

/**
 * Servlet implementation class JSONServlet
 */
@RestController
@RequestMapping("/serverstatus")
public class ServerStatusController {

    static ServerStatus serverStatus = new ServerStatus();
    private Log log = LogFactory.getLog(this.getClass());
    @PostConstruct
    public void init() {
        try {
            FormulaProcessor.loadSupportedFunctions();
            log.info("Formulas cached loaded.");
        }
        catch (Exception e) {
            log.info("Could not load formulas cache", e);
        }
        serverStatus.setStatus(true);

    }

    @RequestMapping(value = "/getServerStatus", method = RequestMethod.POST)
    public @ResponseBody
    ParserServerInfo getServerStatus(@RequestBody ParserServerInfo peachServerInfo) throws Exception {
        try {
            if (serverStatus.isStatus()) {
                peachServerInfo.setStatus("Ready");
            }
        } catch (Exception e) {
            peachServerInfo.setError(e.getMessage());
        }
        return peachServerInfo;
    }

    @RequestMapping(value = "/getDataCache", method = RequestMethod.POST)
    public @ResponseBody
    ResponseContainer getDataCache(@RequestBody RequestContainer requestContainer, HttpServletRequest request) throws Exception {

        ResponseContainer responseContainer = new ResponseContainer();
        try {
            responseContainer.setDataCache(FormulaProcessor.getSupportFunctions());
        } catch (Exception e) {
            responseContainer.setError(e.getMessage());
        }
        return responseContainer;
    }

}
