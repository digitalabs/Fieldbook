package com.efficio.fieldbook.web.common.controller;

import com.efficio.pojos.treeview.TreeNode;
import junit.framework.Assert;
import org.generationcp.middleware.enumeration.SampleListType;
import org.generationcp.middleware.pojos.ListMetadata;
import org.generationcp.middleware.pojos.SampleList;
import org.generationcp.middleware.service.api.SampleListService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class SampleTreeContollerTest {

	private static final String PROGRAM_UUID = "hfjksdhfkasd8-8324783-7knwfh";

	@Mock
	private SampleListService sampleListService;

	@InjectMocks
	private SampleTreeController sampleTreeController;


	@Test
	public void testGetSampleChildNodesForProgramLists() {

		final SampleList sampleList = new SampleList();
		final int id = 1234;
		final String listName = "List Name";
		sampleList.setId(id);
		sampleList.setListName(listName);
		sampleList.setType(SampleListType.SAMPLE_LIST);

		final List<SampleList> sampleLists = Arrays.asList(sampleList);

		final Map<Integer, ListMetadata> sampleListsMetaData = new HashMap<>();

		Mockito.when(sampleListService.getAllSampleTopLevelLists(PROGRAM_UUID)).thenReturn(sampleLists);
		Mockito.when(sampleListService.getListMetadata(sampleLists)).thenReturn(sampleListsMetaData);

		List<TreeNode> treeNodes = sampleTreeController.getSampleChildNodes(SampleTreeController.PROGRAM_LISTS, false, PROGRAM_UUID);

		Mockito.verify(sampleListService).getAllSampleTopLevelLists(PROGRAM_UUID);

		TreeNode node = treeNodes.get(0);
		Assert.assertEquals(String.valueOf(id), node.getKey());
		Assert.assertEquals(listName, node.getTitle());
		Assert.assertFalse(node.getIsLazy());
		Assert.assertFalse(node.getIsFolder());

	}

	@Test
	public void testGetSampleChildNodesForCropLists() {

		final SampleList sampleList = new SampleList();
		final int id = 1234;
		final String listName = "List Name";
		sampleList.setId(id);
		sampleList.setListName(listName);
		sampleList.setType(SampleListType.SAMPLE_LIST);

		final List<SampleList> sampleLists = Arrays.asList(sampleList);

		final Map<Integer, ListMetadata> sampleListsMetaData = new HashMap<>();

		Mockito.when(sampleListService.getAllSampleTopLevelLists(null)).thenReturn(sampleLists);
		Mockito.when(sampleListService.getListMetadata(sampleLists)).thenReturn(sampleListsMetaData);

		List<TreeNode> treeNodes = sampleTreeController.getSampleChildNodes(SampleTreeController.CROP_LISTS, false, PROGRAM_UUID);

		Mockito.verify(sampleListService).getAllSampleTopLevelLists(null);

		TreeNode node = treeNodes.get(0);
		Assert.assertEquals(String.valueOf(id), node.getKey());
		Assert.assertEquals(listName, node.getTitle());
		Assert.assertFalse(node.getIsLazy());
		Assert.assertFalse(node.getIsFolder());

	}

	@Test
	public void testGetSampleChildNodesOfAFolder() {

		final String parentFolderId = "1111";
		final SampleList sampleList = new SampleList();
		final int id = 1234;
		final String listName = "List Name";
		sampleList.setId(id);
		sampleList.setListName(listName);
		sampleList.setType(SampleListType.SAMPLE_LIST);

		final List<SampleList> sampleLists = Arrays.asList(sampleList);

		final Map<Integer, ListMetadata> sampleListsMetaData = new HashMap<>();

		Mockito.when(sampleListService.getSampleListByParentFolderIdBatched(Integer.valueOf(parentFolderId), PROGRAM_UUID, SampleTreeController.BATCH_SIZE)).thenReturn(sampleLists);
		Mockito.when(sampleListService.getListMetadata(sampleLists)).thenReturn(sampleListsMetaData);

		List<TreeNode> treeNodes = sampleTreeController.getSampleChildNodes(parentFolderId, false, PROGRAM_UUID);

		Mockito.verify(sampleListService).getSampleListByParentFolderIdBatched(Integer.valueOf(parentFolderId), PROGRAM_UUID, SampleTreeController.BATCH_SIZE);

		TreeNode node = treeNodes.get(0);
		Assert.assertEquals(String.valueOf(id), node.getKey());
		Assert.assertEquals(listName, node.getTitle());
		Assert.assertFalse(node.getIsLazy());
		Assert.assertFalse(node.getIsFolder());

	}

	@Test
	public void testGetSampleChildNodesOfAFolderChildIsAFolder() {

		final String parentFolderId = "1111";
		final SampleList sampleList = new SampleList();
		final int id = 1234;
		final String listName = "List Name";
		sampleList.setId(id);
		sampleList.setListName(listName);
		sampleList.setType(SampleListType.FOLDER);

		final List<SampleList> sampleLists = Arrays.asList(sampleList);

		final Map<Integer, ListMetadata> sampleListsMetaData = new HashMap<>();
		ListMetadata germplasmFolderMetadata = new ListMetadata();
		germplasmFolderMetadata.setNumberOfChildren(123);
		sampleListsMetaData.put(id, germplasmFolderMetadata);

		Mockito.when(sampleListService.getSampleListByParentFolderIdBatched(Integer.valueOf(parentFolderId), PROGRAM_UUID, SampleTreeController.BATCH_SIZE)).thenReturn(sampleLists);
		Mockito.when(sampleListService.getListMetadata(sampleLists)).thenReturn(sampleListsMetaData);

		List<TreeNode> treeNodes = sampleTreeController.getSampleChildNodes(parentFolderId, false, PROGRAM_UUID);

		Mockito.verify(sampleListService).getSampleListByParentFolderIdBatched(Integer.valueOf(parentFolderId), PROGRAM_UUID, SampleTreeController.BATCH_SIZE);

		TreeNode node = treeNodes.get(0);
		Assert.assertEquals(String.valueOf(id), node.getKey());
		Assert.assertEquals(listName, node.getTitle());
		Assert.assertTrue(node.getIsLazy());
		Assert.assertTrue(node.getIsFolder());

	}


}
