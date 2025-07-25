package id.go.fimo.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Dizzay on 12/28/2017.
 */

public class StringUtils {

    public static final boolean isNullOrEmpty(String value){
        if(value == null || value.isEmpty() || value.equals("") || value.equals("-")){
            return true;
        }else {
            return false;
        }
    }


    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean isEmailValid(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }

    public static boolean isPasswordValid(String password) {

        String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,}$";

        /**if (isDigit) PASSWORD_PATTERN += "(?=.*\\d)";
         if (isCase) PASSWORD_PATTERN += "(?=.*[a-z])";
         PASSWORD_PATTERN += ".{" + minLength + "," + maxLength + "}";
         PASSWORD_PATTERN = "(" + PASSWORD_PATTERN + ")";*/

        //Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        //Matcher matcher = pattern.matcher(password);
        //return matcher.matches();

        return password.length()>=6;
    }

    public static final List<String> PHONE_NUMBER_PREFIXES = Arrays.asList("0811", "0812", "0813", "0821", "0822", "0823", "0851", "0852", "0853",
            "0855", "0856", "0857", "0858", "0814", "0815", "0816",
            "0817", "0818", "0819", "0859", "0877", "0878", "0831", "0832", "0838",
            "0895", "0896", "0897", "0898", "0899",
            "0881", "0882", "0883", "0884", "0885", "0886", "0887", "0888", "0889");


    public static final List<String> PHONENUMBER_PREFIXES = Arrays.asList("62811", "62812", "62813", "62821", "62822", "62823", "62851", "62852", "62853",
            "62855", "62856", "62857", "62858", "62814", "62815", "62816",
            "62817", "62818", "62819", "62859", "62877", "62878", "62831", "62832", "62838",
            "62895", "62896", "62897", "62898", "62899",
            "62881", "62882", "62883", "62884", "62885", "62886", "62887", "62888", "62889");

    public static boolean isMobilePhoneNumberValid(String phoneNumber){
        if (phoneNumber.length()>=10) {
            return PHONE_NUMBER_PREFIXES.contains(phoneNumber.substring(0, 4)) || PHONENUMBER_PREFIXES.contains(phoneNumber.substring(0, 5));
        }
        return false;
    }
}