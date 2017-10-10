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
        if (!response.running) {
            $('#button-index').removeClass("disabled");
        }

        $('#status-index').html(response.message);
    }).fail(function(response) {
        alert('Error buscando el estado de la indexación: ' + JSON.stringify(response));
    });
}
