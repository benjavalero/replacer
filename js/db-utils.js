var DataBaseUtils = {

	baseUrl : 'https://tools.wmflabs.org/replacer/',

	/* Run query in DB to get the title of a page with misspellings.
	 * Run the callback with the page title string. */
	getMisspelledPage : function(callback) {
		// TODO Get a random page
		// TODO Enable console
		// info('Buscando artículos con errores ortográficos…');
		$.ajax({
			url : this.baseUrl + 'php/db-select-replacement.php',
			dataType : 'json'
		}).done(function(response) {
			// debug('Page with misspellings: ' + JSON.stringify(response.titles));
			callback(response.titles[0]);
		}).fail(function(response) {
			// closeAlert(msgId);
			// showAlert('Error buscando artículos con errores ortográficos. ' + JSON.stringify(response), 'danger');
		});
	},

	/* Run query in DB to get the misspellings of a page.
	 * Returns a JSON array with objects (word, cs, suggestion).
	 * Run the callback with the JSON array. */
	getPageMisspellings : function(pageTitle, callback) {
		// TODO Enable console
		// info('Buscando errores ortográficos en el artículo «' + pageTitle + '»…');
		$.ajax({
			url : this.baseUrl + 'php/db-select-misspellings.php',
			dataType : 'json',
			data : {
				title : pageTitle
			}
		}).done(function(response) {
			// debug('Misspellings: ' + JSON.stringify(response.misspellings));
			alert('Misspellings: ' + JSON.stringify(response.misspellings));
			// callback(response.misspellings);
		}).fail(function(response) {
			// closeAlert(msgId);
			// showAlert('Error buscando errores ortográficos en el artículo: ' + pageTitle + '. ' + JSON.stringify(response), 'danger');
		});
	}

};