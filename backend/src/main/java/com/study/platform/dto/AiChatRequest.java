package com.study.platform.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AiChatRequest {
    private List<Map<String, String>> messages;
}
