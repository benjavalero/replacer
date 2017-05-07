$(document).ready(function() {
    $.ajax({
        url : 'count/misspellings',
        dataType : 'json'
    }).done(function(response) {
        $('#count-misspellings').text(response);
    }).fail(function(response) {
        alert('Error en estadísticas: ' + JSON.stringify(response));
    });

    $.ajax({
        url : 'count/articles',
        dataType : 'json'
    }).done(function(response) {
        $('#count-articles').text(response);
    }).fail(function(response) {
        alert('Error en estadísticas: ' + JSON.stringify(response));
    });
});