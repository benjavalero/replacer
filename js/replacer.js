/*** Global variables ***/

// Array with titles (strings) of pages with misspellings 
var misspelledPages;

// String containing the original content of the page
var rawContent;

// String cointaining the displayed content in the screen
var displayedContent;

// Array with the misspellings of the page.
// A misspelling contains the properties:
// * word
// * cs
// * suggestion
var misspellings;

// Array with the misspelling matches of the page.
// A misspelling match contains the properties:
// * word
// * position
// * fix
// * fixed (boolean)
var missMatches = new Array();

$(document).ready(function() {

	$('#button-commit').click(function() {
		postContent();
	});

if (isUserLogged()) {
	getMisspelledPages(function(response) {
		misspelledPages = response.titles;

// FIXME DEBUGGING
misspelledPages = new Array();
misspelledPages.push("Usuario:Benjavalero/Taller");

		var pageTitle = misspelledPages.pop();
		$('#pageTitle').val(pageTitle);
		getPageContent(pageTitle, function(response) {
			rawContent = response;
			displayedContent = response;
			$('#article-content').html(displayedContent);
		});

		getPageMisspellings(pageTitle, function(response) {
			misspellings = response.misspellings;

// FIXME DEBUGGING
misspellings = new Array();
var myMisspell = {word : 'herror', cs : 0, suggestion : 'error'};
misspellings.push(myMisspell);
myMisspell = {word : 'mnos', cs : 0, suggestion : 'menos'};
misspellings.push(myMisspell);
			highlightMisspellings();
		});

		// TODO Cargar más artículos cuando el array se vacíe
	});
}

/*
	$('#button-commit').click(function() {
		getEditToken(pageTitle, function(token) {
			log('Token from callback: ' + token);
			postPageContent(pageTitle, $('#article-content').text(), token, null);
		});
	});
*/

});


/*** UTILS ***/

function isUserLogged() {
	return $('#tokenKey').val().length > 0;
}

/*** STRING UTILS ***/

// <div>  =>  &lt;div&gt;
function encodeHtml(htmlText) {
        return jQuery('<div />').text(htmlText).html();
}

// &lt;div&gt;  =>  <div>
function decodeHtml(htmlText) {
        return jQuery('<div />').html(htmlText).text();
}

// If a character is lower-case
function isUpperCase(ch) {
        return ch == ch.toUpperCase();
}

// país  =>  país
function setFirstUpperCase(word) {
        return word[0].toUpperCase() + word.substr(1);
}

// replaceAt('0123456789', 3, '34', 'XXXX')  =>  '012XXXX56789'
function replaceAt(text, position, replaced, replacement) {
        return text.substr(0, position) + replacement + text.substr(position + replaced.length);
}

/*** ALERT UTILS ***/

function showAlert(message, type, closeDelay) {
	if ($('#alerts-container').length == 0) {
		// alerts-container does not exist, create it
		$('body').append($('<div id="alerts-container" style="position: fixed; width: 50%; left: 25%; top: 10%;">'));
	}

	// default to alert-info; other options include success, warning, danger
	type = type || "info";

	// create the alert div
	var alert = $('<div class="alert alert-' + type + ' fade in">')
		.append($('<button type="button" class="close" data-dismiss="alert">').append("&times;"))
		.append(message); 

	// add the alert div to top of alerts-container, use append() to add to bottom
	$("#alerts-container").prepend(alert);

	// if closeDelay was passed - set a timeout to close the alert
	if (closeDelay) {
		window.setTimeout(function() {alert.alert("close") }, closeDelay);
	}
}

function closeAlert() {
	$('.close').click();
}

/*** DATABASE REQUESTS ***/

/* Run query in DB to get a list of pages with misspellings */
function getMisspelledPages(callback) {
        showAlert('Buscando artículos con errores ortográficos...');
        $.ajax({
                url: 'php/db-select-replacement.php',
                dataType: 'json'
        }).done(function(response) {
                closeAlert();
                callback(response);
        }).fail(function(response) {
                showAlert('Error buscando artículos con errores ortográficos', 'danger', '');
        });
}

/* Run query in DB to get the misspellings of a page */
function getPageMisspellings(pageTitle, callback) {
        showAlert('Buscando errores ortográficos en el artículo: ' + pageTitle);
        $.ajax({
                url: 'php/db-select-misspellings.php',
                dataType: 'json',
                data: {
                        title : pageTitle
                }
        }).done(function(response) {
                closeAlert();
                callback(response);
        }).fail(function(response) {
                showAlert('Error buscando errores ortográficos en el artículo: ' + pageTitle, 'danger', '');
        });
}

/*** WIKIPEDIA REQUESTS ***/

/* Retrieve the content of a page from Wikipedia */
function getPageContent(pageTitle, callback) {
        console.log('Obteniendo contenido del artículo: ' + pageTitle);
        $.ajax({
                url:  'index.php',
                dataType: 'json',
                data: {
			action: 'get',
                        title: pageTitle.replace(' ', '_')
                }
        }).done(function(response) {
                console.log('Contenido obtenido del artículo: ' + pageTitle);

		var pages = response.query.pages;     
		// There is only one page  
		var content;                                                      
		for (var pageId in pages) {                                                                   
			var pageTitle = pages[pageId].title;                                                  
			content = pages[pageId].revisions[0]['*'];
		} 
		callback(encodeHtml(content));                                                     
        }).fail(function(response) {
                showAlert('Error obteniendo el contenido del artículo: ' + pageTitle, 'danger', '');
        });
}


function highlightMisspellings() {
	for (var miss of misspellings) {
		var isCaseSensitive = (miss.cs == 1);
		var flags = 'g';
		if (!isCaseSensitive) {
			flags += 'i';
		}
		var re = new RegExp('\\b(' + miss.word + ')\\b', flags);
		// En este momento, debería ser rawContent == displayedContent
		while ((reMatch = re.exec(rawContent)) != null) {
			// Apply case-insensitive fix if necessary
			var missFix = miss.suggestion;
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
			+ '" data-toggle="tooltip" data-placement="top" class="miss btn btn-danger" type="button">'
			+ missMatch.word + '</button>';
		displayedContent = replaceAt(displayedContent, missMatch.position, missMatch.word, replacement);
	}

	// Mostrar el contenido modificado
        $('#article-content').html(displayedContent);                                                          
        $('#button-commit').collapse('show');                                                         
                                                                                                      
        // Add event to the misspelling buttons                                                       
        $('.miss').click(function() {                                                                 
                turnMisspelling(this.id);                                                             
        });                                                                                           
                                             
	// Show cool Bootstrap tooltips                                                         
        $(function () {                                                                               
                $('[data-toggle="tooltip"]').tooltip()                                                
        });
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
	// FIXME Realmente enviar la página

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

/*
function highlightSyntax(content) {
	var reComment = new RegExp('(&lt;!--.+?--&gt;)', 'g');
	content = content.replace(reComment, '<span class="comment">$1</span>');

	var reLink = new RegExp('(\\[\\[.+?\\]\\])', 'g');
	content = content.replace(reLink, '<span class="link">$1</span>');

	var reHeader = new RegExp('(\\={2,}.+?\\={2,})', 'g');
	content = content.replace(reHeader, '<span class="header">$1</span>');

	return content;
}

*/
