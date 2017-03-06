package es.bvalero.replacer.task;

import es.bvalero.replacer.service.MisspellingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class UpdateMisspellingsTask {

    @Autowired
    private MisspellingService misspellingService;

    @Scheduled(cron = "0 0 3 1/1 * ?")
    void updateMisspellings() {
        misspellingService.updateMisspellingList();
    }

}
