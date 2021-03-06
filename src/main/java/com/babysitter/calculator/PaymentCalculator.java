package com.babysitter.calculator;

import com.babysitter.enums.FamilyEnum;
import com.babysitter.exception.InvalidTimeFormatException;
import com.babysitter.service.TimeAndPayService;
import com.babysitter.utils.DateUtil;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.babysitter.constants.Constants.*;

public class PaymentCalculator {

    TimeAndPayService timeAndPayService;

    public PaymentCalculator() {
        timeAndPayService = new TimeAndPayService();
    }


    public int calculate(String startTime, String endTime, String family) throws InvalidTimeFormatException {
        int totalPay = 0;
        if (validateTimesAndFamily(startTime, endTime, family)) {
            LocalDateTime startDateTime = DateUtil.convertStringIntoLocalDateTime(startTime);
            LocalDateTime endDateTime = DateUtil.convertStringIntoLocalDateTime(endTime);
            if (areTimesValid(startDateTime, endDateTime)) {
                totalPay = calculateTotalPay(startDateTime, endDateTime, family);
            }
        }
        return totalPay;
    }

    private int calculateTotalPay(LocalDateTime startDateTime, LocalDateTime endDateTime, String family) {
        int totalPay = 0;
        if (FamilyEnum.A.toString().equals(family)) {
            LocalDate limitDate = getDayForLimitDateTime(startDateTime).toLocalDate();
            LocalTime limitTime = LocalTime.parse(FAMILY_A_ELEVEN_PM_LIMIT);
            LocalDateTime limitDateTime = LocalDateTime.of(limitDate, limitTime);

            totalPay = timeAndPayService.getTotalPayForSingleTimeLimit(startDateTime, endDateTime, limitDateTime, FAMILY_A_PAY_PER_HOUR_BEFORE_LIMIT, FAMILY_A_PAY_PER_HOUR_AFTER_LIMIT);
        }
        if (FamilyEnum.B.toString().equals(family)) {
            LocalDate firstLimitDate = getDayForLimitDateTime(startDateTime).toLocalDate();
            LocalDate secondLimitDate = getDayForMidnightLimitDateTime(startDateTime).toLocalDate();
            LocalDateTime firstLimitDateTime = LocalDateTime.of(firstLimitDate, LocalTime.parse(FAMILY_B_TEN_PM_LIMIT));
            LocalDateTime secondLimitDateTime = LocalDateTime.of(secondLimitDate, LocalTime.parse(FAMILY_B_MIDNIGHT_LIMIT));

            totalPay = timeAndPayService.getTotalPayForDoubleTimeLimit(startDateTime, endDateTime, firstLimitDateTime, secondLimitDateTime, FAMILY_B_PAY_PER_HOUR_BEFORE_FIRST_LIMIT, FAMILY_B_PAY_PER_HOUR_BETWEEN_LIMITS, FAMILY_B_PAY_PER_HOUR_AFTER_SECOND_LIMIT);
        }

        if (FamilyEnum.C.toString().equals(family)) {
            LocalDate limitDate = getDayForLimitDateTime(startDateTime).toLocalDate();
            LocalTime limitTime = LocalTime.parse(FAMILY_C_NINE_PM_LIMIT);
            LocalDateTime limitDateTime = LocalDateTime.of(limitDate, limitTime);

            totalPay = timeAndPayService.getTotalPayForSingleTimeLimit(startDateTime, endDateTime, limitDateTime, FAMILY_C_PAY_PER_HOUR_BEFORE_LIMIT, FAMILY_C_PAY_PER_HOUR_AFTER_LIMIT);
        }
        return totalPay;
    }

    private Boolean validateTimesAndFamily(String startTime, String endTime, String family) {
        Boolean areInputsValid = Boolean.TRUE;

        if (!areAllInputsNotBlank(startTime, endTime, family) || !isFamilyValid(family)) {
            areInputsValid = Boolean.FALSE;
        }
        return areInputsValid;
    }

    private Boolean areAllInputsNotBlank(String startTime, String endTime, String family) {
        Boolean areAllInputsNotBlank = Boolean.TRUE;

        if (StringUtils.isBlank(startTime)) {
            System.out.println("Start time cannot be blank!");
            areAllInputsNotBlank = Boolean.FALSE;
        }
        if (StringUtils.isBlank(endTime)) {
            System.out.println("End time cannot be blank!");
            areAllInputsNotBlank = Boolean.FALSE;
        }
        if (StringUtils.isBlank(family)) {
            System.out.println("Family cannot be blank!");
            areAllInputsNotBlank = Boolean.FALSE;
        }
        return areAllInputsNotBlank;
    }

    private Boolean isFamilyValid(String family) {
        Boolean isFamilyValid = Boolean.FALSE;

        for (FamilyEnum familyName : FamilyEnum.values()) {
            if (familyName.toString().equals(family)) {
                isFamilyValid = Boolean.TRUE;
            }
        }
        if (!isFamilyValid) {
            System.out.println("The family is not valid!");
        }
        return isFamilyValid;
    }

    private Boolean areTimesValid(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Boolean areTimesValid = Boolean.TRUE;
        if (!isStartTimeBeforeOrEqualToEndTime(startDateTime, endDateTime) || !areTimesWithinWorkableHours(startDateTime, endDateTime)) {
            areTimesValid = Boolean.FALSE;
        }
        return areTimesValid;
    }

    private Boolean isStartTimeBeforeOrEqualToEndTime(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Boolean isStartTimeBeforeOrEqualToEndTime = Boolean.FALSE;
        if (startDateTime.isBefore(endDateTime) || startDateTime.isEqual(endDateTime)) {
            isStartTimeBeforeOrEqualToEndTime = Boolean.TRUE;
        }

        if (!isStartTimeBeforeOrEqualToEndTime) {
            System.out.println("End time cannot be before the start time!");
        }
        return isStartTimeBeforeOrEqualToEndTime;
    }

    private Boolean areTimesWithinWorkableHours(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Boolean areTimesWithinWorkableHours = Boolean.FALSE;
        LocalDateTime allowedEndStartDateTime;

        if (startDateTime.getHour() >= START_TIME_HOUR) {
            allowedEndStartDateTime = LocalDateTime.of(startDateTime.plusDays(1).toLocalDate(), LocalTime.parse(END_TIME));
            if (endDateTime.isEqual(allowedEndStartDateTime) || endDateTime.isBefore(allowedEndStartDateTime)) {
                areTimesWithinWorkableHours = Boolean.TRUE;
            }
        } else if ((startDateTime.getHour() < END_TIME_HOUR)
                || ((startDateTime.getHour() == END_TIME_HOUR) && (startDateTime.getMinute() == ZERO_MINUTE))) {
            allowedEndStartDateTime = LocalDateTime.of(startDateTime.toLocalDate(), LocalTime.parse(END_TIME));
            if (endDateTime.isEqual(allowedEndStartDateTime) || endDateTime.isBefore(allowedEndStartDateTime)) {
                areTimesWithinWorkableHours = Boolean.TRUE;
            }
        }

        if (!areTimesWithinWorkableHours) {
            System.out.println("The Start Time or End Time are not within the Allowed Workable Hours! The Allowed Workable Hours are from 5:00PM to 4:00 AM inclusive.");
        }

        return areTimesWithinWorkableHours;
    }

    private LocalDateTime getDayForLimitDateTime(LocalDateTime startDateTime) {
        if (startDateTime.getHour() <= END_TIME_HOUR) {
            return startDateTime.minusDays(1);
        } else {
            return startDateTime;
        }
    }

    private LocalDateTime getDayForMidnightLimitDateTime(LocalDateTime startDateTime) {
        if (startDateTime.getHour() <= END_TIME_HOUR) {
            return startDateTime;
        } else {
            return startDateTime.plusDays(1);
        }
    }


}
