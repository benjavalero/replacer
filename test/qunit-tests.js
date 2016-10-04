QUnit.test("String Utils", function(assert) {
    assert.ok(StringUtils.isUpperCase('A'));
    assert.notOk(StringUtils.isUpperCase('a'));
    assert.ok(StringUtils.isUpperCase('Á'));
    assert.notOk(StringUtils.isUpperCase('á'));
    assert.ok(StringUtils.isUpperCase('Ñ'));
    assert.notOk(StringUtils.isUpperCase('ñ'));
});

QUnit.test("RegEx Utils", function(assert) {
    var match;

    var exp = "<!-- Esto es un \n comentario -->";
    var text = "xxx " + exp + " zzz";
    var isFound = false;
    while ((match = RegEx.reComment.exec(text)) != null) {
        if (match[0] == exp) {
            isFound = true;
        }
    }
    assert.ok(isFound);

    exp = "&lt;!-- Esto es un \n comentario --&gt;";
    text = "xxx " + exp + " zzz";
    isFound = false;
    while ((match = RegEx.reCommentEncoded.exec(text)) != null) {
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

    exp = "https://google.es?u=t&ja2+rl=http://www.marca.com#page2";
    text = "xxx " + exp + " zzz";
    isFound = false;
    while ((match = RegEx.reHyperlink.exec(text)) != null) {
        if (match[0] == exp) {
            isFound = true;
        }
    }
    assert.ok(isFound);

    exp = "[[España|país]]";
    text = "xxx " + exp + " zzz";
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

    exp = "<ref name= España >";
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
});
