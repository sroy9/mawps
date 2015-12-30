package utils;

import java.util.ArrayList;
import java.util.List;

import structure.Problem;

public class TemplateParser {
	
	public static List<String> templates = new ArrayList<>();
	
	public static void populateTemplateIndexes(List<Problem> entireRepo) {
		templates.clear();
		findUniqueTemplates(entireRepo);
		for (Problem prob : entireRepo) {
			String totalEq = "";
			for (String equation : prob.lEquations) {
				totalEq = totalEq + equation;
			}
			String templ = findTemplate(totalEq);
			int templateIndex = templates.indexOf(templ);
			prob.templateNumber = templateIndex;
		}
	}
	
	public static void findUniqueTemplates(List<Problem> entireRepo) {
		for (Problem prob : entireRepo) {
			String totalEq = "";
			for (String equation : prob.lEquations) {
				totalEq = totalEq + equation;
			}
			String templ = findTemplate(totalEq);
			if (notSeen(templ) ==  true) {
				templates.add(templ);
			}	
		}
	}

	private static String findTemplate(String equation) {
		String tempEq = equation.replaceAll("[()=+-/^]","!");
		while (tempEq.indexOf('*') != -1) {
			tempEq = tempEq.substring(0, tempEq.indexOf('*')) + "!" + 
					tempEq.substring(tempEq.indexOf('*') + 1);
		}
		String resTmpl = "";
		int lastIndex = -1;
//		System.out.println(tempEq);
//		System.out.println(equation);
		while (lastIndex < equation.length()) {
//			System.out.println(lastIndex);
			lastIndex = tempEq.indexOf('!', lastIndex+1);
			if (lastIndex == -1) 
				break;
			resTmpl = resTmpl + equation.charAt(lastIndex);
			
		}
		return resTmpl;
	}

	public static boolean notSeen(String templ) {
		return !(templates.contains(templ));
	}
	
	public static void main(String args[]) {
		System.out.println(findTemplate("(2*x)=(3+7)"));
	}
}