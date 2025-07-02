package ca.concordia.smartsortandroidapp;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import org.tensorflow.lite.Interpreter;
import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.util.*;

public class ImageClassifier {
    private final Interpreter interpreter;
    private final List<String> labels;
    private final int imageSize = 224;

    public ImageClassifier(AssetManager assetManager) throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        interpreter = new Interpreter(loadModelFile(assetManager, "model_unquant.tflite"), options);
        labels = loadLabels(assetManager, "labels.txt");
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabels(AssetManager assetManager, String labelPath) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        List<String> result = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) result.add(line);
        reader.close();
        return result;
    }

    public String classify(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true);
        ByteBuffer inputBuffer = convertBitmapToByteBuffer(resized);

        float[][] output = new float[1][labels.size()];
        interpreter.run(inputBuffer, output);

        int maxIdx = 0;
        float maxProb = 0;
        for (int i = 0; i < labels.size(); i++) {
            if (output[0][i] > maxProb) {
                maxProb = output[0][i];
                maxIdx = i;
            }
        }


        if (maxProb < 0.70f) {
            return "2 Others";
        }

        return labels.get(maxIdx);
    }
    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
        buffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[imageSize * imageSize];
        bitmap.getPixels(intValues, 0, imageSize, 0, 0, imageSize, imageSize);

        for (int pixelValue : intValues) {
            buffer.putFloat(((pixelValue >> 16) & 0xFF) / 255.0f);
            buffer.putFloat(((pixelValue >> 8) & 0xFF) / 255.0f);
            buffer.putFloat((pixelValue & 0xFF) / 255.0f);
        }

        return buffer;
    }
}

