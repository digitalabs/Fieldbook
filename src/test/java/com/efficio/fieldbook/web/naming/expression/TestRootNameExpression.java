package com.efficio.fieldbook.web.naming.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.generationcp.middleware.pojos.Name;
import org.junit.Test;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class TestRootNameExpression extends TestExpression {

	@Test
	public void test() throws Exception {
		List<String> input = Arrays.asList("a(b/c)d", "a/bc", "/abc", "(ab)/(de)", "(abc)a/e", "((a/b))", "b/e(a/b)",
				"(b/e)/(a/b)", "(CML146/CLQ-6203)/CML147", "(CLQ-6203/CML150)/CML144", "(L-133/LSA-297)/PA-1", "((P 47/MPSWCB 4) 11//(MPSWCB",
				"(a//b)", "(a/b/c/d)");

		List<String> expectedOutput = Arrays.asList("a(b/c)d", "(a/bc)", "(/abc)", "((ab)/(de))", "((abc)a/e)", "((a/b))", "(b/e(a/b))",
				"((b/e)/(a/b))", "((CML146/CLQ-6203)/CML147)", "((CLQ-6203/CML150)/CML144)", "((L-133/LSA-297)/PA-1)", "(((P 47/MPSWCB 4) 11//(MPSWCB)",
				"(a//b)", "(a/b/c/d)");
		
		RootNameExpression rne = new RootNameExpression();
		AdvancingSource source = createAdvancingSourceTestData("Germplasm", null, null, null, null, true);
		int i = 0;
		Name name = new Name();
		name.setTypeId(10);
		source.getNames().add(name);
		source.getBreedingMethod().setSnametype(10);
		for (String nameString : input) {
			System.out.println("INPUT = " + nameString);
			List<StringBuilder> builders = new ArrayList<StringBuilder>();
			builders.add(new StringBuilder());
			name.setNval(nameString);
			rne.apply(builders, source);
			String output =  builders.get(0).toString();
			System.out.println("OUTPUT = " + output);
			System.out.println("CORRECT? " + expectedOutput.get(i).equals(output) + "\n\n");
			Assert.assertEquals(expectedOutput.get(i), output);
			i++;
		}
	}
}
