function postPageContent(pageTitle, pageContent, editToken, callback) {
	log('Posting page content of ' + pageTitle + '...');
	$.ajax({
		url: baseUrl + 'php/wikipedia-post-page.php',
		data: {
			title: pageTitle,
			text: pageContent,
			token: editToken
		}
	}).done(function(response) {
		log('Content posted:\n' + JSON.stringify(response));
		callback(response);
	}).fail(function(response) {
		log('Error on updating page content in Wikipedia:\n' + JSON.stringify(response));
	});
};
