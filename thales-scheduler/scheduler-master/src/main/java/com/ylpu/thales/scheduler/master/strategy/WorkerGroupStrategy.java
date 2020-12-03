package com.ylpu.thales.scheduler.master.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ylpu.thales.scheduler.request.GroupStrategyRequest;
import com.ylpu.thales.scheduler.response.GroupStrategyResponse;

public class WorkerGroupStrategy {
    
//    private static Map<String,String> workStrategyMap = new HashMap<String,String>();
//    
//    public static void init() throws Exception {
//        
//        List<GroupStrategyResponse> list = WorkerManager.getAllGroupStrategy();
//        if(list != null && list.size() > 0) {
//            for(GroupStrategyResponse groupStrategy : list) {
//                workStrategyMap.put(groupStrategy.getGroupName(), groupStrategy.getGroupStrategy());
//            }
//        }
//    }
//    
//    public static void addOrUpdateGroupStrategy(GroupStrategyRequest groupStrategy) {
//        workStrategyMap.put(groupStrategy.getGroupName(), groupStrategy.getGroupStrategy());
//    }
//    
//    public static String getGroupStrategy(String groupName) {
//        return workStrategyMap.get(groupName);
//    }
}
