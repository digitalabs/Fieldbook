/*global $:false */
var StockIDFunctions = window.StockIDFunctions;

if (typeof StockIDFunctions === 'undefined') {
    StockIDFunctions = {
        defaultSeparator  : '-',

        openGenerateStockIDModal: function (sourceId) {
            'use strict';
            $('#generateStockIDModal').modal({backdrop: 'static', keyboard: true});
            $('#stockIDModalSourceListID').val(sourceId);
        },

        saveStockList : function(listType) {
            var stockGenerationSettings = {
                // use the default identifier of SID when user provides no value
                breederIdentifier : $('#breederIdentifierField').val().trim() === '' ? 'SID': $('#breederIdentifierField').val().trim(),
                // default separator for now is -
                separator : StockIDFunctions.defaultSeparator
            };

            $.ajax(
                {
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    url: '/Fieldbook/stock/generateStockList/' + listType + '/' + $('#stockIDModalSourceListID').val(),
                    type: 'POST',
                    cache: false,
                    contentType : 'application/json',
                    data : JSON.stringify(stockGenerationSettings),
                    success: function (result) {
                        if (result.isSuccess === '1') {

                        } else {

                        }
                    }
                }
            );

        },

        generateStockListTabIfNecessary : function(listId) {
            'use strict';

            var url = '/Fieldbook/stock/generateStockTabIfNecessary/' + listId;

            $.ajax({
                url: url,
                type: 'GET',
                cache: false,
                success: function(html) {
                    if (html && html.length > 0) {
                        $('#advance-list' + listId + '-li').after(html);
                        $('#stock-list-anchor' + listId).on('shown.bs.tab', function() {
                            if ($(this).data('has-loaded') !== '1') {
                                $(this).data('has-loaded', '1');
                                StockIDFunctions.displayStockList($(this).data('list-id'));
                            }
                        });
                    }
                }
            });
        },

        displayStockList : function(listId) {
            'use strict';

            var url = '/Fieldbook/germplasm/list/stock/' + listId;

            $.ajax({
                url: url,
                type: 'GET',
                cache: false,
                success: function(html) {
                    $('#stock-list' + listId).html(html);
                    //we just show the button
                    $('.export-advance-list-action-button').removeClass('fbk-hide');
                    $('#stock-list' + listId+'-li').addClass('advance-germplasm-items');
                    $('#stock-list' + listId+'-li').data('advance-germplasm-list-id', listId);
                }
            });
        },

        closeStockList : function(listId) {
            'use strict';
            $('li#stock-list' + listId + '-li').remove();

            if($('#list'+listId).length === 1){
                $('#list' + listId).remove();
            }

            setTimeout(function() {
                $('#create-nursery-tab-headers li:eq(0) a').tab('show');
                $('.nav-tabs').tabdrop('layout');
            }, 100);
        },

        openSaveListModal: function () {
            'use strict';

            $('#generateStockIDModal').modal('hide');

            var germplasmTreeNode = $('#germplasmFolderTree').dynatree('getTree');
            $.ajax(
                {
                    url: '/Fieldbook/ListTreeManager/saveStockList/',
                    type: 'GET',
                    cache: false,
                    success: function (html) {
                        $('#saveListTreeModal').data('is-save-stock', '1');
                        $('#saveListTreeModal').data('sourceListId', $('#stockIDModalSourceListID').val());
                        $('#saveGermplasmRightSection').html(html);
                        setTimeout(function () {
                            $('#saveListTreeModal').modal({backdrop: 'static', keyboard: true});
                        }, 300);
                        //we preselect the program lists
                        if (germplasmTreeNode !== null && germplasmTreeNode.getNodeByKey('LISTS') !== null) {
                            germplasmTreeNode.getNodeByKey('LISTS').activate();
                            germplasmTreeNode.getNodeByKey('LISTS').expand();
                        }
                    }
                }
            );
        },

        updateLabels: function () {
            'use strict';
            StockIDFunctions.retrieveNextStockIDPrefix().done(function(nextPrefix) {
                $('#samplePrefixLabel').html(nextPrefix);
                $('#nextStockIDLabel').html(nextPrefix + StockIDFunctions.defaultSeparator + '1');
            });
        },

        retrieveNextStockIDPrefix: function () {
            'use strict';
            var stockGenerationSettings = {
                // use the default identifier of SID when user provides no value
                breederIdentifier : $('#breederIdentifierField').val().trim() === '' ? 'SID': $('#breederIdentifierField').val().trim(),
                // default separator for now is -
                separator : StockIDFunctions.defaultSeparator
            };

            var deferred = $.Deferred();

            $.ajax(
                {
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    url: '/Fieldbook/stock/retrieveNextStockPrefix',
                    type: 'POST',
                    cache: false,
                    contentType : 'application/json',
                    data : JSON.stringify(stockGenerationSettings),
                    success: function (result) {
                        if (result.isSuccess === '1') {
                            deferred.resolve(result.prefix);
                        } else {
                            deferred.reject(result);
                        }
                    }
                }
            );

            return deferred;
        }
    };
}