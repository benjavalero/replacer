document.addEventListener('DOMContentLoaded', function () {

    reqwest({
        url: 'statistics/count/potentialErrors',
        type: 'json',
        success: function(response) {
            document.querySelector('#count-potential-errors').textContent = response;
        }
    });

    reqwest({
        url: 'statistics/count/articles',
        type: 'json',
        success: function(response) {
            document.querySelector('#count-articles').textContent = response;
        }
    });

    reqwest({
        url: 'statistics/count/articles-reviewed',
        type: 'json',
        success: function(response) {
            document.querySelector('#count-articles-reviewed').textContent = response;
        }
    });

    reqwest({
        url: '/statistics/count/misspellings',
        type: 'json',
        success: function(response) {
            var tbody = document.querySelector('#tbody-misspellings');
            response.forEach(function(entry) {
                // Fill the table
                var td1 = document.createElement('td');
                td1.innerHTML = '<a href="random.html?word=' + entry.text + '" target="_blank">' + entry.text + '</a>';
                var td2 = document.createElement('td');
                td2.textContent = entry.count;

                var tr = document.createElement('tr');
                tr.appendChild(td1);
                tr.appendChild(td2);

                tbody.appendChild(tr);
            });

            // Create the pagination
            var datatable = new DataTable(document.querySelector('#table-misspellings'), {
                pageSize: 20,
                sort: [true, true],
                filters: [true, false],
                filterText: 'Filtrar...',
                pagingDivSelector: "#paging-list-misspellings",
                pagingListClass: 'pagination justify-content-center',
                pagingItemClass: 'page-item',
                pagingLinkClass: 'page-link',
                pagingLinkHref: '#'
            });

            // Display the table
            document.querySelector('#find-misspellings').classList.add('hidden');
            document.querySelector('#table-misspellings').classList.remove('hidden');
        }
    });

});