package org.example.core.validator;

import java.time.LocalDate;

public interface DateRangeProvider {
    LocalDate fromDate();
    LocalDate toDate();
}
