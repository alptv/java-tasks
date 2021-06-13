package ru.ifmo.rain.laptev.bank.test;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import ru.ifmo.rain.laptev.bank.server.Server;
import ru.ifmo.rain.laptev.bank.server.ServerException;
import ru.ifmo.rain.laptev.bank.common.Bank;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class BankTestUtils {
    static final String[] NAMES = new String[]{"G", "string", "****|||###", "\'\\\"shielding", "n".repeat(10000)};
    static final String[] SURNAMES = new String[]{"K", "str", "****|||###", "\'\\\"shielding", "s".repeat(100000)};
    static final int[] PASSPORTS = new int[]{1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE, 0};
    static final String[] ACCOUNTS = new String[]{"id1", "stdout", "****|||###", "\'\\\"", "a".repeat(2000)};
    static final int[] AMOUNTS = new int[]{1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE, 0};
    static final int DATA_ARRAYS_LENGTH = 5;
    static final String NULL_STRING = "null";
    private static final int STANDARD_RMI_PORT = 1099;
    private static final String BANK_ADDRESS = "//localhost/bank";

    private BankTestUtils() {
        throw new UnsupportedOperationException("Instance of class 'BankTestUtils' is unsupported");
    }

    static Registry startRMI() {
        try {
            return LocateRegistry.createRegistry(STANDARD_RMI_PORT);
        } catch (RemoteException ignored) {
            //Already created
            try {
                return LocateRegistry.getRegistry();
            } catch (RemoteException e) {
                throw new AssertionError("Can't get rmi registry on port: " + STANDARD_RMI_PORT, e);
            }
        }
    }

    static Bank newBank(final Registry registry) throws ServerException {
        Server.main(null);
        try {
            return (Bank) registry.lookup(BANK_ADDRESS);
        } catch (RemoteException e) {
            throw new AssertionError("Can't lookup for a bank", e);
        } catch (NotBoundException e) {
            throw new AssertionError("Bank is not bound", e);
        }
    }

    static boolean runTests(final Class<?> testClass) {
        final LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectClass(testClass))
                .build();
        final Launcher launcher = LauncherFactory.create();
        final ResultPrintListener listener = new ResultPrintListener();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
        final TestExecutionSummary summary = listener.getSummary();
        final long passedTests = summary.getTestsSucceededCount();
        final long startedTests = summary.getTestsStartedCount();
        return passedTests == startedTests;
    }

    private static void write(final String message) {
        System.out.println(message);
    }

    private static class ResultPrintListener extends SummaryGeneratingListener {
        private static final String PASSED = "PASSED";
        private static final String FAILED = "FAILED";
        private static final String VINTAGE_ROOT = "JUnit Vintage";
        private static final String JUPITER_ROOT = "JUnit Jupiter";
        private static final String SEPARATOR = "=".repeat(30);

        @Override
        public void executionStarted(final TestIdentifier testIdentifier) {
            super.executionStarted(testIdentifier);
            final String testName = testIdentifier.getDisplayName();
            if (testIdentifier.isContainer() && !VINTAGE_ROOT.equals(testName) && !JUPITER_ROOT.equals(testName)) {
                write(SEPARATOR);
                write(testName);
            }

        }

        @Override
        public void executionFinished(final TestIdentifier testIdentifier, final TestExecutionResult testExecutionResult) {
            super.executionFinished(testIdentifier, testExecutionResult);
            if (testIdentifier.isTest()) {
                switch (testExecutionResult.getStatus()) {
                    case SUCCESSFUL:
                        write(getTestName(testIdentifier) + " - " + PASSED);
                        break;
                    case FAILED:
                        write(getTestName(testIdentifier) + " - " + FAILED);
                        break;
                    case ABORTED:
                        write(getTestName(testIdentifier) + " - " + FAILED);
                        break;
                    default:
                        break;
                }
            }
        }

        private String getTestName(final TestIdentifier testIdentifier) {
            final String testName = testIdentifier.getDisplayName();
            return testName.substring(0, Math.max(0, testName.length() - 2));
        }
    }
}
