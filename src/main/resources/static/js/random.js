var pageId;
var pageTitle;
var pageFixes;
var word;

$(document).ready(function() {
    word = $.url('?word');

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
        url : 'article/random' + (word ? '/word/' + word : ''),
        dataType : 'json'
    }).done(function(response) {
        loadArticle(response);
    }).fail(function(response) {
        $('#main-container').prepend('<div class="alert alert-danger alert-dismissible">'
            + '<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>'
            + 'Error cargando artículo con errores ortográfico. Recargue la página.'
            + '</div>');
    });
}


function loadArticle(response) {
    pageId = response.id;
    pageTitle = response.title;
    $('#article-title').text(pageTitle);
    if (response.content) {
        $('#article-link').attr("href", "https://es.wikipedia.org/wiki/" + pageTitle);
        $('#article-link').removeClass("disabled");
        $('#button-save').removeClass("disabled");
        $('#article-content').html(response.content);
    }
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
        $('#main-container').prepend('<div class="alert alert-danger alert-dismissible">'
            + '<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>'
            + 'Error guardando los cambios en ' + pageTitle + ': ' + JSON.stringify(response)
            + '</div>');
    });
}