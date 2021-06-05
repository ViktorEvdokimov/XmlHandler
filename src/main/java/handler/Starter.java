package handler;

import java.util.LinkedList;

public class Starter {
    public static void main(String[] args) {
        LinkedList<String[]> list = new LinkedList<String[]>();
        list.add(new String[]{"patientId", "12345"});
        list.add(new String[]{"parentConditionId", "12345"});
        list.add(new String[]{"patientId", "007"});
        list.add(new String[]{"Менструальный_цикл/encoding/terminology_id[0]", "SomeText"});
        System.out.println(XmlHandler.changeDataInXmlFile("src/main/resources/files/52122.xml", list).toString());
    }
}
