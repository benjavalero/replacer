$(document).ready(function() {

    $('#button-index').click(function() {
        runIndexation($('#force-check').prop('checked'));
    });

    findDumpStatus();

    // Refresh every 15 seconds
    setTimeout("location.reload(true);", 15000);
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
            + 'Error lanzando la indexación: ' + JSON.stringify(response)
            + '</div>');
    });

    $('#button-index').addClass("disabled");
    $('#status-index').html("Comenzando indexación...");
}

function findDumpStatus() {
    $.ajax({
        url : 'dump/status',
        dataType : 'json'
    }).done(function(response) {
        var message;

        if (response.running) {
            message = 'La indexación se está ejecutando.';
            message += '<ul>';
            message += '<li>Inicio: ' + response.startDate + '</li>';
            message += '<li>Núm. artículos procesados: ' + response.numProcessedItems + ' (' + response.percentProgress + '&nbsp;%)</li>';
            message += '<li>Finalización estimada: ' + response.estimatedFinishTime + '</li>';
            message += '</ul>';
        } else {
            $('#button-index').removeClass("disabled");

            message = 'La indexación no se está ejecutando.';
            if (response.endDate) {
                message += '<ul>';
                message += '<li>Última ejecución: ' + response.endDate + '</li>';
                message += '<li>Núm. artículos procesados: ' + response.numProcessedItems + '</li>';
                message += '</ul>';
            }
        }

        $('#status-index').html(message);
    }).fail(function(response) {
        $('#main-container').prepend('<div class="alert alert-danger alert-dismissible">'
            + '<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>'
            + 'Error buscando el estado de la indexación: ' + JSON.stringify(response)
            + '</div>');
    });
}
