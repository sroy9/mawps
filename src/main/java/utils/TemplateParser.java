package utils;

import java.util.ArrayList;

import structure.Problem;

public class TemplateParser {
	public static ArrayList<String> templates = new ArrayList<>();
	
	public static ArrayList<Integer> findTemplateIndexes(ArrayList<Problem> entireRepo) {
		ArrayList<Integer> documentTemplates = new ArrayList<>();
		findUniqueTemplates(entireRepo);
		String totalEq = "";
		for (Problem prob : entireRepo) {
			for (String equation : prob.lEquations) {
				totalEq = totalEq + equation;
			}
			String templ = findTemplate(totalEq);
			int templateIndex = templates.indexOf(templ);
			documentTemplates.add(templateIndex);
		}
		return documentTemplates;
	}
	
	public static void findUniqueTemplates(ArrayList<Problem> entireRepo) {
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
		System.out.println(tempEq);
		System.out.println(equation);
		while (lastIndex < equation.length()) {
			System.out.println(lastIndex);
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

}
