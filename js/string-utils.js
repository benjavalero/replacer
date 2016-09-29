var StringUtils = {

	// <div> => &lt;div&gt;
	encodeHtml : function(htmlText) {
		return jQuery('<div />').text(htmlText).html();
	},

	// &lt;div&gt; => <div>
	decodeHtml : function(htmlText) {
		return jQuery('<div />').html(htmlText).text();
	},

	// If a character is upper-case
	isUpperCase : function(ch) {
		return ch === ch.toUpperCase();
	}

};