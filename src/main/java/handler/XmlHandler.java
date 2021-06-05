package handler;

import java.io.File;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlHandler {

    /**
     *
     * Работа с методом changeDataInXmlFile:
     * на вход подается адрес файла в котором необходимо произвести изменения и лист путей в файле и новых значений.
     * В лист записываются массив из двух строк:
     *      первая: путь к требуемому параметру в файле, либо название параметра если он определен в frequentlyUsedPaths
     *      вторая: значение которое необходимо установить по указанному пути.
     * Также, для наиболее часто используемых параметров подготовлены пути в мапе frequentlyUsedPaths.
     * Если для необходимого параметра есть ключ в frequentlyUsedPaths, вместо адреса можно указать только название ключа
     * с учетом регистра.
     *
     */

    private static boolean needToDefineMap = true;
    private static final Map<String, String> frequentlyUsedPaths = new HashMap<String, String>();

    private static void defineMap() {
        needToDefineMap=false;
        frequentlyUsedPaths.put("patientId", "context/Подробности_контекста/ИД_пациента/value[2]");
        frequentlyUsedPaths.put("documentId", "context/Подробности_контекста/ИД_документа/value[2]");
        frequentlyUsedPaths.put("startTimeId", "context/start_time[0]");
        frequentlyUsedPaths.put("eventId", "context/Подробности_контекста/ИД_события/value[2]");
        frequentlyUsedPaths.put("parentConditionId", "Сведения_о_выполнении/Инструментальное_исследование/instruction_details/instruction_id[0][0]");
        frequentlyUsedPaths.put("conditionId", "uid[0]");
    }


    public static Map<String, Boolean> changeDataInXmlFile (String pathToFile, List<String[]> desiredPathAndNewData){
        // При первом запуске зписываются данные часто используемых адресов
        if(needToDefineMap) defineMap();
        Map<String, Boolean> results = new HashMap<String, Boolean>();
        try {
            // Создается построитель документа
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // Создается дерево DOM документа из файла
            Document document = documentBuilder.parse(pathToFile);

            for (int i = 0; i < desiredPathAndNewData.size(); i++) {
                String[] pathAndData = desiredPathAndNewData.get(i);
                // проверяем есть ли для переменной  адресе путь
                String desiredPath = frequentlyUsedPaths.get(pathAndData[0]);
                if (desiredPath == null) desiredPath = pathAndData[0];
                String[] steps = desiredPath.split("/");
                LinkedList<String> stepList = new LinkedList<String>(Arrays.asList(steps));


                // Получаем корневой элемент
                Node root = document.getDocumentElement();

                NodeList nodeList = root.getChildNodes();

                results.put(pathAndData[0], nodesHandler(nodeList, stepList, pathAndData[1]));
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(pathToFile));
            transformer.transform(source, result);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        return results;
    }

    // метод рекурсивно ищет указанный адрес, если находит записывает значение и выходит из цикла
    private static boolean nodesHandler(NodeList nodeList, LinkedList<String> steps, String newData){
        String step = steps.removeFirst();
//        System.out.println("Looking fore: " + step);
        for (int i=0; i<nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String openSymbol = "\\[";
            String[] parts = step.split(openSymbol);
            if (node.getNodeName().equals(parts[0])){
                NodeList childNodes = node.getChildNodes();
                if (parts.length>1) {
                    for (int j = 1; j < parts.length; j++) {
                        StringBuilder sb = new StringBuilder();
                        for (int k = 0; k < parts[j].length()-1; k++) {
                            sb.append(parts[j].charAt(k));
                        }
                        int nextStep = Integer.parseInt(sb.toString());
                        node = childNodes.item((nextStep + 1) * 2 - 1);
                        childNodes = node.getChildNodes();
                    }
                }
                    if (steps.size() == 0) {
//                        System.out.println(node.getTextContent());
                        childNodes.item(0).setTextContent(newData);
//                        System.out.println("complete");
                        return true;
                    } else {
                        if (nodesHandler(childNodes, steps, newData)) return true;
                    }
            }
        }
        return false;
    }

}
