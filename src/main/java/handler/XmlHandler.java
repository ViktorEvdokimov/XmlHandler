package handler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlHandler {
    static int numberInDesiredGroup;

    public static void main(String[] args) {
        String desiredPath = "context/Подробности_контекста/ИД_пациента/value/rm:id";
        numberInDesiredGroup = 0;
        String filepath = "src/main/resources/files/52122.xml";
        try {
            String[] steps = desiredPath.split("/");
            LinkedList<String> stepList = new LinkedList<String>(Arrays.asList(steps));
            // Создается построитель документа
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // Создается дерево DOM документа из файла
            Document document = documentBuilder.parse(filepath);

            // Получаем корневой элемент
            Node root = document.getDocumentElement();

            NodeList nodeList = root.getChildNodes();

            System.out.println(nodesHandler(nodeList, stepList));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(filepath));
            transformer.transform(source, result);

        } catch (ParserConfigurationException ex) {
            ex.printStackTrace(System.out);
        } catch (SAXException ex) {
            ex.printStackTrace(System.out);
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private static boolean nodesHandler(NodeList nodeList, LinkedList<String> steps){
        String step = steps.removeFirst();
        System.out.println("Looking fore: " + step);
        for (int i=0; i<nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeName().equals(step)){
                if(steps.size()==0){
                    System.out.println(node.getTextContent());
                    NodeList childNodes = node.getChildNodes();
                    childNodes.item(numberInDesiredGroup).setTextContent("qwer4");
                    System.out.println("complete");
                    return true;
                } else {
                    NodeList childNodes = node.getChildNodes();
                    if (nodesHandler(childNodes, steps)) return true;
                }
            }
        }
        return false;
    }

}
