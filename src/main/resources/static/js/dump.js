$(document).ready(function() {

    $('#button-index').click(function() {
        runIndexation($('#force-check').prop('checked'));
    });

    findDumpStatus();

    // Refresh every 10 seconds
    setInterval(findDumpStatus, 10000);
});

function runIndexation(forceIndexation) {
    $.ajax({
        url : 'dump/run',
        dataType : 'json',
        data : {
            force : forceIndexation
        }
    }).done(function(response) {
        // Do nothing
    }).fail(function(response) {
        $('#main-container').prepend('<div class="alert alert-danger alert-dismissible">'
            + '<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>'
            + 'Error lanzando la indexación: ' + JSON.stringify(response.responseJSON.message)
            + '</div>');
    });

    $('#button-index').addClass("disabled");
    $('#force-check').attr("disabled", "disabled");
    $('#status-index').html("Comenzando indexación...");
}

function findDumpStatus() {
    $.ajax({
        url : 'dump/status',
        dataType : 'json'
    }).done(function(response) {
        var message = 'La indexación ';
        message += response.running ? '' : 'no ';
        message += 'se está ejecutando.';

        message += '<ul>';
        message += '<li>Fichero procesado: ' + response.dumpFileName + '</li>'
        message += '<li>' + (response.running ? 'Tiempo estimado' : 'Tiempo total') + ': ' + response.time + ' s</li>';
        message += '<li>Artículos leídos/procesados: ' + response.numArticlesRead + ' / ' + response.numArticlesProcessed + ' (' + response.progress + '&nbsp;%)</li>'
        message += '<li>Tiempo medio por artículo: ' + response.average + ' ms</li>';
        message += '</ul>';

        if (!response.running) {
            $('#button-index').removeClass("disabled");
            $('#force-check').removeAttr("disabled");
        }

        $('#status-index').html(message);

        // Check-box force indexing
        $('#force-check').prop('checked', response.forceProcessArticles);
    }).fail(function(response) {
        $('#main-container').prepend('<div class="alert alert-danger alert-dismissible">'
            + '<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>'
            + 'Error buscando el estado de la indexación: ' + JSON.stringify(response.responseJSON.message)
            + '</div>');
    });
}

