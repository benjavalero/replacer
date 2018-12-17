document.addEventListener('DOMContentLoaded', function () {

    document.querySelector('#button-index').addEventListener('click', function () {
        runIndexation(document.querySelector('#force-check').checked);
    });

    findDumpStatus();

    // Refresh every 10 seconds
    setInterval(findDumpStatus, 10000);
});

function runIndexation(forceIndexation) {
    reqwest({
        url: 'dump/run' + (forceIndexation ? '/force' : ''),
        type: 'json',
        success: function(response) {
            document.querySelector('#notRunning').classList.add('hidden');
            document.querySelector('#running').classList.remove('hidden');

            findDumpStatus();
        }
    });
}

function findDumpStatus() {
    reqwest({
        url: 'dump/status',
        type: 'json',
        success: function(response) {
            if (response.running) {
                document.querySelector('#notRunning').classList.add('hidden');
                document.querySelector('#running').classList.remove('hidden');
            } else {
                document.querySelector('#running').classList.add('hidden');
                document.querySelector('#notRunning').classList.remove('hidden');
                if (response.numArticlesRead) {
                    document.querySelector('#notRunningStatus').classList.remove('hidden');
                }
            }

            document.querySelector('#force-check').checked = response.forceProcess;
            document.querySelectorAll('.dumpName').forEach(function(span) { span.textContent = response.dumpFileName });
            document.querySelectorAll('.dumpTime').forEach(function(span) { span.textContent = response.time });
            document.querySelectorAll('.numArticlesRead').forEach(function(span) { span.textContent = response.numArticlesRead });
            document.querySelectorAll('.numArticlesProcessed').forEach(function(span) { span.textContent = response.numArticlesProcessed });
            document.querySelectorAll('.dumpProgress').forEach(function(span) { span.textContent = response.progress });
            document.querySelectorAll('.dumpAverage').forEach(function(span) { span.textContent = response.average });
        }
    });

}

