var ReplaceUtils = {

	/** Add highlight to some places in the text */
	highlightSyntax : function(text) {
		var replacedText = text;

		replacedText = replacedText.replace(RegEx.reCommentEncoded,
				'<span class="syntax comment">$1</span>');

		replacedText = replacedText.replace(RegEx.reHeader,
				'<span class="syntax header">$1</span>');

		replacedText = replacedText.replace(RegEx.reHyperlink,
				'<span class="syntax hyperlink">$1</span>');

		replacedText = replacedText
				.replace(
						RegEx.reLink,
						'<a href="https://es.wikipedia.org/wiki/$2" class="syntax link" target="_blank">$1</a>');

		return replacedText;
	},

	/* Exceptions where misspellings will be ignored */
	exceptionRegExps : [ RegEx.reTemplateParam, RegEx.reIndexValue,
			RegEx.reUnreplaceableTemplate, RegEx.reTemplateName, RegEx.reQuote,
			RegEx.reQuotes, RegEx.reAngularQuotes, RegEx.reTypographicQuotes,
			RegEx.reDoubleQuotes, RegEx.reFileName, RegEx.reRefName,
			RegEx.reCategory, RegEx.reComment, RegEx.reHyperlink,
			RegEx.reFalsePositives ],

	/* Returns an array with matches in the text of the replacement exceptions.
	 * The objects in the array have the properties (ini, fin, text). */
	findExceptionMatches : function(text) {
		var textExceptions = [];
		for (var i = 0; i < this.exceptionRegExps.length; i++) {
			var exceptionRegExp = this.exceptionRegExps[i];
			var exceptionMatch;
			while ((exceptionMatch = exceptionRegExp.exec(text)) != null) {
				var startPosition = exceptionMatch.index;
				var matchingText = exceptionMatch[0];
				var endPosition = startPosition + matchingText.length;
				textExceptions.push({
					ini : startPosition,
					fin : endPosition,
					text : matchingText
				});
			}
		}
		return textExceptions;
	},

	/* Returns an array with matches in the text of the given misspellings.
	 * The misspellings are objects with (word, cs, suggestion).
	 * The objects in the array have the properties (word, position, suggestions, fix, fixed).
	 * The misspelling in the text exceptions are omitted. */
	findMisspellingMatches : function(text, misspellings) {
		var misspellingMatches = [];
		var textExceptions = this.findExceptionMatches(text);

		for (var j = 0; j < misspellings.length; j++) {
			var misspelling = misspellings[j];

			// Build the misspelling regex
			var isCaseSensitive = (misspelling.cs == '1');
			var word = misspelling.word;
			if (!isCaseSensitive) {
				word = StringUtils.getRegexWordIgnoreCase(word);
			}
			var re = new RegExp('[^' + RegEx.wordCharacterClass + '](' + word
					+ ')[^' + RegEx.wordCharacterClass + ']', 'g');

			var reMatch;
			while ((reMatch = re.exec(text)) != null) {
				var matchWord = reMatch[1];
				// WARN: The regex captures the characters before and after the word
				// WARN: If the misspelling position is 0, then it is not found.
				var matchIndex = reMatch.index + 1;
				// Apply case-insensitive fix if necessary
				// TODO Improve the handling of fix and suggestion
				// For the moment we take as a fix the first word in the suggestion text
				var misspellingFix = misspelling.suggestion.split(/[\s,]/)[0];
				if (!isCaseSensitive && StringUtils.isUpperCase(matchWord[0])) {
					misspellingFix = StringUtils.setFirstUpperCase(misspellingFix);
				}

				// Only treat misspellings not in exception
				var inException = false;
				for (var k = 0; k < textExceptions.length; k++) {
					var exception = textExceptions[k];
					if ((exception.ini <= matchIndex) && (matchIndex <= exception.fin)) {
						inException = true;
					}
				}
				if (!inException) {
					var missMatch = {
						word : matchWord,
						position : matchIndex,
						suggestions : misspelling.suggestion,
						fix : misspellingFix,
						fixed : false
					};
					misspellingMatches.push(missMatch);
				}
			}
		}
		return misspellingMatches;
	},

	/* Replaces the given misspelling matches in the text with buttons.
	 * Returns the replaced text, or NULL if there has been no replacement. */
	replaceMisspellingsWithButtons : function(text, misspellingMatches) {
		var replacedContent = null;
		if (misspellingMatches.length > 0) {
			replacedContent = text;

			// Sort the matches array inversely by position
			misspellingMatches.sort(function(a, b) {
				return b.position - a.position;
			});

			// Loop through the matches array and replace by inputs
			for (var idx = 0; idx < misspellingMatches.length; idx++) {
				var misspellingMatch = misspellingMatches[idx];
				var replacement = '<button id="miss-' + idx + '"' + ' title="'
						+ misspellingMatch.suggestions + '"'
						+ ' data-toggle="tooltip" data-placement="top"'
						+ ' class="miss btn btn-danger" type="button">'
						+ misspellingMatch.word + '</button>';
				replacedContent = StringUtils.replaceAt(replacedContent,
						misspellingMatch.position, misspellingMatch.word,
						replacement);
			}
		}

		return replacedContent;
	},

	trimText : function(text) {
		var reButton = new RegExp('<button[\\S\\s]+?</button>', 'g');
		var matches = [];
		var reMatch;
		while ((reMatch = reButton.exec(text)) != null) {
			matches.push({
				pos : reMatch.index,
				text : reMatch[0]
			});
		}

		var reducedContent = '';
		var lastFin = 0;
		for (var idx = 0; idx < matches.length; idx++) {
			var ini = matches[idx].pos;
			var fin = ini + matches[idx].text.length;
			var buttonText = text.substring(ini, fin);
			var textBefore = text.substring(lastFin, ini);
			lastFin = fin;

			if (idx == 0) {
				reducedContent += StringUtils.trimRight(textBefore) + buttonText;
			} else {
				reducedContent += StringUtils.trimLeftRight(textBefore) + buttonText;
			}
		}

		reducedContent += StringUtils.trimLeft(text.substring(fin));

		return reducedContent;
	},

	/* Removes from the text the paragraphs without misspellings */
	removeParagraphsWithoutMisspellings : function(text) {
		var reParagraph = new RegExp('(^|\\n{2,})[\\S\\s]+?(?=\\n{2,}|$)', 'g');
		var reducedContent = '';

		var reMatch;
		while ((reMatch = reParagraph.exec(text)) != null) {
			if (reMatch[0].indexOf('miss-') != -1) {
				if (reducedContent) {
					reducedContent += '\n<hr>\n';
				}
				reducedContent += this.trimText(reMatch[0]);
			}
		}

		// Remove the repeated new lines
		var reNewLines = new RegExp('\\n{2,}', 'g');
		reducedContent = reducedContent.replace(reNewLines, '');

		return reducedContent;
	},

	/* Replaces the original content with the accepted fixes. */
	replaceFixes : function(text, misspellingMatches) {
		// Loop through inversely the misspelling matches array and replace by the fixes
		var fixedText = text;
		for (var idx = 0; idx < misspellingMatches.length; idx++) {
			var missMatch = misspellingMatches[idx];
			if (missMatch.fixed) {
				fixedText = StringUtils.replaceAt(fixedText,
						missMatch.position, missMatch.word, missMatch.fix);
			}
		}

		return fixedText;
	}

};