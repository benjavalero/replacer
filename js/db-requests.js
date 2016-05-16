function getMisspelledPages(callback) {
	log('Getting pages with misspellings...');
	$.ajax({
		url:  baseUrl + 'php/db-select-replacement.php',
		dataType: 'json',
		data: {
		}
	}).done(function(response) {
		log('Misspelled pages retrieved');
		callback(response);
	}).fail(function(response) {
		log('Error on retrieving misspelled pages content:\n', response);
	});
};

function getPageMisspellings(pageTitle, callback) {
	log('Getting misspellings of: ' + pageTitle + '...');
	$.ajax({
		url:  baseUrl + 'php/db-select-misspellings.php',
		dataType: 'json',
		data: {
			title : pageTitle
		}
	}).done(function(response) {
		log('Page misspellings retrieved');
		callback(response);
	}).fail(function(response) {
		log('Error on retrieving page misspellings:\n', response);
	});
}

/*
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
*/
