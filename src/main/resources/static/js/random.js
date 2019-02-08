var THRESHOLD = 200; // Number of characters to display between replacements
var WIKIPEDIA_BASE_URL = 'https://es.wikipedia.org/wiki/';
var BUTTON_REGEX = new RegExp('<button.+?</button>', 'g');
var REPLACEMENT_PREFIX = 'repl-';
var originalArticle;

// Map to store the replacements. The key is the button ID.
var replacements = [];

document.addEventListener('DOMContentLoaded', function () {
    // Add click event to the misspelling buttons
    document.querySelector('#button-save').addEventListener('click', function () {
        saveChanges();
    });

    addMessage('Buscando artículo con reemplazos…', 'info', true);
    findAndDisplayRandomArticle();
});

function addMessage(message, type, clear) {
    if (clear) {
        document.querySelector('#article').hidden = true;
        document.querySelector('#button-save').hidden = true;
        document.querySelector('#messages').textContent = '';
        document.querySelector('#messages').hidden = false;
    }

    var alertDiv = document.createElement('div');
    alertDiv.classList.add('alert', 'alert-' + type, 'text-center');
    alertDiv.setAttribute('role', 'alert');
    alertDiv.textContent = message;

    document.querySelector('#messages').appendChild(alertDiv);
}

function findAndDisplayRandomArticle() {
    var word = getParams(window.location.href).word;
    findRandomArticle(word, function(article) {
        displayArticle(article);
    });
}

/**
 * Get the URL parameters
 * source: https://css-tricks.com/snippets/javascript/get-url-variables/
 * @param  {String} url The URL
 * @return {Object}     The URL parameters
 */
function getParams(url) {
	var params = {};
	var parser = document.createElement('a');
	parser.href = url;
	var query = parser.search.substring(1);
	var vars = query.split('&');
	for (var i = 0; i < vars.length; i++) {
		var pair = vars[i].split('=');
		params[pair[0]] = decodeURIComponent(pair[1]);
	}
	return params;
};

function findRandomArticle(word, callback) {
    reqwest({
        url: 'article/random/' + (word || ''),
        type: 'json',
        success: function(response) {
            if (!response.content) {
                addMessage(response.title, 'danger');
            } else {
                callback(response);
            }
        }
    });
}

function displayArticle(response) {
    // Hide messages and display article title
    document.querySelector('#messages').hidden = true;
    document.querySelector('#article').hidden = false;
    document.querySelector('#button-save').hidden = false;

    document.querySelector('#title').textContent = response.title;
    document.querySelector('#link').setAttribute('href', WIKIPEDIA_BASE_URL + response.title);

    // Keep original article text to apply the replacements later
    originalArticle = response;

    // Create the replacement buttons and insert them in the text
    var articleContent = response.content;
    response.replacements.forEach(function(replacement) {
        if (replacement.type == 'MISSPELLING') {
            var replacementButton = createReplacementButton(replacement);
            articleContent = replaceText(articleContent, replacement.start, replacement.text, replacementButton.outerHTML);

            // Add the replacement to the map
            replacements[replacementButton.id] = replacement;
        }
    });

    // We could insert the text with "textContent" but we cannot add the buttons later
    // We need to encode the text and insert it with "innerHTML" instead
    articleContent = htmlEscape(articleContent);

    // So we "decode" only the buttons
    articleContent = articleContent.replace(/&lt;button(.+?)&gt;/g, '<button$1>');
    articleContent = articleContent.replace(/&lt;\/button&gt;/g, '</button>');

    // "Trim" the parts with no replacements
    if (response.trimText) {
        articleContent = trimText(articleContent);
    }

    document.querySelector('#content').innerHTML = articleContent;

    // Add click event to the misspelling buttons
    document.querySelectorAll('.replacement').forEach(function(button) {
        button.addEventListener('click', function () {
            toggleReplacement(this.id);
        });
    });

    // Add cool Bootstrap tooltips
    $(function() {
        $('[data-toggle="tooltip"]').tooltip()
    });
}

function createReplacementButton(replacement) {
    var button = document.createElement('button');
    button.textContent = replacement.text;
    button.setAttribute('id', REPLACEMENT_PREFIX + replacement.start);
    button.setAttribute('type', 'button');
    button.setAttribute('data-toggle', 'tooltip');
    button.setAttribute('title', replacement.comment);
    button.classList.add('btn', 'btn-danger', 'replacement');
    return button;
}

function replaceText(fullText, position, currentText, newText) {
    return fullText.slice(0, position)
        + newText
        + fullText.slice(position + currentText.length);
}

function htmlEscape(str) {
    var div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

function trimText(text) {
    var result = '';

    var lastEnd = 0;
    var m;
    while (m = BUTTON_REGEX.exec(text)) {
        var matchText = m[0];
        var matchStart = m.index;
        var matchEnd = m.index + matchText.length;
        var textBefore = text.substring(lastEnd, matchStart);

        if (lastEnd == 0) {
            result += textBefore.length <= THRESHOLD ? textBefore : '...' + textBefore.substring(textBefore.length - THRESHOLD);
        } else {
            result += textBefore.length <= THRESHOLD * 2 ? textBefore
                        : textBefore.substring(0, THRESHOLD) + '... <hr> ...' + textBefore.substring(textBefore.length - THRESHOLD);
        }
        result += matchText;

        lastEnd = matchEnd;
    }

    // Append the rest after the last button
    var rest = text.substring(lastEnd);
    result += rest.length <= THRESHOLD ? rest : rest.substring(0, THRESHOLD) + '...';

    return result;
}

function toggleReplacement(id) {
    var replacement = replacements[id];
    var button = document.querySelector('#' + id);
    if (button.classList.contains('btn-danger')) {
        button.classList.remove('btn-danger');
        button.classList.add('btn-success');
        button.textContent = replacement.suggestion;
    } else {
        button.classList.remove('btn-success');
        button.classList.add('btn-danger');
        button.textContent = replacement.text;
    }
}

function saveChanges() {
    // Take the original text and replace the replacements
    var textToSave = originalArticle.content;
    originalArticle.replacements.forEach(function(replacement) {
        if (replacement.type == 'MISSPELLING') {
            textToSave = replaceText(textToSave, replacement.start, replacement.text,
                document.querySelector('#' + REPLACEMENT_PREFIX + replacement.start).textContent);
        }
    });

    addMessage('Guardando cambios en «' + originalArticle.title + '»…', 'info', true);

    if (textToSave === originalArticle.content) {
        reqwest({
            url: 'article/save/nochanges',
            method: 'post',
            data: { title: originalArticle.title },
            success: function (resp) {
                addMessage('Artículo «' + originalArticle.title + '» marcado como revisado', 'success');
                addMessage('Buscando artículo con reemplazos…', 'info');
                findAndDisplayRandomArticle();
            }
        });
    } else {
        reqwest({
            url: 'article/save',
            method: 'post',
            data: { title: originalArticle.title, text: textToSave },
            success: function (resp) {
                addMessage('Artículo «' + originalArticle.title + '» guardado', 'success');
                addMessage('Buscando artículo con reemplazos…', 'info');
                findAndDisplayRandomArticle();
            }
        });
    }
}