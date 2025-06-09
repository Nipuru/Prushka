package server.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;

/**
 * 文件资源工具类
 * @author Nipuru
 * @since 2024/10/24 13:08
 */
public class ResourceUtil {

    /**
     * 将 jar 包内的某个文件提取到 jar 包所在目录
     */
    public static InputStream getResourceOrExtract(String name) throws Exception {
        URI uri = ResourceUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        File parentFile = new File(uri.getPath()).getParentFile();
        File destFile = new File(parentFile, name);
        if (!destFile.exists()) {
            try(InputStream inputStream = ResourceUtil.class.getClassLoader().getResourceAsStream(name)) {
                if (inputStream == null) {
                    throw new FileNotFoundException(name);
                }

                Files.copy(inputStream, destFile.toPath());
            }
        }
        return new FileInputStream(destFile);
    }
}
