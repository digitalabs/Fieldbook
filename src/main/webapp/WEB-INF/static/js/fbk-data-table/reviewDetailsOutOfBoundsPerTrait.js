jQuery().ready(function() {
		'use strict';
		$('#selectAction').select2({
			minimumResultsForSearch: -1
		}).on('change', function(e) {
			if (e.val !== '2') {
				$('#selectActionValue').val('');
			}
		});
		$('#checkReviewDetailsSelectAll').on('change', function() {
			var isChecked = $('#checkReviewDetailsSelectAll').prop('checked');
			var cells = $('#review-details-table').DataTable().cells().nodes();
			$(cells).find(':checkbox').prop('checked', $(this).is(':checked'));
		});
		$('#traitDetailLink').on('click', function(e) {

			showBaselineTraitDetailsModal($('#traitTermId').val());
			e.preventDefault();
			e.stopPropagation();
		});
		$('#previousTraitLink').on('click', function(e) {

			saveForm('previous');
			e.preventDefault();
			e.stopPropagation();
		});
		$('#nextTraitLink').on('click', function(e) {

			saveForm('next');
			e.preventDefault();
			e.stopPropagation();
		});
	});

	function saveForm(navigateAction) {
		'use strict';

		if (validateReviewDetailsOutOfBoundsDataForm()) {
			saveFormDataToSessionStorage($('#traitTermId').val());
			navigateDetailsOutOfBoundsData(navigateAction);
		}

	}

	function navigateDetailsOutOfBoundsData(action) {
		'use strict';
		if ($('#reviewDetailsOutOfBoundsDataModalBody').length !== 0) {
			$.ajax({
				url: '/Fieldbook/Common/ReviewDetailsOutOfBounds/showDetails/' + action,
				type: 'POST',
				data: $('#reviewDetailsOutOfBoundsForm').serialize(),
				success: function(html) {
					$('#reviewDetailsOutOfBoundsDataModalBody').html(html);
				}
			});
		}
	}
