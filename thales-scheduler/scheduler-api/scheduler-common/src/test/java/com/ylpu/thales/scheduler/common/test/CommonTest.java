package com.ylpu.thales.scheduler.common.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

class Common{
    
    private int id;
    private String name;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    
    
}
public class CommonTest {
    
    private static Map<Integer,Common> map = new HashMap<Integer,Common>();

    public static void main(String[] args) {
        String groupPath = "/thales/workers";
        System.out.println(groupPath.substring(groupPath.lastIndexOf("/") + 1));
        Common common = new Common();
        common.setId(1);
        common.setName("test");
        map.put(common.getId(), common);
        
        Common common1 = new Common();
        common1.setId(1);
        common1.setName("test1");
        map.put(common1.getId(), common1);
        System.out.println(map.size());
        for(Entry<Integer, Common> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue().getName());
        }
        
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append("$SPARK_HOME/bin/" + "spark-submit");

        
        commandBuilder.append(" --class " + "com.test.test" + " \\");
        commandBuilder.append("\n");
        
        commandBuilder.append(" --master yarn \\");
        
        String str = "{\"fileName\" : \"/Users/yupu/shell/test.sh\",\"parameters\" : \"\"}";
        
        System.out.println(commandBuilder);
        
        String test = "/thales/hive/127.0.0.1";
        System.out.println(test.substring(test.lastIndexOf("/") + 1));
    }

}
