/** Global variables */

// Constant to define if debug is enabled
var DEBUG = false;

// Array with titles (strings) of pages with misspellings
var misspelledPageTitles = [];

// String containing the original content of the page
var rawContent;

// Array with the possible misspellings of the page.
// A misspelling contains the properties:
// * word
// * cs
// * suggestion
var pageMisspellings;

// Array with the found misspelling matches of the page.
// A misspelling match contains the properties:
// * word
// * position
// * fix
// * fixed (boolean)
var pageMisspellingMatches;

/** REGEX */

// \w doesn't include accentuated characters
var WORD_CHARACTER_CLASS = '\\wÁáÉéÍíÓóÚúÜüÑñ';

// Exceptions where misspellings will be ignored
// JavaScript doesn't support regex lookbehind
// JavaScript doesn't support dotall flag. Workaround: . => [\\S\\s]
var exceptionRegExps = [];

// Template Param
exceptionRegExps.push(new RegExp(
		'\\|[' + WORD_CHARACTER_CLASS + '\\s]+(?=\\=)', 'g'));

// Index value
exceptionRegExps.push(new RegExp('\\|\\s*índice\\s*=[\\S\\s]*?[\\}\\|]', 'g'));

// Unreplaceable template
exceptionRegExps.push(new RegExp(
		'\\{\\{(?:ORDENAR:|DEFAULTSORT:|NF\\|)[^\\}]*', 'g'));

// Template name
exceptionRegExps.push(new RegExp('\\{\\{[^\\|\\}]+', 'g'));

// Quote
exceptionRegExps.push(new RegExp('\\{\\{(?:[Cc]ita|c?Quote)\\|[^\\}]*', 'g'));

// Quotes
exceptionRegExps.push(new RegExp("'{2,5}.+?'{2,5}", 'g'));

// Angular Quotes
exceptionRegExps.push(new RegExp('«[^»]+»', 'g'));

// Typographic Quotes
exceptionRegExps.push(new RegExp('“[^”]+”', 'g'));

// Double Quotes
exceptionRegExps.push(new RegExp('"[^"]+"', 'g'));

// File Name
exceptionRegExps.push(new RegExp(
		'[\\=\\|:][^\\=\\|:]+\\.(?:svg|jpe?g|JPG|png|PNG|gif|ogg|pdf)', 'g'));

// Ref Name
exceptionRegExps.push(new RegExp('<ref[^>]*>', 'g'));

// Category
exceptionRegExps.push(new RegExp('\\[\\[Categoría:.*?\\]\\]', 'g'));

// Comment
exceptionRegExps.push(new RegExp('<!--[\\S\\s]*?-->', 'g'));

// URL
var reHyperlink = new RegExp('(https?://[\\w\\./\\-\\+\\?&%=:#;]+)', 'g');
exceptionRegExps.push(reHyperlink);

// Ignore some misspellings with most false positives
exceptionRegExps.push(new RegExp(
		'[Ss]ólo|[Ii]ndex|[Ll]ink|[Oo]nline|[Rr]eferences?|Jean|[Aa]un así', 'g'));

$(document).ready(function() {

	// Muestra el texto final con los reemplazos hechos
	$('#button-show-changes').click(function() {
		showChanges(true);
	});

	$('#button-save').click(function() {
		// Empty the article content
		$('#article-content').html('');

		// Hide the Save button
		$('#button-save').collapse('hide');

		// Realiza los reemplazos
		if (showChanges(false)) {
			postPageContent(
					$('#pageTitle').text(),
					$('#content-to-post').text(),
					function() {
						setPageMisspellingsAsFixed(
								$('#pageTitle').text(),
								function() {
									findAndLoadMisspelledPage();
								});
					});
		} else {
			info('No se han realizado cambios en «' + $('#pageTitle').text() + '»');
			setPageMisspellingsAsFixed($('#pageTitle').text(), function() {
				findAndLoadMisspelledPage();
			});
		}
	});

	if (isUserLogged()) {
		findAndLoadMisspelledPage();
	}
});

/** Find and load a misspelled page in the screen */
function findAndLoadMisspelledPage() {
	// Hide the content of the previous page
	$('#content-to-post').collapse('hide');

	// If there are titles available, load one of them.
	// If not, find new titles before.
	if (misspelledPageTitles.length > 0) {
		loadMisspelledPage(misspelledPageTitles.pop());
	} else {
		getMisspelledPages(function(response) {
			misspelledPageTitles = response.titles;
			debug('MisspelledPages: ' + JSON.stringify(misspelledPageTitles));

			findAndLoadMisspelledPage();
		});
	}
}

/** Load a misspelled page in the screen */
function loadMisspelledPage(pageTitle) {
	$('#pageTitle').text(pageTitle);

	getPageContent(pageTitle, function(response) {
		rawContent = response;

		getPageMisspellings(pageTitle, function(response) {
			pageMisspellings = response.misspellings;
			debug('Misspellings: ' + JSON.stringify(pageMisspellings));

			var displayedContent = highlightMisspellings(rawContent);
			// If there are no misspellings, result will be null
			if (displayedContent) {
				displayedContent = hideParagraphsWithoutMisspellings(displayedContent);
				displayedContent = highlightSyntax(displayedContent);
				updateDisplayedContent(displayedContent);
			} else {
				info('No se han encontrado errores en «' + pageTitle + '»');
				setPageMisspellingsAsFixed(pageTitle, function() {
					findAndLoadMisspelledPage();
				});
			}
		});
	});
}

/** UTILS */

function isUserLogged() {
	return $('#tokenKey').val().length > 0;
}

function info(message) {
	$('#article-content').append('\n' + message);
}

function debug(message) {
	if (DEBUG) {
		console.log(message);
	}
}

/** STRING UTILS */

// <div> => &lt;div&gt;
function encodeHtml(htmlText) {
	return jQuery('<div />').text(htmlText).html();
}

// &lt;div&gt; => <div>
function decodeHtml(htmlText) {
	return jQuery('<div />').html(htmlText).text();
}

// If a character is lower-case
function isUpperCase(ch) {
	return ch === ch.toUpperCase();
}

// país => País
function setFirstUpperCase(word) {
	return word[0].toUpperCase() + word.substr(1);
}

function getRegexWordIgnoreCase(word) {
	var ch = word[0];
	return '[' + ch.toUpperCase() + ch.toLowerCase() + ']' + word.substring(1);
}

// replaceAt('0123456789', 3, '34', 'XXXX') => '012XXXX56789'
function replaceAt(text, position, replaced, replacement) {
	return text.substr(0, position) + replacement + text.substr(position + replaced.length);
}

/** ALERT UTILS */

// Muestra una alerta y devuelve su ID por si la queremos cerrar manualmente
var alertId = 0;
function showAlert(message, type, closeDelay) {
	if ($('#alerts-container').length === 0) {
		// alerts-container does not exist, create it
		$('body').append($('<div id="alerts-container" style="position: fixed; width: 50%; left: 25%; top: 70px;">'));
	}

	// default to alert-info; other options include success, warning, danger
	type = type || "info";

	// create the alert div
	var msgId = alertId++;
	var alert = $('<div id="alert-' + msgId + '" class="alert alert-' + type + ' fade in">')
		.append($('<button type="button" class="close close-' + type + '" data-dismiss="alert">')
		.append("&times;"))
		.append(message);

	// add alert div to top of alerts-container, use append() to add to bottom
	$("#alerts-container").append(alert);

	// if closeDelay was passed - set a timeout to close the alert
	if (closeDelay) {
		setTimeout(function() {
			$('#alert-' + msgId).alert("close")
		}, closeDelay);
	}

	return 'alert-' + msgId;
}

function closeAlert(msgId) {
	$('#' + msgId).alert('close');
}

/** DATABASE REQUESTS */

/** Run query in DB to get a list of pages with misspellings */
function getMisspelledPages(callback) {
	info('Buscando artículos con errores ortográficos…');
	$.ajax({
		url : 'php/db-select-replacement.php',
		dataType : 'json'
	}).done(function(response) {
		callback(response);
	}).fail(function(response) {
		closeAlert(msgId);
		showAlert('Error buscando artículos con errores ortográficos. ' + JSON.stringify(response), 'danger');
	});
}

/** Run query in DB to get the misspellings of a page */
function getPageMisspellings(pageTitle, callback) {
	info('Buscando errores ortográficos en el artículo «' + pageTitle + '»…');
	$.ajax({
		url : 'php/db-select-misspellings.php',
		dataType : 'json',
		data : {
			title : pageTitle
		}
	}).done(function(response) {
		callback(response);
	}).fail(function(response) {
		closeAlert(msgId);
		showAlert('Error buscando errores ortográficos en el artículo: ' + pageTitle + '. ' + JSON.stringify(response), 'danger');
	});
}

/** Run query in DB to mark as fixed the misspellings of a page */
function setPageMisspellingsAsFixed(pageTitle, callback) {
	info('Marcando «' + pageTitle + '» como revisado…');
	$.ajax({
		url : 'php/db-update-replacement.php',
		dataType : 'json',
		data : {
			title : pageTitle
		}
	}).done(function(response) {
		debug('dtfixed actualizada: ' + JSON.stringify(response));
		callback();
	}).fail(function(response) {
		showAlert('Error marcando como corregidos los errores ortográficos en el artículo: '
				+ pageTitle + '. ' + JSON.stringify(response), 'danger');
	});
}

/** WIKIPEDIA REQUESTS */

/* Retrieve the content of a page from Wikipedia */
function getPageContent(pageTitle, callback) {
	info('Obteniendo contenido del artículo «' + pageTitle + '»…');
	$.ajax({
		url : 'index.php',
		dataType : 'json',
		data : {
			action : 'get',
			title : pageTitle.replace(' ', '_')
		}
	}).done(function(response) {
		// FIXME Se pueden perder las credenciales. La respuesta es como esta:
		// {"servedby":"mw1142","error":{"code":"mwoauth-invalid-authorization","info":"The authorization headers
		// in your request are not valid: No approved grant was found for that authorization token.","*":"See https
		// ://es.wikipedia.org/w/api.php for API usage"}}
		var pages = response.query.pages;
		// There is only one page
		var content;
		for (var pageId in pages) {
			var pageTitle = pages[pageId].title;
			if (pages[pageId].revisions) {
				content = pages[pageId].revisions[0]['*'];
			}
		}
		if (content) {
			callback(encodeHtml(content));
		} else {
			setPageMisspellingsAsFixed(pageTitle, function() {
				findAndLoadMisspelledPage();
			});
		}
	}).fail(function(response) {
		showAlert('Error obteniendo el contenido del artículo: ' + pageTitle + '. ' + JSON.stringify(response), 'danger');
	});
}

function postPageContent(pageTitle, pageContent, callback) {
	info('Guardando cambios en «' + pageTitle + '»…');
	$.ajax({
		url : 'index.php',
		method : 'POST',
		data : {
			action : 'edit',
			title : pageTitle,
			text : pageContent
		}
	}).done(function(response) {
		info('Contenido guardado');
		callback(response);
	}).fail(function(response) {
		closeAlert(msgId);
		showAlert('Error guardando los cambios en: ' + pageTitle + '. ' + JSON.stringify(response), 'danger');
	});
};

/** REPLACEMENT FUNCTIONS */

/**
 * Find misspellings in the content and highlight them. Returns the highlighted
 * content or NULL if there are no misspellings.
 */
function highlightMisspellings(content) {
	info('Buscando errores ortográficos en el contenido del artículo…');

	/* 1. Find the exception matches */
	var pageExceptions = [];
	for (var i = 0; i < exceptionRegExps.length; i++) {
		var exceptionRegExp = exceptionRegExps[i];
		while ((exceptionMatch = exceptionRegExp.exec(content)) != null) {
			var startPosition = exceptionMatch.index;
			var matchingText = exceptionMatch[0];
			var endPosition = startPosition + matchingText.length;
			pageExceptions.push({
				ini : startPosition,
				fin : endPosition,
				text : matchingText
			});
		}
	}
	debug('Exceptions: ' + JSON.stringify(pageExceptions));

	/* 2. Find the misspelling matches. Ignore the ones in exceptions. */
	pageMisspellingMatches = [];

	for (var j = 0; j < pageMisspellings.length; j++) {
		var pageMisspelling = pageMisspellings[j];

		// Build the misspelling regex
		var isCaseSensitive = (pageMisspelling.cs === '1');
		var flags = 'g';
		var word = pageMisspelling.word;
		if (!isCaseSensitive) {
			word = getRegexWordIgnoreCase(word);
		}
		var re = new RegExp('[^' + WORD_CHARACTER_CLASS + '](' + word + ')[^'
				+ WORD_CHARACTER_CLASS + ']', flags);

		while ((reMatch = re.exec(rawContent)) != null) {
			// WARN: The regex captures the characters before and after the word
			// TODO Improve the handling of fix and suggestion
			var suggestions = pageMisspelling.suggestion.split(' ');
			var misspellingFix = suggestions[0];
			var matchWord = reMatch[1];
			var matchIndex = reMatch.index + 1;
			// Apply case-insensitive fix if necessary
			if (!isCaseSensitive && isUpperCase(matchWord[0])) {
				misspellingFix = setFirstUpperCase(misspellingFix);
			}

			// Only treat misspellings not in exception
			var inException = false;
			for (var k = 0; k < pageExceptions.length; k++) {
				var exception = pageExceptions[k];
				if ((exception.ini <= matchIndex)
						&& (matchIndex <= exception.fin)) {
					inException = true;
				}
			}
			if (!inException) {
				var missMatch = {
					word : matchWord,
					position : matchIndex,
					fix : misspellingFix,
					fixed : false
				};
				pageMisspellingMatches.push(missMatch);
			}
		}
	}
	debug('Miss Matches: ' + JSON.stringify(pageMisspellingMatches));

	var replacedContent = content;
	if (pageMisspellingMatches.length > 0) {
		// Sort the matches array inversely by position
		pageMisspellingMatches.sort(function(a, b) {
			return b.position - a.position;
		});

		// Loop through the matches array and replace by inputs
		for (var idx = 0; idx < pageMisspellingMatches.length; idx++) {
			var misspellingMatch = pageMisspellingMatches[idx];
			var replacement = '<button id="miss-' + idx + '" title="' + misspellingMatch.fix
					+ '" data-toggle="tooltip" data-placement="top" class="miss btn btn-danger" type="button">'
					+ misspellingMatch.word + '</button>';
			replacedContent = replaceAt(replacedContent,
					misspellingMatch.position, misspellingMatch.word,
					replacement);
		}
	} else{
		return null;
	}

	return replacedContent;
}

function turnMisspelling(missId) {
	var idx = missId.split('-')[1];
	var missMatch = pageMisspellingMatches[idx];
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

/**
 * Replace the original content with the accepted fixes.
 * 
 * @param show
 *            If true, show the replaced content in the screen.
 * @return A boolean if there have been changes.
 */
function showChanges(show) {
	var numFixes = 0;

	// Loop through inversely the misspeling matches array and replace by the fixes
	var fixedRawContent = rawContent;
	for (var idx = 0; idx < pageMisspellingMatches.length; idx++) {
		var missMatch = pageMisspellingMatches[idx];
		if (missMatch.fixed) {
			fixedRawContent = replaceAt(fixedRawContent, missMatch.position,
					missMatch.word, missMatch.fix);
			numFixes++;
		}
	}

	var decodedContent = decodeHtml(fixedRawContent);
	$('#content-to-post').text(decodedContent);
	if (show) {
		$('#content-to-post').collapse('show');
	}

	return (numFixes > 0);
}

/** Removes from the content the paragraphs without misspelings */
function hideParagraphsWithoutMisspellings(content) {
	var reNewLines = new RegExp('\\n{2,}', 'g');
	var positions = [];

	// Add the document start as a position
	positions.push(0);

	while ((reMatch = reNewLines.exec(content)) != null) {
		positions.push(reMatch.index);
	}

	// Add the document end as a position
	positions.push(content.length);

	var reducedContent = '';
	for (var idx = 1; idx < positions.length; idx++) {
		var ini = positions[idx - 1];
		var fin = positions[idx];
		var paragraph = content.substring(ini, fin);
		if (paragraph.indexOf('miss-') >= 0) {
			reducedContent += paragraph + '\n<hr>\n';
		}
	}

	// Remove the repeated new lines
	reducedContent = reducedContent.replace(reNewLines, '');

	return reducedContent;
}

/** Add highlight to some places in the content */
function highlightSyntax(content) {
	var replacedContent = content;

	var reComment = new RegExp('(&lt;!--.+?--&gt;)', 'g');
	replacedContent = replacedContent.replace(reComment,
			'<span class="syntax comment">$1</span>');

	var reLink = new RegExp('(\\[\\[.+?\\]\\])', 'g');
	replacedContent = replacedContent.replace(reLink,
			'<span class="syntax link">$1</span>');

	var reHeader = new RegExp('(\\={2,}.+?\\={2,})', 'g');
	replacedContent = replacedContent.replace(reHeader,
			'<span class="syntax header">$1</span>');

	replacedContent = replacedContent.replace(reHyperlink,
			'<span class="syntax hyperlink">$1</span>');

	return replacedContent;
}

/** Display the content in the screen and register some events */
function updateDisplayedContent(content) {
	// Display page content
	$('#article-content').html(content);

	// Show Save button
	$('#button-save').collapse('show');

	if (DEBUG) {
		$('#button-show-changes').collapse('show');
	}

	// Add event to the misspelling buttons
	$('.miss').click(function() {
		turnMisspelling(this.id);
	});

	// Add cool Bootstrap tooltips
	$(function() {
		$('[data-toggle="tooltip"]').tooltip()
	});
}
