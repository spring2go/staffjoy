package xyz.staffjoy.web.config;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Component
public class AssetLoader {

    private String imageBase64;

    static final String IMAGE_FILE_PATH = "static/assets/images/staffjoy_coffee.png";

    public String getImageBase64() {
        return this.imageBase64;
    }

    @PostConstruct
    public void init() throws IOException {

        // load image
        InputStream imageFileInputStream = this.getImageFile();
        byte[] encodedImage = IOUtils.toByteArray(imageFileInputStream);
        byte[] base64EncodedImage = Base64Utils.encode(encodedImage);
        imageBase64 = new String(base64EncodedImage);
    }

    private InputStream getImageFile() throws IOException {
        return new ClassPathResource(IMAGE_FILE_PATH).getInputStream();
    }
}
