package ru.mos.emias.pmer.GenerateDocuments;

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
     * на вход подается адрес файла в котором необходимо произвести изменения и пути в файле, из которых нужно получить
     * значения. Если необходимо найти уникальный тег без указания точного пути к нему допускается в пути указать
     * .../"название уникального тега", далее, если это необходимо, допускается указывать адрес отталкиваясь от
     * найденного тэга.
     * Также, для наиболее часто используемых параметров подготовлены пути в мапе frequentlyUsedPaths.
     * Если для необходимого параметра есть ключ в frequentlyUsedPaths, вместо пути можно указать только название ключа
     * с учетом регистра.
     * Метод возвращает массив строк со значениями в указанными полях, в том порядке в котором были указаны пути. Если
     * не удалось найти значение, то в этой ячейке будет null.
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
        frequentlyUsedPaths.put("compositionId", ".../instruction_details/instruction_id[0][0]");
    }


    public static String[] changeDataInXmlFile (String pathToFile, String ... desiredPaths){
        // При первом запуске зписываются данные часто используемых адресов
        if(needToDefineMap) defineMap();
        pathToFile = "src/test/resources" + pathToFile;
        String[] results = new String[desiredPaths.length];
        try {
            // Создается построитель документа
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // Создается дерево DOM документа из файла
            Document document = documentBuilder.parse(pathToFile);

            for (int i = 0; i < desiredPaths.length; i++) {
                // проверяем есть ли для переменной  адресе путь
                String desiredPath = frequentlyUsedPaths.get(desiredPaths[i]);
                if (desiredPath == null) desiredPath = desiredPaths[i];
                String[] steps = desiredPath.split("/");
                LinkedList<String> stepList = new LinkedList<String>(Arrays.asList(steps));
                // Получаем корневой элемент
                Node root = document.getDocumentElement();
                NodeList nodeList = root.getChildNodes();
                results[i] = nodesHandler(nodeList, stepList);
            }

        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        return results;
    }

    // метод рекурсивно ищет указанный адрес, если находит записывает значение и выходит из цикла
    private static String nodesHandler(NodeList nodeList, LinkedList<String> steps){
        String step = steps.removeFirst();
        if (step.equals("...")) return tegSearcher(nodeList, steps.getFirst(), steps);
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
                    String r = node.getTextContent();
                        return r;
//                        System.out.println("complete");

                } else {
                    String data = nodesHandler(childNodes, steps);
                    if (data != null) return data;
                }
            }
        }
        return null;
    }

    private static String tegSearcher(NodeList nodeList, String target, LinkedList<String> steps){
//        System.out.println("Looking fore: " + step);
        for (int i=0; i<nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String openSymbol = "\\[";
            String[] parts = target.split(openSymbol);
            NodeList childNodes = node.getChildNodes();
            if (node.getNodeName().equals(parts[0])){
                String data = nodesHandler(nodeList, steps);
                if (data != null) return data;
            } else {
                String data = tegSearcher(childNodes, target, steps);
                if (data != null) return data;
            }
        }
        return null;
    }

}