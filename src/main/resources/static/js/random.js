var pageId;
var pageTitle;
var pageFixes;

$(document).ready(function() {
    // Enable the collapse effects
    $('.collapse').collapse();

    $('#button-save').click(function() {
        saveChanges();
    });

    findRandomArticle();
});

function findRandomArticle() {
    // Call REST to get the article to check
    $.ajax({
        url : 'article/random',
        dataType : 'json'
    }).done(function(response) {
        loadArticle(response);
    }).fail(function(response) {
        alert('Error buscando artículos con errores ortográficos: ' + JSON.stringify(response));
    });
}


function loadArticle(response) {
    pageId = response.id;
    pageTitle = response.title;
    $('#article-title').text(pageTitle);
    $('#article-link').attr("href", "https://es.wikipedia.org/wiki/" + pageTitle);
    $('#article-link').removeClass("disabled");
    $('#button-save').removeClass("disabled");
    $('#article-content').html(response.content);
    pageFixes = response.fixes;

    // Add event to the misspelling buttons
    $('.miss').click(function() {
        turnMisspelling(this.id);
    });

    // Add cool Bootstrap tooltips
    $(function() {
        $('[data-toggle="tooltip"]').tooltip()
    });
}

function turnMisspelling(missId) {
    var idx = missId.split('-')[1];
    var missFix = pageFixes[idx];
    if (missFix.fixed) {
        $('#' + missId).removeClass('btn-success');
        $('#' + missId).addClass('btn-danger');
        $('#' + missId).html(missFix.originalText);
        missFix.fixedText = missFix.originalText;
        missFix.fixed = false;
    } else {
        $('#' + missId).removeClass('btn-danger');
        $('#' + missId).addClass('btn-success');
        $('#' + missId).html(missFix.proposedFixes[0]);
        missFix.fixedText = missFix.proposedFixes[0];
        missFix.fixed = true;
    }
}

function saveChanges() {
    var data = {'id' : pageId, 'title' : pageTitle, 'fixes' : pageFixes}

    $('#button-save').addClass("disabled");
    $('#article-link').addClass("disabled");
    $('#article-title').text("Cargando artículo…");
    $('#article-content').html('');

    $.ajax({
        type : "POST",
        contentType : "application/json",
        url : "article/save",
        data : JSON.stringify(data),
        dataType : 'json'
    }).done(function(response) {
        findRandomArticle();
    }).fail(function(response) {
        alert('Error guardando los cambios en ' + pageTitle + ': ' + JSON.stringify(response));
    });
}