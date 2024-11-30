package com.faitoncodes.core_processor_service.util;

import ch.qos.logback.core.util.StringUtil;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

public class DateFormatterUtil {

    public static String extractDateFormUpdatedDateAllExercises(LocalDateTime dateTime){
        StringBuilder finalFormattedDate = new StringBuilder();

        return finalFormattedDate
                .append(dateTime.getDayOfMonth()).append(" de ")
                .append(StringUtil.capitalizeFirstLetter(dateTime.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"))))
                .append(" de ")
                .append(dateTime.getYear())
                .toString();
    }

    public static String extractDateFormUpdatedDateForGetExerciseId(LocalDateTime dateTime){
        StringBuilder finalFormattedDate = new StringBuilder();
        StringBuilder minuteFormatted = new StringBuilder();

        if(dateTime.getMinute() >=0 && dateTime.getMinute() <=9 ){
            minuteFormatted.append("0");
        }
        minuteFormatted.append(dateTime.getMinute());

        return finalFormattedDate
                .append(dateTime.getHour())
                .append(":")
                .append(minuteFormatted.toString())
                .append("h, ")
                .append(dateTime.getDayOfMonth())
                .append("/")
                .append(dateTime.getMonth().getValue())
                .append("/")
                .append(dateTime.getYear())
                .toString();

    }

    public static String extractDateFormDueDate(LocalDateTime dateTime){
        StringBuilder finalFormattedDate = new StringBuilder();
        return finalFormattedDate
                .append(dateTime.getDayOfMonth())
                .append("/")
                .append(dateTime.getMonth().getValue())
                .append("/")
                .append(dateTime.getYear())
                .toString();
    }

    public static String extractDateModalDueDate(LocalDateTime dateTime){
        StringBuilder minuteFormatted = new StringBuilder();

        if(dateTime.getMinute() >=0 && dateTime.getMinute() <=9 ){
            minuteFormatted.append("0");
        }
        minuteFormatted.append(dateTime.getMinute());

        StringBuilder finalFormattedDate = new StringBuilder();
        return finalFormattedDate
                .append(dateTime.getYear())
                .append("-")
                .append(dateTime.getMonth().getValue())
                .append("-")
                .append(dateTime.getDayOfMonth())
                .append(" ")
                .append(dateTime.getHour())
                .append(":")
                .append(minuteFormatted.toString())
                .toString();

    }
}
