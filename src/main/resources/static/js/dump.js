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
            message += '<li>Progreso: ' + response.progress + '&nbsp;%</li>';
            message += '<li>Núm. artículos procesados: ' + response.numProcessedItems + '</li>';
            message += '<li>Finalización estimada: ' + parseMillisecondsIntoReadableTime(response.eta) + '&nbsp;s</li>';
            message += '<li>Tiempo medio por artículo: ' + response.average + ' ms</li>';
            message += '</ul>';
        } else {
            $('#button-index').removeClass("disabled");

            message = 'La indexación no se está ejecutando.';
            if (response.lastRun) {
                message += '<ul>';
                message += '<li>Última ejecución: ' + new Date(response.lastRun) + '</li>';
                message += '<li>Núm. artículos procesados: ' + response.average + '</li>';
                message += '<li>Tiempo medio por artículo: ' + response.numProcessedItems + ' ms</li>';
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

function parseMillisecondsIntoReadableTime(milliseconds) {
    //Get hours from milliseconds
    var hours = milliseconds / (1000*60*60);
    var absoluteHours = Math.floor(hours);
    var h = absoluteHours > 9 ? absoluteHours : '0' + absoluteHours;

    //Get remainder from hours and convert to minutes
    var minutes = (hours - absoluteHours) * 60;
    var absoluteMinutes = Math.floor(minutes);
    var m = absoluteMinutes > 9 ? absoluteMinutes : '0' +  absoluteMinutes;

    //Get remainder from minutes and convert to seconds
    var seconds = (minutes - absoluteMinutes) * 60;
    var absoluteSeconds = Math.floor(seconds);
    var s = absoluteSeconds > 9 ? absoluteSeconds : '0' + absoluteSeconds;

    return h + ':' + m + ':' + s;
}
