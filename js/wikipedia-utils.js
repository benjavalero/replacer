var WikipediaUtils = {

	baseUrl : 'https://tools.wmflabs.org/replacer/',
	apiUrl : 'https://es.wikipedia.org/w/api.php',

	/* Run query in Wikipedia API to get the contents of a page.
	 * Run the callback with the page contents. */
	getPageContent : function(pageTitle, callback) {
		$.ajax({
			url : this.baseUrl + 'index.php',
			dataType : 'json',
			data : {
				action : 'get',
				titles : pageTitle.replace(' ', '_')
			}
		}).done(function(response) {
			var pages = response.query.pages;
			// There is only one page
			var content;
			for (var pageId in pages) {
				if (pages[pageId].revisions) {
					content = pages[pageId].revisions[0]['*'];
				}
			}

			callback(content);
		}).fail(function(response) {
			alert('Error buscando artículos con errores ortográficos. ' + JSON.stringify(response));
		});
	},

	postPageContent : function(pageTitle, pageContent, callback) {
		$.ajax({
			url : this.baseUrl + 'index.php',
			method : 'POST',
			data : {
				action : 'edit',
				title : pageTitle,
				text : pageContent
			}
		}).done(function(response) {
			callback();
		}).fail(function(response) {
			alert('Error guardando los cambios en: ' + pageTitle + '. ' + JSON.stringify(response));
		});
	}

};
