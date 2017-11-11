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
        url : 'statistics/count/articles-reviewed',
        dataType : 'json'
    }).done(function(response) {
        $('#count-articles-reviewed').text(response);
    }).fail(function(response) {
        alert('Error en estadísticas: ' + JSON.stringify(response));
    });

    $.ajax({
        url : 'statistics/count/misspellings',
        dataType : 'json'
    }).done(function(response) {
        $('#list-misspellings').datatable({
            data: response,
            pageSize: 20,
            sort: [true, true],
            filters: [true, false],
            pagingDivSelector: "#paging-list-misspellings"
        });
    }).fail(function(response) {
        alert('Error en estadísticas: ' + JSON.stringify(response));
    });

});