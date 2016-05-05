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

// país  =>  país
function setFirstUpperCase(word) {
	return word[0].toUpperCase() + word.substr(1);
}

// replaceAt('0123456789', 3, '34', 'XXXX')  =>  '012XXXX56789'
function replaceAt(text, position, replaced, replacement) {
	return text.substr(0, position) + replacement + text.substr(position + replaced.length);
}
