var RegEx = {

	// JavaScript doesn't support regex lookbehind
	// JavaScript doesn't support dotall flag. Workaround: . => [\\S\\s]

	reCommentEncoded : new RegExp('(&lt;!--[\\S\\s]*?--&gt;)', 'g'),
	reComment : new RegExp('<!--[\\S\\s]*?-->', 'g'),

	reHeader : new RegExp('(\\={2,}.+?\\={2,})', 'g'),

	reHyperlink : new RegExp('(https?://[\\w\\./\\-\\+\\?&%=:#;]+)', 'g'),

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

	reRefName : new RegExp('<ref[^>]*>', 'g'),

	reCategory : new RegExp('\\[\\[Categoría:.*?\\]\\]', 'g'),

	reFalsePositives : new RegExp(
			'[Ss]ólo|[Ii]ndex|[Ll]ink|[Oo]nline|[Rr]eferences?|Jean|[Aa]un así',
			'g')

};