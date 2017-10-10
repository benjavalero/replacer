$(document).ready(function() {

    $.ajax({
        url : 'statistics/count/potentialErrors',
        dataType : 'json'
    }).done(function(response) {
        $('#count-potential-errors').text(response);
    }).fail(function(response) {
        alert('Error en estadísticas: ' + JSON.stringify(response));
    });

    $.ajax({
        url : 'statistics/count/articles',
        dataType : 'json'
    }).done(function(response) {
        $('#count-articles').text(response);
    }).fail(function(response) {
        alert('Error en estadísticas: ' + JSON.stringify(response));
    });

    $.ajax({
        url : 'statistics/count/misspellings',
        dataType : 'json'
    }).done(function(response) {
        var list = '<ul>';
        for (var i = 0; i < response.length; i++) {
            // var group = response[i];
            list += '<li>' + response[i][0] + ': ' + response[i][1] + '</li>';
        }
        list += '</ul>';
        $('#list-misspellings').html(list);
    }).fail(function(response) {
        alert('Error en estadísticas: ' + JSON.stringify(response));
    });

});