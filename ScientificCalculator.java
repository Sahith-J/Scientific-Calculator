import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ScientificCalculator extends JFrame {
    private JTextField display;

    public ScientificCalculator() {
        setTitle("Scientific Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        display = new JTextField();
        display.setEditable(false);
        display.setFont(display.getFont().deriveFont(24f)); // Increase text size of the display
        mainPanel.add(display, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(7, 4, 10, 10)); // Increase button size and spacing

        // Create operator buttons
        String[] operators = {
                "sin", "cos", "tan", "log",
                "ln", "(", ")", "^",
                "C", "%", "\u2190", "/",
                "7", "8", "9", "x",
                "4", "5", "6", "-",
                "1", "2", "3", "+",
                "00", "0", ".", "="
        };

        for (String operator : operators) {
            JButton button = new JButton(operator);
            button.addActionListener(new OperatorButtonListener());
            button.setFont(button.getFont().deriveFont(20f)); // Increase text size of the buttons
            buttonPanel.add(button);
        }

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        add(mainPanel);
        pack();
        setSize(400, 500); // Increase the size of the calculator window
    }

    private class OperatorButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();
            String operator = button.getText();
            String expression = display.getText();

            switch (operator) {
                case "C":
                    display.setText("");
                    break;
                case "\u2190":
                    if (expression.length() > 0) {
                        display.setText(expression.substring(0, expression.length() - 1));
                    }
                    break;
                case "=":
                    try {
                        double result = evaluateExpression(expression);
                        display.setText(String.valueOf(result));
                    } catch (NumberFormatException ex) {
                        display.setText("Error");
                    }
                    break;
                default:
                    display.setText(display.getText() + operator);
                    break;
            }
        }
    }

    private double evaluateExpression(String expression) {
        try {
            return (double) new Object() {
                int pos = -1, ch;

                void nextChar() {
                    ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
                }

                boolean eat(int charToEat) {
                    while (ch == ' ')
                        nextChar();
                    if (ch == charToEat) {
                        nextChar();
                        return true;
                    }
                    return false;
                }

                double parse() {
                    nextChar();
                    double x = parseExpression();
                    if (pos < expression.length())
                        throw new RuntimeException("Unexpected: " + (char) ch);
                    return x;
                }

                double parseExpression() {
                    double x = parseTerm();
                    while (true) {
                        if (eat('+'))
                            x += parseTerm();
                        else if (eat('-'))
                            x -= parseTerm();
                        else
                            return x;
                    }
                }

                double parseTerm() {
                    double x = parseFactor();
                    while (true) {
                        if (eat('x'))
                            x *= parseFactor();
                        else if (eat('/'))
                            x /= parseFactor();
                        else if (eat('%')) // Add support for modulo operator
                            x %= parseFactor();
                        else
                            return x;
                    }
                }

                double parseFactor() {
                    if (eat('+'))
                        return parseFactor();
                    if (eat('-'))
                        return -parseFactor();

                    double x;
                    int startPos = this.pos;
                    if (eat('(')) {
                        x = parseExpression();
                        eat(')');
                    } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                        while ((ch >= '0' && ch <= '9') || ch == '.')
                            nextChar();
                        x = Double.parseDouble(expression.substring(startPos, this.pos));
                    } else if (ch >= 'a' && ch <= 'z') {
                        while (ch >= 'a' && ch <= 'z')
                            nextChar();
                        String func = expression.substring(startPos, this.pos);
                        x = parseFactor();
                        switch (func) {
                            case "sin":
                                x = Math.sin(Math.toRadians(x));
                                break;
                            case "cos":
                                x = Math.cos(Math.toRadians(x));
                                break;
                            case "tan":
                                x = Math.tan(Math.toRadians(x));
                                break;
                            case "log":
                                x = Math.log10(x);
                                break;
                            case "ln":
                                x = Math.log(x);
                                break;
                            case "!":
                                x = factorial((int) x);
                                break;
                            default:
                                throw new RuntimeException("Unknown function: " + func);
                        }
                    } else {
                        throw new RuntimeException("Unexpected: " + (char) ch);
                    }

                    if (eat('^'))
                        x = Math.pow(x, parseFactor());

                    return x;
                }

                int factorial(int n) {
                    int result = 1;
                    for (int i = 2; i <= n; i++) {
                        result *= i;
                    }
                    return result;
                }
            }.parse();
        } catch (RuntimeException e) {
            return Double.NaN;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ScientificCalculator calculator = new ScientificCalculator();
                calculator.setVisible(true);
            }
        });
    }
}
