package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Node {
	
	public String label;
	public List<Node> children;

	public Node() {
		children = new ArrayList<>();
	}
	
	public Node(String label, List<Node> children) {
		this.label = label;
		this.children = children;
	}
	
	public Node(Node other) {
		this();
		this.label = other.label;
		for(Node child : other.children) {
			this.children.add(new Node(child));			
		}
	}
	
	@Override
	public String toString() {
		if(children.size() == 0) return label;
		if(label.equals("-") || label.equals("/")) {
			return "("+children.get(0).toString() + " " + label + " " + 
					children.get(1).toString()+")";
		}
		if(children.get(0).toString().compareTo(children.get(1).toString()) > 0) {
			return "("+children.get(0).toString() + " " + label + " " + 
					children.get(1).toString()+")";
		} else {
			return "("+children.get(1).toString() + " " + label + " " + 
					children.get(0).toString()+")";
		}
			
	}
	
	public static boolean isEquivalent(Node node1, Node node2) {
		if(node1.children.size() != node2.children.size()) return false;
		if(!node1.label.equals(node2.label)) return false;
		if(node1.label.equals("-") || node1.label.equals("/")) {
			return isEquivalent(node1.children.get(0), node2.children.get(0)) &&
					isEquivalent(node1.children.get(1), node2.children.get(1));
		} else {
			return 
				(isEquivalent(node1.children.get(0), node2.children.get(0)) &&  
				isEquivalent(node1.children.get(1), node2.children.get(1))) || 
				(isEquivalent(node1.children.get(0), node2.children.get(1)) &&
				isEquivalent(node1.children.get(1), node2.children.get(0)));
		}
	}
	
	public static Node parseNode(String eqString) {
		return parse(eqString);
	}
	
	public static Node parse(String eqString) {
		eqString = eqString.trim();
//		System.out.println("EqString : "+eqString);
		int index = eqString.indexOf("=");
		if(index != -1) {
			Node node = new Node();
			node.label = "=";
			node.children.add(parse(eqString.substring(0, index)));
			node.children.add(parse(eqString.substring(index+1)));
			return node;
		}
		if(eqString.charAt(0)=='(' && eqString.charAt(eqString.length()-1)==')') {
			eqString = eqString.substring(1, eqString.length()-1);
		}
		index = indexOfMathOp(eqString, Arrays.asList('+', '-', '*', '/'));
		Node node = new Node();
		if(index > 0) {
			if(eqString.charAt(index) == '+') node.label = "+";
			else if(eqString.charAt(index) == '-') node.label = "-";
			else if(eqString.charAt(index) == '*') node.label = "*";
			else if(eqString.charAt(index) == '/') node.label = "/";
			else node.label = "ISSUE";
			node.children.add(parse(eqString.substring(0, index)));
			node.children.add(parse(eqString.substring(index+1)));
			return node;
		} else {
			node.label = "";
		}
		return node;
	}
	
	public static int indexOfMathOp(String equationString, List<Character> keys) {
		for(int index=0; index<equationString.length(); ++index) {
			if(keys.contains(equationString.charAt(index))) {
				int open = 0, close = 0;
				for(int i=index; i>=0; --i) {
					if(equationString.charAt(i) == ')') close++;
					if(equationString.charAt(i) == '(') open++;
				}
				if(open==close) {
					return index;
				}
			}
		}
		return -1;
	}
	
	public List<Node> getLeaves() {
		List<Node> leaves = new ArrayList<Node>();
		if(children.size() == 0) {
			leaves.add(this);
		} else {
			leaves.addAll(children.get(0).getLeaves());
			leaves.addAll(children.get(1).getLeaves());
		}
		return leaves;
	}
	
	public List<Node> getAllSubNodes() {
		List<Node> all = new ArrayList<Node>();
		all.add(this);
		if(children.size() == 2) {
			all.addAll(children.get(0).getAllSubNodes());
			all.addAll(children.get(1).getAllSubNodes());
		}
		return all;
	}
}
