package br.com.achadoseperdidos.service;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

@Component
public class ImageProcessor {
    private static final int OUTPUT_WIDTH = 1200;
    private static final int OUTPUT_HEIGHT = 1000;
    private static final int MAX_INPUT_DIMENSION = 6000;

    public byte[] resizeAndCrop(byte[] input, String fitMode, double zoom) throws IOException {
        if (!"contain".equals(fitMode) && !"cover".equals(fitMode)) {
            throw new IllegalArgumentException("Escolha um ajuste de imagem válido.");
        }
        if (zoom < 1 || zoom > 2.5) {
            throw new IllegalArgumentException("O zoom deve estar entre 1 e 2,5.");
        }

        BufferedImage source = readImage(input);
        BufferedImage output = new BufferedImage(OUTPUT_WIDTH, OUTPUT_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = output.createGraphics();
        try {
            graphics.setColor(new Color(246, 247, 244));
            graphics.fillRect(0, 0, OUTPUT_WIDTH, OUTPUT_HEIGHT);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double baseScale = "cover".equals(fitMode)
                    ? Math.max((double) OUTPUT_WIDTH / source.getWidth(), (double) OUTPUT_HEIGHT / source.getHeight())
                    : Math.min((double) OUTPUT_WIDTH / source.getWidth(), (double) OUTPUT_HEIGHT / source.getHeight());
            double scale = baseScale * ("cover".equals(fitMode) ? zoom : 1);
            int width = (int) Math.round(source.getWidth() * scale);
            int height = (int) Math.round(source.getHeight() * scale);
            graphics.drawImage(source, (OUTPUT_WIDTH - width) / 2, (OUTPUT_HEIGHT - height) / 2, width, height, null);
        } finally {
            graphics.dispose();
            source.flush();
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (!ImageIO.write(output, "jpg", outputStream)) {
                throw new IOException("Não foi possível converter a imagem.");
            }
            return outputStream.toByteArray();
        } finally {
            output.flush();
        }
    }

    private BufferedImage readImage(byte[] input) throws IOException {
        try (ImageInputStream imageInput = ImageIO.createImageInputStream(new ByteArrayInputStream(input))) {
            if (imageInput == null) {
                throw new IllegalArgumentException("O arquivo não é uma imagem compatível.");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("Use uma imagem JPG ou PNG válida.");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width < 1 || height < 1 || width > MAX_INPUT_DIMENSION || height > MAX_INPUT_DIMENSION) {
                    throw new IllegalArgumentException("A imagem tem dimensões grandes demais.");
                }
                BufferedImage image = reader.read(0);
                if (image == null) {
                    throw new IllegalArgumentException("Não foi possível abrir essa imagem.");
                }
                return image;
            } finally {
                reader.dispose();
            }
        }
    }
}
