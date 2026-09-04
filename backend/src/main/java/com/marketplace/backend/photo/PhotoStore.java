package com.marketplace.backend.photo;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface PhotoStore {

    String store(MultipartFile photo);

    Resource retrieve(String reference);
}
