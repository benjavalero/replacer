/** Global variables */

// Constant to define if debug is enabled
var DEBUG = false;

// Title of the current page with misspellings
var misspelledPageTitle;

// String containing the original content of the page but encoded
var encodedContent;

// Array with the possible misspellings of the page.
// A misspelling contains the properties (word, cs, suggestion).
var pageMisspellings;

// Array with the found misspelling matches of the page.
// A misspelling match contains the properties (word, position, suggestions, fix, fixed).
var pageMisspellingMatches;


$(document).ready(function() {
	// Move down the body acording to the header height
	var headerHeight = $('#cabecera').height();
	$('body').css('padding-top', headerHeight + 5);

	$('#button-save').click(function() {
		// Empty the article content
		$('#article-content').html('');
		$('#link-title').text('');
		$('#link-title').attr("href", '#');

		// Hide the Save button
		$('#ul-save').collapse('hide');

		var fixedContent = ReplaceUtils.replaceFixes(encodedContent, pageMisspellingMatches);
		var decodedFixed = StringUtils.decodeHtml(fixedContent);
		debug('Decoded Fixed:\n\n' + decodedFixed);
		debug('¿Hay cambios? ' + !(fixedContent == encodedContent));

		if (fixedContent == encodedContent) {
			info('No se han realizado cambios en «' + misspelledPageTitle + '»');
			DataBaseUtils.setPageMisspellingsAsFixed(misspelledPageTitle, function() {
				findAndLoadMisspelledPage();
			});
		} else {
			info('Guardando cambios en «' + misspelledPageTitle + '»…');
			WikipediaUtils.postPageContent(misspelledPageTitle, decodedFixed, function() {
				DataBaseUtils.setPageMisspellingsAsFixed(misspelledPageTitle, function() {
					findAndLoadMisspelledPage();
				});
			});
		}
	});

	if (isUserLogged()) {
		findAndLoadMisspelledPage();
	}

});


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


/** Find and load a misspelled page in the screen */
function findAndLoadMisspelledPage() {
	info('Buscando artículos con errores ortográficos…');
	DataBaseUtils.getMisspelledPage(function(title) {
		debug('Título de la página con errores: ' + title);
		misspelledPageTitle = title;
		loadMisspelledPage(misspelledPageTitle);
	});
}


/** Load a misspelled page in the screen */
function loadMisspelledPage(pageTitle) {
	info('Obteniendo contenido del artículo «' + pageTitle + '»…');
	WikipediaUtils.getPageContent(pageTitle, function(content) {
		debug('Contenido de la página:\n\n' + content);

		if (content) {
			encodedContent = StringUtils.encodeHtml(content);

			info('Buscando errores ortográficos en el artículo «' + pageTitle + '»…');
			DataBaseUtils.getPageMisspellings(pageTitle, function(misspellings) {
				debug('Misspellings: ' + JSON.stringify(misspellings));
				pageMisspellings = misspellings;

				pageMisspellingMatches = ReplaceUtils.findMisspellingMatches(encodedContent, misspellings);
				debug('Page Misspellings Matches: ' + JSON.stringify(pageMisspellingMatches));

				if (pageMisspellingMatches.length > 0) {
					var displayedContent = ReplaceUtils.replaceMisspellingsWithButtons(encodedContent, pageMisspellingMatches);
					displayedContent = ReplaceUtils.removeParagraphsWithoutMisspellings(displayedContent);
					displayedContent = ReplaceUtils.highlightSyntax(displayedContent);

					$('#link-title').text(pageTitle);
					$('#link-title').attr("href", 'https://es.wikipedia.org/wiki/' + pageTitle);

					updateDisplayedContent(displayedContent);
				} else {
					info('No se han encontrado errores en «' + pageTitle + '»');
					DataBaseUtils.setPageMisspellingsAsFixed(pageTitle, function() {
						findAndLoadMisspelledPage();
					});
				}
			});
		} else {
			DataBaseUtils.setPageMisspellingsAsFixed(pageTitle, function() {
				findAndLoadMisspelledPage();
			});
		}
	});
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


/** Display the content in the screen and register some events */
function updateDisplayedContent(content) {
	// Display page content
	$('#article-content').html(content);

	// Show Save button
	$('#ul-save').collapse('show');

	// Add event to the misspelling buttons
	$('.miss').click(function() {
		turnMisspelling(this.id);
	});

	// Add cool Bootstrap tooltips
	$(function() {
		$('[data-toggle="tooltip"]').tooltip()
	});
}