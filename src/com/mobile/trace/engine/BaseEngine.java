package com.mobile.trace.engine;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BaseEngine {
    protected final static String getJSonContextData(InputStream in) {
        if (in == null) return null;
        
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setCoalescing(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(in);
            in.close();
            doc.getDocumentElement().normalize();
            NodeList nl = doc.getElementsByTagName("string");
            Node node = nl.item(0);
            String data = node.getFirstChild().getNodeValue();
            
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
