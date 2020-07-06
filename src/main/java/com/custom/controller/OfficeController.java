package com.custom.controller;

import application.server.utils.SystemUtils;
import com.custom.component.filemanager.utils.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author shaofan
 */
@Controller
@RequestMapping("/api/office")
public class OfficeController {


//    @Value("${fileServer.domain}")
//    String domain = "http://127.0.0.1:9001";
    String domain = "http://" + SystemUtils.LOCAL_IP + ":" + "9001";

//    @Value("${files.docservice.url.api}")
    String doc_api = domain + "/web-apps/apps/api/documents/api.js";

    @RequestMapping
    public String office(ModelMap map, String url, String filename) throws UnknownHostException {
        String userAddress = InetAddress.getLocalHost().getHostAddress();
        map.put("key", GenerateRevisionId(userAddress + "/" + filename));

        map.put("url", domain + url);

        map.put("filename", filename);

        map.put("fileType", FileUtils.getExtension(filename).replace(".", ""));

        map.put("doc_api", doc_api);

        map.put("documentType", FileUtils.GetFileType(filename).toString().toLowerCase());

        return "office";
    }

    private static String GenerateRevisionId(String expectedKey) {
        if (expectedKey.length() > 20) {
            expectedKey = Integer.toString(expectedKey.hashCode());
        }

        String key = expectedKey.replace("[^0-9-.a-zA-Z_=]", "_");

        return key.substring(0, Math.min(key.length(), 20));
    }
}
