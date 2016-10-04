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
	},

    // lenteja => [Ll]enteja
    getRegexWordIgnoreCase : function(word) {
        var ch = word[0];
        return '[' + ch.toUpperCase() + ch.toLowerCase() + ']' + word.substring(1);
    },

    // país => País
    setFirstUpperCase : function(word) {
        return word[0].toUpperCase() + word.substr(1);
    }

};