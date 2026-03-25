import java.awt.*;
import javax.swing.*;

public class WaveformDisplay extends JPanel {
    private final String[] signal;
    private final boolean isManchesterLike;
    private final boolean isAMI;

    public WaveformDisplay(String encodedSignal) {
        this.signal = encodedSignal.trim().split("\\s+");
        this.isManchesterLike = encodedSignal.toLowerCase().contains("manchester");
        this.isAMI = encodedSignal.toLowerCase().contains("ami");
        setPreferredSize(new Dimension(1200, 400));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int x = 50;           
        int bitWidth = 60;     
        int midY = 200;
        int highY = midY - 80;
        int lowY = midY + 80;

        g.setColor(Color.BLACK);
        g.setFont(new Font("Consolas", Font.BOLD, 16));
        g.drawString("Digital Signal Output Waveform", 350, 40);
        g.setColor(Color.GRAY);
        g.drawLine(40, midY, 1150, midY);
        g.setColor(Color.BLUE);

        int prevY = midY;

        for (int i = 0; i < signal.length; i++) {
            int currentY = midY;

            if (signal[i].contains("+")) currentY = highY;
            else if (signal[i].contains("-")) currentY = lowY;

            // --- Manchester-like Encoding ---
            if (isManchesterLike) {
                // Split bit into two halves: + then -, or - then +
                int half = bitWidth / 2;
                int firstY = currentY;
                int secondY = (currentY == highY) ? lowY : highY;

                // Transition to first level
                if (i > 0 && prevY != firstY)
                    g.drawLine(x, prevY, x, firstY);

                // Draw first half
                g.drawLine(x, firstY, x + half, firstY);

                // Mid-bit transition
                g.drawLine(x + half, firstY, x + half, secondY);

                // Second half
                g.drawLine(x + half, secondY, x + bitWidth, secondY);

                prevY = secondY;
            }

            // --- AMI Encoding ---
            else if (isAMI) {
                // Pulse only for +V or -V, then return to zero
                if (!signal[i].equals("0")) {
                    // up from zero
                    g.drawLine(x, midY, x, currentY);
                    g.drawLine(x, currentY, x + bitWidth / 2, currentY);
                    // return to zero
                    g.drawLine(x + bitWidth / 2, currentY, x + bitWidth / 2, midY);
                }
                g.drawLine(x + bitWidth / 2, midY, x + bitWidth, midY);
                prevY = midY;
            }

            // --- Normal NRZ or Scrambled encoding ---
            else {
                if (i > 0 && currentY != prevY)
                    g.drawLine(x, prevY, x, currentY);
                g.drawLine(x, currentY, x + bitWidth, currentY);
                prevY = currentY;
            }

            // Label
            g.setColor(Color.DARK_GRAY);
            g.drawString(signal[i], x + 15, midY + 100);
            g.setColor(Color.BLUE);

            x += bitWidth;
        }

        g.setColor(Color.BLACK);
        g.drawString("+V", 10, highY);
        g.drawString("0", 10, midY);
        g.drawString("-V", 10, lowY);
    }

    public static void showWaveform(String encodedSignal) {
        JFrame frame = new JFrame("Digital Signal Output");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new WaveformDisplay(encodedSignal));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
