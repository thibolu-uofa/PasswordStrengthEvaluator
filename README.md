This is a basic toy program to showcase JUnit for AUCSC 325.
This is not a real program, it doesn't really tell you how strong your password is and there are intentional (and non-intentional) bugs in it. 

The code was made entirely with Code Claude agent and not representative of a high quality and well documented code source.

The pom.xml contains the lines for the JaCoCO and PIT maven plugins that we will use in class.

The test cases are in 
[PasswordStrengthEvaluator/blob/main/src/test/java/com/example/password/PasswordEvaluatorTest.java
](https://github.com/thibolu-uofa/PasswordStrengthEvaluator/blob/main/src/test/java/com/example/password/PasswordEvaluatorTest.java)

It include a setup function (with annotation @BeforeEach) and several test cases (with annotation @Test) using different types of assertions. Each test case should have at least one assertion.
