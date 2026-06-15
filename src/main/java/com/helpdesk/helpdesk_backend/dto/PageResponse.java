package com.helpdesk.helpdesk_backend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> data;
    private long total;
    private int page;
    private int totalPages;
}
