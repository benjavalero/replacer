var DataBaseUtils = {

	baseUrl : 'https://tools.wmflabs.org/replacer/',

	/* Run query in DB to get the title of a page with misspellings.
	 * Run the callback with the page title string. */
	getMisspelledPage : function(callback) {
		$.ajax({
			url : this.baseUrl + 'php/db-select-replacement.php',
			dataType : 'json'
		}).done(function(response) {
			callback(response.titles[0]);
		}).fail(function(response) {
			alert('Error buscando artículos con errores ortográficos. ' + JSON.stringify(response));
		});
	},

	/* Run query in DB to get the misspellings of a page.
	 * Returns a JSON array with objects (word, cs, suggestion).
	 * Run the callback with the JSON array. */
	getPageMisspellings : function(pageTitle, callback) {
		$.ajax({
			url : this.baseUrl + 'php/db-select-misspellings.php',
			dataType : 'json',
			data : {
				title : pageTitle
			}
		}).done(function(response) {
			callback(response.misspellings);
		}).fail(function(response) {
			alert('Error buscando errores ortográficos en el artículo: ' + pageTitle + '. ' + JSON.stringify(response));
		});
	},

	/** Run query in DB to mark as fixed the misspellings of a page */
	setPageMisspellingsAsFixed : function(pageTitle, callback) {
		$.ajax({
			url : 'php/db-update-replacement.php',
			dataType : 'json',
			data : {
				title : pageTitle
			}
		}).done(function(response) {
			callback();
		}).fail(function(response) {
			alert('Error marcando como corregidos los errores ortográficos en el artículo: ' + pageTitle + '. ' + JSON.stringify(response));
		});
	}

};