package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.pojos.treeview.TreeTableNode;

public class TreeViewUtilTest extends AbstractBaseIntegrationTest {
	
	private static final Integer LIST_USER_ID = 1;
	
	/** The Constant LOCAL_LIST_1. */
    private static final GermplasmList LOCAL_LIST_1 = 
            new GermplasmList(-1, "Local List 1", null, "FOLDER", LIST_USER_ID, "Local List Description 1", null, 1);
    
    /** The Constant LOCAL_LIST_2. */
    private static final GermplasmList LOCAL_LIST_2 = 
            new GermplasmList(-2, "Local List 2", null, null, LIST_USER_ID, null, null, 1);
    
    /** The Constant LOCAL_LIST_3. */
    private static final GermplasmList LOCAL_LIST_3 = 
            new GermplasmList(-3, "Local List 3", null, "LST", LIST_USER_ID, 
            		"A very long long long long long description ", LOCAL_LIST_1, 1);
 
    /** The Constant LOCAL_GERMPLASM_LIST_TEST_DATA. */
    private static final List<GermplasmList> LOCAL_GERMPLASM_LIST_TEST_DATA = 
            Arrays.asList(LOCAL_LIST_1, LOCAL_LIST_2, LOCAL_LIST_3);
    
    /** The Constant CENTRAL_LIST_1. */
    private static final GermplasmList CENTRAL_LIST_1 = 
            new GermplasmList(1, "Central List 1", null, "FOLDER", LIST_USER_ID, 
            		"Central List Folder 1", null, 1);
    
    /** The Constant CENTRAL_LIST_2. */
    private static final GermplasmList CENTRAL_LIST_2 = 
            new GermplasmList(2, "Central List 2", null, "LST", LIST_USER_ID, 
            		"Central List 2", CENTRAL_LIST_1, 1);
    
    /** The Constant CENTRAL_GERMPLASM_LIST_TEST_DATA. */
    private static final List<GermplasmList> CENTRAL_GERMPLASM_LIST_TEST_DATA = 
            Arrays.asList(CENTRAL_LIST_1, CENTRAL_LIST_2);
    
    private static final List<GermplasmList> EMPTY_GERMPLASM_LIST_TEST_DATA = 
            new ArrayList<GermplasmList>();
    
    private static final List<GermplasmList> NULL_GERMPLASM_LIST_TEST_DATA = null;
    private static GermplasmListManager germplasmListManager;
    private static UserDataManager userDataManager;
    private static List<UserDefinedField> userDefinedFields;
    
    @BeforeClass
    public static void setupClass() throws MiddlewareQueryException {
    	mockGermplasmListManagerAndSomeOfItsMethods();
    	mockUserDataManagerAndSomeOfItsMethods();
    }
    
    private static void mockUserDataManagerAndSomeOfItsMethods() 
    		throws MiddlewareQueryException {
    	userDataManager = Mockito.mock(UserDataManager.class);
		Mockito.when(userDataManager.getUserById(LIST_USER_ID)).thenReturn(null);
	}

	private static void mockGermplasmListManagerAndSomeOfItsMethods() 
			throws MiddlewareQueryException {
		germplasmListManager = Mockito.mock(GermplasmListManager.class);
		userDefinedFields = createGermplasmListUserDefinedFields();
        Mockito.when(germplasmListManager.getGermplasmListTypes())
        	.thenReturn(userDefinedFields);
	}
	
	

	@Test
	public void testConvertGermplasmListToTreeTableNodes() {
		List<GermplasmList> germplasmLists = new ArrayList<GermplasmList>(LOCAL_GERMPLASM_LIST_TEST_DATA);
		germplasmLists.addAll(CENTRAL_GERMPLASM_LIST_TEST_DATA);
		List<TreeTableNode> treeTableNodes = TreeViewUtil.convertGermplasmListToTreeTableNodes(
				germplasmLists, userDataManager, germplasmListManager);
		
		Assert.assertTrue("The list should not be null",treeTableNodes!=null);
		Assert.assertTrue("The list should not be empty",!treeTableNodes.isEmpty());
		Assert.assertEquals("The list should have 5 items",5,treeTableNodes.size());
		for (TreeTableNode treeTableNode : treeTableNodes) {
			GermplasmList germplasmList = null;
			switch(Integer.parseInt(treeTableNode.getId())) {
				case -1: germplasmList = LOCAL_LIST_1; break;
				case -2: germplasmList = LOCAL_LIST_2; break;
				case -3: germplasmList = LOCAL_LIST_3; break;
				case 1: germplasmList = CENTRAL_LIST_1; break;
				case 2: germplasmList = CENTRAL_LIST_2; break;
			}
			Assert.assertEquals("The id should be " + germplasmList.getId(),
					Integer.toString(germplasmList.getId()),treeTableNode.getId());
			Assert.assertEquals("The name should be " + germplasmList.getName(),
					germplasmList.getName(),treeTableNode.getName());
			String descriptionForDisplay = getDescriptionForDisplay(germplasmList.getDescription());
			Assert.assertEquals("The description should be " + descriptionForDisplay,
					descriptionForDisplay,treeTableNode.getDescription());
			String isFolder = "0";
			if(germplasmList.getType()!=null && "FOLDER".equals(germplasmList.getType())){
				isFolder = "1";
			}
			Assert.assertTrue("The tree table node should be a folder",
					isFolder.equals(treeTableNode.getIsFolder()));
			int noOfEntries = germplasmList.getListData().size();
			String noOfEntriesDisplay = noOfEntries==0?"":String.valueOf(noOfEntries);
			Assert.assertEquals("The no of entries should be " + noOfEntriesDisplay,
					noOfEntriesDisplay,treeTableNode.getNoOfEntries());
			String parentId = getParentId(germplasmList);
			Assert.assertEquals("The parent id should be "+parentId,
					parentId,treeTableNode.getParentId());
			String type = getType(germplasmList.getType());
			Assert.assertEquals("The type should be "+type,
					type,treeTableNode.getType());
			String owner = getOwnerListName(germplasmList.getUserId());
			Assert.assertEquals("The owner should be "+owner,
					owner,treeTableNode.getOwner());
		}
	}
	
	public static String getParentId(GermplasmList germplasmList) {
		Integer parentId = germplasmList.getParentId();
		if(parentId==null) {
			if(germplasmList.getId()>0) {
				return "CENTRAL";
			} else {
				return "LOCAL";
			}
		}
		return String.valueOf(parentId);
	}
    
    private String getOwnerListName(Integer userId) {
		return "";
	}

	private String getType(String type) {
		if(type!=null) {
	    	for (UserDefinedField listType : userDefinedFields) {
	            if(type.equals(listType.getFcode())){
	                return listType.getFname();
	            }
	        }
		}
    	return "Germplasm List";
	}

	private String getDescriptionForDisplay(String germplasmDescription){
        String description = "-";
        if(germplasmDescription != null){
            description = germplasmDescription.replaceAll("<", "&lt;");
            description = description.replaceAll(">", "&gt;");
            if(description.length() > 27){
                description = description.substring(0, 27) + "...";
            }
        }
        return description;
    }
    
    private static List<UserDefinedField> createGermplasmListUserDefinedFields() {
		List<UserDefinedField> userDefinedFields = new ArrayList<UserDefinedField>();
		UserDefinedField listType = new UserDefinedField();
		listType.setFcode("LST");
		listType.setFname("LIST FOLDER");
		userDefinedFields.add(listType);
		UserDefinedField folderType = new UserDefinedField();
		folderType.setFcode("LST");
		folderType.setFname("FOLDER");
		userDefinedFields.add(folderType);
		return userDefinedFields;
	}

    
    @Test
	public void testConvertGermplasmListToTreeTableNodes_NullList() {
		List<TreeTableNode> treeTableNodes = TreeViewUtil.convertGermplasmListToTreeTableNodes(
				NULL_GERMPLASM_LIST_TEST_DATA, userDataManager, germplasmListManager);
		Assert.assertTrue("The list should be empty",treeTableNodes.isEmpty());
	}
    
    @Test
	public void testConvertGermplasmListToTreeTableNodes_EmptyList() {
		List<TreeTableNode> treeTableNodes = TreeViewUtil.convertGermplasmListToTreeTableNodes(
				EMPTY_GERMPLASM_LIST_TEST_DATA, userDataManager, germplasmListManager);
		Assert.assertTrue("The list should be empty",treeTableNodes.isEmpty());
	}
	
}
