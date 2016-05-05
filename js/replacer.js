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
		$('#article-content').html(displayedContent);
	}
}

function highlightMisspellings(content) {
	var count = 1;
	for (var [miss, fix] of misspellingsMap) {
		var re = new RegExp('\\b(' + miss + ')\\b', 'gi'); // Global + Case Insensitive
		while ((reMatch = re.exec(content)) != null) {
			// Apply case-insensitive fix if necessary
			var missFix = fix[0];
			if (!fix[1] && isUpperCase(reMatch[0][0])) {
				missFix = setFirstUpperCase(missFix);
			}
			var missMatch = {word : reMatch[0], position : reMatch.index, fix : missFix, fixed: false};
			missMatches.push(missMatch);
		}
	}

	// Recorro inversamente el array de matches y sustituyo por inputs
	for (var idx = missMatches.length - 1; idx >= 0; idx--) {
		var missMatch = missMatches[idx];
		var replacement = '<input id="miss-' + idx + '" type="button" '
			+ 'title="' + missMatch.fix + '" value="' + missMatch.word + '" class="miss" '
			+ 'onclick="turnMisspelling(' + idx + ')" />';
		content = replaceAt(content, missMatch.position, missMatch.word, replacement);
	}

	return content;
}

function turnMisspelling(missId) {
	var missMatch = missMatches[missId];
	if (missMatch.fixed) {
		$('#miss-' + missId).removeClass('fix');
		$('#miss-' + missId).addClass('miss');
		$('#miss-' + missId).val(missMatch.word);
		missMatch.fixed = false;
	} else {
		$('#miss-' + missId).removeClass('miss');
		$('#miss-' + missId).addClass('fix');
		$('#miss-' + missId).val(missMatch.fix);
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
