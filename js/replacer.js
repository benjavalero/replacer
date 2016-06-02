/*** Global variables ***/

// Constant to define if debug is enabled
var DEBUG = true;

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
var missMatches;

// Exceptions where misspellings will be ignored
var excpRegex = new Array();
excpRegex.push(new RegExp('\\|[\\wáéíóúÁÉÍÓÍÚüÜñÑ\\s]+\\=', 'g')); // Template Param
excpRegex.push(new RegExp('\\|\\s*índice\\s*=.*?[\\}\\|]', 'g')); // Index value
excpRegex.push(new RegExp('\\{\\{(?:ORDENAR:|DEFAULTSORT:|NF\\|)[^\\}]*', 'g')); // Unreplaceable template
excpRegex.push(new RegExp('\\{\\{[^\\|\\}]+', 'g')); // Template name
excpRegex.push(new RegExp('\\{\\{(?:[Cc]ita|c?Quote)\\|[^\\}]*', 'g')); // Quote
excpRegex.push(new RegExp("'{1,5}.+?'{1,5}", 'g')); // Quotes
excpRegex.push(new RegExp('«[^»]+»', 'g')); // Angular Quotes
excpRegex.push(new RegExp('“[^”]+”', 'g')); // Typographic Quotes
excpRegex.push(new RegExp('"[^"]+"', 'g')); // Double Quotes
excpRegex.push(new RegExp('[\\=\\|:][^\\=\\|:]+\\.(?:svg|jpe?g|png|gif|ogg|pdf)', 'g')); // File Name
excpRegex.push(new RegExp('<ref[^>]*>', 'g')); // Ref Name
excpRegex.push(new RegExp('\\[\\[Categoría:.*?\\]\\]', 'g')); // Category
excpRegex.push(new RegExp('<!--.*?-->', 'g')); // Comment
excpRegex.push(new RegExp('https?://[\\w\\./\\-\\?&%=:]+', 'g')); // URL

// Ignore some misspellings with most false positives
excpRegex.push(new RegExp('[Ss]ólo|[Ii]ndex|[Ll]ink|[Rr]eferences?', 'g'));

$(document).ready(function() {

	// Muestra el texto final con los reemplazos hechos
	$('#button-show-changes').click(function() {
		showChanges(true);
	});

	$('#button-save').click(function() {
		// Realiza los reemplazos
		if (showChanges(false)) {
			postPageContent(
				$('#pageTitle').text(),
				$('#content-to-post').text(),
				function(response) {
					fixPageMisspellings($('#pageTitle').text());
					loadMisspelledPage();
				}
			);
		} else {
			showAlert('No se han realizado cambios en el artículo', 'info', 3000);
			loadMisspelledPage();
		}
	});

	if (isUserLogged()) {
		findMisspelledPages();
	}
});

function findMisspelledPages() {
	getMisspelledPages(function(response) {
		misspelledPages = response.titles;
		debug('MisspelledPages: ' + misspelledPages.toString());

		loadMisspelledPage();
	});
}

// Carga el siguiente artículo con errores
function loadMisspelledPage() {
	$('#content-to-post').collapse('hide');

	if (misspelledPages.length == 0) {
		findMisspelledPages();
	} else {
		var pageTitle = misspelledPages.pop();
		$('#pageTitle').text(pageTitle);

		getPageContent(pageTitle, function(response) {
			rawContent = response;
			displayedContent = rawContent;
			$('#article-content').html(displayedContent);

			getPageMisspellings(pageTitle, function(response) {
				misspellings = response.misspellings;
				debug('Misspellings: ' + JSON.stringify(misspellings));

				highlightMisspellings();
				hideCorrectParagraphs();
				highlightSyntax();
			});
		});
	}
}


/*** UTILS ***/

function isUserLogged() {
	return $('#tokenKey').val().length > 0;
}

function debug(message) {
	if (DEBUG) {
		console.log(message);
	}
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

// país  =>  País
function setFirstUpperCase(word) {
	return word[0].toUpperCase() + word.substr(1);
}

function getRegexWordIgnoreCase(word) {
	var ch = word[0];
	return '[' + ch.toUpperCase() + ch.toLowerCase() + ']' + word.substring(1);
}

// replaceAt('0123456789', 3, '34', 'XXXX')  =>  '012XXXX56789'
function replaceAt(text, position, replaced, replacement) {
	return text.substr(0, position) + replacement + text.substr(position + replaced.length);
}

/*** ALERT UTILS ***/

// Muestra una alerta y devuelve su ID por si la queremos cerrar manualmente
var alertId = 0;
function showAlert(message, type, closeDelay) {
	if ($('#alerts-container').length == 0) {
		// alerts-container does not exist, create it
		$('body').append($('<div id="alerts-container" style="position: fixed; width: 50%; left: 25%; top: 70px;">'));
	}

	// default to alert-info; other options include success, warning, danger
	type = type || "info";

	// create the alert div
	var msgId = alertId++;
	var alert = $('<div id="alert-' + msgId + '" class="alert alert-' + type + ' fade in">')
		.append($('<button type="button" class="close close-' + type + '" data-dismiss="alert">').append("&times;"))
		.append(message); 

	// add the alert div to top of alerts-container, use append() to add to bottom
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

/*** DATABASE REQUESTS ***/

/* Run query in DB to get a list of pages with misspellings */
function getMisspelledPages(callback) {
	var msgId = showAlert('Buscando artículos con errores ortográficos...');
	$.ajax({
		url: 'php/db-select-replacement.php',
		dataType: 'json'
	}).done(function(response) {
		closeAlert(msgId);
		callback(response);
	}).fail(function(response) {
		closeAlert(msgId);
		showAlert('Error buscando artículos con errores ortográficos. ' + JSON.stringify(response), 'danger');
	});
}

/* Run query in DB to get the misspellings of a page */
function getPageMisspellings(pageTitle, callback) {
	var msgId = showAlert('Buscando errores ortográficos en el artículo: ' + pageTitle);
	$.ajax({
		url: 'php/db-select-misspellings.php',
		dataType: 'json',
		data: {
			title : pageTitle
		}
	}).done(function(response) {
		closeAlert(msgId);
		callback(response);
	}).fail(function(response) {
		closeAlert(msgId);
		showAlert('Error buscando errores ortográficos en el artículo: ' + pageTitle + '. ' + JSON.stringify(response), 'danger');
	});
}

/* Run query in DB to mark as fixed the misspellings of a page */
function fixPageMisspellings(pageTitle) {
	$.ajax({
		url: 'php/db-update-replacement.php',
		dataType: 'json',
		data: {
			title : pageTitle
		}
	}).done(function(response) {
		debug('dtfixed actualizada: ' + JSON.stringify(response));
	}).fail(function(response) {
		showAlert('Error marcando como corregidos los errores ortográficos en el artículo: ' + pageTitle + '. ' + JSON.stringify(response), 'danger');
	});
} 

/*** WIKIPEDIA REQUESTS ***/

/* Retrieve the content of a page from Wikipedia */
function getPageContent(pageTitle, callback) {
	var msgId = showAlert('Obteniendo contenido del artículo: ' + pageTitle);
	$.ajax({
		url: 'index.php',
		dataType: 'json',
		data: {
			action: 'get',
			title: pageTitle.replace(' ', '_')
		}
	}).done(function(response) {
		var pages = response.query.pages;
		// There is only one page
		var content;
		for (var pageId in pages) {
			var pageTitle = pages[pageId].title;
			content = pages[pageId].revisions[0]['*'];
		}
		closeAlert(msgId);
		callback(encodeHtml(content));
	}).fail(function(response) {
		closeAlert(msgId);
		showAlert('Error obteniendo el contenido del artículo: ' + pageTitle + '. ' + JSON.stringify(response), 'danger');
	});
}

function postPageContent(pageTitle, pageContent, callback) {
	var msgId = showAlert('Guardando cambios del artículo: ' + pageTitle);
	$.ajax({
		url: 'index.php',
		method: 'POST',
		data: {
			action: 'edit',
			title: pageTitle,
			text: pageContent
		}
	}).done(function(response) {
		closeAlert(msgId);
		showAlert('Contenido guardado', 'success', 3000);
		callback(response);
	}).fail(function(response) {
		closeAlert(msgId);
		showAlert('Error guardando los cambios en: ' + pageTitle + '. ' + JSON.stringify(response), 'danger');
	});
};


/*** REPLACEMENT FUNCTIONS ***/

function highlightMisspellings() {
	// Busco las excepciones
	var exceptions = new Array();
	for (var excpRe of excpRegex) {
		while ((excpMatch = excpRe.exec(rawContent)) != null) {
			var posIni = excpMatch.index;
			var text = excpMatch[0];
			var posFin = posIni + text.length;
			exceptions.push({ini: posIni, fin: posFin});
		}
	}

	missMatches = new Array();
	for (var miss of misspellings) {
		var isCaseSensitive = (miss.cs == 1);
		var flags = 'g';
		var word = miss.word;
		if (!isCaseSensitive) {
			word = getRegexWordIgnoreCase(word);
		}
		var re = new RegExp('\\b(' + word + ')\\b', flags);

		// En este momento, debería ser rawContent == displayedContent
		if (rawContent != displayedContent) {
			debug('ERROR: los contenidos iniciales no coinciden');
		}
		while ((reMatch = re.exec(rawContent)) != null) {
			// Apply case-insensitive fix if necessary
			var suggestions = miss.suggestion.split(' ');
			var missFix = suggestions[0];
			if (!isCaseSensitive && isUpperCase(reMatch[0][0])) {
				missFix = setFirstUpperCase(missFix);
			}

			// Compruebo que no esté en ninguna excepción
			var inException = false;
			for (var exception of exceptions) {
				if (exception.ini <= reMatch.index && reMatch.index <= exception.fin) {
					inException = true;
				}
			}

			if (!inException) {
				var missMatch = {word : reMatch[0], position : reMatch.index, fix : missFix, fixed: false};
				missMatches.push(missMatch);
			}
		}
	}
	debug('Miss Matches: ' + JSON.stringify(missMatches));

	if (missMatches.length == 0) {
		showAlert('No se han encontrado errores. Cargando siguiente artículo...', 'info', 3000);
		loadMisspelledPage();
	} else {
		// Ordeno el array de errores por posición e inversamente
		missMatches.sort(function(a, b) {
			return b.position - a.position;
		});

		// Recorro el array de matches y sustituyo por inputs
		for (var idx = 0; idx < missMatches.length; idx++) {
			var missMatch = missMatches[idx];
			var replacement = '<button id="miss-' + idx + '" title="' + missMatch.fix
				+ '" data-toggle="tooltip" data-placement="top" class="miss btn btn-danger" type="button">'
				+ missMatch.word + '</button>';
			displayedContent = replaceAt(displayedContent, missMatch.position, missMatch.word, replacement);
		}

		updateDisplayedContent(displayedContent);
	}
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


// Realiza los reemplazos
// Si "show = true", muestra el texto final.
// Devuelve un booleano indicando si hay cambios
function showChanges(show) {
	var numFixes = 0;

	// Recorro inversamente el array de matches y sustituyo por los fixes si procede
	var fixedRawContent = rawContent;
	for (var idx = 0; idx < missMatches.length; idx++) {
		var missMatch = missMatches[idx];
		if (missMatch.fixed) {
			fixedRawContent = replaceAt(fixedRawContent, missMatch.position, missMatch.word, missMatch.fix);
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

function hideCorrectParagraphs() {
	var reNewLines = new RegExp('\\n{2,}', 'g');
	var positions = new Array();
	var content = displayedContent;

	// Añado el inicio del documento como posición
	positions.push(0);

	while ((reMatch = reNewLines.exec(content)) != null) {
		positions.push(reMatch.index);
	}

	// Añado el final del documento como posición
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

	// Quito los saltos de línea repetidos
	reducedContent = reducedContent.replace(reNewLines, '');

	updateDisplayedContent(reducedContent);
}

function highlightSyntax() {
	var content = displayedContent;

	var reComment = new RegExp('(&lt;!--.+?--&gt;)', 'g');
	content = content.replace(reComment, '<span class="syntax comment">$1</span>');

	var reLink = new RegExp('(\\[\\[.+?\\]\\])', 'g');
	content = content.replace(reLink, '<span class="syntax link">$1</span>');

	var reHeader = new RegExp('(\\={2,}.+?\\={2,})', 'g');
	content = content.replace(reHeader, '<span class="syntax header">$1</span>');

	var reHyperlink = new RegExp('(https?://[\\w\\./\\-\\?&%=:]+)', 'g');
	content = content.replace(reHyperlink, '<span class="syntax hyperlink">$1</span>');

	updateDisplayedContent(content);
}

function updateDisplayedContent(content) {
	// Mostrar el contenido modificado
	displayedContent = content;
	$('#article-content').html(content);
	$('#button-show-changes').collapse('show');

	// Add event to the misspelling buttons
	$('.miss').click(function() {
		turnMisspelling(this.id);
	});

	// Show cool Bootstrap tooltips
	$(function () {
		$('[data-toggle="tooltip"]').tooltip()
	});
}
