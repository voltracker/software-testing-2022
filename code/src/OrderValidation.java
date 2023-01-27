package command;

import logging.Logger;
import model.Delivery;
import model.MenuItem;
import model.Order;
import model.OrderOutcome;
import model.Restaurant;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Class containing methods to validate orders
 */
public class OrderValidation {

    // record used to pass the outcome and optionally the corresponding restaurant if the order is valid
    private record pickupRestaurantOutcome(OrderOutcome outcome, Optional<Restaurant> restaurant){
    }

    /**
     * Method that will process a list of orders and set their corresponding order outcomes
     * @param orders list of orders retrieved from REST Server
     * @param restaurants list of Restaurants retrieved from REST Server
     * @return List of Delivery objects
     */
    public static List<Delivery> process(List<Order> orders, List<Restaurant> restaurants){
        // create new list for deliveries
        List<Delivery> deliveries = new ArrayList<>();

        // process each order in the list
        for (Order order: orders) {
            var outcome = validate(order, restaurants);
            // if there is a restaurant found for the order, set it, otherwise leave it null.
            if (outcome.restaurant().isPresent()){
                deliveries.add(new Delivery(order.orderNo(), outcome.outcome(), outcome.restaurant().get(), order.orderTotal()));
            } else {
                deliveries.add(new Delivery(order.orderNo(), outcome.outcome(), null, order.orderTotal()));
            }

        }
        return deliveries;
    }

    /**
     * method used to check if orders have valid card data, if they do, it will check that the order is valid
     * @param order order to validate
     * @param restaurants list of restaurants
     * @return pickupRestaurantOutcome containing the validated order and the corresponding restaurant if there is one
     */
    private static pickupRestaurantOutcome validate(Order order, List<Restaurant> restaurants){
        if (!validCardNumber(order.creditCardNumber())){
            return new pickupRestaurantOutcome(OrderOutcome.InvalidCardNumber, Optional.empty());
        }
        if (!validExpiryDate(order.creditCardExpiry())){
            return new pickupRestaurantOutcome(OrderOutcome.InvalidExpiryDate, Optional.empty());
        }
        if (!validCvv(order.cvv())){
            return new pickupRestaurantOutcome(OrderOutcome.InvalidCvv, Optional.empty());
        }
        return checkPizzaOrder(order, restaurants);
    }

    public static boolean validateCard(String cardNo, String CVV, String expiryMonth){
        if (!validCardNumber(cardNo)){
            return false;
        }
        if (!validCvv(CVV)){
            return false;
        }
        if (!validExpiryDate(expiryMonth)){
            return false;
        }
        return true;
    }

    /**
     * Method to check that the card number is the correct length
     * @param cardNumber card number to validate
     * @return boolean, true if card number is valid, false otherwise
     */
    public static boolean validCardNumber(String cardNumber){
        // matches if there are 16 digits in string ONLY
        String regex = "^\\d{16}$";
        Pattern pattern = Pattern.compile(regex);
        // returns true if card number is valid, false otherwise
        return pattern.matcher(cardNumber).find();
    }

    /**
     * Method to check if the card expiry date has passed or not / is valid
     * @param expiryDate card's expiry date
     * @return boolean, true if expiry date is valid, false otherwise
     */
    public static boolean validExpiryDate(String expiryDate){
        Logger log = Logger.getInstance();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth expiry = YearMonth.parse(expiryDate, formatter);
            // true if expiry date in the future, false if expiry date has passed
            return expiry.isAfter(YearMonth.now());
        } catch (DateTimeParseException e) {
            log.logAction("OrderValidation.validExpiryDate(expiryDate)", LogStatus.VALID_EXPIRY_DATE_PARSE_EXCEPTION);
            return false;
        }
    }

    /**
     * Method used to validate a card's cvv number
     * @param cvv cvv to validate
     * @return boolean, true if valid, false otherwise
     */
    public static boolean validCvv(String cvv){
        // matches if there are either 3 or 4 digits in string ONLY
        String regex = "^\\d{3,4}$";
        Pattern pattern = Pattern.compile(regex);
        // returns true if cvv is valid, false otherwise
        return pattern.matcher(cvv).find();
    }

    /**
     * method to validate whether the pizzas contained in an order are valid
     * @param order order to validate
     * @param restaurants list of restaurants
     * @return pickupOrderOutcome, contains order outcome and corresponding restaurant if applicable
     */
    private static pickupRestaurantOutcome checkPizzaOrder(Order order, List<Restaurant> restaurants){
        // check that there are no less than 1 and no more than 4 pizzas
    	if (order.orderItems().size() > 4 || order.orderItems().size() < 1){
            return new pickupRestaurantOutcome(OrderOutcome.InvalidPizzaCount, Optional.empty());
        }

        // add all pizzas for all restaurants to a list
        List<String> allPizzas = new ArrayList<>();
	    for (Restaurant restaurant : restaurants){
		    allPizzas.addAll(restaurant.getPizzaNames());
	    }

        // if not all the pizzas for the order are in the list of all pizzas, at least one of the pizzas must not exist
        if (!allPizzas.containsAll(order.orderItems())){
            return new pickupRestaurantOutcome(OrderOutcome.InvalidPizzaNotDefined, Optional.empty());
        }

        int totalOfAllPizzas = 0;
        // for each pizza in the order, count the number of times it appears in the all pizzas list
        for (String pizza : order.orderItems()) {
            totalOfAllPizzas += allPizzas.stream()
                    .filter(a -> a.equals(pizza))
                    .toList().size();
        }

        if (totalOfAllPizzas >= 2 * order.orderItems().size()) {
            return new pickupRestaurantOutcome(OrderOutcome.InvalidPizzaCombinationMultipleSuppliers, Optional.empty());
        }

        // set variables finding the corresponding restaurant for the order
        int deliveryCost = 100;
        int restaurantIndex = 0;
        boolean found = false;

        // loop till restaurant found
        while (restaurantIndex < restaurants.size() && !found) {
            // if a restaurant is found that can supply the order, set found to true
            if (restaurants.get(restaurantIndex).getPizzaNames().containsAll(order.orderItems())) {
                found = true;
            } else {
                restaurantIndex++;
            }
        }

        // if there is a restaurant found, calculate the cost of the order
        if(found){

            for (String pizza : order.orderItems()) {
                for (MenuItem item : restaurants.get(restaurantIndex).menu()) {
                    if (item.name().equals(pizza)) {
                        deliveryCost += item.price();
                    }
                 }
            }

            // if the delivery cost doesn't match the one in the order, set the outcome to invalid total
            if (deliveryCost != order.orderTotal()) {
                return new pickupRestaurantOutcome(OrderOutcome.InvalidTotal, Optional.empty());
            } else {
                // otherwise, all criteria have been met, and so the order must be valid
                return new pickupRestaurantOutcome(OrderOutcome.ValidButNotDelivered, Optional.of(restaurants.get(restaurantIndex)));
            }
        } else {
            // not sure why my Multiple suppliers check doesn't work, so I catch it here instead
            return new pickupRestaurantOutcome(OrderOutcome.InvalidPizzaCombinationMultipleSuppliers, Optional.empty());
        }
    }


    // TODO: implement luhn algorithm
    private boolean luhnCheck(String cardNumber){
        return false;
    }

    private enum LogStatus{
        VALID_EXPIRY_DATE_PARSE_EXCEPTION
    }

}
