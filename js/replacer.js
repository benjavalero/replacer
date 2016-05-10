var baseUrl = 'https://tools.wmflabs.org/replacer/'; // PROD: ./

var rawContent;
var displayedContent;

// Key: misspelling
// Value: [fix, case-sensitive]
var misspellingsMap = new Map();
misspellingsMap.set('pais', ['país', false]);
misspellingsMap.set('Maria', ['María', true]);

var missMatches = new Array();

$(document).ready(function() {
	$('#button-get').click(function() {
		getPageContent($('#pageTitle').val(), parseContentJson);
	});

	$('#button-commit').click(function() {
		postContent();
	});

/*
	//postLogin('benjavalero', 'min2979');

	$('#button-commit').click(function() {
		getEditToken(pageTitle, function(token) {
			log('Token from callback: ' + token);
			postPageContent(pageTitle, $('#article-content').text(), token, null);
		});
	});
*/
});

function log(logLine) {
	$('#debug-logs').text($('#debug-logs').text() + logLine + '\n');
};

function parseContentJson(response) {
	var pages = response.query.pages;
	for (var pageId in pages) {
		var content = pages[pageId].revisions[0]['*'];
		rawContent = encodeHtml(content);
		displayedContent = rawContent;

		displayedContent = highlightMisspellings(displayedContent);
		setDisplayedContent(displayedContent);

		displayedContent = highlightSyntax(displayedContent);
		setDisplayedContent(displayedContent);
	}
}

function highlightMisspellings(content) {
	var count = 1;
	for (var [miss, fix] of misspellingsMap) {
		var isCaseSensitive = fix[1];
		var flags = 'g';
		if (!isCaseSensitive) {
			flags += 'i';
		}
		var re = new RegExp('\\b(' + miss + ')\\b', flags);
		while ((reMatch = re.exec(content)) != null) {
			// Apply case-insensitive fix if necessary
			var missFix = fix[0];
			if (!isCaseSensitive && isUpperCase(reMatch[0][0])) {
				missFix = setFirstUpperCase(missFix);
			}
			var missMatch = {word : reMatch[0], position : reMatch.index, fix : missFix, fixed: false};
			missMatches.push(missMatch);
		}
	}

	// Recorro inversamente el array de matches y sustituyo por inputs
	for (var idx = missMatches.length - 1; idx >= 0; idx--) {
		var missMatch = missMatches[idx];
		var replacement = '<button id="miss-' + idx + '" title="' + missMatch.fix
			+ '" class="miss btn btn-danger" type="button">' + missMatch.word + '</button>';
		content = replaceAt(content, missMatch.position, missMatch.word, replacement);
	}

	return content;
}

function turnMisspelling(missId) {
	var idx = missId.split('-')[1];
	var missMatch = missMatches[idx];
	if (missMatch.fixed) {
		$('#' + missId).removeClass('btn-success');
		$('#' + missId).addClass('btn-danger');
		$('#' + missId).html(missMatch.word);
		missMatch.fixed = false;
	} else {
		$('#' + missId).removeClass('btn-danger');
		$('#' + missId).addClass('btn-success');
		$('#' + missId).html(missMatch.fix);
		missMatch.fixed = true;
	}
}

function postContent() {
	// Recorro inversamente el array de matches y sustituyo por los fixes si procede
	var fixedRawContent = rawContent;
	for (var idx = missMatches.length - 1; idx >= 0; idx--) {
		var missMatch = missMatches[idx];
		if (missMatch.fixed) {
			fixedRawContent = replaceAt(fixedRawContent, missMatch.position, missMatch.word, missMatch.fix);
		}
	}

	var decodedContent = decodeHtml(fixedRawContent);
	$('#content-to-post').text(decodedContent);
}

function highlightSyntax(content) {
	var reComment = new RegExp('(&lt;!--.+?--&gt;)', 'g');
	content = content.replace(reComment, '<span class="comment">$1</span>');

	var reLink = new RegExp('(\\[\\[.+?\\]\\])', 'g');
	content = content.replace(reLink, '<span class="link">$1</span>');

	var reHeader = new RegExp('(\\={2,}.+?\\={2,})', 'g');
	content = content.replace(reHeader, '<span class="header">$1</span>');

	return content;
}

function setDisplayedContent(content) {
	$('#article-content').html(content);

	// Add event to the misspelling buttons
	$('.miss').click(function() {
		turnMisspelling(this.id);
	});
}
