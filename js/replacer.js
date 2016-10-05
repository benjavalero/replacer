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
// * suggestions
// * fix
// * fixed (boolean)
var pageMisspellingMatches;

$(document).ready(function() {

	// Muestra el texto final con los reemplazos hechos
	$('#button-show-changes').click(function() {
		showChanges(true);
	});

	$('#button-save').click(function() {
		// Empty the article content
		$('#article-content').html('');
		$('#link-title').text('');

		// Hide the Save button
		$('#ul-save').collapse('hide');

		// Realiza los reemplazos
		if (showChanges(false)) {
			postPageContent(
					$('#page-title').val(),
					$('#content-to-post').text(),
					function() {
						setPageMisspellingsAsFixed(
								$('#page-title').val(),
								function() {
									findAndLoadMisspelledPage();
								});
					});
		} else {
			info('No se han realizado cambios en «' + $('#page-title').val() + '»');
			setPageMisspellingsAsFixed($('#page-title').val(), function() {
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
		DataBaseUtils.getMisspelledPage(function(response) {
			misspelledPageTitles = response;

			findAndLoadMisspelledPage();
		});
	}
}

/** Load a misspelled page in the screen */
function loadMisspelledPage(pageTitle) {
	getPageContent(pageTitle, function(response) {
		rawContent = response;

		DataBaseUtils.getPageMisspellings(pageTitle, function(response) {
			pageMisspellings = response;

			var displayedContent = highlightMisspellings(rawContent);
			// If there are no misspellings, result will be null
			if (displayedContent) {
				displayedContent = ReplaceUtils.removeParagraphsWithoutMisspellings(displayedContent);
				displayedContent = ReplaceUtils.highlightSyntax(displayedContent);

				$('#page-title').val(pageTitle);
				$('#link-title').text(pageTitle);
				$('#link-title').attr("href", 'https://es.wikipedia.org/wiki/' + pageTitle);
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
			callback(StringUtils.encodeHtml(content));
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

	/* 1. Find the misspelling matches. Ignore the ones in exceptions. */
	var pageMisspellingMatches = StringUtils.findMisspellingMatches(rawContent, pageMisspellings);
	debug('Miss Matches: ' + JSON.stringify(pageMisspellingMatches));

	return replaceMisspellingsWithButtons(content, pageMisspellingMatches);
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
    var fixedRawContent = ReplaceUtils.replaceFixes(rawContent, pageMisspellingMatches);
	var decodedContent = StringUtils.decodeHtml(fixedRawContent);
	$('#content-to-post').text(decodedContent);
	if (show) {
		$('#content-to-post').collapse('show');
	}

	return (fixedRawContent == rawContent);
}

/** Display the content in the screen and register some events */
function updateDisplayedContent(content) {
	// Display page content
	$('#article-content').html(content);

	// Show Save button
	$('#ul-save').collapse('show');

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
