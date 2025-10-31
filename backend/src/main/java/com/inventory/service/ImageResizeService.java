package com.inventory.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 이미지 리사이징 서비스
 * - 원본 이미지와 등록용 최적화 이미지를 생성
 * - 번개장터 등록에 적합한 크기로 리사이징
 */
@Service
@Slf4j
public class ImageResizeService {

    // 번개장터 권장 이미지 크기
    private static final int REGISTRATION_MAX_WIDTH = 800;
    private static final int REGISTRATION_MAX_HEIGHT = 800;
    
    // 썸네일 크기
    private static final int THUMBNAIL_MAX_WIDTH = 200;
    private static final int THUMBNAIL_MAX_HEIGHT = 200;
    
    // 이미지 품질 (0.0 ~ 1.0)
    private static final float REGISTRATION_QUALITY = 0.85f;
    private static final float THUMBNAIL_QUALITY = 0.8f;
    private static final long REGISTRATION_MAX_BYTES = 2L * 1024 * 1024; // 2MB 상한

    /**
     * 원본 이미지 리사이징 (등록용)
     * - 번개장터 등록에 최적화된 크기로 리사이징
     * 
     * @param originalImageData 원본 이미지 바이트 배열
     * @param originalFormat 원본 이미지 포맷 (jpg, png 등)
     * @return 리사이징된 이미지 바이트 배열
     */
    public byte[] resizeForRegistration(byte[] originalImageData, String originalFormat) {
        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalImageData));
            if (originalImage == null) {
                log.warn("원본 이미지를 읽을 수 없습니다");
                return originalImageData;
            }

            // 이미지 크기 확인
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            log.info("원본 이미지 크기: {}x{}", originalWidth, originalHeight);

            // 비율 유지 리사이즈(긴 변 800px 이내)
            Dimension newSize = calculateResizeDimensions(originalWidth, originalHeight,
                REGISTRATION_MAX_WIDTH, REGISTRATION_MAX_HEIGHT);

            BufferedImage resizedImage = resizeImage(toRGB(originalImage), newSize.width, newSize.height);

            // JPEG로 인코딩(메타 제거) + 용량 2MB 이하 품질 조정
            byte[] encoded = encodeJpegWithTargetSize(resizedImage, REGISTRATION_QUALITY, REGISTRATION_MAX_BYTES);

            log.info("등록용 이미지 생성 완료: {}x{} -> {}x{}, 최종 크기: {} bytes",
                originalWidth, originalHeight, newSize.width, newSize.height, encoded.length);

            return encoded;
            
        } catch (IOException e) {
            log.error("이미지 리사이징 실패: {}", e.getMessage(), e);
            return originalImageData; // 실패 시 원본 반환
        }
    }

    /**
     * 썸네일 이미지 생성
     * - 목록 표시용 작은 이미지
     * 
     * @param originalImageData 원본 이미지 바이트 배열
     * @param originalFormat 원본 이미지 포맷
     * @return 썸네일 이미지 바이트 배열
     */
    public byte[] createThumbnail(byte[] originalImageData, String originalFormat) {
        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalImageData));
            if (originalImage == null) {
                log.warn("원본 이미지를 읽을 수 없습니다");
                return originalImageData;
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            // 비율을 유지하면서 썸네일 크기로 리사이징
            Dimension thumbnailSize = calculateResizeDimensions(originalWidth, originalHeight, 
                THUMBNAIL_MAX_WIDTH, THUMBNAIL_MAX_HEIGHT);
            
            BufferedImage thumbnailImage = resizeImage(toRGB(originalImage), thumbnailSize.width, thumbnailSize.height);

            // JPEG로 인코딩(간단 품질)
            byte[] thumbnailData = encodeJpegWithTargetSize(thumbnailImage, THUMBNAIL_QUALITY, 256 * 1024);
            log.info("썸네일 이미지 생성 완료: {}x{} -> {}x{}, 크기: {} bytes", 
                originalWidth, originalHeight, thumbnailSize.width, thumbnailSize.height, thumbnailData.length);
            
            return thumbnailData;
            
        } catch (IOException e) {
            log.error("썸네일 생성 실패: {}", e.getMessage(), e);
            return originalImageData; // 실패 시 원본 반환
        }
    }

    /**
     * 리사이징할 새로운 크기 계산
     * - 비율을 유지하면서 최대 크기 내에서 계산
     */
    private Dimension calculateResizeDimensions(int originalWidth, int originalHeight, 
                                             int maxWidth, int maxHeight) {
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);
        
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);
        
        return new Dimension(newWidth, newHeight);
    }

    /**
     * 이미지 리사이징 실행
     * - 고품질 리샘플링 사용
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2d = resizedImage.createGraphics();
        
        // 고품질 렌더링 설정
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        
        return resizedImage;
    }

    private BufferedImage toRGB(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_RGB) return src;
        BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return rgb;
    }

    private byte[] encodeJpegWithTargetSize(BufferedImage image, float startQuality, long maxBytes) throws IOException {
        float q = startQuality;
        byte[] out = encodeJpeg(image, q);
        int steps = 0;
        while (out.length > maxBytes && q > 0.6f && steps < 5) {
            q -= 0.05f; // 품질 단계적으로 낮춤
            out = encodeJpeg(image, q);
            steps++;
        }
        return out;
    }

    private byte[] encodeJpeg(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        writer.setOutput(ios);
        javax.imageio.plugins.jpeg.JPEGImageWriteParam jpegParams = new javax.imageio.plugins.jpeg.JPEGImageWriteParam(null);
        jpegParams.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
        jpegParams.setCompressionQuality(quality);
        writer.write(null, new javax.imageio.IIOImage(image, null, null), jpegParams);
        writer.dispose();
        ios.close();
        return baos.toByteArray();
    }

    /**
     * 이미지 메타데이터 추출
     */
    public ImageMetadata extractMetadata(byte[] imageData) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            if (image != null) {
                return new ImageMetadata(
                    image.getWidth(),
                    image.getHeight(),
                    image.getType(),
                    imageData.length
                );
            }
        } catch (IOException e) {
            log.error("이미지 메타데이터 추출 실패: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 이미지 메타데이터 클래스
     */
    public static class ImageMetadata {
        public final int width;
        public final int height;
        public final int type;
        public final long size;

        public ImageMetadata(int width, int height, int type, long size) {
            this.width = width;
            this.height = height;
            this.type = type;
            this.size = size;
        }
    }
}
