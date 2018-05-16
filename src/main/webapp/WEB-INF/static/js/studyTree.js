/*globals Spinner,console,displayStudyListTree, changeBrowseStudyButtonBehavior, showErrorMessage*/
/*globals addDetailsTab, moveStudy*/
/*exported displayStudyListTree*/


/*
 1 - Can choose folder / study
 2 - Can choose folder only
 3 - Can choose previous study only
 */
var choosingType = 1;
var selectStudyFunction = null;

var url = '',
	lazyReadUrl = '',
	lazyReadAllUrl = '/Fieldbook/StudyTreeManager/retrieveChildren/',
	focusNode = null;

function studyTreeInit() {
	displayStudyListTree('studyTree', 1);
	changeBrowseStudyButtonBehavior(false);
	$('#addFolderDiv', '#studyTreeModal').hide();
	$('#renameFolderDiv', '#studyTreeModal').hide();
	$('#addFolderOkButton', '#studyTreeModal').on('click', createFolder);
	$('#addFolderCancelButton', '#studyTreeModal').on('click', hideAddFolderDiv);
	$('#renameFolderOkButton', '#studyTreeModal').on('click', submitRenameFolder);
	$('#renameFolderCancelButton', '#studyTreeModal').on('click', hideRenameFolderDiv);
	$('#addFolderDiv #addFolderName', '#studyTreeModal').on('keypress', function (event) {
		if (event.keyCode == 13) {
			createFolder();
		}
	});
	$('#renameFolderDiv #newFolderName', '#studyTreeModal').on('keypress', function (event) {
		if (event.keyCode == 13) {
			submitRenameFolder();
		}
	});
	if ($('.landing-page').length !== 0) {
		//means we are in the landing page
		$('.tree-review-button').removeClass('fbk-hide');
	} else {
		$('.tree-select-button').removeClass('fbk-hide');
	}
}

function chooseStudyNode(node, fromEnterKey) {
	'use strict';

	if (!node) {
		showErrorMessage('page-study-tree-message-modal', choosingType === 2 ? chooseProgramStudyFolderError : chooseStudyError);
		return;
	}
	if (choosingType === 1 || choosingType === 3) {
		if (node.data.isFolder === false) {
			if (choosingType === 1) {
				if ($('#studyTreeModal').data('open') === '1'
					&& node.data.programUUID != null) {
					$('#studyTreeModal').data('open', '0');
					openTreeStudy(node.data.key);
				} else {
					addDetailsTab(node.data.key, node.data.title);
				}
			}
			else {
				selectStudyFunction(node.data.key);

			}

			$('#studyTreeModal').modal('hide');
		} else {
			if (fromEnterKey === false) {
				showErrorMessage('page-study-tree-message-modal', chooseStudyError);
			}
		}
	} else if (choosingType === 2) {
		if (node.data.isFolder === true) {
			var folderDataId = node.data.key === 'LOCAL' ? 1 : node.data.key;

			if (selectStudyFunction) {
				selectStudyFunction(folderDataId);
			}

			$('#folderId').val(folderDataId);
			$('#folderName').val(node.data.title);
			$('#folderNameLabel').html(node.data.title);

			$('#studyTreeModal').modal('hide');
			if ($('body').data('doAutoSave') === '1') {
				$('body').trigger({
					type: 'DO_AUTO_SAVE'
				});
				$('body').data('doAutoSave', '0');
			}
		} else {
			if (fromEnterKey === false) {
				showErrorMessage('page-study-tree-message-modal', chooseProgramStudyFolderError);
			}
		}
	}
}

function chooseStudy() {
	'use strict';
	var node = $('#studyTree').dynatree('getTree').getActiveNode();
	chooseStudyNode(node, false);
}

function openStudyNode() {
	'use strict';
	$('#studyTreeModal').data('open', '1');
	chooseStudy();
}

function doStudyLazyLoad(node, preSelectId) {
	'use strict';
	var additionalUrl = '/0';
	if (choosingType === 2) {
		additionalUrl = '/1';
	}
	if (node.data.isFolder === true) {

		node.appendAjax({
			url: lazyReadUrl + node.data.key + additionalUrl,
			dataType: 'json',
			async: false,
			success: function (node) {
				//do nothing

				setTimeout(function () {
					node.expand();
				}, 50);
				if (node.hasChildren()) {
					$('.delete-folder', '#studyTreeModal').addClass('disable-image');
				}

				if (node.data.isFolder === false) {
					addDetailsTab(node.data.key, node.data.title);
					changeBrowseStudyButtonBehavior(false);
				} else {
					if (node.data.key === 'LOCAL') {
						changeBrowseStudyButtonBehavior(true);
						$('.edit-folder', '#studyTreeModal').addClass('disable-image');
						$('.delete-folder', '#studyTreeModal').addClass('disable-image');
					}
					else {
						changeBrowseStudyButtonBehavior(true);
					}
				}
				if (preSelectId !== null) {
					var newNode = $('#studyTree').dynatree('getTree').getNodeByKey(preSelectId);
					if (newNode !== null) {
						newNode.activate();
						newNode.focus();
					}

				}
			},
			error: function (node, XMLHttpRequest, textStatus, errorThrown) {

				console.log('The following error occured: ' + textStatus, errorThrown);
			},
			cache: false
		});
		node.expand();
	}
}

/** Germplasm Tree **/
function displayStudyListTree(treeName, choosingTypeParam, selectStudyFunctionParam, isPreSelect, postInitFunction) {
	'use strict';

	if (selectStudyFunctionParam) {
		selectStudyFunction = selectStudyFunctionParam;
	}

	url = '/Fieldbook/StudyTreeManager/loadInitialTree';
	lazyReadUrl = '/Fieldbook/StudyTreeManager/expandTree/';

	choosingType = choosingTypeParam;
	var additionalUrl = '/0';
	if (choosingType === 2) {
		additionalUrl = '/1';
	}

	url = url + additionalUrl;

	$('#' + treeName).dynatree({
		title: treeName,
		checkbox: false,
		noLink: false,
		autoFocus: false,
		imagePath: '/Fieldbook/static/img/',
		activeVisible: true,
		initAjax: {
			url: url,
			dataType: 'json',
			async: false
		},

		onLazyRead: function (node) {
			doStudyLazyLoad(node);
		},
		classNames: {
			container: 'fbtree-container',
			expander: 'fbtree-expander',
			nodeIcon: 'fbtree-icon',
			combinedIconPrefix: 'fbtree-ico-',
			focused: 'fbtree-focused',
			active: 'fbtree-active',
			nodeLoading: ''
		},
		// TODO : add functionality to update the value in the rename folder input field to match that of the newly active node
		onPostInit: function () {
			if (choosingType === 2) {
				var prefixContainer = '#create-trial';
				if (isPreSelect) {
					var programLocal = $(prefixContainer + ' #studyTree').dynatree('getTree').getNodeByKey('LOCAL');
					if (programLocal != null) {
						programLocal.activate();
						programLocal.focus();
					}
				}
			}

			if (postInitFunction) {
				postInitFunction();
			}

		},
		onFocus: function (node) {
			focusNode = node;
		},

		onActivate: function (node) {
			if (node.data.isFolder === false) {
				changeBrowseStudyButtonBehavior(false);
				$('.delete-folder', '#studyTreeModal').removeClass('disable-image');
			} else {
				if (node.data.key === 'LOCAL') {
					changeBrowseStudyButtonBehavior(true);
					$('.edit-folder', '#studyTreeModal').addClass('disable-image');
					$('.delete-folder', '#studyTreeModal').addClass('disable-image');
				}
				else {
					changeBrowseStudyButtonBehavior(true);
				}
			}
			$('#studyTreeModalBody a.dynatree-title').off('keyup');
			$('#studyTreeModalBody a.dynatree-title').on('keyup', function (e) {
				if (e.keyCode === 13) {
					if ($('.landing-page').length !== 0) {
						//means we are in the landing page
						$('#studyTreeModal').data('open', '1');
					}
					chooseStudyNode(focusNode, true);
				}
			});
		},
		onClick: function (node, event) {
			$('#newFolderName', '#studyTreeModal').val(node.data.title);
			var currentFolderName = node.data.title;
		},
		onDblClick: function (node, event) {
			openStudyNode();
		},
		dnd: {
			onDragStart: function (node) {
				/** This function MUST be defined to enable dragging for the tree.
				 * Return false to cancel dragging of node.
				 */
				if (node.data.key === 'LOCAL') {
					return false;
				}
				return true;
			},
			onDragStop: function (node) {
				// This function is optional.
			},

			preventVoidMoves: true, // Prevent dropping nodes 'before self', etc.

			onDragEnter: function (node, sourceNode) {
				/** sourceNode may be null for non-dynatree droppables.
				 * Return false to disallow dropping on node. In this case
				 * onDragOver and onDragLeave are not called.
				 * Return 'over', 'before, or 'after' to force a hitMode.
				 * Return ['before', 'after'] to restrict available hitModes.
				 * Any other return value will calc the hitMode from the cursor position.
				 */
				return true;
			},
			onDragOver: function (node, sourceNode, hitMode) {
			},

			onDrop: function (node, sourceNode, hitMode, ui, draggable) {
				/** This function MUST be defined to enable dropping of items on
				 * the tree.
				 */
				if (sourceNode.hasChildren()) {
					showErrorMessage('page-study-tree-message-modal', cannotMove + ' ' + sourceNode.data.title + ' ' + hasChildrenString);
				} else if (node.data.isFolder === false) {
					showErrorMessage('page-study-tree-message-modal', cannotMove + ' ' + node.data.title + ' ' + isAStudy);
				} else {
					$.ajax({
						url: lazyReadAllUrl + sourceNode.data.key + additionalUrl,
						type: 'GET',
						cache: false,
						aysnc: false,
						success: function (data) {
							var childCount = $.parseJSON(data).length;
							if (childCount === 0) {
								moveStudy(sourceNode, node);
							} else {
								showErrorMessage('page-study-tree-message-modal', cannotMove + ' ' + sourceNode.data.title + ' ' + hasChildrenString);
							}
						}
					});
				}
			},
			onDragLeave: function (node, sourceNode) {
			}
		}
	});
}

function openTreeStudy(id) {
	'use strict';
	location.href = '/Fieldbook/TrialManager/openTrial/' + id;

}

function addDetailsTab(studyId, title) {
	// if the study is already existing, we show that tab
	'use strict';
	if ($('li#li-study' + studyId).length !== 0) {
		$('li#li-study' + studyId + ' a').tab('show');
	} else {
		$.ajax({
			url: '/Fieldbook/StudyManager/reviewStudyDetails/show/' + studyId,
			type: 'GET',
			cache: false,
			success: function (data) {
				var close = '<i class="glyphicon glyphicon-remove fbk-close-tab" id="' + studyId + '"></i>';
				$('#study-tab-headers').append(
					'<li id="li-study' + studyId + '"><a href="#study' + studyId + '" role="tab" data-toggle="tab"><span class="review-study-name">'
					+ title + '</span>' + close + '</a></li>');
				$('#study-tabs').append(
					'<div class="info tab-pane" id="study' + studyId + '">' + data + '</div>');
				if ($('#review-study-error-' + studyId).val() !== '') {
					createErrorNotification(errorMsgHeader, $('#review-study-error-' + studyId).val());
					$('#study-tab-headers li#li-study' + studyId).remove();
					$('#study-tabs div#study' + studyId).remove();
				} else {
					initializeStudyTabs();
					$('li#li-study' + studyId + ' a').tab('show');
					$('.info#study' + studyId + ' select').each(function () {
						$(this).select2({minimumResultsForSearch: 20});
					});
					truncateStudyVariableNames('#study' + studyId + ' .review-study-name', 20);
					reviewLandingSetup();
				}
			}
		});
	}
	determineIfShowCloseAllStudyTabs();
	// if not we get the info
}