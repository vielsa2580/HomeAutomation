//package com.orvito.homevito.utils;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import com.orvito.homevito.models.MODELHardwareType;
//import com.orvito.homevito.models.MODELNode;
//import com.orvito.homevito.models.MODELRoom;
//
//public class JustForFun {
//	
//	public static List<MODELRoom> getroomlist() {
//		MODELRoom modelRoom1=new MODELRoom("", "Master Bedroom");
//		MODELNode node1=new MODELNode("TubeLight-MB","","",new MODELHardwareType(""));
//		MODELNode node2=new MODELNode("Fan-MB","","",new MODELHardwareType(""));
//		MODELNode node3=new MODELNode("AC-MB","","",new MODELHardwareType(""));
//		List<MODELNode> nodesList1=new ArrayList<MODELNode>();
//		nodesList1.add(node1);
//		nodesList1.add(node2);
//		nodesList1.add(node3);
//		modelRoom1.setNodeList(nodesList1);
//		
//		
//		MODELRoom modelRoom2=new MODELRoom("", "Children Bedroom");
//		MODELNode node4=new MODELNode("TubeLight-CB","","",new MODELHardwareType(""));
//		MODELNode node5=new MODELNode("Fan-CB","","",new MODELHardwareType(""));
//		MODELNode node6=new MODELNode("AC-CB","","",new MODELHardwareType(""));
//		List<MODELNode> nodesList2=new ArrayList<MODELNode>();
//		nodesList2.add(node4);
//		nodesList2.add(node5);
//		nodesList2.add(node6);
//		modelRoom2.setNodeList(nodesList2);
//		
//		MODELRoom modelRoom3=new MODELRoom("", "Guest Bedroom");
//		MODELNode node7=new MODELNode("TubeLight-GB","","",new MODELHardwareType(""));
//		MODELNode node8=new MODELNode("Fan-GB","","",new MODELHardwareType(""));
//		MODELNode node9=new MODELNode("AC-GB","","",new MODELHardwareType(""));
//		List<MODELNode> nodesList3=new ArrayList<MODELNode>();
//		nodesList3.add(node7);
//		nodesList3.add(node8);
//		nodesList3.add(node9);
//		modelRoom3.setNodeList(nodesList3);
//		List<MODELRoom> list=new ArrayList<MODELRoom>();
//		list.add(modelRoom1);
//		list.add(modelRoom2);
//		list.add(modelRoom3);
//		return list;
//	} 
//
//}
