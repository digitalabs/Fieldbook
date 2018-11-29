
function initializeInlineMeasurementInput(possibleValuesSuggestions) {	
	'use strict';
	$('input.data-value').on('keydown', function(ev) {
		if (ev.which === 13) {
			processInlineEditInput();
		}
	});
	$('.inline-input input').on('click', function() {
		$('body').data('last-td-time-clicked', new Date().getTime());
	});
	$('body').on('click', function() {
		var lastTdClicked = $('body').data('last-td-time-clicked') !== null ? $('body').data('last-td-time-clicked') : 0;
		var timeDiff = new Date().getTime() - lastTdClicked;
		if (timeDiff > 20) {
			processInlineEditInput();
		}

	});
	$('.inline-input .numeric-value').on('change', function() {
		processInlineEditInput();
	});
	$('.inline-input .variates-select').each(function() {
		
		var possibleValues_obj = [];
		var defaultJsonVal = null;
		var defaultCValue = $(this).data('cvalueid') == null ? '' :  $(this).data('cvalueid');
		var defaultValue = $(this).data('value') == null ? '' :  $(this).data('value');
		defaultValue = defaultCValue !== '' ? defaultCValue : defaultValue;
		possibleValues_obj.push({'id': '', 'text': 'Please Choose', 'status': '0'});
		if ($('.inline-input').parent('td').hasClass('accepted-value') || $('.inline-input').parent('td').hasClass('invalid-value') || defaultValue === 'missing') {
			//meaning there is an out of bound value here, we should include it in the drop down
			var customVal = {'id': defaultValue, 'text': defaultValue.toString(), 'status': '1'};
			possibleValues_obj.push(customVal);
			defaultJsonVal = customVal;
		}
		$.each(
				possibleValuesSuggestions,
					function(index, value) {
						var jsonVal;
						if (value.id !== undefined) {
							// show only name (code) if categoricalDescriptionView is false else show both key and description
							var descriptionText = window.isCategoricalDescriptionView ? value.displayDescription : value.name;

							jsonVal = {
								'id': value.key,
								'text': descriptionText,
								'status': '0'
							};
						}

						possibleValues_obj.push(jsonVal);
						if (defaultValue !== null
								&& defaultValue !== ''
								&& defaultValue == value.key) {
							defaultJsonVal = jsonVal;
						}

					});

		$(this).select2({
			query:function(query) {

				var data = {
						results: possibleValues_obj
					};
					// return the array that matches
				data.results = $.grep(data.results, function(item,
						index) {
					return ($.fn.select2.defaults.matcher(query.term,
							item.text));

				});
				query.callback(data);
			},
			createSearchChoice: function(term, data) {
				if ($(data).filter(function() { return this.text.localeCompare(term) === 0; }).length === 0) {
					return {id:term, text:term, status:'1'};
				}
			}
		});
		if (defaultJsonVal !== null) {
			$(this).select2('data', defaultJsonVal);
		}
	}).on('change', function() {
		processInlineEditInput();
	});
	if ($('.inline-input .date-input').length > 0) {
		$('.inline-input .date-input').each(function() {
			$(this).datepicker({
				'format' : 'yyyymmdd'
			}).on('changeDate', function() {
				$(this).datepicker('hide');
				$('body').data('last-td-time-clicked', new Date().getTime());
			});
		});
	}
	if ($('.inline-input .datepicker img').length > 0) {
		$('.inline-input  .datepicker img').on('click', function() {
			$(this).parent().parent().find('.date-input').datepicker('show');
		});
	}
}
