package com.delcyon.capo.xml.cdom;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/** JSON → XML converter that is 100 % Java 8 compatible (no switch‑expressions). */
public final class CDocumentJSON
{

    /**
     * Convert the JSON in {@code in} to a W3C DOM.
     *
     * @param in
     *            JSON source
     * @param rootName
     *            name for the root element
     * @return org.w3c.dom.Document representing the XML tree
     */
    public static CDocument convert(InputStream in, String rootName) throws IOException
    {

        JsonReader reader = new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        reader.setLenient(true); // tolerate /* comments */ etc.

        try
        {

            CDocument doc = new CDocument();

            Element root = doc.createElement(rootName);
            doc.appendChild(root);

            reader.beginObject(); // expect top‑level object
            readObject(reader, root, doc);
            reader.endObject();
            reader.close();

            return doc;
        }
        catch (Exception ex)
        { // ParserConfigurationException | IllegalStateException
            throw new IOException("XML building failed", ex);
        }
    }

    /* ------------------------------------------------------------------ */
    /* Helpers */
    /* ------------------------------------------------------------------ */

    private static void readObject(JsonReader r, Element parent, Document doc) throws IOException
    {

        while (r.hasNext())
        {
            String name = r.nextName();
            name = toXmlName(name);
            JsonToken token = r.peek();

            switch (token)
            {
                case STRING:
                    parent.setAttribute(name, r.nextString());
                    break;

                case NUMBER:
                    parent.setAttribute(name, r.nextString()); // preserves exact lexeme
                    break;

                case BOOLEAN:
                    parent.setAttribute(name, Boolean.toString(r.nextBoolean()));
                    break;

                case NULL:
                    r.nextNull();
                    Element nil = doc.createElement(name);
                    addNil(nil, doc);
                    parent.appendChild(nil);
                    break;

                case BEGIN_OBJECT:
                    r.beginObject();
                    Element obj = doc.createElement(name);
                    readObject(r, obj, doc);
                    r.endObject();
                    parent.appendChild(obj);
                    break;

                case BEGIN_ARRAY:
                    r.beginArray();
                    if (!r.hasNext())
                    { // empty array -> empty string attr
                        parent.setAttribute(name, "");
                        r.endArray();
                        break;
                    }

                    JsonToken first = r.peek();
                    if (first == JsonToken.BEGIN_OBJECT)
                    {
                        /* -------- array of objects -> one <name .../> each -------- */
                        while (r.hasNext())
                        {
                            r.beginObject();
                            Element item = doc.createElement(name);
                            readObject(r, item, doc);
                            r.endObject();
                            parent.appendChild(item);
                        }
                        r.endArray();
                    }
                    else
                    {
                        /* -------- array of primitives -> single quoted list -------- */
                        List<String> vals = new ArrayList<String>();
                        while (r.hasNext())
                        {
                            JsonToken t = r.peek();
                            if (t == JsonToken.STRING)
                            {
                                vals.add("'" + escape(r.nextString()) + "'");
                            }
                            else if (t == JsonToken.NUMBER)
                            {
                                vals.add("'" + r.nextString() + "'");
                            }
                            else if (t == JsonToken.BOOLEAN)
                            {
                                vals.add("'" + r.nextBoolean() + "'");
                            }
                            else if (t == JsonToken.NULL)
                            {
                                r.nextNull();
                                vals.add("'null'");
                            }
                            else
                            {
                                throw new IllegalStateException("Mixed types inside primitive array for key " + name);
                            }
                        }
                        r.endArray();
                        parent.setAttribute(name, join(vals, ","));
                    }
                    break;

                default:
                    throw new IllegalStateException("Unhandled token " + token);
            }
        }
    }

    /* Join without Java 8 Stream API for maximal compatibility */
    private static String join(List<String> list, String delim)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++)
        {
            if (i > 0)
            {
                sb.append(delim);
            }
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    /** Escape single quotes for the 'a','b','c' list encoding. */
    private static String escape(String s)
    {
        return s.replace("'", "\\'");
    }

    /** Ensure xsi:nil attribute and namespace are present exactly once. */
    private static void addNil(Element e, Document doc)
    {
        e.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:nil", "true");
        Element root = doc.getDocumentElement();
        if (root != null && !root.hasAttribute("xmlns:xsi"))
        {
            root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        }
    }

    private CDocumentJSON()
    {
        /* utility; no instantiation */ 
    }
    
   

        private static final String XSI_NS   = "http://www.w3.org/2001/XMLSchema-instance";
        private static final String XSI_NIL  = "nil";

        /** Public entry point. */
        public static JsonObject convert(Document doc) {
            Element root = doc.getDocumentElement();
            return elementToJson(root);
        }

        
        public static JsonObject convert(Element root) {

            return elementToJson(root);
        }
        
        /* ------------------------------------------------------------------ */
        /*                         Core recursion                             */
        /* ------------------------------------------------------------------ */

        private static JsonObject elementToJson(Element elem) {
            JsonObject obj = new JsonObject();

            /* ---------- 1.  Attributes -------------------------------------- */
            NamedNodeMap attrs = elem.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr a = (Attr) attrs.item(i);

                /* Skip namespace declarations and xsi:nil */
                if (a.getName().startsWith("xmlns") || isNilAttr(a)) continue;

                String name  = a.getName();
                name = fromXmlName(name);
                String value = a.getValue();

                JsonElement val = attributeValueToJson(value);
                obj.add(name, val);
            }

            /* ---------- 2.  Child elements ---------------------------------- */
            Map<String, List<Element>> byName = groupChildren(elem);

            for (Map.Entry<String, List<Element>> entry : byName.entrySet()) {
                String tag        = entry.getKey();
                tag = fromXmlName(tag);
                List<Element> els = entry.getValue();

                if (els.size() > 1) {                          // array of objects
                    JsonArray arr = new JsonArray();
                    for (Element e : els) arr.add(elementOrNull(e));
                    obj.add(tag, arr);
                } else {                                       // single occurrence
                    obj.add(tag, elementOrNull(els.get(0)));
                }
            }
            return obj;
        }

        /* ------------------------------------------------------------------ */
        /*                    Helpers: attributes & nulls                     */
        /* ------------------------------------------------------------------ */

        /** Attribute → primitive or array‑of‑primitives. */
        private static JsonElement attributeValueToJson(String v) {
            v = v.trim();
            if (v.startsWith("'") && v.endsWith("'")) {
                // Could be a single value ('foo') OR list ('a','b').
                if (v.indexOf("',") > 0) {                     // list detected
                    String content = v.substring(1, v.length() - 1);  // drop outer quotes
                    String[] parts = content.split("'\\s*,\\s*'");
                    JsonArray arr = new JsonArray();
                    for (String p : parts) arr.add(parsePrimitive(unescape(p)));
                    return arr;
                }
                // single quoted scalar
                return parsePrimitive(unescape(v.substring(1, v.length() - 1)));
            }
            return parsePrimitive(v);
        }

        private static JsonElement parsePrimitive(String s) {
            if ("true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s)) {
                return new JsonPrimitive(Boolean.parseBoolean(s));
            }
            if (s.matches("-?\\d+")) {                         // integer
                return new JsonPrimitive(new BigDecimal(s));
            }
            if (s.matches("-?\\d+\\.\\d+")) {                  // decimal
                return new JsonPrimitive(new BigDecimal(s));
            }
            return new JsonPrimitive(s);                       // fallback: string
        }

        private static String unescape(String s) {
            return s.replace("\\'", "'");
        }

        /** Replace element with JsonNull if xsi:nil="true". */
        private static JsonElement elementOrNull(Element e) {
            if (isNil(e)) return JsonNull.INSTANCE;
            return elementToJson(e);
        }

        private static boolean isNil(Element e) {
            return "true".equals(e.getAttributeNS(XSI_NS, XSI_NIL));
        }

        private static boolean isNilAttr(Attr a) {
            return XSI_NIL.equals(a.getLocalName()) && XSI_NS.equals(a.getNamespaceURI());
        }

        /* ------------------------------------------------------------------ */
        /*           Helpers: grouping siblings by tag name                   */
        /* ------------------------------------------------------------------ */

        private static Map<String, List<Element>> groupChildren(Element parent) {
            Map<String, List<Element>> map = new LinkedHashMap<String, List<Element>>();
            NodeList nodes = parent.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node n = nodes.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) n;
                    String   tag = e.getTagName();
                    List<Element> list = map.get(tag);
                    if (list == null) {
                        list = new ArrayList<Element>();
                        map.put(tag, list);
                    }
                    list.add(e);
                }
            }
            return map;
        }

  
        /* ===================================================================== */
        /*  Name‑escape helpers – Java 8 friendly                                */
        /* ===================================================================== */

        /** Convert any JSON key to an XML‑legal name using “_xHHHH_” escapes. */
        private static String toXmlName(String key) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < key.length(); i++) {
                char c = key.charAt(i);
                boolean first = (i == 0);
                boolean legal = (c == '-' || c == '_' || c == '.') ||
                                Character.isLetter(c) || (!first && Character.isDigit(c));
                if (legal) {
                    sb.append(c);
                } else {
                    sb.append("_x")
                      .append(String.format("%04X", (int) c))
                      .append('_');
                }
            }
            if (sb.length() == 0 || !(Character.isLetter(sb.charAt(0)) || sb.charAt(0) == '_')) {
                // guaranteed‑legal leading underscore
                sb.insert(0, '_');
            }
            return sb.toString();
        }

        /** Reverse the “_xHHHH_” escapes. */
        private static String fromXmlName(String name) {
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < name.length(); ) {
                if (i + 6 < name.length() && name.charAt(i) == '_' &&
                    name.charAt(i + 1) == 'x' && name.charAt(i + 6) == '_') {
                    String hex = name.substring(i + 2, i + 6);
                    try {
                        char ch = (char) Integer.parseInt(hex, 16);
                        out.append(ch);
                        i += 7;                               // skip "_xHHHH_"
                        continue;
                    } catch (NumberFormatException ignore) { /* fall through */ }
                }
                out.append(name.charAt(i++));
            }
            return out.toString();
        }

    
}
