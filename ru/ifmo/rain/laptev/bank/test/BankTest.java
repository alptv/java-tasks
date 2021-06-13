package ru.ifmo.rain.laptev.bank.test;

public class BankTest {
    public static void main(String[] args) {
        boolean serverTestPassed = BankTestUtils.runTests(ServerTest.class);
        boolean clientTestPassed = BankTestUtils.runTests(ClientTest.class);
        System.out.println("=".repeat(30));
        System.out.println("SERVER TESTS:  " + (serverTestPassed ? "PASSED" : "FAILED"));
        System.out.println("CLIENT TESTS: " + (clientTestPassed ? "PASSED" : "FAILED"));
        if (serverTestPassed && clientTestPassed) {
            System.exit(0);
        } else {
            System.exit(1);
        }
    }
}
