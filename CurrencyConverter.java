import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;


public class CurrencyConverter extends JFrame {

    private static final String API_KEY = "515cfe481ac5d524f0522be7";
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/USD";

    private final Map<String, Double> exchangeRates = new HashMap<>();
    
    private final JComboBox<String> fromCurrencyComboBox;
    private final JComboBox<String> toCurrencyComboBox;
    private final JTextField amountField;
    private final JLabel resultLabel;
    private final JButton convertButton;

    private final Color primaryColor = new Color(75, 75, 95);
    private final Color secondaryColor = new Color(240, 240, 245);
    private final Color accentColor = new Color(69, 123, 157);
    private final Color textColor = new Color(230, 230, 230);
    private final Color resultPanelColor = new Color(25, 25, 45);

    public CurrencyConverter() {
        setTitle("Currency Converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(secondaryColor);
        setLayout(new BorderLayout(10, 10));

        JLabel headerLabel = new JLabel("Currency Converter", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 32));
        headerLabel.setForeground(primaryColor);
        headerLabel.setBorder(new EmptyBorder(20, 0, 20, 0));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(0, 20, 0, 20),
            BorderFactory.createTitledBorder("Conversion")
        ));
        mainPanel.setBackground(Color.WHITE);

        JPanel amountPanel = createInputPanel("Amount:");
        amountField = new JTextField(10);
        styleTextField(amountField);
        amountPanel.add(amountField);

        JPanel fromPanel = createInputPanel("From Currency:");
        fromCurrencyComboBox = new JComboBox<>();
        styleComboBox(fromCurrencyComboBox);
        fromPanel.add(fromCurrencyComboBox);

        JPanel swapPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        swapPanel.setBackground(Color.WHITE);
        JButton swapButton = new JButton("↑↓");
        swapButton.setFont(new Font("Arial", Font.BOLD, 20));
        swapButton.setToolTipText("Swap Currencies");
        swapButton.addActionListener(e -> swapCurrencies());
        swapPanel.add(swapButton);

        JPanel toPanel = createInputPanel("To Currency:");
        toCurrencyComboBox = new JComboBox<>();
        styleComboBox(toCurrencyComboBox);
        toPanel.add(toCurrencyComboBox);
        
        mainPanel.add(amountPanel);
        mainPanel.add(fromPanel);
        mainPanel.add(swapPanel);
        mainPanel.add(toPanel);
        
        convertButton = new JButton("Convert");
        styleButton(convertButton);
        convertButton.addActionListener(e -> performConversion());
        
        JPanel buttonContainer = new JPanel();
        buttonContainer.setBackground(Color.WHITE);
        buttonContainer.setBorder(new EmptyBorder(10, 0, 10, 0));
        buttonContainer.add(convertButton);
        mainPanel.add(buttonContainer);

        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(resultPanelColor);
        resultPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        resultLabel = new JLabel("Loading rates...", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 40));
        resultLabel.setForeground(Color.WHITE);
        resultPanel.add(resultLabel, BorderLayout.CENTER);
        
        amountField.addActionListener(e -> performConversion());

        add(headerLabel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(resultPanel, BorderLayout.SOUTH);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        setResizable(true);
        toggleUIState(false);
        fetchExchangeRates();
    }

    private void fetchExchangeRates() {
        SwingWorker<Map<String, Double>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Double> doInBackground() throws Exception {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException("API request failed. Status code: " + response.statusCode());
                }

                String jsonResponse = response.body();

                // Check if the API call was successful before parsing rates
                if (jsonResponse.contains("\"result\":\"error\"")) {
                    String errorType = "Unknown error";
                    int errorIndex = jsonResponse.indexOf("\"error-type\":\"");
                    if (errorIndex != -1) {
                        int closingQuoteIndex = jsonResponse.indexOf("\"", errorIndex + 14);
                        if (closingQuoteIndex != -1) {
                            errorType = jsonResponse.substring(errorIndex + 14, closingQuoteIndex);
                        }
                    }
                    throw new RuntimeException("API Error: " + errorType);
                }

                return parseRates(jsonResponse);
            }

            @Override
            protected void done() {
                try {
                    Map<String, Double> rates = get();
                    exchangeRates.clear();
                    exchangeRates.putAll(rates);

                    String[] currencies = rates.keySet().toArray(new String[0]);
                    Arrays.sort(currencies);

                    fromCurrencyComboBox.setModel(new DefaultComboBoxModel<>(currencies));
                    toCurrencyComboBox.setModel(new DefaultComboBoxModel<>(currencies));
                    
                    fromCurrencyComboBox.setSelectedItem("USD");
                    toCurrencyComboBox.setSelectedItem("INR");
                    resultLabel.setText("0.00");
                    toggleUIState(true);

                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage = "Could not fetch rates. Please check your internet connection.";
                    if (cause instanceof RuntimeException) {
                        errorMessage = cause.getMessage();
                    }
                    showError("Failed to Fetch Rates", errorMessage);
                    resultLabel.setText("Error!");
                }
            }
        };
        worker.execute();
    }

    private Map<String, Double> parseRates(String jsonResponse) throws Exception {
        Map<String, Double> rates = new HashMap<>();
        
        int startIndex = jsonResponse.indexOf("\"conversion_rates\":{");
        if (startIndex == -1) {
            throw new Exception("Could not find 'conversion_rates' in API response.");
        }

        startIndex += 19;


        int endIndex = jsonResponse.indexOf("}", startIndex);
        if (endIndex == -1) {
            throw new Exception("Could not find closing brace for 'conversion_rates' in API response.");
        }


        String ratesObject = jsonResponse.substring(startIndex, endIndex);

        String[] ratesPairs = ratesObject.split(",");
        for (String pair : ratesPairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length != 2) {

                continue;
            }

            String currency = keyValue[0].replace("\"", "").trim();
            try {
                double rate = Double.parseDouble(keyValue[1].trim());
                rates.put(currency, rate);
            } catch (NumberFormatException e) {

                System.err.println("Could not parse rate for currency: " + currency);
            }
        }
        return rates;
    }

    private void performConversion() {
        String amountText = amountField.getText().trim();
        String fromCurrency = (String) fromCurrencyComboBox.getSelectedItem();
        String toCurrency = (String) toCurrencyComboBox.getSelectedItem();

        if (amountText.isEmpty()) {
            showError("Input Error", "Please enter an amount to convert.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount < 0) {
                showError("Input Error", "Amount cannot be negative.");
                return;
            }
        } catch (NumberFormatException ex) {
            showError("Input Error", "Invalid amount. Please enter numbers only.");
            return;
        }
        
        if (fromCurrency == null || toCurrency == null) {
            showError("Input Error", "Please select both 'from' and 'to' currencies.");
            return;
        }

        double fromRate = exchangeRates.get(fromCurrency);
        double toRate = exchangeRates.get(toCurrency);
        
        double amountInBase = amount / fromRate;
        double convertedAmount = amountInBase * toRate;

        resultLabel.setText(String.format("%.2f %s", convertedAmount, toCurrency));
    }


    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    private void toggleUIState(boolean enabled) {
        amountField.setEnabled(enabled);
        fromCurrencyComboBox.setEnabled(enabled);
        toCurrencyComboBox.setEnabled(enabled);
        convertButton.setEnabled(enabled);
    }

    private void swapCurrencies() {
        int fromIndex = fromCurrencyComboBox.getSelectedIndex();
        int toIndex = toCurrencyComboBox.getSelectedIndex();
        fromCurrencyComboBox.setSelectedIndex(toIndex);
        toCurrencyComboBox.setSelectedIndex(fromIndex);
    }

    private JPanel createInputPanel(String labelText) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        label.setPreferredSize(new Dimension(120, 30));
        panel.add(label);
        return panel;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            new EmptyBorder(5, 5, 5, 5)
        ));
    }

    private void styleComboBox(JComboBox<String> box) {
        box.setFont(new Font("Arial", Font.PLAIN, 16));
        box.setBackground(Color.WHITE);
        box.setPreferredSize(new Dimension(200, 35));
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(accentColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 30, 12, 30));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {

        }

        SwingUtilities.invokeLater(() -> new CurrencyConverter().setVisible(true));
    }
}

