package com.leafall.yourtaxi.dto.generatedFiles;

import lombok.Data;
import org.springframework.core.io.Resource;

@Data
public class DownloadFileResponseDto {
    private Resource resource;
    private String filename;
}
