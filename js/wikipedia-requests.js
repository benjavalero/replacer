function getPageContent(pageTitle, callback) {
	$.ajax({
		url: 'php/wikipedia-get-page.php',
		dataType: 'json',
		data: {
			title: pageTitle
		}
	}).done(function(response) {
		callback(response);
	}).fail(function(response) {
		alert('Error on retrieving page content from Wikipedia');
	});
};

function postPageContent(pageTitle, pageContent, callback) {
	$.ajax({
		url: 'php/wikipedia-post-page.php',
		data: {
			action: 'edit',
			title: pageTitle,
			text: pageContent
		}
	}).done(function(response) {
		callback(response);
	}).fail(function(data) {
		alert('Error on updating page content in Wikipedia');
	});
};

