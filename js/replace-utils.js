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

	/* Returns an array with matches in the text of the replacement exceptions.
	 * The objects in the array have the properties (ini, fin, text). */
	findExceptionMatches : function(text) {
		var pageExceptions = [];
		for (var i = 0; i < exceptionRegExps.length; i++) {
			var exceptionRegExp = exceptionRegExps[i];
			while ((exceptionMatch = exceptionRegExp.exec(text)) != null) {
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
		return pageExceptions;
	}

};