package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import junit.framework.Assert;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.pojo.treeview.TreeTableNode;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Integration tests for SampleTreeController class
 */
public class SampleTreeControllerTestIT extends AbstractBaseIntegrationTest {

	@Resource
	private SampleTreeController sampleTreeController;

	/** The object mapper. */
	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Test load initial tree.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testLoadInitialTree() throws Exception {

		String jsonResponse = this.sampleTreeController.loadInitialSampleTree("0");

		List<TreeNode> treeNodes = this.objectMapper.readValue(jsonResponse, new TypeReference<List<TreeNode>>() {
		});

		Assert.assertEquals(2, treeNodes.size());
		Assert.assertEquals(SampleTreeController.CROP_LISTS, treeNodes.get(0).getKey());
		Assert.assertEquals(SampleTreeController.PROGRAM_LISTS, treeNodes.get(1).getKey());
	}

	/**
	 * Test load initial tree table.
	 *
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadInitialGermplasmTreeTable() throws Exception {
		ExtendedModelMap model = new ExtendedModelMap();
		this.sampleTreeController.loadInitialSampleTreeTable(model);
		List<TreeTableNode> treeNodes = (List<TreeTableNode>) model.get(SampleTreeController.LIST_ROOT_NODES);
		int numberOfRootNodes = 0;
		List<TreeTableNode> rootNodes = new ArrayList<>();
		for (TreeTableNode treeTableNode : treeNodes) {
			if (treeTableNode.getParentId() == null) {
				rootNodes.add(treeTableNode);
				numberOfRootNodes++;
			}
		}

		Assert.assertEquals("The number of root nodes should be 2", 2, numberOfRootNodes);
		Assert.assertEquals("The first root node should be have an id of " + SampleTreeController.CROP_LISTS,
				SampleTreeController.CROP_LISTS, rootNodes.get(0).getId());
	}

}
