package com.onecart.onecartApp.controller;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.onecart.onecartApp.model.Order;
import com.onecart.onecartApp.model.Product;
import com.onecart.onecartApp.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

@Controller
@RequestMapping("admin/sales")
public class SalesController {

    @Autowired
    OrderService orderService;

    @GetMapping("/download-csv")
    @ResponseBody
    public void downloadCSVSalesReport(HttpServletResponse response) throws IOException {
        // Generate sales data (replace this with your actual data retrieval logic)
        List<Order> orders = orderService.getAllOrders();

        // Create a CSV file with the sales data
        File csvFile = createCsvFile(orders);

        // Configure the response to allow file download
        response.setHeader("Content-Disposition", "attachment; filename=sales_report.csv");
        response.setContentType("text/csv");

        // Copy the file content to the response output stream
        copyFileToResponse(csvFile, response);
    }

    @GetMapping("/download-pdf")
    @ResponseBody
    public void downloadPdfSalesReport(HttpServletResponse response) throws IOException {
        // Generate PDF sales report using iText
        byte[] pdfData = generatePdfReport();

        // Configure the response to allow PDF file download
        response.setHeader("Content-Disposition", "attachment; filename=sales_report.pdf");
        response.setContentType("application/pdf");

        // Copy the PDF data to the response output stream
        try (OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(pdfData);
        } catch (IOException e) {
            // Handle file download error
            e.printStackTrace();
        }
    }

    private File createCsvFile(List<Order> orders) throws IOException {
        File csvFile = new File("sales_report.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {

            double totalSalesAmount = orders.stream()
                    .mapToDouble(Order::getTotalAmount)
                    .sum();
            writer.append("OneCart Sales Report\n\n");
            writer.append("Total Sales Amount: " + totalSalesAmount);

            // Write CSV header
            writer.append("\n\nOrder ID,User,Product,Order Date,Total Amount,Payment Method\n\n");

            // Write order data
            for (Order order : orders) {
                writer.append(order.getId() + ",");
//                writer.append(order.getUser().getUsername() + ",");
                for (Product product : order.getProducts()) {
                    writer.append(product.getName() + ",");
                }
                writer.append(order.getFormattedOrderDate() + ",");
                writer.append(order.getTotalAmount() + ",");
                writer.append(order.getPaymentMethod() + "\n");
            }
        }
        return csvFile;
    }

    private byte[] generatePdfReport() throws IOException {
        // Create a new document
        Document document = new Document();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer;
        try {
            // Initialize PDF writer
            writer = PdfWriter.getInstance(document, baos);

            // Open the document
            document.open();

            // Add content to the PDF
            document.add(new Paragraph("OneCart Sales Report\n\n"));

            // Retrieve orders from your database
            List<Order> orders = orderService.getAllOrders();

            double totalSalesAmount = orders.stream()
                    .mapToDouble(Order::getTotalAmount)
                    .sum();
            document.add(new Paragraph("Total Sales Amount: " + totalSalesAmount));
            document.add(new Paragraph("\n"));

            // Loop through the orders and add their details to the PDF
            for (Order order : orders) {
                // Add order details to the PDF (customize this as needed)
                document.add(new Paragraph("Order ID: " + order.getId()));
//                document.add(new Paragraph("User: " + order.getUser().getUsername()));
                for (Product product : order.getProducts()) {
                    document.add(new Paragraph("Product: " + product.getName()));
                }
                document.add(new Paragraph("Order Date: " + order.getFormattedOrderDate()));
                document.add(new Paragraph("Total Amount: " + order.getTotalAmount()));
                document.add(new Paragraph("Payment Method: " + order.getPaymentMethod()));
                document.add(new Paragraph("\n")); // Add some spacing between orders
            }

            // Close the document
            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    private void copyFileToResponse(File file, HttpServletResponse response) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file);
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            // Handle file download error
            e.printStackTrace();
        }
    }
}
