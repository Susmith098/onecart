package com.onecart.onecartApp.util;

import com.onecart.onecartApp.model.Order;
import com.onecart.onecartApp.model.OrderItem;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;

@Component
public class InvoiceGenerator {

    public void generateInvoice(Order order, OutputStream outputStream) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);

                // Set the initial y-coordinate for the content
                float y = page.getMediaBox().getHeight() - 50;

                // Add the order details
                contentStream.beginText();
                contentStream.newLineAtOffset(50, y);
                contentStream.showText("OneCart | Invoice");
                contentStream.newLineAtOffset(0, -20); // Move down 20 points
                contentStream.showText("Invoice for Order #" + order.getId());
                contentStream.newLineAtOffset(0, -20); // Move down 20 points
                contentStream.showText("Order Date: " + order.getFormattedOrderDate());
                contentStream.newLineAtOffset(0, -20); // Move down 20 points
                contentStream.showText("Order Time: " + order.getFormattedOrderTime());
                contentStream.newLineAtOffset(0, -20); // Move down 20 points
                contentStream.showText("Total Amount: " + order.getTotalAmount());
                contentStream.newLineAtOffset(0, -20); // Move down 20 points
                contentStream.endText();

                // You can add more order details here as needed, adjusting the y-coordinate accordingly.

                // Add a table for displaying order items
                float tableY = y - 100; // Adjust the starting y-coordinate for the table
                float margin = 50;
                float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
                float yPosition = tableY;
                int rows = order.getOrderItems().size() + 1; // Include header row
                float rowHeight = 20f;
                float tableHeight = rowHeight * rows;
                float colWidth = tableWidth / (float) 2; // 2 columns in the table

                // Draw table headers
                float tableXPosition = margin;
                float textXPosition = tableXPosition + 4;
                float textYPosition = yPosition - 15;
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(textXPosition, textYPosition);
                contentStream.showText("Product Name");
                contentStream.newLineAtOffset(colWidth, 0);
                contentStream.showText("Quantity");
                contentStream.endText();
                yPosition -= rowHeight;

                // Draw table content
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                for (OrderItem item : order.getOrderItems()) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(textXPosition, yPosition - rowHeight);
                    contentStream.showText(item.getProduct().getName());
                    contentStream.newLineAtOffset(colWidth, 0);
                    contentStream.showText(String.valueOf(item.getQuantity()));
                    contentStream.endText();
                    yPosition -= rowHeight;
                }

                // Draw table borders
                contentStream.setLineWidth(1f);
                contentStream.moveTo(tableXPosition, tableY);
                contentStream.lineTo(tableXPosition, tableY - tableHeight);
                contentStream.lineTo(tableXPosition + tableWidth, tableY - tableHeight);
                contentStream.lineTo(tableXPosition + tableWidth, tableY);
                contentStream.closePath();
                contentStream.stroke();
            }

            document.save(outputStream);
        }
    }
}
