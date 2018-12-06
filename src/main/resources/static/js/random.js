var threshold = 200; // Number of characters to display between replacements
var originalArticle;

// Map to store the replacements. The key is the button ID.
var replacements = [];

document.addEventListener('DOMContentLoaded', function () {
    reqwest('isAuthenticated', function(authenticated) {
        if (!authenticated) {
            window.location.href = 'index.html';
        }
    });

    // Add click event to the misspelling buttons
    document.querySelector('#button-save').addEventListener('click', function () {
        saveChanges();
    });

    addMessage('Buscando artículo con reemplazos…', 'info', true);
    findRandomArticle();
});

/**
 * Get the URL parameters
 * source: https://css-tricks.com/snippets/javascript/get-url-variables/
 * @param  {String} url The URL
 * @return {Object}     The URL parameters
 */
var getParams = function (url) {
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

function addMessage(message, type, clear) {
    if (clear) {
        document.querySelector('#article').classList.add('hidden');
        document.querySelector('#button-save').classList.add('hidden');
        document.querySelector('#messages').textContent = '';
        document.querySelector('#messages').classList.remove('hidden');
    }

    var alertDiv = document.createElement('div');
    alertDiv.classList.add('alert', 'alert-' + type, 'text-center');
    alertDiv.setAttribute('role', 'alert');
    alertDiv.textContent = message;

    document.querySelector('#messages').appendChild(alertDiv);
}

function findRandomArticle() {
    var word = getParams(window.location.href).word;

    reqwest({
        url: 'article/random/' + (word || ''),
        type: 'json',
        success: function(response) {
            if (!response.content) {
                addMessage(response.title, 'danger');
            } else {
                loadArticle(response);
            }
        }
    });
}

function loadArticle(response) {
    document.querySelector('#messages').classList.add('hidden');
    document.querySelector('#article').classList.remove('hidden');
    document.querySelector('#button-save').classList.remove('hidden');

    document.querySelector('#title').textContent = response.title;
    document.querySelector('#link').setAttribute('href', 'https://es.wikipedia.org/wiki/' + response.title);

    // Insert replacements
    originalArticle = response;
    var articleContent = response.content;
    response.replacements.forEach(function(replacement) {
        if (replacement.type == 'MISSPELLING') {
            var button = document.createElement('button');
            button.textContent = replacement.text;
            button.setAttribute('id', 'repl-' + replacement.start);
            button.setAttribute('type', 'button');
            button.setAttribute('data-toggle', 'tooltip');
            button.setAttribute('title', replacement.comment);
            button.classList.add('btn', 'btn-danger', 'replacement');

            articleContent = articleContent.slice(0, replacement.start)
                + button.outerHTML
                + articleContent.slice(replacement.end);

            // Add the replacement to the map
            replacements[button.id] = replacement;
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

function htmlEscape(str) {
    var div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

function trimText(text) {
    var result = '';

    var re = new RegExp('<button.+?</button>', 'g');
    var lastEnd = 0;
    var m;
    while (m = re.exec(text)) {
        var matchText = m[0];
        var matchStart = m.index;
        var matchEnd = m.index + matchText.length;
        var textBefore = text.substring(lastEnd, matchStart);

        if (lastEnd == 0) {
            result += textBefore.length <= threshold ? textBefore : '...' + textBefore.substring(textBefore.length - threshold);
        } else {
            result += textBefore.length <= threshold * 2 ? textBefore
                        : textBefore.substring(0, threshold) + '... <hr> ...' + textBefore.substring(textBefore.length - threshold);
        }
        result += matchText;

        lastEnd = matchEnd;
    }

    // Append the rest after the last button
    var rest = text.substring(lastEnd);
    result += rest.length <= threshold ? rest : rest.substring(0, threshold) + '...';

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
            textToSave = textToSave.slice(0, replacement.start)
                + document.querySelector('#repl-' + replacement.start).textContent
                + textToSave.slice(replacement.end);
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
                findRandomArticle();
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
                findRandomArticle();
            }
        });
    }
}