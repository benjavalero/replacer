var RegEx = {

	// JavaScript doesn't support regex lookbehind
	// JavaScript doesn't support dotall flag. Workaround: . => [\\S\\s]

	reComment : new RegExp('(&lt;!--[\\S\\s]*?--&gt;)', 'g'),

	reHeader : new RegExp('(\\={2,}.+?\\={2,})', 'g'),

	reHyperlink : new RegExp('(https?://[\\w\\./\\-\\+\\?&%=:#;~]+)', 'g'),

	reLink : new RegExp('(\\[\\[[^\\]]+\\]\\])', 'g'),

	// \w doesn't include accentuated characters
	wordCharacterClass : '\\wÁáÉéÍíÓóÚúÜüÑñ',

	reTemplateParam : new RegExp('\\|[\\wÁáÉéÍíÓóÚúÜüÑñ\\s]+(?=\\=)', 'g'),

	reIndexValue : new RegExp('\\|\\s*índice\\s*=[^\\}\\|]*', 'g'),

	reUnreplaceableTemplate : new RegExp(
			'\\{\\{(?:ORDENAR:|DEFAULTSORT:|NF\\|)[^\\}]*', 'g'),

	reTemplateName : new RegExp('\\{\\{[^\\|\\}]+', 'g'),

	reQuote : new RegExp('\\{\\{(?:[Cc]ita|c?Quote)\\|[^\\}]*', 'g'),

	reQuotes : new RegExp("'{2,5}.+?'{2,5}", 'g'),

	reAngularQuotes : new RegExp('«[^»]+»', 'g'),

	reTypographicQuotes : new RegExp('“[^”]+”', 'g'),

	reDoubleQuotes : new RegExp('"[^"]+"', 'g'),

	reFileName : new RegExp(
			'[\\=\\|:][^\\=\\|:]+\\.(?:svg|jpe?g|JPG|png|PNG|gif|ogg|pdf)', 'g'),

	reRefName : new RegExp('&lt;ref\\s*name\\s*=[\\S\\s]*?&gt;', 'g'),

	reCategory : new RegExp('\\[\\[Categoría:.*?\\]\\]', 'g'),

	reTagMath : new RegExp('&lt;math&gt;[\\S\\s]*?&lt;/math&gt;', 'g'),

	reFalsePositives : new RegExp(
			'Index|Link|Online|[Rr]eferences?|[Aa]un así|&lt;|[Ff]ans|Comics|[Ss]e publica|' +
			'Tropicos\\.org|Julio César|Magazine|Records|Jet Propulsion Laboratory',
			'g')

};