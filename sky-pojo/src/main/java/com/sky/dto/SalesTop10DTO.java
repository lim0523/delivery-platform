package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SalesTop10DTO {
    private LocalDateTime beginDate;

    private LocalDateTime endDate;

    private Integer status;

}
