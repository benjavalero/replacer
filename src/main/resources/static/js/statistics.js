$(document).ready(function() {

    $.ajax({
        url : 'statistics/count/potentialErrors',
        dataType : 'json'
    }).done(function(response) {
        $('#count-potential-errors').text(response);
    }).fail(function(response) {
        $('#main-container').prepend('<div class="alert alert-danger alert-dismissible">'
            + '<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>'
            + 'Error en estadísticas: ' + JSON.stringify(response)
            + '</div>');
    });

    $.ajax({
        url : 'statistics/count/articles',
        dataType : 'json'
    }).done(function(response) {
        $('#count-articles').text(response);
    }).fail(function(response) {
        $('#main-container').prepend('<div class="alert alert-danger alert-dismissible">'
            + '<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>'
            + 'Error en estadísticas: ' + JSON.stringify(response)
            + '</div>');
    });

    $.ajax({
        url : 'statistics/count/articles-reviewed',
        dataType : 'json'
    }).done(function(response) {
        $('#count-articles-reviewed').text(response);
    }).fail(function(response) {
        $('#main-container').prepend('<div class="alert alert-danger alert-dismissible">'
            + '<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>'
            + 'Error en estadísticas: ' + JSON.stringify(response)
            + '</div>');
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
            lineFormat: function (id, data) {
                var res = $('<tr></tr>') ;
                res.append('<td><a href="random.html?word=' + data[0] + '" target="_blank">' + data[0] + '</a></td>') ;
                res.append('<td>' + data[1] + '</td>');
                return res ;
            },
            pagingDivSelector: "#paging-list-misspellings"
        });
    }).fail(function(response) {
        $('#main-container').prepend('<div class="alert alert-danger alert-dismissible">'
            + '<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>'
            + 'Error en estadísticas: ' + JSON.stringify(response)
            + '</div>');
    });

});