import javax.sound.sampled.*;
import java.io.*;

public class SoundAnalyzer {
    static final long RECORD_TIME = 60000;  // 1 minute
    File wavFile = new File("E:/Test/RecordAudio.wav");
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
    TargetDataLine line;

    AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 16; // Changed to 16 bits for better accuracy
        int channels = 1; // Changed to mono for simplicity
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
        return format;
    }

    void start() {
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                System.exit(0);
            }
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            System.out.println("Start capturing...");

            AudioInputStream ais = new AudioInputStream(line);

            System.out.println("Start recording...");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = ais.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            byte[] audioData = byteArrayOutputStream.toByteArray();

            // Calculate decibel level
            double rms = calculateRMS(audioData);
            double db = 20 * Math.log10(rms);

            System.out.println("Decibel level: " + db + " dB");

            AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(audioData), format, audioData.length / format.getFrameSize()), fileType, wavFile);

        } catch (LineUnavailableException | IOException ex) {
            ex.printStackTrace();
        }
    }

    void finish() {
        line.stop();
        line.close();
        System.out.println("Finished");
    }

    double calculateRMS(byte[] audioData) {
        double sum = 0.0;
        for (byte value : audioData) {
            sum += value * value;
        }
        double mean = sum / audioData.length;
        return Math.sqrt(mean);
    }

    public static void main(String[] args) {
        final SoundAnalyzer recorder = new SoundAnalyzer();

        Thread stopper = new Thread(() -> {
            try {
                Thread.sleep(RECORD_TIME);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            recorder.finish();
        });

        stopper.start();

        recorder.start();
    }
}
