QUnit.test("String Utils", function(assert) {
	assert.ok(StringUtils.isUpperCase('A'));
	assert.notOk(StringUtils.isUpperCase('a'));
	assert.ok(StringUtils.isUpperCase('Á'));
	assert.notOk(StringUtils.isUpperCase('á'));
	assert.ok(StringUtils.isUpperCase('Ñ'));
	assert.notOk(StringUtils.isUpperCase('ñ'));

	assert.equal(StringUtils.getRegexWordIgnoreCase('lenteja'), '[Ll]enteja');
	assert.equal(StringUtils.getRegexWordIgnoreCase('Partido'), '[Pp]artido');

	assert.equal(StringUtils.setFirstUpperCase('lenteja'), 'Lenteja');
	assert.equal(StringUtils.setFirstUpperCase('Partido'), 'Partido');

	assert.equal(StringUtils.replaceAt('0123456789', 3, '34', 'XXXX'), '012XXXX56789');
});

QUnit.test("RegEx Utils", function(assert) {
	var match;

	var exp = "&lt;!-- Esto es un \n comentario --&gt;";
	var text = "xxx " + exp + " zzz";
	var isFound = false;
	while ((match = RegEx.reComment.exec(text)) != null) {
		if (match[0] == exp) {
			isFound = true;
		}
	}
	assert.ok(isFound);

	exp = "=== Cabecera ===";
	text = "xxx \n " + exp + " \n zzz";
	isFound = false;
	while ((match = RegEx.reHeader.exec(text)) != null) {
		if (match[0] == exp) {
			isFound = true;
		}
	}
	assert.ok(isFound);

	exp = "https://google.es?u=t&ja2+rl=http://www.marca.com#page~2";
	text = "xxx " + exp + " zzz";
	isFound = false;
	while ((match = RegEx.reHyperlink.exec(text)) != null) {
		if (match[0] == exp) {
			isFound = true;
		}
	}
	assert.ok(isFound);

	exp = "[[España|país]]";
	text = "xxx " + exp + " [[Francia]] zzz";
	isFound = false;
	while ((match = RegEx.reLink.exec(text)) != null) {
		if (match[0] == exp) {
			isFound = true;
		}
	}
	assert.ok(isFound);

	exp = "{{Template| param1 = value1 | parám_ 2 = value2 }} "
			+ "{{Cita|Alea iacta est}} jajaja =";
	text = "xxx " + exp + " zzz";
	var matches = [];
	while ((match = RegEx.reTemplateParam.exec(text)) != null) {
		matches.push(match[0]);
	}
	assert.equal(matches.length, 2);
	assert.ok(matches.indexOf("| param1 ") != -1);
	assert.ok(matches.indexOf("| parám_ 2 ") != -1);

	exp = "| índice = yyyy \n zzz ";
	text = "xxx " + exp + "| zzz";
	isFound = false;
	while ((match = RegEx.reIndexValue.exec(text)) != null) {
		if (match[0] == exp) {
			isFound = true;
		}
	}
	assert.ok(isFound);

	exp = "{{NF|yyy ";
	text = "xxx " + exp + "}} zzz";
	isFound = false;
	while ((match = RegEx.reUnreplaceableTemplate.exec(text)) != null) {
		if (match[0] == exp) {
			isFound = true;
		}
	}
	assert.ok(isFound);

	exp = "{{Plantilla";
	text = "xxx " + exp + "| yyy}} zzz";
	isFound = false;
	while ((match = RegEx.reTemplateName.exec(text)) != null) {
		if (match[0] == exp) {
			isFound = true;
		}
	}
	assert.ok(isFound);

	exp = "{{Cita|yyy \n yyy";
	text = "xxx " + exp + "}} zzz";
	isFound = false;
	while ((match = RegEx.reQuote.exec(text)) != null) {
		if (match[0] == exp) {
			isFound = true;
		}
	}
	assert.ok(isFound);

	exp = "'''I'm Muzzy''' \"zzz\" ''''ttt'' ''uuu\"";
	text = "xxx " + exp + " vvv";
	matches = [];
	while ((match = RegEx.reQuotes.exec(text)) != null) {
		matches.push(match[0]);
	}
	assert.equal(matches.length, 2);
	assert.ok(matches.indexOf("'''I'm Muzzy'''") != -1);
	assert.ok(matches.indexOf("''''ttt''") != -1);

	exp = "«yyy»";
	text = "xxx " + exp + " zzz";
	isFound = false;
	while ((match = RegEx.reAngularQuotes.exec(text)) != null) {
		if (match[0] == exp) {
			isFound = true;
		}
	}
	assert.ok(isFound);

	exp = "“yyy”";
	text = "xxx " + exp + " zzz";
	isFound = false;
	while ((match = RegEx.reTypographicQuotes.exec(text)) != null) {
		if (match[0] == exp) {
			isFound = true;
		}
	}
	assert.ok(isFound);

	exp = ": jande_españa.png";
	text = "xxx [[File" + exp + " | España]] zzz";
	isFound = false;
	while ((match = RegEx.reFileName.exec(text)) != null) {
		if (match[0] == exp) {
			isFound = true;
		}
	}
	assert.ok(isFound);

	exp = "&lt;ref name= España &gt;";
	text = "xxx " + exp + " zzz";
	isFound = false;
	while ((match = RegEx.reRefName.exec(text)) != null) {
		if (match[0] == exp) {
			isFound = true;
		}
	}
	assert.ok(isFound);

	exp = "[[Categoría:Lluvia]]";
	text = "xxx " + exp + " zzz";
	isFound = false;
	while ((match = RegEx.reCategory.exec(text)) != null) {
		if (match[0] == exp) {
			isFound = true;
		}
	}
	assert.ok(isFound);

	exp = "&lt;math&gt;Esto es un ejemplo\n en LaTeX&lt;/math&gt;";
    var text = "xxx " + exp + " zzz";
    var isFound = false;
    while ((match = RegEx.reTagMath.exec(text)) != null) {
        if (match[0] == exp) {
            isFound = true;
        }
    }
    assert.ok(isFound);

    exp = "Un Link en el Index Online de Tropicos.org.";
    text = "xxx " + exp + " vvv";
    matches = [];
    while ((match = RegEx.reFalsePositives.exec(text)) != null) {
        matches.push(match[0]);
    }
    assert.equal(matches.length, 4);
    assert.ok(matches.indexOf("Link") != -1);
    assert.ok(matches.indexOf("Index") != -1);
    assert.ok(matches.indexOf("Online") != -1);
    assert.ok(matches.indexOf("Tropicos.org") != -1);
});

QUnit.test("Replace Utils", function(assert) {
	var text = 'Entre comillas «angulares» y "dobles".';
	var excMatches = ReplaceUtils.findExceptionMatches(text);
	assert.equal(excMatches.length, 2);
	// The matches are found depending on the order of the regex in the exceptions array
	assert.equal(excMatches[0].ini, 15);
	assert.equal(excMatches[0].text, '«angulares»');
	assert.equal(excMatches[1].ini, 29);
	assert.equal(excMatches[1].text, '"dobles"');

	text = 'Y Cayo entre españa y España con la excepción de "españa".';
	var misspellings = [
		{word : 'cayo', cs : 0, suggestion : 'cayó'},
		{word : 'españa', cs : 1, suggestion : 'España'}
	];
	var misspellingMatches = ReplaceUtils.findMisspellingMatches(text, misspellings);
	assert.equal(misspellingMatches.length, 2);
	assert.equal(misspellingMatches[0].position, 2);
	assert.equal(misspellingMatches[1].position, 13);
});