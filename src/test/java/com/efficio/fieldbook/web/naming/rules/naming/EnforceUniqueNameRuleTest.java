package com.efficio.fieldbook.web.naming.rules.naming;

import com.efficio.fieldbook.web.common.bean.AdvanceGermplasmChangeDetail;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Method;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/17/2015
 * Time: 3:17 PM
 */

@RunWith(MockitoJUnitRunner.class)
public class EnforceUniqueNameRuleTest {

	public static final int TEST_MAX_SEQUENCE = 0;
	public static final int ENFORCE_RULE_INDEX = 1;

	private EnforceUniqueNameRule dut;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private AdvancingSource source;

	@Mock
	private Method breedingMethod;

	@Mock
	private NamingRuleExecutionContext context;

	private List<String> tempData;

	@Before
	public void setUp() throws Exception {
		dut = new EnforceUniqueNameRule();
	}

	@Test
	public void testUniqueNameCheckNoMatch() throws MiddlewareQueryException, RuleException{

		setupTestExecutionContext();
		when(germplasmDataManager.checkIfMatches(anyString())).thenReturn(false);
		dut.runRule(context);

		verify(context, never()).setCurrentData(tempData);
		verify(source, never()).setCurrentMaxSequence(TEST_MAX_SEQUENCE + 1);
		verify(source, never()).setChangeDetail(any(AdvanceGermplasmChangeDetail.class));
	}

	@Test
	public void testUniqueNameCheckMatchFoundNoCount() throws MiddlewareQueryException, RuleException{

		setupTestExecutionContext();
		when(germplasmDataManager.checkIfMatches(anyString())).thenReturn(true);
		dut.runRule(context);

		// verify that current rule execution state is pointed back to previous stored data
		verify(context).setCurrentData(tempData);
		verify(source).setChangeDetail(any(AdvanceGermplasmChangeDetail.class));

		// verify that force unique name generation flag is set
		verify(source).setForceUniqueNameGeneration(true);
		// verify that default count rule is provided so that count rule execution will proceed
		verify(breedingMethod).setCount(CountRule.DEFAULT_COUNT);
	}

	@Test
	public void testUniqueNameCheckMatchFoundFlagSet()
			throws MiddlewareQueryException, RuleException {

		setupTestExecutionContext();
		when(germplasmDataManager.checkIfMatches(anyString())).thenReturn(true);
		when(source.isForceUniqueNameGeneration()).thenReturn(true);
		dut.runRule(context);

		// verify that current rule execution state is pointed back to previous stored data
		verify(context).setCurrentData(tempData);
		verify(source).setChangeDetail(any(AdvanceGermplasmChangeDetail.class));

		// verify that max sequence is incremented
		verify(source).setCurrentMaxSequence(TEST_MAX_SEQUENCE + 1);

	}

	@Test
	public void testUniqueNameCheckMatchFoundIsBulking()
			throws MiddlewareQueryException, RuleException {

		setupTestExecutionContext();
		when(germplasmDataManager.checkIfMatches(anyString())).thenReturn(true);
		when(source.isBulk()).thenReturn(true);

		dut.runRule(context);

		// verify that current rule execution state is pointed back to previous stored data
		verify(context).setCurrentData(tempData);
		verify(source).setChangeDetail(any(AdvanceGermplasmChangeDetail.class));

		// verify that force unique name generation flag is set
		verify(source).setForceUniqueNameGeneration(true);
	}

	@Test
	public void testUniqueNameCheckMatchFoundHasCountNonBulking()
			throws MiddlewareQueryException, RuleException {

		setupTestExecutionContext();
		when(germplasmDataManager.checkIfMatches(anyString())).thenReturn(true);
		when(breedingMethod.getCount()).thenReturn(CountRule.DEFAULT_COUNT);

		dut.runRule(context);

		// verify that max sequence is incremented
		verify(source).setCurrentMaxSequence(TEST_MAX_SEQUENCE + 1);

		// verify that current rule execution state is pointed back to previous stored data
		verify(context).setCurrentData(tempData);
		verify(source).setChangeDetail(any(AdvanceGermplasmChangeDetail.class));

		// verify that flag is not set so as to preserve previous count rule logic when incrementing the count
		verify(source, never()).setForceUniqueNameGeneration(true);
	}

	@Test
	public void testGetNextStepKeyNoMatchFound() throws Exception{
		setupTestExecutionContext();

		// when no duplicate is found, a change detail object is not created in the advancing source. we simulate that state here
		when(source.getChangeDetail()).thenReturn(null);

		String nextKey = dut.getNextRuleStepKey(context);

		assertNull("Expected next key is null because unique name check is last in sequence and unique name check should pass", nextKey);
	}

	@Test
	public void testGetNextStepKeyDuplicateFoundCheckFail() throws Exception {
		setupTestExecutionContext();
		AdvanceGermplasmChangeDetail detail = mock(AdvanceGermplasmChangeDetail.class);

		when(source.getChangeDetail()).thenReturn(detail);
		// if a duplicate has been found in previous steps, and a passing name has not yet been found, then the new advance name should still be null on the germplasm change detail object
		when(detail.getNewAdvanceName()).thenReturn(null);

		String nextKey = dut.getNextRuleStepKey(context);

		assertNotNull(
				"Duplicate has been found and check still fails, so next key should not be null",
				nextKey);
		assertEquals(
				"Rule does not pass execution control to CountRule even after failing the check",
				CountRule.KEY, nextKey);
	}

	@Test
	public void testGetNextStepKeyDuplicateFoundCheckPass() throws Exception {
		setupTestExecutionContext();
		AdvanceGermplasmChangeDetail detail = mock(AdvanceGermplasmChangeDetail.class);

		when(source.getChangeDetail()).thenReturn(detail);
		// if a duplicate has been found in previous steps, and a passing name has been found, then the new advance name should not be null
		when(detail.getNewAdvanceName()).thenReturn(new String());

		String nextKey = dut.getNextRuleStepKey(context);

		assertNull(
				"Duplicate has been found and but check passes, so next key should be null",
				nextKey);

	}

	protected void setupTestExecutionContext() {
		List<String> dummySequenceOrder = new ArrayList<>();
		dummySequenceOrder.add(CountRule.KEY);
		dummySequenceOrder.add(EnforceUniqueNameRule.KEY);

		List<String> dummyInitialState = new ArrayList<>();
		dummyInitialState.add("ETFN 1-1");
		dummyInitialState.add("ETFN 1-2");

		tempData = new ArrayList<>();
		tempData.add("ETFN 1-");

		when(context.getAdvancingSource()).thenReturn(source);
		when(context.getGermplasmDataManager()).thenReturn(germplasmDataManager);
		when(context.getExecutionOrder()).thenReturn(dummySequenceOrder);
		when(context.getCurrentData()).thenReturn(dummyInitialState);
		when(context.getTempData()).thenReturn(tempData);
		when(context.getMessageSource()).thenReturn(mock(MessageSource.class));

		when(source.getBreedingMethod()).thenReturn(breedingMethod);
		when(source.getCurrentMaxSequence()).thenReturn(TEST_MAX_SEQUENCE);

		when(context.getCurrentExecutionIndex()).thenReturn(ENFORCE_RULE_INDEX);

	}
}
