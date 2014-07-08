$(document).ready(function() {
	var hasMeasurements = $('#study' + getCurrentStudyIdInTab() + ' #has-measurements').val() === 'true',
		hasFieldmap = false;
	$('#study'+getCurrentStudyIdInTab() + " " + '.toggle-icon').click(function(){
		var expandedImg = $(this).find(".section-expanded");
		var collapsedImg = $(this).find(".section-collapsed");
		expandedImg.toggle();
		collapsedImg.toggle();
	});
	$('#study'+getCurrentStudyIdInTab() + " " + '.section-toggle').on('click', function(){
		var className = $(this).data('target')
		$('#study'+getCurrentStudyIdInTab() + " "+className).slideToggle();
	})

	$("#div-study-tab-" + getCurrentStudyIdInTab() + " #open-study-url-link").click(function() {
		selectedTableIds[0] = $("#div-study-tab-" + getCurrentStudyIdInTab() + " #studyId").val();
	});

	$("#div-study-tab-" + getCurrentStudyIdInTab() + " #advance-study-url-link").click(function() {
		selectedTableIds[0] = $("#div-study-tab-" + getCurrentStudyIdInTab() + " #studyId").val();
	});

	$("#div-study-tab-" + getCurrentStudyIdInTab() + " #div-study-tab-" + getCurrentStudyIdInTab() + " #fieldmap-url-link").click(function() {
		selectedTableIds[0] = $("#div-study-tab-" + getCurrentStudyIdInTab() + " #studyId").val();
	});

	$("#div-study-tab-" + getCurrentStudyIdInTab() + " #label-printing-url-link").click(function() {
		selectedTableIds[0] = $("#div-study-tab-" + getCurrentStudyIdInTab() + " #studyId").val();
	});

	$("#div-study-tab-" + getCurrentStudyIdInTab() + " #view-fieldmap-url-link").click(function() {
		selectedTableIds[0] = $("#div-study-tab-" + getCurrentStudyIdInTab() + " #studyId").val();
	});

	$("#div-study-tab-" + getCurrentStudyIdInTab() + " #toggle-additional-link").click(function() {
		if ($(this).text() == showText) {
			$(this).text(hideText);
		}
		else {
			$(this).text(showText);
		}
	});

	$("#div-study-tab-"+getCurrentStudyIdInTab()+" .dataset-toggle").click(function() {
		loadDatasetDropdown($(this).parent().find("#dataset-selection"));
	});

	$("#div-study-tab-"+getCurrentStudyIdInTab()+" #dataset-selection").change(function() {
		if ($(this).val()) {
			var id = $(this).val();
			var name = $(this).find("option:selected").text();
			var selectedId = $(this).find('option:selected').val();
			var found = false;
			if($(this).val() == 'Please Choose')
				return;

			$('#study' + getCurrentStudyIdInTab() + ' #measurement-tabs').children().hide();
			$('#study' + getCurrentStudyIdInTab() + ' #measurement-tab-headers').find('li').each(function(index) {
				$(this).removeClass("active");

				if ($(this).prop('id').replace('dataset-li', '') === selectedId) {

					$(this).addClass("active");
					$("#dset-tab-" + id).show();
					found = true;
				}
			});

			if (!found) {
				loadDatasetMeasurementRowsViewOnly(id, name);
			}
		}
	});

	$("#div-study-tab-" + getCurrentStudyIdInTab() + " .nurseryDetails").find(".control-label").each(function (index) {
		if ($(this).text() == 'Field Map Created') {
			if ($(this).parent().parent().find(".div-select-val").text() == 'Yes') {
				hasFieldmap = true;
			}
		}
	});
	
	if (!hasMeasurements) {
		$("#div-study-tab-" + getCurrentStudyIdInTab() + " #advance-study-url-link").hide();
		$("#div-study-tab-" + getCurrentStudyIdInTab() + " #fieldmap-url-link").hide();
		$("#div-study-tab-" + getCurrentStudyIdInTab() + " #label-printing-url-link").hide();
		$("#div-study-tab-" + getCurrentStudyIdInTab() + " #export-study-link").hide();
	}

	if (hasFieldmap) {
		$("#div-study-tab-" + getCurrentStudyIdInTab() + " #fieldmap-url-link").hide();
	}
	truncateStudyVariableNames('.fbk-variable', 10);
	checkTraitsAndSelectionVariateTable('#study'+getCurrentStudyIdInTab(), true);
});