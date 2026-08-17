package com.commencis.interview.hooks;

import com.commencis.interview.core.report.AllureEnvironment;
import io.cucumber.java.Before;

/** Rapor metadata'si. Tum senaryolar icin gecerli, kosum basina bir kez yazar. */
public class ReportHooks {

    @Before(order = 0)
    public void writeReportEnvironment() {
        AllureEnvironment.writeOnce();
    }
}
