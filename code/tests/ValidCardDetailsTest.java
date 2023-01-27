import command.OrderValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static command.OrderValidation.validExpiryDate;
import static command.OrderValidation.validateCard;

public class ValidCardDetailsTest {

    String generateValidExpiry(){
        int monthInt = ThreadLocalRandom.current().nextInt(1, 13);
        String month;
        if (monthInt < 10){
            month = '0' + Integer.toString(monthInt);
        } else {
            month = Integer.toString(monthInt);
        }
        String year = Integer.toString(ThreadLocalRandom.current().nextInt(24, 31));
        String date = month + '/' + year;
        return date;
    }

    String generateInvalidExpiry(){
        int monthInt = ThreadLocalRandom.current().nextInt(1, 13);
        String month;
        if (monthInt < 10){
            month = '0' + Integer.toString(monthInt);
        } else {
            month = Integer.toString(monthInt);
        }
        String year = Integer.toString(ThreadLocalRandom.current().nextInt(10, 20));
        String date = month + '/' + year;
        return date;
    }

    String generateValidCVV(){
        String testCVV = "";
        for (int i = 0; i < ThreadLocalRandom.current().nextInt(3, 5); i++) {
            int digit = ThreadLocalRandom.current().nextInt(0, 10);
            testCVV = testCVV.concat(Integer.toString(digit));
        }
        return testCVV;
    }

    String generateInvalidCVV(){
        String testCVV = "";
        int length = ThreadLocalRandom.current().nextInt(0, 100);
        while (length == 3 || length == 4){
            length = ThreadLocalRandom.current().nextInt(0, 100);
        }

        for (int i = 0; i < length; i++) {
            int digit = ThreadLocalRandom.current().nextInt(20, 100);
            testCVV = testCVV.concat(Integer.toString(digit));
        }
        return testCVV;
    }

    String generateValidCardNo(){
        String testCardNo = "";
        for (int i = 0; i < 16; i++) {
            int digit = ThreadLocalRandom.current().nextInt(0, 10);
            testCardNo = testCardNo.concat(Integer.toString(digit));
        }
        return testCardNo;
    }

    String generateInvalidCardNo(){
        String testCardNo = "";

        int length = ThreadLocalRandom.current().nextInt(0, 100);
        while (length == 16){
            length = ThreadLocalRandom.current().nextInt(0, 100);
        }

        for (int i = 0; i < length; i++) {
            int digit = ThreadLocalRandom.current().nextInt(0, 100);
            testCardNo = testCardNo.concat(Integer.toString(digit));
        }

        return testCardNo;
    }

    @Test
    void allValid(){
        String date = "12/23";
        String CVV = "123";
        String cardNo = "1234828282828282";
        Assertions.assertTrue(validateCard(cardNo, CVV, date));
    }

    @RepeatedTest(30)
    void allRandomValid(){
        String date = generateValidExpiry();
        String CVV = generateValidCVV();
        String cardNo = generateValidCardNo();
        Assertions.assertTrue(validateCard(cardNo, CVV, date));
    }

    @RepeatedTest(30)
    void allRandomInvalid(){
        String date = generateInvalidExpiry();
        String CVV = generateInvalidCVV();
        String cardNo = generateInvalidCardNo();
        Assertions.assertFalse(validateCard(cardNo, CVV, date));
    }

    @RepeatedTest(30)
    void InvalidDateRestValid(){
        String date = generateInvalidExpiry();
        String CVV = generateValidCVV();
        String cardNo = generateValidCardNo();
        Assertions.assertFalse(validateCard(cardNo, CVV, date));
    }

    @RepeatedTest(30)
    void CvvAndDateInvalid(){
        String date = generateInvalidExpiry();
        String CVV = generateInvalidCVV();
        String cardNo = generateValidCardNo();
        Assertions.assertFalse(validateCard(cardNo, CVV, date));
    }

}
