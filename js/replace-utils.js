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
    }
};