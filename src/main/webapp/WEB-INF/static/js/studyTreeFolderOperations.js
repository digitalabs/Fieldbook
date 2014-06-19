/* global Spinner:false */
/* global showErrorMessage */
/* global deleteConfirmation */

function addFolder(object) {
    'use strict';
    if (!$(object).hasClass('disable-image')) {
        hideRenameFolderDiv();
        hideDeleteFolderDiv();
        $('#addFolderName').val('');
        $('#addFolderDiv').slideDown('fast');

    }
}

function hideAddFolderDiv() {
    'use strict';
    $('#addFolderDiv').slideUp('fast');
}

function hideRenameFolderDiv() {
    'use strict';
    $('#renameFolderDiv').slideUp('fast');
}

function hideDeleteFolderDiv() {
    'use strict';
    $('#deleteFolderDiv').slideUp('fast');
}

function renameFolder(object) {
    'use strict';

    var currentFolderName;

    if (!$(object).hasClass('disable-image')) {
        hideAddFolderDiv();
        hideDeleteFolderDiv();
        $('#renameFolderDiv').slideDown('fast');
        currentFolderName = $('#studyTree').dynatree('getTree').getActiveNode().data.title;
        $('#newFolderName').val(currentFolderName);
    }
}

function submitRenameFolder() {
    'use strict';

    var folderName = $.trim($('#newFolderName').val()),
            parentFolderId;

    if ($.trim(folderName) === $('#studyTree').dynatree('getTree').getActiveNode().data.title) {
        $('#renameStudyFolder').modal('hide');
        return false;
    }
    if (folderName === '') {
        showErrorMessage('page-rename-study-folder-message-modal', folderNameRequiredMessage);
        return false;
    } else if (!isValidInput(folderName)) {
        showErrorMessage('page-rename-study-folder-message-modal', invalidFolderNameCharacterMessage);
        return false;
    } else {
        parentFolderId = $('#studyTree').dynatree('getTree').getActiveNode().data.key;
        if (parentFolderId === 'LOCAL') {
            parentFolderId = 1;
        }

        Spinner.toggle();
        $.ajax({
            url: '/Fieldbook/StudyTreeManager/renameStudyFolder',
            type: 'POST',
            data: 'folderId=' + parentFolderId + '&newFolderName=' + folderName,
            cache: false,
            success: function (data) {
                var node;
                if (data.isSuccess === '1') {
                    hideRenameFolderDiv();
                    node = $('#studyTree').dynatree('getTree').getActiveNode();
                    node.data.title = folderName
                    $(node.span).find('a').html(folderName);
                    node.focus();
                    //lazy load the node
                    //doStudyLazyLoad($('#studyTree').dynatree('getTree').getActiveNode());
                } else {
                    showErrorMessage('page-rename-study-folder-message-modal', data.message);
                }
                Spinner.toggle();
            }
        });
    }
}

function deleteFolder(object) {
    'use strict';

    var currentFolderName;

    if (!$(object).hasClass('disable-image')) {
        hideAddFolderDiv();
        hideRenameFolderDiv();
        $('#deleteFolderDiv').slideDown('fast');
        currentFolderName = $('#studyTree').dynatree('getTree').getActiveNode().data.title
        $('#deleteFolderLabelID').html(deleteConfirmation + ' ' + currentFolderName + '?');

    }
}

function submitDeleteFolder() {
    'use strict';
    var folderId = $('#studyTree').dynatree('getTree').getActiveNode().data.key;

    Spinner.toggle();
    $.ajax({
        url: '/Fieldbook/StudyTreeManager/deleteStudyFolder',
        type: 'POST',
        data: 'folderId=' + folderId,
        cache: false,
        success: function (data) {
            var node;
            if (data.isSuccess === '1') {
                hideDeleteFolderDiv();
                node = $('#studyTree').dynatree('getTree').getActiveNode();
                if (node != null) {
                    node.remove();
                }
                changeBrowseNurseryButtonBehavior(false);
            } else {
                showErrorMessage('page-delete-study-folder-message-modal', data.message);
            }
            Spinner.toggle();
        }
    });
}