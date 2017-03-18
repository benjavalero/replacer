var pageTitle;
var pageFixes;

$(document).ready(function() {
    // TODO Enable/disable while loading new articles. Also hide/display the article link.
    $('.button-save').click(function() {
        saveChanges();
    });

    // Call REST to get the article to check
    $.ajax({
        url : 'find/random',
        dataType : 'json'
    }).done(function(response) {
        loadArticle(response);
    }).fail(function(response) {
        alert('Error buscando artículos con errores ortográficos. ' + JSON.stringify(response));
    });
});

function loadArticle(response) {
    pageTitle = response.title;
    $('#article-title').text(pageTitle);
    $('#article-link').attr("href", "https://es.wikipedia.org/wiki/" + pageTitle);
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
		$('#' + missId).html(missFix.word);
		missFix.fixed = false;
	} else {
		$('#' + missId).removeClass('btn-danger');
		$('#' + missId).addClass('btn-success');
		$('#' + missId).html(missFix.fix);
		missFix.fixed = true;
	}
}

function saveChanges() {
    var data = {'title' : pageTitle, 'content' : '', 'fixes' : pageFixes}

    $('#article-title').text("Cargando artículo…");
    $('#article-content').html('');

	$.ajax({
		type : "POST",
		contentType : "application/json",
		url : "save/random",
		data : JSON.stringify(data),
		dataType : 'json'
    }).done(function(response) {
        loadArticle(response);
    }).fail(function(response) {
        alert('Error guardando los cambios en: ' + pageTitle + '. ' + JSON.stringify(response));
	});
}