package org.jboss.arquillian.warp.test;

import java.util.LinkedList;
import java.util.List;

import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;

public class TestResultStore {

    private List<TestResult> list = new LinkedList<TestResult>();

    public void pushResult(TestResult testResult) {
        list.add(testResult);
    }

    public TestResult getFirstFailed() {
        for (TestResult result : list) {
            if (result.getStatus() == Status.FAILED) {
                return result;
            }
        }
        return null;
    }
}
