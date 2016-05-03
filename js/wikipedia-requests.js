function getPageContent(pageTitle, callback) {
	log('Getting page content of ' + pageTitle + '...');
	$.ajax({
		url: 'php/wikipedia-get-page.php',
		dataType: 'json',
		data: {
			title: pageTitle
		}
	}).done(function(response) {
		log('Content retrieved');
		callback(response);
	}).fail(function(response) {
		log('Error on retrieving page content from Wikipedia:\n', response);
	});
};

function postPageContent(pageTitle, pageContent, editToken, callback) {
	log('Posting page content of ' + pageTitle + '...');
	$.ajax({
		url: 'php/wikipedia-post-page.php',
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

function getEditToken(pageTitle, callback) {
        log('Getting edit token...');
        $.ajax({
                url: 'php/wikipedia-get-token.php',
		data: {
			title: pageTitle
		},
                dataType: 'json'
        }).done(function(response) {
                log('Token retrieved:\n' + JSON.stringify(response));
		var editToken = response.query.tokens.logintoken;
		log('Edit token: ' + editToken);
                callback(editToken);
        }).fail(function(response) {
                log('Error on getting edit token for Wikipedia:\n', JSON.stringify(response));
        });
};

function postLogin(userName, userPswd, callback) {
        log('Posting login...');
        $.ajax({
                url: 'php/wikipedia-post-login.php',
                data: {
                        name: userName,
                        password: userPswd
                }
        }).done(function(response) {
                log('Login posted:\n' + JSON.stringify(response));
                callback(response);
        }).fail(function(response) {
                log('Error on posting login:\n' + JSON.stringify(response));
        });
};
