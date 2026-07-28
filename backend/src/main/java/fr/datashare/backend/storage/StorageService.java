package fr.datashare.backend.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String store(MultipartFile file);

    Resource load(String storageName);

    void delete(String storageName);
}
