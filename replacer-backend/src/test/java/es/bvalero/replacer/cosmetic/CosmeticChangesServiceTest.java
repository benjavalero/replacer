package es.bvalero.replacer.cosmetic;

import org.junit.Assert;
import org.junit.Test;

public class CosmeticChangesServiceTest {

    private CosmeticChangesService cosmeticChangesService = new CosmeticChangesService();

    @Test
    public void testApplyCosmeticChanges() {
        String text = "A [[Link|link]] to simplify.";
        String expected = "A [[link]] to simplify.";
        Assert.assertEquals(expected, cosmeticChangesService.applyCosmeticChanges(text));
    }

}
