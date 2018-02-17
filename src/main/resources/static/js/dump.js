$(document).ready(function() {

    $('#button-index').click(function() {
        runIndexation();
    });

    findDumpStatus();

    // Refresh every 15 seconds
    setTimeout("location.reload(true);", 15000);
});

function runIndexation() {
    $.ajax({
        url : 'dump/run',
        dataType : 'json'
    }).done(function(response) {
        // Do nothing
    }).fail(function(response) {
        alert('Error lanzando la indexación: ' + JSON.stringify(response));
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
        alert('Error buscando el estado de la indexación: ' + JSON.stringify(response));
    });
}
