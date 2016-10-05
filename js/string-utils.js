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
    },

    // replaceAt('0123456789', 3, '34', 'XXXX') => '012XXXX56789'
    replaceAt : function(text, position, replaced, replacement) {
    	return text.substr(0, position) + replacement + text.substr(position + replaced.length);
    },

    threshold : 50,
    ellipsis : '[...]',

    /* Display only the last n characters of the text, with an ellipsis if needed. */
    trimRight : function(text) {
        if (text.length <= this.threshold) {
            return text;
        } else {
            return this.ellipsis + ' ' + text.substring(text.length - this.threshold, text.length);
        }
    },

    /* Display only the first and last n characters of the text, with an ellipsis if needed. */
    trimLeftRight : function(text) {
        if (text.length <= this.threshold * 2) {
            return text;
        } else {
            return text.substring(0, this.threshold) + ' ' + this.ellipsis + ' ' + text.substring(text.length - this.threshold, text.length);
        }
    },

    /* Display only the first n characters of the text, with an ellipsis if needed. */
    trimLeft : function(text) {
        if (text.length <= this.threshold) {
            return text;
        } else {
            return text.substring(0, this.threshold) + ' ' + this.ellipsis;
        }
    }

};