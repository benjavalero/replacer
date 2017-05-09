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

    $.ajax({
        url : 'list/misspellings',
        dataType : 'json'
    }).done(function(response) {
        var list = '<ul>';
        for (var i = 0; i < response.length; i++) {
            var group = response[i];
            list += '<li>' + group.value + ': ' + group.count + '</li>';
        }
        list += '</ul>';
        $('#list-misspellings').html(list);
    }).fail(function(response) {
        alert('Error en estadísticas: ' + JSON.stringify(response));
    });
});