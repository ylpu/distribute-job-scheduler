package com.ylpu.thales.scheduler.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.ylpu.thales.scheduler.request.JobRequest;
import com.ylpu.thales.scheduler.response.JobTree;

public class CommonTest {
	
	public static void listChildren(JobTree jobTree,List<Integer> children){
		if(jobTree.getChildren() == null) {
			return;
		}else {
			for(JobTree job : jobTree.getChildren()) {
				children.add(job.getJobId());
				listChildren(job,children);
			}
		}
	}
	
    private static boolean isCycleReference(List<Integer> children,String depends) {
		if(StringUtils.isNotBlank(depends)){
		 	   String[] dependIds = depends.split(",");
			   for(String str : dependIds) {
				   if(children.contains(NumberUtils.toInt(str))) {
					   return true;
				   }
			   }
		}
	    return false;
}

	public static void main(String[] args) {
		
		JobTree jobTree = new JobTree();
		jobTree.setJobId(1);
		
		List<JobTree> children = new ArrayList<JobTree>();
		JobTree jobTree1 = new JobTree();
		jobTree1.setJobId(2);
		children.add(jobTree1);
		jobTree.setChildren(children);
		
		List<JobTree> children1 = new ArrayList<JobTree>();
		JobTree jobTree2 = new JobTree();
		jobTree2.setJobId(3);
		children1.add(jobTree2);
		jobTree1.setChildren(children1);
		
		List<Integer> list = new ArrayList<Integer>();
		listChildren(jobTree,list);
		String depends = "4,5";
		System.out.println(isCycleReference(list,depends));
	}
}
