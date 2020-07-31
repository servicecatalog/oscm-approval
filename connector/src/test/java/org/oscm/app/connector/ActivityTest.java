/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019                                           
 *                                                                                                                                 
 *  Creation Date: 28.11.2019                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.oscm.app.connector.framework.Activity;
import org.oscm.app.connector.framework.ProcessException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author goebel
 *
 */
public class ActivityTest {

    final String PROPERTY_TEMPLATE = "&lt;html&gt;&lt;head&gt;&lt;meta http-equiv=&quot;Content-Type&quot; content=&quot;text/html; charset=UTF-8&quot;/&gt;   &lt;style&gt;     td, th, caption  { font: 15px arial;  }       caption  { font: bold 15px arial; background-color: #F2F2F2; margin:5px;}        .tcol       { width: 200px; }     .vcol       { width: 400px; }      .ckey        {  font: 15px arial; }      .cval        {  font: 15px arial; background-color: #F2F2F2; }   &lt;/style&gt;&lt;/head&gt; &lt;body&gt; &lt;p&gt; $(mail.body) &lt;/p&gt; &lt;hr/&gt;  &lt;table &gt;   &lt;caption&gt;SUBSCRIPTION TECHNICAL DATA&lt;/caption&gt;    &lt;colgroup&gt; &lt;col class=&quot;tcol&quot;/&gt;      &lt;col class=&quot;vcol&quot; /&gt;   &lt;/colgroup&gt;      &lt;tbody&gt;       &lt;tr &gt;      &lt;td class=&quot;ckey&quot;&gt;Service Name&lt;/td&gt;      &lt;td class=&quot;cval&quot;&gt;$(service.name)&lt;/td&gt;    &lt;/tr&gt;    &lt;tr &gt;      &lt;td class=&quot;ckey&quot;&gt;Technical Service ID&lt;/td&gt;      &lt;td class=&quot;cval&quot;&gt;$(service.technicalId)&lt;/td&gt;    &lt;/tr&gt;    &lt;tr &gt;      &lt;td class=&quot;ckey&quot;&gt;Organisation ID&lt;/td&gt;      &lt;td class=&quot;cval&quot;&gt;$(user.orgId)&lt;/td&gt;    &lt;/tr&gt;    &lt;tr &gt;      &lt;td class=&quot;ckey&quot;&gt;User Key&lt;/td&gt;      &lt;td class=&quot;cval&quot;&gt;$(user.key)&lt;/td&gt;    &lt;/tr&gt;       &lt;/tbody&gt;  &lt;/table &gt;     &lt;hr/&gt;      &lt;table &gt;    &lt;caption&gt;USER DATA&lt;/caption&gt;    &lt;colgroup &gt;       &lt;col class=&quot;tcol&quot;/&gt;    &lt;col class=&quot;vcol&quot;/&gt;     &lt;/colgroup&gt;      &lt;tbody&gt;      &lt;tr &gt;        &lt;td class=&quot;ckey&quot;&gt;First Name&lt;/td&gt;   &lt;td class=&quot;cval&quot;&gt;$(user.firstname)&lt;/td&gt;      &lt;/tr&gt;      &lt;tr &gt;        &lt;td class=&quot;ckey&quot;&gt;Name&lt;/td&gt;        &lt;td class=&quot;cval&quot;&gt;$(user.lastname)&lt;/td&gt;      &lt;/tr &gt;      &lt;tr &gt;        &lt;td class=&quot;ckey&quot;&gt;Email&lt;/td&gt;        &lt;td class=&quot;cval&quot;&gt;$(user.email)&lt;/td&gt;      &lt;/tr &gt;    &lt;/tbody&gt;   &lt;/table &gt;      &lt;hr/&gt;   &lt;br/&gt;&lt;br/&gt;&lt;p&gt;Best regards,&lt;/p&gt;&lt;p&gt;OSCM Approval Master&lt;br/&gt;&lt;/p&gt;  &lt;/body&gt; &lt;/html&gt;";

    @Test
    public void replacePlaceholderTest() throws Exception {

        // given setting
        final Map<String, String> settings = Stream.of(new String[][] { //
                { "UserKey", "1000" }, // UserKey
                { "Organization", "ACME Supplier" }, // Organization
                { "service.name", "Test Service" }, // service.name
                { "service.technicalId", "TEC Service" }, // service.technicalId
                { "user.orgId", "ACME Supplier" }, // user.orgId
                { "user.email", "lorenz.goebel@est.fujitsu.com" }, // user.email
                { "user.firstname", "Lorenz" }, // user.firstname
                { "user.lastname", "Goebel" }, // user.lastname
                { "user.key", "1000" }, // user.key 
                { "mail.body", loadMailBody() } //mail.body

        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        // when
        String formatted = replacePlaceholders(settings);

        String output = makePretty(formatted);

        System.out.println(indent(output));
    }

   
    private String replacePlaceholders(final Map<String, String> settings) throws ProcessException {
        return new Activity() {

            @Override
            public void doConfigure(Properties props) throws ProcessException {
            }

            public String replacePlaceholder(String value, Map<String, String> transmitData)
                    throws ProcessException {
                return super.replacePlaceholder(value, transmitData);
            }
        }.replacePlaceholder(PROPERTY_TEMPLATE, settings);
    }

    private String loadMailBody() {
        Properties props = new Properties();
        try (InputStream in = getClass().getResource("/messages_en.properties").openStream()) {
            props.load(in);

        } catch (IOException io) {
            throw new RuntimeException(io);
        }
        String txt = props.getProperty("mail_approval.text");
        txt = txt.replace("{0}", "https//myserver:8881/approval");
        return txt;
    }

    private String indent(String page) {
        try {
            NodeList nodes = asDocument(page).getElementsByTagName("html");
            Transformer tf = TransformerFactory.newInstance().newTransformer();

            tf.setOutputProperty(OutputKeys.METHOD, "html");
            tf.setOutputProperty("http://www.oracle.com/xml/is-standalone", "yes");
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            tf.setOutputProperty(OutputKeys.INDENT, "yes");

            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(nodes.item(0));

            tf.transform(source, result);
            return result.getWriter().toString();
        } catch (TransformerException | TransformerFactoryConfigurationError | SAXException
                | ParserConfigurationException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static Document asDocument(String page)
            throws SAXException, ParserConfigurationException, IOException {

        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();

        dfactory.setValidating(false);
        dfactory.setIgnoringElementContentWhitespace(true);
        dfactory.setNamespaceAware(true);
        DocumentBuilder builder = dfactory.newDocumentBuilder();

        Document doc = builder.parse(new InputSource(new StringReader(page)));
        return doc;
    }

    private String makePretty(String val) {
        val = val.replaceAll("&lt;", "<");
        val = val.replaceAll("&gt;", ">");
        val = val.replaceAll("&quot;", "\"");
        return val;
    }

}
