import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static command.OrderValidation.validExpiryDate;

public class ValidExpiryDateTest {

    @Test
    void validExpiryDateTest(){
        String date = "12/23";
        Assertions.assertTrue(validExpiryDate(date));
    }

    @Test
    void invalidExpiryDateTest(){
        String date = "01/20";
        Assertions.assertFalse(validExpiryDate(date));
    }

    @Test
    void invalidFormatExpiryDateTest(){
        String date = "12/23/12/12";
        Assertions.assertFalse(validExpiryDate(date));
    }
    @Test
    void invalidTypeExpiryDateTest(){
        String date = "hello world!";
        Assertions.assertFalse(validExpiryDate(date));
    }
    @RepeatedTest(10)
    void validRandomExpiryDateTest(){
        int monthInt = ThreadLocalRandom.current().nextInt(1, 13);
        String month;
        if (monthInt < 10){
            month = '0' + Integer.toString(monthInt);
        } else {
            month = Integer.toString(monthInt);
        }
        String year = Integer.toString(ThreadLocalRandom.current().nextInt(24, 31));
        String date = month + '/' + year;
        System.out.println(date);
        Assertions.assertTrue(validExpiryDate(date));
    }
    @Test
    void invalidRandomExpiryDateTest(){
        String month = Integer.toString(ThreadLocalRandom.current().nextInt(1, 13));
        String year = Integer.toString(ThreadLocalRandom.current().nextInt(10, 22));
        String date = month + '/' + year;
        Assertions.assertFalse(validExpiryDate(date));
    }

}
