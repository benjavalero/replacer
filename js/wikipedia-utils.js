var WikipediaUtils = {

	// baseUrl : 'https://tools.wmflabs.org/replacer/',
	apiUrl : 'https://es.wikipedia.org/w/api.php',

	/* Run query in Wikipedia API to get the contents of a page.
	 * Run the callback with the page contents. */
	getPageContent : function(pageTitle, callback) {
		// TODO Enable console
		// info('Obteniendo contenido del artículo «' + pageTitle + '»…');
		$.ajax({
			url : this.apiUrl,
			dataType : 'json',
			data : {
				action : 'query',
				titles : pageTitle.replace(' ', '_'),
				prop : 'revisions',
				rvprop : 'content',
				format : 'json'
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
			// debug('Page content: ' + content);
			callback(content);
		}).fail(function(response) {
			// closeAlert(msgId);
			// showAlert('Error buscando artículos con errores ortográficos. ' + JSON.stringify(response), 'danger');
		});
	}

};
