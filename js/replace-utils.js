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
     * The mispellings are objects with (word, cs, suggestion).
     * The objects in the array have the properties (word, position, suggestions, fix, fixed).
     * The misspelling in the text exceptions are omitted. */
    findMisspellingMatches : function (text, misspellings) {
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
            var re = new RegExp('[^' + RegEx.wordCharacterClass + '](' + word + ')[^'
                    + RegEx.wordCharacterClass + ']', 'g');

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
                    if ((exception.ini <= matchIndex)
                            && (matchIndex <= exception.fin)) {
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
                var replacement = '<button id="miss-' + idx + '"'
                        + ' title="' + misspellingMatch.suggestions + '"'
                        + ' data-toggle="tooltip" data-placement="top" class="miss btn btn-danger" type="button">'
                        + misspellingMatch.word + '</button>';
                replacedContent = StringUtils.replaceAt(replacedContent,
                        misspellingMatch.position, misspellingMatch.word,
                        replacement);
            }
        }

        return replacedContent;
    },

     trimParagraph : function(paragraph) {
        var reButton = new RegExp('[\\S\\s]{0,50}<button[\\S\\s]+?</button>[\\S\\s]{0,50}', 'g');
        var trimmed = '';

        var reMatch;
        while ((reMatch = reButton.exec(paragraph)) != null) {
            if (reMatch.index > 0) {
                trimmed += '\n...';
            }
            trimmed += reMatch[0];
            if (reMatch.index + reMatch[0].length < paragraph.length) {
                trimmed += '...\n';
            }
        }

        return trimmed;
     },

     /* Removes from the text the paragraphs without misspelings */
     removeParagraphsWithoutMisspellings : function(text) {
        var positions = [];

        // Add the document start as a position
        positions.push(0);

        var reMatch;
        while ((reMatch = RegEx.reNewLines.exec(text)) != null) {
            positions.push(reMatch.index);
        }

        // Add the document end as a position
        positions.push(text.length);

        var reducedContent = '';
        for (var idx = 1; idx < positions.length; idx++) {
            var ini = positions[idx - 1];
            var fin = positions[idx];
            var paragraph = text.substring(ini, fin);
            if (paragraph.indexOf('miss-') != -1) {
                if (reducedContent) {
                    reducedContent += '\n<hr>\n';
                }
                reducedContent += this.trimParagraph(paragraph);
            }
        }

        // Remove the repeated new lines
        reducedContent = reducedContent.replace(RegEx.reNewLines, '');

        return reducedContent;
     }

};